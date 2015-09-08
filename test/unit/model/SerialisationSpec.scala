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

import model.RcsDataSet
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class SerialisationSpec extends UnitSpec {

  "An RcsDataSetStatus" should {

    val rcsDataSets = Seq(RcsDataSet("123-456-789", "Sending"))

    val dataSetJson = s"""{
      |    "dataSets": [
      |        {
      |            "dataSetID": "123-456-789",
      |            "status": "Sending",
      |            "links": [
      |                {
      |                    "rel": "details",
      |                    "href": "https://hostname:443/payments/worldpay/123-456-789"
      |                }
      |            ]
      |        }],
      |        "links": [
      |        {
      |            "rel": "self",
      |            "href": "https://hostname:443/payments/worldpay?fromDate=2014-10-09&toDate=2014-10-09"
      |        }
      |    ]
      |  }""".stripMargin

    "deserialise from correctly formatted json" in {
      val dataSets = Json.parse(dataSetJson.stripMargin).\("dataSets")
      dataSets.as[Seq[RcsDataSet]] shouldBe rcsDataSets
    }
  }
}
