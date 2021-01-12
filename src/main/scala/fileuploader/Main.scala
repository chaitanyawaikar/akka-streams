package fileuploader

import fileuploader.gateway.{SNSClient, SqsClient}
import fileuploader.service.FileUploaderService

import scala.util.{Failure, Success}

object Main extends App {

  import scala.concurrent.ExecutionContext.Implicits.global

  private lazy val snsPublisher = new SNSClient()
  private lazy val sqsClient = new SqsClient()
  private lazy val fileUploaderService = new FileUploaderService(snsPublisher, sqsClient)
  private lazy val filePath = "/Users/chaitanyawaikar/Desktop"

  fileUploaderService.uploadFiles(filePath) onComplete {
    case Success(messages) => println(s"All messages from SQS ${messages.foreach(println)}")
    case Failure(ex) => println(s"Failed with exception $ex")
  }
}
