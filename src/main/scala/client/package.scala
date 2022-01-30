import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3.SttpBackend
import zio.Task

package object client {
  type HttpClient = Task[SttpBackend[Task, ZioStreams with WebSockets]]
}
