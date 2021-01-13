organization := "tb"
name := "akka-chat"

version := "0.1.2"

scalaVersion := "2.13.4"

val akkaVersion = "2.6.10"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.30"
libraryDependencies += "org.clapper" %% "grizzled-slf4j" % "1.3.4"
libraryDependencies += "junit" % "junit" % "4.13.1" % Test

libraryDependencies += "org.mockito" % "mockito-core" % "3.7.0" % Test



fork in run := true
outputStrategy := Some(StdoutOutput)
connectInput in run := true

scalafmtOnCompile := true

scalacOptions ++= List(
  "-feature",
  "-language:higherKinds",
  "-Xlint",
  "-Yrangepos",
  "-Ywarn-unused"
)

enablePlugins(UniversalPlugin, JavaAppPackaging)