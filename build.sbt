name := """broker-app"""

version := "1.0-SNAPSHOT"

lazy val commonSettings = Seq(
  organization := "at.segv.play.broker",
  scalaVersion := "2.11.1"
)



lazy val api = (project in file("broker-api")).settings(commonSettings: _*)

lazy val root = (project in file(".")).enablePlugins(PlayScala).settings(commonSettings: _*).dependsOn(api).enablePlugins(SbtWeb)




libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws
)

libraryDependencies   ++= Seq( "com.typesafe.akka" %% "akka-actor" % "2.3.9" ,
  "com.typesafe.akka" %% "akka-remote" % "2.3.9",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.9",
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  "org.webjars" % "bootstrap" % "3.3.2-2",
  "org.webjars" % "backbonejs" % "1.1.2-2"
)
