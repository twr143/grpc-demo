package demo3tier.api.http

import cats.effect.IO
import cats.implicits._
import demo3tier.api.util.Id
import com.softwaremill.tagging._
import com.typesafe.scalalogging.StrictLogging
import io.circe.Printer
import sttp.client3.SttpClientException._
import sttp.model.StatusCode
import sttp.tapir.Codec.PlainCodec
import sttp.tapir.json.circe.TapirJsonCirce
import sttp.tapir.{ EndpointOutput, PublicEndpoint, Schema, SchemaType, Tapir}
import demo3tier.api.Fail
import demo3tier.api.infrastructure.Json._
import sttp.tapir.generic.auto._

import scala.util.control.NonFatal

/**
  * Helper class for defining HTTP endpoints. Import the members of this class when defining an HTTP API using tapir.
  */
class Http() extends Tapir with TapirJsonCirce with TapirSchemas with StrictLogging {

  /**
    * Description of the output, that is used to represent an error that occurred during endpoint invocation.
    */
  val failOutput
      : EndpointOutput[(StatusCode, Error_OUT)] = statusCode and jsonBody[Error_OUT] //.map(identity)(a => Error_OUT(a.error + " 123"))

  /**
    * Base endpoint description for non-secured endpoints. Specifies that errors are always returned as JSON values
    * corresponding to the [[Error_OUT]] class.
    */
  val baseEndpoint: PublicEndpoint[Unit, (StatusCode, Error_OUT), Unit, Any] =
    endpoint.errorOut(failOutput)

  /**
    * Base endpoint description for secured endpoints. Specifies that errors are always returned as JSON values
    * corresponding to the [[Error_OUT]] class, and that authentication is read from the `Authorization: Bearer` header.
    */
  val secureEndpoint: PublicEndpoint[Id, (StatusCode, Error_OUT), Unit, Any] =
    baseEndpoint.in(auth.bearer[String]().map(_.asInstanceOf[Id])(identity))
  //
  private val InternalServerError = (StatusCode.InternalServerError, List("Internal server error"))

  private val failToResponseData: Fail => (StatusCode, List[String]) = {
    case Fail.NotFound(what)        => (StatusCode.NotFound, List(what))
    case Fail.PayloadTooLarge(what) => (StatusCode.PayloadTooLarge, List(what))
    case Fail.Conflict(msg)         => (StatusCode.Conflict, List(msg))
    case Fail.IncorrectInput(msg)   => (StatusCode.BadRequest, List(msg))
    case Fail.IncorrectInputL(lmsg) => (StatusCode.BadRequest, lmsg)
    case Fail.Forbidden             => (StatusCode.Forbidden, List("Forbidden"))
    case Fail.Unauthorized          => (StatusCode.Unauthorized, List("Unauthorized"))
    case Fail.UnauthorizedM(msg)    => (StatusCode.Unauthorized, List(msg))
    case _                          => InternalServerError
  }
//
  def exceptionToErrorOut(e: Throwable): (StatusCode, Error_OUT) = {
    val (statusCode, message) = e match {
      case f: Fail              => failToResponseData(f)
      case re: ReadException    => (StatusCode.RequestTimeout, List(s"read ${re.getMessage}"))
      case ce: ConnectException => (StatusCode.RequestTimeout, List(s"connect ${ce.getMessage}"))
      case _ =>
        logger.error("Exception when processing request", e)
        InternalServerError
    }
    val errorOut = Error_OUT(message)
    (statusCode, errorOut)
  }

  def toOutF[T]: IO[T] => IO[Either[(StatusCode, Error_OUT), T]] = { f =>
    f.map(t => t.asRight[(StatusCode, Error_OUT)]).recover {
      case NonFatal(e) => exceptionToErrorOut(e).asLeft[T]
    }
  }

  override def jsonPrinter: Printer = noNullsPrinter
}

/**
  * Schemas for custom types used in endpoint descriptions (as parts of query parameters, JSON bodies, etc.)
  */
trait TapirSchemas {

  implicit def taggedPlainCodec[U, T](implicit uc: PlainCodec[U]): PlainCodec[U @@ T] =
    uc.map(_.taggedWith[T])(identity)

  implicit val schemaForId: Schema[Id] = Schema(SchemaType.SString[Id]())

  implicit def schemaForTagged[U, T](implicit uc: Schema[U]): Schema[U @@ T] = uc.asInstanceOf[Schema[U @@ T]]
}

case class Error_OUT(error: List[String])
