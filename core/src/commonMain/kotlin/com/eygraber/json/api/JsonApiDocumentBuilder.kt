package com.eygraber.json.api

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private data class Relationship(
  val name: String,
  val propertyName: String,
  val type: String,
)

private fun JsonObject.id(): String = requireNotNull(this["id"]?.jsonPrimitive?.contentOrNull) {
  "A JsonTypeApi must have a non null id"
}

private fun JsonObject.relationshipId(key: String): String =
  requireNotNull(this[key]?.jsonObject?.get("id")?.jsonPrimitive?.contentOrNull) {
    "A JsonTypeApi must have a non null id for a relationship"
  }

private fun JsonObject.filterIdAndRelationships(
  relationshipPropertyNames: Set<String>
) = buildJsonObject {
  filterNot {
    it.key == "id" || it.key in relationshipPropertyNames
  }.forEach { (k, v) ->
    put(k, v)
  }
}

fun JsonApiDocumentBuilders.jsonApiResourceForFoo(
  attributes: JsonObject,
  requiresInclusion: (String, String, JsonObject) -> Boolean,
  addToIncluded: (JsonApiResource) -> Unit,
): JsonApiResource {
  val id = attributes.id()

  // these are generated
  val barId = attributes.relationshipId("bar_property")

  // the set is generated
  val myAttributes = attributes.filterIdAndRelationships(setOf("bar_property"))

  // the map is generated
  val relationshipReferences = mapOf(
    "bar_relationship" to JsonApiRelationship(
      data = JsonApiResourceIdentifier(
        id = barId,
        type = "bar_type"
      )
    )
  )

  val res = JsonApiResource(
    id = id,
    type = "foo",
    attributes = myAttributes,
    relationships = relationshipReferences
  )

  attributes["bar_property"]?.jsonObject?.let { barAttributes ->
    if(requiresInclusion(barId, "bar_type", barAttributes.filterIdAndRelationships(emptySet()))) {
      addToIncluded(
        jsonApiResourceForBar(
          barAttributes,
        )
      )
    }
  }

  return res
}

fun JsonApiDocumentBuilders.jsonApiResourceForBar(
  attributes: JsonObject,
): JsonApiResource {
  val id = attributes.id()

  // the set is generated
  val myAttributes = buildJsonObject {
    attributes.filterIdAndRelationships(emptySet()).forEach { (k, v) ->
      put(k, v)
    }
  }

  // the map is generated
  val relationshipReferences = null

  val res = JsonApiResource(
    id = id,
    type = "bar_type",
    attributes = myAttributes,
    relationships = relationshipReferences
  )

  return res
}

object JsonApiDocumentBuilders

fun JsonApi.buildDocument(foo: Foo): JsonApiDocument {
  val attributes = json.encodeToJsonElement(foo).jsonObject

  val resourceAttributesByIdAndType = mutableMapOf<Pair<String, String>, JsonElement?>()

  val included = mutableListOf<JsonApiResource>()

  val data = JsonApiDocumentBuilders.jsonApiResourceForFoo(
    attributes,
    requiresInclusion = { id, type, resourceAttributes ->
      resourceAttributes != resourceAttributesByIdAndType[id to type]
    },
    addToIncluded = {
      resourceAttributesByIdAndType[it.id to it.type] = it.attributes
      included += it
    }
  )

  return JsonApiDocument.Data.Object(
    data = data,
    included = included
  )
}
