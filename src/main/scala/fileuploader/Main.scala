package fileuploader

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import fileuploader.gateway.sns.SNSPublisher
import fileuploader.service.FileUploaderService

import scala.util.{Failure, Success}

object Main extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("file-uploader")
  implicit val systemEnv: Config = ConfigFactory.systemEnvironment()

  import scala.concurrent.ExecutionContext.Implicits.global

  private lazy val snsPublisher = new SNSPublisher()
  private lazy val fileUploaderService = new FileUploaderService(snsPublisher)

  fileUploaderService.uploadFiles("/Users/chaitanyawaikar/Desktop") onComplete {
    case Success(_) => println("All events fired to SNS")
    case Failure(ex) => println(s"Failed with exception $ex")
  }
}
