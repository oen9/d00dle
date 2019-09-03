package oen.d00dle.modules

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import oen.d00dle.services.AppData.GameData
import oen.d00dle.services.AppData.ChangeNicknameA

object Lobby {

  case class Props(proxy: ModelProxy[GameData])
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
            ),
            <.div(^.cls := "row mt-2",
              <.div(^.cls := "col text-center",
                <.div(^.cls := "btn-group btn-group-toggle", VdomAttr("data-toggle") := "buttons",
                  <.label(^.cls := "btn btn-secondary",
                    <.input(^.tpe := "radio", ^.name := "option1", ^.id := "option1", ^.checked := false),
                    "READY"
                  ),
                  <.label(^.cls := "btn btn-secondary active",
                    <.input(^.tpe := "radio", ^.name := "option2", ^.id := "option2", ^.checked := false),
                    "not yet"
                  )
                ),
                <.span(^.cls := "ml-2", <.button(^.cls := "btn btn-danger", "quit", ^.onClick ==> acceptNickname)),
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
          <.div(^.cls := "card-header", "in lobby: foo"),
          <.div(^.cls := "card-body",
            <.div(^.cls := "list-group border mt-2",
              <.div(^.cls := "list-group-item list-group-item-action",
                <.div(^.cls := "row",
                  <.div(^.cls := "col text-right", "foo"),
                  <.div(^.cls := "col text-left", <.i(^.cls := "far fa-check-circle green"))
                )
              ),
              <.div(^.cls := "list-group-item list-group-item-action",
                <.div(^.cls := "row",
                  <.div(^.cls := "col text-right", "bar bar bar"),
                  <.div(^.cls := "col text-left", <.i(^.cls := "far fa-times-circle red"))
                )
              ),
              <.div(^.cls := "list-group-item list-group-item-action",
                <.div(^.cls := "row",
                  <.div(^.cls := "col text-right", "baz"),
                  <.div(^.cls := "col text-left", <.i(^.cls := "far fa-check-circle green"))
                )
              )
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
