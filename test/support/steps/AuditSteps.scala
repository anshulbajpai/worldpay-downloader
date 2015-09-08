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

import scala.collection.JavaConversions._
import play.api.libs.json._

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import cucumber.api.DataTable
import cucumber.api.scala.ScalaDsl
import org.joda.time.DateTime

import uk.gov.hmrc.play.audit.model.{DataEvent, MergedDataEvent, DataCall}


class ÏAuditSteps extends ScalaDsl with BaseSteps {

  implicit val dateTimeReads = DateTimeReads.dateTimeReads
  implicit val dataCallFormat = Json.format[DataCall]
  implicit val mergedDataEventFormat = Json.format[MergedDataEvent]
  implicit val dataEventFormat = Json.format[DataEvent]


  Then( """^a (simple|merged) event with source '(.+)' and type '(.+)' has been audited with:$""") { (eventType: String, auditSource: String, auditType: String, data: DataTable) =>
    eventType match {
      case "simple" => eventHasBeenAudited(auditSource, auditType, data)
      case "merged" => mergedEventHasBeenAudited(auditSource, auditType, data)
    }
  }

  Then( """^no audit event of type '(.*)' with source '(.*)' is generated$""") { (auditType: String, auditSource: String) =>
    val mergedGetEvents = allMergedEvents().filter(event=> event.auditSource==auditSource && event.auditType==auditType && event.request.detail("method") == "GET")
    mergedGetEvents.forall(event => event.response.detail("responseMessage")=="noaudit") should be (true)

    val mergedPostEvents = allMergedEvents().filter(event=> event.auditSource==auditSource && event.auditType==auditType && event.request.detail("method") == "POST")
    mergedPostEvents.forall(event => event.request.detail("requestBody")=="noaudit") should be (true)
    mergedPostEvents.forall(event => event.response.detail("responseMessage")=="noaudit") should be (true)
  }

  private def eventHasBeenAudited(auditSource: String, auditType: String, data: DataTable) = {
    val expectedData = tupple3(data)

    val tags = expectedData collect { case("tags", k, v) => (k,v) }
    val detail = expectedData collect { case("detail", k, v) => (k,v) }

    val eventToFind = DataEvent(auditSource, auditType).withTags(tags: _*).withDetail(detail: _*)

    val unmatchedCriteria = allSimpleEvents().map(event => findSimpleMismatches(eventToFind, event)).sortBy(_.size)

    withClue(
      s"""Could not find this event: $eventToFind
         | - These are the unmatched criteria of the likeliest match: ${unmatchedCriteria.headOption}
         | ${showNonMergedEventsIfAny(auditSource, auditType)}\n""".stripMargin) {
      unmatchedCriteria.headOption shouldBe Some(Iterable.empty)
    }
  }

  private def showNonMergedEventsIfAny(auditSource: String, auditType: String) = {
    val simpleEvents = allSimpleEventsFor(auditSource, auditType)
    if (simpleEvents.nonEmpty)
      s"- These are non-merged events with the same source and type: \n ${simpleEvents.mkString("\n")}"
    else "\n"
  }

  private def mergedEventHasBeenAudited(auditSource: String, auditType: String, data: DataTable) = {
    val expectedData = tupple4(data)

    val requestTags = expectedData collect { case("request", "tags", k, v) => (k,v) }
    val requestDetails = expectedData collect { case("request", "detail", k, v) => (k,v) }
    val responseTags = expectedData collect { case("response", "tags", k, v) => (k,v) }
    val responseDetails = expectedData collect { case("response", "detail", k, v) => (k,v) }

    val eventToFind = MergedDataEvent(auditSource, auditType,
      request = DataCall(requestTags.toMap, requestDetails.toMap, DateTime.now),
      response = DataCall(responseTags.toMap, responseDetails.toMap, DateTime.now))

    val unmatchedCriteria = allMergedEvents().map(event => findMergedMismatches(eventToFind, event)).sortBy(_.size)

    withClue(s"""Could not find this merged event: $eventToFind
                | - These are the unmatched criteria of the likeliest match: ${unmatchedCriteria.headOption}
                | - These are events with the same source and type: \n ${allMergedEventsFor(auditSource, auditType).mkString("\n")}""".stripMargin) {
      unmatchedCriteria.headOption shouldBe Some(Iterable.empty)
    }
  }
  
  private def findMergedMismatches(expected: MergedDataEvent, actual: MergedDataEvent): Iterable[(String, String, String)] = {
    findUnmatchedCriteria("", directProperties(expected), directProperties(actual)) ++
    findUnmatchedCriteria("request.tags", expected.request.tags, actual.request.tags) ++
    findUnmatchedCriteria("request.detail", expected.request.detail, actual.request.detail) ++
    findUnmatchedCriteria("response.tags", expected.response.tags, actual.response.tags) ++
    findUnmatchedCriteria("response.detail", expected.response.detail, actual.response.detail)
  }

  private def findSimpleMismatches(expected: DataEvent, actual: DataEvent): Iterable[(String, String, String)] = {
    findUnmatchedCriteria("", directProperties(expected), directProperties(actual)) ++
    findUnmatchedCriteria("tags.", expected.tags, actual.tags) ++
    findUnmatchedCriteria("detail.", expected.detail, actual.detail)
  }

  type EitherDataEvent = {
    val auditSource: String
    val auditType: String
  }

  private def directProperties(event: EitherDataEvent): Map[String, String] = Map(
    "auditSource" -> event.auditSource,
    "auditType" -> event.auditType
  )

  private def findUnmatchedCriteria(path: String, expected: Map[String, String], actual: Map[String, String]): Iterable[(String, String, String)] =
    expected.collect {
      case (k,v) if !actual.get(k).exists(matchesValue(v)) => (path + k, v, actual.get(k).toString)
    }

  private def allSimpleEvents(): Seq[DataEvent] = {
    val auditRequests = WireMock.findAll(postRequestedFor(urlMatching("/write/audit")))
    auditRequests map { req => Json.parse(req.getBodyAsString).as[DataEvent]}
  }

  private def allMergedEvents(): Seq[MergedDataEvent] = {
    val auditRequests = WireMock.findAll(postRequestedFor(urlMatching("/write/audit/merged")))
    auditRequests map { req => Json.parse(req.getBodyAsString).as[MergedDataEvent]}
  }

  private def allSimpleEventsFor(auditSource: String, auditType: String) = allSimpleEvents() filter (a => a.auditSource == auditSource && a.auditType == auditType)
  private def allMergedEventsFor(auditSource: String, auditType: String) = allMergedEvents() filter (a => a.auditSource == auditSource && a.auditType == auditType)


  val Regex = "regex=(.*)".r
  val EndsWith = "endsWith=(.*)".r
  private def matchesValue(expected: String)(actual: String): Boolean = {
    expected match {
      case Regex(expression) => actual.matches(s"(?s)$expression")
      case EndsWith(suffix) => actual.endsWith(suffix)
      case plainText => actual.contains(plainText)
    }
  }

  private def tupple3(dataTable: DataTable): Seq[(String,String,String)] = dataTable.raw() map (x => (x(0), x(1), x(2)))
  private def tupple4(dataTable: DataTable): Seq[(String,String,String,String)] = dataTable.raw() map (x => (x(0), x(1), x(2), x(3)))
}

object DateTimeReads {
  implicit def dateTimeReads = new Reads[DateTime] {
    def reads(value: JsValue): JsResult[DateTime] = JsSuccess(DateTime.parse(value.as[String]))
  }
}
