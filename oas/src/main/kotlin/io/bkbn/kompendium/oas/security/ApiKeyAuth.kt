package io.bkbn.kompendium.oas.security

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

// TODO... is there even an official ktor api auth mechanism??


@OptIn(ExperimentalSerializationApi::class)
@Serializable
class ApiKeyAuth private constructor(val `in`: String, val name: String) : SecuritySchema {
  @EncodeDefault(EncodeDefault.Mode.ALWAYS)
  val type: String = "apiKey"

  constructor(location: ApiKeyLocation, name: String) : this(location.value, name)

  enum class ApiKeyLocation(val value: String) {
    HEADER("header"),
    QUERY("query"),
    COOKIE("cookie")
  }
}
