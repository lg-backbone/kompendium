package io.bkbn.kompendium.core.util

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.javaField
import org.slf4j.LoggerFactory
import java.lang.reflect.ParameterizedType
import java.util.Locale

object Helpers {

  private const val COMPONENT_SLUG = "#/components/schemas"

  fun KType.getSimpleSlug(): String = when {
    this.arguments.isNotEmpty() -> genericNameAdapter(this, classifier as KClass<*>)
    else -> (classifier as KClass<*>).simpleName ?: error("Could not determine simple name for $this")
  }

  fun KType.getReferenceSlug(): String = when {
    arguments.isNotEmpty() -> "$COMPONENT_SLUG/${genericNameAdapter(this, classifier as KClass<*>)}"
    else -> "$COMPONENT_SLUG/${(classifier as KClass<*>).simpleName}"
  }

  /**
   * Adapts a class with type parameters into a reference friendly string
   */
  private fun genericNameAdapter(type: KType, clazz: KClass<*>): String {
    val classNames = type.arguments
      .map { it.type?.classifier as KClass<*> }
      .map { it.simpleName }
    return classNames.joinToString(separator = "-", prefix = "${clazz.simpleName}-")
  }
}