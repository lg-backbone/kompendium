package io.bkbn.kompendium.oas

import io.bkbn.kompendium.oas.common.ExternalDocumentation
import io.bkbn.kompendium.oas.common.Tag
import io.bkbn.kompendium.oas.component.Components
import io.bkbn.kompendium.oas.info.Info
import io.bkbn.kompendium.oas.path.Path
import io.bkbn.kompendium.oas.server.Server
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 * This is the root object of the OpenAPI document.
 *
 * https://spec.openapis.org/oas/v3.1.0#openapi-object
 *
 * @param openapi This string MUST be the version number of the OpenAPI Specification that the OpenAPI document uses.
 * Kompendium only supports OpenAPI 3.1
 * @param jsonSchemaDialect The default value for the $schema keyword within Schema Objects contained within this OAS document.
 * Kompendium only supports the 2020 draft
 * @param info Provides metadata about the API.
 * @param servers An array of Server Objects, which provide connectivity information to a target server.
 * If the property is not provided, or is an empty array, the default value would be a Server Object with a url value of /.
 * @param paths The available paths and operations for the API.
 * @param webhooks The incoming webhooks that MAY be received as part of this API and that the API consumer MAY choose to implement.
 * @param components An element to hold various schemas for the document.
 * @param security A declaration of which security mechanisms can be used across the API.
 * @param tags A list of tags used by the document with additional metadata.
 * @param externalDocs Additional external documentation.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class OpenApiSpec(
  @EncodeDefault(EncodeDefault.Mode.ALWAYS)
  val openapi: String = "3.1.0",
  @EncodeDefault(EncodeDefault.Mode.ALWAYS)
  val jsonSchemaDialect: String = "https://json-schema.org/draft/2020-12/schema",
  val info: Info,
  val servers: MutableList<Server> = mutableListOf(),
  val paths: MutableMap<String, Path> = mutableMapOf(),
  val webhooks: MutableMap<String, Path> = mutableMapOf(),
  val components: Components = Components(),
  val security: MutableList<Map<String, List<String>>> = mutableListOf(),
  val tags: MutableList<Tag> = mutableListOf(),
  val externalDocs: ExternalDocumentation? = null
)
