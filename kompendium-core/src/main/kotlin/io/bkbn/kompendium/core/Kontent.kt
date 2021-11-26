package io.bkbn.kompendium.core

import io.bkbn.kompendium.annotations.UndeclaredField
import io.bkbn.kompendium.core.metadata.SchemaMap
import io.bkbn.kompendium.core.util.Helpers.genericNameAdapter
import io.bkbn.kompendium.core.util.Helpers.getSimpleSlug
import io.bkbn.kompendium.core.util.Helpers.logged
import io.bkbn.kompendium.oas.schema.AnyOfSchema
import io.bkbn.kompendium.oas.schema.ArraySchema
import io.bkbn.kompendium.oas.schema.DictionarySchema
import io.bkbn.kompendium.oas.schema.EnumSchema
import io.bkbn.kompendium.oas.schema.FormattedSchema
import io.bkbn.kompendium.oas.schema.ObjectSchema
import io.bkbn.kompendium.oas.schema.SimpleSchema
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField
import kotlin.reflect.typeOf
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

/**
 * Responsible for generating the schema map that is used to power all object references across the API Spec.
 */
object Kontent {

  private val logger = LoggerFactory.getLogger(javaClass)

  /**
   * Analyzes a type [T] for its top-level and any nested schemas, and adds them to a [SchemaMap], if provided
   * @param T type to analyze
   * @param cache Existing schema map to append to
   * @return an updated schema map containing all type information for [T]
   */
  @OptIn(ExperimentalStdlibApi::class)
  inline fun <reified T> generateKontent(
    cache: SchemaMap = emptyMap()
  ): SchemaMap {
    val kontentType = typeOf<T>()
    return generateKTypeKontent(kontentType, cache)
  }

  /**
   * Analyzes a [KType] for its top-level and any nested schemas, and adds them to a [SchemaMap], if provided
   * @param type [KType] to analyze
   * @param cache Existing schema map to append to
   * @return an updated schema map containing all type information for [KType] type
   */
  fun generateKontent(
    type: KType,
    cache: SchemaMap = emptyMap()
  ): SchemaMap {
    var newCache = cache
    gatherSubTypes(type).forEach {
      newCache = generateKTypeKontent(it, newCache)
    }
    return newCache
  }

  private fun gatherSubTypes(type: KType): List<KType> {
    val classifier = type.classifier as KClass<*>
    return if (classifier.isSealed) {
      classifier.sealedSubclasses.map {
        it.createType(type.arguments)
      }
    } else {
      listOf(type)
    }
  }

  /**
   * Analyze a type [T], but filters out the top-level type
   * @param T type to analyze
   * @param cache Existing schema map to append to
   * @return an updated schema map containing all type information for [T]
   */
  @OptIn(ExperimentalStdlibApi::class)
  inline fun <reified T> generateParameterKontent(
    cache: SchemaMap = emptyMap()
  ): SchemaMap {
    val kontentType = typeOf<T>()
    return generateKTypeKontent(kontentType, cache)
      .filterNot { (slug, _) -> slug == (kontentType.classifier as KClass<*>).simpleName }
  }

  /**
   * Analyze a type but filters out the top-level type
   * @param type to analyze
   * @param cache Existing schema map to append to
   * @return an updated schema map containing all type information for [T]
   */
  fun generateParameterKontent(
    type: KType,
    cache: SchemaMap = emptyMap()
  ): SchemaMap {
    return generateKTypeKontent(type, cache)
      .filterNot { (slug, _) -> slug == (type.classifier as KClass<*>).simpleName }
  }

