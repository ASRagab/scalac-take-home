package client

import sttp.client3._
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.model.{Header, StatusCode}
import utils.Logging
import zio.ZIO
import zio.test.Assertion._
import zio.test._

object GithubApiTest extends ZIOSpecDefault {

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

  override def spec = suite("GithubApi")(
    suite("getLastPage")(
      test("getLastPage zio should return last page from header") {
        for {
          lastPage <- githubApi.getLastPage(testUri)
        } yield assert(lastPage)(equalTo(expected))
      },
      test("getLastPage empty should return default uri") {
        for {
          lastPage <- githubApi.getLastPage(empty)
        } yield assert(lastPage)(equalTo(empty))
      }
    ),
    suite("getLinkHeader")(
      test("getLinkHeader should return the links as list of strings") {
        for {
          response <- githubApi.getLinkHeader(testUri)
        } yield assert(response)(equalTo(List(validLinkHeader)))
      },
      test("getLinkHeader should return empty list if no link header") {
        for {
          response <- githubApi.getLinkHeader(empty)
        } yield assert(response)(equalTo(List.empty[String]))
      }
    ),
    suite("getAllPages")(
      test("getAllPages returns list of length equal to lastPage") {
        assertTrue(githubApi.getAllPages(expected, 100) == 3)
      },
      test("getAllPages should return list of length 1 if no link header") {
        assertTrue(githubApi.getAllPages(empty, 100).length == 1)
      }
    )
  )
}
