package akkastreamsprimer

import akka.Done
import akka.stream.scaladsl.{Flow, Sink, Source}
import akkastreamsprimer.util.SystemSetup

import scala.concurrent.Future

object FirstPrinciples extends SystemSetup with App {

  val source = Source(1 to 10)
  val sink: Sink[Int, Future[Done]] = Sink.foreach[Int](println)

  val graph = source.to(sink)
  graph.run()

  // Transform elements from the source
  val flow = Flow[Int].map(x => x * 2)
  val sourceWithFlow = source.via(flow)
  val sinkWithFlow = flow.to(sink)

  // Complete picture
  source
    .via(flow)
    .to(sink)
    .run()

  // Types of sources
  val finiteSource1 = Source.single(1)
  val finiteSource2 = Source.single(List(1, 2, 3, 4, 5, 6))
  val emptySource = Source.empty[Int]
  val infiniteSource = Source(LazyList.from(1))
  // Source from future
  val futureSource = Source.future(Future.successful(1))

  // Types of Sink
  val sinkThatDiscardsElements = Sink.ignore
  val foreachSink = Sink.foreach[Int](println)
  val headSink = Sink.head[Int]
  val foldingSink = Sink.fold[Int, Int](0)((a, b) => a + b)

  // Types of flow
  val mapFlow = Flow[Int].map(x => x * 2)
  val takeFlow = Flow[Int].take(4) // take first 4 elements from the stream and then close the stream and convert the first 4 elements to a finite stream
  val filterFlow = Flow[Int].filter(x => x % 2 == 0)
  // We do not have a FLATMAP in FLOW

  // Examples of sources, sinks and flows

  // Source -> Flow -> Flow -> Sink
  val graphExample1 =
    Source(1 to 10)
      .via(mapFlow)
      .via(filterFlow)
      .to(sink)
      .run()
  val graphExample1SyntacticSugar1 = Source(1 to 10).runForeach(println)
  val graphExample1SyntacticSugar2 = Source(1 to 10).map(x => x * 2).runForeach(println)

  /*
  Exercise:-  Create a stream that takes names of persons and returns the first 2 names with length > 5 characters
  */
  val personList: List[String] = List("Sam", "Tim", "Jonathan", "TimSouthee", "SteveAnderson", "RogerFederer")
  val exerciseGraph = Source(personList)
    .via(Flow[String].filter(_.length > 5))
    .via(Flow[String].take(2))
    .to(Sink.foreach(println))
    .run()

  // with syntactic sugar
  Source(personList)
    .filter(_.length > 5)
    .take(2)
    .to(Sink.foreach(println))
    .run()
}
