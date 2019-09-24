package oen.d00dle.components

import oen.d00dle.shared.Dto.GameUser
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import oen.d00dle.shared.Dto

object ScoreList {
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
              )
            ),
            <.tbody(
              props.users.zipWithIndex.map { case (u, idx) =>
                <.tr(^.key := u.u.id,
                  <.th(^.scope := "row", idx + 1),
                  <.td(
                    u.u.name,
                    <.small(^.cls := "ml-1", s"(${u.u.id})"),
                    <.small(^.cls := "ml-1 red", "quitted").when(u.presenceState == Dto.Quitted)
                  ),
                  <.td(u.points)
                )
              }.toVdomArray
            )
          )
        )
      )
    }.build


  def apply(users: IndexedSeq[GameUser]) = pointsList(Props(users))
}
