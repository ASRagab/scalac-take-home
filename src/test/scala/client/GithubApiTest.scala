package client

import sttp.client3._
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.model.{Header, StatusCode}
import utils.Logging
import zio.ZIO
import zio.test.Assertion._
import zio.test._

object GithubApiTest extends DefaultRunnableSpec {

  val validLinkHeader =
    """
      |<https://api.github.com/repositories/370657256/contributors?per_page=100&page=2>; rel="next", <https://api.github.com/repositories/370657256/contributors?per_page=100&page=3>; rel="last"
      |""".stripMargin

  val response =
    Response[String](
      body = "",
      code = StatusCode.Ok,
      statusText = "Ok",
      headers = List(Header("link", validLinkHeader))
    )

  val stub = AsyncHttpClientZioBackend.stub
    .whenRequestMatches(_.uri.path.contains("zio"))
    .thenRespondF(_ => ZIO.succeed(response))
    .whenRequestMatches(_.uri.path.contains("empty"))
    .thenRespondF(_ => ZIO.succeed(Response[String](body = "", code = StatusCode.Ok, statusText = "Ok")))

  val githubApi = new GithubApi(stub, Some("token"), Logging())
  val expected  = uri"https://api.github.com/repositories/370657256/contributors?per_page=100&page=3"

  val testUri = uri"https://api.github.com/repos/zio"
  val empty   = uri"https://api.github.com/repos/empty"

  override def spec: ZSpec[TestEnvironment, Any] = suite("GithubApi")(
    suite("getLastPage")(
      test("getLastPage zio should return last page from header") {
        assertM(githubApi.getLastPage(testUri))(equalTo(expected))
      },
      test("getLastPage empty should return default uri") {
        assertM(githubApi.getLastPage(empty))(equalTo(empty))
      }
    ),
    suite("getLinkHeader")(
      test("getLinkHeader should return the links as list of strings") {
        assertM(githubApi.getLinkHeader(testUri))(equalTo(List(validLinkHeader)))
      },
      test("getLinkHeader should return empty list if no link header") {
        assertM(githubApi.getLinkHeader(empty))(equalTo(List.empty))
      }
    ),
    suite("getAllPages")(
      test("getAllPages returns list of length equal to lastPage") {
        assert(githubApi.getAllPages(expected, 100).length)(equalTo(3))
      },
      test("getAllPages should return list of length 1 if no link header") {
        assert(githubApi.getAllPages(empty, 100).length)(equalTo(1))
      }
    )
  )
}
