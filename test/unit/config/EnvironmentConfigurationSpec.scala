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

package unit.config

import java.io.File

import config.{MerchantKeys, EnvironmentConfiguration}
import play.api.{Mode, Configuration, GlobalSettings}
import play.api.test.{FakeApplication, WithApplication}
import uk.gov.hmrc.play.test.UnitSpec
import unit.{WorldpayMerchantConfiguration, SftpConfiguration}

import scala.util.{Failure, Success}

class EnvironmentConfigurationSpec extends UnitSpec with SftpConfiguration with WorldpayMerchantConfiguration {


  val allMandatoryConfig = MandatorySftpConfig ++ MandatoryMerchantConfig

  "The EnvironmentConfiguration object" should {

    "Load and base64 unencode knownHosts" in new WithApplication(FakeApplication(additionalConfiguration = allMandatoryConfig)) {
      EnvironmentConfiguration.knownHosts shouldBe "192.168.50.4 ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEAnNfGDVihL2drs6HzKX/+ZRXc5TaGvjqBQKWo9zK5DGRn/sYryJmOrAnUeiqnklNzZ8F/d1shpF7xPu/SFi4KfuEDy+iopdlbaA4c3EjfFuuH2+apS+f+Nwj0n+P4eTf5dsDve021EMLInGYlBh6HZkA+scrFczPzTAvL4xumdGum23FWfKCjE9PAgvl+cEu64y5QglKn8H1ywHQjW1bxu6ZFrGTfYwYkPBWeHe2zRxaUJ8z2T56ufWkUpItCXknlQRxXiWzFARlohAnSAe5hCR5Lwle4ypOsKjZ//rmMWte3HIiL5S9vgNFvcxnsmL+45brdyl03DwZPx7d7Ke5PTQ=="
    }

    "Load and base64 unencode privateKey" in new WithApplication(FakeApplication(additionalConfiguration = allMandatoryConfig)) {
      EnvironmentConfiguration.privateKey shouldBe
        """-----BEGIN RSA PRIVATE KEY-----
          |MIIEogIBAAKCAQEA6NF8iallvQVp22WDkTkyrtvp9eWW6A8YVr+kz4TjGYe7gHzI
          |w+niNltGEFHzD8+v1I2YJ6oXevct1YeS0o9HZyN1Q9qgCgzUFtdOKLv6IedplqoP
          |kcmF0aYet2PkEDo3MlTBckFXPITAMzF8dJSIFo9D8HfdOV0IAdx4O7PtixWKn5y2
          |hMNG0zQPyUecp4pzC6kivAIhyfHilFR61RGL+GPXQ2MWZWFYbAGjyiYJnAmCP3NO
          |Td0jMZEnDkbUvxhMmBYSdETk1rRgm+R4LOzFUGaHqHDLKLX+FIPKcF96hrucXzcW
          |yLbIbEgE98OHlnVYCzRdK8jlqm8tehUc9c9WhQIBIwKCAQEA4iqWPJXtzZA68mKd
          |ELs4jJsdyky+ewdZeNds5tjcnHU5zUYE25K+ffJED9qUWICcLZDc81TGWjHyAqD1
          |Bw7XpgUwFgeUJwUlzQurAv+/ySnxiwuaGJfhFM1CaQHzfXphgVml+fZUvnJUTvzf
          |TK2Lg6EdbUE9TarUlBf/xPfuEhMSlIE5keb/Zz3/LUlRg8yDqz5w+QWVJ4utnKnK
          |iqwZN0mwpwU7YSyJhlT4YV1F3n4YjLswM5wJs2oqm0jssQu/BT0tyEXNDYBLEF4A
          |sClaWuSJ2kjq7KhrrYXzagqhnSei9ODYFShJu8UWVec3Ihb5ZXlzO6vdNQ1J9Xsf
          |4m+2ywKBgQD6qFxx/Rv9CNN96l/4rb14HKirC2o/orApiHmHDsURs5rUKDx0f9iP
          |cXN7S1uePXuJRK/5hsubaOCx3Owd2u9gD6Oq0CsMkE4CUSiJcYrMANtx54cGH7Rk
          |EjFZxK8xAv1ldELEyxrFqkbE4BKd8QOt414qjvTGyAK+OLD3M2QdCQKBgQDtx8pN
          |CAxR7yhHbIWT1AH66+XWN8bXq7l3RO/ukeaci98JfkbkxURZhtxV/HHuvUhnPLdX
          |3TwygPBYZFNo4pzVEhzWoTtnEtrFueKxyc3+LjZpuo+mBlQ6ORtfgkr9gBVphXZG
          |YEzkCD3lVdl8L4cw9BVpKrJCs1c5taGjDgdInQKBgHm/fVvv96bJxc9x1tffXAcj
          |3OVdUN0UgXNCSaf/3A/phbeBQe9xS+3mpc4r6qvx+iy69mNBeNZ0xOitIjpjBo2+
          |dBEjSBwLk5q5tJqHmy/jKMJL4n9ROlx93XS+njxgibTvU6Fp9w+NOFD/HvxB3Tcz
          |6+jJF85D5BNAG3DBMKBjAoGBAOAxZvgsKN+JuENXsST7F89Tck2iTcQIT8g5rwWC
          |P9Vt74yboe2kDT531w8+egz7nAmRBKNM751U/95P9t88EDacDI/Z2OwnuFQHCPDF
          |llYOUI+SpLJ6/vURRbHSnnn8a/XG+nzedGH5JGqEJNQsz+xT2axM0/W/CRknmGaJ
          |kda/AoGANWrLCz708y7VYgAtW2Uf1DPOIYMdvo6fxIB5i9ZfISgcJ/bbCUkFrhoH
          |+vq/5CIWxCPp0f85R4qxxQ5ihxJ0YDQT9Jpx4TMss4PSavPaBH3RXow5Ohe+bYoQ
          |NE5OgEXk2wVfZczCZpigBKbKZHNYcelXtTt/nP3rsCuGcM4h53s=
          |-----END RSA PRIVATE KEY-----
          |""".stripMargin
    }

  }

