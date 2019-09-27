package oen.d00dle.services.handlers

import oen.d00dle.services.AppData._
import diode.ModelRW
import diode.ActionHandler
import diode.ActionResult
import cats.implicits._
import com.softwaremill.quicklens._
import oen.d00dle.shared.Dto.GameUser
import oen.d00dle.shared.Dto

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

    case NowDrawsA(userId) =>
      val newValue = for {
        state <- value
        user <- state.users.find(_.u.id == userId)
        msg = Dto.ChatMsg("system", -1, s"Now draws: ${user.u.name} (${user.u.id})", Dto.SystemMsg)
      } yield state
                .modify(_.msgs).using(_ :+ msg)
                .modify(_.secret).setTo(None)
                .modify(_.picture).setTo(None)
      updated(newValue)

    case YouDrawA(secret) =>
      val newValue = for {
        state <- value
        msg = Dto.ChatMsg("system", -1, "Your turn!", Dto.SystemMsg)
      } yield state
                .modify(_.msgs).using(_ :+ msg)
                .modify(_.secret).setTo(secret.some)
                .modify(_.picture).setTo(None)
      updated(newValue)

    case PictureChangedA(pic) =>
      val newValue = value.modify(_.each.picture).setTo(pic.some)
      updated(newValue)
  }
}
