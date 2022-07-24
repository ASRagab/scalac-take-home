import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.logging.slf4j.Slf4jLoggingBackend
import zio._

package object client {
  type HttpBackend = SttpBackend[Task, ZioStreams with WebSockets]
  type HttpClient  = Task[HttpBackend]

  case class EnvConfig(token: Option[String])

  object EnvConfig {
    val layer = ZLayer.succeed(EnvConfig(Option(java.lang.System.getenv("GH_TOKEN"))))
  }

  object Client {
    val layer: ZLayer[Any, Throwable, HttpBackend] = {
      ZLayer.fromZIO(
        AsyncHttpClientZioBackend()
          .map(backend =>
            Slf4jLoggingBackend(backend, logResponseBody = true, logResponseHeaders = false, includeTiming = true)
          )
      )
    }

  }

}
