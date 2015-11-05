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

import java.util.UUID

import com.jcraft.jsch.JSch
import com.kenshoo.play.metrics.MetricsRegistry
import config.{EnvironmentConfiguration => EC}
import connectors.{EmisReconcileReportDownloader, HodsApiConnector, SSHConfig}
import metrics.EmisStatusMetricsPublisher
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import parsers.StreamingEmisReportParser.MerchantTransaction
import play.api.Logger
import play.api.mvc.{Action, Result}
import transformer.EmisReportTransformer
import uk.gov.hmrc.play.config.{AppName, ServicesConfig}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


object EMISReportController extends EMISReportController with ServicesConfig {

  override val emisStatusMetricsPublisher: EmisStatusMetricsPublisher = EC.emisStatusMetricsPublisher
  override val emisReportTransformer =  EC.emisReportTransformer
  override val emisDownloader = new EmisReconcileReportDownloader(emisStatusMetricsPublisher)(
    SSHConfig(
      user = EC.user,
      port = EC.port,
      host = EC.host,
      privateKey = EC.privateKey,
      knownHosts = EC.knownHosts,
      strictHostKeyChecking = EC.strictHostKeyChecking,
      destinationFolder = EC.destinationFolder,
      privateKeyPassPhrase = EC.privateKeyPassPhrase,
      timeout = EC.timeout),
    new JSch)

  override val hodsApiConnector = new HodsApiConnector {
    override val rcsPaymentsUrl = baseUrl("hodsapi")
    override val http = new WSGet with WSPost with WSDelete with AppName {
      override val hooks = NoneRequired
    }
    override val metricsRegistry = MetricsRegistry.defaultRegistry
  }

}

trait EMISReportController extends BaseController {

  val emisStatusMetricsPublisher: EmisStatusMetricsPublisher
  val emisReportTransformer: EmisReportTransformer
  val emisDownloader: EmisReconcileReportDownloader
  val hodsApiConnector: HodsApiConnector
  val downloadDateFormat = DateTimeFormat.forPattern("yyyyMMdd")

  def download(rawDownloadDate: String, chunkSizeOpt: Option[Int], force: Option[Boolean]) = Action.async { implicit request =>
    triggerEmisDownloadFor(LocalDate.parse(rawDownloadDate, downloadDateFormat), chunkSizeOpt.getOrElse(EC.chunkSize), force.getOrElse(false))(HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session)))
  }

  def triggerEmisDownloadFor(downloadDate: LocalDate, chunkSize: Int = EC.chunkSize, force: Boolean = false)(hc: HeaderCarrier): Future[Result] = {
    emisStatusMetricsPublisher.started()

    implicit val headerCarrier =  hc.withExtraHeaders(("X-Session-ID", s"session-${UUID.randomUUID()}"))

    if (force) {
      Logger.warn("EMIS report processing was forced")
      triggerDownload(downloadDate, chunkSize)
    } else {
      EMISReportService.canSubmitDataset(hodsApiConnector)(downloadDate) flatMap {
        case AllowSubmission => triggerDownload(downloadDate, chunkSize)
        case PreventSubmission =>
          val errorMessage = s"Not getting EMIS report as RCS told us data set for $downloadDate has already been released"
          Logger.error(errorMessage)
          Future.successful(BadRequest)
        case UnknownStatus(dataset) =>
          val errorMessage = s"Not processing EMIS report due to unexpected status '${dataset.status}' returned from existing dataset: ${dataset.dataSetID}"
          Logger.error(errorMessage)
          Future.successful(BadRequest)
      }
    }
  }

  private def triggerDownload(downloadDate: LocalDate, chunkSize: Int)(implicit hc: HeaderCarrier): Future[Result] = {

    emisDownloader.withReport(downloadDate) { emisReport =>
      val publishedReportContents = publishEmisStats(emisReport)
      val chunks = emisReportTransformer.toRcsChunks(publishedReportContents, chunkSize)

      if (chunks.hasNext) {
        val work = hodsApiConnector.sendPayments(UUID.randomUUID().toString, chunks)

        work.onComplete {
          case Success(_) => Logger.info("Completed the processing of the report")
          case Failure(e) => Logger.error("Emis report processing failed", e)
        }

        work
      } else {
        Future.successful(())
      }
    }.map{ _ =>
      emisStatusMetricsPublisher.completedSuccessfully()
      Accepted
    }
  }

  private def publishEmisStats(emiReportContent: Iterator[MerchantTransaction]): Iterator[MerchantTransaction] = {

    emiReportContent.filter { merchantTransaction =>
      emisStatusMetricsPublisher.merchants.merchantIdSupported(merchantTransaction.merchant.merchantId)
    }.map { merchantTransaction =>
      emisStatusMetricsPublisher.reportSalesValue(merchantTransaction.merchant.merchantId, merchantTransaction.merchant.acceptedSalesValue)
      emisStatusMetricsPublisher.reportSalesCount(merchantTransaction.merchant.merchantId, merchantTransaction.merchant.acceptedSalesCount)
      merchantTransaction
    }
  }
}
