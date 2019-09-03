package oen.d00dle.actors
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import akka.actor.typed.ActorRef

object UserDispatcher {
  sealed trait UserDispatcherMsg
  case class CreateUser(respondTo: ActorRef[UserCreated]) extends UserDispatcherMsg

  case class UserCreated(user: ActorRef[UserActor.UserActorMsg])

  def behavior(nextId: Int): Behavior[UserDispatcherMsg] = Behaviors.receive { (ctx, msg) =>
    msg match {
      case CreateUser(respondTo) =>
        val newUser = ctx.spawn(UserActor(nextId), s"user-$nextId")
        respondTo ! UserCreated(newUser)
        behavior(nextId + 1)
    }
  }

  def apply(): Behavior[UserDispatcherMsg] = behavior(1)
}
