package demo3tier.api.metrics

import cats.data.Kleisli
import cats.effect.IO
import demo3tier.api.version.BuildInfo
import sttp.tapir.server.ServerEndpoint
import demo3tier.api.http.Http
import demo3tier.api.infrastructure.Json._
import sttp.tapir.generic.auto._

/**
  * Defines an endpoint which exposes the current application version information.
  */
class VersionApi(http: Http) {
  import VersionApi._
  import http._
  val versionK: Kleisli[IO, Unit, Version_OUT] = Kleisli { _ =>
    IO(Version_OUT(BuildInfo.builtAtString, BuildInfo.lastCommitHash))
  }
  val versionEndpoint: ServerEndpoint[Any, IO] = baseEndpoint.get
    .in("version")
    .out(jsonBody[Version_OUT])
    .serverLogic(versionK mapF toOutF run)
}

object VersionApi {
  case class Version_OUT(buildDate: String, buildSha: String)
}
