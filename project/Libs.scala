import Versions._
import sbt._

object Libs {

  private val akkaLibs: Seq[sbt.ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion % Test,
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
  )

  private val alpakkaLibs: Seq[sbt.ModuleID] = Seq(
    "com.lightbend.akka" %% "akka-stream-alpakka-sns" % "2.0.2",
    "com.lightbend.akka" %% "akka-stream-alpakka-sqs" % "2.0.2"
  )

  private val serializerLibs: Seq[sbt.ModuleID] = Seq(
    "com.typesafe.play" %% "play-json" % "2.9.2"
  )

  val runtimeLibs: Seq[sbt.ModuleID] = akkaLibs ++ alpakkaLibs ++ serializerLibs
}
