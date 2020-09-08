package forex.http.rates

import forex.domain.Currency
import forex.http.rates.errors.Error
import forex.http.rates.errors.Error._
import org.http4s.QueryParamDecoder
import org.http4s.dsl.impl.QueryParamDecoderMatcher

object QueryParams {

  private[http] implicit val currencyQueryParam: QueryParamDecoder[Either[Error, Currency]] =
    QueryParamDecoder[String].map(param => Currency.fromString(param).toRight(InvalidCurrency(param)))

  object FromQueryParam extends QueryParamDecoderMatcher[Either[Error, Currency]]("from")
  object ToQueryParam extends QueryParamDecoderMatcher[Either[Error, Currency]]("to")

}
