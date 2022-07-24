package models.server

import io.circe.Codec
import io.circe.generic.semiauto._

case class ContributorCount(
    name: String,
    contributions: Int
)

object ContributorCount {
  implicit val countCodec: Codec.AsObject[ContributorCount] = deriveCodec[ContributorCount]
}
