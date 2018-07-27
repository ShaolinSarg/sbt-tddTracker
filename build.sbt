val scalaTest = "org.scalatest" %% "scalatest"    % "3.0.5" % Test
val mockito =   "org.mockito"   %  "mockito-core" % "1.8.5" % Test
val scalaHttp = "org.scalaj"    %% "scalaj-http"  % "2.4.0"

lazy val commonSettings = Seq(
  organization := "sarginson",
  version      := "0.1.0",
  scalaVersion := "2.12.6"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "sbtTddMetricsTracker",
    sbtPlugin := true,
    libraryDependencies ++= Seq(scalaTest, scalaHttp, mockito)
  )
