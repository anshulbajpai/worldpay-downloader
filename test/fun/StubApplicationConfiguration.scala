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

package fun

import support.steps.Env.{stubHost, stubPort}

trait StubApplicationConfiguration {

  val config = Map[String, Any](
    "sftp.user" -> "TestHmrcUser",
    "sftp.host" -> "localhost",
    "sftp.port" -> 2223,
    "sftp.timeout" -> 5000,
    "sftp.knownHosts" -> "",
    "sftp.companyName" -> "HMRE",
    "sftp.destinationFolder" -> "", // root destination folder used for Stub tests (with embedded SFTP server)
    "sftp.strictHostKeyChecking" -> "no",
    "sftp.privateKey" -> "LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQ0KUHJvYy1UeXBlOiA0LEVOQ1JZUFRFRA0KREVLLUluZm86IEFFUy0xMjgtQ0JDLDI1NDdBQTY3Qzc3MTZFNjJEOTVBMzA5RUJFN0E1QjE0DQoNCkUvQnBRUHU5OTJ0Q0xUMXp6aFFYcGhIbUl5QUs4MldnNU90NE80TzkzTVhvNVduelVnWGtWcG11bTlsMUJ5OEUNCklZUGtEa0kyVlJPaGFLMGRYdmduQTBWYXBoTWJ6QWdKbU9pYmwzbnRHR1BZekxjeW8rQ0RVb2hnQ2NuYUhRR2oNCm1MTG0zMUFOWk1OcXdkM0Nmc3JuWGJEWk5CRkNWUVF0SnluT05VeTVWc1A0eGZiamFDbzA3R0RRUDFHT0hQUVkNClVoT1hxSlFmSkZGdkNtR2Z0dnpiUUpqRDE4OXZzSGRJTURvZmgva2tJY3JuS243U2VBLzZZNzFvSzRoUVNmakMNCmVaRkM0SVg3LzA4WC81WGEvZlBuajJ0WXdmdzBYeGZTUXJpTHB1VHZjd3lXRUo4UmVMQlprQWY4RG81VkdFU3UNCmRPVVMyR0xURTQwVWE5b1VLRy9Ra0drMnRwWUVNTFdaUy9pYUxDcHEzUzhESGNFVXFvLzBESnJHZ2xCMWVoakcNCjUzbWtPU1N2VWt1eHc1U1Z3OXZtTUF5TW4zUHlKTGpWOU5DQkZsVitDSm8vUDBXb2pqaVgwYzNZWW51UUpETmoNCnBkSDJ3bHp5OFAvNVhYSTNjM09DU1JNdEZkT0JOZVJwWjU3Y3RxZmZYaTM3RnpBL2Exc2NCSW9GeWlJcGRsUW8NClFBSlJxUkx2UlFsc1JOQm9XbzhKaksrbzc2a3ZYVGFDbEgwNHJFVVdWS1NjUEo1WENnMjhZZHpqNHJuQXdwRUkNCmQzK0VHTGR1N2NyL0NrOHg2V3ZteTZFYmkydkVYVjNPcUhCSjQxZFI2T2YzdUhxRllCWFFjTGJpajZtU2hLeHoNCkxsOEdvRU5nYnIxWm4wUS9CODd3ZWpKa1pkM3h4bjJTODhkd1NuR0xVOXZLOWFnODR3eW5IRnNLOUs0bC9ndEsNCmxhVWVkT0pxTXpvcHk4NTFiMWZEbi9JbFEyZ2h1YmdXelp6YWE3UUtSRFlESnVrQ2lVeFh2cGxiZXZjT3I1N00NClhhN2ZHUWhwWXlnUmFZTVVqSnJNL2FaYVQrN2k0czExeTZjUVg5c2g0YUlWbnJOMS9KeDc0V0pzUUdGVzJ2cmINCkRrR2Y4ck1BRGprd3BjcStHU0MyNHhQSDdWMWp4d0YyNzE1R3plNEJrbFEzdnFaYm41VTZjNG9VMWxDQkVkUWENCmNENHBFcWJkaFhHUkVpL2dhdFpiTXd0NkIrNUNFanNiMG9PeHplKzVaY1JKakx2SEhYNCt5UW9mS0EvditCTy8NCmtTUnVRUEFGY3VmWHU1YWhmOVFGU2grSWhYdExheUp3ZnBGZXZDQkRmVVZPMDRHdXBxcDBmYzFaamE5aXhRL28NCjNZTlBmS2h3Y3E2Vmg3T0lnV3hqT3REc0NnU2E4S3NuVFFoV2VMREhrc3FLT05QajBFVGxSdU1VamZybHIwUFoNCmZEUlNjdDFMa21QTWp4VGhNSGlvRGVJSjdtRCt4cUUyN0JqRUJaVk1iQWloUUZmQVJqOVFHYjlvTTVUM2szaUUNCmd1WFdQU0hWOGxUZ3VWSXlKWmwrZEFQakNEWis4SFR2NHp0dTFMTTRpV25lVEJKL0g4a2dwZ28zNTIyYVg3d0UNCjU4NWNtMHV1QlJXL0xtYURvMEdVSVFNL0dYK3V0Q2VXaW92aG5EdjVVOXdrSm8xNFlBRzdLeTV2RldxQ1VtUWINCmszUXR2MGp3MVZZaFU3UGlxdlpOSm1NdGMwdHY4M0pwbkJVU0g0WnVPYlhFVk8zY2RTZXZlMmtqTWdqbk8xbVgNCnRVeFlUQjZ4UXVQT1ZwdlB3VFhTYzhhSllicER0Q0RoclprWkl0YTZWZjNSYVA3SUxPMVVTeVpkYWZmNC9HV0gNCkoyVWJ6L1kwWE8zb1l4bjhmczgvOWJybmc4YStCUHNNTE5TL3dZQ3doamlaL3o1eTg2cEYyKzUwYTNKQ2VqY1INCkFaTnAvZ2ZJSldHWVlvK2t0U3BPNzJ0VGpMbXBLWnpFSEZicU9QNzFlUGVub0F4ZDZrOVBGN0FtOFR1aWk1ZnQNCndXbEFmQUNLK05RR2lJOHIzV2s0Z1VyWW0xOEdSZDNGMGp4RTRSbnhDd213dnY2U1phZC84am5FanpCL1hhSW0NCi0tLS0tRU5EIFJTQSBQUklWQVRFIEtFWS0tLS0tDQo=",
    "sftp.privateKeyPassPhrase" -> "UDR5bTNudHNQMTR0ZjBybSE=",
    "auditing.consumer.baseUri.host" -> stubHost,
    "auditing.consumer.baseUri.port" -> stubPort,
    "microservice.services.hodsapi.host" -> stubHost,
    "microservice.services.hodsapi.port" -> stubPort,
    "microservice.services.lock.host" -> stubHost,
    "microservice.services.lock.port" -> stubPort,
    "worldpay.merchant.saForDebitCardId" -> "11010000",
    "worldpay.merchant.saForDebitCardAccount" -> "33333333",
    "worldpay.merchant.saForCreditCardId" -> "11020000",
    "worldpay.merchant.saForCreditCardAccount" -> "33333333",
    "worldpay.merchant.vatForDebitCardId" -> "12010000",
    "worldpay.merchant.vatForDebitCardAccount" -> "55555555",
    "worldpay.merchant.vatForCreditCardId" -> "12020000",
    "worldpay.merchant.vatForCreditCardAccount" -> "55555555",
    "worldpay.merchant.ctForDebitCardId" -> "13010000",
    "worldpay.merchant.ctForDebitCardAccount" -> "33333333",
    "worldpay.merchant.ctForCreditCardId" -> "13020000",
    "worldpay.merchant.ctForCreditCardAccount" -> "33333333",
    "worldpay.merchant.epayeForDebitCardId" -> "14010000",
    "worldpay.merchant.epayeForDebitCardAccount" -> "33333333",
    "worldpay.merchant.epayeForCreditCardId" -> "14020000",
    "worldpay.merchant.epayeForCreditCardAccount" -> "33333333",
    "worldpay.merchant.otherTaxesForDebitCardId" -> "15010000",
    "worldpay.merchant.otherTaxesForDebitCardAccount" -> "33333333",
    "worldpay.merchant.otherTaxesForCreditCardId" -> "15020000",
    "worldpay.merchant.otherTaxesForCreditCardAccount" -> "33333333",
    "worldpay.merchant.sdltForDebitCardId" -> "16010000",
    "worldpay.merchant.sdltForDebitCardAccount" -> "33333333",
    "worldpay.merchant.sdltForCreditCardId" -> "16020000",
    "worldpay.merchant.sdltForCreditCardAccount" -> "33333333"
  )


}
