package oen.d00dle.services.handlers

import oen.d00dle.services.AppData._
import diode.ModelRW
import diode.ActionHandler
import diode.ActionResult
import oen.d00dle.shared.Dto.LobbyData
import com.softwaremill.quicklens._

class LobbiesHandler[M](modelRW: ModelRW[M, Option[IndexedSeq[LobbyData]]]) extends ActionHandler(modelRW) {

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case GotLobbiesA(lobbies) =>
      updated(Some(lobbies))

    case LobbyAddedA(lobby) =>
      val newValue = value.modify(_.each).using(lobby +: _)
      updated(newValue)

    case LobbyClosedA(id) =>
      val newValue = value.modify(_.each).using(_.filter(_.id != id))
      updated(newValue)
  }
}
