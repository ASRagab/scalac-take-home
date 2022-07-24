package models.client

import io.circe.generic.semiauto.deriveCodec

import java.time.LocalDateTime

case class Basic(serverTime: LocalDateTime)

object Basic {
  implicit val codec = deriveCodec[Basic]
}
