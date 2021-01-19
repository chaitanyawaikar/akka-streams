package fileuploader

import fileuploader.gateway.AWSUtils._
import fileuploader.gateway.{SNSClient, SqsClient}
import fileuploader.service.FileUploaderService

object Main extends App {

  import scala.concurrent.ExecutionContext.Implicits.global

  private lazy val snsPublisher = new SNSClient()
  private lazy val sqsClient = new SqsClient()
  private lazy val fileUploaderService = new FileUploaderService(snsPublisher, sqsClient)
  private lazy val filePath = config.getString("service.filePath")

  for {
    _ <- fileUploaderService.uploadFiles(filePath)
    _ <- sqsClient.pollMessages()
  } yield ()
}
