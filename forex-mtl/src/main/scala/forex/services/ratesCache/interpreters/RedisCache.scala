package forex.services.ratesCache.interpreters

import cats.Applicative
import cats.effect.{ Async, Concurrent, ContextShift, Resource }
import cats.implicits._
import dev.profunktor.redis4cats.RedisCommands
import forex.domain.Rate
import forex.services.ratesCache.Algebra
import forex.services.ratesCache.Converters._
import forex.services.ratesCache.Protocol._
import forex.services.ratesCache.errors._
import forex.services.ratesCache.interpreters.RedisCache.PAIRS_SET_NAME

class RedisCache[F[_]: Applicative: Async: Concurrent: ContextShift](
    redisClient: Resource[F, RedisCommands[F, String, String]]
) extends Algebra[F] {

  override def get(pairs: Seq[Rate.Pair]): F[Error Either Map[Rate.Pair, Rate]] = redisClient.use { client =>
    val keys: Seq[String] = pairs.map(pairToKey)
    client
      .mGet(keys.toSet)
      .map(
        _.toList
          .traverse {
            case (key, value) =>
              for {
                pair <- parseRedisKey(key)
                value <- parseRedisValue(value).leftMap(toContentParsingError(key))
              } yield pair -> convertToRate(pair)(value)
          }
          .map(_.toMap)
      )

  }

  override def getKeys: F[Either[Error, Seq[Rate.Pair]]] =
    redisClient.use(_.sMembers(PAIRS_SET_NAME).handleError(_ => Set.empty).map(_.toList.traverse(parseRedisKey)))

  override def put(pairs: Seq[Rate]): F[Error Either Unit] =
    redisClient.use { client =>
      val keyValues = pairs.map(rateToKeyValue).toMap

      client.sAdd(PAIRS_SET_NAME, keyValues.keySet.toSeq: _*) *> client.mSet(keyValues).map(Right(_))
    }
}

object RedisCache {
  val PAIRS_SET_NAME = "pairs"
}
