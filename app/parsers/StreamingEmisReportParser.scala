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

package parsers

import config.MicroserviceAuditConnector
import metrics.EmisStatusMetricsPublisher
import model.{MerchantOutlet, Transaction, TransactionData}
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.Logger
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.parsing.combinator.RegexParsers


object StreamingEmisReportParser extends RegexParsers {

  case class MerchantTransaction(merchant: MerchantOutlet, transaction: Transaction)

  def apply(content: Iterator[String]): Iterator[MerchantTransaction] = {
    new TransactionIterator(content)
  }


  //TODO Maybe split this into 3 iterators?
  class TransactionIterator(emisIterator: Iterator[String]) extends Iterator[MerchantTransaction] {

    var currentMerchant: Option[MerchantOutlet] = None
    var currentTransactionData: Option[TransactionData] = None

    override def hasNext: Boolean = {
      emisIterator.hasNext
    }

    override def next(): MerchantTransaction = {
      val nextLine = emisIterator.next()

      nextLine.take(2) match {
        case "00" => next()
        case "05" => next()
        case "10" => currentMerchant = Some(MerchantOutletParser(nextLine)); next()
        case "15" => currentTransactionData = Some(TransactionDataParser(nextLine)); next()
        case "16" => {
          val result = MerchantTransaction(currentMerchant.get, Transaction(currentTransactionData.get, TransactionSuppDataParser(nextLine)))

          currentTransactionData = None

          result
        }
      }
    }
  }

}

object EmisLoggingIterator {
  def apply(date: LocalDate, emisStatusMetricsPublisher: EmisStatusMetricsPublisher)(emisIterator: Iterator[String]) = new EmisLoggingIterator(date, emisStatusMetricsPublisher, emisIterator)
}

class EmisLoggingIterator(date: LocalDate, emisStatusMetricsPublisher: EmisStatusMetricsPublisher, emisIterator: Iterator[String]) extends Iterator[String] {

  var transactionsProcessed: Long = 0

  override def hasNext: Boolean = emisIterator.hasNext

  override def next(): String = {
    val nextLine = emisIterator.next()

    nextLine.take(2) match {
      case "00" => transactionsProcessed = 0
        logMainHeaderElements(nextLine)
      case "10" => val merchantOutlet = MerchantOutletParser(nextLine)
        Logger.info(s"processing merchant header '$nextLine'")
        transactionsProcessed = transactionsProcessed + merchantOutlet.acceptedSalesCount
        logMerchantHeaderElements(date, merchantOutlet)

      case _ =>
        if (!hasNext) Logger.info(s"processed ${transactionsProcessed} payments")
    }
    nextLine
  }

  def logMainHeaderElements(headerString: String): Unit = {
    val header = HeaderParser(headerString)
    Logger.info(s"processing EMIS report with header $headerString")
  }

  def logMerchantHeaderElements(date: LocalDate, merchantOutlet: MerchantOutlet): Unit = {
    emisStatusMetricsPublisher.merchants.withMerchantId(merchantOutlet.merchantId) match {
      case Some(m) =>
        val paymentType = if (m.isCreditCard) "Credit Card" else "Debit Card"
        Logger.info( s"""processing merchant ID ${m.merchantId} (${m.taxType} - $paymentType) with ${merchantOutlet.acceptedSalesCount} payments for ${DateTimeFormat.forPattern("dd/MM/yyyy").print(merchantOutlet.tradingDate)}""")
        emisStatusMetricsPublisher.reportTransactionByMerchant(m.taxType, paymentType, date, merchantOutlet.acceptedSalesCount)
      case None =>
        Logger.error(s"Unidentified merchant Id: ${merchantOutlet.merchantId} found in EMIS report" )
    }
  }
}

trait EmisAuditingIterator {
  val auditConnector: AuditConnector

  def apply(iterator: Iterator[String]): Iterator[String] = {
    new AuditingIterator(iterator)
  }

  class AuditingIterator(emisIterator: Iterator[String]) extends Iterator[String] {

    override def hasNext: Boolean = emisIterator.hasNext

    override def next(): String = {
      val nextLine = emisIterator.next()

      nextLine.take(2) match {
        case "00" => auditConnector.sendEvent(DataEvent("worldpay-downloader", "EmisFileHeader").withDetail("line" -> nextLine))
        case "05" => auditConnector.sendEvent(DataEvent("worldpay-downloader", "EmisMerchantCompanyHeader").withDetail("line" -> nextLine))
        case "10" => auditConnector.sendEvent(DataEvent("worldpay-downloader", "EmisMerchantOutletHeader").withDetail("line" -> nextLine))
        case _ =>
      }

      nextLine
    }
  }

}

object EmisAuditingIterator extends EmisAuditingIterator {
  override val auditConnector: AuditConnector = MicroserviceAuditConnector
}
