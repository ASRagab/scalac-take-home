package models.client

import io.circe.generic.semiauto._

case class Resources(core: Core)

object Resources {
  implicit val decoder = deriveDecoder[Resources]
  implicit val encoder = deriveEncoder[Resources]
}
