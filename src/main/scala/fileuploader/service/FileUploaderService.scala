package fileuploader.service

import java.io.File
import java.time.OffsetDateTime

import fileuploader.gateway.{SNSClient, SqsClient}
import fileuploader.models.{Event, FileType, FileUpload, Folder, File => CustomFile}
import play.api.libs.json.Json
import software.amazon.awssdk.services.sqs.model.Message

import scala.concurrent.{ExecutionContext, Future}

class FileUploaderService(
                           snsPublisher: SNSClient,
                           sqsClient: SqsClient
                         )(implicit val executionContext: ExecutionContext) {

  def uploadFiles(filePath: String): Future[Seq[Message]] = {
    for {
      _ <- Future.sequence(
        getInternalFiles(filePath)
          .map(snsPublisher.publishMessage)
      )
      messagesFromSqs <- sqsClient.pollMessages()
    } yield messagesFromSqs
  }

  private def getInternalFiles(filePath: String): List[String] = {
    val directory = new File(filePath)
    val files = if (directory.exists() && directory.isDirectory) {
      directory.listFiles().toList.filter(_.isFile)
    } else List[File]()

    files
      .map(convertToFileEvent)
      .map(x => Json.toJson(x).toString())
  }

  private def convertToFileEvent(file: File): Event = {
    val now = OffsetDateTime.now()
    val fileType: FileType = if (file.isFile) CustomFile else Folder
    Event(
      eventId = now.toString,
      eventType = FileUpload,
      fileType = fileType,
      filePath = file.getAbsolutePath,
      createdAt = now
    )
  }
}