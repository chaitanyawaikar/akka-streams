package fileuploader.gateway

import akka.Done
import akka.stream.alpakka.sns.scaladsl.SnsPublisher
import akka.stream.scaladsl.{Sink, Source}
import fileuploader.gateway.AWSUtils._
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsAsyncClient

import scala.concurrent.Future

class SNSClient() {

  private implicit val snsClient: SnsAsyncClient =
    SnsAsyncClient
      .builder()
      .credentialsProvider(credentialsProvider)
      .region(Region.EU_CENTRAL_1)
      .httpClient(httpClient)
      .overrideConfiguration(overrideConfig)
      .build()

  def publishMessage(message: String): Future[Done] = {
    Source
      .single(message)
      .via(SnsPublisher.flow(snsTopicArn))
      .runWith(Sink.foreach(res => println(res.messageId())))
  }

  actorSystem.registerOnTermination(snsClient.close())
}
