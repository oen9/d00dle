package oen.d00dle.modules

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import oen.d00dle.services.AppData.GameData
import oen.d00dle.services.AppData.ChangeNicknameA
import oen.d00dle.services.AppData.JoinLobbyA
import oen.d00dle.services.AppData.FullLobby
import oen.d00dle.shared.Dto.NotReady
import oen.d00dle.shared.Dto.Ready
import oen.d00dle.services.AppData.QuitLobbyA
import japgolly.scalajs.react.extra.router.RouterCtl
import oen.d00dle.D00dleJS.Loc
import oen.d00dle.D00dleJS.HomeLoc
import oen.d00dle.services.AppData.SetReadyA
import oen.d00dle.services.AppData.SetNotReadyA
import oen.d00dle.services.AppData.GameMode
import oen.d00dle.D00dleJS.GameLoc

object Lobby {

  case class Props(router: RouterCtl[Loc], proxy: ModelProxy[Option[GameData]], lobbyId: Int)
  case class State(nickname: String)

  class Backend($: BackendScope[Props, State]) {

    def updateNickname(e: ReactEventFromInput): Callback = {
      val newValue = e.target.value
      $.modState(_.copy(nickname = newValue))
    }

    def acceptNickname(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      state <- $.state
      props <- $.props
      _ <- props.proxy.dispatchCB(ChangeNicknameA(state.nickname))
    } yield ()

    def quitLobby(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      props <- $.props
      _ <- props.proxy.dispatchCB(QuitLobbyA)
    } yield ()

    def setReady(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      props <- $.props
      _ <- props.proxy.dispatchCB(SetReadyA)
    } yield ()

    def setNotReady(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      props <- $.props
      _ <- props.proxy.dispatchCB(SetNotReadyA)
    } yield ()

    def joinLobby = for {
      props <- $.props
      _ <- props.proxy.dispatchCB(JoinLobbyA(props.lobbyId))
    } yield ()

    def onMount = for {
      props <- $.props
      _ <- props.proxy()
                .flatMap(_.lobby.flatMap(_.toOption))
                .filter(_.id == props.lobbyId)
                .fold(joinLobby)(_ => tryJumpToGame)
    } yield ()

    def tryJumpToGame = for {
      props <- $.props
      mode = for {
          gameData <- props.proxy()
          lobby <- gameData.lobby
          fullLobby <- lobby.toOption
        } yield fullLobby.mode
      _ <- mode.filter(_ == GameMode).fold(Callback.empty)(_ => props.router.set(GameLoc))
    } yield ()

    def render(props: Props, state: State) =
      <.div(^.cls := "container",
        (for {
          gameData <- props.proxy()
          fullLobby <- gameData.lobby
        } yield (gameData, fullLobby)).fold(
          <.div(
            <.div(^.cls := "d-flex justify-content-center",
              <.div(^.cls := "spinner-border text-primary", ^.role := "status",
                <.span(^.cls := "sr-only", "Loading...")
              )
            ),
            <.div(^.cls := "d-flex justify-content-center", "joining lobby in progress ...")
          ): VdomElement
        ) { case (gameData, lobby) =>
          lobby match {
            case Left(msg) =>
            <.div(
              <.div(^.cls := "d-flex justify-content-center", <.div(msg)),
              <.div(^.cls := "d-flex justify-content-center",
                props.router.link(HomeLoc)(^.cls := "btn btn-primary", "jump to lobbies")
              )
            )
            case Right(fullLobby) => renderLobby(props, state, gameData, fullLobby)
          }
        }
      )

    def renderLobby(props: Props, state: State, gameData: GameData, fullLobby: FullLobby) =
      React.Fragment(
        <.div(^.cls := "card",
          <.div(^.cls := "card-header", "you"),
          <.div(^.cls := "card-body",
            <.form(
              <.div(^.cls := "form-group row mt-2",
                  <.div(^.cls := "col text-right mt-2",
                    <.span(^.cls := "mr-1", "nickname"),
                    <.small(^.cls := "text-muted",
                      s"${gameData.user.nickname} (${gameData.user.id})"
                    ),
                  ),
                  <.div(^.cls := "col",
                    <.input(^.cls := "form-control", ^.value := state.nickname, ^.onChange ==> updateNickname)
                  ),
                  <.div(^.cls := "col text-left", <.button(^.cls := "btn btn-primary", "ok", ^.onClick ==> acceptNickname)),
              )
            ),
            <.div(^.cls := "row mt-2",
              <.div(^.cls := "col text-center",
                <.div(^.cls := "btn-group btn-group-toggle", VdomAttr("data-toggle") := "buttons",
                  <.label(^.cls := "btn btn-secondary",
                    <.input(^.tpe := "radio", ^.name := "option1", ^.id := "option1", VdomAttr("defaultChecked") := false),
                    ^.onClick ==> setReady, "READY"
                  ),
                  <.label(^.cls := "btn btn-secondary active",
                    <.input(^.tpe := "radio", ^.name := "option2", ^.id := "option2", VdomAttr("defaultChecked") := true),
                    ^.onClick ==> setNotReady, "not yet"
                  )
                ),
                <.span(^.cls := "ml-2", <.button(^.cls := "btn btn-danger", "quit", ^.onClick ==> quitLobby)),
              ),
            ),
          ),
          <.div(^.cls := "card-footer text-center text-muted",
            "The game begins when all players are ready and there are at least two players. This lobby will disappear when all players quit.",
            <.br(),
            "(open in another tab to test)"
          )
        ),

        <.div(^.cls := "card mt-2 mb-2",
          <.div(^.cls := "card-header", s"in lobby: ${fullLobby.name}"),
          <.div(^.cls := "card-body",
            <.div(^.cls := "list-group border mt-2",
              fullLobby.users.map { user =>
                <.div(^.cls := "list-group-item list-group-item-action", ^.key := user.u.id,
                  <.div(^.cls := "row",
                    <.div(^.cls := "col text-right",
                      user.u.name,
                        <.small(^.cls := "ml-1 text-muted", s"(${user.u.id})")
                    ),
                    <.div(^.cls := "col text-left",
                      user.readyState match {
                        case NotReady => <.i(^.cls := "far fa-times-circle red")
                        case Ready => <.i(^.cls := "far fa-check-circle green")
                      }
                    )
                  )
                ),
              }.toVdomArray
            )
          )
        ),
      )
  }

  val component = ScalaComponent.builder[Props]("Lobby")
    .initialStateFromProps(props => State(nickname = props.proxy().map(_.user.nickname).fold("unknown")(identity)))
    .renderBackend[Backend]
    .componentDidMount(_.backend.onMount)
    .componentWillUpdate(_.backend.tryJumpToGame)
    .build

  def apply(router: RouterCtl[Loc], proxy: ModelProxy[Option[GameData]], lobbyId: Int) = component(Props(router, proxy, lobbyId))
}
