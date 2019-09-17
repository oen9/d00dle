package oen.d00dle.services.handlers

import oen.d00dle.services.AppData._
import diode.ModelRW
import diode.ActionHandler
import diode.ActionResult
import com.softwaremill.quicklens._

class UserHandler[M](modelRW: ModelRW[M, Option[User]]) extends ActionHandler(modelRW) {

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case NicknameChangedA(id, nickname) =>
      val predicate: User => Boolean = _.id == id
      val newValue = value.modify(_.eachWhere(predicate).nickname).setTo(nickname)
      updated(newValue)
  }
}
