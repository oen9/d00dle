package oen.d00dle.services

import diode.{Action, ActionHandler, Circuit, ModelRW}
import diode.react.ReactConnector
import oen.d00dle.services.AppData._
import oen.d00dle.services.handlers.WebsockLifecycleHandler

case class Clicks(count: Int)

case object IncreaseClicks extends Action

class ClicksHandler[M](modelRW: ModelRW[M, Clicks]) extends ActionHandler(modelRW) {
  override def handle = {
    case IncreaseClicks => updated(value.copy(count = value.count + 1))
  }
}

object AppCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  override protected def initialModel: RootModel = RootModel(
    clicks = Clicks(0),
    wsConnection = WsConnection(Websock.connect(dispatch))
  )

  override protected def actionHandler: AppCircuit.HandlerFunction = composeHandlers(
    new ClicksHandler(zoomTo(_.clicks)),
    new WebsockLifecycleHandler(zoomTo(_.wsConnection), dispatch[Action])
  )
}
