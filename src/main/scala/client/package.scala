import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3.SttpBackend
import zio.Task

package object client {
  type HttpBackend = SttpBackend[Task, ZioStreams with WebSockets]
  type HttpClient  = Task[HttpBackend]
}
