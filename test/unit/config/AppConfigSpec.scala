/*
 * Copyright 2020 HM Revenue & Customs
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

import base.UnitSpec
import com.typesafe.config.{Config, ConfigFactory}
import play.api.Mode.Test
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}

class AppConfigSpec extends UnitSpec {

  private val emptyConfig: Config = ConfigFactory.parseString("""
    |google-analytics.token=N/A
    |google-analytics.host=localhostGoogle
    |
    |mongodb.uri="mongodb://localhost:27017/customs-exports-internal"
    |
    |microservice.services.auth.host=authHost
    |microservice.services.auth.port=9988
    """.stripMargin)
  private val validConfig: Config =
    ConfigFactory.parseString("""
        |urls.login="http://localhost:9949/auth-login-stub/gg-sign-in"
        |urls.loginContinue="http://localhost:9000/customs-declare-exports-frontend"
        |
        |mongodb.uri="mongodb://localhost:27017/customs-exports-internal"
        |
        |google-analytics.token=N/A
        |google-analytics.host=GoogleHost
        |
        |microservice.services.auth.host=authHost
        |microservice.services.auth.port=9988
        |
        |microservice.services.customs-declare-exports-movements.host=movementsBackendHost
        |microservice.services.customs-declare-exports-movements.port=1234
        |
        |urls.customsDeclarationsGoodsTakenOutOfEu=customsDeclarationsGoodsTakenOutOfEuURL
        |urls.serviceAvailability=serviceAvailabilityURL
      """.stripMargin)

  private val invalidAppConfig: Config = ConfigFactory.parseString("""
      |mongodb.uri="mongodb://localhost:27017/customs-exports-internal"
      |""".stripMargin)
  private val emptyConfiguration = Configuration(emptyConfig)
  private val validConfiguration = Configuration(validConfig)
  private val invalidServicesConfiguration = Configuration(invalidAppConfig)

  private val environment = Environment.simple()
  private def appConfig(conf: Configuration) = {
    def runMode(conf: Configuration): RunMode = new RunMode(conf, Test)
    def servicesConfig(conf: Configuration) = new ServicesConfig(conf, runMode(conf))

    new AppConfig(conf, servicesConfig(conf), environment, "AppName")
  }

  private val validAppConfig: AppConfig = appConfig(validConfiguration)
  private val emptyAppConfig: AppConfig = appConfig(emptyConfiguration)

  "AppConfig" should {

    "return correct value" when {

      "asked for analytics token" in {
        validAppConfig.analyticsToken mustBe "N/A"
      }

      "asked for analytics host" in {
        validAppConfig.analyticsHost mustBe "GoogleHost"
      }

      "asked for auth URL" in {
        validAppConfig.authBaseUrl mustBe "http://authHost:9988"
      }

      "asked for movements backend URL" in {
        validAppConfig.customsDeclareExportsMovementsUrl mustBe "http://movementsBackendHost:1234"
      }

      "asked for customsDeclarationsGoodsTakenOutOfEu URL" in {
        validAppConfig.customsDeclarationsGoodsTakenOutOfEuUrl mustBe "customsDeclarationsGoodsTakenOutOfEuURL"
      }

      "asked for serviceAvailability URL" in {
        validAppConfig.serviceAvailabilityUrl mustBe "serviceAvailabilityURL"
      }

    }

    "throw an exception" when {

      "movements backend URL is missing" in {
        intercept[Exception](emptyAppConfig.customsDeclareExportsMovementsUrl).getMessage mustBe
          "Could not find config customs-declare-exports-movements.host"
      }

      "customsDeclarationsGoodsTakenOutOfEu URL is missing" in {
        intercept[Exception](emptyAppConfig.customsDeclarationsGoodsTakenOutOfEuUrl).getMessage mustBe
          "Missing configuration key: urls.customsDeclarationsGoodsTakenOutOfEu"
      }

      "serviceAvailability URL is missing" in {
        intercept[Exception](emptyAppConfig.serviceAvailabilityUrl).getMessage mustBe
          "Missing configuration key: urls.serviceAvailability"
      }
    }

  }

}
