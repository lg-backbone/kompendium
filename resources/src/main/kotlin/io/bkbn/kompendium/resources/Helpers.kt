package io.bkbn.kompendium.resources

import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

fun KClass<*>.getResourcePathFromClass(): String {
  val resource = findAnnotation<Resource>()
    ?: error("Cannot notarize a resource without annotating with @Resource")

  val path = resource.path
  val parent = memberProperties.map { it.returnType.classifier as KClass<*> }.find { it.hasAnnotation<Resource>() }

  return if (parent == null) {
    path
  } else {
    parent.getResourcePathFromClass() + path
  }
}

@OptIn(ExperimentalSerializationApi::class)
fun KClass<*>.getPathFromClass(application: Application): String {
  findAnnotation<Resource>() ?: error("Cannot notarize a resource without annotating with @Resource")

  val format = application.plugin(Resources).resourcesFormat
  val serializer = format.serializersModule.serializer(this, emptyList(), false)
  // ResourcesFormat does not encode the top level '/'
  return '/' + format.encodeToPathPattern(serializer)
}
