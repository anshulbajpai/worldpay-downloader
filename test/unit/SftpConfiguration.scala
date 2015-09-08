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

package unit

import _root_.config.MerchantKeys

trait SftpConfiguration {

  lazy val MandatorySftpConfig = Map[String,Any](
    "sftp.user" -> "vagrant",
    "sftp.host" -> "192.168.50.4",
    "sftp.port" ->  22,
    "sftp.companyName" ->  "HMRE",
    "sftp.privateKey" -> "LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFb2dJQkFBS0NBUUVBNk5GOGlhbGx2UVZwMjJXRGtUa3lydHZwOWVXVzZBOFlWcitrejRUakdZZTdnSHpJCncrbmlObHRHRUZIekQ4K3YxSTJZSjZvWGV2Y3QxWWVTMG85SFp5TjFROXFnQ2d6VUZ0ZE9LTHY2SWVkcGxxb1AKa2NtRjBhWWV0MlBrRURvM01sVEJja0ZYUElUQU16RjhkSlNJRm85RDhIZmRPVjBJQWR4NE83UHRpeFdLbjV5MgpoTU5HMHpRUHlVZWNwNHB6QzZraXZBSWh5ZkhpbEZSNjFSR0wrR1BYUTJNV1pXRlliQUdqeWlZSm5BbUNQM05PClRkMGpNWkVuRGtiVXZ4aE1tQllTZEVUazFyUmdtK1I0TE96RlVHYUhxSERMS0xYK0ZJUEtjRjk2aHJ1Y1h6Y1cKeUxiSWJFZ0U5OE9IbG5WWUN6UmRLOGpscW04dGVoVWM5YzlXaFFJQkl3S0NBUUVBNGlxV1BKWHR6WkE2OG1LZApFTHM0akpzZHlreStld2RaZU5kczV0amNuSFU1elVZRTI1SytmZkpFRDlxVVdJQ2NMWkRjODFUR1dqSHlBcUQxCkJ3N1hwZ1V3RmdlVUp3VWx6UXVyQXYrL3lTbnhpd3VhR0pmaEZNMUNhUUh6ZlhwaGdWbWwrZlpVdm5KVVR2emYKVEsyTGc2RWRiVUU5VGFyVWxCZi94UGZ1RWhNU2xJRTVrZWIvWnozL0xVbFJnOHlEcXo1dytRV1ZKNHV0bktuSwppcXdaTjBtd3B3VTdZU3lKaGxUNFlWMUYzbjRZakxzd001d0pzMm9xbTBqc3NRdS9CVDB0eUVYTkRZQkxFRjRBCnNDbGFXdVNKMmtqcTdLaHJyWVh6YWdxaG5TZWk5T0RZRlNoSnU4VVdWZWMzSWhiNVpYbHpPNnZkTlExSjlYc2YKNG0rMnl3S0JnUUQ2cUZ4eC9SdjlDTk45NmwvNHJiMTRIS2lyQzJvL29yQXBpSG1IRHNVUnM1clVLRHgwZjlpUApjWE43UzF1ZVBYdUpSSy81aHN1YmFPQ3gzT3dkMnU5Z0Q2T3EwQ3NNa0U0Q1VTaUpjWXJNQU50eDU0Y0dIN1JrCkVqRlp4Szh4QXYxbGRFTEV5eHJGcWtiRTRCS2Q4UU90NDE0cWp2VEd5QUsrT0xEM00yUWRDUUtCZ1FEdHg4cE4KQ0F4Ujd5aEhiSVdUMUFINjYrWFdOOGJYcTdsM1JPL3VrZWFjaTk4SmZrYmt4VVJaaHR4Vi9ISHV2VWhuUExkWAozVHd5Z1BCWVpGTm80cHpWRWh6V29UdG5FdHJGdWVLeHljMytMalpwdW8rbUJsUTZPUnRmZ2tyOWdCVnBoWFpHCllFemtDRDNsVmRsOEw0Y3c5QlZwS3JKQ3MxYzV0YUdqRGdkSW5RS0JnSG0vZlZ2djk2Ykp4Yzl4MXRmZlhBY2oKM09WZFVOMFVnWE5DU2FmLzNBL3BoYmVCUWU5eFMrM21wYzRyNnF2eCtpeTY5bU5CZU5aMHhPaXRJanBqQm8yKwpkQkVqU0J3TGs1cTV0SnFIbXkvaktNSkw0bjlST2x4OTNYUytuanhnaWJUdlU2RnA5dytOT0ZEL0h2eEIzVGN6CjYrakpGODVENUJOQUczREJNS0JqQW9HQkFPQXhadmdzS04rSnVFTlhzU1Q3Rjg5VGNrMmlUY1FJVDhnNXJ3V0MKUDlWdDc0eWJvZTJrRFQ1MzF3OCtlZ3o3bkFtUkJLTk03NTFVLzk1UDl0ODhFRGFjREkvWjJPd251RlFIQ1BERgpsbFlPVUkrU3BMSjYvdlVSUmJIU25ubjhhL1hHK256ZWRHSDVKR3FFSk5Rc3oreFQyYXhNMC9XL0NSa25tR2FKCmtkYS9Bb0dBTldyTEN6NzA4eTdWWWdBdFcyVWYxRFBPSVlNZHZvNmZ4SUI1aTlaZklTZ2NKL2JiQ1VrRnJob0gKK3ZxLzVDSVd4Q1BwMGY4NVI0cXh4UTVpaHhKMFlEUVQ5SnB4NFRNc3M0UFNhdlBhQkgzUlhvdzVPaGUrYllvUQpORTVPZ0VYazJ3VmZaY3pDWnBpZ0JLYktaSE5ZY2VsWHRUdC9uUDNyc0N1R2NNNGg1M3M9Ci0tLS0tRU5EIFJTQSBQUklWQVRFIEtFWS0tLS0tCg==",
    "sftp.strictHostKeyChecking" -> "yes",
    "sftp.knownHosts" -> "MTkyLjE2OC41MC40IHNzaC1yc2EgQUFBQUIzTnphQzF5YzJFQUFBQUJJd0FBQVFFQW5OZkdEVmloTDJkcnM2SHpLWC8rWlJYYzVUYUd2anFCUUtXbzl6SzVER1JuL3NZcnlKbU9yQW5VZWlxbmtsTnpaOEYvZDFzaHBGN3hQdS9TRmk0S2Z1RUR5K2lvcGRsYmFBNGMzRWpmRnV1SDIrYXBTK2YrTndqMG4rUDRlVGY1ZHNEdmUwMjFFTUxJbkdZbEJoNkhaa0Erc2NyRmN6UHpUQXZMNHh1bWRHdW0yM0ZXZktDakU5UEFndmwrY0V1NjR5NVFnbEtuOEgxeXdIUWpXMWJ4dTZaRnJHVGZZd1lrUEJXZUhlMnpSeGFVSjh6MlQ1NnVmV2tVcEl0Q1hrbmxRUnhYaVd6RkFSbG9oQW5TQWU1aENSNUx3bGU0eXBPc0tqWi8vcm1NV3RlM0hJaUw1Uzl2Z05GdmN4bnNtTCs0NWJyZHlsMDNEd1pQeDdkN0tlNVBUUT09",
    "sftp.destinationFolder" -> "/var/tmp",
    "sftp.privateKeyPassPhrase" -> "UDR5bTNudHNQMTR0ZjBybSE",
    "sftp.timeout" -> 5000,
    "auditing.consumer.baseUri.host" -> "localhost",
    "auditing.consumer.baseUri.port" -> 11111

  )
}


trait WorldpayMerchantConfiguration {
  val MandatoryMerchantConfig = MerchantKeys.keys.map(k => s"worldpay.merchant.$k" -> "02345678").toMap
}
