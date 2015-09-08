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

import java.io.ByteArrayInputStream

import model.{TransactionType, PaymentStatus}
import org.joda.time.LocalDate
import parsers.StreamingEmisReportParser
import uk.gov.hmrc.play.test.UnitSpec

import scala.io.Source

class StreamingEmisReportParserSpec extends UnitSpec{

  val EmisReport =
    """|000000000100000000009210000006000000010000000000000000000000000000003334520000000000000000000000000000100000000000000000008000000000000000111050002002
      |050000016200000001550650000001000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
      |10000001630000051833083140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
      |1500000164465935******7108   1506000000025000101141158000E041920A                            ADE000
      |1600000165807293100000000000001234567890K-12345670
      |1500000166465935******7108   1506000001234560102141158005E041920P                            ADE000
      |160000016780729310000000000000000P1000001231510X-0
      |10000001691000051833083140411110415000000000000000000000000000000000000000001230000000000000000000000000000000000000000000000000100000000000000
      |1500000170465935******7108   1506000000028001001141158002E041920R                            ADE000
      |1600000171807293100000000000009999999999K-99999990
      |1500000170465935******7108   1506000000028001001141158003E041920R                            ADE000
      |1600000171807293100000000000009999999999K-99999990"""
      .stripMargin

  trait Setup {
    val transactions = StreamingEmisReportParser(Source.fromBytes(EmisReport.getBytes).getLines()).toSeq
  }

  class LargeEmisReportGenerator(noOfTransactions: Int) extends Iterator[String] {
    var count = 0

    override def hasNext: Boolean = {
      if(count < noOfTransactions) true else false
    }

    override def next(): String = {
      val emisReportHeader = List("000000000100000000009210000006000000010000000000000000000000000000003334520000000000000000000000000000100000000000000000008000000000000000111050002002",
                               "050000016200000001550650000001000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000",
                               "10000001630000051833083140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000")

      val transactionData = "1500000164465935******7108   1506000000025000101141158000E041920A                            ADE000"
      val suplimentryData = "1600000165807293100000000000001234567890K-12345670"
      val curr = count
      count += 1

      curr match {
        case i if i <= 2 => emisReportHeader(i)
        case i if i%2!=0 => transactionData
        case _ => suplimentryData
      }
    }
  }

  def time(block: => Unit): Unit = {
    val startTime = System.currentTimeMillis()
    block
    println("Time taken in milisecond: " + (System.currentTimeMillis()-startTime))
  }

  "StreamingEmisReportParser" should {
    "contain merchants with a merchant id, accepted sales value and accepted sales count" in new Setup {
      transactions(0).merchant.merchantId should be("51833083")
      transactions(1).merchant.acceptedSalesValue should be(9600)
      transactions(0).merchant.acceptedSalesCount should be(6)

      transactions(2).merchant.merchantId should be("1000051833083")
      transactions(2).merchant.acceptedSalesValue should be(123)
      transactions(2).merchant.acceptedSalesCount should be(1)
    }

    "contain the reference for each transaction" in new Setup {
      transactions(0).transaction.supplementaryData.originatorsReference should be("1234567890K-12345670")
      transactions(1).transaction.supplementaryData.originatorsReference should be("000P1000001231510X-0")
      transactions(2).transaction.supplementaryData.originatorsReference should be("9999999999K-99999990")
    }

    "contain the date for each transaction" in new Setup {
      transactions(0).transaction.data.date should be (new LocalDate(2014, 1, 1))
      transactions(1).transaction.data.date should be (new LocalDate(2014, 2, 1))
      transactions(2).transaction.data.date should be (new LocalDate(2014, 1, 10))
    }

    "contain the transaction amount for each transaction" in new Setup {
      transactions(0).transaction.data.amountInPence should be (2500)
      transactions(1).transaction.data.amountInPence should be (123456)
      transactions(2).transaction.data.amountInPence should be (2800)
    }

    "contain the transaction status for each transaction" in new Setup {
      transactions(0).transaction.data.status should be (PaymentStatus.Accepted)
      transactions(1).transaction.data.status should be (PaymentStatus.Pending)
      transactions(2).transaction.data.status should be (PaymentStatus.Rejected)
    }

    "contain the transaction type for each transaction" in new Setup{
      transactions(0).transaction.data.transactionType should be(TransactionType.Purchase)
      transactions(1).transaction.data.transactionType should be(TransactionType.Refund)
      transactions(2).transaction.data.transactionType should be(TransactionType.CashAdvance)
      transactions(3).transaction.data.transactionType should be(TransactionType.CashBack)
    }
  }
}
