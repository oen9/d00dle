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
    case Dto.LobbyClosed(lobbyId)  => dispatch(LobbyClosedA(lobbyId))
    case Dto.JoinedLobby(id, name, users) => dispatch(JoinedLobbyA(FullLobby(id, name, users)))
    case Dto.LobbyNotFound(id) => dispatch(LobbyNotFoundA(id))
    case Dto.SomeoneJoinedLobby(lu) => dispatch(SomeoneJoinedLobbyA(lu))
    case Dto.SomeoneLeftLobby(id) => dispatch(SomeoneLeftLobbyA(id))
    case Dto.LobbyUserChanged(lu) => dispatch(LobbyUserChangedA(lu))
    case Dto.GameStarted(gu) => dispatch(GameStartedA(gu))
    case Dto.GameUserChanged(gu) => dispatch(GameUserChangedA(gu))
    case Dto.NewChatMsg(msg) => dispatch(NewChatMsgA(msg))

    case unknown => println(s"[ws] unsupported data: $unknown")
  }
}
