package server

import client._
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.logging.slf4j.Slf4jLoggingBackend
import zio._

trait Env {
  def token: Option[String]
}

object Env {
  val live = ZLayer.succeed(new Env {
    override val token: Option[String] = Option(java.lang.System.getenv("GH_TOKEN"))
  })

  val test = ZLayer.succeed(new Env {
    override val token: Option[String] = Some("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")
  })
}

trait Client {
  def httpClient: HttpClient
}

object Client {
  def live = AsyncHttpClientZioBackend()
    .map(backend =>
      Slf4jLoggingBackend(backend, logResponseBody = true, logResponseHeaders = false, includeTiming = true)
    )
    .toLayer
}

trait Backend {
  def githubAPI: GithubAPI
}

object Backend {
  val live: URLayer[Client with Env, Backend] = ZLayer {
    for {
      client <- ZIO.service[Client]
      env    <- ZIO.service[Env]
    } yield new Backend {
      override def githubAPI: GithubAPI = new GithubAPI(client.httpClient, env.token)
    }
  }
}
