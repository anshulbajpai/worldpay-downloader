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

package unit.parsers

import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import parsers.{MerchantOutletParser, HeaderParser}
import uk.gov.hmrc.play.test.UnitSpec

class ModelParserSpecs extends UnitSpec with MockitoSugar {

  val headerText   = "000000000100000000009210000006000000010000000000000000000000000000003334520000000000000000000000000000100000000000000000008000000000000000111050002002"
  val merchantText = "10000000030000022133922140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000"

  "Header Parser" should {
    "find the correct count value" in {
      val header = HeaderParser(headerText)

      header.transactionCount shouldBe 6
    }
  }

  "Merchant Parser" should {
    "find the merchant ID" in {
      val merchant = MerchantOutletParser(merchantText)

      merchant.merchantId shouldBe "22133922"
    }
    "find trading date" in {
      val merchant = MerchantOutletParser(merchantText)
      merchant.tradingDate shouldBe new LocalDate(2011,4,14)
    }
  }
}
