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

import java.io.{ByteArrayInputStream, InputStream}

import com.jcraft.jsch._
import config.EnvironmentConfiguration._
import exceptions.ReportDownloaderException
import metrics.EmisStatusMetricsPublisher
import org.joda.time.LocalDate
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import parsers.StreamingEmisReportParser.MerchantTransaction
import parsers.{EmisAuditingIterator, EmisLoggingIterator, StreamingEmisReportParser}
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source.fromInputStream
import scala.util.Try

case class SSHConfig(host: String,
                     port: Int,
                     timeout: Int,
                     user: String,
                     privateKey: String,
                     knownHosts: String,
                     strictHostKeyChecking: String,
                     destinationFolder: String,
                     privateKeyPassPhrase:String)

class EmisReconcileReportDownloader(emisStatusMetricsPublisher: EmisStatusMetricsPublisher)(sshConfig: SSHConfig, jSch: JSch) {

  try {
    jSch.addIdentity("id", sshConfig.privateKey.getBytes, null, sshConfig.privateKeyPassPhrase.getBytes)
  } catch {
    case e: JSchException => throw new JSchException(s"Private key: ${sshConfig.privateKey}", e)
  }
  jSch.setKnownHosts(new ByteArrayInputStream(sshConfig.knownHosts.getBytes))

  val piscesDateFormat = DateTimeFormat.forPattern("ddMMyy")

  def withReport(date: LocalDate)(reportProcessor : (Iterator[MerchantTransaction]) => Future[Unit]): Future[Unit] = Future {
    withConnectedSession { (session, channelSftp) =>
      reportProcessor {
        StreamingEmisReportParser {
          EmisLoggingIterator(date, emisStatusMetricsPublisher) {
            EmisAuditingIterator {
              fetchEmisReport(date, channelSftp, session)
            }
          }
        }
      }
    }
  }

  def withConnectedSession[A](fun: (Session, ChannelSftp) => Future[A]): Unit = {
    val session = jSch.getSession(sshConfig.user, sshConfig.host, sshConfig.port)
    session.setConfig("StrictHostKeyChecking", sshConfig.strictHostKeyChecking)
    val channelSftp = sessionConnect(session)
    channelConnect(session, channelSftp, sshConfig.timeout)
    fun(session, channelSftp).onComplete{ _ =>
      Logger.info("Completed channelSftp usage")
      session.disconnect()
      channelSftp.disconnect()
    }
  }

  private def fetchEmisReport(date: LocalDate, channelSftp: ChannelSftp, session:Session): Iterator[String] = {
    val fullPathToFile = s"${sshConfig.destinationFolder}/${buildFilenameFrom(piscesDateFormat, date)}"
    fromInputStream(obtainStream(session, channelSftp, fullPathToFile)).getLines()
  }

  private def buildFilenameFrom(formatter: DateTimeFormatter, date: LocalDate) = s"MA.PISCESSW.#M.RECON.${companyName}.D${formatter.print(date)}"

  private def channelConnect(session:Session, channelSftp: ChannelSftp, timeout: Int) =
    executeAndHandle[Unit, JSchException](session)(channelSftp.connect(timeout), "Could not connect to SFTP")

  private def sessionConnect(session: Session): ChannelSftp = executeAndHandle[ChannelSftp, JSchException](session)({
    session.connect()
    session.openChannel("sftp").asInstanceOf[ChannelSftp]
  }, "Could not connect to SFTP")

  private def obtainStream(session:Session, channelSftp: ChannelSftp, fullPathToFile: String): InputStream =
    executeAndHandle[InputStream, SftpException](session)(channelSftp.get(fullPathToFile), "SFTP download error")

  private def executeAndHandle[T, U](session:Session)(fun: => T, msg: String)(implicit m: Manifest[U]): T = Try {
    fun
  }.recover {
    case e: U =>
      val message = s"$msg: ${e.getMessage}"
      Logger.error(message)
      throw new ReportDownloaderException(message, e)
    case t =>
      Logger.error(s"Unexpected error when using SFTP connection info: server:'${session.getHost}', port:'${session.getPort}', user:'${session.getUserName}': error: ${t.getMessage}", t)
      throw t
  }.get
}
