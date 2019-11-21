package repository

import org.scalatest.{MustMatchers, WordSpec}
import uk.gov.hmrc.mongo.MongoSpecSupport

abstract class RepositorySpec extends WordSpec with MustMatchers with MongoSpecSupport { self =>

}
