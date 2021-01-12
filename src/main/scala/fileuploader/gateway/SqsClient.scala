package fileuploader.gateway

import akka.stream.alpakka.sqs.SqsSourceSettings
import akka.stream.alpakka.sqs.scaladsl.SqsSource
import akka.stream.scaladsl.Sink
import fileuploader.gateway.AWSUtils._
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.Message

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class SqsClient() {

  implicit val sqsClient: SqsAsyncClient =
    SqsAsyncClient
      .builder()
      .credentialsProvider(credentialsProvider)
      .region(Region.EU_CENTRAL_1)
      .httpClient(httpClient)
      .overrideConfiguration(overrideConfig)
      .build()

  def pollMessages(): Future[Seq[Message]] = {
    for {
      messages <- SqsSource(
        sqsTopicArn,
        SqsSourceSettings().withCloseOnEmptyReceive(true).withWaitTime(10.millis)
      ).runWith(Sink.seq)
    } yield messages
  }

  actorSystem.registerOnTermination(sqsClient.close())
}
