package models.server

import io.circe.generic.semiauto._

case class ContributorCount(
    name: String,
    contributions: Int
)

object ContributorCount {
  implicit val decoder = deriveDecoder[ContributorCount]
  implicit val encoder = deriveEncoder[ContributorCount]

}
