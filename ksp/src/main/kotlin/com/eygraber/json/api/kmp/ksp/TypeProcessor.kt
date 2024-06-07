package com.eygraber.json.api.kmp.ksp

import com.eygraber.json.api.kmp.annotations.Relationship
import com.eygraber.json.api.kmp.annotations.Type
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
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import kotlinx.serialization.SerialName

private class TypeInfo(
  val type: KSClassDeclaration,
  val name: String,
  val relationships: Sequence<RelationshipInfo>
)

private class RelationshipInfo(
  val name: String,
  val propertyName: String,
  val property: KSPropertyDeclaration
)

private fun Sequence<KSTypeReference>.containsAncestor(fqn: String): Boolean = firstOrNull {
  val type = it.resolve()
  fqn == type.declaration.qualifiedName?.asString() ||
    (type.declaration as? KSClassDeclaration)?.superTypes?.containsAncestor(fqn) == true
} != null

class TypeProcessor(
  private val codeGenerator: CodeGenerator,
  private val logger: KSPLogger,
) : SymbolProcessor {
  private val types = mutableMapOf<String, TypeInfo>()

  @OptIn(KspExperimental::class)
  override fun process(resolver: Resolver): List<KSAnnotated> {
    val symbols = resolver.getSymbolsWithAnnotation(
      requireNotNull(Type::class.qualifiedName),
    )

    for(symbol in symbols) {
      require(symbol is KSClassDeclaration)

      if(!symbol.superTypes.containsAncestor("com.eygraber.json.api.kmp.JsonApiType")) {
        logger.error("A @Type needs to extend from JsonApiType", symbol)
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

      val relationships = symbol.getAllProperties().mapNotNull { property ->
        property.getAnnotationsByType(Relationship::class).firstOrNull()?.value?.let { relationshipName ->
          val serialName = property.getAnnotationsByType(SerialName::class).firstOrNull()?.value
          RelationshipInfo(
            name = relationshipName,
            propertyName = serialName ?: property.simpleName.getShortName(),
            property = property
          )
        }
      }

      types[typeName] = TypeInfo(
        type = symbol,
        name = typeName,
        relationships = relationships
      )

      // inline fun createDocument(type: TheActualType)
      // val attributes = json.encodeToJsonElement(type).jsonObject
      // val typeValue = type.typeValue // generate the getter property

      // extract id from attributes
      // val relationshipInfo = getRelationshipInfo(typeValue)
      // extract all relationship attributes from attributes via property name from relationship info
      // build JsonApiResourceIdentifiers from relationship (get id from relationship attributes and type from relationship info)
      // build the top level JsonApiResource from id, typeValue, attributes, and relationships
      //
      // build included recursively from all of the relationships

    //   val file = FileSpec.builder(
    //     packageName = clazz.packageName.asString(),
    //     fileName = "ActualInject${funcReturn.resolve().declaration.simpleName.asString()}",
    //   ).apply {
    //     addFunction(
    //       FunSpec.builder(funcName).apply {
    //         addAnnotation(GenerateActual::class)
    //
    //         addModifiers(
    //           clazz.getVisibility().toKModifier() ?: KModifier.INTERNAL,
    //           KModifier.ACTUAL,
    //         )
    //
    //         receiver(funcReceiverType)
    //
    //         for(param in clazz.parameters) {
    //           val paramName = requireNotNull(param.name?.asString()) {
    //             "$funcName cannot have a parameter without a name"
    //           }
    //
    //           addParameter(paramName, param.type.toTypeName())
    //         }
    //
    //         val funcParams = clazz.parameters.joinToString { it.name?.asString().toString() }
    //
    //         addCode(
    //           CodeBlock.of(
    //             "return %T.create($funcParams)",
    //             funcReceiverType,
    //           ),
    //         )
    //
    //         returns(funcReturn.toTypeName())
    //       }.build(),
    //     )
    //   }.build()
    //
    //   file.writeTo(codeGenerator, aggregating = true)
    }

    return emptyList()
  }

  override fun finish() {
    val file = FileSpec.builder(
      packageName = "com.eygraber.json.api",
      fileName = "JsonApiDocumentBuildersGen.kt",
    )
    types.values.forEach { typeInfo ->
      file.addFunction(
        FunSpec.builder("buildDocument").apply {
          receiver(ClassName("com.eygraber.json.api", "JsonApi"))

          addParameter(
            typeInfo.type.simpleName.asString().replaceFirstChar(Char::lowercaseChar),
            typeInfo.type.toClassName()
          )

          val funcParams = clazz.parameters.joinToString { it.name?.asString().toString() }

          addCode(
            CodeBlock.of(
              "return %T.create($funcParams)",
              funcReceiverType,
            ),
          )

          returns(funcReturn.toTypeName())
        }.build(),
    }
    /*
      val propertyClass = property.type.resolve().declaration as KSClassDeclaration
        if(propertyClass.superTypes.containsAncestor("com.eygraber.json.api.kmp.JsonApiType")) {
          val relationshipTypeValue = propertyClass.getAnnotationsByType(Type::class).first().value
        }
        else {
          logger.error("An @Relationship property needs to extend from JsonApiType", symbol)
          null
        }
       */
  }
}

class TypeProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = TypeProcessor(
    environment.codeGenerator,
    environment.logger,
  )
}
