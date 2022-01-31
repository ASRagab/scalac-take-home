package server.services

import client._
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.logging.slf4j.Slf4jLoggingBackend
import zio._

case class EnvConfig(token: Option[String])

object EnvConfig {
  val live = ZLayer.succeed(EnvConfig(Option(java.lang.System.getenv("GH_TOKEN"))))
}

case class Client(http: HttpClient)

object Client {
  val live = ZLayer.succeed {
    Client(
      AsyncHttpClientZioBackend().map(backend =>
        Slf4jLoggingBackend(backend, logResponseBody = true, logResponseHeaders = false, includeTiming = true)
      )
    )
  }
}

case class Backend(githubApi: GithubApi)

object Backend {
  val live: URLayer[Client with EnvConfig, Task[Backend]] = ZLayer {
    for {
      client <- ZIO.service[Client]
      env    <- ZIO.service[EnvConfig]
    } yield client.http.flatMap(backend => ZIO.attempt(Backend(new GithubApi(backend, env.token))))
  }
}
