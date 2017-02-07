import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO

import scala.concurrent.ExecutionContext.Implicits.global


object together extends App {
  implicit val system = ActorSystem("count-words")
  implicit val materializer = ActorMaterializer()

  val file = Paths.get("/Users/nicolas/Google Drive/presentations/akka-streams-intro/src/main/resources/lorem.txt")

  println("Launching count ...")

  val blueprint = FileIO.fromPath(file)
    .map(_.decodeString("UTF-8"))
    .map(str => str.split("\\s+").length)
    .fold(0)((acc, length) => acc + length)

  // Materialization
  blueprint.runForeach(count => println(s"Number of words: $count"))
    .flatMap { _ =>
      system.terminate()
    }
}
