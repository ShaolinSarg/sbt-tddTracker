val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
val scalaHttp = "org.scalaj" %% "scalaj-http" % "2.3.0"

lazy val commonSettings = Seq(
  organization := "sarginson",
  version := "0.1.0",
  scalaVersion := "2.12.4"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "sbtTddMetricsTracker",
    sbtPlugin := true,
    libraryDependencies ++= Seq(scalaTest, scalaHttp)
  )
