scalaVersion := "2.13.8"

name := "waveskimmer-statemachine"
organization := "org.waveskimmer"
version := "1.0-snapshot"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % "1.4.3"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.14" % "test"
)
