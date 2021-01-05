package akkastreams.primer

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink, Source}
import akkastreams.util.Data.{numbersRange, sentences}
import akkastreams.util.SystemSetup

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MaterializingStreams extends SystemSetup with App {

  val source = Source(numbersRange)
  val flow = Flow[Int].map(_ * 2)
  val sink = Sink.foreach[Int](println)

  // Graph is just a blue print of a stream and does nothing
  val graph = source.via(flow).to(sink)

  // When we run the graph, it allocates resources like thread pools and actors to run it
  // This yields a result which is called a materialized value
  val result: NotUsed = graph.run()

  /*
     1. Return last element of the source
     2. Compute the total word count of a stream of sentences
   */

  import actorSystem.dispatcher

  val source1 = Source(numbersRange)
  val sink1: Sink[Int, Future[Int]] = Sink.last[Int]
  source1.runWith(sink1) onComplete {
    case Success(data) => println(s"The last element of source is $data")
    case Failure(ex) => println(s"Unable to fetch the last element of the stream. Failed with exception $ex")
  }

  val sentencesSource = Source(sentences)
  val eventualWordCount: Future[Int] = sentencesSource
    .via(Flow[String].map(sentence => sentence.split(" ").map(_.length).sum))
    .via(Flow[Int].reduce(_ + _))
    .runWith(Sink.head[Int])

  eventualWordCount onComplete {
    case Success(data) => println(s"The total word count is $data")
    case Failure(ex) => println(s"Unable to fetch the word count of the stream. Failed with exception $ex")
  }
}
