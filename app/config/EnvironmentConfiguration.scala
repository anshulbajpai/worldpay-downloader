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

package config

import metrics.EmisStatusMetricsPublisher
import model.Merchants
import transformer.EmisReportTransformer
import utils.ConfigurationImplicits._

import scala.util.{Success, Try}

object EnvironmentConfiguration {

  def configuration = play.api.Play.current.configuration

  def user = configuration.mandatoryString("sftp.user")

  def host = configuration.mandatoryString("sftp.host")

  def port = configuration.mandatoryInteger("sftp.port")

  def companyName = configuration.mandatoryString("sftp.companyName")

  def privateKey = Base64Decoder.decode(configuration.mandatoryString("sftp.privateKey"))

  def strictHostKeyChecking = configuration.mandatoryString("sftp.strictHostKeyChecking")

  def knownHosts = Base64Decoder.decode(configuration.mandatoryString("sftp.knownHosts"))

  def destinationFolder = configuration.mandatoryString("sftp.destinationFolder")

  def privateKeyPassPhrase = Base64Decoder.decode(configuration.mandatoryString("sftp.privateKeyPassPhrase"))

  def chunkSize = configuration.mandatoryInteger("chunkSize")

  def timeout = configuration.mandatoryInteger("sftp.timeout")

  import config.MerchantKeys._

  def worldpayEnvironmentConfigurationTryKeys: Map[String, Try[String]] = keys map { key => key -> Try(configuration.mandatoryString(s"worldpay.merchant.$key"))} toMap

  lazy val merchants = {
    val conf = worldpayEnvironmentConfigurationTryKeys.map {
      case (k, Success(v)) => (k,v)
    }
    Merchants(conf)
  }

  lazy val emisStatusMetricsPublisher = EmisStatusMetricsPublisher(merchants)
  lazy val emisReportTransformer = EmisReportTransformer(merchants)

  def verify() = {
    worldpayEnvironmentConfigurationTryKeys.values ++ Seq(Try(user), Try(host), Try(port), Try(privateKey), Try(strictHostKeyChecking), Try(knownHosts), Try(destinationFolder), Try(privateKeyPassPhrase))
      .filter(_.isFailure) foreach { t =>
      if (t.isFailure) throw t.failed.get
    }
  }
}
