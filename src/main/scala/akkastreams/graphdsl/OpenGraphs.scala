package akkastreams.graphdsl

import akka.stream.{FlowShape, SinkShape, SourceShape}
import akka.stream.scaladsl._
import akkastreams.util.SystemSetup


object OpenGraphs extends SystemSetup with App {

  /*

  Combined source should emit all elements from source 1 first and then from source 2
                 Source 1 ------->
                                  |
                                  |---> CombinedSource (concat component to be used)
                                  |
                 Source 2 ------->
 */

  val source1 = Source(1 to 50)
  val source2 = Source(50 to 100)
  // Step 1 : Create a graph
  val combinedSource = Source.fromGraph(
    GraphDSL.create() { implicit buidler =>
      import GraphDSL.Implicits._

      // Step 2 : Create components required for graph
      val concat = buidler.add(Concat[Int](2))

      // Step 3 : Join the graph components
      source1 ~> concat
      source2 ~> concat
      SourceShape(concat.out) // concat has an ouput port open that we need to connect
    }
  )
  //  combinedSource.runWith(Sink.foreach[Int](println))


  /*
                   ------> Sink1
                  |
         Source ->|
                  |
                   ------> Sink2
 */

  val sink1 = Sink.foreach[Int](x => println(s"Element $x in Sink1"))
  val sink2 = Sink.foreach[Int](x => println(s"Element $x in Sink2"))

  // Step 1 : Create a graph
  val sinkGraph = Sink.fromGraph(
    GraphDSL.create() { implicit buidler =>
      import GraphDSL.Implicits._

      // Step 2 : Create components required for graph
      val broadcast = buidler.add(Broadcast[Int](2))

      // Step 3 : Join the graph components
      broadcast ~> sink1
      broadcast ~> sink2
      SinkShape(broadcast.in) // Broadcast has an input port open that we need to connect
    }
  )
  //  source1.runWith(sinkGraph)

  /*
      Exercise - Create a complex flow
                   NormalFlow ------> IncrementerFlow ------> MultiplierFlow
   */
  val incrementerFlow = Flow[Int].map(_ + 1)
  val multiplierFlow = Flow[Int].map(_ * 10)

  // In flows, we cannot join incrementerFlow
  val flowGraph = Flow.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      // Create the required components
      val incrementerShape = builder.add(incrementerFlow)
      val multiplierShape = builder.add(multiplierFlow)

      // Connect the graph components
      incrementerShape ~> multiplierShape
      FlowShape(incrementerShape.in, multiplierShape.out)
    }
  ) // this is a static graph
  source1.via(flowGraph).to(Sink.foreach[Int](println))
}
