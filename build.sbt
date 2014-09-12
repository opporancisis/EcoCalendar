name := baseDirectory.value.getName

scalaVersion := "2.10.4"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava,SbtWeb)

TwirlKeys.templateImports += "play.data.format.Formatters"

LessKeys.compress in Assets := true

pipelineStages := Seq(uglify, digest, gzip)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

resolvers ++= Seq(
	Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
	Resolver.url("Objectify Play Snapshot Repository", url("http://schaloner.github.com/snapshots/"))(Resolver.ivyStylePatterns),
	"Objectify Play Repository (release)" at "http://schaloner.github.com/releases/",
	"Objectify Play Repository (snapshot)" at "http://schaloner.github.com/snapshots/",
   "play-easymail (release)" at "http://joscha.github.io/play-easymail/repo/releases/",
   "play-easymail (snapshot)" at "http://joscha.github.io/play-easymail/repo/snapshots/",
   "play-authenticate (release)" at "http://joscha.github.io/play-authenticate/repo/releases/",
   "play-authenticate (snapshot)" at "http://joscha.github.io/play-authenticate/repo/snapshots/"
)

libraryDependencies ++= Seq(
  javaCore,
  javaJdbc,
  javaEbean,
  javaWs,
  cache,
  "be.objectify" %% "deadbolt-java" % "2.3.0-RC1",
  "com.feth" %% "play-authenticate" % "0.6.5-SNAPSHOT",
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.webjars" % "tablesorter" % "2.15.5",
  "org.webjars" % "d3js" % "3.4.4-1",
  "org.webjars" % "nvd3" % "1.1.15-beta-1",
  "org.webjars" % "momentjs" % "2.8.1-1",
  "org.webjars" % "bootstrap-datepicker" % "1.3.0-1",
  "org.webjars" % "knockout" % "3.1.0",
  "org.webjars" % "select2" % "3.4.6",
  "org.webjars" % "underscorejs" % "1.6.0-3",
  "org.webjars" % "font-awesome" % "4.1.0",
  "com.github.javafaker" % "javafaker" % "0.3" exclude("org.slf4j", "slf4j-simple"),
  "org.apache.poi" % "poi" % "3.10-FINAL",
  "avalon-framework" % "avalon-framework-api" % "4.2.0",
  "avalon-framework" % "avalon-framework-impl" % "4.2.0",
  "org.apache.xmlgraphics" % "fop" % "1.1" exclude("org.apache.avalon.framework", "avalon-framework-api") exclude("org.apache.avalon.framework", "avalon-framework-impl"),
  "commons-io" % "commons-io" % "2.4",
  "org.apache.commons" % "commons-exec" % "1.2",
  "org.julienrf" %% "play-jsmessages" % "1.6.2"
)

