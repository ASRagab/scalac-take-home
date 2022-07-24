package models.client

import io.circe.Codec
import io.circe.generic.semiauto._

case class Resources(core: Core)

object Resources {
  implicit val resourcesCodec: Codec.AsObject[Resources] = deriveCodec[Resources]
}
