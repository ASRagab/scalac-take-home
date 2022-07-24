package models.client

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import java.time.LocalDateTime

case class Healthcheck(serverTime: LocalDateTime)

object Healthcheck {
  implicit val healthcheckCodec: Codec.AsObject[Healthcheck] = deriveCodec[Healthcheck]
}
