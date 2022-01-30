package models.client

import io.circe.generic.semiauto._

case class Repo(
    id: Long,
    name: String,
    full_name: String,
    size: Long
)

object Repo {
  implicit val repoEncoder = deriveEncoder[Repo]
  implicit val repoDecoder = deriveDecoder[Repo]
}
