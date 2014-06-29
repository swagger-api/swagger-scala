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
class MySwagger(val apiVersion: String, val apiInfo: ApiInfo) extends ScalatraSwaggerEngine[Api] {
  
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
implicit formats: SwaggerFormats  = com.wordnik.swagger.SwaggerSerializers.defaultFormats
val mySwagger = new MySwagger(apiVersion, apiInfo)
Swagger.renderIndex(mySwagger, path => "/" + path)
```

And to render the description of a single doc you can use this:
 
```scala
implicit formats: SwaggerFormats  = com.wordnik.swagger.SwaggerSerializers.defaultFormats
val mySwagger = new MySwagger(apiVersion, apiInfo)
mySwagger.doc("/users").fold(JNothing)(Swagger.renderDoc(mySwagger, _, "/")) 
```

