package forex.services.ratesCache

import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.NoOp._
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.RatesCacheServices
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AsyncFeatureSpec
import org.scalatest.flatspec.AsyncFlatSpec

class RedisCacheTest extends RatesCacheAlgebraTest {
  override def prepareInstance: Algebra[IO] = {

    val redisClient = Redis[IO].utf8(s"redis://localhost")

    RatesCacheServices.redis[IO](redisClient)
  }
}

abstract class RatesCacheAlgebraFlatTest extends AsyncFlatSpec with AsyncIOSpec {}

abstract class RatesCacheAlgebraTest extends AsyncFeatureSpec with GivenWhenThen with AsyncIOSpec {

  info("The Rate Cache should save and provide rates for any currency pair along with timestamp value")
  info("It also should store the set of currency pairs which were cached before")

  Feature("Save and get Rate for a pair") {
    Scenario("Save Rate and read it back") {
      Given("Empty instance")
      val instance = prepareInstance

      When("A new value is written and red back")
      val valueToWrite = Rate(
        Rate.Pair(Currency.USD, Currency.EUR),
        Price(Integer.valueOf(11)),
        Timestamp(OffsetDateTime.now.truncatedTo(ChronoUnit.MILLIS))
      )
      val writtenValue = for {
        _ <- instance.put(Seq(valueToWrite))
        result <- instance.get(Seq(valueToWrite.pair))
      } yield result

      Then("The value red successfully")
      writtenValue.asserting(v => assert(v.isRight))
      writtenValue.asserting(v => assert(v.right.get(valueToWrite.pair) == valueToWrite))
    }

    Scenario("Read non-cached value returns empty") {
      Given("Empty instance")
      val instance = prepareInstance

      When("Read a value which is not in cache")
      val valueToRead    = Rate.Pair(Currency.SGD, Currency.NZD)
      val valueFromCache = instance.get(Seq(valueToRead))

      Then("The value red successfully")
      valueFromCache.asserting(v => assert(v.isRight))
      valueFromCache.asserting(v => assert(!v.right.get.contains(valueToRead)))
    }
  }

  Feature("Tracks written keys") {
    Scenario("Save Rate and inspect written keys") {
      Given("Cache contains values")
      val instance = prepareInstance
      val valuesToWrite = Seq(
        Rate(
          Rate.Pair(Currency.USD, Currency.EUR),
          Price(Integer.valueOf(11)),
          Timestamp(OffsetDateTime.now.truncatedTo(ChronoUnit.MILLIS))
        ),
        Rate(
          Rate.Pair(Currency.EUR, Currency.USD),
          Price(Integer.valueOf(11)),
          Timestamp(OffsetDateTime.now.truncatedTo(ChronoUnit.MILLIS))
        )
      )

      val writtenIO = instance.put(valuesToWrite)

      When("Query keys")
      val keys = writtenIO *> instance.getKeys

      Then("Keys of all cached values returned")
      keys.asserting(v => assert(v.isRight))
      keys.asserting(v => assert(valuesToWrite.map(_.pair).forall(v.right.get.contains(_))))
    }
  }

  def prepareInstance: Algebra[IO]
}
