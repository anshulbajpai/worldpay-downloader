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

package metrics

import java.util.concurrent.atomic.AtomicInteger

import com.codahale.metrics.Gauge
import com.kenshoo.play.metrics.MetricsRegistry
import model.Merchants
import org.joda.time.LocalDate

object EmisStatusMetricsPublisher {
  def apply(merchants: Merchants) = new EmisStatusMetricsPublisher(merchants)
}

class EmisStatusMetricsPublisher(val merchants: Merchants) {

  val emisStatus = new AtomicInteger(0)

  val graphiteReportStatus = new Gauge[Integer]() {
    override def getValue = emisStatus.get
  }

  MetricsRegistry.defaultRegistry.register("emis.status", graphiteReportStatus)

  private val salesCountMetricsMap = merchants.allMerchants.map(m => m.merchantId -> MetricsRegistry.defaultRegistry.counter(s"emis.${m.taxType}.sales.occurence")).toMap
  private val salesAmountMetricsMap = merchants.allMerchants.map(m => m.merchantId -> MetricsRegistry.defaultRegistry.counter(s"emis.${m.taxType}.sales.value")).toMap

  def reportSalesCount(merchantId: String, value: Long) = salesCountMetricsMap.get(merchantId).map(_.inc(value))

  def reportSalesValue(merchantId: String, value: Long) = salesAmountMetricsMap.get(merchantId).map(_.inc(value))

  def reportTransactionByMerchant(tax: String, paymentType: String, date: LocalDate, counter: Long) = {
    val registryVariable = Array(
      "emis",
      "transactions",
      tax.toUpperCase,
      paymentType.toUpperCase.split(" ").head,
      date.getYear,
      "%02d".format(date.getMonthOfYear),
      "%02d".format(date.getDayOfMonth)).mkString(".")

    MetricsRegistry.defaultRegistry.counter(registryVariable).inc(counter)
  }

  def started(): Unit = {
    emisStatus.set(1)
  }

  def completedSuccessfully() {
    emisStatus.set(0)
  }
}
