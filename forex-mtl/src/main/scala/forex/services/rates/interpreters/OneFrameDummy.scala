package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.{ Price, Rate, Timestamp }
import forex.services.rates.errors._

class OneFrameDummy[F[_]: Applicative] extends Algebra[F] {

  override def get(pairs: Seq[Rate.Pair]): F[Error Either Map[Rate.Pair, Rate]] =
    pairs.map(p => p -> Rate(p, Price(BigDecimal(100)), Timestamp.now)).toMap.asRight[Error].pure[F]

}
