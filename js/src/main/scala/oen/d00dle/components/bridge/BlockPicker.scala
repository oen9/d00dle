package oen.d00dle.components.bridge

import com.payalabs.scalajs.react.bridge.{ReactBridgeComponent, WithPropsNoChildren}
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js
import japgolly.scalajs.react.Callback

object BlockPicker extends ReactBridgeComponent {

  @JSImport("react-color", "BlockPicker")
  @js.native
  object RawComponent extends js.Object

  override lazy val componentValue = RawComponent

  @js.native
  trait ColorEvt extends js.Object {
    def hex: String
  }

  def apply(
    triangle: js.UndefOr[String] = js.undefined,
    color: js.UndefOr[String] = js.undefined,
    colors: js.UndefOr[js.Array[String]] = js.undefined,
    onChangeComplete: js.UndefOr[ColorEvt => Callback] = js.undefined
  ): WithPropsNoChildren = autoNoChildren
}
