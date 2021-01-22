package fileuploader.util

import java.net.URI

import com.spotify.docker.client.{DefaultDockerClient, DockerClient}
import com.whisk.docker.DockerReadyChecker.LogLineContains
import com.whisk.docker._
import com.whisk.docker.impl.spotify.SpotifyDockerFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.concurrent.duration._

trait DockerSqsService
  extends Suite
    with DockerKit
    with BeforeAndAfterAll
    with ScalaFutures
    with PatienceConfig {

  val defaultSqsPort = 9324
  val sqsTopicArn = "sqs-arn"
  val awsAccessKey = "access-key"
  val awsSecretKey = "secret-key"

  val sqsContainer: DockerContainer = DockerContainer("s12v/elasticmq:0.13.2")
    .withPorts(defaultSqsPort -> None)
    .withReadyChecker(
      LogLineContains("SQS server has started")
        .looped(15, 1.second)
    )

  val sqsPort: Int = {
    sqsContainer
      .getPorts()
      .map(_.apply(defaultSqsPort))
      .futureValue
  }
  val sqsEndpoint = s"http://localhost:$sqsPort"

  private val client: DockerClient = {
//    DefaultDockerClient.fromEnv().build()
    DefaultDockerClient.builder()
      .uri(URI.create("unix:///var/run/docker.sock"))
      .build()
  }

  override implicit val dockerFactory: DockerFactory = new SpotifyDockerFactory(client)

  override lazy val containerManager: DockerContainerManager = {
    val containers = dockerContainers
    val client: DockerClient =     DefaultDockerClient.builder()
      .uri(URI.create("unix:///var/run/docker.sock"))
      .build()
    println(client.ping())
    println(client.listImages())
    val factory = new SpotifyDockerFactory(client)
    val executor = factory.createExecutor()
    new DockerContainerManager(containers, executor)
  }

  abstract override def dockerContainers: List[DockerContainer] =
    sqsContainer :: super.dockerContainers

  override def beforeAll(): Unit = {
    super.beforeAll()
  }
}
