package fileuploader.gateway

import akka.actor.ActorSystem
import com.github.matsluni.akkahttpspi.AkkaHttpClient
import com.typesafe.config.{Config, ConfigFactory}
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.core.internal.retry.SdkDefaultRetrySetting
import software.amazon.awssdk.core.retry.RetryPolicy
import software.amazon.awssdk.core.retry.backoff.BackoffStrategy
import software.amazon.awssdk.core.retry.conditions.RetryCondition
import software.amazon.awssdk.http.async.SdkAsyncHttpClient

object AWSUtils {

  implicit lazy val actorSystem: ActorSystem = ActorSystem("file-uploader")
  lazy val envConfig: Config = ConfigFactory.systemEnvironment()
  lazy val credentialsProvider: StaticCredentialsProvider =
    StaticCredentialsProvider.create(
      AwsBasicCredentials.create(
        envConfig.getString("AWS_ACCESS_KEY_ID"),
        envConfig.getString("AWS_SECRET_KEY_ID")
      )
    )
  lazy val config: Config = ConfigFactory.load()
  lazy val snsTopicArn: String = config.getString("aws.sns-arn")
  lazy val sqsTopicUrl: String = config.getString("aws.sqs-arn")
  lazy val httpClient: SdkAsyncHttpClient =
    AkkaHttpClient
      .builder()
      .withActorSystem(actorSystem)
      .build()

  lazy val overrideConfig: ClientOverrideConfiguration = {
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
