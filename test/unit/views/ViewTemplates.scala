/*
 * Copyright 2021 HM Revenue & Customs
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

package views

import config.AppConfig
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.config.{AccessibilityStatementConfig, AssetsConfig, GTMConfig, OptimizelyConfig}
import uk.gov.hmrc.play.views.html.helpers.ReportAProblemLink
import uk.gov.hmrc.play.views.html.layouts._
import views.html.templates.{govuk_internal_template, govuk_wrapper, main_template}
trait ViewTemplates {

  private val minimalConfiguration: Configuration = Configuration(
    "assets.url" -> "localhost",
    "assets.version" -> "0",
    "google-analytics.token" -> "0",
    "google-analytics.host" -> "localhost:8080",
    "metrics.name" -> "",
    "metrics.rateUnit" -> "SECONDS",
    "metrics.durationUnit" -> "SECONDS",
    "metrics.showSamples" -> "false",
    "metrics.jvm" -> "false",
    "metrics.logback" -> "false",
    "microservice.services.contact-frontend.host" -> "localhost",
    "microservice.services.contact-frontend.port" -> "9250",
    "microservice.services.auth.host" -> "localhost",
    "microservice.services.auth.port" -> "8500",
    "timeoutDialog.timeout" -> "13min",
    "timeoutDialog.countdown" -> "3min"
  )

  private val minimalAppConfig: AppConfig =
    new AppConfig(minimalConfiguration, new ServicesConfig(minimalConfiguration), Environment.simple(), "appName")

  private val head: Head = new Head(
    new OptimizelySnippet(new OptimizelyConfig(minimalConfiguration)),
    new AssetsConfig(minimalConfiguration),
    new GTMSnippet(new GTMConfig(minimalConfiguration))
  )

  private val footer: Footer = new Footer(new AssetsConfig(minimalConfiguration))

  private val accessibilityStatementConfig = new AccessibilityStatementConfig(minimalConfiguration)

  private val govuk_wrapper: govuk_wrapper = new govuk_wrapper(
    head,
    new HeaderNav(),
    footer,
    new ServiceInfo(),
    new MainContentHeader(),
    new MainContent(),
    new FooterLinks(accessibilityStatementConfig),
    new ReportAProblemLink(),
    new govuk_internal_template(),
    minimalAppConfig
  )

  protected val main_template = new main_template(govuk_wrapper, new Sidebar(), new Article())

}
