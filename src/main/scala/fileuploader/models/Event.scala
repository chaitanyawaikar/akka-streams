package fileuploader.models

import java.time.OffsetDateTime

import play.api.libs.json.{Json, OFormat}

case class Event(
             eventId: String,
             eventType: EventType,
             fileType: FileType,
             filePath: String,
             createdAt: OffsetDateTime
           )

object Event {

  implicit val eventFormat: OFormat[Event] = Json.format[Event]

}
