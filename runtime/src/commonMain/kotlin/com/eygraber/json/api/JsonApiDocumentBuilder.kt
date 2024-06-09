package com.eygraber.json.api

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

public object JsonApiDocumentBuilder {
  public fun JsonObject.id(): String = requireNotNull(this["id"]?.jsonPrimitive?.contentOrNull) {
    "A JsonTypeApi must have a non null id"
  }

  public fun JsonObject.relationshipId(key: String): String =
    requireNotNull(this[key]?.jsonObject?.get("id")?.jsonPrimitive?.contentOrNull) {
      "A JsonTypeApi must have a non null id for a relationship"
    }

  public fun JsonObject.filterIdAndRelationships(
    relationshipPropertyNames: Set<String>,
  ) = buildJsonObject {
    filterNot {
      it.key == "id" || it.key in relationshipPropertyNames
    }.forEach { (k, v) ->
      put(k, v)
    }
  }
}
