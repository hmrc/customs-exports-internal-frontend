/*
 * Copyright 2020 HM Revenue & Customs
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

package mongobee.changesets;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import java.util.concurrent.TimeUnit;

@ChangeLog
public class IleQueriesChangelog {
    private String collection = "ileQueries";

    @ChangeSet(order = "001", id = "Add ttl of 1 min", author = "Steve Sugden")
    public void addIleQueryTTL(MongoDatabase db) {

        /*
        Note:  Index may not exist if db has been dropped as it was previously created by the IleQueryRepository class
         */
        try {
            db.getCollection(collection).dropIndex("ttl");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        IndexOptions options = new IndexOptions().expireAfter(1L, TimeUnit.MINUTES).name("ttl");
        db.getCollection(collection).createIndex(Indexes.ascending("createdAt"), options);
    }

}