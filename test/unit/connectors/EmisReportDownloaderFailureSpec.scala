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

import com.jcraft.jsch._
import connectors.EmisReconcileReportDownloader
import exceptions.ReportDownloaderException
import metrics.EmisStatusMetricsPublisher
import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{ShouldMatchers, WordSpec}
import parsers.StreamingEmisReportParser.MerchantTransaction
import play.api.test.{FakeApplication, WithApplication}
import unit.{WorldpayMerchantConfiguration, SftpConfiguration}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmisReportDownloaderFailureSpec extends WordSpec with ShouldMatchers with MockitoSugar with ScalaFutures with SftpConfiguration with WorldpayMerchantConfiguration {

  val allMandatoryConfig = MandatorySftpConfig ++ MandatoryMerchantConfig
  val mockJshc = mock[JSch]
  val mockSession = mock[Session]
  val emisStatusMetricsPublisher = mock[EmisStatusMetricsPublisher]
  val mockChannel = mock[ChannelSftp]
  val defaultReportProcessor = (a: Iterator[MerchantTransaction]) => Future(())

  "EMISReportDownloader" should {

    "should handle an SFTP server being down" in new WithApplication(FakeApplication(additionalConfiguration = allMandatoryConfig))  {
      //arrange
      val mockSession = mock[Session]

      when(mockJshc.getSession(fakeSshConfig.user, fakeSshConfig.host, fakeSshConfig.port)).thenReturn(mockSession)
      when(mockSession.connect()).thenThrow(new JSchException("No route to host"))

      //act
      val e = fakeEmisReconcileReportDownloader.withReport(LocalDate.now())(defaultReportProcessor).failed.futureValue

      //assert
      e.isInstanceOf[ReportDownloaderException] shouldBe true
      e.getMessage shouldBe "Could not connect to SFTP: No route to host"
    }

    "should handle an EMIS report not available on SFTP Server" in new WithApplication(FakeApplication(additionalConfiguration = allMandatoryConfig))  {
      //arrange
      val mockSession = mock[Session]
      val mockChannel = mock[ChannelSftp]

      when(mockJshc.getSession(fakeSshConfig.user, fakeSshConfig.host, fakeSshConfig.port)).thenReturn(mockSession)
      when(mockSession.openChannel("sftp")).thenReturn(mockChannel)
      when(mockChannel.get(anyString())).thenThrow(new SftpException(ChannelSftp.SSH_FX_NO_SUCH_FILE, "No such file"))

      //act
      val e = fakeEmisReconcileReportDownloader.withReport(LocalDate.now())(defaultReportProcessor).failed.futureValue

      //assert
      e.isInstanceOf[ReportDownloaderException] shouldBe true
      e.getMessage shouldBe "SFTP download error: No such file"
    }

    "should handle timeout on connecting to SFTP Server" in new WithApplication(FakeApplication(additionalConfiguration = allMandatoryConfig))  {
      //arrange
      val mockSession = mock[Session]
      val mockChannel = mock[ChannelSftp]

      when(mockJshc.getSession(fakeSshConfig.user, fakeSshConfig.host, fakeSshConfig.port)).thenReturn(mockSession)
      when(mockSession.openChannel(Matchers.any())).thenReturn(mockChannel)
      when(mockChannel.connect(Matchers.any())).thenThrow(new JSchException("Timeout message"))

      //act
      val e = fakeEmisReconcileReportDownloader.withReport(LocalDate.now())(defaultReportProcessor).failed.futureValue

      //assert
      e.isInstanceOf[ReportDownloaderException] shouldBe true
      e.getMessage shouldBe "Could not connect to SFTP: Timeout message"
    }

    "should handle exceptions other than jsch and sftp exceptions" in new WithApplication(FakeApplication(additionalConfiguration = allMandatoryConfig)) {
      //arrange
      val mockSession = mock[Session]

      when(mockJshc.getSession(fakeSshConfig.user, fakeSshConfig.host, fakeSshConfig.port)).thenReturn(mockSession)
      when(mockSession.connect()).thenThrow(new RuntimeException("Boom!"))

      //act
      val e = fakeEmisReconcileReportDownloader.withReport(LocalDate.now())(defaultReportProcessor).failed.futureValue

      //assert
      e.isInstanceOf[RuntimeException] shouldBe true
      e.getMessage shouldBe "Boom!"
    }

  }

  val fakeEmisReconcileReportDownloader = new EmisReconcileReportDownloader(emisStatusMetricsPublisher)(fakeSshConfig, mockJshc)

}
