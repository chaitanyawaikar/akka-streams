package fileuploader.gateway.sns

import akka.Done
import akka.actor.ActorSystem
import akka.stream.alpakka.sns.scaladsl.SnsPublisher
import akka.stream.scaladsl.{Sink, Source}
import com.github.matsluni.akkahttpspi.AkkaHttpClient
import com.typesafe.config.Config
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.core.internal.retry.SdkDefaultRetrySetting
import software.amazon.awssdk.core.retry.RetryPolicy
import software.amazon.awssdk.core.retry.backoff.BackoffStrategy
import software.amazon.awssdk.core.retry.conditions.RetryCondition
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsAsyncClient

import scala.concurrent.Future

class SNSPublisher(implicit val config: Config, system: ActorSystem) {

  private val credentialsProvider = StaticCredentialsProvider.create(
    AwsBasicCredentials.create(
      config.getString("AWS_ACCESS_KEY_ID"),
      config.getString("AWS_SECRET_KEY_ID")
    )
  )
  private val snsTopicArn = "arn:aws:sns:eu-central-1:170463560535:file-events"

  private implicit val awsSnsClient: SnsAsyncClient =
    SnsAsyncClient
      .builder()
      .credentialsProvider(credentialsProvider)
      .region(Region.EU_CENTRAL_1)
      .httpClient(AkkaHttpClient.builder().withActorSystem(system).build())
      .overrideConfiguration(overrideConfig)
      .build()

  def publishMessage(message: String): Future[Done] = {
    Source
      .single(message)
      .via(SnsPublisher.flow(snsTopicArn))
      .runWith(Sink.foreach(res => println(res.messageId())))
  }

  system.registerOnTermination(awsSnsClient.close())

  private def overrideConfig: ClientOverrideConfiguration = {
    ClientOverrideConfiguration
      .builder()
      .retryPolicy(
        RetryPolicy.builder
          .backoffStrategy(BackoffStrategy.defaultStrategy)
          .throttlingBackoffStrategy(BackoffStrategy.defaultThrottlingStrategy)
          .numRetries(SdkDefaultRetrySetting.DEFAULT_MAX_RETRIES)
          .retryCondition(RetryCondition.defaultRetryCondition)
          .build
      )
      .build()
  }
}
