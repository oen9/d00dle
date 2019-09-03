package oen.d00dle.actors
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import oen.d00dle.shared.Dto._

object UserActor {
  val DEFAULT_USER_NAME = "unknown"

  sealed trait UserActorMsg
  case class RegisterOut(respondTo: ActorRef[OutData]) extends UserActorMsg
  case class InData(wsData: WsData) extends UserActorMsg

  sealed trait OutData
  case class ToOut(wsData: WsData) extends OutData
  case object Complete extends UserActorMsg with OutData
  case class Fail(ex: Throwable) extends UserActorMsg with OutData

  def initBehavior(id: Int): Behavior[UserActorMsg] = Behaviors.receive { (ctx, msg) =>
    msg match {
      case RegisterOut(respondTo) =>
        respondTo ! ToOut(UserCreated(id, DEFAULT_USER_NAME))
        behavior(id, respondTo, DEFAULT_USER_NAME)

      case unknown =>
        ctx.log.error("user: {} is waiting for outRef. Unhandled: {}", id, unknown)
        Behavior.same
    }
  }

  def behavior(
    id: Int,
    outRef: ActorRef[OutData],
    nickname: String
  ): Behavior[UserActorMsg] = Behaviors.receive { (ctx, msg) =>
    msg match {
      case InData(log@Log(msg)) =>
        ctx.log.info("log msg: [{}] from user: {}", msg, id)
        outRef ! ToOut(log)
        Behavior.same
      case InData(ChangeNickname(newNickname)) =>
        ctx.log.debug("user: {} changed nickname from [{}] to [{}]", id, nickname, newNickname)
        outRef ! ToOut(NicknameChanged(id, newNickname)) // TODO broadcast it inside lobby
        behavior(id, outRef, newNickname)
      case InData(msg) =>
        outRef ! ToOut(msg)
        Behavior.same

      case RegisterOut(respondTo) =>
        ctx.log.error("outRef already registered for user: {}", id)
        Behavior.same

      case Complete =>
        ctx.log.info("user: {} quitted", id)
        Behavior.stopped
      case Fail(ex) =>
        ctx.log.error(ex, "fail for user: {}", id)
        Behavior.stopped
    }
  }

  def apply(id: Int): Behavior[UserActorMsg] = Behaviors.setup { ctx =>
    ctx.log.info("user: {} created", id)
    initBehavior(id)
  }
}
