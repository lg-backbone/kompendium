package io.bkbn.kompendium.auth.util

import io.bkbn.kompendium.core.Notarized.notarizedGet
import io.bkbn.kompendium.core.fixtures.TestParams
import io.bkbn.kompendium.core.fixtures.TestResponse
import io.bkbn.kompendium.core.fixtures.TestResponseInfo
import io.bkbn.kompendium.core.metadata.MethodInfo
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.OAuthServerSettings
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.basic
import io.ktor.auth.jwt.jwt
import io.ktor.auth.oauth
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.http.HttpMethod
import io.ktor.response.respondText
import io.ktor.routing.route
import io.ktor.routing.routing

fun Application.setupOauth() {
  install(Authentication) {
    oauth("oauth") {
      urlProvider = { "http://localhost:8080/callback" }
      client = HttpClient(CIO)
      providerLookup = {
        OAuthServerSettings.OAuth2ServerSettings(
          name = "google",
          authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
          accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
          requestMethod = HttpMethod.Post,
          clientId = System.getenv("GOOGLE_CLIENT_ID"),
          clientSecret = System.getenv("GOOGLE_CLIENT_SECRET"),
          defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.profile")
        )
      }
    }
  }
}

fun Application.configBasicAuth() {
  install(Authentication) {
    basic(AuthConfigName.Basic) {
      realm = "Ktor Server"
      validate { credentials ->
        if (credentials.name == credentials.password) {
          UserIdPrincipal(credentials.name)
        } else {
          null
        }
      }
    }
  }
}

fun Application.configJwtAuth() {
  install(Authentication) {
    jwt(AuthConfigName.JWT) {
      realm = "Ktor server"
    }
  }
}

fun Application.notarizedAuthenticatedGetModule(vararg authenticationConfigName: String) {
  routing {
    authenticate(*authenticationConfigName) {
      route("/test") {
        notarizedGet(testGetInfo(*authenticationConfigName)) {
          call.respondText { "hey dude ‼️ congratz on the get request" }
        }
      }
    }
  }
}

fun testGetInfo(vararg security: String) =
  MethodInfo.GetInfo<TestParams, TestResponse>(
    summary = "Another get test",
    description = "testing more",
    responseInfo = TestResponseInfo.testGetResponse,
    securitySchemes = security.toSet()
  )

object AuthConfigName {
  const val Basic = "basic"
  const val JWT = "jwt"
  const val OAuth = "oauth"
}
