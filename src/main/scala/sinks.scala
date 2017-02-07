import akka.stream.scaladsl.Sink

object sinks extends App {
  // Sink that folds over the stream and returns a Future of the final result
  Sink.fold[Int, Int](0)(_ + _)

  // Sink that returns a Future, containing the first element of the stream
  Sink.head

  // Sink that returns a Future, containing the last element of the Stream
  Sink.last

  // A Sink that consumes a stream without doing anything with the elements
  Sink.ignore

  // A Sink that executes a side-effecting call for every element of the stream
  Sink.foreach[String](println(_))
}