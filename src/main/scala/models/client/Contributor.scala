package models.client

import io.circe.generic.semiauto._
import models.server.ContributorCount

case class Contributor(
    id: Long,
    login: String,
    `type`: String,
    contributions: Int
)

object Contributor {
  implicit val userDecoder = deriveDecoder[Contributor]
  implicit val userEncoder = deriveEncoder[Contributor]

  def mergeAndSort(contributors: List[Contributor])(implicit ord: Ordering[Contributor]) =
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
    def toContributorCount = ContributorCount(contributor.login, contributor.contributions)
  }
}

object ContributorSorts {
  implicit val defaultSort = new Ordering[Contributor] {
    override def compare(x: Contributor, y: Contributor): Int = {
      val byContributions = y.contributions.compareTo(x.contributions) // descending numeric

      if (byContributions == 0)
        x.login.compareTo(y.login) // descending alphabetical
      else
        byContributions
    }
  }
}
