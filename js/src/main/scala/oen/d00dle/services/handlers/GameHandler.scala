package oen.d00dle.services.handlers

import oen.d00dle.services.AppData._
import diode.ModelRW
import diode.ActionHandler
import diode.ActionResult
import cats.implicits._

class GameHandler[M](modelRW: ModelRW[M, Option[GameState]]) extends ActionHandler(modelRW) {

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case InitGameStateA(users) =>
      updated(GameState(users).some)
  }
}
