package server.services

import zio._
import models.client._
import client.GithubApi

trait GithubService {
  protected[server] def getContributors(orgName: String): Task[List[Contributor]]

  protected[server] def getRepos(orgName: String): Task[List[Repo]]

  protected[server] def getRateLimit: Task[RateLimit]
}

case class GithubServiceLive(githubApi: GithubApi) extends GithubService {
  override protected[server] def getContributors(orgName: String): Task[List[Contributor]] =
    githubApi.getAllContributors(orgName)

  override protected[server] def getRepos(orgName: String): Task[List[Repo]] =
    githubApi.getAllRepos(orgName)

  override protected[server] def getRateLimit: Task[RateLimit] =
    githubApi.getRateLimit
}

object GithubService {
  val layer = ZLayer.fromFunction(GithubServiceLive.apply _)
}
