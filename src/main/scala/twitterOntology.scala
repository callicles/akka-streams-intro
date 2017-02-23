import akka.stream.{ActorMaterializer, KillSwitch, KillSwitches, OverflowStrategy}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.alpakka.s3.acl.CannedAcl
import akka.stream.alpakka.s3.auth.AWSCredentials
import akka.stream.alpakka.s3.scaladsl.S3Client
import akka.stream.scaladsl.{Framing, Keep,}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import twitter.{Tweet, Twitter}
import io.circe.parser._
import io.circe.generic.auto._

object twitterOntology extends App {

  val conf = ConfigFactory.load()

  implicit val system = ActorSystem("twitter")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  val s3ClientId = conf.getString("aws.clientId")
  val s3SecretToken = conf.getString("aws.clientSecret")
  val bucket = conf.getString("aws.bucket")

  val s3Client = new S3Client(AWSCredentials(s3ClientId, s3SecretToken), "us-east-1")

  val connectionFlow = Http().outgoingConnectionHttps("stream.twitter.com")

  val twitter = new Twitter()

  var parsedTweet = 0

  println("Starting tweet collection ...")

  val (killSwitch: KillSwitch, uploadResultFut) = twitter
    .tweetSource
    .via(Framing.delimiter(ByteString("\r\n"), maximumFrameLength = Int.MaxValue))
    .viaMat(KillSwitches.single[ByteString])(Keep.right)
    .filter(_.nonEmpty)
    .map(_.utf8String)
    .buffer(10, OverflowStrategy.dropNew)
    .map(json => decode[Tweet](json))
    .collect {
      case Right(tweet) =>
        parsedTweet += 1

        print(s"\r Processed Tweets: $parsedTweet")
        tweet
    }
    .map(tweet =>  ByteString(tokenizer.tokenizeTweet(tweet).mkString(",") + "\n"))
    .toMat(s3Client.multipartUpload(
      bucket, "twitter_tokenized_tweets/tweets.txt", cannedAcl = CannedAcl.BucketOwnerFullControl
    ))(Keep.both)
    .run()

  while (parsedTweet < 1000){
    Thread.sleep(1000)
  }

  killSwitch.shutdown()
  uploadResultFut.map(_ => println("\nUpload to S3 completed"))
}






