package oen.d00dle.modules

import diode.react.ModelProxy
import oen.d00dle.components.BlueButton
import oen.d00dle.services.IncreaseClicks
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import oen.d00dle.services.AppData.RootModel

object Home {

  case class Props(proxy: ModelProxy[RootModel])

  class Backend($: BackendScope[Props, Unit]) {
    def tick(): Callback = $.props.flatMap(_.proxy.dispatchCB(IncreaseClicks))

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
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[RootModel]) = component(Props(proxy))
}
