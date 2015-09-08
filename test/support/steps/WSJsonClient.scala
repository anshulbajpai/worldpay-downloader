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

import play.api.libs.ws.{WS, WSRequestHolder, WSResponse}
import play.api.mvc.Results

import scala.concurrent.Future

trait WSJsonClient {

  val builder = new (com.ning.http.client.AsyncHttpClientConfig.Builder)()
  implicit val sslClient = new play.api.libs.ws.ning.NingWSClient(builder.build())
  val baseUrl = Env.host

  def postEmptyBody(url:String) : Future[WSResponse] = {
    WS.clientUrl(s"$baseUrl$url").post(Results.EmptyContent())
  }

  def postJson(url:String, body:String) : Future[WSResponse] = {
    wsClient(url).post { body }
  }

  private def wsClient(url: String): WSRequestHolder = {
    WS.clientUrl(s"$baseUrl$url")
      .withHeaders("Content-Type" -> "application/json")
  }

  def getJson(url:String) : Future[WSResponse] = {
    wsClient(url)
      .get()
  }

  def deleteJson(url:String) : Future[WSResponse] = {
    WS.clientUrl(s"$baseUrl$url").delete()
  }
}
