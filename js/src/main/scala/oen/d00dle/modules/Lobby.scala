package oen.d00dle.modules

import diode.react.ModelProxy
import oen.d00dle.components.BlueButton
import oen.d00dle.services.IncreaseClicks
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import oen.d00dle.services.RootModel
import oen.d00dle.services.TryGetRandom

object Lobby {

  case class Props(proxy: ModelProxy[RootModel])

  class Backend($: BackendScope[Props, Unit]) {
    def tick(): Callback = $.props.flatMap(_.proxy.dispatchCB(IncreaseClicks))
    def getRandom(): Callback = $.props.flatMap(_.proxy.dispatchCB(TryGetRandom()))

    def render(props: Props) =
      <.div(^.cls := "container",
        <.div(^.cls := "card",
          <.div(^.cls := "card-header", "you"),
          <.div(^.cls := "card-body",
            <.div(^.cls := "row mt-2",
              <.div(^.cls := "col text-right mt-2", "nickname"),
              <.div(^.cls := "col", <.input(^.cls := "form-control")),
              <.div(^.cls := "col text-left", BlueButton(BlueButton.Props("ok", tick()))),
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
                )
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
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[RootModel]) = component(Props(proxy))
}
