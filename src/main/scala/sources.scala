import akka.stream.scaladsl.Source

import scala.concurrent.Future

object sources extends App {
  // Create a source from an Iterable
  Source[Int](List(1, 2, 3))

  // Create a source from a String
  Source[Char]("A String decomposed in its characters")

  // Create a source from a Future
  Source.fromFuture[String](Future.successful("Hello Streams!"))

  // Create a source from a single element
  Source.single[String]("only one element")

  // an empty source
  Source.empty
}
