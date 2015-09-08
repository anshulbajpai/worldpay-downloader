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

package unit.schedule

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import org.joda.time.DateTime
import org.scalatest._
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import schedule.SchedulingService

class SchedulingServiceSpec extends WordSpec with ShouldMatchers with ScalaFutures with Eventually {


  "The scheduling service" should {

    "execute a task at pre-defined intervals" in {
      val scheduler = ActorSystem("scheduling").scheduler

      val nextExecutions = Seq(DateTime.now, DateTime.now.plusMillis(1), DateTime.now.plusDays(1), DateTime.now.plusDays(2)).iterator

      def runImmediatelyOnce = () => nextExecutions.next()

      val invokes = new AtomicInteger()

      SchedulingService.scheduleNextJob(scheduler, runImmediatelyOnce) {
        invokes.incrementAndGet()
      }

      eventually {
        invokes.get should be(2)
      }
    }

  }
}
