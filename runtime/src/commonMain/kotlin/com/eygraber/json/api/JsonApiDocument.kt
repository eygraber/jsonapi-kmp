package com.eygraber.json.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable(with = JsonApiDocumentSerializer::class)
public sealed interface JsonApiDocument {
  public val meta: JsonElement?
  public val jsonapi: JsonApiObject?

  @Serializable
  public sealed interface Data : JsonApiDocument {
    public val links: Links?
    public val included: List<JsonApiResource>?

    @Serializable
    public data class Object(
      public val resource: JsonApiResource? = null,
      public override val meta: JsonElement? = null,
      public override val jsonapi: JsonApiObject? = null,
      public override val links: Links? = null,
      public override val included: List<JsonApiResource>? = null,
    ) : Data

    @Serializable
    public data class Array(
      public val resources: List<JsonApiResource>? = null,
      public override val meta: JsonElement? = null,
      public override val jsonapi: JsonApiObject? = null,
      public override val links: Links? = null,
      public override val included: List<JsonApiResource>? = null,
    ) : Data

    @Serializable
    public data class Links(
      public val self: JsonApiLink? = null,
      public val related: JsonApiLink? = null,
      public val describedBy: JsonApiLink? = null,
      public val first: JsonApiLink? = null,
      public val last: JsonApiLink? = null,
      public val prev: JsonApiLink? = null,
      public val next: JsonApiLink? = null,
    )
  }

  @Serializable
  public data class Errors(
    public val errors: List<JsonApiError>,
    public override val meta: JsonElement? = null,
    public override val jsonapi: JsonApiObject? = null,
    public val links: JsonApiError.Links? = null,
  ) : JsonApiDocument
}

@Serializable
public data class JsonApiResource(
  public val id: String,
  public val type: String,
  public val attributes: JsonElement? = null,
  public val relationships: Map<String, JsonApiRelationship>? = null,
  public val links: JsonApiLinks? = null,
  public val meta: JsonElement? = null,
)

@Serializable
public data class JsonApiRelationship(
  public val links: JsonApiLinks? = null,
  public val data: JsonApiResourceIdentifier? = null,
  public val meta: JsonElement? = null,
)

@Serializable
public data class JsonApiResourceIdentifier(
  public val id: String,
  public val type: String,
  public val meta: JsonElement? = null,
)

@Serializable
public data class JsonApiLink(
  public val href: String,
  public val rel: String? = null,
  public val describedBy: JsonApiLink? = null,
  public val title: String? = null,
  public val type: String? = null,
  public val hrefLang: List<String>? = null,
  public val meta: JsonObject? = null,
)

@Serializable
public data class JsonApiLinks(
  public val self: JsonApiLink? = null,
  public val related: JsonApiLink? = null,
)

@Serializable
public data class JsonApiError(
  public val id: String? = null,
  public val links: Links? = null,
  public val status: String? = null,
  public val code: String? = null,
  public val title: String? = null,
  public val detail: String? = null,
  public val source: Source? = null,
  public val meta: JsonElement? = null,
) {
  @Serializable
  public data class Source(
    public val pointer: String? = null,
    public val parameter: String? = null,
  )

  @Serializable
  public data class Links(
    public val about: JsonApiLink? = null,
    public val type: JsonApiLink? = null,
  )
}

@Serializable
public data class JsonApiObject(
  public val version: String? = null,
  public val meta: JsonElement? = null,
)
