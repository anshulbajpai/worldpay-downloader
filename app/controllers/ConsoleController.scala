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

package controllers

import config.ProdGlobal
import connectors.{LockConnectorUtils, HodsApiConnector, LockConnector}
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.{Play, Logger}
import play.api.mvc.{Result, Action}
import schedule.SchedulingService
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.Play.current
import uk.gov.hmrc.play.microservice.controller.BaseController
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


trait ConsoleController extends BaseController {

  val schedulingService: SchedulingService
  val lockConnector: LockConnector
  val emisReportService: EMISReportService
  val hodsApiConnector: HodsApiConnector

  val processedDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd")

  def getNextScheduleDate() = Action.async { implicit request =>
    Future.successful(schedulingService.getNextScheduleDate().fold(NotFound("No next date is scheduled."))(nextScheduledDate => Ok(nextScheduledDate.toString("dd/MM/yyyy HH:mm"))))
  }


  def getLockStatus() = Action.async { implicit request =>
    lockConnector.exists(ProdGlobal.lockName).map(exists =>
      if (exists) Ok(s"${LockConnector.lockServiceName} exists") else NotFound
    )
  }

  def releaseLock() = Action.async { implicit request =>
    Logger.info("Attempting to release the lock ...")
    lockConnector.release(ProdGlobal.lockName).map(released => {
      Logger.info(s"Released? $released")
      if (released) Ok(s"${LockConnector.lockServiceName} released") else InternalServerError
    }
    )
  }

  def getProcessedStatus(rawDownloadDate: String) = Action.async { implicit request =>
    emisReportService.canSubmitDataset(hodsApiConnector)(LocalDate.parse(rawDownloadDate, processedDateFormat)) map {
      case AllowSubmission => NotFound
      case otherStatus => Logger.info(s"canSubmitDataset: $otherStatus"); Ok
    }
  }

  def downloadAndProcessWithLock(rawDownloadDate: String) = Action.async { implicit request =>

    def task() = {
      EMISReportController.triggerEmisDownloadFor(LocalDate.now)(HeaderCarrier())
    }

    LockConnectorUtils.tryLock[Result](lockConnector, ProdGlobal.lockName, task).map {
      case Some(result) => result
      case None => Conflict
    }
  }

  def initScheduler() = Action.async { implicit request =>
    Logger.info("Re-initialising Scheduler ...")
    ProdGlobal.initSchedule(Play.application)
    Future.successful(Ok(""))
  }

}

object ConsoleController extends ConsoleController {
  override val schedulingService = SchedulingService
  override val lockConnector = LockConnector
  override val emisReportService = EMISReportService
  override val hodsApiConnector = EMISReportController.hodsApiConnector

}
