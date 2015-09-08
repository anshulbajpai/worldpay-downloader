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

package model

import config.MerchantKeys
import org.joda.time.LocalDate

object Merchants {
  def apply(config: Map[String, String]) = new Merchants(config)
}

class Merchants (config: Map[String, String])  extends Enumeration {
  import MerchantKeys._

  lazy val SelfAssessmentForDebitCard = MerchantDetails( config(SaForDebitCardId), config(SaForDebitCardAccount), "SA", false )
  lazy val SelfAssessmentForCreditCard  = MerchantDetails( config(SaForCreditCardId), config(SaForCreditCardAccount), "SA", true )
  lazy val VatForDebitCard  = MerchantDetails( config(VatForDebitCardId), config(VatForDebitCardAccount), "VAT", false )
  lazy val VatForCreditCard  = MerchantDetails( config(VatForCreditCardId), config(VatForCreditCardAccount), "VAT", true )
  lazy val CorporationTaxForDebitCard  = MerchantDetails( config(CtForDebitCardId), config(CtForDebitCardAccount), "CT", false )
  lazy val CorporationTaxForCreditCard  = MerchantDetails( config(CtForCreditCardId), config(CtForCreditCardAccount), "CT", true )
  lazy val EPayeForDebitCard  = MerchantDetails( config(EPayeForDebitCardId), config(EPayeForDebitCardAccount), "EPAYE", false )
  lazy val EPayeForCreditCard  = MerchantDetails( config(EPayeForCreditCardId), config(EPayeForCreditCardAccount), "EPAYE", true )
  lazy val OtherTaxesForDebitCard  = MerchantDetails( config(OtherTaxesForDebitCardId), config(OtherTaxesForDebitCardAccount), "OTHER", false )
  lazy val OtherTaxesForCreditCard  = MerchantDetails( config(OtherTaxesForCreditCardId), config(OtherTaxesForCreditCardAccount), "OTHER", true )
  lazy val StampDutyLandTaxForDebitCard  = MerchantDetails( config(SdltForDebitCardId), config(SdltForDebitCardAccount), "SDLT", false )
  lazy val StampDutyLandTaxForCreditCard  = MerchantDetails( config(SdltForCreditCardId), config(SdltForCreditCardAccount), "SDLT", true )

  lazy val allMerchants = Seq( SelfAssessmentForDebitCard, 
    SelfAssessmentForCreditCard,
    VatForDebitCard, VatForCreditCard,
    CorporationTaxForDebitCard, CorporationTaxForCreditCard,
    EPayeForDebitCard, EPayeForCreditCard,
    OtherTaxesForDebitCard, OtherTaxesForCreditCard,
    StampDutyLandTaxForDebitCard, StampDutyLandTaxForCreditCard)


  def merchantIdSupported(merchantId: String): Boolean = withMerchantId(merchantId).isDefined

  def withMerchantId(targetMerchantId: String): Option[MerchantDetails] = allMerchants.find(_.merchantId == targetMerchantId)

  case class MerchantDetails(merchantId: String, accountNumber: String, taxType: String, isCreditCard: Boolean) extends Val(nextId, merchantId) {
    val isDebitCard = !isCreditCard
  }
}

case class EmisReport(header: Header, merchants: Seq[Merchant])
case class Header(transactionCount:Int)
case class Merchant(header: Option[MerchantHeader], outlet: MerchantOutlet, transactions: Seq[Transaction])
case class MerchantHeader()
case class MerchantOutlet(merchantId: String, acceptedSalesValue: Long, acceptedSalesCount:Long, tradingDate: LocalDate)
case class Transaction(data: TransactionData, supplementaryData: TransactionSuppData)
case class TransactionData(amountInPence: Int, date: LocalDate, status: PaymentStatus.Value, transactionType:TransactionType.Value)
case class TransactionSuppData(originatorsReference: String)


object PaymentStatus extends Enumeration {
  type Status = Value
  val Accepted = Value("A")
  val Pending = Value("P")
  val Rejected = Value("R")
}

object TransactionType extends Enumeration {
  type Status = Value
  val Purchase = Value(0)
  val Refund = Value(5)
  val CashBack = Value(3)
  val CashAdvance = Value(2)

}
