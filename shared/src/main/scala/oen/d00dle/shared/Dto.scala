package oen.d00dle.shared

object Dto {
  sealed trait WsData

  case class Err(t: String) extends WsData

  sealed trait Event extends WsData
  case class UserCreated(id: Int, nickname: String) extends Event

  sealed trait Cmd extends WsData
  case class Log(msg: String) extends Cmd

  import io.circe.generic.extras.Configuration
  implicit val circeConfig = Configuration.default.withDiscriminator("type").withDefaults
}
