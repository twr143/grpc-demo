package demo3tier.api.util

import cats.effect.IO
import io.circe.Decoder
import sttp.client3.Response
import io.circe.parser.decode
import com.typesafe.scalalogging.StrictLogging
import demo3tier.api.Fail
import demo3tier.api.Fail._

/**
  * Created by Ilya Volynin on 16.12.2019 at 15:21.
  */
object SttpUtils extends StrictLogging {

  def handleRemoteResponse[R](implicit dec: Decoder[R]): Response[Either[String, String]] => IO[R] = { res =>
    if (res.code.isSuccess) {
      res.body.fold(
        fa => {
          logger.error(s"respose code success {}, but body {}", res.code.toString(), res.body)
          IO.raiseError(NotFound(fa))
        },
        fb =>
          decode[R](fb).fold(error => {
            logger.error(s"error decoding {}", error)
            IO.raiseError(Fail.IncorrectInput(fb))
          }, l => IO(l))
      )
    } else {
      logger.error(s"unsuccessfull response, code {}, body ", res.code, res.body)
      IO.raiseError(Fail.IncorrectInput(s"unsuccessfull response ${res.code}"))
    }
  }
}
