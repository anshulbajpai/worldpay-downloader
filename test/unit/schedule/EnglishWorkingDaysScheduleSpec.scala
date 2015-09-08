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

package unit.schedule

import org.joda.time.{DateTimeZone, LocalDate, LocalTime}
import org.scalatest._
import schedule.Schedules
import uk.gov.hmrc.time.workingdays.{BankHoliday, BankHolidaySet}

class EnglishWorkingDaysScheduleSpec extends WordSpec with ShouldMatchers {
  
  implicit class LocalDateExtensions(localDate:LocalDate){
    def at(time:LocalTime) ={
      localDate.toDateTime(time, DateTimeZone.forID("Europe/London"))
    }
  }

  val Exactly_10am = new LocalTime(10,0)
  val Before_10am  = new LocalTime(9,59)
  val After_10am   = new LocalTime(16,37)
  val Anytime      = new LocalTime(12,5)

  val Monday_2_Jan    = new LocalDate(2012, 1, 2)
  val Tueday_3_Jan    = new LocalDate(2012, 1, 3)
  val Saturday_7_Jul  = new LocalDate(2012, 7, 7)
  val Sunday_8_Jul    = new LocalDate(2012, 7, 8)
  val Monday_9_Jul    = new LocalDate(2012, 7, 9)
  val Tuesday_10_Jul  = new LocalDate(2012, 7, 10)
  val Monday_24_Dec   = new LocalDate(2012, 12, 24)
  val Thursday_27_Dec = new LocalDate(2012, 12, 27)
  val Boxing_Day      = new LocalDate(2012, 12, 26)



  "The EnglishWorkingDaysSchedule" should {

    implicit val bankHolidays = BankHolidays.eventSet

    "return 10am on the same working day if the given time is before that time" in {
      Schedules.next10amLondonTimeInAWorkingDay(Monday_9_Jul at Before_10am) should be(Monday_9_Jul at Exactly_10am)
    }

    "return 10am on the next working day if the given time is after that time" in {
      Schedules.next10amLondonTimeInAWorkingDay(Monday_9_Jul at After_10am) should be(Tuesday_10_Jul at Exactly_10am)
    }

    "return 10am on the next working day if the given time is a bank holiday" in {
      Schedules.next10amLondonTimeInAWorkingDay(Boxing_Day at Anytime) should be(Thursday_27_Dec at Exactly_10am)
    }

    "return 10am on the next working day even if there are consecutive bank holidays" in {
      Schedules.next10amLondonTimeInAWorkingDay(Monday_24_Dec at After_10am) should be(Thursday_27_Dec at Exactly_10am)
    }

    "return 10am on the next working day if the given time is a weekend day" in {
      Schedules.next10amLondonTimeInAWorkingDay(Saturday_7_Jul at Anytime) should be(Monday_9_Jul at Exactly_10am)
      Schedules.next10amLondonTimeInAWorkingDay(Sunday_8_Jul at Anytime) should be(Monday_9_Jul at Exactly_10am)
    }

    "return 10am on the next working day if the given time is a bank holiday substitute" in {
      Schedules.next10amLondonTimeInAWorkingDay(Monday_2_Jan at Anytime) should be(Tueday_3_Jan at Exactly_10am)
    }
  }
}

object BankHolidays {

  val eventSet =
    BankHolidaySet(
      "england-and-wales",
      List(
        BankHoliday(title = "New Year's Day", date = new LocalDate(2012, 1, 2)),
        BankHoliday(title = "Easter Monday", date = new LocalDate(2012, 4, 9)),
        BankHoliday(title = "Early May bank holiday", date = new LocalDate(2012, 5, 7)),
        BankHoliday(title = "Spring bank holiday", date = new LocalDate(2012, 6, 5)),
        BankHoliday(title = "Queen’s Diamond Jubilee", date = new LocalDate(2012, 6, 5)),
        BankHoliday(title = "Summer bank holiday", date = new LocalDate(2012, 8, 27)),
        BankHoliday(title = "Christmas Day", date = new LocalDate(2012, 12, 25)),
        BankHoliday(title = "Boxing Day", date = new LocalDate(2012, 12, 26)),
        BankHoliday(title = "New Year's Day", date = new LocalDate(2013, 1, 1))
      )
    )
}
