package demo3tier.api.http

import cats.data.{Kleisli, NonEmptyList, OptionT}
import cats.implicits._
import cats.effect._
import com.comcast.ip4s.{Host, Port}
import com.typesafe.scalalogging.StrictLogging
import demo3tier.api.util.{Http4sCorrelationMiddleware, ServerEndpoints}
import io.prometheus.client.CollectorRegistry
import org.http4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.metrics.prometheus.Prometheus
import org.http4s.server.{Router, Server}
import org.http4s.server.middleware.{CORS, Metrics}
import org.http4s.server.staticcontent._
import sttp.tapir.resourceGetServerEndpoint
import sttp.tapir.server.ServerEndpoint
import demo3tier.api.infrastructure.CorrelationId
import demo3tier.api.util.Http4sCorrelationMiddleware._

import scala.concurrent.duration.DurationInt


/**
  * Interprets the endpoint descriptions (defined using tapir) as http4s routes, adding CORS, metrics, api docs
  * and correlation id support.
  *
  * The following endpoints are exposed:
  * - `/api/v1` - the main API
  * - `/api/v1/docs` - swagger UI for the main API
  * - `/admin` - admin API
  * - `/` - serving frontend resources
  */
class HttpApi(
    http: demo3tier.api.http.Http,
    endpoints: ServerEndpoints,
    adminEndpoints: ServerEndpoints,
    serviceEndpoints: ServerEndpoints,
    collectorRegistry: CollectorRegistry,
    config: HttpConfig
) extends StrictLogging {
  private val apiContextPath = List("api", "v1")
  private val endpointsToRoutes = new EndpointsToRoutes(http, apiContextPath)

  lazy val mainRoutes: HttpRoutes[IO] =
    Http4sCorrelationMiddleware(CorrelationId).withCorrelationId(loggingMiddleware(endpointsToRoutes(endpoints concatNel serviceEndpoints)))
  private lazy val adminRoutes: HttpRoutes[IO] = endpointsToRoutes(adminEndpoints)
  private lazy val docsRoutes: HttpRoutes[IO] = endpointsToRoutes.toDocsRoutes(endpoints)

  /**
    * The resource describing the HTTP server; binds when the resource is allocated.
    */
  lazy val resource: Resource[IO, Server] = {
    val classifierFunc = (r: Request[IO]) => r.uri.path.toString.toLowerCase.some
    val prometheusHttp4sMetrics = Prometheus.metricsOps[IO](collectorRegistry, "iv_template_server")
    prometheusHttp4sMetrics
      .map(m => Metrics[IO](m, Status.NotFound.some, _ => Status.InternalServerError.some, classifierFunc)(mainRoutes))
      .>>= { monitoredRoutes =>
        val app: HttpApp[IO] = Router(
          // for /api/v1 requests, first trying the API; then the docs; then, returning 404
          s"/${apiContextPath.mkString("/")}" -> CORS.policy.withAllowOriginAll
            .withAllowCredentials(false)(monitoredRoutes <+> docsRoutes <+> respondWithNotFound),
          "/admin" -> adminRoutes,
          // for all other requests, first trying getting existing webapp resource;
          // otherwise, returning index.html; this is needed to support paths in the frontend apps (e.g. /login)
          // the frontend app will handle displaying appropriate error messages
          "" -> (webappRoutes <+> indexResponse())
        ).orNotFound

        EmberServerBuilder
          .default[IO]
          .withHost(Host.fromString(config.host).get)
          .withPort(Port.fromString(config.port.toString).get)
          .withHttpApp(app)
          .build

      }
  }

  private def indexResponse(): HttpRoutes[IO] = {
    val loader = classOf[HttpApi].getClassLoader
    val indexEndpoint: ServerEndpoint[Any, IO] = resourceGetServerEndpoint("")(loader, s"/webapp/index.html")
    endpointsToRoutes(NonEmptyList.one(indexEndpoint))
  }

  private val respondWithNotFound: HttpRoutes[IO] = Kleisli(_ => OptionT.pure(Response.notFound))

  private def loggingMiddleware(service: HttpRoutes[IO]): HttpRoutes[IO] = Kleisli { req: Request[IO] =>
    OptionT(for {
      _ <- IO(logger.debug(s"Starting request to: ${req.uri.path}"))
      r <- service(req).value
    } yield r)
  }

  /**
    * Serves the webapp resources (html, js, css files), from the /webapp directory on the classpath.
    */
  private lazy val webappRoutes: HttpRoutes[IO] = {
    val rootRoute = indexResponse()
    val resourcesRoutes = resourceServiceBuilder[IO]("/webapp").toRoutes
    rootRoute <+> resourcesRoutes
  }
}
