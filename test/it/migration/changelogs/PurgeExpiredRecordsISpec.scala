package migration.changelogs

import base.IntegrationTestMigrationToolSpec
import migration.changelogs.PurgeExpiredRecordsISpec.{cacheRecordWithMongoDate, cacheRecordWithStringDate}
import migrations.changelogs.MigrationDefinition
import migrations.changelogs.cache.PurgeExpiredRecords
import org.bson.Document

import scala.jdk.CollectionConverters.{IterableHasAsScala, SeqHasAsJava}

class PurgeExpiredRecordsISpec extends IntegrationTestMigrationToolSpec {

  override val collectionUnderTest: String = "cache"
  override val changeLog: MigrationDefinition = new PurgeExpiredRecords()

  override def beforeEach(): Unit = {
    super.beforeEach()
    removeAll(getCollection(collectionUnderTest))
  }

  "PurgeExpiredRecords" should {

    "remove any records where the 'updated' fields is of any type other than Date" when {

      "the collection has one correct and one expired record" in {
        val collection = getCollection(collectionUnderTest)
        collection.insertMany(Seq(Document.parse(cacheRecordWithMongoDate), Document.parse(cacheRecordWithStringDate)).asJava)

        changeLog.migrationFunction(database)

        val results = collection.find().asScala.toSeq
        results.size mustBe 1
        results.head mustBe Document.parse(cacheRecordWithMongoDate)
      }
    }
  }
}

object PurgeExpiredRecordsISpec {
  val cacheRecordWithMongoDate: String =
    """
      |{
      |  "_id": {
      |    "$oid": "649bee5d2d99186e40c4cc4e"
      |  },
      |  "providerId": "1234",
      |  "queryUcr": {
      |    "ucr": "9GB123999746000-DUCR12345-2",
      |    "ucrType": "DP"
      |  },
      |  "updated": {
      |    "$date": {
      |      "$numberLong": "1687940701946"
      |    }
      |  }
      |}""".stripMargin

  val cacheRecordWithStringDate: String =
    """
      |{
      |  "_id": {
      |    "$oid": "649c440bd01baabe97276564"
      |  },
      |  "providerId": "1234",
      |  "queryUcr": {
      |    "ucr": "9GB123999746000-DUCR12345-2",
      |    "ucrType": "DP"
      |  },
      |  "updated": "Wed Jun 28 2023 09:25:01 GMT+0100 (British Summer Time)"
      |}""".stripMargin

}
