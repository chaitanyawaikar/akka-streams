import Versions._
import sbt._

object TestLibs {

  private val dockerTestLibs: Seq[ModuleID] = Seq(
    "com.whisk" %% "docker-testkit-scalatest" % "0.9.9" % "test",
    "com.whisk" %% "docker-testkit-impl-spotify" % "0.9.9" % "test")

  private val testLibs: Seq[sbt.ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "3.2.3" % Test,
    "org.scalamock" %% "scalamock" % "5.1.0" % Test,
    "org.mockito" % "mockito-core" % "3.7.7" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,
    "com.amazonaws" % "aws-java-sdk-sqs" % "1.11.939"
  )

  val testDependencies: Seq[sbt.ModuleID] = dockerTestLibs ++ testLibs
}
