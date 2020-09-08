package forex.services.ratesCache

import cats.implicits.catsSyntaxTuple2Semigroupal
import forex.domain.{ Currency, Rate }
import org.scalacheck.Gen
import org.scalacheck.cats.implicits._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class ProtocolTest extends AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  "Cache keys" should "be serialized and parsed " in {
    val currencyGen = Gen.oneOf(Currency.values)
    val pairGen     = (currencyGen, currencyGen).mapN(Rate.Pair)

    forAll(pairGen) { pair =>
      Protocol.parseRedisKey(Protocol.pairToKey(pair)) mustEqual Right(pair)
    }

  }

}
