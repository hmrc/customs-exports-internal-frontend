/*
 * Copyright 2023 HM Revenue & Customs
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
package routines

import com.mongodb.client.{MongoClients, MongoDatabase}
import org.scalatest.Assertion
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import repository.TestMongoDB

import scala.jdk.CollectionConverters._

class DeleteMigrationCollectionsRoutineISpec extends AnyWordSpec with GuiceOneAppPerSuite with TestMongoDB {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .disable[com.codahale.metrics.MetricRegistry]
      .configure(mongoConfiguration)
      .build()

  private val mongoDatabase: MongoDatabase = {
    val mongoURI = mongoConfiguration.get[String]("mongodb.uri").replace("sslEnabled", "ssl")
    MongoClients.create(mongoURI).getDatabase(DatabaseName)
  }

  private val collections = List("exportsMigrationChangeLog", "exportsMigrationLock")

  "DeleteMigrationCollectionsRoutine" should {
    "drop all migration-related collections" in {
      collections.foreach(mongoDatabase.createCollection)

      expectedNumberOfCollections(2)

      app.injector.instanceOf[DeleteMigrationCollectionsRoutine].execute().futureValue

      expectedNumberOfCollections(0)
    }
  }

  private def expectedNumberOfCollections(expectedSize: Int): Assertion =
    mongoDatabase.listCollectionNames().asScala.filter(collections.contains).toList.size mustBe expectedSize
}
