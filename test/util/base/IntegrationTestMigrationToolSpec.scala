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

package base

import com.fasterxml.jackson.databind.ObjectMapper
import com.kenshoo.play.metrics.PlayModule
import com.mongodb.client.{MongoClients, MongoCollection, MongoDatabase}
import migrations.changelogs.MigrationDefinition
import org.bson.Document
import org.mongodb.scala.bson.BsonDocument
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

trait IntegrationTestMigrationToolSpec extends IntegrationTestBaseSpec with GuiceOneAppPerSuite {

  val collectionUnderTest: String
  val changeLog: MigrationDefinition

  override implicit lazy val app: Application = GuiceApplicationBuilder().disable[PlayModule].build()

  private val mongoClient = MongoClients.create()

  val database: MongoDatabase = mongoClient.getDatabase("test-customs-declare-exports")

  override def afterAll(): Unit = {
    mongoClient.close()
    super.afterAll()
  }

  def getCollection(collectionId: String): MongoCollection[Document] = database.getCollection(collectionId)

  def removeAll(collection: MongoCollection[Document]): Long = collection.deleteMany(BsonDocument()).getDeletedCount

  def runTest(inputDataJson: String, expectedResult: String): Unit = {
    val collection = getCollection(collectionUnderTest)
    removeAll(collection)
    collection.insertOne(Document.parse(inputDataJson))

    changeLog.migrationFunction(database)

    val result: Document = collection.find.first

    compareJson(result.toJson, expectedResult)
  }

  def runTest(inputDataJson: String): Document = {
    val collection = getCollection(collectionUnderTest)
    removeAll(collection)
    collection.insertOne(Document.parse(inputDataJson))

    changeLog.migrationFunction(database)

    collection.find.first
  }

  def compareJson(actual: String, expected: String): Unit = {
    val mapper = new ObjectMapper

    val jsonActual = mapper.readTree(actual)
    val jsonExpected = mapper.readTree(expected)

    jsonActual mustBe jsonExpected
  }
}
