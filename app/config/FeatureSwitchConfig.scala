/*
 * Copyright 2024 HM Revenue & Customs
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

import features.Feature.Feature
import features.FeatureStatus.FeatureStatus
import features.{Feature, FeatureStatus}
import javax.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class FeatureSwitchConfig @Inject() (configuration: Configuration) {

  lazy val defaultFeatureStatus: FeatureStatus.Value =
    FeatureStatus.withName(loadConfig(feature2Key(Feature.default)))

  def isFeatureOn(feature: Feature): Boolean = featureStatus(feature) == FeatureStatus.enabled

  def setFeatureStatus(feature: Feature, status: FeatureStatus): Unit =
    sys.props += (feature2Key(feature) -> status.toString)

  def featureStatus(feature: Feature): FeatureStatus =
    getFeatureStatusFromProperties(feature).getOrElse(getFeatureStatusFromConfig(feature))

  private def getFeatureStatusFromProperties(feature: Feature): Option[FeatureStatus] =
    sys.props
      .get(feature2Key(feature))
      .map(str2FeatureStatus)

  private def getFeatureStatusFromConfig(feature: Feature): FeatureStatus =
    configuration
      .getOptional[String](feature2Key(feature))
      .map(str2FeatureStatus)
      .getOrElse(defaultFeatureStatus)

  private def loadConfig(key: String): String =
    configuration.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private def feature2Key(feature: Feature): String = s"features.$feature"

  private def str2FeatureStatus(str: String): FeatureStatus = FeatureStatus.withName(str)
}
