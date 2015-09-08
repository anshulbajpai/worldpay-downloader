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

import com.jcraft.jsch.{ChannelSftp, JSch, Session}
import connectors.EmisReconcileReportDownloader
import metrics.EmisStatusMetricsPublisher
import org.joda.time.LocalDate
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.{Future, Promise}
import scala.util.Try

class EmisReconcileReportDownloaderSpec extends UnitSpec with ScalaFutures with MockitoSugar with Eventually with IntegrationPatience {

  "EmisReconcileReportDownloader " should {


    "take the timeout from config" in {
      val jsch = mock[JSch]
      val emisStatusMetricsPublisher = mock[EmisStatusMetricsPublisher]
      val mockSession = mock[Session]
      val mockChannelSftp = mock[ChannelSftp]

      when(jsch.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession)
      when(mockSession.openChannel("sftp")).thenReturn(mockChannelSftp)

      val downloader = new EmisReconcileReportDownloader(emisStatusMetricsPublisher)(fakeSshConfig, jsch)

      downloader.withReport(LocalDate.now)(_ => Future.successful(()))

      eventually {
        verify(mockChannelSftp).connect(100000)
      }
    }

    "wait for completion of the report processing before closing the session" in {
      val jsch = mock[JSch]
      val emisStatusMetricsPublisher = mock[EmisStatusMetricsPublisher]
      val mockSession = mock[Session]
      val mockChannelSftp = mock[ChannelSftp]

      when(jsch.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession)
      when(mockSession.openChannel("sftp")).thenReturn(mockChannelSftp)

      val downloader = new EmisReconcileReportDownloader(emisStatusMetricsPublisher)(fakeSshConfig, jsch)

      val p: Promise[Unit] = Promise[Unit]()
      val future: Future[Unit] = p.future

      downloader.withConnectedSession(longRunningFuture(future))
      verify(mockChannelSftp, times(0)).disconnect()
      verify(mockSession, times(0)).disconnect()

      p.complete(Try(()))
      await(future)

      eventually {
        verify(mockChannelSftp, times(1)).disconnect()
        verify(mockSession, times(1)).disconnect()
      }
    }
  }

  def longRunningFuture(f:Future[Unit])(session: Session, channel:ChannelSftp) = {
    f
  }
}
