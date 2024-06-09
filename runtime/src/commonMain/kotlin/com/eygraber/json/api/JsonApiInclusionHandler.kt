package com.eygraber.json.api

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

public class JsonApiInclusionHandler {
  private val resourceAttributesByIdAndType = mutableMapOf<Pair<String, String>, JsonElement?>()
  private val mutableIncluded = mutableListOf<JsonApiResource>()
  val included: List<JsonApiResource> = mutableIncluded

  fun isAddToIncludedNeeded(id: String, type: String, resourceAttributes: JsonObject): Boolean =
    resourceAttributes != resourceAttributesByIdAndType[id to type]

  fun addToIncluded(resource: JsonApiResource) {
    resourceAttributesByIdAndType[resource.id to resource.type] = resource.attributes
    mutableIncluded += resource
  }
}
