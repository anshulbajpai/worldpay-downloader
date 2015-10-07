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

package utils

import org.joda.time.LocalDate

import model.Pence

object Commission {

  val switchDate = new LocalDate(2015, 11, 2)
  val oldRate = 1.4
  val newRate = 1.5

  def apply(total: Pence, transactionDate: LocalDate): Pence = if (transactionDate.isBefore(switchDate)) calculate(total, oldRate)  else calculate(total, newRate)
  private def calculate(total: Pence, rate: Double) = total map (t => math.max(1,  math.round(t / (100 + rate) * rate)))

}
