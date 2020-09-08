package forex.services.ratesCache

import forex.domain.Rate
import forex.services.ratesCache.errors._

trait Algebra[F[_]] {
  def getKeys: F[Error Either Seq[Rate.Pair]]

  def get(pairs: Seq[Rate.Pair]): F[Error Either Map[Rate.Pair, Rate]]
  def put(pairs: Seq[Rate]): F[Error Either Unit]
}
