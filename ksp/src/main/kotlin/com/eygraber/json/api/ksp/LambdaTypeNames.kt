package com.eygraber.json.api.ksp

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.UNIT

internal object LambdaTypeNames {
  val isAddToIncludedNeeded = LambdaTypeName.get(
    STRING,
    STRING,
    ClassNames.jsonObject,
    returnType = BOOLEAN,
  )

  val addToIncluded = LambdaTypeName.get(
    ClassNames.jsonApiResource,
    returnType = UNIT,
  )
}
