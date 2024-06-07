package com.eygraber.json.api

import com.eygraber.json.api.kmp.annotations.Relationship
import com.eygraber.json.api.kmp.annotations.Type
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

object JsonApiDocumentSerializer : KSerializer<JsonApiDocument> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("JsonApiDocument") {
    element<JsonObject>("data", isOptional = true)
    element<JsonArray>("errors", isOptional = true)
    element<JsonElement>("meta", isOptional = true)
    element<JsonElement>("jsonapi", isOptional = true)
    element<JsonObject>("links", isOptional = true)
    element<JsonArray>("included", isOptional = true)
  }

  override fun deserialize(decoder: Decoder): JsonApiDocument {
    val input = decoder as? JsonDecoder ?: throw SerializationException("Expected JsonInput for ${decoder::class}")
    val jsonObject = input.decodeJsonElement().jsonObject

    return when {
      "data" in jsonObject -> when (val data = jsonObject["data"]) {
        is JsonArray -> JsonApiDocument.Data.Array(
          data = data.map { input.json.decodeFromJsonElement(JsonApiResource.serializer(), it) },
          meta = jsonObject["meta"],
          jsonapi = jsonObject["jsonapi"]?.let { input.json.decodeFromJsonElement(JsonApiObject.serializer(), it) },
          links = jsonObject["links"]?.let {
            input.json.decodeFromJsonElement(
              JsonApiDocument.Data.Links.serializer(),
              it,
            )
          },
          included = jsonObject["included"]?.let {
            input.json.decodeFromJsonElement(
              ListSerializer(JsonApiResource.serializer()),
              it,
            )
          },
        )

        is JsonObject -> JsonApiDocument.Data.Object(
          data = input.json.decodeFromJsonElement(JsonApiResource.serializer(), data),
          meta = jsonObject["meta"],
          jsonapi = jsonObject["jsonapi"]?.let { input.json.decodeFromJsonElement(JsonApiObject.serializer(), it) },
          links = jsonObject["links"]?.let {
            input.json.decodeFromJsonElement(
              JsonApiDocument.Data.Links.serializer(),
              it,
            )
          },
          included = jsonObject["included"]?.let {
            input.json.decodeFromJsonElement(
              ListSerializer(JsonApiResource.serializer()),
              it,
            )
          },
        )

        else -> error("data must be an array or object")
      }

      "errors" in jsonObject -> JsonApiDocument.Errors(
        errors = input.json.decodeFromJsonElement(ListSerializer(JsonApiError.serializer()), jsonObject["errors"]!!),
        meta = jsonObject["meta"],
        jsonapi = jsonObject["jsonapi"]?.let { input.json.decodeFromJsonElement(JsonApiObject.serializer(), it) },
        links = jsonObject["links"]?.let { input.json.decodeFromJsonElement(JsonApiError.Links.serializer(), it) },
      )

      else -> throw SerializationException("Unknown JsonApiDocument type")
    }
  }

  override fun serialize(encoder: Encoder, value: JsonApiDocument) {
    val output = encoder as? JsonEncoder ?: throw SerializationException("Expected JsonOutput for ${encoder::class}")
    val jsonObject = buildJsonObject {
      when (value) {
        is JsonApiDocument.Data -> {
          when (value) {
            is JsonApiDocument.Data.Array -> value.data?.let { data ->
              put(
                "data",
                buildJsonArray {
                  data.forEach {
                    add(output.json.encodeToJsonElement(JsonApiResource.serializer(), it))
                  }
                },
              )
            }

            is JsonApiDocument.Data.Object -> value.data?.let {
              put("data", output.json.encodeToJsonElement(JsonApiResource.serializer(), it))
            }
          }
          value.meta?.let { put("meta", it) }
          value.jsonapi?.let { put("jsonapi", output.json.encodeToJsonElement(JsonApiObject.serializer(), it)) }
          value.links?.let {
            put(
              "links",
              output.json.encodeToJsonElement(JsonApiDocument.Data.Links.serializer(), it),
            )
          }
          value.included?.let {
            put(
              "included",
              output.json.encodeToJsonElement(ListSerializer(JsonApiResource.serializer()), it),
            )
          }
        }

        is JsonApiDocument.Errors -> {
          put("errors", output.json.encodeToJsonElement(ListSerializer(JsonApiError.serializer()), value.errors))
          value.meta?.let { put("meta", it) }
          value.jsonapi?.let { put("jsonapi", output.json.encodeToJsonElement(JsonApiObject.serializer(), it)) }
          value.links?.let { put("links", output.json.encodeToJsonElement(JsonApiError.Links.serializer(), it)) }
        }
      }
    }
    output.encodeJsonElement(jsonObject)
  }
}

