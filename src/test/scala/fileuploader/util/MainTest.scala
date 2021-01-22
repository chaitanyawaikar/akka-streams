package fileuploader.util

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.sqs.{AmazonSQSAsync, AmazonSQSAsyncClientBuilder}
import software.amazon.awssdk.regions.Region

object MainTest extends App {

  val endpoint = "http://localhost:9324"
  val region = "elasticmq"
  val accessKey = "x"
  val secretKey = "x"
  //  val client =
  //    AmazonSQSClientBuilder
  //      .standard
  //      .withCredentials(
  //        new AWSStaticCredentialsProvider(
  //          new BasicAWSCredentials(accessKey, secretKey)
  //        )
  //      ).withEndpointConfiguration(
  //      new AwsClientBuilder.EndpointConfiguration(endpoint, region)
  //    ).build

  val sqsAsyncClient: AmazonSQSAsync = {
    val sqsClientBuilder: AmazonSQSAsyncClientBuilder = AmazonSQSAsyncClientBuilder.standard()
    val awsStaticCredentialsProvider = new AWSStaticCredentialsProvider(
      new BasicAWSCredentials(accessKey, secretKey)
    )
    sqsClientBuilder.withCredentials(
      awsStaticCredentialsProvider
    )

    sqsClientBuilder.withEndpointConfiguration(
      new EndpointConfiguration(endpoint, Region.EU_CENTRAL_1.toString)
    )
    sqsClientBuilder.build()
  }

//  private val url: String = sqsAsyncClient.createQueue("file_uploader").getQueueUrl
//  println(url)
  val url = "http://localhost:9324/queue/file_uploader"

  (1 to 5).foreach{
    _ =>
      val value = "{}"
      sqsAsyncClient.sendMessage(url, value)
  }
}
