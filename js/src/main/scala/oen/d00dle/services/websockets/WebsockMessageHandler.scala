package oen.d00dle.services.websockets
import oen.d00dle.shared.Dto.WsData
import oen.d00dle.shared.Dto.UserCreated
import oen.d00dle.services.AppData.WSConnected
import diode.Action

object WebsockMessageHandler {
  def handle(msg: WsData, dispatch: Action => Unit): Unit = msg match {
    case uc: UserCreated => dispatch(WSConnected(uc))
    case unknown => println(s"[ws] unsupported data: $unknown")
  }
}
