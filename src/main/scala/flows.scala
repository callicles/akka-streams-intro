import akka.stream.ThrottleMode
import akka.stream.scaladsl.Source

import scala.concurrent.duration._
import scala.language.postfixOps

object flows extends App {

  // Adds 1 to each element of the list
  Source(List(1, 2, 3))
    .map(_ + 1)

  // Keep all even elements
  Source(List(1, 2, 3))
    .filter(_ % 2 == 0)

  // Consume 100 element every minute with a maximum of 200 elements
  // per minute if source is really fast
  Source(List(1, 2, 3))
    .throttle(100, 1 minute, 200, ThrottleMode.shaping)
}