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

package connectors

import config.WSHttp
import org.apache.http.HttpStatus
import play.api.Logger
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.ws.{WSDelete, WSPost, WSGet, WSHttp}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait LockConnector extends ServicesConfig {

  trait WSHttpType extends scala.AnyRef with uk.gov.hmrc.play.http.ws.WSGet

  val http: WSGet with WSPost with WSDelete
  val lockServiceUrl : String

  def exists(name: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    http.doGet(generateUrl(name)).map(_.status == HttpStatus.SC_OK)

  def create(name: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    http.doEmptyPost(generateUrl(name)).map(_.status == HttpStatus.SC_CREATED)

  def release(name: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    http.doDelete(generateUrl(name)).map(_.status == HttpStatus.SC_NO_CONTENT)



  private def generateUrl(name: String) = s"$lockServiceUrl/locks/$name"

}

object LockConnector extends LockConnector {

  val lockServiceName = "lock"

  override val http = WSHttp
  override val lockServiceUrl = baseUrl(lockServiceName)
}

object LockConnectorUtils {

  def tryLock[T](lockConnector: LockConnector, lockName: String, task: => Future[T])(implicit headerCarrier: HeaderCarrier): Future[Option[T]] = {
    Logger.info(s"Checking for the lock named: $lockName")
    lockConnector.create(lockName).flatMap { successfullyCreated => {
    if (successfullyCreated) {
      Logger.info("Yes, this lock has been granted.")
      task.flatMap { case x => {
        Logger.info("Now attempting to release this lock ...")
        lockConnector.release(lockName).map(successfullyReleased => {
          if(successfullyReleased) Logger.info("... successfully release.")
          else Logger.info("... release failed!")
          Some(x)})
      } }
    }
    else {
      Logger.info("*No* - this lock was not granted.")
      Future.successful(None)
    }
  }.recoverWith {
    case ex => {
      Logger.error("We got an exception from the 'lock service'",ex)
      lockConnector.release(lockName).flatMap { successfullyReleased =>
        if(successfullyReleased) Logger.info("... successfully release.")
        else Logger.info("... release failed!")
        Future.failed(ex) }}
  }
  }}


}
