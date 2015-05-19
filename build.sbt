name := baseDirectory.value.getName

scalaVersion := "2.11.1"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava,SbtWeb)

TwirlKeys.templateImports ++= Seq(
	"play.data.format.Formatters",
	"views.html.bootstrap._"
)

LessKeys.compress in Assets := true

pipelineStages := Seq(rjs, uglify, digest, gzip)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")

offline := true

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
  "com.feth" %% "play-authenticate" % "0.6.8",
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.webjars" % "bootstrap-timepicker" % "0.2.3-1",
  "org.webjars" % "tablesorter" % "2.15.5",
  "org.webjars" % "momentjs" % "2.8.1-1",
  "org.webjars" % "bootstrap-datepicker" % "1.3.0-1",
  "org.webjars" % "knockout" % "3.1.0",
  "org.webjars" % "select2" % "3.4.6",
  "org.webjars" % "underscorejs" % "1.6.0-3",
  "com.github.javafaker" % "javafaker" % "0.3" exclude("org.slf4j", "slf4j-simple"),
  "commons-io" % "commons-io" % "2.4",
  "org.apache.commons" % "commons-exec" % "1.2",
  "org.julienrf" %% "play-jsmessages" % "1.6.2"
)

