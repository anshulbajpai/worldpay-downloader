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

package schedule

import org.joda.time.{DateTimeZone, DateTime, LocalTime}
import uk.gov.hmrc.time.workingdays.{BankHolidaySet, _}


object Schedules {

  val Ten_am = new LocalTime(10, 0)

  val londonTimeZone = DateTimeZone.forID("Europe/London")

  def next10amLondonTimeInAWorkingDay(from: DateTime)(implicit bankHolidays: BankHolidaySet) = {
    val localDate = from.toLocalDate

    if (from.toDateTime(londonTimeZone).toLocalTime.isBefore(Ten_am))
      localDate.toDateTime(Ten_am, londonTimeZone)
    else
      localDate.plusWorkingDays(1).toDateTime(Ten_am, londonTimeZone)
  }
}
