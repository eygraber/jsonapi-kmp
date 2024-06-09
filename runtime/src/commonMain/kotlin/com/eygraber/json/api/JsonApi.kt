package com.eygraber.json.api

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

public class JsonApi(val json: Json) {
  public inline fun <reified T> toResource(data: JsonApiDocument.Data.Object): T? {
    val json = Json(from = json) {
      ignoreUnknownKeys = true
    }

    val attributesJson = data.resource?.attributes?.jsonObject?.toMutableMap() ?: mutableMapOf()

    // Merge relationships into attributes
    data.resource?.relationships?.forEach { (key, relationship) ->
      relationship.data?.let { resourceIdentifier ->
        val relatedResource =
          data.included?.find { it.id == resourceIdentifier.id && it.type == resourceIdentifier.type }
        relatedResource?.attributes?.let { attributesJson[key] = it }
      }
    }

    // Decode the merged attributes into the desired type
    return json.decodeFromJsonElement(JsonObject(attributesJson))
  }

  public inline fun <reified T> toResources(data: JsonApiDocument.Data.Array): List<T> {
    val json = Json(from = json) {
      ignoreUnknownKeys = true
    }

    return data.resources?.map { resource ->
      val attributesJson = resource.attributes?.jsonObject?.toMutableMap() ?: mutableMapOf()

      // Merge relationships into attributes
      resource.relationships?.forEach { (key, relationship) ->
        relationship.data?.let { resourceIdentifier ->
          val relatedResource =
            data.included?.find { it.id == resourceIdentifier.id && it.type == resourceIdentifier.type }
          relatedResource?.attributes?.let { attributesJson[key] = it }
        }
      }

      // Decode the merged attributes into the desired type
      json.decodeFromJsonElement<T>(JsonObject(attributesJson))
    }.orEmpty()
  }

  // private inline fun <reified T> mergeAttributesAndRelationships(
  //   attributes: T,
  //   relationships: Map<String, JsonApiResource?>?,
  //   json: Json,
  // ): T {
  //   val attributesJson = json.encodeToJsonElement(attributes).jsonObject.toMutableMap()
  //
  //   relationships?.forEach { (key, resource) ->
  //     resource?.let {
  //       val resolvedRelationship = json.encodeToJsonElement(it).jsonObject
  //       attributesJson[key] = JsonObject(resolvedRelationship)
  //     }
  //   }
  //
  //   return json.decodeFromJsonElement(JsonObject(attributesJson))
  // }
}
