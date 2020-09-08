package forex.http.rates

import cats.Show
import forex.http.rates.errors.Error.{InvalidCurrency, RateLookupFailed}
import forex.programs.rates.errors.{Error => ProgramError}

object errors {

  sealed trait Error extends Exception

  object Error {
    final case class InvalidCurrency(value: String) extends Error
    final case class RateLookupFailed(msg: String) extends Error
  }

  def toHttpError(programError: ProgramError): Error =
    programError match {
      case ProgramError.RateLookupFailed(msg) => RateLookupFailed(msg)
    }

  implicit val showError: Show[Error] = Show.show {
    case InvalidCurrency(value)  => s"Invalid currency: $value"
    case RateLookupFailed(value) => s"Rate lookup failed: $value"
  }
}
