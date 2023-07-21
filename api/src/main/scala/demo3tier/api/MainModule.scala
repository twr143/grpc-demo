package demo3tier.api

import java.time.Clock
import cats.data.NonEmptyList
import demo3tier.api.http.{Http, HttpApi}
import demo3tier.api.infrastructure.InfrastructureModule
import demo3tier.api.metrics.MetricsModule
import demo3tier.api.service.ServiceModule
import demo3tier.api.util.{BaseModule, DefaultIdGenerator, IdGenerator, ServerEndpoints}


/**
  * Main application module. Depends on resources initalised in [[InitModule]].
  */
trait MainModule
    extends BaseModule
      with MetricsModule
      with ServiceModule
    with InfrastructureModule
{

  override lazy val idGenerator: IdGenerator = DefaultIdGenerator
  override lazy val clock: Clock = Clock.systemUTC()

  lazy val http: Http = new Http()

  private lazy val adminEndpoints: ServerEndpoints = NonEmptyList.of(metricsApi.metricsEndpoint, versionApi.versionEndpoint)
  private lazy val endpoints
      : ServerEndpoints = adminEndpoints//userApi.endpoints concatNel passwordResetApi.endpoints concatNel fsApi.endpoints concatNel mfApi.endpoints concatNel lvApi.endpoints

  lazy val httpApi: HttpApi = new HttpApi(http, endpoints, adminEndpoints, serviceApi.endpoints, collectorRegistry, config.api)

}
