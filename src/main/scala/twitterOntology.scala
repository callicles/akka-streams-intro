import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.alpakka.s3.acl.CannedAcl
import akka.stream.alpakka.s3.auth.AWSCredentials
import akka.stream.alpakka.s3.scaladsl.S3Client
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import twitter.{Tweet, Twitter}
import io.circe.parser._
import io.circe.generic.auto._

import scala.concurrent.Future

object twitterOntology extends App {

  val conf = ConfigFactory.load()

  implicit val system = ActorSystem("twitter")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  val s3ClientId = conf.getString("aws.clientId")
  val s3SecretToken = conf.getString("aws.clientSecret")
  val bucket = conf.getString("aws.bucket")

  println(s3ClientId)
  println(s3SecretToken)
  val s3Client = new S3Client(AWSCredentials(s3ClientId, s3SecretToken), "us-east-1")

  val connectionFlow = Http().outgoingConnectionHttps("stream.twitter.com")
  val url = "https://stream.twitter.com/1.1/statuses/sample.json?stall_warnings=true"
  val twitter = new Twitter()

  var parsedTweet = 0

  twitter
    .generateGetAuthHeaders(url)
    .flatMap { authHeader =>

      val streamRequest: HttpRequest = HttpRequest(
        method = HttpMethods.GET,
        uri = Uri(url),
        headers = List(authHeader)
      )

      Source
        .single(streamRequest)
        .via(connectionFlow)
        .runWith(Sink.head)
    }
    .flatMap { response =>
      if (response.status.intValue() != 200) {
        println(response.entity.dataBytes.runForeach(_.utf8String))
        Future.successful(Unit)
      } else {
        response.entity.withSizeLimit(-1).dataBytes
          .scan("")((acc, curr) => if (acc.contains("\r\n")) curr.utf8String else acc + curr.utf8String)
          .buffer(10, OverflowStrategy.dropNew)
          .filter(_.contains("\r\n"))
          //.log("incoming", println)
          .filter {
            case jsonString if jsonString.startsWith("""{"disconnect""") =>
              println(s"Disconnected: $jsonString")
              false
            case jsonString if jsonString.startsWith("""{"warning""") =>
              println(s"Warning: $jsonString")
              false
            case _ => true
          }
          .map(json => decode[Tweet](json))
          .collect {
            case Right(tweet) =>
              parsedTweet += 1
              print(s"\r Processed Tweets: $parsedTweet")
              tweet
          }
          .map(tokenizer.tokenizeTweet)
          .map(tokens => ByteString(tokens.mkString(",") + "\n"))
          .runWith(s3Client.multipartUpload(
            bucket, "twitter_tokenized_tweets/tweets.txt", cannedAcl = CannedAcl.BucketOwnerFullControl
          ))
      }
    }
    .onFailure {
      case e =>
        println(e)
        system.terminate()
    }
}






