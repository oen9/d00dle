package oen.d00dle.actors
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorRef
import io.scalaland.chimney.dsl._
import oen.d00dle.shared.Dto
import com.softwaremill.quicklens._

object LobbyManager {
  sealed trait LobbyManagerMsg
  case class RegisterNewUser(ref: ActorRef[UserActor.UserActorMsg]) extends LobbyManagerMsg
  case class GetLobbyList(respondTo: ActorRef[UserActor.UserActorMsg]) extends LobbyManagerMsg
  case class CreateNewLobby(name: String) extends LobbyManagerMsg
  case class LobbyClosed(lobbyId: Int) extends LobbyManagerMsg

  case class UserTerminated(ref: ActorRef[UserActor.UserActorMsg]) extends LobbyManagerMsg
  case class FindLobby(respondTo: ActorRef[FindLobbyResponse], lobbyId: Int) extends LobbyManagerMsg

  sealed trait FindLobbyResponse
  case class LobbyFound(ref: ActorRef[LobbyActor.LobbyMsg], lobbyId: Int) extends FindLobbyResponse
  case class LobbyNotFound(lobbyId: Int) extends FindLobbyResponse

  case class GameStarted(lobbyId: Int) extends LobbyManagerMsg
  sealed trait LobbyMode
  case object GameMode extends LobbyMode
  case object PendingMode extends LobbyMode
  case class LobbyData(id: Int, name: String, ref: ActorRef[LobbyActor.LobbyMsg], mode: LobbyMode = PendingMode)

  def behavior(
    counter: Int,
    lobbies: IndexedSeq[LobbyData],
    users: Set[ActorRef[UserActor.UserActorMsg]]
  ): Behavior[LobbyManagerMsg] = Behaviors.receive { (ctx, msg) =>
    msg match{
      case RegisterNewUser(ref) =>
        ctx.log.debug("user registered: {}", ref)
        ctx.watchWith(ref, UserTerminated(ref))
        behavior(counter, lobbies, users + ref)

      case GetLobbyList(respondTo) =>
        val lobbiesToOut = lobbies.filter(_.mode == PendingMode).into[IndexedSeq[Dto.LobbyData]].transform
        respondTo ! UserActor.Forward(Dto.LobbyList(lobbiesToOut))
        Behavior.same

      case CreateNewLobby(name) =>
        val nextId = counter + 1
        val lobbyRef = ctx.spawn(LobbyActor(nextId, name, ctx.self), s"lobby-$nextId")
        ctx.watchWith(lobbyRef, LobbyClosed(nextId))
        val createdLobby = LobbyData(nextId, name, lobbyRef)
        ctx.log.info("new lobby: {}", createdLobby)
        val broadcastData = UserActor.Forward(Dto.LobbyAdded(createdLobby.into[Dto.LobbyData].transform))
        users.foreach(_ ! broadcastData)
        behavior(nextId, createdLobby +: lobbies, users)

      case LobbyClosed(lobbyId) =>
        val msg = Dto.LobbyClosed(lobbyId)
        users.foreach(_ ! UserActor.Forward(msg))
        ctx.log.info("lobby closed: {}", lobbyId)
        behavior(counter, lobbies.filter(_.id != lobbyId), users)

      case FindLobby(respondTo, lobbyId) =>
        lobbies.find(lobby => lobby.id == lobbyId && lobby.mode == PendingMode) match {
          case None => respondTo ! LobbyNotFound(lobbyId)
          case Some(lobbyData) => respondTo ! LobbyFound(lobbyData.ref, lobbyData.id)
        }
        Behavior.same

      case GameStarted(lobbyId) =>
        val findLobbyPredicate: LobbyData => Boolean = _.id == lobbyId
        val updlobbies = lobbies.modify(_.eachWhere(findLobbyPredicate).mode).setTo(GameMode)
        val msg = Dto.LobbyClosed(lobbyId)
        users.foreach(_ ! UserActor.Forward(msg))
        behavior(counter, updlobbies, users)

      case UserTerminated(ref) =>
        ctx.log.debug("user terminated: {}", ref)
        behavior(counter, lobbies, users - ref)
    }
  }

  def apply(): Behavior[LobbyManagerMsg] = Behaviors.setup { ctx =>
    val exampleRef = ctx.spawn(LobbyActor(0, "lobby-example", ctx.self), "lobby-example-0")
    ctx.watchWith(exampleRef, LobbyClosed(0))
    behavior(
      counter = 0,
      lobbies = IndexedSeq(LobbyData(0, "example lobby", exampleRef)),
      users = Set()
    )
  }
}
