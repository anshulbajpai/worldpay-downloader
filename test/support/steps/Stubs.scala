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

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.UrlMatchingStrategy


trait Stubs {
  def stubFor(stub: Stub) {
    stub.create()
  }
}

trait Stub {
  def create(): Unit

  def stubForPage(urlMatchingStrategy: UrlMatchingStrategy, heading: String, body: String = "") = {
    stubFor(get(urlMatchingStrategy)
      .willReturn(
        aResponse()
          .withStatus(200)
          .withBody(s"<html><body><h1>$heading</h1><p>This is a stub<p>$body</body></html>")
      ))
  }
}

object Auditing extends Stub {

  def create() = {
    stubFor(post(urlEqualTo("/write/audit"))
      .willReturn(
        aResponse()
          .withStatus(200)))

    stubFor(post(urlEqualTo("/write/audit/merged"))
      .willReturn(
        aResponse()
          .withStatus(200)))
  }
}
