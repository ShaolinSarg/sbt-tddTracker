val scalaTest = "org.scalatest" %% "scalatest" % "2.2.4" % Test
val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.12.5" % Test

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"


lazy val commonSettings = Seq(
  organization := "com.sarginsonconsulting",
  version := "0.1.0",
  scalaVersion := "2.11.7"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "sbt TDD metrics tracker",
    libraryDependencies ++= Seq(scalaTest, scalaCheck),
    testListeners += new TddTestListner
  )
