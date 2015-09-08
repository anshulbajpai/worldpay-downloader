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

import java.util.concurrent.ConcurrentLinkedQueue

import com.codahale.metrics.MetricRegistry
import com.kenshoo.play.metrics.MetricsRegistry
import connectors.{Connector, HodsApiConnector}
import model.{ChunkRelease, RcsChunk}
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Writes
import play.api.test.{FakeApplication, WithApplication}
import uk.gov.hmrc.play.audit.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config.RunMode
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.test.UnitSpec
import unit.{SftpConfiguration, WorldpayMerchantConfiguration}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HodsApiConnectorSpec extends UnitSpec with Eventually with MockitoSugar with SftpConfiguration with WorldpayMerchantConfiguration with IntegrationPatience {

  val allMandatoryConfig = MandatorySftpConfig ++ MandatoryMerchantConfig

  "Connector.transmit" should {

    val chunkRelease = ChunkRelease("", List())


    trait Setup {
      val order = new ConcurrentLinkedQueue[String]()

      def httpResponse(delay: Int, rank: String, response: Int) = Future {
        Thread.sleep(delay)
        order.add(rank)
        HttpResponse(response, None)
      }
      def httpResponseWithChunk(delay: Int, rank: String, response: Int) = Future {
        Thread.sleep(delay)
        order.add(rank)
        (HttpResponse(response, None), chunkRelease)
      }
    }

    "execute the functions in order" in new Setup {
      await(
        Connector.transmit(httpResponse(100, "1", 200),
          Iterator(() => httpResponseWithChunk(50, "2", 202), () => httpResponseWithChunk(10, "3", 202), () => httpResponseWithChunk(2, "4", 202)), (chunks) => httpResponse(0, "5", 200)))
      order.asScala.toSeq should equal(Seq("1", "2", "3", "4", "5"))
    }

    "not execute the subsequent functions if earlier call fails" in new Setup {
      an [IllegalStateException] should be thrownBy await(Connector.transmit(httpResponse(100, "1", 200), Iterator(() =>httpResponseWithChunk(2, "2", 202), () =>httpResponseWithChunk(50, "3", 403)), (chunks) => httpResponse(100, "4", 200)))
      order.asScala.toSeq should not contain "4"
    }
  }

  "HodsApiConnector " should {

    trait Setup  {
      implicit val headerCarrier = HeaderCarrier()

      lazy val hodsApiConnector = new HodsApiConnector {
        override val rcsPaymentsUrl: String = ""
        override val http: HttpPost with HttpGet with HttpDelete = new HttpPost with HttpGet with HttpDelete with SuppressHttpAuditing {

          override protected def doPost[A](url: String, body: A, headers: Seq[(String,String)])(implicit rds: Writes[A], hc: HeaderCarrier): Future[HttpResponse] =
            if(url.contains("chunk")) {
              Future.successful(HttpResponse(202))
            } else {
              Future.successful(HttpResponse(200))
            }

          override protected def doFormPost(url: String, body: Map[String, Seq[String]])(implicit hc: HeaderCarrier): Future[HttpResponse] = Future.successful(HttpResponse(200))

          override protected def doGet(url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = Future.successful(HttpResponse(200))

          override protected def doDelete(url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = Future.successful(HttpResponse(200))

          protected def doPostString(url: String, body: String, headers: Seq[(String, String)])(implicit hc: HeaderCarrier) = ???

          protected def doEmptyPost[A](url: String)(implicit hc: HeaderCarrier) = ???

          override def auditConnector: AuditConnector = ???

          override def appName: String = ???
        }

        override lazy val metricsRegistry: MetricRegistry = MetricsRegistry.defaultRegistry

      }
    }

    "collect the response time for the rcs communication through HodsApi" in new WithApplication(FakeApplication(additionalConfiguration = allMandatoryConfig)) with Setup {
      println("Env = " + RunMode.env)
      val chunks = Seq((RcsChunk(Seq()), ChunkRelease("CHUNK_ID", List())))
      hodsApiConnector.sendPayments("DATASET_ID", chunks.iterator)

      eventually {
        hodsApiConnector.metricsRegistry.timer("RcsInitiationResponseTime").getCount should be(1)
        hodsApiConnector.metricsRegistry.timer("RcsChunkResponseTime").getCount should be(1)
        hodsApiConnector.metricsRegistry.timer("RcsReleaseResponseTime").getCount should be(1)
      }
    }

    "collect the response time for rcs communication with multiple chunks through HodsApi" in new WithApplication(FakeApplication(additionalConfiguration = allMandatoryConfig)) with Setup {
      val chunks = List.fill(100)((RcsChunk(Seq()), ChunkRelease("CHUNK_ID", List())))
      hodsApiConnector.sendPayments("DATASET_ID", chunks.iterator)

      eventually {
        hodsApiConnector.metricsRegistry.timer("RcsInitiationResponseTime").getCount should be(1)
        hodsApiConnector.metricsRegistry.timer("RcsChunkResponseTime").getCount should be(100)
        hodsApiConnector.metricsRegistry.timer("RcsReleaseResponseTime").getCount should be(1)
      }
    }
  }
}
