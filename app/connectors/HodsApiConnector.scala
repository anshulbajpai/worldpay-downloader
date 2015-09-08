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

import java.util.UUID

import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{ExecutionContext, Future}

import com.codahale.metrics.MetricRegistry
import org.joda.time.LocalDate
import play.api.libs.json.Json

import uk.gov.hmrc.play.http.{HttpDelete, HttpGet, HttpPost, HttpResponse}

import model.{ChunkRelease, RcsChunk, RcsDataSet}
import uk.gov.hmrc.play.audit.http.HeaderCarrier
import uk.gov.hmrc.play.http.HttpReads.readSeqFromJsonProperty



object Connector {
  def expectStatus(description: String, expected: Int)(response: HttpResponse): Unit = {
    val actual = response.status
    if (actual != expected) throw new IllegalStateException(s"Expected the $description call to return $expected, was $actual")
  }

  def transmit(initF: => Future[HttpResponse],
               chunkSeq: => Iterator[() => Future[(HttpResponse, ChunkRelease)]],
               releaseF: => (Seq[ChunkRelease]) => Future[HttpResponse])(implicit ec: ExecutionContext): Future[Unit] ={

    for {
      init <- initF.map(expectStatus("initiation", 200))
      chunkReleases <- sequence(chunkSeq.map(expect202).map(extractChunkRelease))
      release <- releaseF(chunkReleases).map(expectStatus("release", 200))
    } yield()
  }

  private def expect202[A](fn: () => Future[(HttpResponse,A)]): () => Future[(HttpResponse, A)] = {
    () => fn.apply().map { chunk =>
      expectStatus("chunk", 202)(chunk._1)
      chunk
    }
  }

  def extractChunkRelease[A,B](fn: () => Future[(A,B)]): () => Future[B] = {
    () => fn.apply().map(_._2)
  }

  private def sequence[A](futures: Iterator[() => Future[A]]): Future[Seq[A]] = {
    futures.foldLeft(Future.successful(Seq.empty[A])) {(resultF, futureFactory) =>
      for {result <- resultF
           a <- futureFactory.apply()
      } yield result :+ a
    }
  }
}

trait HodsApiConnector {
  val http: HttpPost with HttpGet with HttpDelete
  val rcsPaymentsUrl: String
  val metricsRegistry: MetricRegistry

  def sendPayments(datasetId: String, rcsChunks: Iterator[(RcsChunk, ChunkRelease)])(implicit hc: HeaderCarrier):Future[Unit] ={
    Connector.transmit(
      rcsInitiation(datasetId ,hc),
      rcsChunks.zipWithIndex.map {
        case (rcsChunkAndRelease, index) => () =>
          rcsTransmission(index + 1, datasetId, rcsChunkAndRelease ,hc)
      },
      rcsRelease(datasetId, hc)
    )
  }

  def deleteDataset(datasetId: String, hc: HeaderCarrier) = {
    implicit val headerCarrier = addRequestIdHeader(hc)
    http.DELETE(s"$rcsPaymentsUrl/payments/worldpay/$datasetId")
  }


  def listDataSets(fromDate: LocalDate, toDate: LocalDate, hc: HeaderCarrier):Future[Seq[RcsDataSet]] = {
    implicit val headerCarrier = addRequestIdHeader(hc)
    http.GET[Seq[RcsDataSet]](s"$rcsPaymentsUrl/payments/worldpay?fromDate=$fromDate&toDate=$toDate")(readSeqFromJsonProperty("dataSets"), headerCarrier)//, "dataSets")
  }

  private def rcsInitiation(dataSetId: String, hc: HeaderCarrier): Future[HttpResponse] = {
    timed("RcsInitiationResponseTime") {
      implicit val headerCarrier = addRequestIdHeader(hc)
      http.POST(s"$rcsPaymentsUrl/payments/worldpay/$dataSetId", "")
    }
  }

  private def rcsTransmission(index:Int, dataSetId: String, rcsChunk: (RcsChunk, ChunkRelease), hc: HeaderCarrier): Future[(HttpResponse, ChunkRelease)] = {
    timed("RcsChunkResponseTime") {
      implicit val headerCarrier = addRequestIdHeader(hc)
      http.POST(s"$rcsPaymentsUrl/payments/worldpay/$dataSetId/chunks/$index", Json.toJson(rcsChunk._1)).map { r => r -> rcsChunk._2}
    }
  }

  private def rcsRelease(dataSetId: String, hc: HeaderCarrier)(releaseSummary: Seq[ChunkRelease]): Future[HttpResponse] = {
    timed("RcsReleaseResponseTime") {
      implicit val headerCarrier = addRequestIdHeader(hc)
      http.POST(s"$rcsPaymentsUrl/payments/worldpay/$dataSetId/release", Json.toJson(Map("chunks" -> releaseSummary)))
    }
  }

  private def timed[T](timerName: String)(body: => Future[T]): Future[T] = {
    val responseTimer = metricsRegistry.timer(timerName).time()
    val f = body
    f.onComplete { tryHttpResponse =>
      responseTimer.stop()
    }
    f
  }

  private def addRequestIdHeader(hc: HeaderCarrier): HeaderCarrier = {
    hc.withExtraHeaders(("X-Request-ID", s"govuk-tax-${UUID.randomUUID().toString}"))
  }
}
