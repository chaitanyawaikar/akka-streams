package fileuploader.gateway

import akka.actor.ActorSystem
import akka.stream.alpakka.sqs.{MessageAttributeName, SenderId, SentTimestamp, SqsSourceSettings}
import com.github.matsluni.akkahttpspi.AkkaHttpClient
import com.typesafe.config.{Config, ConfigFactory}
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.core.internal.retry.SdkDefaultRetrySetting
import software.amazon.awssdk.core.retry.RetryPolicy
import software.amazon.awssdk.core.retry.backoff.BackoffStrategy
import software.amazon.awssdk.core.retry.conditions.RetryCondition
import software.amazon.awssdk.http.async.SdkAsyncHttpClient

import scala.concurrent.duration._
import scala.collection.immutable

object AWSUtils {

  implicit lazy val actorSystem: ActorSystem = ActorSystem("file-uploader")
  lazy val envConfig: Config = ConfigFactory.systemEnvironment()
  lazy val config: Config = ConfigFactory.load()
  lazy val credentialsProvider: StaticCredentialsProvider =
    StaticCredentialsProvider.create(
      AwsBasicCredentials.create(
        envConfig.getString("AWS_ACCESS_KEY_ID"),
        envConfig.getString("AWS_SECRET_KEY_ID")
      )
    )
  lazy val snsTopicArn: String = /*config.getString("sns-arn")*/ "arn:aws:sns:eu-central-1:170463560535:file-events"
  lazy val sqsTopicArn: String = /*config.getString("sqs-arn")*/ "https://sqs.eu-central-1.amazonaws.com/170463560535/file-events"
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

  lazy val sqsSourceSettings: SqsSourceSettings = SqsSourceSettings()
    .withWaitTime(10.seconds)
    .withMaxBufferSize(100)
    .withMaxBatchSize(10)
    .withAttributes(immutable.Seq(SenderId, SentTimestamp))
    //    .withMessageAttribute(MessageAttributeName.create("bar.*"))
    .withCloseOnEmptyReceive(true)
    .withVisibilityTimeout(10.seconds)

}
