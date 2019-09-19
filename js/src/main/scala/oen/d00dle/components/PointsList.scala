package oen.d00dle.components

import oen.d00dle.shared.Dto.GameUser
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object PointsList {
  case class Props(users: IndexedSeq[GameUser])

  val pointsList = ScalaComponent.builder[Props]("PointsList")
    .render_P { props =>
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
    }.build


  def apply(users: IndexedSeq[GameUser]) = pointsList(Props(users))
}