@Serializable
@Type("foo")
data class Foo(
  override val id: ResourceId = ResourceId("Test1"),
  val name: String,
  @Relationship("bar_relationship") val bar_property: Bar
) : JsonApiType

@Serializable
@Type("bar_type")
data class Bar(
  override val id: ResourceId = ResourceId("Test2"),
  val baz: String,
) : JsonApiType

class JsonApi(val json: Json) {
  inline fun <reified T> toType(data: JsonApiDocument.Data.Object): T? {
    val json = Json(from = json) {
      ignoreUnknownKeys = true
    }

    val attributesJson = data.data?.attributes?.jsonObject?.toMutableMap() ?: mutableMapOf()

    // Merge relationships into attributes
    data.data?.relationships?.forEach { (key, relationship) ->
      relationship.data?.let { resourceIdentifier ->
        val relatedResource =
          data.included?.find { it.id == resourceIdentifier.id && it.type == resourceIdentifier.type }
        relatedResource?.attributes?.let { attributesJson[key] = it }
      }
    }

    // Decode the merged attributes into the desired type
    return json.decodeFromJsonElement(JsonObject(attributesJson))
  }

  inline fun <reified T> mergeAttributesAndRelationships(
    attributes: T,
    relationships: Map<String, JsonApiResource?>?,
    json: Json,
  ): T {
    val attributesJson = json.encodeToJsonElement(attributes).jsonObject.toMutableMap()

    relationships?.forEach { (key, resource) ->
      resource?.let {
        val resolvedRelationship = json.encodeToJsonElement(it).jsonObject
        attributesJson[key] = JsonObject(resolvedRelationship)
      }
    }

    return json.decodeFromJsonElement(JsonObject(attributesJson))
  }
}

@Serializable(with = JsonApiDocumentSerializer::class)
sealed interface JsonApiDocument {
  val meta: JsonElement?
  val jsonapi: JsonApiObject?

  @Serializable
  sealed interface Data : JsonApiDocument {
    val links: Links?
    val included: List<JsonApiResource>?

    @Serializable
    data class Object(
      val data: JsonApiResource? = null,
      override val meta: JsonElement? = null,
      override val jsonapi: JsonApiObject? = null,
      override val links: Links? = null,
      override val included: List<JsonApiResource>? = null,
    ) : Data

    @Serializable
    data class Array(
      val data: List<JsonApiResource>? = null,
      override val meta: JsonElement? = null,
      override val jsonapi: JsonApiObject? = null,
      override val links: Links? = null,
      override val included: List<JsonApiResource>? = null,
    ) : Data

    @Serializable
    data class Links(
      val self: JsonApiLink? = null,
      val related: JsonApiLink? = null,
      val describedBy: JsonApiLink? = null,
      val first: JsonApiLink? = null,
      val last: JsonApiLink? = null,
      val prev: JsonApiLink? = null,
      val next: JsonApiLink? = null,
    )
  }

  @Serializable
  data class Errors(
    val errors: List<JsonApiError>,
    override val meta: JsonElement? = null,
    override val jsonapi: JsonApiObject? = null,
    val links: JsonApiError.Links? = null,
  ) : JsonApiDocument
}

@Serializable
data class JsonApiResource(
  val id: String,
  val type: String,
  val attributes: JsonElement? = null,
  val relationships: Map<String, JsonApiRelationship>? = null,
  val links: JsonApiLinks? = null,
  val meta: JsonElement? = null,
)

@Serializable
data class JsonApiRelationship(
  val links: JsonApiLinks? = null,
  val data: JsonApiResourceIdentifier? = null,
  val meta: JsonElement? = null,
)

@Serializable
data class JsonApiResourceIdentifier(
  val id: String,
  val type: String,
  val meta: JsonElement? = null,
)

@Serializable
data class JsonApiLink(
  public val href: String,
  public val rel: String? = null,
  public val describedBy: JsonApiLink? = null,
  public val title: String? = null,
  public val type: String? = null,
  public val hrefLang: List<String>? = null,
  public val meta: JsonObject? = null,
)

@Serializable
data class JsonApiLinks(
  val self: JsonApiLink? = null,
  val related: JsonApiLink? = null,
)

@Serializable
data class JsonApiError(
  val id: String? = null,
  val links: Links? = null,
  val status: String? = null,
  val code: String? = null,
  val title: String? = null,
  val detail: String? = null,
  val source: Source? = null,
  val meta: JsonElement? = null,
) {
  @Serializable
  data class Source(
    val pointer: String? = null,
    val parameter: String? = null,
  )

  @Serializable
  data class Links(
    val about: JsonApiLink? = null,
    val type: JsonApiLink? = null,
  )
}

@Serializable
data class JsonApiObject(
  val version: String? = null,
  val meta: JsonElement? = null,
)


