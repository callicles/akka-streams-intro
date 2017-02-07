package twitter

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import com.hunorkovacs.koauth.domain.KoauthRequest
import com.hunorkovacs.koauth.service.consumer.DefaultConsumerService
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}

class Twitter(implicit val ec: ExecutionContext) {

  private val config = ConfigFactory.load()

  private val consumerKey = config.getString("twitter.consumerKey")
  private val consumerSecret =  config.getString("twitter.consumerSecret")
  private val accessToken =  config.getString("twitter.accessToken")
  private val accessTokenSecret = config.getString("twitter.accessTokenSecret")

  private val consumer = new DefaultConsumerService(ec)

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
