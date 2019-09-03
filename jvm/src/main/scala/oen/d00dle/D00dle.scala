package oen.d00dle

import cats.effect._
import scala.util.Success
import scala.util.Failure
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.Http
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.DispatcherSelector
import oen.d00dle.actors.UserDispatcher
import oen.d00dle.actors.UserDispatcher.UserDispatcherMsg
import akka.stream.typed.scaladsl.ActorMaterializer

object D00dle extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = for { // I just like cats and fp
    _ <- createServer[IO]()
  } yield ExitCode.Success

  def createServer[F[_] : Effect]() = Effect[F].delay {
    val config = ConfigFactory.load()

    val host = config.getString("http.host")
    val port = config.getInt("http.port")
    val assets = config.getString("assets")

    implicit val system: ActorSystem[UserDispatcherMsg] = ActorSystem(UserDispatcher(), "d00dle", config)
    implicit val systemUntyped = system.toUntyped
    implicit val materializer = ActorMaterializer()

    implicit val executionContext = system.dispatchers.lookup(DispatcherSelector.default())

    val api = AppApi(assets)
    val bindingFuture = Http().bindAndHandle(api.routes, host, port)

    bindingFuture.onComplete {
      case Success(serverBinding) =>
        system.log.info("Bound to {}", serverBinding.localAddress)

      case  Failure(t) =>
        system.log.error(t, "Failed to bind to {}:{}!", host, port)
        system.terminate()
    }
  }
}
