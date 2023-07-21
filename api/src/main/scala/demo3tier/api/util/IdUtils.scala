package demo3tier.api.util
import cats.data.Kleisli
import cats.effect.IO

/**
  * Created by Ilya Volynin on 08.03.2020 at 14:16.
  */
object IdUtils {
  def IdToProductK: Kleisli[IO, Id, Product] = Kleisli(id => IO(new Tuple1[Id](id)))
}
