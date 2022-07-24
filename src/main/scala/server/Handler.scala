package server

import models.client.{Basic, Contributor}
import models.client.ContributorSorts._
import zhttp.http.Response
import zio.ZIO
import io.circe.syntax._
import server.services.GithubService

import java.time.LocalDateTime
import scala.util.chaining._

object Handler {
  def allContributors(orgName: String): ZIO[GithubService, Throwable, Response] = {
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

  def allRepos(orgName: String): ZIO[GithubService, Throwable, Response] = {
    ZIO.serviceWithZIO[GithubService] { client =>
      client
        .getRepos(orgName)
        .map { repos =>
          Response.json(repos.asJson.spaces2)
        }
    }
  }

  def rateLimit: ZIO[GithubService, Throwable, Response] = {
    ZIO.serviceWithZIO[GithubService] { client =>
      client.getRateLimit
        .map { rateLimit =>
          Response.json(rateLimit.asJson.spaces2)
        }
    }
  }

  def basic: ZIO[GithubService, Nothing, Response] = ZIO.serviceWithZIO[GithubService] { _ =>
    ZIO.succeed(Response.json(Basic(LocalDateTime.now()).asJson.spaces2))
  }
}
