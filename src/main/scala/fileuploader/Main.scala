package fileuploader

import fileuploader.gateway.AWSUtils._
import fileuploader.gateway.{SNSClient, SqsClient}
import fileuploader.service.FileUploaderService
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient

object Main extends App {

  import scala.concurrent.ExecutionContext.Implicits.global

  private lazy val snsPublisher = new SNSClient()
  implicit val sqsAsyncClient: SqsAsyncClient =
    SqsAsyncClient
      .builder()
      .credentialsProvider(credentialsProvider)
      .region(Region.EU_CENTRAL_1)
      .httpClient(httpClient)
      .overrideConfiguration(overrideConfig)
      .build()

  private lazy val sqsClient = new SqsClient(sqsTopicUrl = sqsTopicUrl)
  private lazy val fileUploaderService = new FileUploaderService(snsPublisher, sqsClient)
  private lazy val filePath = config.getString("service.filePath")

  for {
    _ <- fileUploaderService.uploadFiles(filePath)
    _ <- sqsClient.pollMessages()
  } yield ()
}
