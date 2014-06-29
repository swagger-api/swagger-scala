# swagger-scala

This implementation of swagger used to live in scalatra, it got extracted so it could be shared by more than one project.

## How to use?

The main implementation of a swagger integration requires an implementation of com.wordnik.SwaggerEngine

Here is the implementation of that class for the default scalatra implementation

```scala
trait ScalatraSwaggerEngine[A <: SwaggerApi[_]] extends SwaggerEngine[A] {
  
  /**
   * Registers the documentation for an API with the given path.
   */
  def register(listingPath: String, resourcePath: String, description: Option[String], s: SwaggerSupportSyntax with SwaggerSupportBase, consumes: List[String], produces: List[String], protocols: List[String], authorizations: List[String])

}

/**
 * An instance of this class is used to hold the API documentation.
 */
class Swagger(val apiVersion: String, val apiInfo: ApiInfo) extends ScalatraSwaggerEngine[Api] {
  
  val swaggerVersion = com.wordnik.swagger.Swagger.SpecVersion
  
  private[this] val logger = Logger[this.type]

  /**
   * Registers the documentation for an API with the given path.
   */
  def register(listingPath: String, resourcePath: String, description: Option[String], s: SwaggerSupportSyntax with SwaggerSupportBase, consumes: List[String], produces: List[String], protocols: List[String], authorizations: List[String]) = {
    logger.debug(s"registering swagger api with: { listingPath: $listingPath, resourcePath: $resourcePath, description: $resourcePath, servlet: ${s.getClass} }")
    val endpoints: List[Endpoint] = s.endpoints(resourcePath) collect { case m: Endpoint => m }
    _docs += listingPath -> Api(
      apiVersion,
      swaggerVersion,
      resourcePath,
      description,
      (produces ::: endpoints.flatMap(_.operations.flatMap(_.produces))).distinct,
      (consumes ::: endpoints.flatMap(_.operations.flatMap(_.consumes))).distinct,
      (protocols ::: endpoints.flatMap(_.operations.flatMap(_.protocols))).distinct,
      endpoints,
      s.models.toMap,
      (authorizations ::: endpoints.flatMap(_.operations.flatMap(_.authorizations))).distinct,
      0)
  }
}
```

This allows you to build the meta data necessary to generate a swagger api description. 
When you do generate the json as api description you should make use of the json formats provided in
`com.wordnik.swagger.SwaggerSerializers.defaultFormats`

For example to render the index json you can use this method: 

```scala
implicit formats: Formats = com.wordnik.swagger.SwaggerSerializers.defaultFormats

def renderIndex[A <: SwaggerApi[_]](swagger: SwaggerEngine[A], mkPath: String => String): JValue = {
  val docs = swagger.docs.toList.asInstanceOf[List[A]]
  ("apiVersion" -> swagger.apiVersion) ~
  ("swaggerVersion" -> swagger.swaggerVersion) ~
  ("apis" ->
    (docs.filter(_.apis.nonEmpty).toList map {
      doc =>
        ("path" -> mkPath(doc.resourcePath)) ~
        ("description" -> doc.description)
    })) ~
  ("authorizations" -> swagger.authorizations.foldLeft(JObject(Nil)) { (acc, auth) =>
    acc merge JObject(List(auth.`type` -> Extraction.decompose(auth)))
  }) ~
  ("info" -> Option(swagger.apiInfo).map(Extraction.decompose(_)))
}
```

And to render the description of a single doc you can use this:
 
```scala
implicit formats: Formats = com.wordnik.swagger.SwaggerSerializers.defaultFormats

def renderDoc[A <: SwaggerApi[_]](swagger: SwaggerEngine[A], doc: A, basePath: String): JValue = {
  def dontAddOnEmpty(key: String, value: List[String])(json: JValue) = {
    val v: JValue = if (value.nonEmpty) key -> value else JNothing
    json merge v
  }

  val json = Extraction.decompose(doc) merge
    ("basePath" -> basePath) ~
    ("swaggerVersion" -> swagger.swaggerVersion) ~
    ("apiVersion" -> swagger.apiVersion)
  
  val consumes = dontAddOnEmpty("consumes", doc.consumes)_
  val produces = dontAddOnEmpty("produces", doc.produces)_
  val protocols = dontAddOnEmpty("protocols", doc.protocols)_
  val authorizations = dontAddOnEmpty("authorizations", doc.authorizations)_
  
  (consumes andThen produces andThen protocols andThen authorizations)(json)
}
```

