package forex.services.ratesCache

import forex.services.ratesCache.errors.Error.InvalidEntryForKey
import io.circe.{Error => CirceError}

object errors {

  sealed trait Error

  object Error {

    final case class InvalidKey(key: String) extends Error
    final case class InvalidEntryForKey(key: String, message: String) extends Error
  }

  def toContentParsingError(key: String)(p: CirceError): InvalidEntryForKey =
    InvalidEntryForKey(key, p.getMessage)

}
