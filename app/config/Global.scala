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

package config

import com.typesafe.config.Config
import connectors.{BankHolidaysConnector, LockConnector, LockConnectorUtils}
import controllers.EMISReportController
import net.ceedubs.ficus.Ficus._
import org.joda.time.{DateTime, LocalDate}
import play.api.libs.concurrent.Akka
import play.api.{Application, Configuration, Logger, Play}
import schedule.{Schedules, SchedulingService}
import uk.gov.hmrc.play.audit.filters.AuditFilter
import uk.gov.hmrc.play.audit.http.HeaderCarrier
import uk.gov.hmrc.play.config.{AppName, ControllerConfig}
import uk.gov.hmrc.play.http.logging.filters.LoggingFilter
import uk.gov.hmrc.play.microservice.bootstrap.DefaultMicroserviceGlobal
import uk.gov.hmrc.time.workingdays.BankHolidaySet

import scala.concurrent.Future

object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
}

object MicroserviceAuditFilter extends AuditFilter with AppName {
  override val auditConnector = MicroserviceAuditConnector
  override def controllerNeedsAuditing(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsAuditing
}

object MicroserviceLoggingFilter extends LoggingFilter {
  override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

trait WorldPayDownloaderGlobal extends DefaultMicroserviceGlobal {

  import scala.concurrent.ExecutionContext.Implicits.global

  val lockName = "worldpay-downloader"

  implicit val emptyHeaderCarrier = HeaderCarrier()

  def nextExecution(implicit bankHolidays: BankHolidaySet): () => DateTime
  def task(): Future[Unit]

  val lockConnector: LockConnector

  def runTaskIfLockAvailable(): Future[Option[Unit]] = LockConnectorUtils.tryLock[Unit](lockConnector,lockName, task)

  override def onStart(app: Application): Unit = {
    super.onStart(app)

    initSchedule(app)

    EnvironmentConfiguration.verify()
  }

  def initSchedule(app: Application): Unit = {
    BankHolidaysConnector.bankHolidaysForEnglandAndWales().map {bankHolidays =>
      Logger.info(s"Bank Holidays received, so setting schedule ...")
      SchedulingService.scheduleNextJob(Akka.system(app).scheduler, nextExecution(bankHolidays)) {
        runTaskIfLockAvailable()
      }
      Logger.info("Schedule successfully initiated.")
    }.recover {
      case ex => Logger.error("Failed to get the Bank Holidays, so failed to set Scheduler. Must re-deploy.", ex)
    }
  }
}


object ProdGlobal extends WorldPayDownloaderGlobal {

  import scala.concurrent.ExecutionContext.Implicits.global

  override lazy val lockConnector = LockConnector

  override def nextExecution(implicit bankHolidays: BankHolidaySet) = { () =>
    Schedules.next10amLondonTimeInAWorkingDay(DateTime.now)
  }

  override def task() = {
    Logger.info("EMIS report download automatically triggered")

    EMISReportController.triggerEmisDownloadFor(LocalDate.now)(HeaderCarrier()) map { result =>
      Logger.info("EMIS report automatic download task finished")
    }
  }

  override val auditConnector = MicroserviceAuditConnector

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig("microservice.metrics")

  override val loggingFilter = MicroserviceLoggingFilter

  override val microserviceAuditFilter = MicroserviceAuditFilter

  override val authFilter = None

}
