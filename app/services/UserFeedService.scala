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
package services

import models.account.FeedItem
import play.api.libs.json.{JsObject, Json}
import repositories.UserFeedRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object UserFeedService extends UserFeedService {
  val userFeedRepository = UserFeedRepository
}

trait UserFeedService {

  val userFeedRepository : UserFeedRepository

  def createFeedItem(feedItem: FeedItem) : Future[Boolean] = {
    userFeedRepository.createFeedItem(feedItem) map {
      _.hasErrors
    }
  }

  def getFeedList(userId : String) : Future[Option[JsObject]] = {
    userFeedRepository.getFeedItems(userId) map {
      list => convertToJsObject(list)
    }
  }

  def convertToJsObject(list : Option[List[FeedItem]]) : Option[JsObject] = {
    for {
      fi <- list
    } yield {
      Json.obj("feed-array" -> fi)
    }
  }
}
