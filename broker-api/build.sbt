name := """broker-api"""

organization := "at.segv.play.broker"

version := "1.0.1"

scalaVersion := "2.11.1"

lazy val api = project in file(".")

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
