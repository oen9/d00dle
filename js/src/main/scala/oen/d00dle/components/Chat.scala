package oen.d00dle.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.html
import oen.d00dle.shared.Dto
import oen.d00dle.services.AppData.User

object Chat {

  case class Props(me: User, msgs: Seq[Dto.ChatMsg])

  class Backend($: BackendScope[Props, Unit]) {

    private[this] val chatDiv = <.div(^.cls := "chat overflow-auto w-100")
    private[this] val chatDivRef = Ref[html.Div]

    def scrollChat(): Callback = chatDivRef.foreach(chatD => {
      chatD.scrollTop = chatD.scrollHeight
    })

    def render(props: Props) =
      <.div(^.cls := "row pt-2",
        chatDiv.withRef(chatDivRef)(
          props.msgs.zipWithIndex.map { case (msg, msgId) =>
              msg.chatMsgType match {
                case Dto.SystemMsg =>
                  <.div(^.key := msgId, ^.cls := "alert alert-primary mb-0", msg.msg)
                case _ =>
                  <.div(^.key := msgId,
                    (^.cls := "chat-me").when(msg.userId == props.me.id),
                    <.small(^.cls := "text-muted",
                      s"${msg.nickname} (${msg.userId}):",
                    ),
                    <.span(
                      ^.cls := "ml-1",
                      (^.cls := "red").when(msg.chatMsgType == Dto.Guessed),
                      msg.msg
                    )
                  )
              }
          }.toVdomArray
        )
      )
  }

  val component = ScalaComponent.builder[Props]("Chat")
    .renderBackend[Backend]
    .componentDidUpdate(_.backend.scrollChat())
    .componentDidMount(_.backend.scrollChat())
    .build

  def apply(me: User, msgs: Seq[Dto.ChatMsg]) = component(Props(me, msgs))
}
