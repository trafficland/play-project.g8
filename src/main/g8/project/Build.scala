import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {
  import Dependencies._

  val appName         = "$name$"
  val appVersion      = "0.1.0-SNAPSHOT"

  resolvers ++= resolutionRepos

  val appDependencies = compileDeps ++ testDeps

  lazy val UnitTest = config("unit") extend Test

  val dist = TaskKey[File]("dist", "Build the standalone application package")
  val distTask = (distDirectory, baseDirectory, playPackageEverything, dependencyClasspath in Runtime, target, normalizedName, version) map {
    (dist, root, packaged, dependencies, target, id, version) =>
      val packageName = id + "-" + version
      val packageDirectory = packageName + "/" + id
      val zip = dist / (packageName + ".zip")

      IO delete dist
      IO createDirectory dist

      val libs = {
        dependencies.filter(_.data.ext == "jar").map { dependency =>
          val filename = for {
            module <- dependency.metadata.get(AttributeKey[ModuleID]("module-id"))
            artifact <- dependency.metadata.get(AttributeKey[Artifact]("artifact"))
          } yield {
            module.organization + "." + module.name + "-" + Option(artifact.name.replace(module.name, "")).filterNot(_.isEmpty).map(_ + "-").getOrElse("") + module.revision + ".jar"
          }
          val path = ("lib/" + filename.getOrElse(dependency.data.getName))
          dependency.data -> path
        } ++ packaged.map(jar => jar -> ("/lib/" + jar.getName))
      }

      val start = target / "start"

      val customConfig = Option(System.getProperty("config.file"))
      val customFileName = customConfig.map(f => Some((new File(f)).getName)).getOrElse(None)

      IO.write(start,
        """#!/usr/bin/env sh
scriptdir=`dirname \$0`
classpath=""" + libs.map { case (jar, path) => "\$scriptdir/" + path }.mkString("\"", ":", "\"") + """
exec /opt/java \$* -cp \$classpath """ + customFileName.map(fn => "-Dconfig.file=`dirname \$0`/conf/" + fn + " ").getOrElse("-Dconfig.file=`dirname \$0`/conf/application.conf ") + """play.core.server.NettyServer `dirname \$0`
                                                                                                                                                                                """ /* */ )
      val scripts = Seq(start -> (packageDirectory + "/start"))

      val other = Seq((root / "README") -> (packageDirectory + "/README"))

      val productionConfig = customFileName.map(fn => target / fn).getOrElse(target / "application.conf")

      val prodApplicationConf = customConfig.map { location =>
        val customConfigFile = new File(location)
        IO.copyFile(customConfigFile, productionConfig)
        Seq(productionConfig -> (packageDirectory + "/conf/" + customConfigFile.getName))
      }.getOrElse(Nil)

      val defaultApplicationConf = Seq(new File("conf/application.conf") -> (packageDirectory + "/conf/application.conf"))

      IO.zip(libs.map { case (jar, path) => jar -> (packageDirectory + "/" + path) } ++ scripts ++ other ++ prodApplicationConf ++ defaultApplicationConf, zip)
      IO.delete(start)
      IO.delete(productionConfig)

      println()
      println(appName + " has been packaged.  The package can be found at " + zip.getCanonicalPath + "!")
      println()

      zip
  }

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
    dist <<= distTask,
    testOptions in Test := Seq(
      Tests.Setup { () => System.setProperty("config.file", "conf/test.conf") }
    ),
    testOptions in UnitTest := Seq(
      Tests.Setup { () => System.setProperty("config.file", "conf/test.conf") },
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
    "TrafficLand Artifactory Server" at "http://build01.tl.com:8081/artifactory/repo"
  )

  object V {
    val tlcommons = "1.2"
    val reactive  = "0.8"
    val scalatest = "$scalatest_version$"
  }

  val compileDeps = Seq(
    "com.trafficland"         %% "tlcommons"               % V.tlcommons,
    "org.reactivemongo"       %% "reactivemongo"           % V.reactive
  )

  val testDeps = Seq(
    "org.scalatest"           %% "scalatest"         % V.scalatest
  )
}