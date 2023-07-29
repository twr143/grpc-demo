package demo3tier.api

import cats.effect.std.Dispatcher
import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import com.typesafe.scalalogging.StrictLogging
import demo3tier.api.infrastructure.CorrelationId
import demo3tier.api.metrics.Metrics
import demo3tier.api.config.Config
import grpc.model.userservice.UserManagerFs2Grpc
import io.grpc.Metadata
import sttp.client3.SttpBackend



object Main extends IOApp with StrictLogging {
  def run(args: scala.List[String]): cats.effect.IO[cats.effect.ExitCode] = {
    CorrelationId.init()
    Metrics.init()
    Thread.setDefaultUncaughtExceptionHandler((t, e) => logger.error("Uncaught exception in thread: " + t, e))

    val initModule = new InitModule {}
    initModule.logConfig()

    (initModule.baseSttpBackend, initModule.managedChannelResource(initModule.config.core.address), Dispatcher.parallel[IO])
      .mapN((_,_,_))
      .use {
        case (_baseSttpBackend, netty, dispatcher) =>

              val modules = new MainModule {

                override def baseSttpBackend: SttpBackend[IO, Any] = _baseSttpBackend

                override def config: Config = initModule.config

                override val umc: UserManagerFs2Grpc[IO, Metadata] = UserManagerFs2Grpc.stub[IO](dispatcher,netty)
              }
              modules.httpApi.resource.useForever

      }

  }
}
