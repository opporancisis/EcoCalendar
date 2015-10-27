import sbt._
import Keys._
import play.twirl.sbt.Import._
import com.typesafe.sbt.less.Import._
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.uglify.Import._
import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.gzip.Import._
import play.sbt.PlayImport._
import play.PlayImport.PlayKeys._
import play.sbt.routes.RoutesKeys
import play.sbt.routes.RoutesKeys._

object Commons {
  val appVersion = "1.0"

  val commonSettings: Seq[Def.Setting[_]] = Seq(
    organization := "ru.ecocalendar",
    version := appVersion,
    scalaVersion := "2.11.7",
    TwirlKeys.templateImports ++= Seq(
      "play.data.format.Formatters",
      "views.html.bootstrap._",
      "be.objectify.deadbolt.java.views.html._"
    ),
    RoutesKeys.routesImport ++= Seq(
        "models.user.User"
    ),
    routesGenerator := InjectedRoutesGenerator,
    offline := true,
    LessKeys.compress in Assets := true,
    pipelineStages := Seq(uglify, digest, gzip),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a"),
    //javaOptions in Test += "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=9997",
    unmanagedResourceDirectories in Test += baseDirectory.value / "test" / "fixtures",
    unmanagedResourceDirectories in Compile += baseDirectory.value / "test" / "fixtures",
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers ++= Seq(
      Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
      Resolver.url("Objectify Play Snapshot Repository", url("http://schaloner.github.com/snapshots/"))(Resolver.ivyStylePatterns),
      "Objectify Play Repository (release)" at "http://schaloner.github.com/releases/",
      "Objectify Play Repository (snapshot)" at "http://schaloner.github.com/snapshots/",
      "play-easymail (release)" at "http://joscha.github.io/play-easymail/repo/releases/",
      "play-easymail (snapshot)" at "http://joscha.github.io/play-easymail/repo/snapshots/"
    )
  )
}