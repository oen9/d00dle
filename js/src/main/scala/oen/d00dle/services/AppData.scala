package oen.d00dle.services
import diode.Action
import org.scalajs.dom.raw.WebSocket

object AppData {
  case class RootModel(clicks: Clicks, wsConnection: WsConnection)
  case class WsConnection(ws: WebSocket, gameData: Option[GameData] = None)
  case class GameData(nick: String)

  case object WSConnect extends Action
  case class WSConnected(s: String) extends Action
  case object WSDisconnected extends Action
}
