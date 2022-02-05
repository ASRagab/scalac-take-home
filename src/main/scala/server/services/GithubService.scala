package server.services

import models.client._
import zio._

trait GithubService {
  protected[server] def getContributors(orgName: String): Task[List[Contributor]]

  protected[server] def getRepos(orgName: String): Task[List[Repo]]

  protected[server] def getRateLimit: Task[RateLimit]
}

object GithubService {
  val live = ZLayer {
    ZIO.service[Backend].map { backend =>
      new GithubService {

        protected[server] def getContributors(orgName: String): Task[List[Contributor]] =
          backend.githubApi.getAllContributors(orgName)

        protected[server] def getRepos(orgName: String): Task[List[Repo]] =
          backend.githubApi.getAllRepos(orgName)

        protected[server] def getRateLimit: Task[RateLimit] =
          backend.githubApi.getRateLimit
      }
    }
  }
}
