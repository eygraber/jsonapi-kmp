package com.eygraber.json.api.ksp

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName

internal class ResourceBuilderRelationshipHelper(
  relationships: List<RelationshipInfo>,
  relationshipResolver: ResourceBuilderResolver,
) {
  private val relationshipPropertyNames = mutableListOf<String>()
  private val relationshipIdStatements = mutableListOf<FunSpec.Builder.() -> Unit>()
  private val relationshipReferencesMappings = mutableListOf<FunSpec.Builder.() -> Unit>()
  private val relationshipBuilders = mutableListOf<FunSpec.Builder.() -> Unit>()

  val hasRelationshipReferences get() = relationshipReferencesMappings.isNotEmpty()

  init {
    relationships.forEachIndexed { index, relationship ->
      val resolvedRelationship = relationshipResolver(relationship)

      if(resolvedRelationship != null) {
        val relationshipTypeName = resolvedRelationship.typeName

        relationshipPropertyNames += relationship.propertyName

        relationshipIdStatements += {
          addRelationshipIdStatement(
            relationshipPropertyName = relationship.propertyName,
            isLastStatement = index == relationships.lastIndex,
          )
        }

        relationshipReferencesMappings += {
          addRelationshipReferenceMapping(
            relationshipName = relationship.name,
            relationshipTypeName = relationshipTypeName,
          )
        }

        relationshipBuilders += {
          addRelationshipResourceBuilderBlock(
            relationshipTypeName = relationshipTypeName,
            relationshipPropertyName = relationship.propertyName,
            relationshipsRelationshipPropertyNames = resolvedRelationship.relationshipPropertyNames,
          )
        }
      }
    }
  }

  fun FunSpec.Builder.addRelationshipPropertyNamesStatement() {
    addCode(
      when {
        relationshipPropertyNames.isEmpty() ->
          "val relationshipPropertyNames = emptySet<String>()\n"

        else ->
          """
          |val relationshipPropertyNames = setOf(
          |${relationshipPropertyNames.joinToString(separator = ",\n") { "  \"$it\"" }}
          |)
          |
          |
          """.trimMargin()
      },
    )
  }

  fun FunSpec.Builder.addRelationshipIdStatements() {
    relationshipIdStatements.forEach { addIdStatement ->
      addIdStatement()
    }
  }

  fun FunSpec.Builder.addRelationshipReferencesMappings() {
    if(relationshipReferencesMappings.isNotEmpty()) {
      addCode("val relationshipReferences = mapOf(\n")
      relationshipReferencesMappings.forEach { addMapping ->
        addMapping()
      }
      addCode(")\n\n")
    }
  }

  fun FunSpec.Builder.addRelationshipBuilders() {
    relationshipBuilders.forEach { addRelationshipBuilder ->
      addRelationshipBuilder()
    }
  }
}

private fun FunSpec.Builder.addRelationshipIdStatement(
  relationshipPropertyName: String,
  isLastStatement: Boolean,
) {
  val trailing = if(isLastStatement) "\n" else ""

  addStatement(
    "val ${relationshipPropertyName}Id = attributes.%M(%S)$trailing",
    MemberNames.relationshipId,
    relationshipPropertyName,
  )
}

private fun FunSpec.Builder.addRelationshipReferenceMapping(
  relationshipName: String,
  relationshipTypeName: String,
) {
  addCode(
    """
    |  %S to %T(
    |    data = %T(
    |      id = ${relationshipName}Id,
    |      type = %S
    |    )
    |  ),
    |
    """.trimMargin(),
    relationshipName,
    ClassNames.jsonApiRelationship,
    ClassNames.jsonApiResourceIdentifier,
    relationshipTypeName,
  )
}

private fun FunSpec.Builder.addRelationshipResourceBuilderBlock(
  relationshipTypeName: String,
  relationshipPropertyName: String,
  relationshipsRelationshipPropertyNames: Set<String>,
) {
  val relationshipApiResourceBuilderMN = MemberName(
    TypeProcessor.GEN_PACKAGE,
    "${relationshipTypeName}Builder",
    isExtension = true,
  )

  beginControlFlow(
    "attributes[%S]?.%M?.let { relationshipAttributes ->",
    relationshipPropertyName,
    MemberNames.jsonObject,
  )

  addCode(
    when {
      relationshipsRelationshipPropertyNames.isEmpty() ->
        "val relationshipDataAttributes = relationshipAttributes.%M(emptySet())\n"

      else ->
        """
        |val relationshipDataAttributes = relationshipAttributes.%M(
        |  setOf(
        |${relationshipsRelationshipPropertyNames.joinToString(separator = ",\n") { "    \"$it\"" }}
        |  )
        |)
        |
        |
        """.trimMargin()
    },
    MemberNames.filterIdAndRelationships,
  )

  beginControlFlow(
    "if(isAddToIncludedNeeded(${relationshipPropertyName}Id, %S, relationshipDataAttributes))",
    relationshipTypeName,
  )

  addCode(
    """
    |addToIncluded(
    |  %M(
    |    attributes = relationshipAttributes,
    |    isAddToIncludedNeeded = isAddToIncludedNeeded,
    |    addToIncluded = addToIncluded,
    |  )
    |)
    |
    """.trimMargin(),
    relationshipApiResourceBuilderMN,
  )

  endControlFlow()

  endControlFlow()
  addCode("\n")
}
