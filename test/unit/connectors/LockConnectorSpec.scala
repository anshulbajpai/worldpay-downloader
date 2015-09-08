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

package unit.connectors

import config.WorldPayDownloaderGlobal
import connectors.{LockConnectorUtils, LockConnector}
import org.joda.time.DateTime
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.{Configuration, Application}
import uk.gov.hmrc.play.audit.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.time.workingdays.BankHolidaySet

import scala.concurrent.Future


class LockConnectorSpec extends UnitSpec with MockitoSugar {


  trait Setup {

    val LockBlue = "blue"
    val LockPurple = "purple"

    implicit val headerCarrier = HeaderCarrier()
    implicit val lockConnector = new LockConnector {
        override val http = mock[WSHttp]
        override val lockServiceUrl = "https://lock-service-host"
      }

    var ranTask = false

    def task = {
      ranTask = true
      Future.successful[Unit](())
    }
  }





  "Querying for a lock" should {

    "return false, when it doesn't exist" in new Setup() {

      expectGet(LockBlue, 404)

      val verdict = await(lockConnector.exists(LockBlue))

      verdict shouldBe false

    }

    "return true, when it exists" in new Setup() {

      expectGet(LockPurple, 200)

      val verdict = await(lockConnector.exists(LockPurple))

      verdict shouldBe true
    }

  }

  "Attempting to create a lock" should {

    "return false, if a lock by the same name already exists." in new Setup()  {

      expectPost(LockPurple, 409)

      val verdict = await(lockConnector.create(LockPurple))

      verdict shouldBe false
    }

    "return true, if a lock by the same name doesn't exist." in new Setup()  {

      expectPost(LockPurple, 201)

      val verdict = await(lockConnector.create(LockPurple))

      verdict shouldBe true
    }
  }

  "Attempting to release a lock" should {

    "return false, if a lock by the same name doesn't exist." in new Setup()  {

      expectDelete(LockPurple, 404)

      val verdict = await(lockConnector.release(LockPurple))

      verdict shouldBe false
    }

    "return true, if a lock by the same name already exists." in new Setup()  {

      expectDelete(LockPurple, 204)

      val verdict = await(lockConnector.release(LockPurple))

      verdict shouldBe true
    }
  }

  "Trying a lock" should {

    "should execute the supplied task, if the lock is available, and release it immediately afterwards" in new Setup() {

      expectPost(LockBlue, 201)
      expectDelete(LockBlue, 204)

      await(LockConnectorUtils.tryLock(lockConnector,LockBlue,task))

      ranTask shouldBe true

    }

    "should not execute the supplied task, if the lock is unavailable" in new Setup() {

      expectPost(LockBlue, 409)

      await(LockConnectorUtils.tryLock(lockConnector,LockBlue,task))

      ranTask shouldBe false

    }

  }

  private def expectGet(lockName: String, returnedStatusCode : Int)(implicit lockConnector: LockConnector, headerCarrier: HeaderCarrier) = {
    when(lockConnector.http.doGet(Matchers.eq(generateUrl(lockName)))(Matchers.eq(headerCarrier))).thenReturn(Future.successful(HttpResponse(returnedStatusCode, None)))
  }


  private def expectPost(lockName: String, returnedStatusCode : Int)(implicit lockConnector: LockConnector, headerCarrier: HeaderCarrier)  = {
    when(lockConnector.http.doEmptyPost(Matchers.eq(generateUrl(lockName)))(Matchers.eq(headerCarrier))).thenReturn(Future.successful(HttpResponse(returnedStatusCode, None)))
  }

  private def expectDelete(lockName: String, returnedStatusCode : Int)(implicit lockConnector: LockConnector, headerCarrier: HeaderCarrier)  = {
    when(lockConnector.http.doDelete(Matchers.eq(generateUrl(lockName)))(Matchers.eq(headerCarrier))).thenReturn(Future.successful(HttpResponse(returnedStatusCode, None)))
  }

  private def generateUrl(lockName: String) = s"https://lock-service-host/locks/$lockName"

}
