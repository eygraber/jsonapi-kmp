package com.eygraber.json.api.ksp

import com.squareup.kotlinpoet.MemberName

internal object MemberNames {
  val id = MemberName(
    TypeProcessor.GEN_PACKAGE,
    "id",
    isExtension = true,
  )

  val relationshipId = MemberName(
    TypeProcessor.GEN_PACKAGE,
    "relationshipId",
    isExtension = true,
  )

  val filterIdAndRelationships = MemberName(
    TypeProcessor.GEN_PACKAGE,
    "filterIdAndRelationships",
    isExtension = true,
  )

  val encodeToJsonElement = MemberName(
    "kotlinx.serialization.json",
    "encodeToJsonElement",
    isExtension = true,
  )

  val jsonObject = MemberName(
    "kotlinx.serialization.json",
    "jsonObject",
    isExtension = true,
  )
}
