package oen.d00dle.modules

import oen.d00dle.D00dleJS.{AboutLoc, HomeLoc, Loc}
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import japgolly.scalajs.react.vdom.html_<^._
import oen.d00dle.D00dleJS.GameLoc
import diode.react.ModelProxy
import oen.d00dle.services.AppData.GameData

object Layout {

  case class Props(router: RouterCtl[Loc], resolution: Resolution[Loc], proxy: ModelProxy[Option[GameData]])

  val menuItems = Seq(
    HomeLoc,
    GameLoc,
    AboutLoc
  )

  class Backend($: BackendScope[Props, Unit]) {
    def render(props: Props) = {
      def nav(props: Props) =
        <.div(^.cls := "navbar navbar-expand-md navbar-dark bg-dark",
          props.router.link(HomeLoc)(
            ^.cls := "navbar-brand",
            <.img(^.src := "front-res/img/logo-mini.png"),
            " d00dle"
          ),
          <.button(^.cls := "navbar-toggler", ^.tpe := "button", VdomAttr("data-toggle") := "collapse", VdomAttr("data-target") := "#navbarNav", ^.aria.controls := "navbarNav", ^.aria.expanded := "false", ^.aria.label := "Toggle navigation",
            <.span(^.cls := "navbar-toggler-icon")
          ),
          <.div(^.cls := "collapse navbar-collapse", ^.id := "navbarNav",
            <.ul(^.cls := "navbar-nav mr-auto",
              menuItems.map(item =>
                <.li(^.key := item.name, ^.cls := "nav-item", (^.cls := "active").when(props.resolution.page == item),
                  props.router.link(item)(^.cls := "nav-link", item.name)
                )
              ).toVdomArray
            )
          )
        )

      def contentBody(props: Props) = props.resolution.render()

      def footer(props: Props) =
        <.div(^.cls := "footer bg-dark text-white d-flex justify-content-center mt-auto py-3",
          "© 2019 oen"
        )

      React.Fragment(
        nav(props),
        <.div(^.cls := "container-fluid",
          <.div(^.cls := "mt-5", ^.role := "main",
            props.proxy().fold(
              <.div(
                <.div(^.cls := "d-flex justify-content-center",
                  <.div(^.cls := "spinner-border text-primary", ^.role := "status",
                    <.span(^.cls := "sr-only", "Loading...")
                  )
                ),
                <.div(^.cls := "d-flex justify-content-center", "connecting ...")
              ): VdomElement
            )(_ => contentBody(props))
          )
        ),
        footer(props)
      )
    }
  }


  val component = ScalaComponent.builder[Props]("Layout")
    .renderBackend[Backend]
    .build

  def apply(ctl: RouterCtl[Loc], resolution: Resolution[Loc], proxy: ModelProxy[Option[GameData]]) = component(Props(ctl, resolution, proxy))
}
