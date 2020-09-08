package forex

import cats.Applicative
import cats.effect.{ConcurrentEffect, ContextShift, Resource, Timer}
import dev.profunktor.redis4cats.effect.Log
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}

class Module[F[_]: ConcurrentEffect: Timer: Applicative: ContextShift: Log](config: ApplicationConfig) {

  //TODO: is it OK using `global` EC here
  val client: Resource[F, Client[F]] =
    BlazeClientBuilder[F](scala.concurrent.ExecutionContext.global).resource
  val redisClient: Resource[F, RedisCommands[F, String, String]] = Redis[F].utf8(s"redis://${config.redisHost}")

  private val redisCache: RatesCacheService[F]    = RatesCacheServices.redis(redisClient)
  private val ratesService: RatesService[F]       = RatesServices.oneFrameDirect[F](client, config.oneFrame)
  private val ratesCachedService: RatesService[F] = RatesServices.cached[F](ratesService, redisCache)

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](ratesCachedService)

  private val ratesHttpRoutes: HttpRoutes[F] = new RatesHttpRoutes[F](ratesProgram).routes

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware   = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    }
  }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[F] =>
    Timeout(config.http.timeout)(http)
  }

  private val http: HttpRoutes[F] = ratesHttpRoutes

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

}
