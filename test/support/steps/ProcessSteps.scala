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

import java.util.{List => JList, UUID}

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import cucumber.api.scala.{EN, ScalaDsl}
import org.joda.time.LocalDate
import org.scalatest.ShouldMatchers
import org.scalatest.concurrent.{IntegrationPatience, Eventually, ScalaFutures}
import org.scalatest.time.SpanSugar
import scala.collection.JavaConversions._
import scala.util.{Success, Try}

class ProcessSteps extends ScalaDsl with EN with ScalaFutures with ShouldMatchers with Eventually with IntegrationPatience with SpanSugar {

  var initiatePaymentRequests: JList[LoggedRequest] = _

  And("""the (?:.* )?is available on "(.*)" returning a (200|202) status on success"""){ (url: String, status: Int) =>
    stubFor(post(urlMatching(url))
      .willReturn(aResponse().withStatus(status)))
  }

  And("""the (?:.* )?on "(.*)" fails returning a (\d+) status"""){ (url: String, status: Int) =>
    stubFor(post(urlMatching(url))
      .willReturn(aResponse().withStatus(status)))
  }

  Given("""^rcs does not already have a report for the date "(.*?)"$"""){ (date:String) =>
    val formattedDate = reformat(date)
    stubFor(get(urlEqualTo(s"/payments/worldpay?fromDate=$formattedDate&toDate=$formattedDate"))
                .willReturn(aResponse().withStatus(404)))
  }

  Given("""^RCS has a report for the date "(\d{4})/(\d{2})/(\d{2})" with id "(.*?)" with status "(.*?)"$"""){
    (year: Int, month: Int, day: Int, id: String, status: String) =>
      rcsHasReport(new LocalDate(year, month, day), id, status)
  }

  Given("""^RCS is available"""){
    stubFor(post(urlMatching("/payments/worldpay/.*")).willReturn(aResponse().withStatus(200)))
    stubFor(post(urlMatching("/payments/worldpay/.*/chunks/[0-9]+")).willReturn(aResponse().withStatus(202)))
    stubFor(post(urlMatching("/payments/worldpay/.*/release")).willReturn(aResponse().withStatus(200)))
    stubFor(delete(urlMatching("/payments/worldpay/.*")).willReturn(aResponse().withStatus(200)))
  }

  def rcsHasReport(date: LocalDate, id: String, status: String) = {
    val formattedDate = date.toDateTimeAtCurrentTime.toString("yyyy-MM-dd")
    stubFor(get(urlEqualTo(s"/payments/worldpay?fromDate=$formattedDate&toDate=$formattedDate"))
      .willReturn(aResponse().withStatus(200).withBody(
      s"""{
      |    "dataSets": [
      |        {
      |            "dataSetID": "$id",
      |            "status": "$status",
      |            "links": [
      |                {
      |                    "rel": "details",
      |                    "href": "https://hostname:443/payments/worldpay/$id"
      |                }
      |            ]
      |        }],
      |        "links": [
      |        {
      |            "rel": "self",
      |            "href": "https://hostname:443/payments/worldpay?fromDate=$formattedDate&toDate=$formattedDate"
      |        }
      |    ]
      |  }""".stripMargin

    )))
  }

  Given("""^RCS already has a report for today with status "(.+)"$""") { (status: String) =>
    rcsHasReport(LocalDate.now, UUID.randomUUID().toString, status)
  }

  def reformat(date: String): String = {
    date.replaceAll("/", "-")
  }

  Then("""^a GET to the RCS List Data Set through HodsApi endpoint "(.*?)" is queried from "(.*?)" to "(.*?)"$"""){ (endpoint:String, dateFrom:String, dateTo:String) =>
    verifyListDatasetQuery(endpoint, dateFrom, dateTo)
  }

  def verifyListDatasetQuery(endpoint: String, dateFrom: String, dateTo: String) {
    eventually {
      WireMock.verify(getRequestedFor(urlEqualTo(s"$endpoint?fromDate=${reformat(dateFrom)}&toDate=${reformat(dateTo)}")))
    }
  }


