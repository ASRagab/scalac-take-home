package models.client

import io.circe.Codec
import io.circe.generic.semiauto._

case class Repo(
    id: Long,
    name: String,
    full_name: String,
    size: Long
)

object Repo {
  implicit val repoCodec: Codec.AsObject[Repo] = deriveCodec[Repo]
}
