package akkastreams.primer

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import akkastreams.util.SystemSetup
import scala.language.postfixOps

object BackPressureBasics extends SystemSetup with App {

  val fastSource = Source(1 to 1000)
  val fastSink = Sink.foreach[Int](println)

  // Consumers are the ones who decide the flow of elements.
  // Back pressure is all about synchronization b/w source and sink.

  val slowSink = Sink.foreach[Int] { x =>
    Thread.sleep(1000)
    println(s"Sink $x")
  }

  // The below code will print the elements in a sync manner because it runs the code on
  // a single actor. Hence there is no backpressure involved here.
  //  fastSource.runWith(slowSink)

  // However, the moment we involve the .async method,
  // it runs the messages on a separate actor and there the concept of backpressure kicks in.

  val flow = Flow[Int].map { x =>
    val elem = x + 1
    println(s"Flow $elem")
    elem
  }

  fastSource.async
    .via(flow).async
    .runWith(slowSink)
  /*
  The following code produces output :-
      Flow 2
      Flow 3
      Flow 4
      Flow 5
      Flow 6
      Flow 7
      Flow 8
      Flow 9
      Flow 10
      Flow 11
      Flow 12
      Flow 13
      Flow 14
      Flow 15
      Flow 16
      Flow 17
      Sink 2
      Sink 3
      Sink 4

      Here the sink is slow and hence it tells the flow component to stop sending elements and hence flow component
      further sends this message to the source component to slow it down. But, before doing this, the flow component,
      fills the internal buffer size which is 16 by default and hence we see only 16 elements processed by the flow.
   */

  /*
    Strategies for back-pressure
    1. Try to slow down the stream if possible
    2. Buffer elements until demand exists
    3. Drop down elements from the buffer after it overflows
    4. kill the whole stream itself
   */

  val bufferedFlow = flow.buffer(10, OverflowStrategy.backpressure)

  fastSource.async
    .via(bufferedFlow).async
    .runWith(slowSink)

  // Manual back-pressure => Throttling
  import scala.concurrent.duration._
  fastSource
    .throttle(2, 1 second)
    .runWith(Sink.foreach(println))

}
