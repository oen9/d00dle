package oen.d00dle.services.websockets
import oen.d00dle.shared.Dto.WsData
import oen.d00dle.shared.Dto.UserCreated
import oen.d00dle.services.AppData.WSConnected
import diode.Action
import oen.d00dle.shared.Dto.NicknameChanged
import io.scalaland.chimney.dsl._
import oen.d00dle.services.AppData.NicknameChangedA

object WebsockMessageHandler {
  def handle(msg: WsData, dispatch: Action => Unit): Unit = msg match {
    case uc: UserCreated => dispatch(WSConnected(uc))
    case nc: NicknameChanged => dispatch(nc.into[NicknameChangedA].transform)
    case unknown => println(s"[ws] unsupported data: $unknown")
  }
}
