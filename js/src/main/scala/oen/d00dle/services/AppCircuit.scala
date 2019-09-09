package oen.d00dle.services

import diode.{Action, Circuit}
import diode.react.ReactConnector
import oen.d00dle.services.AppData._
import oen.d00dle.services.websockets.Websock
import oen.d00dle.services.handlers.WebsockLifecycleHandler
import oen.d00dle.services.handlers.WebsockCmdHandler
import oen.d00dle.services.handlers.UserHandler
import com.softwaremill.quicklens._
import oen.d00dle.shared.Dto.LobbyData
import oen.d00dle.services.handlers.LobbiesHandler

object AppCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  override protected def initialModel: RootModel = RootModel(
    wsConnection = WsConnection(Websock.connect(dispatch))
  )

  def updateUser(root: RootModel, user: Option[User]): RootModel =
    user.fold(root)(root.modify(_.wsConnection.gameData.each.user).setTo)

  def updateLobbies(root: RootModel, user: Option[IndexedSeq[LobbyData]]): RootModel =
    user.fold(root)(root.modify(_.wsConnection.gameData.each.lobbies).setTo)

  override protected def actionHandler: AppCircuit.HandlerFunction = composeHandlers(
    new WebsockLifecycleHandler(zoomTo(_.wsConnection), dispatch[Action]),
    new WebsockCmdHandler(zoomTo(_.wsConnection)),
    new UserHandler(zoomMapRW(_.wsConnection.gameData)(_.user)(updateUser(_, _))),
    new LobbiesHandler(zoomMapRW(_.wsConnection.gameData)(_.lobbies)(updateLobbies(_, _)))
  )
}
