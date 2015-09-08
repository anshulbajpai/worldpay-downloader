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

package unit.model

import uk.gov.hmrc.play.test.UnitSpec
import unit.MerchantBuilder

class MerchantsSpec extends UnitSpec with MerchantBuilder {

  "Getting the MerchantDetails for a specified merchant id" should {

    "return the expected details for SA for Debit Card" in {
      val saForDebitCard = merchants.withMerchantId("12345678").get
      saForDebitCard.merchantId shouldBe "12345678"
      saForDebitCard.accountNumber shouldBe "19999200"
      saForDebitCard.taxType shouldBe "SA"
      saForDebitCard.isCreditCard shouldBe false
    }

    "return the expected details for SA for Credit Card" in {
      val saForCreditCard = merchants.withMerchantId("12345688").get
      saForCreditCard.merchantId shouldBe "12345688"
      saForCreditCard.accountNumber shouldBe "19999200"
      saForCreditCard.taxType shouldBe "SA"
      saForCreditCard.isCreditCard shouldBe true
    }

    "return the expected details for VAT for Debit Card" in {
      val vatForDebitCard = merchants.withMerchantId("10345678").get
      vatForDebitCard.merchantId shouldBe "10345678"
      vatForDebitCard.accountNumber shouldBe "19999211"
      vatForDebitCard.taxType shouldBe "VAT"
      vatForDebitCard.isCreditCard shouldBe false
    }

    "return the expected details for VAT for Credit Card" in {
      val vatForCreditCard = merchants.withMerchantId("11345678").get
      vatForCreditCard.merchantId shouldBe "11345678"
      vatForCreditCard.accountNumber shouldBe "19999211"
      vatForCreditCard.taxType shouldBe "VAT"
      vatForCreditCard.isCreditCard shouldBe true
    }
    //
    "return the expected details for CT for Debit Card" in {
      val ctForDebitCard = merchants.withMerchantId("12305678").get
      ctForDebitCard.merchantId shouldBe "12305678"
      ctForDebitCard.accountNumber shouldBe "19999222"
      ctForDebitCard.taxType shouldBe "CT"
      ctForDebitCard.isCreditCard shouldBe false
    }

    "return the expected details for CT for Credit Card" in {
      val ctForCreditCard = merchants.withMerchantId("12315678").get
      ctForCreditCard.merchantId shouldBe "12315678"
      ctForCreditCard.accountNumber shouldBe "19999222"
      ctForCreditCard.taxType shouldBe "CT"
      ctForCreditCard.isCreditCard shouldBe true
    }

    "return the expected details for EPAYE for Debit Card" in {
      val ePayeForDebitCard = merchants.withMerchantId("12345078").get
      ePayeForDebitCard.merchantId shouldBe "12345078"
      ePayeForDebitCard.accountNumber shouldBe "19999233"
      ePayeForDebitCard.taxType shouldBe "EPAYE"
      ePayeForDebitCard.isCreditCard shouldBe false
    }

    "return the expected details for EPAYE for Credit Card" in {
      val ePayeForCreditCard = merchants.withMerchantId("12345178").get
      ePayeForCreditCard.merchantId shouldBe "12345178"
      ePayeForCreditCard.accountNumber shouldBe "19999233"
      ePayeForCreditCard.taxType shouldBe "EPAYE"
      ePayeForCreditCard.isCreditCard shouldBe true
    }

    "return the expected details for Other taxes for Debit Card" in {
      val otherForDebitCard = merchants.withMerchantId("12333078").get
      otherForDebitCard.merchantId shouldBe "12333078"
      otherForDebitCard.accountNumber shouldBe "19999555"
      otherForDebitCard.taxType shouldBe "OTHER"
      otherForDebitCard.isCreditCard shouldBe false
    }

    "return the expected details for Other taxes for Credit Card" in {
      val otherForCreditCard = merchants.withMerchantId("12444078").get
      otherForCreditCard.merchantId shouldBe "12444078"
      otherForCreditCard.accountNumber shouldBe "19999555"
      otherForCreditCard.taxType shouldBe "OTHER"
      otherForCreditCard.isCreditCard shouldBe true
    }

    "return the expected details for SDLT for Debit Card" in {
      val sdltForDebitCard = merchants.withMerchantId("10045078").get
      sdltForDebitCard.merchantId shouldBe "10045078"
      sdltForDebitCard.accountNumber shouldBe "19999255"
      sdltForDebitCard.taxType shouldBe "SDLT"
      sdltForDebitCard.isCreditCard shouldBe false
    }

    "return the expected details for SDLT for Credit Card" in {
      val sdltForDebitCard = merchants.withMerchantId("11145078").get
      sdltForDebitCard.merchantId shouldBe "11145078"
      sdltForDebitCard.accountNumber shouldBe "19999255"
      sdltForDebitCard.taxType shouldBe "SDLT"
      sdltForDebitCard.isCreditCard shouldBe true
    }

  }
}
