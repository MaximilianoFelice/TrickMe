name := "TrickMe"

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.2" % "test"


libraryDependencies += "io.reactivex" %% "rxscala" % "0.23.1"


libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.9"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.3.9"


libraryDependencies += "commons-codec" % "commons-codec" % "1.10"