package fileuploader.util

import java.net.URI

import akka.actor.ActorSystem
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.sqs.{AmazonSQSAsync, AmazonSQSAsyncClientBuilder}
import com.github.matsluni.akkahttpspi.AkkaHttpClient
import fileuploader.gateway.{AWSUtils, SqsClient}
import org.mockito.Mockito.verifyNoMoreInteractions
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.Eventually
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterEach, Inside}
import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.http.async.SdkAsyncHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.utils.AttributeMap

trait BaseSetup extends AnyFlatSpec
  with MockFactory
  with Matchers
  with Inside
  with Eventually
  with BeforeAndAfterEach
  with DockerSqsService {

  private val logger = LoggerFactory.getLogger(getClass)
  implicit val actorSystem: ActorSystem = ActorSystem("sqs-client-actor-system")
  val httpClient: SdkAsyncHttpClient = AkkaHttpClient
    .builder()
    .buildWithDefaults(AttributeMap.empty())
  val credentialsProvider: StaticCredentialsProvider = StaticCredentialsProvider.create(
    AwsBasicCredentials.create(awsAccessKey, awsSecretKey)
  )
  implicit val asyncSqsClient: SqsAsyncClient =
    SqsAsyncClient
      .builder()
      .credentialsProvider(credentialsProvider)
      .region(Region.EU_CENTRAL_1)
      .httpClient(httpClient)
      .overrideConfiguration(AWSUtils.overrideConfig)
      .build()

  val sqsClient = new SqsClient(sqsTopicArn)

  // Client to create queue in SQS
  val sqsAsyncClient: AmazonSQSAsync = {
    val sqsClientBuilder: AmazonSQSAsyncClientBuilder = AmazonSQSAsyncClientBuilder.standard()
    val awsStaticCredentialsProvider = new AWSStaticCredentialsProvider(
      new BasicAWSCredentials(awsAccessKey, awsSecretKey)
    )
    sqsClientBuilder.withCredentials(
      awsStaticCredentialsProvider
    )

    sqsClientBuilder.withEndpointConfiguration(
      new EndpointConfiguration(sqsEndpoint, Region.EU_CENTRAL_1.toString)
    )
    sqsClientBuilder.build()
  }

  override def beforeAll(): Unit = {
    initialize()
  }

  override def afterEach(): Unit = {
    verifyNoMoreInteractions(sqsClient)
  }

  override def afterAll(): Unit = {
    actorSystem.terminate()
  }

  protected[fileuploader] def createSqsQueue(sqsAsync: AmazonSQSAsync, queueName: String): String = {
    val url = new URI(sqsAsync.createQueue(queueName).getQueueUrl)
    val sqsEndpointUrl = s"$sqsEndpoint${url.getPath}"
    logger.info(sqsEndpointUrl)
    sqsEndpointUrl
  }

  protected[fileuploader] def initialize(): Unit = {
    createSqsQueue(sqsAsyncClient, "file_uploader")
  }
}
