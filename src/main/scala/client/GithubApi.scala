package client

import client.GithubApi._
import io.circe
import io.circe.{Decoder, Encoder}
import models.client.{Contributor, Repo}
import org.slf4j.LoggerFactory
import sttp.client3._
import sttp.client3.circe._
import sttp.model.{Header, HeaderNames, Uri}
import zio.{Task, ZIO}

import scala.concurrent.duration._
import scala.util.chaining._
import scala.util.matching.Regex

class GithubApi(backend: HttpBackend, token: Option[String]) {

  /** First retrieve the list of repositories for the given org, then generate a list of page links to
    * @param orgName
    * @return
    */
  def getAllContributors(orgName: String): Task[List[Contributor]] =
    for {
      repos <- getAllRepos(orgName)
      nonEmptyRepos = repos.filter(_.size > 0)
      contributorPages <- ZIO.foreachPar(nonEmptyRepos)(repo => getAllContributorPages(orgName, repo.name))
      contributors     <- ZIO.foreachPar(contributorPages.flatten)(page => call[Contributor](page))
    } yield contributors.flatten

  def getAllRepos(orgName: String): Task[List[Repo]] =
    for {
      lastPage <- getLastPage(reposUri(orgName))
      pages    <- ZIO.succeed(getAllPages(lastPage, perPage = defaultPerPage))
      repos    <- ZIO.foreachPar(pages)(page => call[Repo](page))
    } yield repos.flatten

  protected[client] def getAllContributorPages(orgName: String, repoName: String): Task[List[Uri]] =
    for {
      lastPage <- getLastPage(contributorsUri(orgName, repoName, perPage = defaultPerPage))
      pages    <- ZIO.succeed(getAllPages(lastPage, perPage = defaultPerPage))
    } yield pages

  /** Extracts the last page number from the uri and then generates all page links by ranging from 1 to the last page.
    * @param lastPage
    * @return
    */
  protected[client] def getAllPages(lastPage: Uri, perPage: Int): List[Uri] = {
    lastPage.params
      .get("page")
      .map(_.toInt)
      .map(page =>
        (1 to page).map(pageNo => lastPage.withParams("page" -> pageNo.toString, "per_page" -> perPage.toString))
      )
      .getOrElse(List(lastPage))
      .toList
  }

  /** We grab the link header and try to extract the last page, if it exists, otherwise we assume there's only one page
    * of results.
    * @param uri
    * @return
    */
  protected[client] def getLastPage(uri: Uri): Task[Uri] =
    getLinkHeader(uri).map { links =>
      if (links.isEmpty) uri
      else {
        links.head
          .tap(link => logger.debug(s"Found last page: $link"))
          .split(",")
          .find(_.contains("rel=\"last\""))
          .flatMap(linkExtractor.findFirstIn)
          .flatMap(Uri.parse(_).toOption)
          .getOrElse(uri)
      }
    }

  protected[client] def getLinkHeader(uri: Uri): Task[List[String]] =
    basicRequest
      .headers(getHeaders(token): _*)
      .head(uri)
      .send(backend)
      .map(_.headers("link").toList)

  /** Generic sttp call to the Github API, will fail on any non-2xx response
    * @param uri
    * @tparam A
    * @return
    */
  private def call[A: Encoder: Decoder](uri: Uri): Task[List[A]] = {
    basicRequest
      .get(uri)
      .readTimeout(30.seconds)
      .headers(getHeaders(token): _*)
      .response(asJson[List[A]])
      .send(backend)
      .pipe(handleResponse)
  }
}

object GithubApi {
  private val logger = LoggerFactory.getLogger(this.getClass)

  val api = "https://api.github.com"

  val linkExtractor: Regex = raw"(?<=<).*(?=>)".r

  val authHeader: String => Header = token => Header(HeaderNames.Authorization, s"token $token")

  // Recommended Header https://docs.github.com/en/rest/overview/media-types#request-specific-version
  val acceptHeader = Header(HeaderNames.Accept, "application/vnd.github.v3+json")

  val defaultPerPage = 100

  def getHeaders(maybeToken: Option[String]): List[Header] =
    maybeToken
      .map(token => List(Header(HeaderNames.Authorization, s"token $token"), acceptHeader))
      .getOrElse(List(acceptHeader))

  def reposUri(orgName: String, page: Int = 1, perPage: Int = defaultPerPage): Uri =
    uri"$api/orgs/$orgName/repos?per_page=$perPage&page=$page"

  def contributorsUri(orgName: String, repoName: String, page: Int = 1, perPage: Int = defaultPerPage): Uri =
    uri"$api/repos/$orgName/$repoName/contributors?per_page=$perPage&page=$page"

  // TODO: Handle response such that certain status codes are not always failures and return and empty A instead
  def handleResponse[A](responseZIO: Task[Response[Either[ResponseException[String, circe.Error], A]]]): Task[A] =
    responseZIO.flatMap(response =>
      response.body match {
        case Right(a) => ZIO.succeed(a)
        case Left(error) =>
          logger.error(s"Error: ${response.request.uri} | ${error}")
          ZIO.fail(error)
      }
    )
}
