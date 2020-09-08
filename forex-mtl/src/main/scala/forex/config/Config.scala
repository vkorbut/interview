package forex.config

import cats.effect.Sync
import fs2.Stream
import org.http4s.Uri
import pureconfig.error.CannotConvert
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.auto._

object Config {

  implicit val config: ConfigReader[Uri] = ConfigReader.fromNonEmptyString(
    value =>
      Uri
        .fromString(value)
        .toTry
        .toEither
        .left
        .map(err => CannotConvert(value, Uri.getClass.getSimpleName, err.getMessage))
  )
  /**
   * @param path the property path inside the default configuration
   */
  def stream[F[_]: Sync](path: String): Stream[F, ApplicationConfig] = {
    Stream.eval(Sync[F].delay(
      ConfigSource.default.at(path).loadOrThrow[ApplicationConfig]))
  }

}
