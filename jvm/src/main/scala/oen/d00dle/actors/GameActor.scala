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
  case class SendChatMsg(nickname: String, userId: Int, msg: String) extends GameActorMsg

  case class GameUser(ref: Option[ActorRef[UserActor.UserActorMsg]], dtoData: Dto.GameUser)
  case class GameState(id: Int, users: Vector[GameUser], draftsman: GameUser, queue: Vector[GameUser], secret: String)

  def behavior(state: GameState): Behavior[GameActorMsg] = Behaviors.receive { case (ctx, msg) =>

    msg match {
      case SendChatMsg(nickname, userId, msg) =>
        val guessState = if (msg == state.secret && userId != state.draftsman.dtoData.u.id) Dto.Guessed else Dto.NotGuessed
        broadcastMessage(state.users, Dto.NewChatMsg(Dto.ChatMsg(nickname, userId, msg, guessState)))
        guessState match {
          case Dto.Guessed =>
            val (newDraftsman, newQueue, newSecret) = initNextDrawing(state.queue :+ state.draftsman)
            val updUsers = updateScores(state.draftsman, userId, state.users)
            updUsers
              .filter(u => u.dtoData.u.id == userId || u.dtoData.u.id == state.draftsman.dtoData.u.id)
              .foreach(broadcastUserChanged(updUsers, _))
            val newState = state.copy(
              users = updUsers,
              draftsman = newDraftsman,
              queue = newQueue,
              secret = newSecret
            )
            behavior(newState)
          case _ =>
            Behaviors.same
        }

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
    broadcastMessage(users, msg)
  }

  def broadcastMessage(users: Vector[GameUser], msg: UserActor.UserActorMsg) = users.map(_.ref).flatten.foreach(_ ! msg)

  def apply(gameId: Int, lobbyUsers: Vector[LobbyActor.LobbyUser]): Behavior[GameActorMsg] = Behaviors.setup { ctx =>
    val gameUsers = lobbyUsers.map(
      _.into[GameUser]
      .withFieldComputed(_.ref, _.ref.some)
      .transform
    )
    lobbyUsers.foreach(u => ctx.watchWith(u.ref, UserQuitted(u.dtoData.u.id)))
    broadcastMessage(gameUsers, UserActor.InitGame(ctx.self, gameUsers.map(_.dtoData)))
    broadcastMessage(gameUsers, Dto.NewChatMsg(systemChatMsgTemplate.copy(msg = "Welcome!")))

    val (draftsman, queue, secret) = initNextDrawing(gameUsers)

    behavior(GameState(
      id = gameId,
      users = gameUsers,
      draftsman = draftsman,
      queue = queue,
      secret = secret
    ))

  }

  def initNextDrawing(users: Vector[GameUser]): (GameUser, Vector[GameUser], String) = {
    val draftsman = users.head
    val queue = users.tail

    val nextSecret = getDummySecret

    broadcastMessage(queue, Dto.NowDraws(draftsman.dtoData.u.id))
    draftsman.ref.foreach(_ ! UserActor.Forward(Dto.YouDraw(nextSecret)))

    (draftsman, queue, nextSecret)
  }

  def updateScores(draftsman: GameUser, winnerId: Int, users: Vector[GameUser]): Vector[GameUser] = {
    val draftsmanScore = 15
    val winnerScore = 10

    val draftsmanPredicate: GameUser => Boolean = _.dtoData.u.id == draftsman.dtoData.u.id
    val winnerPredicate: GameUser => Boolean = _.dtoData.u.id == winnerId

    users
      .modify(_.eachWhere(draftsmanPredicate).dtoData.points).using(_ + draftsmanScore)
      .modify(_.eachWhere(winnerPredicate).dtoData.points).using(_ + winnerScore)
  }

  val systemChatMsgTemplate = Dto.ChatMsg("system", -1, "", Dto.SystemMsg)
  val dummySecrets = Vector("cat", "dog", "car", "eye", "hand", "water")
  def getDummySecret: String = dummySecrets.get(scala.util.Random.nextInt(dummySecrets.length)).getOrElse("child")
}
