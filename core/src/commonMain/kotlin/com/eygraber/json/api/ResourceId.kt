package com.eygraber.json.api

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
public value class ResourceId(public val id: String) {
  public val isLocal: Boolean get() = id.startsWith("lid:")
  public val isSpecified: Boolean get() = this != NoId

  public companion object {
    public val NoId: ResourceId = ResourceId("")

    public fun local(id: String): ResourceId = ResourceId("lid:$id")
  }
}
