import sbt.Keys.version
import Libs._
import TestLibs._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      version := "0.1",
      scalaVersion := "2.13.4",
      libraryDependencies ++= runtimeLibs ++ testDependencies
    )),
    name := "akka-streams-primer"
  )