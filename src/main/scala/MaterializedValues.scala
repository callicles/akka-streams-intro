import akka.NotUsed
import akka.stream.scaladsl.Tcp.OutgoingConnection
import akka.stream.scaladsl.{Flow, Sink, Source, Tcp}
import akka.util.ByteString

import scala.concurrent.Future

object MaterializedValues extends App {

  // Outputs ints and doesn't materializes any value
  val intSource: Source[Int, NotUsed] = Source(List(1, 2, 3))

  // Inputs and outputs are BytesStrings and materialized value
  // is a Future[OutgoingConnection]
  val killSwitchFlow: Flow[ByteString, ByteString, Future[OutgoingConnection]] =
    Tcp().outgoingConnection("localhost", 8080)

  // First value is the input, second is the materialized value
  val intSink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)
}
