package fileuploader.models

import play.api.libs.json._

sealed trait EventType {
  val name: String
}

case object FileUpload extends EventType {
  override val name: String = "file_upload"
}

object EventType {

  implicit object EventTypeFormat extends Format[EventType] {
    implicit def reads(json: JsValue): JsResult[EventType] =
      json match {
        case JsString(FileUpload.name) => JsSuccess(FileUpload)
        case _ => JsError(s"cannot parse EventType $json")
      }

    implicit def writes(eventType: EventType): JsString = JsString(eventType.name.toString)
  }
}