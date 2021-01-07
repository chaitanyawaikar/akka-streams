package akkastreams.graphdsl

import akka.NotUsed
import akka.stream.ClosedShape
import akka.stream.scaladsl._
import akkastreams.util.SystemSetup

object GraphBasics extends SystemSetup with App {

  val input = Source(1 to 100)
  val incrementer = Flow[Int].map(x => x + 1)
  val doubler = Flow[Int].map(x => x * 2)
  val output = Sink.foreach[(Int, Int)](println)

  /*
                   ------> Incrementer ----->
                  |                          |
          Input ->|                          |---> Output
                  |                          |
                   ------> Doubler --------->
   */

  // Step 1 -> Create the basic graph
  val graph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      // Step 2 -> Add necessary components for this graph
      // Now we need to fan out i.e send the elements from input to Incrementer and Doubler
      val broadcast = builder.add(Broadcast[Int](2))
      // Now we need to fan in i.e elements from Incrementer and Doubler back to Output
      val zip = builder.add(Zip[Int, Int])

      // Step 3 -> Joining all components together
      input ~> broadcast
      broadcast.out(0) ~> incrementer ~> zip.in0
      broadcast.out(1) ~> doubler ~> zip.in1
      zip.out ~> output
      ClosedShape // shape
    } // graph
  ) // runnable graph

    graph.run() // finally able to run the graph and materialize it

  /*
      Exercise 1 -> Feed the source into 2 sinks at the same time
   */

  val firstSink = Sink.foreach[Int](x => println(s"Element $x in Sink1"))
  val secondSink = Sink.foreach[Int](x => println(s"Element $x in Sink2"))

  val exerciseGraph1 = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val broadcast = builder.add(Broadcast[Int](2))
      input ~> broadcast ~> firstSink
      broadcast ~> secondSink
      ClosedShape
    }
  )
    exerciseGraph1.run()

  /*
      Exercise 2 -> Feed the source into 2 sinks at the same time

                   Fast Source ----->                              -----> Sink1
                                     |                            |
                                     |---> Merge ---> Balance --->|
                                     |                            |
                   Slow Source ----->                              -----> Sink1
   */

  import scala.concurrent.duration._
  val fastSource = Source(1 to 100).throttle(1, 2 seconds)
  val slowSource = Source(101 to 200).throttle(1, 5 seconds)
  val sink1 = Sink.foreach[Int](x => println(s"Element $x in Sink1"))
  val sink2 = Sink.foreach[Int](x => println(s"Element $x in Sink2"))

  val exerciseGraph2 = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      // Building the components
      val merge = builder.add(Merge[Int](2))
      val balance = builder.add(Balance[Int](2))

      // Joining all the components together
      fastSource ~> merge
      slowSource ~> merge
      merge ~> balance
      balance ~> sink1
      balance ~> sink2
      ClosedShape
    }
  )
  exerciseGraph2.run()

  /*
      Fan-out
         - Broadcast
         - Balance

      Fan-in
         - Zip / ZipWith
         - Merge
         - Concat
   */
}
