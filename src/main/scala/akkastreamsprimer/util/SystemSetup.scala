package akkastreamsprimer.util

import akka.actor.ActorSystem
import akka.stream.Materializer

trait SystemSetup {

  implicit val actorSystem: ActorSystem = ActorSystem("Akka-streams-primer")
  implicit val materializer: Materializer = Materializer(actorSystem)
}
