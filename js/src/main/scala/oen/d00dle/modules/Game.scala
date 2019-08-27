package oen.d00dle.modules

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import oen.d00dle.services.RootModel
import oen.d00dle.services.TryGetRandom
import oen.d00dle.components.CanvasDraw
import oen.d00dle.components.BlockPicker
import oen.d00dle.components.SketchPicker
import japgolly.scalajs.react.vdom.HtmlStyles.color
import scala.scalajs.js

object Game {

  case class Props(proxy: ModelProxy[RootModel])
  case class State(color: String = "#03a9f4", brushRadius: Int = 1, lazyRadius: Int = 0)

  class Backend($: BackendScope[Props, State]) {
    def getRandom(): Callback = $.props.flatMap(_.proxy.dispatchCB(TryGetRandom()))

    def changeColor(newColor: BlockPicker.ColorEvt) = $.modState(_.copy(color = newColor.hex))

    def clear() = Callback(getCanvasOps.clear())
    def undo() = Callback(getCanvasOps.undo())

    def updateSize(e: ReactEventFromInput): Callback = for {
      _ <- e.preventDefaultCB
      newValue = e.target.value.toInt
      _ <- $.modState(_.copy(brushRadius = newValue))
    } yield ()

    def updateLazy(e: ReactEventFromInput): Callback = for {
      _ <- e.preventDefaultCB
      newValue = e.target.value.toInt
      _ <- $.modState(_.copy(lazyRadius = newValue))
    } yield ()

    private[this] val ref = Ref.toJsComponent(CanvasDraw.component)
    private[this] def getCanvasOps: CanvasDraw.CanvasDrawOps = ref.raw.current.asInstanceOf[CanvasDraw.CanvasDrawOps]

    def render(props: Props, state: State) =
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
                  color = state.color,
                  onChangeComplete = changeColor _
                )
              ),
              <.div(^.cls :="modal-footer",
                <.button(^.tpe := "button", ^.cls :="btn btn-primary", VdomAttr("data-dismiss") := "modal", "Close"),
              )
            )
          )
        ),







        <.div(^.cls := "row",
          <.div(^.cls := "col col-md-2",
            <.div(^.cls := "row",
              <.div(^.cls := "col d-flex justify-content-center",
                BlockPicker(triangle = "hide",
                  color = state.color,
                  onChangeComplete = changeColor _,
                  colors = js.Array("#f44336", "#e91e63", "#9c27b0", "#673ab7", "#3f51b5",
                    "#2196f3", "#03a9f4", "#00bcd4", "#009688", "#4caf50",
                    "#8bc34a", "#cddc39", "#ffeb3b", "#ffc107", "#ff9800",
                    "#ff5722", "#795548", "#607d8b", "#D9E3F0", "#FDA1FF")
                )
              )
            ),
            <.div(^.cls := "row pt-2",
              <.div(^.cls := "col text-center",
                <.button(^.tpe := "button", ^.cls := "btn btn-primary w-100",
                  VdomAttr("data-toggle") :="modal", VdomAttr("data-target") := "#customColorModal",
                  "custom color", <.i(^.cls := "fas fa-palette pl-2")
                ),
              )
            ),
            <.div(^.cls := "row mt-2",
              <.div(^.cls := "col text-center", <.div(^.cls := "btn btn-secondary w-100", "undo", ^.onClick --> undo())),
            ),
            <.div(^.cls := "row mt-2",
              <.div(^.cls := "col text-center", <.div(^.cls := "btn btn-danger w-100", "clear", ^.onClick --> clear()))
            ),
            <.div(^.cls := "row mt-2",
              <.div(^.cls := "col text-center mt-2", "size:"),
              <.div(^.cls := "col text-center",
                <.input(^.tpe := "range", ^.cls := "form-control",
                  ^.min := 1,
                  ^.max := 50,
                  ^.step := 1,
                  ^.onChange ==> updateSize,
                  ^.value := state.brushRadius
                )
              )
            ),
            <.div(^.cls := "row mt-2",
              <.div(^.cls := "col text-center mt-2", "lazy radius:"),
              <.div(^.cls := "col text-center",
                <.input(^.tpe := "range", ^.cls := "form-control",
                  ^.min := 0,
                  ^.max := 50,
                  ^.step := 1,
                  ^.onChange ==> updateLazy,
                  ^.value := state.lazyRadius
                )
              )
            ),
          ),
          <.div(^.cls := "col col-md-8",
            <.div(^.cls := "row p-2",
              <.div(^.cls := "alert alert-warning w-100 text-center", "YOUR TURN! Try to draw: ", <.b("car")),
              <.div(^.cls := "alert alert-info w-100 text-center", "Guess what's that!")
            ),
            <.div(^.cls := "row p-2",
              <.div(^.cls := "col overflow-auto",
                <.div(^.cls := "game-size p-4 mx-auto",
                  CanvasDraw.component.withRef(ref)(CanvasDraw.props(
                    canvasWidth = 800,
                    brushColor = state.color,
                    canvasHeight = 600,
                    brushRadius = state.brushRadius,
                    lazyRadius = state.lazyRadius
                  ))()
                )
              ),
            )
          ),
          <.div(^.cls := "col col-md-2",
            <.div(^.cls := "row",
              <.div(^.cls := "col",
                <.div(^.cls := "row",
                  <.div(^.cls := "ranking overflow-auto w-100",
                    <.table(^.cls := "table table-striped",
                      <.thead(
                        <.tr(
                          <.th("#"),
                          <.th("nickname"),
                          <.th("score")
                        ),
                      ),
                      <.tbody(
                        <.tr(
                          <.th(^.scope := "row", "1"),
                          <.td("foo"),
                          <.td("10")
                        ),
                        <.tr(
                          <.th(^.scope := "row", "2"),
                          <.td("bar"),
                          <.td("5"),
                        ),
                        <.tr(
                          <.th(^.scope := "row", "3"),
                          <.td("baz"),
                          <.td("0"),
                        ),
                      )
                    ),
                  )
                ),

                <.div(^.cls := "row pt-2",
                  <.div(^.cls := "chat overflow-auto w-100",
                      <.div("foo: cat"),
                      <.div("bar: dog"),
                      <.div("foo: cat"),
                      <.div("bar: dog"),
                      <.div("foo: cat"),
                      <.div("bar: dog"),
                      <.div("foo: cat"),
                      <.div("bar: dog"),
                      <.div("foo: cat"),
                      <.div("bar: dog"),
                      <.div("foo: cat"),
                      <.div("bar: dog"),
                      <.div("foo: cat"),
                      <.div("bar: dog"),
                    )
                ),
                <.div(^.cls := "row pt-2",
                  <.input(^.cls := "form-control w-100", ^.tpe := "text")
                ),
                <.div(^.cls := "row pt-2",
                  <.button(^.cls := "btn btn-primary w-100", "send")
                )

              )
            )
          )
        )
      )
  }

  val component = ScalaComponent.builder[Props]("Home")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[RootModel]) = component(Props(proxy))
}
