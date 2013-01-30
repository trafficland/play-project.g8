// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.1-RC2")

resolvers += Resolver.url("TrafficLand Artifactory Plugins Server", url("http://build01.tl.com:8081/artifactory/repo"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.trafficland" % "tl-sbt-plugins" % "0.6.6")