  Then("""^a (GET|POST|DELETE) with requestId and sessionId in header is sent to endpoint "(.*?)"$"""){ (method: String,  url: String) =>
    verifyCallToEndpointWithHeaderIsSent(method, url)
  }

  And("""a (GET|POST|DELETE) to the (?:.* )?endpoint "(.*)" is sent$""") { (method: String, url: String) =>
    verifyCallToEndpointIsSent(method, url)
  }

  And("""a (GET|POST|DELETE) to the (?:.* )?endpoint "(.*)" is sent exactly once$""") { (method: String, url: String) =>
    verifyCallToEndpointIsSent(method, url, Some(1))
  }

  And("""a (GET|POST|DELETE) to the (?:.* )?endpoint "(.*)" is sent exactly (\d+) times$""") { (method: String, url: String, times: Int) =>
    verifyCallToEndpointIsSent(method, url, Some(times))
  }


  def verifyCallToEndpointWithHeaderIsSent(method: String,url: String, times: Option[Int] = None) {
    eventually {
      method match {
        case "GET" => times.fold(WireMock.verify(getRequestedFor(urlMatching(url)).withHeader("X-Request-ID", matching("govuk-tax-.*")).withHeader("X-Session-ID", matching("session-.*"))))(t => WireMock.verify(t, getRequestedFor(urlMatching(url)).withHeader("X-Request-ID", matching("govuk-tax-.*")).withHeader("X-Session-ID", matching("session-.*"))))
        case "POST" => times.fold(WireMock.verify(postRequestedFor(urlMatching(url)).withHeader("X-Request-ID", matching("govuk-tax-.*")).withHeader("X-Session-ID", matching("session-.*"))))(t => WireMock.verify(t, postRequestedFor(urlMatching(url)).withHeader("X-Request-ID", matching("govuk-tax-.*")).withHeader("X-Session-ID", matching("session-.*"))))
        case "DELETE" => times.fold(WireMock.verify(deleteRequestedFor(urlMatching(url)).withHeader("X-Request-ID", matching("govuk-tax-.*")).withHeader("X-Session-ID", matching("session-.*"))))(t => WireMock.verify(t, deleteRequestedFor(urlMatching(url)).withHeader("X-Request-ID", matching("govuk-tax-.*")).withHeader("X-Session-ID", matching("session-.*"))))
      }
    }
  }

  def verifyCallToEndpointIsSent(method: String, url: String, times: Option[Int] = None) {
    eventually {
      method match {
        case "GET" => times.fold(WireMock.verify(getRequestedFor(urlMatching(url))))(t => WireMock.verify(t, getRequestedFor(urlMatching(url))))
        case "POST" => times.fold(WireMock.verify(postRequestedFor(urlMatching(url))))(t => WireMock.verify(t, postRequestedFor(urlMatching(url))))
        case "DELETE" => times.fold(WireMock.verify(deleteRequestedFor(urlMatching(url))))(t => WireMock.verify(t, deleteRequestedFor(urlMatching(url))))
      }
    }
  }

  And("""a POST to the (?:.* )?endpoint "(.*)" is sent with payload:""") { (url: String, body: String) =>
    verifyPostIsSentWithPayload(url, body)
  }

  def verifyPostIsSentWithPayload(url: String, body: String) {
    eventually {
      WireMock.verify(
        postRequestedFor(urlMatching(url))
          .withRequestBody(equalToJson(body)))
    }
  }

  And("""a POST to the (?:.* )?endpoint "(.*)" is NOT sent""") { (url: String) =>
    verifyPostIsNotSent(url)
  }

  def verifyPostIsNotSent(url: String) = {
    Try {
      eventually(timeout(1 second)) {
        WireMock.verify(postRequestedFor(urlMatching(url)))
      }
    } match {
      case Success(_) => fail("A POST to the URL did happen: " + url)
      case _ =>
    }
  }


  Given("""^a DELETE to the RCS Delete Data Set through HodsApi endpoint "(.*)" will fail with status "(\d+)"$"""){ (url: String, status: Int) =>
    stubFor(delete(urlMatching(url)).willReturn(aResponse().withStatus(status)))
  }
}
