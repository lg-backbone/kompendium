package io.bkbn.kompendium.oas.schema

data class FreeFormSchema(override val nullable: Boolean? = null) : TypedSchema {
  val additionalProperties: Boolean = true
  override val type: String = "object"
  override val default: Any? = null
}
