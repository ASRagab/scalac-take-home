package server.services

import models.client.{Contributor, Repo}
import zio._

trait GithubService {
  protected[server] def getContributors(orgName: String): Task[List[Contributor]]

  protected[server] def getRepos(orgName: String): Task[List[Repo]]
}

object GithubService {
  val live = ZLayer {
    for {
      backend <- ZIO.service[Task[Backend]]
    } yield new GithubService {

      protected[server] def getContributors(orgName: String): Task[List[Contributor]] =
        backend.toManaged.use(_.githubApi.getAllContributors(orgName))

      protected[server] def getRepos(orgName: String): Task[List[Repo]] =
        backend.toManaged.use(_.githubApi.getAllRepos(orgName))
    }
  }
}
