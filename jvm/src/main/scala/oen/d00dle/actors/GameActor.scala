package oen.d00dle.actors
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import akka.actor.typed.ActorRef
import oen.d00dle.shared.Dto
import io.scalaland.chimney.dsl._
import com.softwaremill.quicklens._
import cats.implicits._

object GameActor {
  sealed trait GameActorMsg
  case class UserQuitted(userId: Int) extends GameActorMsg

  case class GameUser(ref: Option[ActorRef[UserActor.UserActorMsg]], dtoData: Dto.GameUser)
  case class GameState(id: Int, users: Vector[GameUser])

  def behavior(state: GameState): Behavior[GameActorMsg] = Behaviors.receive { case (ctx, msg) =>

    msg match {
      case UserQuitted(userId) =>
        val userByIdPredicate: GameUser => Boolean = _.dtoData.u.id == userId
        state.users.find(userByIdPredicate) match {
          case Some(user) =>
            val updUser = user
              .modify(_.dtoData.presenceState).setTo(Dto.Quitted)
              .modify(_.ref).setTo(None)
            val newState = state.modify(_.users.eachWhere(userByIdPredicate)).setTo(updUser)
            broadcastUserChanged(newState.users, updUser)

            newState.users.filter(_.dtoData.presenceState == Dto.Present).size match {
              case 0 =>
                ctx.log.info("Game [{}] stopped", state.id)
                Behaviors.stopped
              case 1 => behavior(newState) // TODO end game
              case _ => behavior(newState)
            }
          case None => Behaviors.same
        }
    }
  }

  def broadcastUserChanged(users: Vector[GameUser], changedUser: GameUser): Unit = {
    val msgToBroadcast = Dto.GameUserChanged(changedUser.dtoData)
    broadcastMessage(users, msgToBroadcast)
  }

  def broadcastMessage(users: Vector[GameUser], dto: Dto.WsData): Unit = {
    val msg = UserActor.Forward(dto)
    users.map(_.ref).flatten.foreach(_ ! msg)
  }

  def apply(gameId: Int, lobbyUsers: Vector[LobbyActor.LobbyUser]): Behavior[GameActorMsg] = Behaviors.setup { ctx =>
    val gameUsers = lobbyUsers.map(
      _.into[GameUser]
      .withFieldComputed(_.ref, _.ref.some)
      .transform
    )
    lobbyUsers.foreach(u => ctx.watchWith(u.ref, UserQuitted(u.dtoData.u.id)))
    broadcastMessage(gameUsers, Dto.GameStarted(gameUsers.map(_.dtoData)))
    behavior(GameState(gameId, gameUsers))
  }
}
