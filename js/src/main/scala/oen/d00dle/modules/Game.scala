package oen.d00dle.modules

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import oen.d00dle.components.bridge.CanvasDraw
import oen.d00dle.components.bridge.BlockPicker
import japgolly.scalajs.react.vdom.HtmlStyles.color
import scala.scalajs.js
import oen.d00dle.services.AppData.GameData
import oen.d00dle.services.AppData.GameState
import japgolly.scalajs.react.extra.router.RouterCtl
import oen.d00dle.D00dleJS.Loc
import oen.d00dle.D00dleJS.HomeLoc
import oen.d00dle.components.ScoreList
import oen.d00dle.components.Chat
import oen.d00dle.components.CustomColorButton
import oen.d00dle.services.AppData.SendNewChatMsgA
import oen.d00dle.services.AppData.User
import scala.scalajs.js
import cats.implicits._
import oen.d00dle.services.AppData.ChangePictureA

object Game {

  case class Props(router: RouterCtl[Loc], proxy: ModelProxy[Option[GameData]])
  case class State(
    color: String = "#03a9f4",
    brushRadius: Int = 1,
    lazyRadius: Int = 0,
    chatMsg: String = "",
    interval: Option[js.timers.SetIntervalHandle] = None,
    picture: String = ""
  )

  class Backend($: BackendScope[Props, State]) {
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

    def updateChatMsg(e: ReactEventFromInput): Callback = {
      val newValue = e.target.value
      $.modState(_.copy(chatMsg = newValue))
    }

    def acceptChatMsg(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      state <- $.state
      props <- $.props
      _ <- if (state.chatMsg.nonEmpty)
            props.proxy.dispatchCB(SendNewChatMsgA(state.chatMsg)) >>
            $.modState(_.copy(chatMsg = ""))
          else Callback.empty
    } yield ()

    def tick() = for {
      state <- $.state
      props <- $.props
      newPicture = getCanvasOps.getSaveData()
      _ <- if (state.picture != newPicture)
              props.proxy.dispatchCB(ChangePictureA(newPicture)) >>
              $.modState(_.copy(picture = newPicture))
            else Callback.empty
    } yield ()

    def startOrStopInterval() = for {
      state <- $.state
      props <- $.props
      secret = props.proxy().flatMap(_.game).flatMap(_.secret)
      interval = state.interval
      _ <- (secret, interval) match {
        case (Some(_), None) => initInterval
        case (None, Some(_)) => clearInterval
        case _ => Callback.empty
      }
    } yield ()

    def initInterval = $.modState { state =>
      val interval = js.timers.setInterval(2000)(tick.runNow())
      state.copy(interval = interval.some)
    }

    def clearInterval(): Callback = $.modState { state =>
      state.interval.foreach(js.timers.clearInterval)
      state.copy(interval = None)
    }

    def willUnmount(): Callback = for {
      state <- $.state
      _ <- Callback(state.interval.foreach(js.timers.clearInterval))
    } yield ()

    private[this] val ref = Ref.toJsComponent(CanvasDraw.component)
    private[this] def getCanvasOps: CanvasDraw.CanvasDrawOps = ref.raw.current.asInstanceOf[CanvasDraw.CanvasDrawOps]

    def render(props: Props, state: State) = (for {
      gameData <- props.proxy()
      gameState <- gameData.game
      user = gameData.user
    } yield fullRender(gameState, user, state)).getOrElse {
      <.div(
        <.div(^.cls := "d-flex justify-content-center", <.div("You aren't connected to any game. Find a new one!")),
        <.div(^.cls := "d-flex justify-content-center",
          props.router.link(HomeLoc)(^.cls := "btn btn-primary", "jump to lobbies")
        )
      )
    }

    def fullRender(gameState: GameState, me: User, state: State) =
      React.Fragment(
        <.div(^.cls := "row",
          <.div(^.cls := "col col-md-2",
            <.div(^.cls := "row",
              <.div(^.cls := "col d-flex justify-content-center",
                BlockPicker(
                  triangle = "hide",
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
                CustomColorButton(state.color, changeColor)
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
              gameState.secret.fold(<.div(^.cls := "alert alert-info w-100 text-center", "Guess what's that!")) { secret =>
                <.div(^.cls := "alert alert-warning w-100 text-center", "YOUR TURN! Try to draw: ", <.b(secret)),
              }
            ),
            <.div(^.cls := "row p-2",
              <.div(^.cls := "col overflow-auto",
                <.div(^.cls := "game-size p-4 mx-auto",
                  CanvasDraw.component.withRef(ref)(CanvasDraw.props(
                    canvasWidth = 800,
                    brushColor = state.color,
                    canvasHeight = 600,
                    brushRadius = state.brushRadius,
                    lazyRadius = state.lazyRadius,
                    disabled = gameState.secret.fold(true)(_ => false),
                    saveData = gameState.picture.fold(CanvasDraw.emptyValue)(identity),
                  ))()
                )
              ),
            )
          ),
          <.div(^.cls := "col col-md-2",
            <.div(^.cls := "row",
              <.div(^.cls := "col",
                ScoreList(gameState.users),
                Chat(me, gameState.msgs),
                <.form(
                  <.div(^.cls := "row pt-2",
                    <.input(^.cls := "form-control w-100", ^.tpe := "text", ^.value := state.chatMsg, ^.onChange ==> updateChatMsg)
                  ),
                  <.div(^.cls := "row pt-2",
                    <.button(^.cls := "btn btn-primary w-100", "guess", ^.onClick ==> acceptChatMsg)
                  )
                )
              )
            )
          )
        )
      )
  }

  val component = ScalaComponent.builder[Props]("Game")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount(_.backend.startOrStopInterval())
    .componentDidUpdate(_.backend.startOrStopInterval())
    .componentWillUnmount(_.backend.willUnmount)
    .build

  def apply(router: RouterCtl[Loc], proxy: ModelProxy[Option[GameData]]) = component(Props(router, proxy))
}
