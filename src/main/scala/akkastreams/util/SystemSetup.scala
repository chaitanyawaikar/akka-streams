package akkastreams.util

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}

trait SystemSetup {

  implicit val actorSystem: ActorSystem = ActorSystem("Akka-streams-primer")
  implicit val materializer: Materializer = ActorMaterializer()
}
