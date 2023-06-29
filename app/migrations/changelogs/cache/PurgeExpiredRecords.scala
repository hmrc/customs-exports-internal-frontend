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

package migrations.changelogs.cache

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.{`type`, not}
import migrations.changelogs.{MigrationDefinition, MigrationInformation}
import org.bson.{BsonType, Document}
import org.mongodb.scala.model.Filters.equal
import play.api.Logging

import scala.jdk.CollectionConverters._

class PurgeExpiredRecords extends MigrationDefinition with Logging {

  override val migrationInformation: MigrationInformation =
    MigrationInformation(id = s"CEDS-4542 Purge expired cache records", order = 1, author = "Tom Robinson")

  override def migrationFunction(db: MongoDatabase): Unit = {
    val updatedField = "updated"
    val cacheCollection = db.getCollection("cache")

    logger.info(s"Applying '${migrationInformation.id}' db migration...")

    def filter = not(`type`(updatedField, BsonType.DATE_TIME))

    try {
      val documentsToUpdate: Iterable[Document] = cacheCollection
        .find(filter)
        .asScala

      val totalDeleted = documentsToUpdate.foldLeft(0) { (count, document) =>
        val documentId = document.get("_id")
        val docFilter = equal("_id", documentId)
        val deleteResult = cacheCollection.deleteOne(docFilter)
        count + deleteResult.getDeletedCount.toInt
      }

      logger.info(s"Deleted $totalDeleted documents from the cache collection where 'updated' was not a Date.")
      logger.info(s"Finished applying '${migrationInformation.id}' db migration.")
    } catch {
      case e: Exception => logger.error(s"An error occurred during the db migration '${migrationInformation.id}'", e)
    }
  }
}
