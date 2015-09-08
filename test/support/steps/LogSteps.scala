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

import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.ShouldMatchers
import org.scalatest.concurrent.ScalaFutures

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

class LogSteps extends ScalaDsl with EN with Stubs with ScalaFutures with ShouldMatchers with LogFinder {


  Then("""^I see '(.+)' in the logs at (INFO|WARN|ERROR) level$"""){ (msg: String, level: String) =>
    messageShouldBeInTheLogSince(msg, level)(ScenarioStartLine)
  }

  Then("""^I see '(.+)' in the logs since the start of the application at (INFO|WARN|ERROR) level$"""){ (msg: String, level: String) =>
    messageShouldBeInTheLogSince(msg, level)(ApplicationStartLine)
  }

}

trait LogFinder extends ShouldMatchers {
  val ScenarioStartLine: ((String) => Boolean) = _.startsWith("== SCENARIO")
  val ApplicationStartLine: ((String) => Boolean) = _.startsWith("Application started")

  def messageShouldBeInTheLogSince(msg: String, level: String)(p: (String) => Boolean) = {
    val logLines = Source.fromFile(Env.logFile.getPath).getLines().toStream.reverse
    println(logLines)
    val lineFound = logLines.takeWhile(!p(_)).exists { line =>
      line.contains(s"level=[$level]") && line.contains(msg)
    }

    withClue(s"Couldn't find '$msg' at $level in the log") {
      lineFound should be(true)
    }
  }
}
