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

import play.api.libs.json._
import model.RcsDataSetStatuses.RcsDataSetStatus

case class RcsChunk(payments: Seq[RcsPayment]) {
  def chunkRelease(chunkId:Int): ChunkRelease = {

    val paymentAmounts = for {
      payment <- payments
      paymentItem <- payment.paymentItems if paymentItem.transactionType == RcsPaymentItem.HodPayment
    } yield paymentItem.amount

    val totalAmount = Pence(paymentAmounts.map(_.value).sum)
    val numberOfPayments = paymentAmounts.size

    ChunkRelease(chunkId = chunkId.toString, transactionTypes = List(ChunkTransactionSummary(RcsPaymentItem.HodPayment, totalAmount, numberOfPayments)))
  }
}

case class Pence(value:Long) extends AnyVal {
  def map(f: (Long => Number)): Pence = Pence(f(value).longValue())
}

object Pence {

  implicit val reader = Reads.of[Long].map(new Pence(_))
  implicit val writer = Writes[Pence] { p => JsNumber(BigDecimal(p.value))}
}

case class RcsPayment(consecNumber: Int, date: String, paymentProviderId:String, paymentItems: List[RcsPaymentItem])

case class RcsPaymentItem(transactionType: String, destinationAccountNumber: String, amount: Pence, reference: String)

case class ChunkTransactionSummary(transactionType: String, totalValue: Pence, numberOfPayments: Int)

case class ChunkRelease(chunkId: String, transactionTypes: List[ChunkTransactionSummary])

case class RcsDataSet(dataSetID: String, status: String)

object RcsPaymentItem {
  val HodPayment = "26"
  val CommissionPayment = "27"
  implicit val formats = Json.format[RcsPaymentItem]
}

object RcsPayment {
  implicit val formats = Json.format[RcsPayment]
}

object RcsChunk {
  val DefaultChunkId = "1"
  implicit val formats = Json.format[RcsChunk]
}

object ChunkTransactionSummary {
  implicit val formats = Json.format[ChunkTransactionSummary]
}

object ChunkRelease {
  implicit val formats = Json.format[ChunkRelease]
}

object RcsDataSet {
  implicit val rcsDataSetReads = Json.reads[RcsDataSet]
}

object RcsDataSetStatuses extends Enumeration {
  type RcsDataSetStatus = Value
  val Released = Value
}
