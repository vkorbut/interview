package forex.services.rates

import forex.services.rates.errors.Error.CacheError
import forex.services.ratesCache.errors.Error.{ InvalidEntryForKey, InvalidKey }
import forex.services.ratesCache.{ errors => ratesCacheErrors }

object errors {

  sealed trait Error
  object Error {
    final case class OneFrameLookupFailed(msg: String) extends Error
    final case object CacheError extends Error
  }

  def toRatesError(error: ratesCacheErrors.Error): Error = error match {
    case InvalidKey(_)            => CacheError
    case InvalidEntryForKey(_, _) => CacheError
  }
}
