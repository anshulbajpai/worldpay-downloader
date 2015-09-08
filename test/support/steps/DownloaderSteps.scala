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

package support.steps

import java.io.File
import java.nio.file.Files._

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import cucumber.api.PendingException
import cucumber.api.scala.{EN, ScalaDsl}
import org.joda.time.{DateTime, LocalDate}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{OptionValues, ShouldMatchers}

class DownloaderSteps extends ScalaDsl with EN with ScalaFutures with OptionValues with ShouldMatchers with IntegrationPatience with WSJsonClient{

  val reportContents = """00#####################0000001#######################################################################################################################
                         |05########################################################################################################################################
                         |10########0000011010000140411#######################################00000001234###########################################0000001##############
                         |15##############******####   ####00000001234091214######0#######A                            ######
                         |16############################K1097172564K-G69GBH0""".stripMargin

  Given("""^the application is running$""") { () =>
    // nothing
  }

  Given("""^a Report "(.*?)" is available for download on WorldPay containing$"""){ (reportName:String, reportContents:String) =>
    write(new File(Env.sftpRoot, reportName).toPath, reportContents.stripMargin.getBytes)
  }

   Given("""^an empty Report "(.*?)" is available for download on WorldPay"""){ (reportName:String) =>
    write(new File(Env.sftpRoot, reportName).toPath, "".getBytes)
  }

  Given("""^I have valid private and public key to the WorldPay SFTP site$"""){ () =>
  }

  When("""^I trigger the download for the date "(\d{4})/(\d{2})/(\d{2})"$"""){ (year: Int, month: Int, day: Int) =>
    val formattedDate = new LocalDate(year, month, day).toString("yyyyMMdd")
    postEmptyBody(s"/download/$formattedDate").futureValue
  }

  When("""^I trigger the download with chunk size "(.*)" for the date "(.*?)"$"""){ (chunkSize:String, reconciliationDate:String) =>
    val (year, month, day) = splitDate(reconciliationDate)
    postEmptyBody(s"/download/$year$month$day?chunkSize=$chunkSize").futureValue.status shouldBe 202
  }

  When("""^I force another download for today"""){ () =>
    val formattedDate = DateTime.now.toString("yyyyMMdd")
    postEmptyBody(s"/download/$formattedDate?force=true").futureValue.status shouldBe 202
  }


  Given("""^an EMIS report is available for today's date$"""){ () =>
    writeReportFor(LocalDate.now())
  }

  Given("""^an EMIS report is available for the date "(\d{4})/(\d{2})/(\d{2})"$"""){ (year: Int, month: Int, day: Int) =>
    writeReportFor(new LocalDate(year, month, day))
  }

  private def writeReportFor(date: LocalDate) = {
    val dateFormatted = date.toString("ddMMyy")
    val reportName = s"MA.PISCESSW.#M.RECON.HMRE.D$dateFormatted"
    write(new File(Env.sftpRoot, reportName).toPath, reportContents.getBytes)
  }

  Given("""^the RCS system is available$""") { () =>
    stubFor(get(urlMatching(s"/payments/worldpay\\?fromDate=.*&toDate=.*"))
      .willReturn(aResponse().withStatus(404)))
    stubFor(post(urlMatching("/payments/worldpay/.*"))
      .willReturn(aResponse().withStatus(200)))
    stubFor(post(urlMatching("/payments/worldpay/.*/chunks/1"))
      .willReturn(aResponse().withStatus(202)))
    stubFor(post(urlMatching("/payments/worldpay/.*/release"))
      .willReturn(aResponse().withStatus(200)))
  }

  When("""^I trigger the download for today$"""){ () =>
    val dateFormatted = LocalDate.now().toString("yyyyMMdd")
    postEmptyBody(s"/download/$dateFormatted").futureValue
  }

  Then("""^the RCS system receives a report$"""){ () =>
    WireMock.verify(
      postRequestedFor(urlMatching("/payments/worldpay/.*")))
  }

  private def splitDate(date: String) = {
    val dateSplitted = date.split("/")
    (dateSplitted(0), dateSplitted(1), dateSplitted(2))
  }

}
