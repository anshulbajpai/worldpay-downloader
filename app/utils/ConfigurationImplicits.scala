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

package utils

import play.api.Configuration


object ConfigurationImplicits {
  implicit class MandatoryConfiguration(val configuration: Configuration) extends AnyVal {
    def mandatoryString(key: String, validValues: Option[Set[String]] = None) = configuration.getString(key, validValues).getOrElse(throw new IllegalStateException(s"Missing configuration for $key"))
    def mandatoryInteger(key: String) = configuration.getInt(key).getOrElse(throw new IllegalStateException(s"Missing configuration for $key"))
  }
}
