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

import model._
import org.joda.time.LocalDate
import org.joda.time.format.{DateTimeParser, DateTimeFormat}

object HeaderParser {
  def apply(line: String): Header = {
    val transactionCount = line.substring(23, 23+7).toInt
    Header(transactionCount)
  }
}

object MerchantOutletParser {
  def apply(line: String): MerchantOutlet = {
    val merchantId = line.substring(10, 10 + 13).dropWhile(_ == '0')
    val acceptedDebitsValue = line.substring(68, 68 + 11)
    val acceptedDebitsCount = line.substring(122, 122 + 7)
    val tradingDate = extractTradingDate(line)

    MerchantOutlet(merchantId, acceptedDebitsValue.toLong, acceptedDebitsCount.toLong, tradingDate)
  }


  private def extractTradingDate(line: String): LocalDate = {
    val rawTradingDateAsDDMMYY = line.substring(23, 23 + 6)
    LocalDate.parse(rawTradingDateAsDDMMYY, DateTimeFormat.forPattern("ddMMyy"))
  }
}

object TransactionDataParser {
  val transactionDateFormat = DateTimeFormat.forPattern("ddMMyy")

  def apply(line: String): TransactionData = {
    val amountInPenceStr = line.substring(33, 33 + 11)
    val date = transactionDateFormat.parseLocalDate(line.substring(44, 44 + 6))
    val status = PaymentStatus.withName(line.substring(64, 64 + 1))
    val transactionType = TransactionType(line.substring(56, 56 + 1).toInt)

    TransactionData(amountInPenceStr.toInt, date, status, transactionType)
  }
}

object TransactionSuppDataParser {
  def apply(line: String): TransactionSuppData = {
    val originatorsReference = line.substring(30)

    TransactionSuppData(originatorsReference)
  }
}
