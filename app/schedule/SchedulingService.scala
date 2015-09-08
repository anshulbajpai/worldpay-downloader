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

package schedule

import java.util.concurrent.TimeUnit

import akka.actor.Scheduler
import org.joda.time.DateTime
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

trait SchedulingService {

  var nextDateTimeOpt: Option[DateTime] = None

  def scheduleNextJob(scheduler: Scheduler, nextExecution: () => DateTime)(task: => Unit): Unit = {

    nextDateTimeOpt = Some(nextExecution())

    nextDateTimeOpt.map(nextDateTime => {
      Logger.info("Next download scheduled for " + nextDateTime.toString("dd/MM/yyyy HH:mm"))
      val millisUntilNextExecution = nextDateTime.getMillis - DateTime.now().getMillis
      scheduler.scheduleOnce(FiniteDuration(millisUntilNextExecution, TimeUnit.MILLISECONDS)) {
        // Run the task first (immediately)
        Logger.info("Now running the scheduled task.")
        scheduler.scheduleOnce(FiniteDuration(0, TimeUnit.MILLISECONDS)) { task }
        // Then schedule the next occurrence
        Logger.info("Setting the next scheduled run ...")
        scheduleNextJob(scheduler, nextExecution)(task)
      }
    })
  }

  def getNextScheduleDate(): Option[DateTime] = nextDateTimeOpt
}

object SchedulingService extends SchedulingService
