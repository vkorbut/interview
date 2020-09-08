package forex.programs.rates

import cats.data.EitherT
import cats.syntax.either._
import cats.{Applicative, Monad}
import forex.domain._
import forex.programs.rates.errors.Error._
import forex.programs.rates.errors._
import forex.services.RatesService

class Program[F[_]: Applicative: Monad](
    ratesService: RatesService[F]
) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[Error Either Rate] = {
    val pair = Rate.Pair(request.from, request.to)
    EitherT(ratesService.get(Seq(pair)))
      .leftMap(toProgramError)
      .flatMap(_.get(pair).toRight[Error](RateLookupFailed(s"Unknown pair:$pair")).toEitherT[F])
      .value
  }
}

object Program {

  def apply[F[_]: Applicative: Monad](
      ratesService: RatesService[F]
  ): Algebra[F] = new Program[F](ratesService)

}
