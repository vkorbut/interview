package forex.services.rates

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import cats.implicits.toShow
import forex.domain.Rate.Pair
import forex.domain.{Currency, Price, Timestamp}
import io.circe.Decoder
import io.circe.generic.semiauto._
import org.http4s.QueryParamEncoder

import scala.util.control.NonFatal

object Protocol {

  final case class OneFrameRequest(pairs: Seq[Pair])

  final case class OneFrameDataItem(
      from: Currency,
      to: Currency,
      price: Price,
      time_stamp: Timestamp
  )

  /*
  TODO: handle error case
  final case class OneFrameErrorItem(
                                      error: String
                                    ) extends OneFrameResponseItem
   */
  final case class OneFrameResponse(value: Seq[OneFrameDataItem])

  implicit val currencyDecoder: Decoder[Currency] = Decoder[String].map(Currency.fromString).map(_.get) //TODO: unsafe
  implicit val priceDecoder: Decoder[Price]       = Decoder[Double].map(BigDecimal(_)).map(Price.apply)

  val oneFrameDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")

  implicit val timestampDecoder: Decoder[Timestamp] = Decoder.decodeString.emap { s =>
    try {
      Right(Timestamp(OffsetDateTime.from(oneFrameDateFormat.parse(s))))
    } catch {
      case NonFatal(e) => Left(e.getMessage)
    }
  }

  implicit val dataItemDecoder: Decoder[OneFrameDataItem] = deriveDecoder[OneFrameDataItem]

  implicit val responseDecoder: Decoder[OneFrameResponse] =
    implicitly[Decoder[Seq[OneFrameDataItem]]].map(OneFrameResponse)

  implicit val pairEncoder: QueryParamEncoder[Pair] = QueryParamEncoder[String].contramap[Pair](_.show)

}
