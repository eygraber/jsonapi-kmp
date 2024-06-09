package com.eygraber.json.api.ksp

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec

internal fun FileSpec.Builder.addFunctionResourceBuilder(
  typeName: String,
  resourceBuilderFunctionName: String,
  relationships: List<RelationshipInfo>,
  relationshipResolver: ResourceBuilderResolver,
) {
  addFunction(
    FunSpec.builder(resourceBuilderFunctionName).apply {
      receiver(ClassNames.jsonApiDocumentBuilders)

      addParameter(
        "attributes",
        ClassNames.jsonObject,
      )

      addParameter(
        "isAddToIncludedNeeded",
        LambdaTypeNames.isAddToIncludedNeeded,
      )

      addParameter(
        "addToIncluded",
        LambdaTypeNames.addToIncluded,
      )

      returns(ClassNames.jsonApiResource)

      addStatement("val id = attributes.%M()\n", MemberNames.id)

      with(
        ResourceBuilderRelationshipHelper(
          relationships = relationships,
          relationshipResolver = relationshipResolver,
        ),
      ) {
        addRelationshipPropertyNamesStatement()

        addStatement(
          "val resourceDataAttributes = attributes.%M(relationshipPropertyNames)\n",
          MemberNames.filterIdAndRelationships,
        )

        addRelationshipIdStatements()

        addRelationshipReferencesMappings()

        val relationshipsParam = when {
          hasRelationshipReferences -> "relationshipReferences"
          else -> "null"
        }

        addCode(
          """
              |val res = %T(
              |  id = id,
              |  type = %S,
              |  attributes = resourceDataAttributes,
              |  relationships = $relationshipsParam,
              |)
              |
              |
          """.trimMargin(),
          ClassNames.jsonApiResource,
          typeName,
        )

        addRelationshipBuilders()
      }

      addCode("return res")
    }.build(),
  )
}
