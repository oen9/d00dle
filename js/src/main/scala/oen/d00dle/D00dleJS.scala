package oen.d00dle

import oen.d00dle.modules.{About, Home, Layout}
import oen.d00dle.services.AppCircuit
import japgolly.scalajs.react.extra.router._
import org.scalajs.dom.document
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import oen.d00dle.modules.Lobby
import oen.d00dle.modules.Game
import oen.d00dle.services.AppData.GameData
import oen.d00dle.services.AppData.User

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
    val gameWrapper = AppCircuit.connect(_.wsConnection.gameData.fold(GameData(User(0, "error")))(identity))
    val layoutWrapper = AppCircuit.connect(_.wsConnection.gameData)

    val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
      import dsl._

      (emptyRule
        | staticRoute(root, HomeLoc) ~> render(gameWrapper(Home(_)))
        | staticRoute("#about", AboutLoc) ~> render(About())
        | staticRoute("#lobby", LobbyLoc) ~> render(gameWrapper(Lobby(_)))
        | staticRoute("#game", GameLoc) ~> render(homeWrapper(Game.apply))
        )
        .notFound(redirectToPage(HomeLoc)(Redirect.Replace))
        .setTitle(p => s"PAGE = $p | Example App")
    }.renderWith((ctl, resolution) => layoutWrapper(Layout(ctl, resolution, _)))

    val router = Router(BaseUrl.until_#, routerConfig)
    router().renderIntoDOM(target)
  }
}
