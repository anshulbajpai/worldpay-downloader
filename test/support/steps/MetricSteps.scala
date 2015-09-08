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

package support.steps

import cucumber.api.DataTable
import cucumber.api.scala.ScalaDsl
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.libs.json.JsNumber
import scala.collection.JavaConverters._

class MetricsSteps extends ScalaDsl with BaseSteps with WSJsonClient with ScalaFutures with IntegrationPatience {

  And( """^The metric registry contains the following counters""") { (counters: DataTable) =>
    registryContainsCounters(counters)
  }

  def registryContainsCounters(counters: DataTable): Unit = {
    eventually {
      val json = getJson("/admin/metrics").futureValue
      counters.asLists(classOf[String]).asScala.foreach {
        c =>
          val metricRegistryVar = c.get(0)
          val metricRegistryCounter = c.get(1).toLong
          withClue(s"Expected: $metricRegistryVar with value $metricRegistryCounter -> ") {
            (json.json \ "counters" \ metricRegistryVar \ "count").asOpt[JsNumber].map(_.value).getOrElse(0) should be(metricRegistryCounter)
          }
      }
    }
  }
}
