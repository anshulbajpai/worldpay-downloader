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

package unit

import _root_.model.Merchants

trait MerchantBuilder {

  case class MerchantForTesting(taxType: String, merchantIdDebit: String, merchantIdCredit: String, account: String)

  val m = List(
    MerchantForTesting("sa", "12345678", "12345688", "19999200"),
    MerchantForTesting("vat", "10345678", "11345678", "19999211"),
    MerchantForTesting("ct", "12305678", "12315678", "19999222"),
    MerchantForTesting("epaye", "12345078", "12345178", "19999233"),
    MerchantForTesting("otherTaxes", "12333078", "12444078", "19999555"),
    MerchantForTesting("sdlt", "10045078", "11145078", "19999255")
  )

  val merchantMap = m.map( element => element.taxType -> element ).toMap

  val conf: List[List[(String, String)]] = for {
    tm  <- m
  } yield {
    List(
      s"${tm.taxType}ForDebitCardId" -> tm.merchantIdDebit,
      s"${tm.taxType}ForCreditCardId" -> tm.merchantIdCredit,
      s"${tm.taxType}ForDebitCardAccount" -> tm.account,
      s"${tm.taxType}ForCreditCardAccount" -> tm.account
    )
  }

  val merchants = Merchants(conf.flatten.toMap)

}
