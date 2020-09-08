package forex.http
package rates

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits.{ catsSyntaxApplicativeId, toShow }
import cats.syntax.flatMap._
import forex.http.rates.errors.Error.{ InvalidCurrency, RateLookupFailed }
import forex.http.rates.errors.toHttpError
import forex.programs.RatesProgram
import forex.programs.rates.{ Protocol => RatesProgramProtocol }
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{ HttpRoutes, Request, Response }

class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._
  import Protocol._
  import QueryParams._

  private[http] val prefixPath = "/rates"

  private val pf: PartialFunction[Request[F], F[Either[errors.Error, Response[F]]]] = {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      (for {
        request <- EitherT(from.flatMap(f => to.map(RatesProgramProtocol.GetRatesRequest(f, _))).pure[F])
        rate <- EitherT(rates.get(request)).leftMap(e => toHttpError(e))
        response <- EitherT.right[errors.Error](Ok(rate.asGetApiResponse))
      } yield response).value
  }

  def handleErrors(e: errors.Error): F[Response[F]] =
    e match {
      case _: InvalidCurrency  => BadRequest(e.show)
      case _: RateLookupFailed => BadGateway(e.show)
    }

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F](pf.andThen(_.flatMap(_.fold(handleErrors, _.pure[F]))))

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
