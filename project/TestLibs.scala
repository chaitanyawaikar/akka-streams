import Versions._
import sbt._

object TestLibs {

  val testDependencies: Seq[sbt.ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "3.2.3" % Test,
    "org.scalamock" %% "scalamock" % "5.1.0" % Test,
    "org.mockito" % "mockito-core" % "3.7.7" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test
  )
}
