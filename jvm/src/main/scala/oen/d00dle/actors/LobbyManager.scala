package oen.d00dle.actors
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorRef
import io.scalaland.chimney.dsl._
import oen.d00dle.shared.Dto

object LobbyManager {
  sealed trait LobbyMsg
  case class RegisterNewUser(ref: ActorRef[UserActor.UserActorMsg]) extends LobbyMsg
  case class UserTerminated(ref: ActorRef[UserActor.UserActorMsg]) extends LobbyMsg
  case class GetLobbyList(respondTo: ActorRef[UserActor.UserActorMsg]) extends LobbyMsg
  case class CreateNewLobby(name: String) extends LobbyMsg

  case class LobbyData(id: Int, name: String)

  def behavior(
    counter: Int,
    lobbies: IndexedSeq[LobbyData],
    users: Set[ActorRef[UserActor.UserActorMsg]]
  ): Behavior[LobbyMsg] = Behaviors.receive { (ctx, msg) =>
    msg match{
      case RegisterNewUser(ref) =>
        ctx.log.debug("user registered: {}", ref)
        ctx.watchWith(ref, UserTerminated(ref))
        behavior(counter, lobbies, users + ref)

      case GetLobbyList(respondTo) =>
        val lobbiesToOut = lobbies.into[IndexedSeq[Dto.LobbyData]].transform
        respondTo ! UserActor.Forward(Dto.LobbyList(lobbiesToOut))
        Behavior.same

      case CreateNewLobby(name) =>
        val nextId = counter + 1
        val createdLobby = LobbyData(nextId, name)
        ctx.log.info("new lobby: {}", createdLobby)
        val broadcastData = UserActor.Forward(Dto.LobbyAdded(createdLobby.into[Dto.LobbyData].transform))
        users.foreach(_ ! broadcastData)
        behavior(nextId, createdLobby +: lobbies, users)

      case UserTerminated(ref) =>
        ctx.log.debug("user terminated: {}", ref)
        behavior(counter, lobbies, users - ref)
    }
  }

  val defaultLobbies = IndexedSeq(LobbyData(3, "foo"), LobbyData(2, "bar"), LobbyData(1, "baz"))
  def apply(): Behavior[LobbyMsg] = behavior(3, defaultLobbies, Set())
}
