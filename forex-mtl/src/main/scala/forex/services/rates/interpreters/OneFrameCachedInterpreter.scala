package forex.services.rates.interpreters

import cats._
import cats.data._
import cats.effect.{Clock, Sync, Timer}
import forex.config.ApplicationConfig.maxRateAge
import forex.domain.Rate
import forex.services.{RatesCacheService, RatesService}
import forex.services.rates.Algebra
import forex.services.rates.errors.{Error, toRatesError}

import scala.concurrent.duration.SECONDS

class OneFrameCachedInterpreter[F[_]: Sync: Applicative: Timer](underlying: RatesService[F],
                                                                ratesCacheService: RatesCacheService[F])
    extends Algebra[F] {

  override def get(pairs: Seq[Rate.Pair]): F[Error Either Map[Rate.Pair, Rate]] = {

    val ratesCache: EitherT[F, Error, Map[Rate.Pair, Rate]] =
      EitherT(ratesCacheService.get(pairs)).leftMap(toRatesError)

    (for {
      entries <- ratesCache
      now <- EitherT.right[Error](Clock.create.realTime(SECONDS))
      thresholdTime = now - maxRateAge.toSeconds
      found         = entries.filter(_._2.timestamp.value.toEpochSecond > thresholdTime)
      isUpToDate      = pairs.forall(found.contains)
      result <- if (isUpToDate) EitherT.rightT[F, Error](found) else EitherT(refreshCacheAndGet(pairs))
    } yield result).value
  }

  def refreshCacheAndGet(pairs: Seq[Rate.Pair]): F[Error Either Map[Rate.Pair, Rate]] =
    (for {
      keysInCache <- EitherT(ratesCacheService.getKeys).leftMap(toRatesError)
      newValues <- EitherT(underlying.get((pairs ++ keysInCache).distinct))
      _ <- EitherT(ratesCacheService.put(newValues.values.toSeq)).leftMap(toRatesError)
    } yield newValues).value

}
