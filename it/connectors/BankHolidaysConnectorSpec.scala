package connectors

import org.scalatest.ShouldMatchers
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.test.FakeApplication
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

class BankHolidaysConnectorSpec extends UnitSpec with ShouldMatchers with ScalaFutures with IntegrationPatience {

  "The BankHolidaysConnector" should {
    "fetch the bank holiday dates from the GDS Bank Holiday API" in running(FakeApplication()) {
      val bankHolidays = BankHolidaysConnector.bankHolidaysForEnglandAndWales().futureValue
      bankHolidays.division should be("england-and-wales")
    }
  }


}
