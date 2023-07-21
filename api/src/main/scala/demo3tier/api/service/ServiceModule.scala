package demo3tier.api.service
import cats.effect.{Deferred, IO}
import demo3tier.api.http.Http
import demo3tier.api.util.BaseModule

/**
  * Created by Ilya Volynin on 18.04.2020 at 9:57.
  */
trait ServiceModule extends BaseModule {

  lazy val serviceModel = new ServiceModel
  lazy val serviceApi = new ServiceApi(http, serviceService, shutdownSignal)
  lazy val serviceService = new ServiceService(serviceModel)

  def http: Http
  def shutdownSignal: Deferred[IO, Unit]
}
