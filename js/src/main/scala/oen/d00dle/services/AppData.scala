package oen.d00dle.services
import diode.Action
import org.scalajs.dom.raw.WebSocket
import oen.d00dle.shared.Dto
import oen.d00dle.shared.Dto.GameUser

object AppData {
  case class RootModel(wsConnection: WsConnection)
  case class WsConnection(ws: WebSocket, gameData: Option[GameData] = None)
  case class GameData(
    user: User,
    lobbies: IndexedSeq[Dto.LobbyData] = IndexedSeq(),
    lobby: Option[Either[String, FullLobby]] = None,
    game: Option[GameState] = None
  )
  case class User(id: Int, nickname: String)
  sealed trait LobbyMode
  case object GameMode extends LobbyMode
  case object PendingMode extends LobbyMode
  case class FullLobby(id: Int, name: String, users: Seq[Dto.LobbyUser], mode: LobbyMode = PendingMode)
  case class GameState(users: IndexedSeq[Dto.GameUser], msgs: IndexedSeq[Dto.ChatMsg] = IndexedSeq())

  case object WSConnect extends Action
  case class WSConnected(u: Dto.UserCreated) extends Action
  case object WSDisconnected extends Action

  case class ChangeNicknameA(nickname: String) extends Action
  case class NicknameChangedA(id: Int, nickname: String) extends Action

  case class CreateLobbyA(name: String) extends Action
  case class JoinLobbyA(id: Int) extends Action
  case object QuitLobbyA extends Action
  case object SetReadyA extends Action
  case object SetNotReadyA extends Action

  case class GotLobbiesA(lobbies: IndexedSeq[Dto.LobbyData]) extends Action
  case class LobbyAddedA(lobby: Dto.LobbyData) extends Action
  case class LobbyClosedA(id: Int) extends Action
  case class JoinedLobbyA(lobby: FullLobby) extends Action
  case class LobbyNotFoundA(id: Int) extends Action
  case class SomeoneJoinedLobbyA(lu: Dto.LobbyUser) extends Action
  case class SomeoneLeftLobbyA(id: Int) extends Action
  case class LobbyUserChangedA(lu: Dto.LobbyUser) extends Action

  case class GameStartedA(users: IndexedSeq[Dto.GameUser]) extends Action
  case class InitGameStateA(users: IndexedSeq[Dto.GameUser]) extends Action
  case class GameUserChangedA(gu: GameUser) extends Action
  case class SendNewChatMsgA(msg: String) extends Action
  case class NewChatMsgA(msg: Dto.ChatMsg) extends Action
}
