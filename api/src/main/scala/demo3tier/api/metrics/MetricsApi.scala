package demo3tier.api.metrics

import java.io.StringWriter
import cats.data.Kleisli
import cats.effect.IO
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import sttp.tapir.server.ServerEndpoint
import demo3tier.api.http.Http

/**
  * Defines an endpoint which exposes the current state of the metrics, which can be later read by a Prometheus server.
  */
class MetricsApi(http: Http, registry: CollectorRegistry) {
  import http._

  val metricsK: Kleisli[IO, Unit, String] = Kleisli { _ =>
    IO {
      val writer = new StringWriter
      TextFormat.write004(writer, registry.metricFamilySamples)
      writer.toString
    }
  }
  val metricsEndpoint: ServerEndpoint[Any, IO] = baseEndpoint.get
    .in("metrics")
    .out(stringBody)
    .serverLogic(metricsK mapF toOutF run)
}
