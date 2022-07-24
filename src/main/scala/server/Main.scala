package server

import client.{Client, EnvConfig, GithubApi}
import server.services.GithubService
import utils.Logging
import zhttp.http._
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio._

object Main extends ZIOAppDefault {

  val app: Http[GithubService, Throwable, Request, Response] = Http
    .collectZIO[Request] {
      case Method.GET -> !! / "org" / orgName / "contributors" => Handler.allContributors(orgName)
      case Method.GET -> !! / "org" / orgName / "repos"        => Handler.allRepos(orgName)
      case Method.GET -> !! / "api" / "rate-limit"             => Handler.rateLimit
      case Method.GET -> !!                                    => Handler.healthcheck
    }
    .orElse(Http.notFound)

  private val server = Server.app(app).withPort(8080)

  override def run: ZIO[Any, Throwable, Nothing] =
    server.make
      .flatMap { start =>
        Console.printLine(s"Server started on port ${start.port}") *> ZIO.never
      }
      .provide(
        EventLoopGroup.auto(),
        ServerChannelFactory.auto,
        Scope.default,
        Logging.layer,
        EnvConfig.layer,
        Client.layer,
        GithubApi.layer,
        GithubService.layer
      )
}
