enablePlugins(GatlingPlugin)

scalaVersion := "2.13.5"
val gatlingVersion = "3.13.4"
val circeVersion = "0.14.9"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "test"
libraryDependencies += "io.gatling" % "gatling-test-framework" % gatlingVersion % "test"
libraryDependencies += "com.lihaoyi" %% "upickle" % "4.1.0"
libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.11.4"
libraryDependencies += "io.spray" %% "spray-json" % "1.3.6"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

