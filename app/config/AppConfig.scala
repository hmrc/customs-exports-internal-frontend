/*
 * Copyright 2019 HM Revenue & Customs
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

import javax.inject.{Inject, Named, Singleton}
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(
  runModeConfiguration: Configuration,
  servicesConfig: ServicesConfig,
  environment: Environment,
  @Named("appName") serviceIdentifier: String
) {
  private val contactBaseUrl = servicesConfig.baseUrl("contact-frontend")

  private val assetsUrl = runModeConfiguration.get[String]("assets.url")

  val runningAsDev: Boolean = {
    runModeConfiguration
      .getOptional[String]("run.mode")
      .map(_.equals(Mode.Dev.toString))
      .getOrElse(Mode.Dev.equals(environment.mode))
  }
  val assetsPrefix: String = assetsUrl + runModeConfiguration.get[String]("assets.version")
  val analyticsToken: String = runModeConfiguration.get[String](s"google-analytics.token")
  val analyticsHost: String = runModeConfiguration.get[String](s"google-analytics.host")
  val reportAProblemPartialUrl: String = s"$contactBaseUrl/contact/problem_reports_ajax?service=$serviceIdentifier"
  val reportAProblemNonJSUrl: String = s"$contactBaseUrl/contact/problem_reports_nonjs?service=$serviceIdentifier"
  val authBaseUrl: String = servicesConfig.baseUrl("auth")

  // from external UI
  private def loadConfig(key: String): String =
    runModeConfiguration
      .getOptional[String](key)
      .getOrElse(throw new Exception(s"Missing configuration key: $key"))

  lazy val customsDeclarationsGoodsTakenOutOfEuUrl = loadConfig("urls.customsDeclarationsGoodsTakenOutOfEu")
  lazy val serviceAvailabilityUrl = loadConfig("urls.serviceAvailability")

  lazy val customsDeclareExportsMovements = servicesConfig.baseUrl("customs-declare-exports-movements")
}
