package oen.d00dle.services.handlers
import oen.d00dle.services.AppData._
import diode.ModelRW
import diode.ActionHandler
import diode.ActionResult
import com.softwaremill.quicklens._
import diode.Effect
import diode.Action
import oen.d00dle.services.websockets.Websock
import cats.implicits._

class WebsockLifecycleHandler[M](modelRW: ModelRW[M, WsConnection], dispatch: Action => Unit) extends ActionHandler(modelRW) {

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case WSConnected(user) =>
      val gameData = GameData(user.id, user.nickname)
      val newValue = value.modify(_.gameData).setTo(gameData.some)
      updated(newValue)

    case WSDisconnected =>
      import scala.concurrent.ExecutionContext.Implicits.global
      import scala.concurrent.duration.DurationInt
      import diode.Implicits.runAfterImpl

      val newValue = value.modify(_.gameData).setTo(None)
      updated(newValue, Effect.action(WSConnect).after(5.second))

    case WSConnect =>
      val newWs = Websock.connect(dispatch)
      val newValue = WsConnection(newWs)
      updated(newValue)
  }
}
