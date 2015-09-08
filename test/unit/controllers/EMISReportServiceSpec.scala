/*
 * Copyright 2015 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package unit.controllers

import com.codahale.metrics.MetricRegistry
import config.WSHttp
import connectors.HodsApiConnector
import controllers.{AllowSubmission, EMISReportService, PreventSubmission, UnknownStatus}
import model.RcsDataSet
import org.joda.time.LocalDate
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.test.{FakeApplication, FakeHeaders, WithApplication}
import uk.gov.hmrc.play.audit.http.HeaderCarrier
import uk.gov.hmrc.play.http.{HttpDelete, HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec
import unit.{SftpConfiguration, WorldpayMerchantConfiguration}

import scala.concurrent.Future


class EMISReportServiceSpec extends UnitSpec with ScalaFutures with SftpConfiguration with WorldpayMerchantConfiguration with IntegrationPatience {

  val allMandatoryConfig = MandatorySftpConfig ++ MandatoryMerchantConfig


  "The EMISReportService" should {

    implicit def hc = HeaderCarrier.fromHeadersAndSession(FakeHeaders())

    "allow a re-submission if the existing datasets are not released or unknown status" in new WithApplication(FakeApplication(additionalConfiguration = allMandatoryConfig)){
      val connector = hodsApiConnector(Seq(RcsDataSet("1", "Deleted"), RcsDataSet("2", "Open")))

      EMISReportService.canSubmitDataset(connector)(LocalDate.now()).futureValue shouldBe AllowSubmission

    }
    "prevent a re-submission if there is an existing dataset already released" in {
      Seq("Released", "Sending", "Sent", "Failed Recon", "Tech Failure") foreach { preventStatus =>
        val connector = hodsApiConnector(Seq(RcsDataSet("1", preventStatus), RcsDataSet("2", "Open")))
        EMISReportService.canSubmitDataset(connector)(LocalDate.now()).futureValue shouldBe PreventSubmission
      }
    }

    "prevent a re-submission if any of the status is unknown" in {
      val connector = hodsApiConnector(Seq(RcsDataSet("1", "Surprise"), RcsDataSet("2", "Open")))

      EMISReportService.canSubmitDataset(connector)(LocalDate.now()).futureValue shouldBe UnknownStatus(RcsDataSet("1", "Surprise"))
    }
  }

  def hodsApiConnector(datasetResults:Seq[RcsDataSet]): HodsApiConnector = new HodsApiConnector {
    override val rcsPaymentsUrl: String = ""
    override val http: HttpPost with HttpGet with HttpDelete = WSHttp
    override val metricsRegistry: MetricRegistry = new MetricRegistry

    override def listDataSets(fromDate: LocalDate, toDate: LocalDate, hc: HeaderCarrier): Future[Seq[RcsDataSet]] = {
      Future.successful(datasetResults)
    }

    override def deleteDataset(datasetId: String, hc: HeaderCarrier): Future[HttpResponse] = {
      Future.successful(HttpResponse(200))
    }
  }

}
