package forex.config

import org.http4s.Uri

import scala.concurrent.duration.{ DurationInt, FiniteDuration }

case class ApplicationConfig(
    http: HttpConfig,
    oneFrame: OneFrameConfiguration,
    redisHost: String
)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)

case class OneFrameConfiguration(
    ratesEndpoint: Uri,
    token: String
)

object ApplicationConfig {
  val maxRateAge: FiniteDuration = 5.minutes
}
