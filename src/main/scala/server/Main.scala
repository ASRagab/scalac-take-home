package server

import zhttp.http._
import zhttp.service.{EventLoopGroup, Server}
import zhttp.service.server.ServerChannelFactory
import zio._

object Main extends ZIOAppDefault {
  val clientLayer = Environment.live ++ Client.live
  val services = (Environment.live ++ Client.live) >>> Backend.live >>> GithubService.live

  val app: Http[GithubService, Throwable, Request, Response] = Http.collectZIO[Request] {
    case Method.GET -> !! / "org" / orgName / "contributors" =>
      ZIO.environmentWithZIO(_.get.getContributors(orgName))
    case Method.GET -> !! / "org" / orgName / "repos" =>
      ZIO.environmentWithZIO(_.get.getRepos(orgName))
  }

  private val port = 8080
  private val server =
    Server.app(app).withPort(port)

  override def run = {
    server.make
      .use { start =>
        Console.printLine(s"Server started on port ${start.port}") *> ZIO.never
      }
      .provideCustom(services >>> ServerChannelFactory.auto ++ EventLoopGroup.auto())
      .exitCode
  }

}
