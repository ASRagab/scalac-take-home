package models.client

import io.circe.Codec
import io.circe.generic.semiauto._

case class RateLimit(resources: Resources)

object RateLimit {
  implicit val rateLimitCodec: Codec.AsObject[RateLimit] = deriveCodec[RateLimit]
}
