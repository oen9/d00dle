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

  sealed trait Loc { def name: String }
  case object HomeLoc extends Loc { val name = "home" }
  case object AboutLoc extends Loc { val name = "about" }
  case class LobbyLoc(id: Int) extends Loc { val name = "lobby" }
  case object GameLoc extends Loc { val name = "game" }

  @JSImport("bootstrap", JSImport.Default)
  @js.native
  object Bootstrap extends js.Object

  def main(args: Array[String]): Unit = {
    val target = document.getElementById("main")

    Bootstrap

    val homeWrapper = AppCircuit.connect(identity(_))
    val gameWrapper = AppCircuit.connect(_.wsConnection.gameData.fold(GameData(User(0, "error")))(identity))
    val layoutWrapper = AppCircuit.connect(_.wsConnection.gameData)
    val lobbyWrapper = AppCircuit.connect(_.wsConnection.gameData)

    val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
      import dsl._

      (emptyRule
        | staticRoute(root, HomeLoc) ~> renderR(router => gameWrapper(Home(router, _)))
        | staticRoute("#about", AboutLoc) ~> render(About())
        | dynamicRouteCT("#lobby" / int.caseClass[LobbyLoc]) ~> dynRenderR((loc, router) => lobbyWrapper(Lobby(router, _, loc.id)))
        | staticRoute("#game", GameLoc) ~> render(homeWrapper(Game.apply))
        )
        .notFound(redirectToPage(HomeLoc)(Redirect.Replace))
        .setTitle(p => s"d00dle - ${p.name}")
    }.renderWith((ctl, resolution) => layoutWrapper(Layout(ctl, resolution, _)))

    val router = Router(BaseUrl.until_#, routerConfig)
    router().renderIntoDOM(target)
  }
}
