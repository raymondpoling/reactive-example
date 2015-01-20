name := "instrumentation-monad"

version := "1.0"

scalaVersion := "2.11.5"

resolvers += "Spray" at "http://repo.spray.io"

libraryDependencies += "com.wandoulabs.akka" %% "spray-websocket" % "0.1.3"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.6"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.1"

libraryDependencies += "com.typesafe.akka" % "akka-stream-experimental_2.11" % "1.0-M2"