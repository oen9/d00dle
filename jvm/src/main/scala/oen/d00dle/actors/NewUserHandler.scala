package oen.d00dle.actors

import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future
import oen.d00dle.actors.UserDispatcher._
import oen.d00dle.actors.UserActor._
import oen.d00dle.shared.Dto._
import io.circe.generic.extras.auto._
import io.circe.syntax._
import io.circe.parser._
import akka.NotUsed
import akka.stream.typed.scaladsl.ActorSink
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Flow
import akka.stream.typed.scaladsl.ActorSource
import akka.stream.OverflowStrategy
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.DispatcherSelector
import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.model.ws.Message
import akka.actor.typed.scaladsl.AskPattern._

object NewUserHandler {
  def handle(implicit system: ActorSystem[UserDispatcherMsg]): Future[Flow[Message, Message, NotUsed]] = {
    implicit val timeout: Timeout = 3.seconds
    implicit val scheduler = system.scheduler
    implicit val executionContext = system.dispatchers.lookup(DispatcherSelector.default())

    val userCreated: Future[UserDispatcher.UserCreated] = system.ask(CreateUser.apply)

    def createSink(userRef: ActorRef[UserActorMsg]) = {
      val userSink = ActorSink.actorRef[UserActorMsg](ref = userRef, onCompleteMessage = Complete, onFailureMessage = Fail.apply)
      Flow[Message]
        .flatMapConcat {
          case TextMessage.Strict(msgText) =>
            val decoded = decode[WsData](msgText).fold(e => Err(e.getMessage()), identity)
            Source.single(InData(decoded))
          case _ =>
            Source.empty[InData]
        }.to(userSink)
    }

    def createSource(userRef: ActorRef[UserActorMsg]) = {
      ActorSource
        .actorRef[OutData](
          completionMatcher = { case Complete => },
          failureMatcher = { case Fail(ex) => ex },
          bufferSize = 64,
          overflowStrategy = OverflowStrategy.fail
        )
        .mapMaterializedValue { outActor =>
          userRef ! RegisterOut(outActor)
          NotUsed
        }
        .flatMapConcat {
          case toOut: ToOut =>
            val json = toOut.wsData.asJson.noSpaces
            Source.single(TextMessage(json))
          case unknown =>
            system.log.error("unknown msg to send through websock: {}", unknown)
            Source.empty[TextMessage]
        }
    }

    for {
      newUser <- userCreated
      userRef = newUser.user
      sink = createSink(userRef)
      source = createSource(userRef)
    } yield {
      Flow.fromSinkAndSource(sink, source)
    }
  }
}
