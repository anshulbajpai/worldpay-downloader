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

import java.io.ByteArrayInputStream

import com.jcraft.jsch.{ChannelSftp, JSch}
import com.typesafe.config.ConfigFactory
import config.{EnvironmentConfiguration => EC}
import connectors.{EmisReconcileReportDownloader, SSHConfig}
import metrics.EmisStatusMetricsPublisher
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, Tag}
import play.api.test.{FakeApplication, WithApplication}
import uk.gov.hmrc.play.test.UnitSpec
import unit.{MerchantBuilder, SftpConfiguration}

object Vagrant extends Tag("Vagrant")

class EmisReconcileReportDownloaderVagrantSpec extends UnitSpec with ScalaFutures with BeforeAndAfter with SftpConfiguration with MerchantBuilder {

  "The EmisReconcileReportDownloaderSpec client" should {
    "get EMIS report from Vagrant SFTP Server" taggedAs Vagrant in new WithApplication(FakeApplication(additionalConfiguration = MandatorySftpConfig)) {

      val conf = ConfigFactory.load()

      val sftpSshConfig = SSHConfig(
        user = EC.user,
        port = EC.port,
        host = EC.host,
        privateKey = EC.privateKey,
        knownHosts = EC.knownHosts,
        strictHostKeyChecking = EC.strictHostKeyChecking,
        destinationFolder = EC.destinationFolder,
        privateKeyPassPhrase = EC.privateKeyPassPhrase,
        timeout = EC.timeout)

      val sftp = new SftpConnector(sftpSshConfig)

      val reportBody = """000000000100000000009210000006000000010000000000000000000000000000003334520000000000000000000000000000100000000000000000008000000000000000111050002002
                         |050000000200000001550650000001000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
                         |10000000030000011122933000000110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
                         |1500000004465935******7108   1506000000025000101141158000E041920A                            ADE000
                         |160000000580729310000000000000K1234567890K-1234560
                         |1500000006465935******7108   1506000001234560101141158000E041920A                            ADE000
                         |160000000780729310000000000000V1234567891014-Y5RY0""".stripMargin
      sftp.write(reportBody)

      val downloader = new EmisReconcileReportDownloader(EmisStatusMetricsPublisher(merchants))(sftpSshConfig, new JSch)
      await {
        downloader.withReport(new LocalDate(2014, 9, 5)) { merchantIterator =>
          merchantIterator.length should be(2)
        }
      }

    }
  }
}

class SftpConnector(sshConfig: SSHConfig) {
  val dateFormat = DateTimeFormat.forPattern("ddMMyy")
  val jsch: JSch = new JSch()
  val destinationFolder = "/var/tmp"
  val filePrefix: String = s"MA.PISCESSW.#M.RECON.${EC.companyName}.D"

  def write(report: String) = {
    jsch.addIdentity("id", sshConfig.privateKey.getBytes, null.asInstanceOf[Array[Byte]], sshConfig.privateKeyPassPhrase.getBytes)

    val session = initSession

    val openChannel = session.openChannel("sftp")
    openChannel.connect()
    val channelSftp = openChannel.asInstanceOf[ChannelSftp]

    val fileName = s"$destinationFolder/$filePrefix${dateFormat.print(new LocalDate(2014, 9, 5))}"
    val reportInBytes = new ByteArrayInputStream(report.getBytes)
    channelSftp.put(reportInBytes, fileName)
    reportInBytes.close()

    openChannel.disconnect()
    session.disconnect()
  }

  def initSession = {
    jsch.setKnownHosts(new ByteArrayInputStream(sshConfig.knownHosts.getBytes()))
    val sess = jsch.getSession(sshConfig.user, sshConfig.host, sshConfig.port)
    sess.setConfig("StrictHostKeyChecking", sshConfig.strictHostKeyChecking)
    sess.connect()
    sess
  }
}
