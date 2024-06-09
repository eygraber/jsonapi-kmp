package com.eygraber.json.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject

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
      "data" in jsonObject -> when(val data = jsonObject["data"]) {
        is JsonArray -> JsonApiDocument.Data.Array(
          resources = data.map { input.json.decodeFromJsonElement(JsonApiResource.serializer(), it) },
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
          resource = input.json.decodeFromJsonElement(JsonApiResource.serializer(), data),
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
      when(value) {
        is JsonApiDocument.Data -> {
          when(value) {
            is JsonApiDocument.Data.Array -> value.resources?.let { data ->
              put(
                "data",
                buildJsonArray {
                  data.forEach {
                    add(output.json.encodeToJsonElement(JsonApiResource.serializer(), it))
                  }
                },
              )
            }

            is JsonApiDocument.Data.Object -> value.resource?.let {
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
