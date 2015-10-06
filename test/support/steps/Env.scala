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

import java.io.File
import java.nio.file.{StandardOpenOption, Files}

import com.github.tomakehurst.wiremock.client.WireMock
import cucumber.api.Scenario
import cucumber.api.scala.{EN, ScalaDsl}
import fun.FeatureSuite
import org.junit.BeforeClass


object Env extends ScalaDsl with EN with Stubs {

  var hostPost = 9000
  var host =  s"http://localhost:$hostPost"

  val stubPort = 11111
  val stubHost = "localhost"

  lazy val sftpRoot = Files.createTempDirectory("sftp-root").toFile
  lazy val logFile = new File("logs", "worldpay-downloader.log")


  Before { scenario =>
    FeatureSuite.ensureSetup
    markScenarioStartInLogFile(scenario)
    stubFor(Auditing)
  }

  After { scenario =>
    WireMock.reset()
  }

  private def markScenarioStartInLogFile(scenario: Scenario) = {
    Files.write(logFile.toPath, s"\n\n== SCENARIO: ${scenario.getName} ==\n\n".getBytes, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
  }


}
