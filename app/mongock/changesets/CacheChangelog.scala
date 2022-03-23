/*
 * Copyright 2022 HM Revenue & Customs
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

package mongock.changesets

import java.time.Instant
import java.util.Date

import com.github.cloudyrock.mongock.{ChangeLog, ChangeSet}
import com.mongodb.BasicDBObject
import com.mongodb.client.MongoDatabase
import org.bson.Document

@ChangeLog
class CacheChangelog {
  private val collection = "cache"

  @ChangeSet(order = "001", id = "Internal Movements DB Baseline", author = "Steve Sugden")
  def dbBaseline(db: MongoDatabase): Unit = {}

  @ChangeSet(order = "002", id = "Add updated timestamp for existing cache objects", author = "Steve Sugden")
  def addUpdatedField(db: MongoDatabase): Unit = {
    val query = new Document
    val update = new Document("$set", new Document("updated", Date.from(Instant.now)))
    db.getCollection(collection).updateMany(new BasicDBObject(query), new BasicDBObject(update))
  }
}
