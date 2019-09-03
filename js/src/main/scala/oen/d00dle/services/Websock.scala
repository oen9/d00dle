package oen.d00dle.services

import diode.{Effect, NoAction}
import org.scalajs.dom
import org.scalajs.dom.{CloseEvent, Event, MessageEvent, WebSocket}
import oen.d00dle.shared.Dto._
import oen.d00dle.services.AppData._
import io.circe.generic.extras.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import diode.Action

object Websock {

  val protocol = if ("http:" == dom.window.location.protocol) "ws://" else "wss://"
  val host = dom.window.location.host // prod
  // val host = "localhost:8080" // dev
  val url = protocol + host + "/game"

  def connect(dispatch: Action => Unit) = {
    def onopen(e: Event): Unit = { dispatch(WSConnected("foo")) }

    def onmessage(e: MessageEvent): Unit = {
      import io.circe.parser.decode
      println("websock msg: " + e.data.toString())
      decode[WsData](e.data.toString).fold(e => println(s"error: $e"), {
        case unknown => println(s"[ws] unsupported data: $unknown")
      })
    }

    def onerror(e: Event): Unit = {
      val msg: String = e.asInstanceOf[js.Dynamic]
        .message.asInstanceOf[js.UndefOr[String]]
        .fold(s"error occurred!")("error occurred: " + _)
      println(s"[ws] $msg")
    }

    def onclose(e: CloseEvent): Unit = {
      dispatch(WSDisconnected)
      println("disconnected")
    }

    val ws = new WebSocket(url)
    ws.onopen = onopen _
    ws.onclose = onclose _
    ws.onmessage = onmessage _
    ws.onerror = onerror _
    ws
  }

  def send(ws: dom.WebSocket, data: WsData): Unit = {
    if (ws.readyState == 1) {
      val msg = data.asJson.noSpaces
      ws.send(msg)
    }
  }

  def sendAsEffect(ws: dom.WebSocket, data: WsData)(implicit ec: ExecutionContext): Effect = Effect.action {
    send(ws, data)
    NoAction
  }
}
