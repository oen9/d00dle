package oen.d00dle.services
import diode.Action
import org.scalajs.dom.raw.WebSocket
import oen.d00dle.shared.Dto

object AppData {
  case class RootModel(wsConnection: WsConnection)
  case class WsConnection(ws: WebSocket, gameData: Option[GameData] = None)
  case class GameData(user: User)
  case class User(id: Int, nickname: String)

  case object WSConnect extends Action
  case class WSConnected(u: Dto.UserCreated) extends Action
  case object WSDisconnected extends Action

  case class ChangeNicknameA(nickname: String) extends Action
  case class NicknameChangedA(id: Int, nickname: String) extends Action
}
