package oen.d00dle.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import oen.d00dle.components.bridge.SketchPicker
import oen.d00dle.components.bridge.BlockPicker

object CustomColorButton {

  case class Props(color: String, onChange: BlockPicker.ColorEvt => Callback)

  class Backend($: BackendScope[Props, Unit]) {

    def render(props: Props) =
      React.Fragment(
        <.div(^.cls :="modal", ^.id := "customColorModal", ^.tabIndex := -1, ^.role := "dialog",
          <.div(^.cls :="modal-dialog modal-dialog-centered", ^.role := "document",
            <.div(^.cls := "modal-content",
              <.div(^.cls := "modal-header",
                <.h5(^.cls := "modal-title", "Pick custom color"),
                <.button(^.tpe := "button", ^.cls := "close", VdomAttr("data-dismiss") := "modal", ^.aria.label := "Close",
                  <.span(^.aria.hidden := "true", "×"),
                ),
              ),
              <.div(^.cls :="modal-body d-flex justify-content-center",
                SketchPicker(
                  color = props.color,
                  onChangeComplete = props.onChange
                )
              ),
              <.div(^.cls :="modal-footer",
                <.button(^.tpe := "button", ^.cls :="btn btn-primary", VdomAttr("data-dismiss") := "modal", "Close"),
              )
            )
          )
        ),
        <.button(^.tpe := "button", ^.cls := "btn btn-primary w-100",
          VdomAttr("data-toggle") :="modal", VdomAttr("data-target") := "#customColorModal",
          "custom color", <.i(^.cls := "fas fa-palette pl-2")
        ),
      )
  }

  val component = ScalaComponent.builder[Props]("Chat")
    .renderBackend[Backend]
    .build

  def apply(color: String, onChange: BlockPicker.ColorEvt => Callback) = component(Props(color, onChange))
}
