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
    "be.objectify" %% "deadbolt-java" % "2.4.3",
    "com.feth" %% "play-authenticate" % "0.7.0-SNAPSHOT",
    "org.julienrf" %% "play-jsmessages" % "2.0.0",
    "org.webjars" %% "webjars-play" % "2.4.0-2",
    //"org.webjars.bower" % "bootstrap-validator" % "0.9.0",
    "org.webjars" % "bootstrap-datepicker" % "1.4.0",
    "org.webjars" % "bootstrap" % "3.3.5",
    "org.webjars" % "bootstrap-timepicker" % "0.2.3-1",
    //"org.webjars" % "tablesorter" % "2.15.5",
	  "org.webjars.bower" % "angular-smart-table" % "2.1.4",
    "org.webjars" % "momentjs" % "2.10.2",
    "org.webjars" % "knockout" % "3.3.0",
    "org.webjars" % "select2" % "3.4.6",
    "org.webjars" % "underscorejs" % "1.6.0-3",
    "org.webjars.bower" % "angularjs" % "1.4.7",
    "org.webjars.bower" % "angular-resource" % "1.4.7",
    "org.webjars.bower" % "angular-bootstrap" % "0.14.3",
    "org.webjars.bower" % "angular-toggle-switch" % "1.3.0",
    //"org.webjars.bower" % "oi.multiselect" % "0.2.12",
    "org.webjars.bower" % "angular-bootstrap-show-errors" % "2.3.0",
    "org.webjars.bower" % "angular-ui-select" % "0.13.2",
    "com.github.javafaker" % "javafaker" % "0.3" exclude("org.slf4j", "slf4j-simple"),
    "org.apache.commons" % "commons-exec" % "1.3",
    "commons-io" % "commons-io" % "2.4"
  )
}