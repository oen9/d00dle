package oen.d00dle.services.handlers

import oen.d00dle.services.AppData._
import diode.ModelRW
import diode.ActionHandler
import diode.ActionResult
import com.softwaremill.quicklens._
import cats.implicits._
import oen.d00dle.shared.Dto
import diode.Effect

class LobbyHandler[M](modelRW: ModelRW[M, Option[Either[String, FullLobby]]]) extends ActionHandler(modelRW) {

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case JoinedLobbyA(lobby) =>
      updated(lobby.asRight.some)

    case LobbyNotFoundA(id) =>
      updated(s"Lobby with id [$id] not found.".asLeft[FullLobby].some)

    case SomeoneJoinedLobbyA(lu) =>
      val newValue = value.modify(_.each.eachRight.users).using(_ :+ lu)
      updated(newValue)

    case SomeoneLeftLobbyA(id) =>
      val newValue = value.modify(_.each.eachRight.users).using(_.filter(_.u.id != id))
      updated(newValue)

    case LobbyUserChangedA(lu) =>
      val userToUpdate: Dto.LobbyUser => Boolean = _.u.id == lu.u.id
      val newValue = value.modify(_.each.eachRight.users.eachWhere(userToUpdate)).setTo(lu)
      updated(newValue)

    case QuitLobbyA =>
      updated("You've left this lobby".asLeft[FullLobby].some)

    case GameStartedA(users) =>
      val newValue = value.modify(_.each.eachRight.mode).setTo(GameMode)
      import scala.concurrent.ExecutionContext.Implicits.global
      updated(newValue, Effect.action(InitGameStateA(users)))
  }
}
