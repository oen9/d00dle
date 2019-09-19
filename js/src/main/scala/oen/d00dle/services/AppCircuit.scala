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
import oen.d00dle.services.handlers.LobbyHandler
import oen.d00dle.services.handlers.GameHandler

object AppCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  override protected def initialModel: RootModel = RootModel(
    wsConnection = WsConnection(Websock.connect(dispatch))
  )

  def updateUser(root: RootModel, user: Option[User]): RootModel =
    user.fold(root)(root.modify(_.wsConnection.gameData.each.user).setTo)

  def updateLobby(root: RootModel, fullLobby: Option[Either[String, FullLobby]]): RootModel =
    root.modify(_.wsConnection.gameData.each.lobby).setTo(fullLobby)

  def updateLobbies(root: RootModel, lobbies: Option[IndexedSeq[LobbyData]]): RootModel =
    lobbies.fold(root)(root.modify(_.wsConnection.gameData.each.lobbies).setTo)

  def updateGameState(root: RootModel, newGameState: Option[GameState]): RootModel =
    root.modify(_.wsConnection.gameData.each.game).setTo(newGameState)

  override protected def actionHandler: AppCircuit.HandlerFunction = composeHandlers(
    new WebsockLifecycleHandler(zoomTo(_.wsConnection), dispatch[Action]),
    new UserHandler(zoomMapRW(_.wsConnection.gameData)(_.user)(updateUser(_, _))),
    new LobbiesHandler(zoomMapRW(_.wsConnection.gameData)(_.lobbies)(updateLobbies(_, _))),
    new GameHandler(zoomFlatMapRW(_.wsConnection.gameData)(_.game)(updateGameState(_, _))),
    foldHandlers(
      new WebsockCmdHandler(zoomTo(_.wsConnection)),
      new LobbyHandler(zoomFlatMapRW(_.wsConnection.gameData)(_.lobby)(updateLobby(_, _))),
    )
  )
}
