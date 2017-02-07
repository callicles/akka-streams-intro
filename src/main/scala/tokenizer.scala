
import java.io.StringReader

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import twitter.Tweet

object tokenizer {

  val analyzer = new StandardAnalyzer()

  def tokenizeTweet(tweet: Tweet): List[String] = {
    val tokenStream = analyzer.tokenStream("tweet", new StringReader(tweet.text))

    val termAttribute = tokenStream.addAttribute(classOf[CharTermAttribute])

    try {
      tokenStream.reset()

      val iterator = new Iterator[String] {
        def hasNext = tokenStream.incrementToken()
        def next() = termAttribute.toString
      }

      val tokens = iterator.toList

      tokenStream.end()
      tokens
    } finally {
      tokenStream.close() // Release resources associated with this stream.
    }
  }
}
