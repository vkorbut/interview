package forex.services.rates

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Resource}
import forex.config.OneFrameConfiguration
import forex.domain.Currency._
import forex.domain.Rate
import forex.services.RatesServices
import forex.services.rates.OneFrameDirectTest.testConfig
import org.http4s.Http4sLiteralSyntax
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AsyncFeatureSpec

class OneFrameDirectTest extends RatesAlgebraTest {
  override def prepareInstance: Algebra[IO] = {

    val client: Resource[IO, Client[IO]] = BlazeClientBuilder[IO](scala.concurrent.ExecutionContext.global).resource

    RatesServices.oneFrameDirect(
      client,
      testConfig
    )
  }
}

object OneFrameDirectTest {
  val testConfig = OneFrameConfiguration(uri"http://localhost:8080/rates", "10dc303535874aeccc86a8251e6992f5")
}

abstract class RatesAlgebraTest extends AsyncFeatureSpec with GivenWhenThen with AsyncIOSpec {

  info("The Rate service should provide rates for any currency pair(s) along with timestamp value")

  Feature("Get rate for a pair") {
    Scenario("Query rate for a single pair") {
      Given("Empty instance")
      val instance = prepareInstance

      When("A rate value queried")
      val pairToQuery = Rate.Pair(USD, EUR)
      val value       = instance.get(Seq(pairToQuery))

      Then("The value red successfully")
      value.asserting {
        case Right(value) => assert(value.contains(pairToQuery) && value(pairToQuery).pair == pairToQuery)
        case Left(_)      => fail()
      }
    }

    Scenario("Query rate for a multiple pairs") {
      Given("Empty instance")
      val instance = prepareInstance

      When("A multiple rates queried")
      val pairsToQuery = Seq(
        Rate.Pair(USD, EUR),
        Rate.Pair(EUR, USD),
        Rate.Pair(JPY, GBP)
      )
      val value = instance.get(pairsToQuery)

      Then("The value red successfully")
      value.asserting {
        case Right(value) => assert(pairsToQuery.forall(value.contains))
        case Left(_)      => fail()
      }
    }
  }

  def prepareInstance: Algebra[IO]
}
