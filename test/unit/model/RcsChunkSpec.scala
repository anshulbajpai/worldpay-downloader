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

import model._
import uk.gov.hmrc.play.test.UnitSpec


class RcsChunkSpec extends UnitSpec {

  implicit def IntPence(i:Int):Pence = Pence(i)

   "RcsChunk " should {
    "calculate the chunk summary" in {

      val rcsChunk = RcsChunk(payments = List(
        RcsPayment(consecNumber = 1, date = "14001", paymentProviderId = "1234567890K-123456-0",  List(
          RcsPaymentItem("26", "12345678", 1200, "1234567890K")
        ))))

      val chunkRelease = rcsChunk.chunkRelease(1)
      val transactionSummary: ChunkTransactionSummary = chunkRelease.transactionTypes.head
      
      chunkRelease.chunkId should be("1")
      chunkRelease.transactionTypes.size should be(1)
      transactionSummary.transactionType should be("26")
      transactionSummary.totalValue.value should be(1200)
      transactionSummary.numberOfPayments should be(1)
    }

     "the chunk summary calculation should only include transaction types of 26 at present" in {
       val rcsChunk = RcsChunk(payments = List(
         RcsPayment(consecNumber = 1, date = "14001", paymentProviderId = "1234567890K-123456-0", List(
           RcsPaymentItem("27", "12345678", 1200, "1234567890K"),
           RcsPaymentItem("26", "12345678", 1200, "1234567890K")
         ))))

       val chunkRelease = rcsChunk.chunkRelease(1)
       val transactionSummary: ChunkTransactionSummary = chunkRelease.transactionTypes.head

       chunkRelease.chunkId should be("1")
       transactionSummary.transactionType should be("26")
       transactionSummary.totalValue.value should be(1200)
       transactionSummary.numberOfPayments should be(1)
     }

     "calculate the chunk summary with very large payments" in {

       val largePaymentAmount = Integer.MAX_VALUE

       val rcsChunk = RcsChunk(payments = List(
         RcsPayment(consecNumber = 1, date = "14001", paymentProviderId = "1234567890K-123456-0", List(RcsPaymentItem("26", "12345678", largePaymentAmount, "1234567890K"))),
         RcsPayment(consecNumber = 2, date = "14001", paymentProviderId = "0987654321K-123456-0", List(RcsPaymentItem("26", "12345678", largePaymentAmount, "0987654321K")))
       ))

       val chunkRelease = rcsChunk.chunkRelease(1)
       chunkRelease.transactionTypes.head.totalValue.value shouldBe (BigInt(Integer.MAX_VALUE) + BigInt(Integer.MAX_VALUE))

     }

     "calculate the chunk summary with multiple payments" in {
       val rcsChunk = RcsChunk(payments = List(
         RcsPayment(consecNumber = 1, date = "14001", paymentProviderId = "1234567890K-123456-0", List(RcsPaymentItem("26", "12345678", 1200, "1234567890K"))),
         RcsPayment(consecNumber = 1, date = "14001", paymentProviderId = "0987654321K-123456-0", List(RcsPaymentItem("26", "12345678", 1200, "0987654321K")))
       ))

       val chunkRelease = rcsChunk.chunkRelease(1)
       val transactionSummary: ChunkTransactionSummary = chunkRelease.transactionTypes.head

       chunkRelease.chunkId should be("1")
       chunkRelease.transactionTypes.size should be(1)
       transactionSummary.transactionType should be("26")
       transactionSummary.totalValue.value should be(2400)
       transactionSummary.numberOfPayments should be(2)
     }
     
     "chunk summary should have the id passed through" in {
       val rcsChunk = RcsChunk(payments = List(
         RcsPayment(consecNumber = 1, date = "14001", paymentProviderId = "1234567890K-123456-0",  List(
           RcsPaymentItem("26", "12345678", 1200, "1234567890K")
         ))))

       val chunkRelease = rcsChunk.chunkRelease(12)

       chunkRelease.chunkId should be("12")
     }

  }
}
