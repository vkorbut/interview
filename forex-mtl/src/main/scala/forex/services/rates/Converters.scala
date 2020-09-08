package forex.services.rates

import forex.domain._
import forex.services.rates.Protocol.OneFrameResponse

object Converters {

  private[rates] implicit class OneFrameResponseOpt(val response: OneFrameResponse) extends AnyVal {
    def asRates: Seq[Rate] =
      response.value.map(
        item => Rate(pair = Rate.Pair(item.from, item.to), price = item.price, timestamp = item.time_stamp)
      )
  }
}
