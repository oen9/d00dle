package oen.d00dle.services.handlers

import oen.d00dle.services.AppData._
import diode.ModelRW
import diode.ActionHandler
import diode.ActionResult
import cats.implicits._
import com.softwaremill.quicklens._
import oen.d00dle.shared.Dto.GameUser

class GameHandler[M](modelRW: ModelRW[M, Option[GameState]]) extends ActionHandler(modelRW) {

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case InitGameStateA(users) =>
      updated(GameState(users).some)

    case GameUserChangedA(gu) =>
      val userById: GameUser => Boolean = _.u.id == gu.u.id
      val newValue = value.modify(_.each.users.eachWhere(userById)).setTo(gu)
      updated(newValue)

    case NewChatMsgA(msg) =>
      val newValue = value.modify(_.each.msgs).using(_ :+ msg)
      updated(newValue)
  }
}
