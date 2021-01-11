package fileuploader.service

import java.io.File
import java.time.OffsetDateTime

import akka.Done
import fileuploader.gateway.sns.SNSPublisher
import fileuploader.models.{Event, FileType, FileUpload, Folder, File => CustomFile}
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

class FileUploaderService(
                           snsPublisher: SNSPublisher
                         )(implicit val executionContext: ExecutionContext) {

  def uploadFiles(filePath: String): Future[List[Done]] = {
    Future.sequence(
      getInternalFiles(filePath)
        .map(convertToFileEvent)
        .map(x => Json.toJson(x).toString())
        .map(snsPublisher.publishMessage)
    )
  }

  private def getInternalFiles(filePath: String): List[File] = {
    val directory = new File(filePath)
    val files = if (directory.exists() && directory.isDirectory) {
      directory.listFiles().toList.filter(_.isFile)
    } else List[File]()
    println(s"File count ${files.size}")
    files
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