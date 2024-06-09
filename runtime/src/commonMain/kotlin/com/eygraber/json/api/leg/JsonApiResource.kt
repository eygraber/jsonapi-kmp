package com.eygraber.json.api.leg

import com.eygraber.json.api.ResourceId

// import com.eygraber.json.api.leg.JsonApiResourceSerializer
// import kotlinx.serialization.Serializable
//
// @Serializable(JsonApiResourceSerializer::class)
public interface JsonApiResource {
  public val id: ResourceId

  public fun jsonApiType(): String
}
