package demo3tier.api.metrics

import io.prometheus.client.CollectorRegistry
import demo3tier.api.http.Http

trait MetricsModule {
  lazy val metricsApi = new MetricsApi(http, collectorRegistry)
  lazy val versionApi = new VersionApi(http)
  lazy val collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry

  def http: Http
}
