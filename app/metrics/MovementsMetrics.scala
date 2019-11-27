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

package metrics

import com.codahale.metrics.Timer.Context
import com.kenshoo.play.metrics.Metrics
import javax.inject.{Inject, Singleton}
import metrics.MetricIdentifiers._
import models.cache.JourneyType._

@Singleton
class MovementsMetrics @Inject()(metrics: Metrics) {

  val timers = Map(
    ARRIVE -> metrics.defaultRegistry.timer(s"$arrivalMetric.timer"),
    RETROSPECTIVE_ARRIVE -> metrics.defaultRegistry.timer(s"$retrospectiveArrivalMetric.timer"),
    DEPART -> metrics.defaultRegistry.timer(s"$departureMetric.timer"),
    ASSOCIATE_UCR -> metrics.defaultRegistry.timer(s"$associationMetric.timer"),
    DISSOCIATE_UCR -> metrics.defaultRegistry.timer(s"$disassociationMetric.timer"),
    SHUT_MUCR -> metrics.defaultRegistry.timer(s"$shutMucr.timer")
  )

  val counters = Map(
    ARRIVE -> metrics.defaultRegistry.counter(s"$arrivalMetric.counter"),
    RETROSPECTIVE_ARRIVE -> metrics.defaultRegistry.counter(s"$retrospectiveArrivalMetric.counter"),
    DEPART -> metrics.defaultRegistry.counter(s"$departureMetric.counter"),
    ASSOCIATE_UCR -> metrics.defaultRegistry.counter(s"$associationMetric.counter"),
    DISSOCIATE_UCR -> metrics.defaultRegistry.counter(s"$disassociationMetric.counter"),
    SHUT_MUCR -> metrics.defaultRegistry.counter(s"$shutMucr.counter")
  )

  def startTimer(feature: JourneyType): Context = timers(feature).time()

  def incrementCounter(feature: JourneyType): Unit = counters(feature).inc()
}

object MetricIdentifiers {
  val arrivalMetric = "arrival"
  val retrospectiveArrivalMetric = "retrospectiveArrival"
  val departureMetric = "departure"
  val associationMetric = "association"
  val disassociationMetric = "disassociation"
  val shutMucr = "shut"
}
