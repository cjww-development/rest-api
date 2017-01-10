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
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import repositories.UserFeedRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object UserFeedService extends UserFeedService {
  val userFeedRepository = UserFeedRepository
}

trait UserFeedService {

  val userFeedRepository : UserFeedRepository

  private val MIN = 0
  private val MAX = 10

  def createFeedItem(feedItem: FeedItem) : Future[Boolean] = {
    userFeedRepository.createFeedItem(feedItem) map {
      _.hasErrors
    }
  }

  def flipList(list : Option[List[FeedItem]]) : Option[List[FeedItem]] = {
    list.isDefined match {
      case false => None
      case true => Some(list.get.reverse.slice(MIN, MAX))
    }
  }

  def getFeedList(userId : String) : Future[Option[JsObject]] = {
    userFeedRepository.getFeedItems(userId) map {
      list =>
        convertToJsObject(flipList(list))
    }
  }

  def convertToJsObject(list : Option[List[FeedItem]]) : Option[JsObject] = {
    for {
      fi <- list
    } yield {
      val obj = Json.obj("feed-array" -> fi)
      Logger.debug(s"[UserFeedService] - [convertToJsObject] : $obj")
      obj
    }
  }
}
