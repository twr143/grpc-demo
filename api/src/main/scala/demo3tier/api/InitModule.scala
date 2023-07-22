package demo3tier.api

import cats.effect.{IO, Resource}
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.SttpBackend
import demo3tier.api.config.ConfigModule
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import sttp.client3.asynchttpclient.fs2.AsyncHttpClientFs2Backend
import io.grpc.ManagedChannel
import fs2.grpc.syntax.all._


/**
  * Initialised resources needed by the application to start.
  */
trait InitModule extends ConfigModule {
  lazy val baseSttpBackend: Resource[IO, SttpBackend[IO, Fs2Streams[IO] with WebSockets]] =
    AsyncHttpClientFs2Backend.resource()

  def managedChannelResource(address: String): Resource[IO, ManagedChannel] =
    NettyChannelBuilder
      .forTarget(address)
      .defaultLoadBalancingPolicy("round_robin")
      .usePlaintext()
      .resource[IO]

}
