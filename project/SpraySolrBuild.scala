import sbt._
import sbt.Keys._

object SpraySolrBuild extends Build {

  lazy val csiService = Project(
    id = "spray-solr",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "Spray Solr",
      organization := "io.spray",
      organizationHomepage := Some(new URL("http://spray.io")),
      homepage := Some(new URL("https://github.com/Bathtor/spray-solr")),
      description := "A simple Solr DSL for Scala and Spray",
      version := "0.2-SNAPSHOT",
      startYear := Some(2012),
      licenses := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
      scalaVersion := "2.10.0-RC2",
      scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:postfixOps", "-language:implicitConversions"),
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      resolvers += "spray repo" at "http://repo.spray.io",
      //resolvers += "nightly spray" at "http://nightlies.spray.io"
      resolvers += "sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases/",
      resolvers += "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
      libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10.0-RC2" % "2.1.0-RC2",
      libraryDependencies += "com.typesafe.akka" %   "akka-slf4j_2.10.0-RC2" % "2.1.0-RC2",
      libraryDependencies += "com.typesafe.akka" %   "akka-testkit_2.10.0-RC2" % "2.1.0-RC2",
      libraryDependencies += "org.scalatest" %%  "scalatest" % "1.8-B2" cross CrossVersion.full,
      libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.6.6",
      libraryDependencies += "io.spray" %%  "spray-json" % "1.2.2" cross CrossVersion.full,
      libraryDependencies += "io.spray" % "spray-can" % "1.1-M5"
    )
  ) 
}
