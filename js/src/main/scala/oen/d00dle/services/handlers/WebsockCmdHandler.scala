package oen.d00dle.services.handlers

import oen.d00dle.services.AppData._
import diode.ModelRW
import diode.ActionHandler
import diode.ActionResult
import oen.d00dle.services.websockets.Websock
import oen.d00dle.shared.Dto
import scala.concurrent.ExecutionContext.Implicits.global

class WebsockCmdHandler[M](modelRW: ModelRW[M, WsConnection]) extends ActionHandler(modelRW) {

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case ChangeNicknameA(nickname) =>
      effectOnly(Websock.sendAsEffect(value.ws, Dto.ChangeNickname(nickname)))

    case CreateLobbyA(name) =>
      effectOnly(Websock.sendAsEffect(value.ws, Dto.CreateLobby(name)))
  }
}
