package com.eygraber.json.api.ksp

import com.eygraber.json.api.JsonApiType
import com.eygraber.json.api.annotations.Relationship
import com.eygraber.json.api.annotations.Type
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

class TypeProcessor(
  private val codeGenerator: CodeGenerator,
  private val logger: KSPLogger,
) : SymbolProcessor {
  private val typeLookup = mutableMapOf<KSClassDeclaration, String>()
  private val types = mutableMapOf<String, TypeInfo>()

  @OptIn(KspExperimental::class)
  override fun process(resolver: Resolver): List<KSAnnotated> {
    val symbols = resolver.getSymbolsWithAnnotation(
      requireNotNull(Type::class.qualifiedName),
    )

    for(symbol in symbols) {
      require(symbol is KSClassDeclaration)

      if(!symbol.superTypes.containsAncestor(JsonApiType::class)) {
        logger.error("A Type needs to extend from ${JsonApiType::class.simpleName}", symbol)
        continue
      }

      val typeName = symbol.getAnnotationsByType(Type::class).first().value
      val existingType = types[typeName]
      if(existingType != null) {
        if(existingType.type != symbol) {
          logger.error("@Type(\"$typeName\") is already used by ${existingType.type}", symbol)
        }

        continue
      }

      typeLookup[symbol] = typeName

      if(symbol.getAnnotationsByType(Serializable::class).firstOrNull() == null) {
        logger.error("$symbol should be annotated with @Serializable", symbol)
        continue
      }

      val relationships = symbol.getRelationships()

      types[typeName] = TypeInfo(
        type = symbol,
        name = typeName,
        relationships = relationships,
      )
    }

    return emptyList()
  }

  override fun finish() {
    val relationshipResolver = types.resolveRelationships(logger)

    types.values.forEach { typeInfo ->
      val relationships = typeInfo.relationships.toList()

      val jsonApiTypeName = typeInfo.type.simpleName.asString()

      FileSpec.builder(
        packageName = GEN_PACKAGE,
        fileName = "JsonApi${jsonApiTypeName}DocumentBuilder.kt",
      ).apply {
        val resourceBuilderFunctionName = "${typeInfo.name}Builder"

        addFunctionBuildDocumentFrom(
          typeInfo,
          resourceBuilderFunctionName,
        )

        addFunctionResourceBuilder(
          typeName = typeInfo.name,
          resourceBuilderFunctionName = resourceBuilderFunctionName,
          relationships = relationships,
          relationshipResolver = relationshipResolver,
        )
      }.build().writeTo(codeGenerator, aggregating = false)
    }
  }

  companion object {
    const val GEN_PACKAGE = "com.eygraber.json.api"
  }
}

internal class TypeInfo(
  val type: KSClassDeclaration,
  val name: String,
  val relationships: Sequence<RelationshipInfo>,
)

internal class RelationshipInfo(
  val name: String,
  val propertyName: String,
  val property: KSPropertyDeclaration,
)

internal fun Sequence<KSTypeReference>.containsAncestor(clazz: KClass<*>): Boolean = firstOrNull {
  val type = it.resolve()
  clazz.qualifiedName == type.declaration.qualifiedName?.asString() ||
    (type.declaration as? KSClassDeclaration)?.superTypes?.containsAncestor(clazz) == true
} != null

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

class TypeProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = TypeProcessor(
    environment.codeGenerator,
    environment.logger,
  )
}
