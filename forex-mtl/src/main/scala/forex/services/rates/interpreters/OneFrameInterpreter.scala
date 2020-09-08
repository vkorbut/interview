package forex.services.rates.interpreters

import cats._
import cats.implicits._
import cats.effect.{ Resource, Sync }
import forex.config.OneFrameConfiguration
import forex.domain.Rate
import forex.http._
import forex.services.rates.Algebra
import forex.services.rates.Converters._
import forex.services.rates.Protocol.{ OneFrameResponse, _ }
import forex.services.rates.errors.Error
import org.http4s.Method.GET
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl._
import org.http4s.{ Header, Request }

class OneFrameInterpreter[F[_]: Sync: Applicative](client: Resource[F, Client[F]], config: OneFrameConfiguration)
    extends Algebra[F] {

  def get(pairs: Seq[Rate.Pair]): F[Error Either Map[Rate.Pair, Rate]] =
    for {
      request <- getPairsRequest(pairs)
      response <- client.use(_.expect[OneFrameResponse](request))
    } yield {
      response.asRates.groupBy(_.pair).mapValues(_.head).asRight
    }

  private def getPairsRequest(pairs: Seq[Rate.Pair]): F[Request[F]] =
    GET(config.ratesEndpoint +? ("pair", pairs), Header("token", config.token))

}
