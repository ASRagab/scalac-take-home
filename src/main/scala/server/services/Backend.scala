package server.services

import client._
import sttp.capabilities
import sttp.capabilities.zio.ZioStreams
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.logging.slf4j.Slf4jLoggingBackend
import utils.Logging
import zio._

case class EnvConfig(token: Option[String])

object EnvConfig {
  val live = ZLayer.succeed(EnvConfig(Option(java.lang.System.getenv("GH_TOKEN"))))
}

object Client {
  val live: ZLayer[Any, Throwable, SttpBackend[Task, ZioStreams with capabilities.WebSockets]] =
    AsyncHttpClientZioBackend()
      .map(backend =>
        Slf4jLoggingBackend(backend, logResponseBody = true, logResponseHeaders = false, includeTiming = true)
      )
      .toLayer

}

case class Backend(githubApi: GithubApi)

object Backend {
  val live: ZLayer[
    Logging with EnvConfig with SttpBackend[Task, ZioStreams with capabilities.WebSockets],
    Throwable,
    Backend
  ] = ZLayer {
    for {
      httpClient <- ZIO.service[SttpBackend[Task, ZioStreams with capabilities.WebSockets]]
      env        <- ZIO.service[EnvConfig]
      logging    <- ZIO.service[Logging]
      api         = new GithubApi(httpClient, env.token, logging)
      backend    <- ZIO.attempt(Backend(api))
    } yield backend
  }
}
