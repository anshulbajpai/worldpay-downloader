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
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import uk.gov.hmrc.play.test.UnitSpec
import utils.Commission

class CommissionSpec extends UnitSpec with GeneratorDrivenPropertyChecks {

  "The commission" should {

    "be 1.5% of the total value by example" in {
      expectedCommission foreach { case (total, commission) =>
        Commission(Pence(total)) should be(Pence(commission))
      }
    }

  }

  val expectedCommission = Seq(
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
