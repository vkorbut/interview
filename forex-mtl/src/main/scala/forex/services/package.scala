package forex

package object services {
  type RatesService[F[_]] = rates.Algebra[F]
  final val RatesServices = rates.Interpreters

  type RatesCacheService[F[_]] = ratesCache.Algebra[F]
  final val RatesCacheServices = ratesCache.Interpreters
}
