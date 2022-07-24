package models.client

import io.circe.Codec
import io.circe.generic.semiauto._
import models.server.ContributorCount

case class Contributor(
    id: Long,
    login: String,
    `type`: String,
    contributions: Int
)

object Contributor {
  implicit val contributorCodec: Codec.AsObject[Contributor] = deriveCodec[Contributor]

  def mergeAndSort(contributors: List[Contributor])(implicit ord: Ordering[Contributor]): List[Contributor] =
    contributors
      .groupBy(_.id)
      .map { case (id, contributors) =>
        Contributor(
          id = id,
          login = contributors.head.login,
          `type` = contributors.head.`type`,
          contributions = contributors.map(_.contributions).sum
        )
      }
      .toList
      .sorted

  implicit class ContributorOps(private val contributor: Contributor) extends AnyVal {
    def toContributorCount: ContributorCount = ContributorCount(contributor.login, contributor.contributions)
  }
}

object ContributorSorts {
  implicit val defaultSort: Ordering[Contributor] = (x: Contributor, y: Contributor) => {
    val byContributions = y.contributions.compareTo(x.contributions) // descending numeric

    if (byContributions == 0)
      x.login.compareTo(y.login) // descending alphabetical
    else
      byContributions
  }
}
