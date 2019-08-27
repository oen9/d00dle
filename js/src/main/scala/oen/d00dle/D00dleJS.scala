package oen.d00dle

import oen.d00dle.modules.{About, Home, Layout}
import oen.d00dle.services.AppCircuit
import japgolly.scalajs.react.extra.router._
import org.scalajs.dom.document
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import oen.d00dle.modules.Lobby
import oen.d00dle.modules.Game

object D00dleJS {

  sealed abstract class Loc(val name: String)
  case object HomeLoc extends Loc("home")
  case object AboutLoc extends Loc("about")
  case object LobbyLoc extends Loc("lobby test")
  case object GameLoc extends Loc("game test")

  @JSImport("bootstrap", JSImport.Default)
  @js.native
  object Bootstrap extends js.Object

  def main(args: Array[String]): Unit = {
    val target = document.getElementById("main")

    Bootstrap

    val homeWrapper = AppCircuit.connect(identity(_))

    val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
      import dsl._

      (emptyRule
        | staticRoute(root, HomeLoc) ~> render(homeWrapper(Home(_)))
        | staticRoute("#about", AboutLoc) ~> render(About())
        | staticRoute("#lobby", LobbyLoc) ~> render(homeWrapper(Lobby.apply))
        | staticRoute("#game", GameLoc) ~> render(homeWrapper(Game.apply))
        )
        .notFound(redirectToPage(HomeLoc)(Redirect.Replace))
        .setTitle(p => s"PAGE = $p | Example App")
    }.renderWith(Layout.apply)

    val router = Router(BaseUrl.until_#, routerConfig)
    router().renderIntoDOM(target)
  }
}
