package models.client

import io.circe.generic.semiauto._

case class RateLimit(resources: Resources)

object RateLimit {
  implicit val decoder = deriveDecoder[RateLimit]
  implicit val encoder = deriveEncoder[RateLimit]
}
