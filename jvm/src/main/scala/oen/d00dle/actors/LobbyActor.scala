package oen.d00dle.actors
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorRef
import oen.d00dle.shared.Dto
import com.softwaremill.quicklens._

object LobbyActor {
  sealed trait LobbyMsg
  case class JoinLobby(userId: Int, nickname: String, ref: ActorRef[UserActor.UserActorMsg]) extends LobbyMsg
  case class ExitLobby(userId: Int) extends LobbyMsg
  case class SetReady(userId: Int) extends LobbyMsg
  case class SetNotReady(userId: Int) extends LobbyMsg

  case class NicknameChanged(userId: Int, newNickname: String) extends LobbyMsg

  case object GameFinished extends LobbyMsg

  case class LobbyUser(ref: ActorRef[UserActor.UserActorMsg], dtoData: Dto.LobbyUser)

  def behavior(
    id: Int,
    name: String,
    lobbyManager: ActorRef[LobbyManager.LobbyManagerMsg],
    users: Map[Int, LobbyUser] = Map()
  ): Behavior[LobbyMsg] = Behaviors.receive { (ctx, msg) =>

    def withChangedUsers(updUsers: Map[Int, LobbyUser]): Behavior[LobbyMsg] = behavior(id, name, lobbyManager, updUsers)

    def startGame(updUsers: Map[Int, LobbyUser]): Behavior[LobbyMsg] = {
      lobbyManager ! LobbyManager.GameStarted(id)

      updUsers.values.map(_.ref).foreach(ctx.unwatch)
      val gameRef = ctx.spawn(GameActor(id, updUsers.values.toVector), s"game-$id")
      ctx.watchWith(gameRef, GameFinished)

      ctx.log.info("All users in lobby [{}] ready. Game started!", id)
      behavior(id, name, lobbyManager, updUsers)
      Behaviors.receiveMessage {
        case GameFinished =>
          ctx.log.info("Game finished. Closing lobby [{}]", id)
          Behaviors.stopped
        case ExitLobby(userId) =>
          gameRef ! GameActor.UserQuitted(userId)
          Behaviors.same
        case msg =>
          ctx.log.error("Lobby [{}] already started game. Message [{}] unhandled", id, msg)
          Behaviors.same
      }
    }

    msg match {
      case JoinLobby(userId, nickname, ref) =>
        val lobbyUser = LobbyUser(ref, Dto.LobbyUser(Dto.User(userId, nickname)))
        ctx.watchWith(ref, ExitLobby(userId))
        val newUsers = users + (userId -> lobbyUser)

        ref ! UserActor.Forward(Dto.JoinedLobby(id, name, newUsers.values.map(_.dtoData).toSeq))
        val msgToBroadcast = UserActor.Forward(Dto.SomeoneJoinedLobby(lobbyUser.dtoData))
        users.values.foreach(_.ref ! msgToBroadcast)
        withChangedUsers(newUsers)

      case ExitLobby(userId) =>
        users.get(userId).foreach(u => ctx.unwatch(u.ref))
        val updUsers = users - userId
        ctx.log.debug("Exit lobby: {} user: {}", id, userId)
        val msgToBroadcast = UserActor.Forward(Dto.SomeoneLeftLobby(userId))
        updUsers.values.foreach(_.ref ! msgToBroadcast)
        if (updUsers.isEmpty) Behavior.stopped
        else withChangedUsers(updUsers)

      case NicknameChanged(userId, newNickname) =>
        val updUsers = users.modify(_.at(userId).dtoData.u.name).setTo(newNickname)
        broadcastUserChanged(updUsers, userId)
        withChangedUsers(updUsers)

      case SetReady(userId) =>
        val updUsers = users.modify(_.at(userId).dtoData.readyState ).setTo(Dto.Ready)
        broadcastUserChanged(updUsers, userId)

        val allReady = updUsers.forall { case (_, lu) => lu.dtoData.readyState == Dto.Ready }
        if (allReady && updUsers.size >= 2) startGame(updUsers)
        else withChangedUsers(updUsers)

      case SetNotReady(userId) =>
        val updUsers = users.modify(_.at(userId).dtoData.readyState ).setTo(Dto.NotReady)
        broadcastUserChanged(updUsers, userId)
        withChangedUsers(updUsers)

      case _ => Behavior.same
    }
  }

  def broadcastUserChanged(users: Map[Int, LobbyUser], userId: Int): Unit = {
    users.get(userId).foreach { lobbyUser =>
      val msgToBroadcast = Dto.LobbyUserChanged(lobbyUser.dtoData)
      broadcastMessage(users, msgToBroadcast)
    }
  }

  def broadcastMessage(users: Map[Int, LobbyUser], dto: Dto.WsData): Unit = {
    val msg = UserActor.Forward(dto)
    users.values.map(_.ref).foreach(_ ! msg)
  }

  def apply(id: Int, name: String, lobbyManager: ActorRef[LobbyManager.LobbyManagerMsg]): Behavior[LobbyMsg] = behavior(id, name, lobbyManager)
}
