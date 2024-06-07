package com.eygraber.json.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class JsonApiTest {
  @Test
  fun testBuildDocument() {
    val api = JsonApi(Json { prettyPrint = true })
    val doc = api.buildDocument(
      Foo(
        id = ResourceId("1"),
        name = "Eli",
        bar_property = Bar(
          id = ResourceId("2"),
          baz = "This is a baz"
        )
      )
    )

    println(Json.encodeToString(doc))
  }
}
