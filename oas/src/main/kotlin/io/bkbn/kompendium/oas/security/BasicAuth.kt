package io.bkbn.kompendium.oas.security

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable


@OptIn(ExperimentalSerializationApi::class)
@Serializable
class BasicAuth : SecuritySchema {
  @EncodeDefault(EncodeDefault.Mode.ALWAYS)
  val type: String = "http"
  @EncodeDefault(EncodeDefault.Mode.ALWAYS)
  val scheme: String = "basic"
}
