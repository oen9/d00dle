package oen.d00dle.modules

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import oen.d00dle.services.AppData.GameData
import oen.d00dle.services.AppData.ChangeNicknameA
import oen.d00dle.services.AppData.CreateLobbyA
import japgolly.scalajs.react.extra.router.RouterCtl
import oen.d00dle.D00dleJS.Loc
import oen.d00dle.D00dleJS.LobbyLoc

object Home {

  case class Props(router: RouterCtl[Loc], proxy: ModelProxy[GameData])
  case class State(nickname: String, newLobby: String = "")

  class Backend($: BackendScope[Props, State]) {
    def updateNickname(e: ReactEventFromInput): Callback = {
      val newValue = e.target.value
      $.modState(_.copy(nickname = newValue))
    }

    def updateNewLobby(e: ReactEventFromInput): Callback = {
      val newValue = e.target.value
      $.modState(_.copy(newLobby = newValue))
    }

    def acceptNickname(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      state <- $.state
      props <- $.props
      _ <- props.proxy.dispatchCB(ChangeNicknameA(state.nickname))
    } yield ()

    def acceptNewLobby(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      state <- $.state
      props <- $.props
      _ <- if (state.newLobby.nonEmpty) {
              props.proxy.dispatchCB(CreateLobbyA(state.newLobby)) >>
              $.modState(_.copy(newLobby = ""))
            }
            else Callback.empty
    } yield ()

    def render(props: Props, state: State) =
      <.div(^.cls := "container",
        <.div(^.cls := "card",
          <.div(^.cls := "card-header", "you"),
          <.div(^.cls := "card-body",
            <.form(
              <.div(^.cls := "form-group row mt-2",
                <.div(^.cls := "col text-right mt-2",
                  <.span(^.cls := "mr-1", "nickname"),
                  <.small(^.cls := "text-muted", s"${props.proxy().user.nickname} (${props.proxy().user.id})"),
                ),
                <.div(^.cls := "col",
                  <.input(^.cls := "form-control", ^.value := state.nickname, ^.onChange ==> updateNickname)
                ),
                <.div(^.cls := "col text-left", <.button(^.cls := "btn btn-primary", "ok", ^.onClick ==> acceptNickname)),
              )
            )
          )
        ),

        <.div(^.cls := "card mt-2 mb-2",
          <.div(^.cls := "card-header", "Lobby list"),
          <.div(^.cls := "card-body",
            <.form(
              <.div(^.cls := "form-group row",
                <.div(^.cls := "col text-right mt-2", "create and enter new lobby"),
                <.div(^.cls := "col",
                  <.input(^.cls := "form-control", ^.value := state.newLobby, ^.onChange ==> updateNewLobby)
                ),
                <.div(^.cls := "col text-left", <.button(^.cls := "btn btn-primary", "ok", ^.onClick ==> acceptNewLobby)),
              )
            ),
            <.div(^.cls := "list-group border mt-2",
              props.proxy().lobbies.map { lobby =>
                props.router.link(LobbyLoc(lobby.id))(^.cls := "list-group-item list-group-item-action", ^.key := lobby.id,
                  <.span(^.cls := "mr-1", lobby.name),
                  <.small(^.cls := "text-muted", s"(${lobby.id})"),
                  <.i(^.cls := "fas fa-sign-in-alt float-right")
                )
              }.toVdomArray,
            )
          )
        ),
      )
  }

  val component = ScalaComponent.builder[Props]("Home")
    .initialStateFromProps(props => State(nickname = props.proxy().user.nickname))
    .renderBackend[Backend]
    .build

  def apply(router: RouterCtl[Loc], proxy: ModelProxy[GameData]) = component(Props(router, proxy))
}
