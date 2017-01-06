// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package repositories

import config.MongoCollections
import connectors.MongoConnector
import models.account.FeedItem
import play.api.libs.json.{JsObject, OFormat}
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future

object UserFeedRepository extends UserFeedRepository {
  val mongoConnector = MongoConnector
}

trait UserFeedRepository extends MongoCollections {

  val mongoConnector : MongoConnector

  def createFeedItem(feedItem : FeedItem)(implicit format : OFormat[FeedItem]) : Future[WriteResult] = {
    mongoConnector.create[FeedItem](USER_FEED, feedItem.withId)
  }

  def getFeedItems(userId : String) : Future[Option[List[FeedItem]]] = {
    val query = BSONDocument("userId" -> userId)
    mongoConnector.readBulk[FeedItem](USER_FEED, query, MAX_USER_FEED)
  }
}
