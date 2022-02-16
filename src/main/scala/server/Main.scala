package server

import server.services.{Backend, Client, EnvConfig, GithubService}
import utils.Logging
import zhttp.http._
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio._

object Main extends ZIOAppDefault {

  val app: Http[GithubService, Throwable, Request, Response] = Http
    .collectZIO[Request] {
      case Method.GET -> !! / "org" / orgName / "contributors" => Handler.handleGetAllContributors(orgName)
      case Method.GET -> !! / "org" / orgName / "repos"        => Handler.handleGetAllRepos(orgName)
      case Method.GET -> !! / "api" / "rate-limit"             => Handler.handleGetRateLimit
    }
    .orElse(Http.notFound)

  private val server = Server.app(app).withPort(8080)

  override def run: ZIO[ZEnv, Throwable, Nothing] =
    server.make
      .use { start =>
        Console.printLine(s"Server started on port ${start.port}") *> ZIO.never
      }
      .provideCustom(
        EventLoopGroup.auto(),
        ServerChannelFactory.auto,
        EnvConfig.live,
        Client.live,
        Logging.live,
        Backend.live,
        GithubService.live
      )
}
