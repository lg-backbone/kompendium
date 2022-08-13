package io.bkbn.kompendium.core.fixtures

import io.bkbn.kompendium.annotations.Field
import io.bkbn.kompendium.annotations.FreeFormObject
import io.bkbn.kompendium.annotations.Param
import io.bkbn.kompendium.annotations.ParamType
import io.bkbn.kompendium.annotations.UndeclaredField
import io.bkbn.kompendium.annotations.constraint.Format
import io.bkbn.kompendium.annotations.constraint.MaxItems
import io.bkbn.kompendium.annotations.constraint.MaxLength
import io.bkbn.kompendium.annotations.constraint.MaxProperties
import io.bkbn.kompendium.annotations.constraint.Maximum
import io.bkbn.kompendium.annotations.constraint.MinItems
import io.bkbn.kompendium.annotations.constraint.MinLength
import io.bkbn.kompendium.annotations.constraint.MinProperties
import io.bkbn.kompendium.annotations.constraint.Minimum
import io.bkbn.kompendium.annotations.constraint.MultipleOf
import io.bkbn.kompendium.annotations.constraint.Pattern
import io.bkbn.kompendium.annotations.constraint.UniqueItems
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.util.UUID

@Serializable
data class TestNested(val nesty: String)

@Serializable
data class TestRequest(
  val fieldName: TestNested,
  val b: Double,
  val aaa: List<Long>
)

@Serializable
data class TestSimpleRequest(
  val a: String,
  val b: Int
)

@Serializable
data class TestResponse(val c: String)

@Serializable
enum class TestEnum {
  YES,
  NO
}

@Serializable
data class NullableEnum(val a: TestEnum? = null)

data class TestCreatedResponse(val id: Int, val c: String)

data class DateTimeString(
  val a: Instant
)

data class DefaultField(
  val a: String = "hi",
  val b: Int
)

data class NullableField(
  val a: String?
)

data class ComplexRequest(
  val org: String,
  @Field("amazing_field")
  val amazingField: String,
  val tables: List<NestedComplexItem>
)

data class NestedComplexItem(
  val name: String,
  val alias: CustomAlias
)

typealias CustomAlias = Map<String, CrazyItem>

data class CrazyItem(val enumeration: SimpleEnum)

enum class SimpleEnum {
  ONE,
  TWO
}

data class ExceptionResponse(val message: String)

sealed class FlibbityGibbit {
  abstract val z: String
}

data class SimpleGibbit(val a: String, override val z: String = "z") : FlibbityGibbit()
data class ComplexGibbit(val b: String, val c: Int, override val z: String) : FlibbityGibbit()

sealed interface SlammaJamma

data class OneJamma(val a: Int) : SlammaJamma
data class AnothaJamma(val b: Float) : SlammaJamma

data class InsaneJamma(val c: SlammaJamma) : SlammaJamma

sealed interface Flibbity<T>

data class Gibbity<T>(val a: T) : Flibbity<T>
data class Bibbity<T>(val b: String, val f: T) : Flibbity<T>

data class NestedFlibbity<T>(
  val flibbity: Flibbity<T>
)

enum class ColumnMode {
  NULLABLE,
  REQUIRED,
  REPEATED
}

data class ColumnSchema(
  val name: String,
  val type: String,
  val description: String,
  val mode: ColumnMode,
  val subColumns: List<ColumnSchema> = emptyList()
)

@Serializable
public data class ProfileUpdateRequest(
  public val mood: String?,
  public val viewCount: Long?,
  public val metadata: ProfileMetadataUpdateRequest?
)


@Serializable
public data class ProfileMetadataUpdateRequest(
  public val isPrivate: Boolean?,
  public val otherThing: String?
)
