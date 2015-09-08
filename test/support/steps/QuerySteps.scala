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


import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import cucumber.api.PendingException
import cucumber.api.scala.{EN, ScalaDsl}
import org.joda.time.{LocalDate, DateTime}
import org.joda.time.format.DateTimeFormat
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.libs.ws.WSResponse

import scala.util.Try

class QuerySteps extends ScalaDsl with EN with WSJsonClient with ScalaFutures with IntegrationPatience {

  val dateTimeFormat = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm")
  val lockServiceUrl = "/locks/worldpay-downloader"


  var lastResponseOpt : Option[WSResponse] = None

  When("""^I ask for the next scheduled date"""){ () =>
    lastResponseOpt = Some(getJson("/nextScheduledDate").futureValue)
  }

  Then("""^I received a (\d+) response with a valid date"""){ (expectedStatusCode:Int) =>
    assertExpectedStatusCode(expectedStatusCode)
    assertValidDateReceived()
  }



  Given("""^that the lock does not currently exist$"""){ () =>
    stubFor(get(urlEqualTo(lockServiceUrl)).willReturn(aResponse().withStatus(404)))
  }

  Given("""^that the lock currently exists$"""){ () =>
    stubFor(get(urlEqualTo(lockServiceUrl)).willReturn(aResponse().withStatus(200)))
  }

  Given("""^that we can create the lock$"""){ () =>
    stubFor(post(urlEqualTo(lockServiceUrl)).willReturn(aResponse().withStatus(201)))
  }

  Given("""^that we cannot create the lock$"""){ () =>
    stubFor(post(urlEqualTo(lockServiceUrl)).willReturn(aResponse().withStatus(409)))
  }

  Given("""^we can release the lock$"""){ () =>
    stubFor(delete(urlEqualTo(lockServiceUrl)).willReturn(aResponse().withStatus(204)))
  }

  Given("""^we cannot release the lock$"""){ () =>
    stubFor(delete(urlEqualTo(lockServiceUrl)).willReturn(aResponse().withStatus(404)))
  }

  When("""^I request release of the current lock$"""){ () =>
    lastResponseOpt = Some(deleteJson("/lock").futureValue)
  }

  When("""^I ask for the current lock status$"""){ () =>
    lastResponseOpt = Some(getJson("/lock").futureValue)
  }

  Then("""^I receive a (\d+) response$"""){ (expectedStatusCode:Int) =>
    assertExpectedStatusCode(expectedStatusCode)
  }

  Given("""^that the file for (.+) has not been processed$"""){ (date:String) =>

    val dataSetAsJson = s"""{
                         |    "dataSets": []
                         |  }""".stripMargin

    stubFor(get(urlEqualTo(generateFileProcessedUrl(date))).willReturn(aResponse().withBody(dataSetAsJson).withStatus(200)))
  }

  Given("""^that the file for (.+) has already been processed$"""){ (date:String) =>
    stubThatFileHasBeenProcessed(date)
  }


  Given("""^that today's file has already been processed$""") {
    stubThatFileHasBeenProcessed(todayAsRawDate)
  }


  When("""^I ask if the file for (.+) has been processed$"""){ (date:String) =>
    lastResponseOpt = Some(getJson(s"/processed/$date")futureValue)
  }

  When("""^I request that the file for (.+) be downloaded & processed$"""){ (date:String) =>
    lastResponseOpt = Some(postEmptyBody(s"/processed/$date").futureValue)
  }

  When("""^I request that today's file be downloaded & processed$"""){
    lastResponseOpt = Some(postEmptyBody(s"/processed/$todayAsRawDate").futureValue)
  }

  Then("""^we can verify that the lock was released$"""){ () =>
    WireMock.verify(1, deleteRequestedFor(urlMatching(lockServiceUrl)))
  }

  Given("""^that we request a re-initialisation of the Scheduler$"""){ () =>
    lastResponseOpt = Some(postEmptyBody(s"/scheduler").futureValue)
  }

  Given("""^I wait for a little bit while the app is initiated$"""){ () =>
    Thread.sleep(500)
  }



  private def todayAsRawDate = LocalDate.now().toString("yyyy-MM-dd")

  private def assertExpectedStatusCode(expectedStatusCode: Int): Unit =
    getExpectedLastResponseOpt.map(lastResponse => assert(lastResponse.status == expectedStatusCode, s"Expected status: $expectedStatusCode but was: ${lastResponse.status}"))


  private def getExpectedLastResponseOpt: Option[WSResponse] =
    if (!lastResponseOpt.isDefined) {
      assert(false, "No last response")
      None
    } else lastResponseOpt

  private def generateFileProcessedUrl(date:String) = s"/payments/worldpay?fromDate=$date&toDate=$date"

  private def stubThatFileHasBeenProcessed(rawDate: String): Unit = {
    val dataSetAsJson = s"""{
                           |    "dataSets": [
                           |        {
                           |            "dataSetID": "123-456-789",
                           |            "status": "Sending",
                           |            "links": [
                           |                {
                           |                    "rel": "details",
                           |                    "href": "https://hostname:443/payments/worldpay/123-456-789"
                           |                }
                           |            ]
                           |        }],
                           |        "links": [
                           |        {
                           |            "rel": "self",
                           |            "href": "https://hostname:443/payments/worldpay?fromDate=2014-10-09&toDate=2014-10-09"
                           |        }
                           |    ]
                           |  }""".stripMargin

    stubFor(get(urlEqualTo(generateFileProcessedUrl(rawDate))).willReturn(aResponse().withBody(dataSetAsJson).withStatus(200)))
  }

  private def assertValidDateReceived(): Unit = {
    getExpectedLastResponseOpt.map (lastResponse => {
      val possibleDate = lastResponse.body
      Try {
        DateTime.parse(possibleDate,dateTimeFormat)
        ()
      }.getOrElse(assert(false,s"Invalid rawDate: $possibleDate"))
    }
    )
  }
}
