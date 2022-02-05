package server

import models.client.Contributor
import models.client.ContributorSorts._
import zhttp.http.Response
import zio.ZIO
import io.circe.syntax._
import server.services.GithubService

import scala.util.chaining._

object Handler {
  def handleGetAllContributors(orgName: String): ZIO[GithubService, Throwable, Response] = {
    ZIO.serviceWithZIO[GithubService] { client =>
      client
        .getContributors(orgName)
        .map { contributors =>
          Contributor
            .mergeAndSort(contributors)
            .pipe(contributors => Response.json(contributors.map(_.toContributorCount).asJson.spaces2))
        }
    }
  }

  def handleGetAllRepos(orgName: String): ZIO[GithubService, Throwable, Response] = {
    ZIO.serviceWithZIO[GithubService] { client =>
      client
        .getRepos(orgName)
        .map { repos =>
          Response.json(repos.asJson.spaces2)
        }
    }
  }

  def handleGetRateLimit: ZIO[GithubService, Throwable, Response] = {
    ZIO.serviceWithZIO[GithubService] { client =>
      client.getRateLimit
        .map { rateLimit =>
          Response.json(rateLimit.asJson.spaces2)
        }
    }
  }
}
