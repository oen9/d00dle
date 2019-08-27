package oen.d00dle.components

import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js

import japgolly.scalajs.react._
import scalajs.js

object CanvasDraw {
  @JSImport("react-canvas-draw", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  @js.native
  trait Props extends js.Object {
    val canvasWidth: js.native
    val canvasHeight: js.native
  }

  @js.native
  trait CanvasDrawOps extends js.Object {
    def clear(): Unit
    def getSaveData(): String
    def undo(): Unit
  }

  def props(
    canvasWidth: js.UndefOr[Int] = js.undefined,
    canvasHeight: js.UndefOr[Int] = js.undefined,
    brushRadius: js.UndefOr[Int] = js.undefined,
    backgroundColor: js.UndefOr[String] = js.undefined,
    lazyRadius: js.UndefOr[Int] = js.undefined,
    brushColor: js.UndefOr[String] = js.undefined
  ): Props = {
    js.Dynamic.literal(
      canvasWidth = canvasWidth,
      canvasHeight = canvasHeight,
      brushColor = brushColor,
      brushRadius = brushRadius,
      backgroundColor = backgroundColor,
      lazyRadius = lazyRadius
    ).asInstanceOf[Props]
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)
}
