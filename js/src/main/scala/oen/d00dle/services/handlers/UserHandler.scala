package oen.d00dle.services.handlers

import oen.d00dle.services.AppData._
import diode.ModelRW
import diode.ActionHandler
import diode.ActionResult
import com.softwaremill.quicklens._

class UserHandler[M](modelRW: ModelRW[M, Option[User]]) extends ActionHandler(modelRW) {

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case NicknameChangedA(id, nickname) =>
      val newValue = value .modify(_.each).using { user => // due to eachWhere bug
        if (id == user.id) user.modify(_.nickname).setTo(nickname)
        else user
      }
      updated(newValue)
  }
}
