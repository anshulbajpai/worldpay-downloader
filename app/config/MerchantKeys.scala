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

package config

object MerchantKeys {
  val SaForDebitCardId = "saForDebitCardId"
  val SaForCreditCardId = "saForCreditCardId"
  val SaForDebitCardAccount = "saForDebitCardAccount"
  val SaForCreditCardAccount = "saForCreditCardAccount"
  val VatForDebitCardId = "vatForDebitCardId"
  val VatForCreditCardId = "vatForCreditCardId"
  val VatForDebitCardAccount = "vatForDebitCardAccount"
  val VatForCreditCardAccount = "vatForCreditCardAccount"
  val CtForDebitCardId = "ctForDebitCardId"
  val CtForCreditCardId = "ctForCreditCardId"
  val CtForDebitCardAccount = "ctForDebitCardAccount"
  val CtForCreditCardAccount = "ctForCreditCardAccount"
  val EPayeForDebitCardId = "epayeForDebitCardId"
  val EPayeForCreditCardId = "epayeForCreditCardId"
  val EPayeForDebitCardAccount = "epayeForDebitCardAccount"
  val EPayeForCreditCardAccount = "epayeForCreditCardAccount"
  val OtherTaxesForDebitCardId = "otherTaxesForDebitCardId"
  val OtherTaxesForCreditCardId = "otherTaxesForCreditCardId"
  val OtherTaxesForDebitCardAccount = "otherTaxesForDebitCardAccount"
  val OtherTaxesForCreditCardAccount = "otherTaxesForCreditCardAccount"
  val SdltForDebitCardId = "sdltForDebitCardId"
  val SdltForCreditCardId = "sdltForCreditCardId"
  val SdltForDebitCardAccount = "sdltForDebitCardAccount"
  val SdltForCreditCardAccount = "sdltForCreditCardAccount"


  val keys: Seq[String] = {
    val prefix = Seq("sa", "vat", "ct", "epaye", "otherTaxes", "sdlt")
    val suffix = Seq("ForDebitCardId", "ForCreditCardId", "ForDebitCardAccount", "ForCreditCardAccount")
    for {
      p <- prefix
      s <- suffix
    } yield s"$p$s"
  }
  
}
