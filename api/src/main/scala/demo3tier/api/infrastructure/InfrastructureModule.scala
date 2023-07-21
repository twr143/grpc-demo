package demo3tier.api.infrastructure

import cats.effect.IO
import sttp.client3.prometheus.PrometheusBackend
import sttp.client3.SttpBackend
import com.typesafe.scalalogging.StrictLogging
import sttp.client3.logging.slf4j.Slf4jLoggingBackend

trait InfrastructureModule extends StrictLogging {
  lazy val sttpBackend: SttpBackend[IO, Any] = Slf4jLoggingBackend(
    SetCorrelationIdBackend(PrometheusBackend(baseSttpBackend))
  )

  def baseSttpBackend: SttpBackend[IO, Any]
}
