import sbt._
import Keys._
import play.sbt.PlayImport._

object Dependencies {

  val portalDependencies: Seq[ModuleID] = Seq(
    javaCore,
    javaJdbc,
    javaWs,
    cache,
    evolutions,
    "org.webjars" %% "webjars-play" % "2.4.0-2",
    "org.webjars" % "bootstrap-datepicker" % "1.4.0",
    "org.apache.commons" % "commons-exec" % "1.3",
    "org.julienrf" %% "play-jsmessages" % "2.0.0",
    "be.objectify" %% "deadbolt-java" % "2.4.3",
    "com.feth" %% "play-authenticate" % "0.7.0-SNAPSHOT",
    "org.webjars" % "bootstrap" % "3.2.0",
    "org.webjars" % "bootstrap-timepicker" % "0.2.3-1",
    "org.webjars" % "tablesorter" % "2.15.5",
    "org.webjars" % "momentjs" % "2.10.2",
    "org.webjars" % "knockout" % "3.3.0",
    "org.webjars" % "select2" % "3.4.6",
    "org.webjars" % "underscorejs" % "1.6.0-3",
    "com.github.javafaker" % "javafaker" % "0.3" exclude("org.slf4j", "slf4j-simple"),
    "commons-io" % "commons-io" % "2.4"
  )
}