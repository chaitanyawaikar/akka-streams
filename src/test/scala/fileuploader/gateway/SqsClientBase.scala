package fileuploader.gateway

import fileuploader.util.BaseSetup
import org.mockito.Mockito._

class SqsClientBase extends BaseSetup {

  "SqsClient" should "restart if it fails to connect to SQS" in {
    //given


    println("Hello World")
    //when

    //then
    verify(sqsClient.getSource, times(3))
  }
}
