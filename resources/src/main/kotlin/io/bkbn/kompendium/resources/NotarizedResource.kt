package io.bkbn.kompendium.resources

import io.bkbn.kompendium.core.attribute.KompendiumAttributes
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.core.plugin.NotarizedRoute.addToSpec
import io.bkbn.kompendium.core.plugin.NotarizedRoute.calculateRoutePath
import io.bkbn.kompendium.core.plugin.NotarizedRoute.collectAuthMethods
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.Hook
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.routing.Route

object NotarizedResource {
  object InstallHook : Hook<(ApplicationCallPipeline) -> Unit> {
    override fun install(pipeline: ApplicationCallPipeline, handler: (ApplicationCallPipeline) -> Unit) {
      handler(pipeline)
    }
  }

  inline operator fun <reified T> invoke() = createRouteScopedPlugin(
    name = "NotarizedResource<${T::class.qualifiedName}>",
    createConfiguration = NotarizedRoute::Config
  ) {
    on(InstallHook) {
      val route = it as? Route ?: return@on
      val spec = application.attributes[KompendiumAttributes.openApiSpec]
      val authMethods = route.collectAuthMethods()
      val fullPath = T::class.getPathFromClass(application)

      addToSpec(spec, fullPath, authMethods)
    }
  }
}
