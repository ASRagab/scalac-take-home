package server

import io.circe.syntax._
import models.client.{Contributor, ContributorSorts}
import zhttp.http.Response
import zio._

trait GithubService {
  protected[server] def getContributors(
      orgName: String
  )(implicit ord: Ordering[Contributor] = ContributorSorts.defaultSort): Task[Response]

  protected[server] def getRepos(orgName: String): Task[Response]
}

object GithubService {
  val live = { backend: Backend =>
    new GithubService {

      private val api = backend.githubAPI

      protected[server] def getContributors(
          orgName: String
      )(implicit ord: Ordering[Contributor] = ContributorSorts.defaultSort): Task[Response] =
        api
          .getAllContributors(orgName)
          .map(Contributor.mergeAndSort)
          .map(contributors => Response.json(contributors.map(_.toContributorCount).asJson.spaces2))

      protected[server] def getRepos(orgName: String): Task[Response] =
        api.getAllRepos(orgName).map(repos => Response.json(repos.asJson.spaces2))
    }
  }.toLayer

}
