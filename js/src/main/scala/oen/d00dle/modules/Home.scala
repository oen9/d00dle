package oen.d00dle.modules

import diode.react.ModelProxy
import oen.d00dle.components.BlueButton
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import oen.d00dle.services.AppData.GameData
import oen.d00dle.services.AppData.ChangeNicknameA

object Home {

  case class Props(proxy: ModelProxy[GameData])
  case class State(nickname: String)

  class Backend($: BackendScope[Props, State]) {
    def tick(): Callback = Callback(println("tick"))

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
            <.div(^.cls := "row",
              <.div(^.cls := "col text-right mt-2", "create and enter new lobby"),
              <.div(^.cls := "col", <.input(^.cls := "form-control")),
              <.div(^.cls := "col text-left", BlueButton(BlueButton.Props("ok", tick()))),
            ),
            <.div(^.cls := "list-group border mt-2",
              <.a(^.cls := "list-group-item list-group-item-action", "foo", <.i(^.cls := "fas fa-sign-in-alt float-right")),
              <.a(^.cls := "list-group-item list-group-item-action", "bar", <.i(^.cls := "fas fa-sign-in-alt float-right")),
              <.a(^.cls := "list-group-item list-group-item-action", "baz", <.i(^.cls := "fas fa-sign-in-alt float-right")),
            )
          )
        ),
      )
  }

  val component = ScalaComponent.builder[Props]("Home")
    .initialStateFromProps(props => State(nickname = props.proxy().user.nickname))
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[GameData]) = component(Props(proxy))
}
