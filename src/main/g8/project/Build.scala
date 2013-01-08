import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {
  import Dependencies._

  val appName         = "$name$"
  val appVersion      = "0.0.1-SNAPSHOT"

  resolvers ++= resolutionRepos

  val appDependencies = compileDeps ++ testDeps

  lazy val UnitTest = config("unit") extend Test

  val main = play.Project(
    appName,
    appVersion,
    appDependencies
  )
  .configs(UnitTest)
  .settings(inConfig(UnitTest)(Defaults.testTasks) : _*)
  .settings(defaultScalaSettings:_*)
  .settings(
    scalaVersion := "$scala_version$",
    resolvers ++= resolutionRepos,
    sourceGenerators in Compile <+= sourceManaged in Compile map { outDir: File =>
      writeVersion(outDir)
    },
    testOptions in Test := Nil,
    testOptions in UnitTest := Seq(
      Tests.Filter { _.contains(".unit.") }
    ),
    parallelExecution in Test := false,
    parallelExecution in UnitTest := false
  )

  def writeVersion(outDir: File) = {
    val file = outDir / "AppInfo.scala"
    IO.write(file,
    """package controllers
    object AppInfo {
      val version = "%s"
      val name = "%s"
      val vendor = "TrafficLand, Inc."
    }""".format(appVersion, appName))
    Seq(file)
  }
}

object Dependencies {
  val resolutionRepos = Seq(
    "TrafficLand Artifactory Server" at "http://build01.tl.com:8081/artifactory/repo",
    "sgodbillon" at "https://bitbucket.org/sgodbillon/repository/raw/master/snapshots/"
  )

  object V {
    val tlcommons = "1.1"
    val reactive  = "0.1-SNAPSHOT"
    val scalatest = "$scalatest_version$"
  }

  val compileDeps = Seq(
    "com.trafficland"     %% "tlcommons_2.10"               % V.tlcommons,
    "reactivemongo"       %  "reactivemongo_2.10.0-RC2"     % V.reactive
  )

  val testDeps = Seq(
    "org.scalatest"             % "scalatest_2.10.0-RC2"         % V.scalatest
  )
}