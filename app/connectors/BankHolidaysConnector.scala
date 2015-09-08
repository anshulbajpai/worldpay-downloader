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

package connectors

import config.WSHttp
import play.api.libs.json.Json
import uk.gov.hmrc.time.workingdays.{BankHolidaySet, BankHoliday}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.play.audit.http.HeaderCarrier

object BankHolidaysConnector {

  val http = WSHttp

  implicit val emptyHeaderCarrier = HeaderCarrier()

  def bankHolidaysForEnglandAndWales(): Future[BankHolidaySet] = {
    implicit val bankHolidayReads = Json.reads[BankHoliday]
    implicit val bankHolidaySetReads = Json.reads[BankHolidaySet]

    http.doGet("https://www.gov.uk/bank-holidays.json") map { r =>
      val allBankHolidays = Json.parse(r.body).as[Map[String,BankHolidaySet]]
      allBankHolidays("england-and-wales")
    }
  }

}
