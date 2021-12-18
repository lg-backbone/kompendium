package io.bkbn.kompendium.playground

import io.bkbn.kompendium.annotations.KompendiumField
import io.bkbn.kompendium.annotations.KompendiumParam
import io.bkbn.kompendium.annotations.ParamType
import io.bkbn.kompendium.core.Kompendium
import io.bkbn.kompendium.core.Notarized.notarizedGet
import io.bkbn.kompendium.core.Notarized.notarizedPost
import io.bkbn.kompendium.core.metadata.RequestInfo
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.GetInfo
import io.bkbn.kompendium.core.metadata.method.PostInfo
import io.bkbn.kompendium.core.routes.redoc
import io.bkbn.kompendium.oas.OpenApiSpec
import io.bkbn.kompendium.oas.info.Contact
import io.bkbn.kompendium.oas.info.Info
import io.bkbn.kompendium.oas.info.License
import io.bkbn.kompendium.oas.server.Server
import io.bkbn.kompendium.playground.BasicModels.BasicParameters
import io.bkbn.kompendium.playground.BasicModels.BasicResponse
import io.bkbn.kompendium.playground.BasicModels.BasicRequest
import io.bkbn.kompendium.playground.BasicPlaygroundToC.simpleGetExample
import io.bkbn.kompendium.playground.BasicPlaygroundToC.simpleGetExampleWithParameters
import io.bkbn.kompendium.playground.BasicPlaygroundToC.simplePostRequest
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.serialization.Serializable
import java.net.URI
import java.util.UUID

/**
 * Application entrypoint.  Run this and head on over to `localhost:8081/docs`
 * to see a very simple yet beautifully documented API
 */
fun main() {
  embeddedServer(
    Netty,
    port = 8081,
    module = Application::mainModule
  ).start(wait = true)
}

// Application Module
private fun Application.mainModule() {
  // Installs Simple JSON Content Negotiation
  install(ContentNegotiation) {
    json()
  }
  // Installs the Kompendium Plugin and sets up baseline server metadata
  install(Kompendium) {
    spec = BasicMetadata.spec
  }
  // Configures the routes for our API
  routing {
    // This adds ReDoc support at the `/docs` endpoint.
    // By default, it will point at the `/openapi.json` created by Kompendium
    redoc(pageTitle = "Simple API Docs")
    // Kompendium infers the route path from the Ktor Route.  This will show up as the root path `/`
    notarizedGet(simpleGetExample) {
      call.respond(HttpStatusCode.OK, BasicResponse(c = UUID.randomUUID().toString()))
    }
    // It can also infer path parameters
    route("/{a}") {
      notarizedGet(simpleGetExampleWithParameters) {
        val a = call.parameters["a"] ?: error("Unable to read expected path parameter")
        val b = call.request.queryParameters["b"]?.toInt() ?: error("Unable to read expected query parameter")
        call.respond(HttpStatusCode.OK, BasicResponse(c = "$a: $b"))
      }
    }
    route("/create") {
      notarizedPost(simplePostRequest) {
        val request = call.receive<BasicRequest>()
        when (request.d) {
          true -> call.respond(HttpStatusCode.OK, BasicResponse(c = "So it is true!"))
          false -> call.respond(HttpStatusCode.OK, BasicResponse(c = "Oh, I knew it!"))
        }
      }
    }
  }
}

// This is a table of contents to hold all the metadata for our various API endpoints
object BasicPlaygroundToC {
  /**
   * This is the information required to document a simple request.  Here we declare that our endpoint
   * takes no parameters, and will return an object of type [BasicResponse]
   * with status code [HttpStatusCode.OK]
   */
  val simpleGetExample = GetInfo<Unit, BasicResponse>(
    summary = "Simple, Documented GET Request",
    description = "This is to showcase just how easy it is to document your Ktor API!",
    responseInfo = ResponseInfo(
      status = HttpStatusCode.OK,
      description = "This means everything went as expected!",
      examples = mapOf("demo" to BasicResponse(c = "52c099d7-8642-46cc-b34e-22f39b923cf4"))
    ),
    tags = setOf("Simple")
  )

  /**
   * This showcases a GET request with parameters.  Here we declare that our endpoint takes a path and a query
   * parameter. This is inferred by the annotations on [BasicParameters.a] and [BasicParameters.b]
   * respectively.
   */
  val simpleGetExampleWithParameters = GetInfo<BasicParameters, BasicResponse>(
    summary = "Simple, Documented GET Request with Parameters",
    description = "This showcases how easy it is to document your input parameters!",
    responseInfo = ResponseInfo(
      status = HttpStatusCode.OK,
      description = "This means everything went as expected!",
      examples = mapOf("demo" to BasicResponse(c = "52c099d7-8642-46cc-b34e-22f39b923cf4"))
    ),
    tags = setOf("Parameters")
  )

  /**
   * This showcases a POST request with a request body of type [BasicRequest]
   */
  val simplePostRequest = PostInfo<Unit, BasicRequest, BasicResponse>(
    summary = "Simple, Documented POST Request",
    description = "Showcases how easy it is to document a post request!",
    requestInfo = RequestInfo(
      description = "This is the required info for this request!",
      examples = mapOf("demo" to BasicRequest(true))
    ),
    responseInfo = ResponseInfo(
      status = HttpStatusCode.OK,
      description = "This means everything went as expected!",
      examples = mapOf("demo" to BasicResponse(c = "So it is true!"))
    ),
    tags = setOf("Simple")
  )
}

// Contains the root metadata for our server.  This is all the stuff that is defined once
// and cannot be inferred from the Ktor application
object BasicMetadata {
  val spec = OpenApiSpec(
    info = Info(
      title = "Simple Demo API",
      version = "1.33.7",
      description = "Wow isn't this cool?",
      termsOfService = URI("https://example.com"),
      contact = Contact(
        name = "Homer Simpson",
        email = "chunkylover53@aol.com",
        url = URI("https://gph.is/1NPUDiM")
      ),
      license = License(
        name = "MIT",
        url = URI("https://github.com/bkbnio/kompendium/blob/main/LICENSE")
      )
    ),
    servers = mutableListOf(
      Server(
        url = URI("https://myawesomeapi.com"),
        description = "Production instance of my API"
      ),
      Server(
        url = URI("https://staging.myawesomeapi.com"),
        description = "Where the fun stuff happens"
      )
    )
  )
}

object BasicModels {
  @Serializable
  data class BasicResponse(val c: String)

  @Serializable
  data class BasicParameters(
    @KompendiumParam(type = ParamType.PATH)
    val a: String,
    @KompendiumParam(type = ParamType.QUERY)
    val b: Int
  )

  @Serializable
  data class BasicRequest(
    @KompendiumField(description = "This is a super important field!!")
    val d: Boolean
  )
}