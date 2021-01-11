package fileuploader.models

import play.api.libs.json._

sealed trait FileType {
  val `type`: String
}

object FileType {

  implicit object EventTypeFormat extends Format[FileType] {
    implicit def reads(json: JsValue): JsResult[FileType] =
      json match {
        case JsString(File.`type`) => JsSuccess(File)
        case JsString(Folder.`type`) => JsSuccess(Folder)
        case _ => JsError(s"Cannot parse FileType $json")
      }

    implicit def writes(model: FileType): JsString = JsString(model.`type`.toString)
  }
}

case object File extends FileType {
  override val `type`: String = "file"
}

case object Folder extends FileType {
  override val `type`: String = "folder"
}