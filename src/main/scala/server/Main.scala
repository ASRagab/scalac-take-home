package server

import server.services.{Backend, Client, EnvConfig, GithubService}
import zhttp.http._
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio._

object Main extends ZIOAppDefault {

  val app: Http[GithubService, Throwable, Request, Response] = Http
    .collectZIO[Request] {
      case Method.GET -> !! / "org" / orgName / "contributors" => Handler.handleGetAllContributors(orgName)
      case Method.GET -> !! / "org" / orgName / "repos"        => Handler.handleGetAllRepos(orgName)
    }
    .orElse(Http.notFound)

  private val port = 8080
  private val server =
    Server.app(app).withPort(port)

  override def run =
    server.make
      .use { start =>
        Console.printLine(s"Server started on port ${start.port}") *> ZIO.never
      }
      .provideCustom(
        EventLoopGroup.auto(),
        ServerChannelFactory.auto,
        EnvConfig.live,
        Client.live,
        Backend.live,
        GithubService.live
      )
      .exitCode

}
