package demo3tier.api.userManager

import cats.effect.IO
import demo3tier.api.http.Http
import grpc.model.userservice.UserManagerFs2Grpc
import io.grpc.{ManagedChannel, Metadata}

trait UMModule {

  val umc : UserManagerFs2Grpc[IO, Metadata]
  def http: Http

  lazy val umApi = new UMApi(http,umc)



}
