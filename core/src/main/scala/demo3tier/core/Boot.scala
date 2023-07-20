package demo3tier.core

import cats.effect.{Deferred, IO, IOApp, Resource}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import grpc.model.userservice.{ListUserRequest, ListUserResponse, User, UserManagerFs2Grpc}
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.{Metadata, ServerServiceDefinition}
import fs2.grpc.syntax.all._


object Boot extends IOApp with StrictLogging {
  class UserManagerImpl() extends UserManagerFs2Grpc[IO, Metadata] {
    val userList =  Seq(User(1,"Ilya"), User(2,"Malik"))
    override def getUsers(request: ListUserRequest, ctx: Metadata): IO[ListUserResponse] = IO(ListUserResponse(userList))
  }

  def userManagerService(): Resource[IO, ServerServiceDefinition] =
    UserManagerFs2Grpc.bindServiceResource(new UserManagerImpl())


  def runS(port: Int, userManagerService: ServerServiceDefinition) =
    NettyServerBuilder
      .forPort(port)
      .addService(userManagerService)
      .resource[IO]
      .evalMap(server =>
        for {
          s <- IO(server.start())
          _ <- IO(logger.warn("server is running..."))
          d <- Deferred[IO, cats.effect.ExitCode]
        } yield (s,d)
      )
      .useForever

  def run(args: scala.List[String]): cats.effect.IO[cats.effect.ExitCode] = {
    val config: Config = ConfigFactory.load()
    val port = config.getString("port").toInt
    val rs = for {
      ums <- userManagerService()
        } yield (ums)
        rs.use(s => runS(port, s))
    }

}
