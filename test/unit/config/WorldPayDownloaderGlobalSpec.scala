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

package unit.config

import config.WorldPayDownloaderGlobal
import connectors.LockConnector
import org.joda.time.DateTime
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.concurrent.Eventually
import org.scalatest.mock.MockitoSugar
import play.api.{Application, Configuration}
import uk.gov.hmrc.play.audit.filters.AuditFilter
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.http.logging.filters.LoggingFilter
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.time.workingdays.BankHolidaySet

import scala.concurrent.Future

class WorldPayDownloaderGlobalSpec extends UnitSpec with Eventually with MockitoSugar {

  trait Setup {

    val global = new WorldPayDownloaderGlobal {

      override implicit val lockConnector = mock[LockConnector]

      var ranTask = false

      override def task = {
        ranTask = true
        Future.successful[Unit](())
      }

      override def nextExecution(implicit bankHolidays: BankHolidaySet) = DateTime.now

      override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = ???

      override def auditConnector: AuditConnector = ???

      override def loggingFilter: LoggingFilter = ???

      override def microserviceAuditFilter: AuditFilter = ???

      override def authFilter = None
    }
  }



  "The global object" should {
    "run a task if the lock is available" in new Setup() {

      expectSuccessfulLockConnectorCreate(global.lockConnector)
      expectSuccessfulLockConnectorRelease(global.lockConnector)

      await(global.runTaskIfLockAvailable())

      global.ranTask shouldBe true

      ensureThatLockWasReleased(global.lockConnector)
    }

    "not run a task if the lock is not available" in new Setup() {

      expectFailedLockConnectorCreate(global.lockConnector)

      await(global.runTaskIfLockAvailable())

      global.ranTask shouldBe false
    }
  }

  private def expectSuccessfulLockConnectorCreate(mockLockConnector: LockConnector)= {
    expectLockConnectorCreate(mockLockConnector, true)
  }

  private def expectFailedLockConnectorCreate(mockLockConnector: LockConnector)= {
    expectLockConnectorCreate(mockLockConnector, false)
  }


  private def expectSuccessfulLockConnectorRelease(mockLockConnector: LockConnector)= {
    expectLockConnectorRelease(mockLockConnector, true)
  }

  private def expectFailedLockConnectorRelease(mockLockConnector: LockConnector)= {
    expectLockConnectorRelease(mockLockConnector, false)
  }

  private def expectLockConnectorCreate(mockLockConnector: LockConnector, returnedVerdict: Boolean)= {
    when(mockLockConnector.create(Matchers.eq("worldpay-downloader"))(Matchers.any())).thenReturn(returnedVerdict)
  }

  private def expectLockConnectorRelease(mockLockConnector: LockConnector, returnedVerdict: Boolean)= {
    when(mockLockConnector.release(Matchers.eq("worldpay-downloader"))(Matchers.any())).thenReturn(returnedVerdict)
  }

  private def ensureThatLockWasReleased(mockLockConnector: LockConnector): Unit = {
    verify(mockLockConnector).release(Matchers.eq("worldpay-downloader"))(Matchers.any())
  }



}
