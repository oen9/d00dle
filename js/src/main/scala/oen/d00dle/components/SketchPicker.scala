package oen.d00dle.components

import com.payalabs.scalajs.react.bridge.{ReactBridgeComponent, WithPropsNoChildren}
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js
import japgolly.scalajs.react.Callback

object SketchPicker extends ReactBridgeComponent {

  @JSImport("react-color", "SketchPicker")
  // @JSImport("react-color", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  override lazy val componentValue = RawComponent

  def apply(
    color: js.UndefOr[String] = js.undefined,
    onChangeComplete: js.UndefOr[BlockPicker.ColorEvt => Callback] = js.undefined,
    disableAlpha: js.UndefOr[Boolean] = true
  ): WithPropsNoChildren = autoNoChildren
}
