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

package transformer

import decoder.TaxReferenceVersion0
import encoder.OrderIdVersion0
import model._
import org.joda.time.format.DateTimeFormat
import parsers.StreamingEmisReportParser.MerchantTransaction
import play.api.Logger
import utils.Commission

import scala.util.Try


object EmisReportTransformer {
  def apply(merchants: Merchants) = new EmisReportTransformer(merchants)
}

class EmisReportTransformer(merchants: Merchants) {

  val rcsPaymentDateFormat = DateTimeFormat.forPattern("yyDDD")
  val orderIdVersion0 = new OrderIdVersion0()

  private def toRcsPayments(emisReport: Iterator[MerchantTransaction]): Iterator[RcsPayment] = {
    val valids: Iterator[(MerchantTransaction, String)] = emisReport
      .filter { merchantTransaction => merchants.merchantIdSupported(merchantTransaction.merchant.merchantId)}
      .filter { merchantTransaction => merchantTransaction.transaction.data.status == PaymentStatus.Accepted}
      .filter { merchantTransaction =>
      val isPurchase = merchantTransaction.transaction.data.transactionType == TransactionType.Purchase
      if (!isPurchase) Logger.warn(s"Transaction is not a Purchase, but a ${merchantTransaction.transaction.data.transactionType}: ${merchantTransaction.transaction.supplementaryData.originatorsReference}")
      isPurchase
    }
      .map { merchantTransaction => getValidIds(merchantTransaction)}
      .flatten

    valids.zipWithIndex.map { case ((merchantTransaction, taxRef), index) =>
      buildPayment(merchantTransaction, index + 1, merchantTransaction.merchant.merchantId, taxRef)
    }
  }

  private def buildPayment(merchantTransaction: MerchantTransaction, consecNumber: Int, merchantId: String, taxRef: String) = {
    val merchantOpt = merchants.withMerchantId(merchantId)
    val paymentItems = merchantOpt match {
      case Some(m) if m.isCreditCard =>
        val commission = Commission(Pence(merchantTransaction.transaction.data.amountInPence))

        List(RcsPaymentItem(
          RcsPaymentItem.HodPayment,
          destinationAccountNumber = m.accountNumber,
          Pence(merchantTransaction.transaction.data.amountInPence - commission.value),
          reference = taxRef
        ),
          RcsPaymentItem(RcsPaymentItem.CommissionPayment,
            destinationAccountNumber = m.accountNumber,
            commission,
            reference = taxRef
          ))
      case Some(m) if m.isDebitCard =>
        List(RcsPaymentItem(
          RcsPaymentItem.HodPayment,
          destinationAccountNumber = m.accountNumber,
          Pence(merchantTransaction.transaction.data.amountInPence),
          reference = taxRef)
        )
      case _ =>
        Logger.error(s"Unknown MerchantId: $merchantId")
        List.empty
    }

    val originatorsReference: String = merchantTransaction.transaction.supplementaryData.originatorsReference
    RcsPayment(
      consecNumber,
      rcsPaymentDateFormat.print(merchantTransaction.transaction.data.date),
      orderIdVersion0.generateIdFromEmisOriginatorRef(originatorsReference).getOrElse(originatorsReference),
      paymentItems
    )
  }

  private def getValidIds(merchantTransaction: MerchantTransaction): Option[(MerchantTransaction, String)] = {
    val ref: Try[String] = TaxReferenceVersion0(merchantTransaction.transaction.supplementaryData.originatorsReference)
    if (ref.isFailure) {
      Logger.warn(s"failed to parse id '${merchantTransaction.transaction.supplementaryData.originatorsReference}' " +
        s"for merchant id: $merchantTransaction", ref.failed.get)

      None
    }
    else
      Some(merchantTransaction -> ref.get)
  }

  def toRcsChunks(emisReport: Iterator[MerchantTransaction], chunkSize: Int): Iterator[(RcsChunk, ChunkRelease)] = {
    toRcsPayments(emisReport)
      .grouped(chunkSize)
      .map(payments => RcsChunk(payments))
      .zipWithIndex.map { case (chunk, i) => chunk -> chunk.chunkRelease(i + 1)}
  }
}
