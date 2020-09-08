package forex.services.ratesCache

import cats.Applicative
import cats.effect.{Async, Concurrent, ContextShift, Resource}
import dev.profunktor.redis4cats.RedisCommands
import forex.services.ratesCache.interpreters.RedisCache

object Interpreters {
  def redis[F[_]: Applicative: Async: Concurrent: ContextShift](
      redisClient: Resource[F, RedisCommands[F, String, String]]
  ): Algebra[F] = new RedisCache[F](redisClient)
}
