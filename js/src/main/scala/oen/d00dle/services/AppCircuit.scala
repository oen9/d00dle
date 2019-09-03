package oen.d00dle.services

import diode.{Action, Circuit}
import diode.react.ReactConnector
import oen.d00dle.services.AppData._
import oen.d00dle.services.websockets.Websock
import oen.d00dle.services.handlers.WebsockLifecycleHandler

object AppCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  override protected def initialModel: RootModel = RootModel(
    wsConnection = WsConnection(Websock.connect(dispatch))
  )

  override protected def actionHandler: AppCircuit.HandlerFunction = composeHandlers(
    new WebsockLifecycleHandler(zoomTo(_.wsConnection), dispatch[Action])
  )
}
