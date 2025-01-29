package io.bkbn.kompendium.oas.security

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class OAuth(val description: String? = null, val flows: Flows) : SecuritySchema {
  @EncodeDefault(EncodeDefault.Mode.ALWAYS)
  val type: String = "oauth2"

  @Serializable
  data class Flows(
    val implicit: Implicit? = null,
    val authorizationCode: AuthorizationCode? = null,
    val password: Password? = null,
    val clientCredentials: ClientCredential? = null,
  ) {

    sealed interface Flow {
      val authorizationUrl: String?
        get() = null
      val tokenUrl: String?
        get() = null
      val refreshUrl: String?
        get() = null
      val scopes: Map<String, String>
        get() = emptyMap()
    }

    @Serializable
    data class Implicit(
      override val authorizationUrl: String,
      override val refreshUrl: String? = null,
      override val scopes: Map<String, String> = emptyMap()
    ) : Flow

    @Serializable
    data class AuthorizationCode(
      override val authorizationUrl: String,
      override val tokenUrl: String? = null,
      override val refreshUrl: String? = null,
      override val scopes: Map<String, String> = emptyMap()
    ) : Flow

    @Serializable
    data class Password(
      override val tokenUrl: String? = null,
      override val refreshUrl: String? = null,
      override val scopes: Map<String, String> = emptyMap()
    ) : Flow

    @Serializable
    data class ClientCredential(
      override val tokenUrl: String? = null,
      override val refreshUrl: String? = null,
      override val scopes: Map<String, String> = emptyMap()
    ) : Flow
  }
}
