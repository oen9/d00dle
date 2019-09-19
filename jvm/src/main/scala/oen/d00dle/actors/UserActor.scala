package oen.d00dle.actors
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import oen.d00dle.shared.Dto._
import cats.implicits._

object UserActor {
  val DEFAULT_USER_NAME = "unknown"

  sealed trait UserActorMsg
  case class RegisterOut(respondTo: ActorRef[OutData]) extends UserActorMsg
  case class InData(wsData: WsData) extends UserActorMsg
  case class Forward(wsData: WsData) extends UserActorMsg

  case class FindLobbyResponseWrapper(response: LobbyManager.FindLobbyResponse) extends UserActorMsg
  case class InitGame(gameRef: ActorRef[GameActor.GameActorMsg], gameUsers: IndexedSeq[GameUser]) extends UserActorMsg

  sealed trait OutData
  case class ToOut(wsData: WsData) extends OutData
  case object Complete extends UserActorMsg with OutData
  case class Fail(ex: Throwable) extends UserActorMsg with OutData

  case class LobbyRef(ref: ActorRef[LobbyActor.LobbyMsg], lobbyId: Int)

  def initBehavior(id: Int, lobbyManager: ActorRef[LobbyManager.LobbyManagerMsg]): Behavior[UserActorMsg] = Behaviors.receive { (ctx, msg) =>
    msg match {
      case RegisterOut(respondTo) =>
        respondTo ! ToOut(UserCreated(id, DEFAULT_USER_NAME))
        lobbyManager ! LobbyManager.GetLobbyList(ctx.self)
        val findLobbyResponseMapper: ActorRef[LobbyManager.FindLobbyResponse] = ctx.messageAdapter(FindLobbyResponseWrapper)
        behavior(id, respondTo, DEFAULT_USER_NAME, lobbyManager, findLobbyResponseMapper, None, None)

      case unknown =>
        ctx.log.error("user: {} is waiting for outRef. Unhandled: {}", id, unknown)
        Behavior.same
    }
  }

  def behavior(
    id: Int,
    outRef: ActorRef[OutData],
    nickname: String,
    lobbyManager: ActorRef[LobbyManager.LobbyManagerMsg],
    findLobbyResponseMapper: ActorRef[LobbyManager.FindLobbyResponse],
    currentLobby: Option[LobbyRef],
    currentGame: Option[ActorRef[GameActor.GameActorMsg]]
  ): Behavior[UserActorMsg] = Behaviors.receive { (ctx, msg) =>
    msg match {
      case InData(log@Log(msg)) =>
        ctx.log.info("log msg: [{}] from user: {}", msg, id)
        outRef ! ToOut(log)
        Behavior.same

      case InData(ChangeNickname(newNickname)) =>
        ctx.log.debug("user: {} changed nickname from [{}] to [{}]", id, nickname, newNickname)
        currentLobby foreach(_.ref ! LobbyActor.NicknameChanged(id, newNickname))
        outRef ! ToOut(NicknameChanged(id, newNickname))
        behavior(id, outRef, newNickname, lobbyManager, findLobbyResponseMapper, currentLobby, currentGame)

      case InData(CreateLobby(name)) =>
        lobbyManager ! LobbyManager.CreateNewLobby(name)
        Behavior.same

      case InData(JoinLobby(lobbyId)) =>
        currentLobby match {
          case None =>
            lobbyManager ! LobbyManager.FindLobby(findLobbyResponseMapper, lobbyId)
          case Some(lobbyRef) if lobbyRef.lobbyId != lobbyId =>
            lobbyRef.ref ! LobbyActor.ExitLobby(id)
            lobbyManager ! LobbyManager.FindLobby(findLobbyResponseMapper, lobbyId)
          case Some(lobbyRef) =>
        }
        Behavior.same

      case InData(QuitLobby) =>
        currentLobby.foreach(_.ref ! LobbyActor.ExitLobby(id))
        behavior(id, outRef, nickname, lobbyManager, findLobbyResponseMapper, None, currentGame)

      case InData(SetReady) =>
        currentLobby.foreach(_.ref ! LobbyActor.SetReady(id))
        Behavior.same

      case InData(SetNotReady) =>
        currentLobby.foreach(_.ref ! LobbyActor.SetNotReady(id))
        Behavior.same

      case InitGame(gameRef, gameUsers) =>
        outRef ! ToOut(GameStarted(gameUsers))
        behavior(id, outRef, nickname, lobbyManager, findLobbyResponseMapper, currentLobby, gameRef.some)

      case Forward(msg) =>
        outRef ! ToOut(msg)
        Behavior.same

      case FindLobbyResponseWrapper(LobbyManager.LobbyFound(lobbyRef, lobbyId)) =>
        lobbyRef ! LobbyActor.JoinLobby(id, nickname, ctx.self)
        behavior(id, outRef, nickname, lobbyManager, findLobbyResponseMapper, LobbyRef(lobbyRef, lobbyId).some, currentGame)

      case FindLobbyResponseWrapper(LobbyManager.LobbyNotFound(lobbyId)) =>
        outRef ! ToOut(LobbyNotFound(lobbyId))
        Behavior.same

      case Complete =>
        ctx.log.info("user: {} quitted", id)
        Behavior.stopped
      case Fail(ex) =>
        ctx.log.error(ex, "fail for user: {}", id)
        Behavior.stopped
      case RegisterOut(respondTo) =>
        ctx.log.error("outRef already registered for user: {}", id)
        Behavior.same
      case InData(msg) =>
        ctx.log.info("unsupported WsData: {}", msg)
        Behavior.same
    }
  }

  def apply(id: Int, lobbyManager: ActorRef[LobbyManager.LobbyManagerMsg]): Behavior[UserActorMsg] = Behaviors.setup { ctx =>
    ctx.log.info("user: {} created", id)
    initBehavior(id, lobbyManager)
  }
}
