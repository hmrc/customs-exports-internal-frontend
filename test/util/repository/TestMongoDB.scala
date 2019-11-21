package repository

import play.api.Configuration

trait TestMongoDB {

  protected val mongoConfiguration: Configuration =
    Configuration.from(Map("mongodb.uri" -> "mongodb://localhost:27017/test-customs-exports-internal"))

}
