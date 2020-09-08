package forex.services.ratesCache

import forex.domain.Rate
import forex.services.ratesCache.Protocol.{RedisRecord, pairToKey}
import io.circe.syntax.EncoderOps

object Converters {

  def rateToKeyValue(rate: Rate): (String, String) =
    pairToKey(rate.pair) -> RedisRecord(rate.timestamp, rate.price).asJson.noSpaces

  def convertToRate(pair: Rate.Pair)(redisRecord: RedisRecord): Rate =
    Rate(pair, redisRecord.price, redisRecord.timestamp)
}
