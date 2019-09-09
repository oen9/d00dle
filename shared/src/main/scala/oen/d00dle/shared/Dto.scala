package oen.d00dle.shared

object Dto {
  sealed trait WsData

  case class Err(t: String) extends WsData
  case class LobbyData(id: Int, name: String)
  case class LobbyList(lobbies: IndexedSeq[LobbyData]) extends WsData

  sealed trait Event extends WsData
  case class UserCreated(id: Int, nickname: String) extends Event
  case class NicknameChanged(id: Int, nickname: String) extends Event
  case class LobbyAdded(lobby: LobbyData) extends Event

  sealed trait Cmd extends WsData
  case class Log(msg: String) extends Cmd
  case class ChangeNickname(nickname: String) extends Cmd
  case class CreateLobby(name: String) extends Cmd

  import io.circe.generic.extras.Configuration
  implicit val circeConfig = Configuration.default.withDiscriminator("type").withDefaults
}
