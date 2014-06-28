name := "swagger-scala"

organization in ThisBuild := "com.wordnik.swagger"

scalaVersion := "2.11.1"

crossScalaVersions := Seq("2.10.0", "2.11.1")

libraryDependencies += "org.json4s" %% "json4s-core" % "3.2.10"

libraryDependencies += "org.json4s" %% "json4s-ext" % "3.2.10"

libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.2.10" % "provided"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.0.0"

libraryDependencies += "org.scala-lang" % "scalap" % scalaVersion.value

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

libraryDependencies += "joda-time" % "joda-time" % "2.3" % "provided"

libraryDependencies += "org.joda" % "joda-convert" % "1.6" % "provided"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2" % "provided"