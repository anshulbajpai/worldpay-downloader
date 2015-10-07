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

package unit.utils

import model.Pence
import org.joda.time.LocalDate
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import uk.gov.hmrc.play.test.UnitSpec
import utils.Commission

class CommissionSpec extends UnitSpec with GeneratorDrivenPropertyChecks {


  val September_20_2015 = new LocalDate(2015, 9, 20)
  val November_2_2015 = new LocalDate(2015, 11, 2)
  val December_26_2020 = new LocalDate(2020, 12, 26)

  "The commission" should {

    "be 1.4% of the total value by example, if the payment date was before 2 November, 2015" in {
      expectedCommissionBefore2November2015 foreach { case (total, commission) =>
        Commission(Pence(total), September_20_2015) should be(Pence(commission))
      }
    }

    "be 1.5% of the total value by example, if the payment date was on 2 November, 2015" in {
      expectedCommissionAfter1November2015 foreach { case (total, commission) =>
        Commission(Pence(total), November_2_2015) should be(Pence(commission))
      }
    }

    "be 1.5% of the total value by example, if the payment date was after 2 November, 2015" in {
      expectedCommissionAfter1November2015 foreach { case (total, commission) =>
        Commission(Pence(total), December_26_2020) should be(Pence(commission))
      }
    }
  }

  val expectedCommissionBefore2November2015 = Seq(
    2 -> 1,
    3 -> 1,
    6 -> 1,
    7 -> 1,
    32 -> 1,
    33 -> 1,
    34 -> 1,
    35 -> 1,
    36 -> 1,
    37 -> 1,
    38 -> 1,
    39 -> 1,
    40 -> 1,
    71 -> 1,
    72 -> 1,
    73 -> 1,
    152 -> 2,
    254 -> 4,
    355 -> 5,
    456 -> 6,
    1576 -> 22,
    10059 -> 139,
    12412 -> 171,
    29159 -> 403,
    30474 -> 421,
    216119 -> 2984,
    216120 -> 2984,
    233390 -> 3222,
    2341690 -> 32331,
    7735894 -> 106807,
    9270768 -> 127999,
    9987900 -> 137900,
    9998040 -> 138040

  )

  val expectedCommissionAfter1November2015 = Seq(
    35 -> 1,
    36 -> 1,
    37 -> 1,
    38 -> 1,
    10069 -> 149,
    233621 -> 3453,
    9997750 -> 147750,
    30504 -> 451,
    1577 -> 23,
    9279911 -> 137142,
    2343999 -> 34640,
    7743523 -> 114436,
    12425 -> 184,
    216333 -> 3197,
    10007900 -> 147900
  )
}
