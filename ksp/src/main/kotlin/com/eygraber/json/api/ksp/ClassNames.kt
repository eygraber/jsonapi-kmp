package com.eygraber.json.api.ksp

import com.eygraber.json.api.JsonApi
import com.eygraber.json.api.JsonApiDocument
import com.eygraber.json.api.JsonApiDocumentBuilder
import com.eygraber.json.api.JsonApiInclusionHandler
import com.eygraber.json.api.JsonApiRelationship
import com.eygraber.json.api.JsonApiResource
import com.eygraber.json.api.JsonApiResourceIdentifier
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.json.JsonObject

internal object ClassNames {
  val jsonApi = JsonApi::class.asClassName()
  val jsonApiDocument = JsonApiDocument::class.asClassName()
  val jsonApiDocumentDataObject = JsonApiDocument.Data.Object::class.asClassName()
  val jsonApiRelationship = JsonApiRelationship::class.asClassName()
  val jsonApiResource = JsonApiResource::class.asClassName()
  val jsonApiResourceIdentifier = JsonApiResourceIdentifier::class.asClassName()

  val jsonObject = JsonObject::class.asClassName()

  val inclusionHandler = JsonApiInclusionHandler::class.asClassName()
  val jsonApiDocumentBuilders = JsonApiDocumentBuilder::class.asClassName()
}
