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

import org.joda.time.LocalDate
import scala.concurrent.Future
import play.api.Logger

import uk.gov.hmrc.play.audit.http.HeaderCarrier

import connectors.HodsApiConnector
import model.RcsDataSet

import scala.concurrent.ExecutionContext.Implicits.global


trait EMISReportService {

  private val AllowSubmissionStatus = Seq("Open", "Deleted")
  private val PreventSubmissionStatus = Seq("Released", "Sending", "Sent", "Failed Recon", "Tech Failure")

  def canSubmitDataset(hodsApiConnector: HodsApiConnector)(downloadDate: LocalDate)(implicit hc: HeaderCarrier): Future[RcsDatasetStatus] = {
    hodsApiConnector.listDataSets(downloadDate, downloadDate, hc).map {
      case dss if dss.isEmpty => AllowSubmission
      case dss if dss.forall(ds => AllowSubmissionStatus.contains(ds.status)) =>
        dss.filter(_.status == "Open") map { ds =>
          val delResponseF = hodsApiConnector.deleteDataset(ds.dataSetID, hc)
          delResponseF.onFailure {
            case e => Logger.warn(s"Could not delete dataset: ${ds.dataSetID}")
          }
        }
        AllowSubmission
      case dss if dss.exists(ds => PreventSubmissionStatus.contains(ds.status)) => PreventSubmission
      case dss =>
        val unknownStatusDataset = dss.filterNot(ds => AllowSubmissionStatus.contains(ds.status) || PreventSubmissionStatus.contains(ds.status)).head
        UnknownStatus(unknownStatusDataset)
    }
  }
}

object EMISReportService extends EMISReportService

sealed trait RcsDatasetStatus

object AllowSubmission extends RcsDatasetStatus
object PreventSubmission extends RcsDatasetStatus
final case class UnknownStatus(dataset: RcsDataSet) extends RcsDatasetStatus
