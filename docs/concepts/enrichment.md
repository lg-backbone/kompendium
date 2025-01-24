Kompendium allows users to enrich their data types with additional information. This can be done by defining an
`ObjectEnrichment` object and passing it to the `enrichment` parameter of the relevant `requestType` or `responseType`.

```kotlin
data class SimpleData(val a: String, val b: Int? = null)

val myEnrichment = ObjectEnrichment<SimpleData>(id = "simple-enrichment") {
  SimpleData::a {
    StringEnrichment("a") {
      description = "This will update the field description"
    }
  }
  SimpleData::b {
    NumberEnrichment("b") {
      // Will indicate in the UI that the field will be removed soon
      deprecated = true
    }
  }
}

// In your route documentation
fun Routing.enrichedSimpleRequest() {
  route("/example") {
    install(NotarizedRoute()) {
      parameters = TestModules.defaultParams
      post = PostInfo.builder {
        summary(TestModules.defaultPathSummary)
        description(TestModules.defaultPathDescription)
        request {
          requestType<SimpleData>(enrichment = myEnrichment) // Simply attach the enrichment to the request
          description("A test request")
        }
        response {
          responseCode(HttpStatusCode.Created)
          responseType<TestCreatedResponse>()
          description(TestModules.defaultResponseDescription)
        }
      }
    }
  }
}
```

{% hint style="warning" %}
An enrichment must provide an `id` field that is unique to the data class that is being enriched. This is because
under the hood, Kompendium appends this id to the data class identifier in order to support multiple different
enrichments
on the same data class.

If you provide duplicate ids, all but the first enrichment will be ignored, as Kompendium will view that as a cache hit,
and skip analyzing the new enrichment.
{% endhint %}

### Nested Enrichments

Enrichments are portable and composable, meaning that we can take an enrichment for a child data class
and apply it inside a parent data class.

```kotlin
data class ParentData(val a: String, val b: ChildData)
data class ChildData(val c: String, val d: Int? = null)

val childEnrichment = ObjectEnrichment<ChildData>(id = "child-enrichment") {
  description = "This will update the field description of field b on parent data"
  ChildData::c {
    StringEnrichment("c") {
      description = "This will update the field description of field c on child data"
    }
  }
  ChildData::d {
    NumberEnrichment("d") {
      description = "This will update the field description of field d on child data"
    }
  }
}

val parentEnrichment = ObjectEnrichment<ParentData>(id = "parent-enrichment") {
  ParentData::a {
    StringEnrichment("a") {
      description = "This will update the field description"
    }
  }
  ParentData::b {
    childEnrichment // Will apply the child enrichment to the internals of field b
  }
}
```

## Available Enrichments

All enrichments support the following properties:

- description -> Provides a reader friendly description of the field in the object
- deprecated -> Indicates that the field is deprecated and should not be used

### Strings (`StringEnrichment`)

- minLength -> The minimum length of the string
- maxLength -> The maximum length of the string
- pattern -> A regex pattern that the string must match
- contentEncoding -> The encoding of the string
- contentMediaType -> The media type of the string

### Numbers (`NumberEnrichment`)

- minimum -> The minimum value of the number
- maximum -> The maximum value of the number
- exclusiveMinimum -> Indicates that the minimum value is exclusive
- exclusiveMaximum -> Indicates that the maximum value is exclusive
- multipleOf -> Indicates that the number must be a multiple of the provided value

### Arrays (`CollectionEnrichment`)

- minItems -> The minimum number of items in the array
- maxItems -> The maximum number of items in the array
- uniqueItems -> Indicates that the array must contain unique items
- itemEnrichment -> An enrichment object for the array items

### Maps (`MapEnrichment`)

- minProperties -> The minimum number of items in the map
- maxProperties -> The maximum number of items in the map
- keyEnrichment -> A `StringEnrichment` object for the map keys
- valueEnrichment -> An enrichment object for the map values

### Booleans (`BooleanEnrichment`)

(No additional properties)
