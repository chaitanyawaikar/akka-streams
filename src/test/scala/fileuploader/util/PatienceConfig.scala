package fileuploader.util

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

trait PatienceConfig extends ScalaFutures {

  implicit val customPatienceConfig = PatienceConfig(timeout = Span(50, Seconds), interval = Span(500, Millis))
}
