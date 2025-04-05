import sbt._
import Keys._

object Dependencies {
  val logbackVersion = "0.9.16"
  val grizzlyVersion = "1.9.19"

  val logbackcore    = "ch.qos.logback" % "logback-core"     % logbackVersion
  val logbackclassic = "ch.qos.logback" % "logback-classic"  % logbackVersion

  val jacksonjson = "org.codehaus.jackson" % "jackson-core-lgpl" % "1.7.2"
  val combinators = "org.scala-lang.modules" % "scala-parser-combinators" % "2.1.0"

}
