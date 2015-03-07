name := """broker-app"""

version := "1.0-SNAPSHOT"

lazy val commonSettings = Seq(
  organization := "at.segv.play.broker",
  scalaVersion := "2.11.1"
)


lazy val api = (project in file("broker-api")).settings(commonSettings: _*)

lazy val root = (project in file(".")).enablePlugins(PlayScala).settings(commonSettings: _*).dependsOn(api)




libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws
)

libraryDependencies +=   "com.typesafe.akka" %% "akka-actor" % "2.3.9"

libraryDependencies +=   "com.typesafe.akka" %% "akka-remote" % "2.3.9"

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.3.9"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.13"


