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

  case class LobbyUser(ref: ActorRef[UserActor.UserActorMsg], dtoData: Dto.LobbyUser)

  def behavior(
    id: Int,
    name: String,
    users: Map[Int, LobbyUser] = Map()
  ): Behavior[LobbyMsg] = Behaviors.receive { (ctx, msg) =>
    msg match{
      case JoinLobby(userId, nickname, ref) =>
        val lobbyUser = LobbyUser(ref, Dto.LobbyUser(Dto.User(userId, nickname)))
        ctx.watchWith(ref, ExitLobby(userId))
        val newUsers = users + (userId -> lobbyUser)

        ref ! UserActor.Forward(Dto.JoinedLobby(id, name, newUsers.values.map(_.dtoData).toSeq))
        val msgToBroadcast = UserActor.Forward(Dto.SomeoneJoinedLobby(lobbyUser.dtoData))
        users.values.foreach(_.ref ! msgToBroadcast)
        behavior(id, name, newUsers)

      case ExitLobby(userId) =>
        users.get(userId).foreach(u => ctx.unwatch(u.ref))
        val updUsers = users - userId
        ctx.log.debug("Exit lobby: {} user: {}", id, userId)
        val msgToBroadcast = UserActor.Forward(Dto.SomeoneLeftLobby(userId))
        updUsers.values.foreach(_.ref ! msgToBroadcast)
        if (updUsers.isEmpty) Behavior.stopped
        else behavior(id, name, updUsers)

      case NicknameChanged(userId, newNickname) =>
        val updUsers = users.modify(_.at(userId).dtoData.u.name).setTo(newNickname)
        broadcastUserChanged(updUsers, userId)
        behavior(id, name, updUsers)

      case SetReady(userId) =>
        val updUsers = users.modify(_.at(userId).dtoData.readyState ).setTo(Dto.Ready)
        broadcastUserChanged(updUsers, userId)
        behavior(id, name, updUsers)

      case SetNotReady(userId) =>
        val updUsers = users.modify(_.at(userId).dtoData.readyState ).setTo(Dto.NotReady)
        broadcastUserChanged(updUsers, userId)
        behavior(id, name, updUsers)


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

  def apply(id: Int, name: String): Behavior[LobbyMsg] = behavior(id, name)
}