  /**
   * Recursively fills schema map depending on [KType] classifier
   * @param type [KType] to parse
   * @param cache Existing schema map to append to
   */
  fun generateKTypeKontent(
    type: KType,
    cache: SchemaMap = emptyMap()
  ): SchemaMap = logged(object {}.javaClass.enclosingMethod.name, mapOf("cache" to cache)) {
    logger.debug("Parsing Kontent of $type")
    when (val clazz = type.classifier as KClass<*>) {
      Unit::class -> cache
      Int::class -> cache.plus(clazz.simpleName!! to FormattedSchema("int32", "integer"))
      Long::class -> cache.plus(clazz.simpleName!! to FormattedSchema("int64", "integer"))
      Double::class -> cache.plus(clazz.simpleName!! to FormattedSchema("double", "number"))
      Float::class -> cache.plus(clazz.simpleName!! to FormattedSchema("float", "number"))
      String::class -> cache.plus(clazz.simpleName!! to SimpleSchema("string"))
      Boolean::class -> cache.plus(clazz.simpleName!! to SimpleSchema("boolean"))
      UUID::class -> cache.plus(clazz.simpleName!! to FormattedSchema("uuid", "string"))
      BigDecimal::class -> cache.plus(clazz.simpleName!! to FormattedSchema("double", "number"))
      BigInteger::class -> cache.plus(clazz.simpleName!! to FormattedSchema("int64", "integer"))
      ByteArray::class -> cache.plus(clazz.simpleName!! to FormattedSchema("byte", "string"))
      else -> when {
        clazz.isSubclassOf(Collection::class) -> handleCollectionType(type, clazz, cache)
        clazz.isSubclassOf(Enum::class) -> handleEnumType(clazz, cache)
        clazz.isSubclassOf(Map::class) -> handleMapType(type, clazz, cache)
        else -> handleComplexType(type, clazz, cache)
      }
    }
  }

  /**
   * In the event of an object type, this method will parse out individual fields to recursively aggregate object map.
   * @param clazz Class of the object to analyze
   * @param cache Existing schema map to append to
   */
  // TODO Fix as part of this issue https://github.com/bkbnio/kompendium/issues/80
  @Suppress("LongMethod", "ComplexMethod")
  private fun handleComplexType(type: KType, clazz: KClass<*>, cache: SchemaMap): SchemaMap {
    // This needs to be simple because it will be stored under it's appropriate reference component implicitly
    val slug = type.getSimpleSlug()
    // Only analyze if component has not already been stored in the cache
    return when (cache.containsKey(slug)) {
      true -> {
        logger.debug("Cache already contains $slug, returning cache untouched")
        cache
      }
      false -> {
        logger.debug("$slug was not found in cache, generating now")
        var newCache = cache
        // Grabs any type parameters as a zip with the corresponding type argument
        val typeMap = clazz.typeParameters.zip(type.arguments).toMap()
        // associates each member with a Pair of prop name to property schema
        val fieldMap = clazz.memberProperties.associate { prop ->
          logger.debug("Analyzing $prop in class $clazz")
          // Grab the field of the current property
          val field = prop.javaField?.type?.kotlin ?: error("Unable to parse field type from $prop")
          logger.debug("Detected field $field")
          // Yoinks any generic types from the type map should the field be a generic
          val yoinkBaseType = if (typeMap.containsKey(prop.returnType.classifier)) {
            logger.debug("Generic type detected")
            typeMap[prop.returnType.classifier]?.type!!
          } else {
            prop.returnType
          }
          // converts the base type to a class
          val yoinkedClassifier = yoinkBaseType.classifier as KClass<*>
          // in the event of a sealed class, grab all sealed subclasses and create a type from the base args
          val yoinkedTypes = if (yoinkedClassifier.isSealed) {
            yoinkedClassifier.sealedSubclasses.map { it.createType(yoinkBaseType.arguments) }
          } else {
            listOf(yoinkBaseType)
          }
          // if the most up-to-date cache does not contain the content for this field, generate it and add to cache
          if (!newCache.containsKey(field.simpleName)) {
            logger.debug("Cache was missing ${field.simpleName}, adding now")
            yoinkedTypes.forEach {
              newCache = generateKTypeKontent(it, newCache)
            }
          }
          // TODO This in particular is worthy of a refactor... just not very well written
          // builds the appropriate property schema based on the property return type
          val propSchema = if (typeMap.containsKey(prop.returnType.classifier)) {
            if (yoinkedClassifier.isSealed) {
              val refs = yoinkedClassifier.sealedSubclasses
                .map { it.createType(yoinkBaseType.arguments) }
                .map { it.getSimpleSlug() }
                .map { newCache[it] ?: error("$it not available") }
              AnyOfSchema(refs)
            } else {
              newCache[typeMap[prop.returnType.classifier]?.type!!.getSimpleSlug()] ?: error("womp womp")
            }
          } else {
            if (yoinkedClassifier.isSealed) {
              val refs = yoinkedClassifier.sealedSubclasses
                .map { it.createType(yoinkBaseType.arguments) }
                .map { newCache[it.getSimpleSlug()] ?: error("womp womp $it") }
              AnyOfSchema(refs)
            } else {
              newCache[field.getSimpleSlug(prop)]!!
            }
          }
          Pair(prop.name, propSchema)
        }
        logger.debug("Looking for undeclared fields")
        val undeclaredFieldMap = clazz.annotations.filterIsInstance<UndeclaredField>().associate {
          val undeclaredType = it.clazz.createType()
          newCache = generateKontent(undeclaredType, newCache)
          it.field to newCache[undeclaredType.getSimpleSlug()]!!
        }
        logger.debug("$slug contains $fieldMap")
        val schema = ObjectSchema(fieldMap.plus(undeclaredFieldMap))
        logger.debug("$slug schema: $schema")
        newCache.plus(slug to schema)
      }
    }
  }

