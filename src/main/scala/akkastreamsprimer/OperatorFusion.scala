package akkastreamsprimer

import akka.stream.scaladsl.{Sink, Source}
import akkastreamsprimer.util.SystemSetup

object OperatorFusion extends SystemSetup with App {

  // The following code works in a sync way and guarantees ordering of elements.
  // The elements flow from source one by one and complete each of the three flows A, B and C.
  // The underlying principle is based on an actor which receives the element in the form of message one at a time and completes the 3 flows for that element.
  Source(1 to 3)
    .map(e => {println(s"Flow A, Element $e"); e})
    .map(e => {println(s"Flow B, Element $e"); e})
    .map(e => {println(s"Flow C, Element $e"); e})
    .runWith(Sink.ignore)

  //But this defeats the use of akka streams as we would like to achieve async functionality.
  // Hence we use the concept of async boundaries that would run the elements on different actors.

  Source(1 to 3)
    .map(e => {println(s"Flow A, Element $e"); e}).async
    .map(e => {println(s"Flow B, Element $e"); e}).async
    .map(e => {println(s"Flow C, Element $e"); e}).async
    .runWith(Sink.ignore)

  // When akka stream components are fused, they run on the same actor.
  // This would be highly beneficial when we need to perform some compute intensive operation.
}
