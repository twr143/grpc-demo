package demo3tier.api.userManager

import cats.data.{Kleisli, NonEmptyList}
import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import demo3tier.api.http.Http
import demo3tier.api.util.ServerEndpoints
import grpc.model.userservice.{ListUserRequest, User, UserManagerFs2Grpc}
import io.circe.generic.AutoDerivation
import io.circe.generic.extras.Configuration
import io.grpc.Metadata
import sttp.tapir.generic.auto._


class UMApi(h: Http, ums: UserManagerFs2Grpc[IO, Metadata]) extends StrictLogging {

  import UMApi._
  import h._

  val getUsersK: Kleisli[IO, Unit, GetUsers_OUT] =
    Kleisli { _ =>
      ums.getUsers(ListUserRequest(), new Metadata()).map(r => GetUsers_OUT(r.users))
    }
  private val getUsersEndpoint = baseEndpoint.get
    .in("users")
    .out(jsonBody[GetUsers_OUT])
    .serverLogic(getUsersK mapF toOutF run)

  val endpoints: ServerEndpoints =
    NonEmptyList
      .of(
        getUsersEndpoint
      )


}

object UMApi extends AutoDerivation {
  case class GetUsers_IN(name: String)

  case class GetUsers_OUT(users: Seq[User])

  implicit val configuration: Configuration = Configuration.default.withDiscriminator("tp")


}
