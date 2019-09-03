package oen.d00dle

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.actor.typed.ActorSystem
import scala.util.Random
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import oen.d00dle.actors.UserDispatcher.UserDispatcherMsg
import akka.stream.typed.scaladsl.ActorMaterializer
import akka.actor.typed.DispatcherSelector
import oen.d00dle.actors.NewUserHandler

trait AppApi {
  implicit def system: ActorSystem[UserDispatcherMsg]
  def assetsPath: String
  implicit def materializer: ActorMaterializer
  implicit val executionContext = system.dispatchers.lookup(DispatcherSelector.default())

  val routes: Route = cors() {
    getStatic ~
    jsonExample ~
    websocketRoute
  }

  def getStatic: Route = get {
    pathSingleSlash {
      getFromResource("index.html")
    } ~
    (path("favicon.ico") & get) {
      getFromResource("favicon.ico")
    } ~
    pathPrefix("front-res") {
      getFromResourceDirectory("front-res")
    } ~
    pathPrefix("assets") {
      getFromDirectory(assetsPath)
    }
  }

  def jsonExample = {
    import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
    import io.circe.generic.extras.auto._
    import oen.d00dle.shared.Dto._
    val data: Event = UserCreated(123, "test")

    pathPrefix("json") {
      get {
        path("random") {
          complete(UserCreated(Random.nextInt(100), "test"): Event)
        } ~
        complete(data)
      } ~
      post {  // httpie: http localhost:8080/json msg=hello type=Log
        entity(as[WsData]) { event =>
          system.log.info(event.toString)
          complete("ok")
        }
      }
    }
  }

  val websocketRoute =
    path("game") {
      onSuccess(NewUserHandler.handle)(handleWebSocketMessages)
    }
}

class AppApiImpl(
  val assetsPath: String,
  val system: ActorSystem[UserDispatcherMsg],
  val materializer: ActorMaterializer
) extends AppApi

object AppApi {
  def apply(assetsPath: String)(implicit system: ActorSystem[UserDispatcherMsg], materializer: ActorMaterializer): AppApi =
    new AppApiImpl(assetsPath, system, materializer)
}
