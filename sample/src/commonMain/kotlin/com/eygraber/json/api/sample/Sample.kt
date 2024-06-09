package com.eygraber.json.api.sample

import com.eygraber.json.api.JsonApiType
import com.eygraber.json.api.ResourceId
import com.eygraber.json.api.annotations.Relationship
import com.eygraber.json.api.annotations.Type
import kotlinx.serialization.Serializable

@Serializable
@Type("foo")
data class Foo(
  override val id: ResourceId = ResourceId("Test1"),
  val name: String,
  @Relationship("bar") val bar: Bar,
) : JsonApiType

@Serializable
@Type("bar")
data class Bar(
  override val id: ResourceId = ResourceId("Test2"),
  @Relationship("baz") val baz: Baz,
  @Relationship("boom") val boom: Boom,
) : JsonApiType

@Serializable
@Type("baz")
data class Baz(
  override val id: ResourceId = ResourceId("Test3"),
  val boo: String,
) : JsonApiType

@Serializable
@Type("boom")
data class Boom(
  override val id: ResourceId = ResourceId("Test4"),
  val boo: String,
) : JsonApiType
