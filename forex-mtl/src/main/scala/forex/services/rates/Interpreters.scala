package forex.services.rates

import cats.Applicative
import cats.effect.{ Resource, Sync, Timer }
import forex.config.OneFrameConfiguration
import forex.services.{ RatesCacheService, RatesService }
import interpreters._
import org.http4s.client.Client

object Interpreters {
  def dummy[F[_]: Applicative](): Algebra[F] = new OneFrameDummy[F]()

  def oneFrameDirect[F[_]: Applicative: Sync](client: Resource[F, Client[F]],
                                              config: OneFrameConfiguration): Algebra[F] =
    new OneFrameInterpreter[F](client, config)

  def cached[F[_]: Applicative: Sync: Timer](service: RatesService[F], cache: RatesCacheService[F]): Algebra[F] =
    new OneFrameCachedInterpreter[F](service, cache)
}
