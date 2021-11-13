package io.bkbn.kompendium.core

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import io.bkbn.kompendium.core.Kontent.generateKontent
import io.bkbn.kompendium.core.Kontent.generateParameterKontent
import io.bkbn.kompendium.core.util.ComplexRequest
import io.bkbn.kompendium.core.util.TestBigNumberModel
import io.bkbn.kompendium.core.util.TestByteArrayModel
import io.bkbn.kompendium.core.util.TestInvalidMap
import io.bkbn.kompendium.core.util.TestNestedModel
import io.bkbn.kompendium.core.util.TestSimpleModel
import io.bkbn.kompendium.core.util.TestSimpleWithEnumList
import io.bkbn.kompendium.core.util.TestSimpleWithEnums
import io.bkbn.kompendium.core.util.TestSimpleWithList
import io.bkbn.kompendium.core.util.TestSimpleWithMap
import io.bkbn.kompendium.core.util.TestWithUUID
import io.bkbn.kompendium.oas.schema.DictionarySchema
import io.bkbn.kompendium.oas.schema.FormattedSchema
import io.bkbn.kompendium.oas.schema.ObjectSchema

@ExperimentalStdlibApi
internal class KontentTest {

  @Test
  fun `Unit returns empty map on generate`() {
    // do
    val result = generateKontent<Unit>()

    // expect
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `Primitive types return a single map result`() {
    // do
    val result = generateKontent<Long>()

    // expect
    assertEquals(1, result.count(), "Should have a single result")
    assertEquals(FormattedSchema("int64", "integer"), result["Long"])
  }

  @Test
  fun `Object with BigDecimal and BigInteger types`() {
    // do
    val result = generateKontent<TestBigNumberModel>()

    // expect
    assertEquals(3, result.count())
    assertTrue { result.containsKey(TestBigNumberModel::class.simpleName) }
    assertEquals(FormattedSchema("double", "number"), result["BigDecimal"])
    assertEquals(FormattedSchema("int64", "integer"), result["BigInteger"])
  }

  @Test
  fun `Object with ByteArray type`() {
    // do
    val result = generateKontent<TestByteArrayModel>()

    // expect
    assertEquals(2, result.count())
    assertTrue { result.containsKey(TestByteArrayModel::class.simpleName) }
    assertEquals(FormattedSchema("byte", "string"), result["ByteArray"])
  }

  @Test
  fun `Objects reference their base types in the cache`() {
    // do
    val result = generateKontent<TestSimpleModel>()

    // expect
    assertNotNull(result)
    assertEquals(3, result.count())
    assertTrue { result.containsKey(TestSimpleModel::class.simpleName) }
  }

  @Test
  fun `generation works for nested object types`() {
    // do
    val result = generateKontent<TestNestedModel>()

    // expect
    assertNotNull(result)
    assertEquals(4, result.count())
    assertTrue { result.containsKey(TestNestedModel::class.simpleName) }
    assertTrue { result.containsKey(TestSimpleModel::class.simpleName) }
  }

  @Test
  fun `generation does not repeat for cached items`() {
    // when
    val clazz = TestNestedModel::class
    val initialCache = generateKontent<TestNestedModel>()

    // do
    val result = generateKontent<TestSimpleModel>(initialCache)

    // expect TODO Spy to check invocation count?
    assertNotNull(result)
    assertEquals(4, result.count())
    assertTrue { result.containsKey(clazz.simpleName) }
    assertTrue { result.containsKey(TestSimpleModel::class.simpleName) }
  }

  @Test
  fun `generation allows for enum fields`() {
    // do
    val result = generateKontent<TestSimpleWithEnums>()

    // expect
    assertNotNull(result)
    assertEquals(3, result.count())
    assertTrue { result.containsKey(TestSimpleWithEnums::class.simpleName) }
  }

  @Test
  fun `generation allows for map fields`() {
    // do
    val result = generateKontent<TestSimpleWithMap>()

    // expect
    assertNotNull(result)
    assertEquals(5, result.count())
    assertTrue { result.containsKey("Map-String-TestSimpleModel") }
    assertTrue { result.containsKey(TestSimpleWithMap::class.simpleName) }

    val os = result[TestSimpleWithMap::class.simpleName] as ObjectSchema
    assertNotNull(os) // todo improve
  }

  @Test
  fun `map fields that are not string result in error`() {
    // expect
    assertFailsWith<IllegalStateException> { generateKontent<TestInvalidMap>() }
  }

  @Test
  fun `generation allows for collection fields`() {
    // do
    val result = generateKontent<TestSimpleWithList>()

    // expect
    assertNotNull(result)
    assertEquals(6, result.count())
    assertTrue { result.containsKey("List-TestSimpleModel") }
    assertTrue { result.containsKey(TestSimpleWithList::class.simpleName) }
  }

  @Test
  fun `Can parse enum list as a field`() {
    // do
    val result = generateKontent<TestSimpleWithEnumList>()

    // expect
    assertNotNull(result)
  }

  @Test
  fun `UUID schema support`() {
    // do
    val result = generateKontent<TestWithUUID>()

    // expect
    assertNotNull(result)
    assertEquals(2, result.count())
    assertTrue { result.containsKey(UUID::class.simpleName) }
    assertTrue { result.containsKey(TestWithUUID::class.simpleName) }
    val expectedSchema = result[UUID::class.simpleName] as FormattedSchema
    assertEquals(FormattedSchema("uuid", "string"), expectedSchema)
  }

  @Test
  fun `Generate top level list response`() {
    // do
    val result = generateKontent<List<TestSimpleModel>>()

    // expect
    assertNotNull(result)
  }

  @Test
  fun `Can handle a complex type`() {
    // do
    val result = generateKontent<ComplexRequest>()

    // expect
    assertNotNull(result)
    assertEquals(7, result.count())
    assertTrue { result.containsKey("Map-String-CrazyItem") }
    val ds = result["Map-String-CrazyItem"] as DictionarySchema
    assertNotNull(ds) // todo improve
  }

  @Test
  fun `Parameter kontent filters out top level declaration`() {
    // do
    val result = generateParameterKontent<TestSimpleModel>()

    // expect
    assertNotNull(result)
    assertEquals(2, result.count())
    assertFalse { result.containsKey(TestSimpleModel::class.simpleName) }
  }

}