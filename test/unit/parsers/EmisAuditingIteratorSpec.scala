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

import scala.concurrent.ExecutionContext.Implicits.global

import org.mockito.ArgumentCaptor
import org.mockito.Mockito._

import org.scalatest.mock.MockitoSugar

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import parsers.EmisAuditingIterator


class EmisAuditingIteratorSpec extends UnitSpec with MockitoSugar {
  "auditing iterator" should {
    "audit file headers" in new Setup() {
      checkAuditEventsOnIterate("000000000100000000009210000006000000010000000000000000000000000000003334520000000000000000000000000000100000000000000000008000000000000000111050002002", "EmisFileHeader")
    }
    "audit merchant headers" in new Setup() {
      checkAuditEventsOnIterate("050000000200000001550650000001000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000", "EmisMerchantCompanyHeader")
    }
    "audit merchant outlet headers" in new Setup() {
      checkAuditEventsOnIterate("10000000030000072484393000000110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000", "EmisMerchantOutletHeader")
    }
  }

  trait Setup {
    val EmisAuditingIterator = new EmisAuditingIterator {
      override val auditConnector: AuditConnector = mock[AuditConnector]
    }

    def checkAuditEventsOnIterate(lineToCheck: String, eventType: String) {
      val emisAuditingIterator = EmisAuditingIterator(Seq(lineToCheck, "other line").iterator)
      emisAuditingIterator.toList
      val captor = ArgumentCaptor.forClass(classOf[DataEvent])
      verify(EmisAuditingIterator.auditConnector).sendEvent(captor.capture())

      val event = captor.getValue
      val details = event.detail

      event.auditSource shouldBe "worldpay-downloader"
      event.auditType shouldBe eventType
      details.get("line") shouldBe Some(lineToCheck)
    }
  }


}
