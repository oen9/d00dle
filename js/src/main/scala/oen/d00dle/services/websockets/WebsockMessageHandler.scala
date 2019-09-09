package oen.d00dle.services.websockets

import oen.d00dle.shared.Dto
import oen.d00dle.services.AppData._
import diode.Action
import io.scalaland.chimney.dsl._

object WebsockMessageHandler {
  def handle(msg: Dto.WsData, dispatch: Action => Unit): Unit = msg match {
    case uc: Dto.UserCreated => dispatch(WSConnected(uc))
    case nc: Dto.NicknameChanged => dispatch(nc.into[NicknameChangedA].transform)
    case Dto.LobbyList(lobbies) => dispatch(GotLobbiesA(lobbies))
    case Dto.LobbyAdded(lobby)  => dispatch(LobbyAddedA(lobby))
    case unknown => println(s"[ws] unsupported data: $unknown")
  }
}