  "In order to make sure that the SFTP configuration is available at start, the application" should {

    "check that the mandatory property 'sftp.user' is defined" in {
      val thrown = intercept[Throwable](new WithApplication(without("sftp.user")) {println(EnvironmentConfiguration.user)})
      originalCause(thrown).getMessage should be("Missing configuration for sftp.user")
    }

    "check that the mandatory property 'sftp.host' is defined" in {
      val thrown = intercept[Throwable](new WithApplication(without("sftp.host")) {println(EnvironmentConfiguration.host)})
      originalCause(thrown).getMessage should be("Missing configuration for sftp.host")
    }

    "check that the mandatory property 'sftp.port' is defined" in {
      val thrown = intercept[Throwable](new WithApplication(without("sftp.port")) {println(EnvironmentConfiguration.port)})
      originalCause(thrown).getMessage should be("Missing configuration for sftp.port")
    }

    "check that the mandatory property 'sftp.privateKey' is defined" in {
      val thrown = intercept[Throwable](new WithApplication(without("sftp.privateKey")) {println(EnvironmentConfiguration.privateKey)})
      originalCause(thrown).getMessage should be("Missing configuration for sftp.privateKey")
    }

    "check that the mandatory property 'sftp.strictHostKeyChecking' is defined" in {
      val thrown = intercept[Throwable](new WithApplication(without("sftp.strictHostKeyChecking")) {println(EnvironmentConfiguration.strictHostKeyChecking)})
      originalCause(thrown).getMessage should be("Missing configuration for sftp.strictHostKeyChecking")
    }

    "check that the mandatory property 'sftp.knownHosts' is defined" in {
      val thrown = intercept[Throwable](new WithApplication(without("sftp.knownHosts")) {println(EnvironmentConfiguration.knownHosts)})
      originalCause(thrown).getMessage should be("Missing configuration for sftp.knownHosts")
    }

    "check that the mandatory property 'sftp.destinationFolder' is defined" in {
      val thrown = intercept[Throwable](new WithApplication(without("sftp.destinationFolder")) {println(EnvironmentConfiguration.destinationFolder)})
      originalCause(thrown).getMessage should be("Missing configuration for sftp.destinationFolder")
    }

    "check that the mandatory property 'sftp.privateKeyPassPhrase' is defined" in {
      val thrown = intercept[Throwable](new WithApplication(without("sftp.privateKeyPassPhrase")) {println(EnvironmentConfiguration.privateKeyPassPhrase)})
      originalCause(thrown).getMessage should be("Missing configuration for sftp.privateKeyPassPhrase")
    }
  }

  "To make sure that the Worldpay Merchant details configuration is available at the start, that application" should {

    MerchantKeys.keys.foreach { k =>
      s"check that the 'worldpay.merchant.$k property is defined" in {
        new WithApplication(without(s"worldpay.merchant.$k")) {
          val result = EnvironmentConfiguration.worldpayEnvironmentConfigurationTryKeys(k)
          result match {
            case Success(conf) => fail("Expected exception")
            case Failure(ex) => ex.getMessage should be(s"Missing configuration for worldpay.merchant.$k")
          }
        }
      }
    }
  }

  private def without(property: String) = FakeApplication(withGlobal = Some(new GlobalSettings() {
    override def onLoadConfig(config: Configuration, path: File, classloader: ClassLoader, mode: Mode.Mode): Configuration = {
      Configuration.empty
    }
  }), additionalConfiguration = (allMandatoryConfig).filterNot(_._1 == property))

  private def originalCause(exception: Throwable): Throwable = Option(exception.getCause) match {
    case Some(e) => originalCause(e)
    case _ => exception
  }

}
