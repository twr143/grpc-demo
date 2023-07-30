package demo3tier.api.service
import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import cats.data.Kleisli
import cats.data.NonEmptyList
import cats.effect.IO
import io.circe.{Codec, Encoder}
import io.circe.generic.AutoDerivation
import io.circe.generic.extras.Configuration
import demo3tier.api.util.ServerEndpoints
import demo3tier.api.http.Http
import io.circe.generic.extras.semiauto._
import sttp.model.StatusCode

/**
  * Created by Ilya Volynin on 18.04.2020 at 9:58.
  */
class ServiceApi(http: Http) {
  import http._
  private val UserPath = "user"
  private val SystemPath = "system"
  val shutdownK: Kleisli[IO, Unit, StatusCode] =
    Kleisli { _ =>
      for {
        r <- IO.delay(StatusCode.Accepted)
      } yield r
    }
  private val shutdownEndpoint = baseEndpoint.get
    .in(SystemPath / "shutdown")
    .out(statusCode)
    .serverLogic(shutdownK mapF toOutF run)


  val endpoints: ServerEndpoints =
    NonEmptyList
      .of(
        shutdownEndpoint
      )

}
object ServiceApi extends AutoDerivation {
  case class UserOut(login: String, emailLowerCased: String, createdOn: Instant, id: String)
  implicit val configuration: Configuration = Configuration.default.withDiscriminator("tp")
//  implicit val codecInstant: Codec[Instant] = deriveConfiguredCodec
  val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
  implicit val encodeInstant: Encoder[Instant] =
    Encoder.encodeString.contramap[Instant](i => formatter.format(LocalDateTime.ofInstant(i, ZoneOffset.ofHours(3))))

  implicit val codecUser: Codec[UserOut] = deriveConfiguredCodec

}
