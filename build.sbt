import Dependencies._
import play.sbt.PlayImport._

lazy val ecocalendar = (project in file(".")).
  settings(Commons.commonSettings: _*).
  settings(
    libraryDependencies ++= portalDependencies
  ).
  enablePlugins(PlayJava,SbtWeb,PlayEbean)
