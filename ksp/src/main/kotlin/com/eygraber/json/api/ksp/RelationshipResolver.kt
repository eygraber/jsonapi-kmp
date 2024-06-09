package com.eygraber.json.api.ksp

import com.eygraber.json.api.JsonApiType
import com.eygraber.json.api.annotations.Relationship
import com.eygraber.json.api.annotations.Type
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import kotlinx.serialization.SerialName

internal data class ResolvedRelationshipInfo(
  val typeName: String,
  val relationshipPropertyNames: Set<String>,
)

internal class ResourceBuilderResolver(
  private val relationshipTypeNameLookup: Map<KSTypeReference, String?>,
  private val typeNameToRelationshipPropertyNames: Map<String, Set<String>>,
) {
  operator fun invoke(relationship: RelationshipInfo): ResolvedRelationshipInfo? =
    relationshipTypeNameLookup[relationship.property.type]?.let { typeName ->
      ResolvedRelationshipInfo(
        typeName = typeName,
        relationshipPropertyNames = typeNameToRelationshipPropertyNames[typeName].orEmpty(),
      )
    }
}

@OptIn(KspExperimental::class)
internal fun Map<String, TypeInfo>.resolveRelationships(
  logger: KSPLogger,
): ResourceBuilderResolver {
  val relationshipTypeClassLookup = mutableMapOf<KSTypeReference, KSClassDeclaration>()
  val relationshipTypeNameLookup = mutableMapOf<KSTypeReference, String?>()

  val typeNameToRelationshipPropertyNames = mutableMapOf<String, Set<String>>()

  values.forEach { typeInfo ->
    val relationshipNameLookup = mutableSetOf<String>()

    typeInfo.relationships.forEach { relationship ->
      val propertyClass = relationshipTypeClassLookup.getOrPut(relationship.property.type) {
        relationship.property.type.resolve().declaration as KSClassDeclaration
      }

      val typeName = relationshipTypeNameLookup.getOrPut(relationship.property.type) {
        when {
          !propertyClass.superTypes.containsAncestor(JsonApiType::class) -> {
            logger.error(
              "$propertyClass needs to extend ${JsonApiType::class.simpleName} to be a @Relationship",
              relationship.property,
            )
            null
          }

          else -> propertyClass.getAnnotationsByType(Type::class).firstOrNull()?.value.also { value ->
            if(value == null) {
              logger.error(
                "$propertyClass needs to be annotated with @Type to be a @Relationship",
                relationship.property,
              )
            }
          }
        }
      }

      if(typeName != null) {
        if(relationshipNameLookup.add(relationship.name)) {
          val relationshipsOfRelationship = this[typeName]?.relationships ?: propertyClass.getRelationships()
          typeNameToRelationshipPropertyNames.getOrPut(typeName) {
            relationshipsOfRelationship.mapTo(HashSet()) { it.propertyName }
          }
        } else {
          logger.error(
            "${typeInfo.type} shouldn't have multiple @Relationship properties with the value \"${relationship.name}\"",
            relationship.property,
          )
        }
      }
    }
  }

  return ResourceBuilderResolver(
    relationshipTypeNameLookup,
    typeNameToRelationshipPropertyNames,
  )
}

@OptIn(KspExperimental::class)
private fun KSClassDeclaration.getRelationships() =
  getAllProperties().mapNotNull { property ->
    property.getAnnotationsByType(Relationship::class).firstOrNull()?.value?.let { relationshipName ->
      val serialName = property.getAnnotationsByType(SerialName::class).firstOrNull()?.value
      RelationshipInfo(
        name = relationshipName,
        propertyName = serialName ?: property.simpleName.getShortName(),
        property = property,
      )
    }
  }
