import sbt._
import Keys._
import trafficland.opensource.sbt.plugins._
import trafficland.sbt.plugins._

object ApplicationBuild extends Build {
  import Dependencies._

  val appName         = "$name$"
  val appVersion      = "0.1.0-SNAPSHOT".toReleaseFormat

  resolvers ++= resolutionRepos

  val appDependencies = compileDeps ++ testDeps

  lazy val UnitTest = config("unit") extend Test

  val main = play.Project(
    appName,
    appVersion,
    appDependencies
  )
  .settings(Play20PluginSet.plugs : _*)
  .configs(UnitTest)
  .settings(inConfig(UnitTest)(Defaults.testTasks) : _*)
  .settings(
    resolvers ++= resolutionRepos,
    testOptions in Test := Seq(
      Tests.Setup { () => System.setProperty("config.file", "conf/test.conf") }
    ),
    testOptions in UnitTest := Seq(
      Tests.Setup { () => System.setProperty("config.file", "conf/test.conf") },
      Tests.Filter { _.contains(".unit.") }
    ),
    parallelExecution in Test := false,
    parallelExecution in UnitTest := false,
    sbt.Keys.fork in Test := false
  )
}

object Dependencies {
  val resolutionRepos = Seq(
  )

  object V {
    val tlcommons = "1.2"
    val reactive  = "0.8"
    val scalatest = "2.0.M5b"
  }

  val compileDeps = Seq(
    "com.trafficland"         %% "tlcommons"               % V.tlcommons,
    "org.reactivemongo"       %% "reactivemongo"           % V.reactive,
    "com.google.inject"       %  "guice"                   % "3.0",
  )

  val testDeps = Seq(
    "org.scalatest"           %% "scalatest"         % V.scalatest
  )
}