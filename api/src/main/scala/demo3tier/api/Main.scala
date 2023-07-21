package demo3tier.api

import cats.effect.{Deferred, ExitCode, IO, IOApp}
import com.typesafe.scalalogging.StrictLogging
import demo3tier.api.infrastructure.CorrelationId
import demo3tier.api.metrics.Metrics
import demo3tier.api.config.Config
import sttp.client3.SttpBackend
import scala.io.StdIn.readLine



object Main extends IOApp with StrictLogging {
  def run(args: scala.List[String]): cats.effect.IO[cats.effect.ExitCode] = {
    CorrelationId.init()
    Metrics.init()
    Thread.setDefaultUncaughtExceptionHandler((t, e) => logger.error("Uncaught exception in thread: " + t, e))

    val initModule = new InitModule {}
    initModule.logConfig()

    initModule.baseSttpBackend
      .use {
        case (_baseSttpBackend) =>
          for {
            signal <- Deferred[IO, Unit]
            _ <- {
              val modules = new MainModule {

                override def baseSttpBackend: SttpBackend[IO, Any] = _baseSttpBackend

                override def config: Config = initModule.config

                override def shutdownSignal: Deferred[IO, Unit] = signal

              }
              modules.httpApi.resource.useForever.as(ExitCode.Success)
            }
          } yield ExitCode.Success
      }

  }
}
