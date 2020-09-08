package forex.services.rates

import forex.domain.Rate
import errors._

trait Algebra[F[_]] {
  def get(pair: Seq[Rate.Pair]): F[Error Either Map[Rate.Pair, Rate]]
}
