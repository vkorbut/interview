package forex.services.ratesCache

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import cats.implicits._
import forex.domain.Currency.CURRENCY_CODE_LEN
import forex.domain.Rate.Pair
import forex.domain.{Currency, Price, Timestamp}
import forex.services.ratesCache.errors.Error
import forex.services.ratesCache.errors.Error.InvalidKey
import io.circe
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder, ObjectEncoder, parser}

import scala.util.control.NonFatal

object Protocol {

  final case class RedisRecord(timestamp: Timestamp, price: Price)

  implicit val priceDecoder: Decoder[Price] = Decoder[String].map(BigDecimal(_)).map(Price.apply)
  implicit val priceEncoder: Encoder[Price] = Encoder[String].contramap[Price](_.value.show)

  implicit val redisRecordEncoder: ObjectEncoder[RedisRecord] = deriveEncoder[RedisRecord]
  implicit val redisRecordDecoder: Decoder[RedisRecord]       = deriveDecoder[RedisRecord]

  implicit val currencyDecoder: Decoder[Currency] =
    Decoder[String].emap(c => Currency.fromString(c).toRight(s"cannot parse currency $c"))

  val redisDateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")

  implicit val timestampDecoder: Decoder[Timestamp] = Decoder.decodeString.emap { s =>
    try {
      Right(Timestamp(OffsetDateTime.from(redisDateTimeFormat.parse(s))))
    } catch {
      case NonFatal(e) => Left(e.getMessage)
    }
  }

  implicit val timestampEncoder: Encoder[Timestamp] = Encoder.encodeString.contramap[Timestamp] { s =>
    redisDateTimeFormat.format(s.value)
  }

  val KEY_LENGTH: Int = CURRENCY_CODE_LEN * 2

  def pairToKey(pair: Pair): String =
    pair.show.ensuring(_.length == KEY_LENGTH)

  def parseRedisKey(key: String): Either[Error, Pair] =
    if (key.length != KEY_LENGTH)
      Left(InvalidKey(key))
    else {
      val (fromStr, toStr) = key.splitAt(CURRENCY_CODE_LEN)

      val parseCurrency = Currency.fromString _ andThen (_.toRight(InvalidKey(key)))

      for {
        from <- parseCurrency(fromStr)
        to <- parseCurrency(toStr)
      } yield Pair(from, to)
    }

  def parseRedisValue(value: String): Either[circe.Error, RedisRecord] =
    parser
      .parse(value)
      .right
      .flatMap(_.as[RedisRecord])
}
