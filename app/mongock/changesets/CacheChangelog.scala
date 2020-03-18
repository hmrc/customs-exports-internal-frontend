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
