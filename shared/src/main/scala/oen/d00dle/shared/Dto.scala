package oen.d00dle.shared

object Dto {
  case class User(id: Int, name: String)

  sealed trait ReadyState
  case object Ready extends ReadyState
  case object NotReady extends ReadyState
  case class LobbyUser(u: User, readyState: ReadyState = NotReady)

  sealed trait PresenceState
  case object Present extends PresenceState
  case object Quitted extends PresenceState
  case class GameUser(u: User, points: Int = 0, presenceState: PresenceState = Present)

  sealed trait WsData

  case class Err(t: String) extends WsData
  case class LobbyData(id: Int, name: String)
  case class LobbyList(lobbies: IndexedSeq[LobbyData]) extends WsData

  sealed trait Event extends WsData
  case class UserCreated(id: Int, nickname: String) extends Event
  case class NicknameChanged(id: Int, nickname: String) extends Event

  case class LobbyAdded(lobby: LobbyData) extends Event
  case class LobbyNotFound(id: Int) extends Event
  case class JoinedLobby(id: Int, name: String, users: Seq[LobbyUser]) extends Event
  case class LobbyClosed(id: Int) extends Event
  case class SomeoneJoinedLobby(lu: LobbyUser) extends Event
  case class SomeoneLeftLobby(id: Int) extends Event
  case class LobbyUserChanged(lu: LobbyUser) extends Event

  case class GameStarted(users: IndexedSeq[GameUser]) extends Event
  case class GameUserChanged(gu: GameUser) extends Event
  sealed trait ChatMsgType
  case object NotGuessed extends ChatMsgType
  case object Guessed extends ChatMsgType
  case object SystemMsg extends ChatMsgType
  case class ChatMsg(nickname: String, userId: Int, msg: String, chatMsgType: ChatMsgType)
  case class NewChatMsg(msg: ChatMsg) extends Event

  sealed trait Cmd extends WsData
  case class Log(msg: String) extends Cmd
  case class ChangeNickname(nickname: String) extends Cmd
  case class CreateLobby(name: String) extends Cmd
  case class JoinLobby(id: Int) extends Cmd
  case object QuitLobby extends Cmd
  case object SetReady extends Cmd
  case object SetNotReady extends Cmd

  case class SendNewChantMsg(msg: String) extends Cmd

  import io.circe.generic.extras.Configuration
  implicit val circeConfig = Configuration.default.withDiscriminator("type").withDefaults
}
