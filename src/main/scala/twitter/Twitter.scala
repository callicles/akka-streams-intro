package twitter

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpHeader, HttpMethods, HttpRequest, Uri}
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.hunorkovacs.koauth.domain.KoauthRequest
import com.hunorkovacs.koauth.service.consumer.DefaultConsumerService
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}

class Twitter(implicit val ec: ExecutionContext, actorSystem: ActorSystem) {

  private val config = ConfigFactory.load()

  private val consumerKey = config.getString("twitter.consumerKey")
  private val consumerSecret =  config.getString("twitter.consumerSecret")
  private val accessToken =  config.getString("twitter.accessToken")
  private val accessTokenSecret = config.getString("twitter.accessTokenSecret")

  private val consumer = new DefaultConsumerService(ec)

  private val url = "https://stream.twitter.com/1.1/statuses/sample.json?stall_warnings=true"

  private val connectionFlow = Http().outgoingConnectionHttps("stream.twitter.com")

  def tweetSource: Source[ByteString, NotUsed] = {
    Source
      .fromFuture(generateGetAuthHeaders(url))
      .map { authHeader =>
        HttpRequest(
          method = HttpMethods.GET,
          uri = Uri(url),
          headers = List(authHeader)
        )
      }
      .via(connectionFlow)
      .flatMapConcat(res => res.entity.withSizeLimit(-1).dataBytes)
  }

  def generateGetAuthHeaders(url: String): Future[HttpHeader] = {
    consumer.createOauthenticatedRequest(
      KoauthRequest(
        method = "GET",
        url = url,
        authorizationHeader = None,
        None
      ),
      consumerKey,
      consumerSecret,
      accessToken,
      accessTokenSecret
    ).map { res =>
      HttpHeader.parse("Authorization", res.header) match {
        case ParsingResult.Ok(h, _) => h
        case ParsingResult.Error(e) => throw new Exception(e.detail)
      }
    }
  }

}
