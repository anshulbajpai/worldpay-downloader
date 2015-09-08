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

import com.ning.http.client.AsyncHttpClientConfig
import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.ShouldMatchers
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.libs.ws.ning.NingWSClient
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.Future

class HealthCheckSteps extends ScalaDsl with EN with Stubs with ScalaFutures with ShouldMatchers with IntegrationPatience {

  val baseUrl = Env.host
  implicit val wsClient = new NingWSClient(new AsyncHttpClientConfig.Builder().build())

  var pingResponse: Future[WSResponse] = null

  When("""^I ping the microservice service using the endpoint '/ping/ping'$"""){ () =>
    pingResponse = WS.clientUrl(s"$baseUrl/ping/ping").get()
  }

  Then("""^I should get a successful response$"""){ () =>
    pingResponse.futureValue.status should be(200)
  }
}
