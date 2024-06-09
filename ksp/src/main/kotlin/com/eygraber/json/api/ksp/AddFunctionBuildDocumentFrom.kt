package com.eygraber.json.api.ksp

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ksp.toClassName

internal fun FileSpec.Builder.addFunctionBuildDocumentFrom(
  typeInfo: TypeInfo,
  resourceBuilderFunctionName: String,
) {
  val resourceBuilderMN = MemberName(
    TypeProcessor.GEN_PACKAGE,
    resourceBuilderFunctionName,
    isExtension = true,
  )

  val resourceParameterName = typeInfo.type.simpleName.asString().replaceFirstChar(Char::lowercaseChar)

  addFunction(
    FunSpec.builder("buildDocumentFrom").apply {
      receiver(ClassNames.jsonApi)

      addParameter(
        resourceParameterName,
        typeInfo.type.toClassName(),
      )

      returns(ClassNames.jsonApiDocument)

      addStatement(
        "val resourceAttributes = json.%M($resourceParameterName).%M\n",
        MemberNames.encodeToJsonElement,
        MemberNames.jsonObject,
      )

      addStatement(
        "val inclusionHandler = %T()\n",
        ClassNames.inclusionHandler,
      )

      addCode(
        """
        |val data = %T.%M(
        |  attributes = resourceAttributes,
        |  isAddToIncludedNeeded = inclusionHandler::isAddToIncludedNeeded,
        |  addToIncluded = inclusionHandler::addToIncluded,
        |)
        |
        |
        """.trimMargin(),
        ClassNames.jsonApiDocumentBuilders,
        resourceBuilderMN,
      )

      addCode(
        CodeBlock.of(
          """
          |return %T(
          |  data = data,
          |  included = inclusionHandler.included,
          |)
          """.trimMargin(),
          ClassNames.jsonApiDocumentDataObject,
        ),
      )
    }.build(),
  )
}
