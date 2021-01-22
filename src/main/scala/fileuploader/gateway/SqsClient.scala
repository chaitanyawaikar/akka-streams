package fileuploader.gateway

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.alpakka.sqs.scaladsl.SqsSource
import akka.stream.alpakka.sqs.{MessageAction, SqsSourceSettings}
import akka.stream.scaladsl.{Flow, RestartSource, Sink, Source}
import akka.stream.{ActorAttributes, Supervision}
import fileuploader.models.Event
import play.api.libs.json.Json
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.Message

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class SqsClient(sqsTopicUrl: String)(implicit sqsClient: SqsAsyncClient,
                                     actorSystem: ActorSystem) {

  def pollMessages(): Future[Unit] = {
    for {
      _ <-
        getSource
          .via(getFlow)
          .runWith(Sink.ignore)
    } yield ()
  }

  private[gateway] def getSource: Source[Message, NotUsed] = {
    val supervisionStrategy: Supervision.Decider = Supervision.stoppingDecider
    val sourceSettings = SqsSourceSettings()
      .withWaitTime(10 seconds)
      .withMaxBufferSize(150)
      .withMaxBatchSize(10)
    val sqsSource = SqsSource(
      sqsTopicUrl,
      sourceSettings
    ).withAttributes(ActorAttributes.supervisionStrategy(supervisionStrategy))

    RestartSource.onFailuresWithBackoff(
      minBackoff = 300.millis,
      maxBackoff = 5.seconds,
      randomFactor = 0.25
    )(() => sqsSource)
  }

  private[gateway] def getFlow: Flow[Message, Unit, NotUsed] = {

    def processMessage(message: Message): Future[Event] = {
      val event = Json.parse(message.body()).as[Event]
      println(s"Processing event => $event")
      Future.successful(event)
    }

    Flow[Message].mapAsyncUnordered(8) {
      message =>
        for {
          _ <- processMessage(message)
          _ <- Future.successful(MessageAction.delete(message))
        } yield ()
    }
  }

  actorSystem.registerOnTermination(sqsClient.close())
}