  /**
   * Handler for when an [Enum] is encountered
   * @param clazz Class of the object to analyze
   * @param cache Existing schema map to append to
   */
  private fun handleEnumType(clazz: KClass<*>, cache: SchemaMap): SchemaMap {
    val options = clazz.java.enumConstants.map { it.toString() }.toSet()
    return cache.plus(clazz.simpleName!! to EnumSchema(options))
  }

  /**
   * Handler for when a [Map] is encountered
   * @param type Map type information
   * @param clazz Map class information
   * @param cache Existing schema map to append to
   */
  private fun handleMapType(type: KType, clazz: KClass<*>, cache: SchemaMap): SchemaMap {
    logger.debug("Map detected for $type, generating schema and appending to cache")
    val (keyType, valType) = type.arguments.map { it.type }
    logger.debug("Obtained map types -> key: $keyType and value: $valType")
    if (keyType?.classifier != String::class) {
      error("Invalid Map $type: OpenAPI dictionaries must have keys of type String")
    }
    var updatedCache = generateKTypeKontent(valType!!, cache)
    val valClass = valType.classifier as KClass<*>
    val valClassName = valClass.simpleName
    val referenceName = genericNameAdapter(type, clazz)
    val valueReference = when (valClass.isSealed) {
      true -> {
        val subTypes = gatherSubTypes(valType)
        AnyOfSchema(subTypes.map {
          updatedCache = generateKTypeKontent(it, updatedCache)
          updatedCache[it.getSimpleSlug()] ?: error("${it.getSimpleSlug()} not found")
        })
      }
      false -> updatedCache[valClassName] ?: error("$valClassName not found")
    }
    val schema = DictionarySchema(additionalProperties = valueReference)
    updatedCache = generateKontent(valType, updatedCache)
    return updatedCache.plus(referenceName to schema)
  }

  /**
   * Handler for when a [Collection] is encountered
   * @param type Collection type information
   * @param clazz Collection class information
   * @param cache Existing schema map to append to
   */
  private fun handleCollectionType(type: KType, clazz: KClass<*>, cache: SchemaMap): SchemaMap {
    logger.debug("Collection detected for $type, generating schema and appending to cache")
    val collectionType = type.arguments.first().type!!
    val collectionClass = collectionType.classifier as KClass<*>
    logger.debug("Obtained collection class: $collectionClass")
    val referenceName = genericNameAdapter(type, clazz)
    var updatedCache = generateKTypeKontent(collectionType, cache)
    val valueReference = when (collectionClass.isSealed) {
      true -> {
        val subTypes = gatherSubTypes(collectionType)
        AnyOfSchema(subTypes.map {
          updatedCache = generateKTypeKontent(it, cache)
          updatedCache[it.getSimpleSlug()] ?: error("${it.getSimpleSlug()} not found")
        })
      }
      false -> updatedCache[collectionClass.simpleName] ?: error("${collectionClass.simpleName} not found")
    }
    val schema = ArraySchema(items = valueReference)
    updatedCache = generateKontent(collectionType, cache)
    return updatedCache.plus(referenceName to schema)
  }
}
