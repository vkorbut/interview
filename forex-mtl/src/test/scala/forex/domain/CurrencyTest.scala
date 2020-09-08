package forex.domain

import cats.implicits.toShow
import org.scalacheck.Gen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class CurrencyTest extends AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  "Currency" should "translate to text and back" in {
    val genCurrency: Gen[Currency] = Gen.oneOf(Currency.values)

    forAll(genCurrency)(currency =>
      Currency.fromString(currency.show) must contain (currency)
    )
  }

  "Currency.show" should s"have length == Currency.CURRENCY_CODE_LEN" in {
    val genCurrency: Gen[Currency] = Gen.oneOf(Currency.values)

    forAll(genCurrency)(currency =>
      currency.show must have size(Currency.CURRENCY_CODE_LEN)
    )
  }
}
