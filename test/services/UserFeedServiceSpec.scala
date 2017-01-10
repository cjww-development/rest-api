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

import mocks.MongoMocks
import models.account.{EventDetail, FeedItem, SourceDetail}
import org.joda.time.DateTime
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsObject, Json}
import repositories.UserFeedRepository
import org.mockito.Mockito._
import org.mockito.Matchers

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class UserFeedServiceSpec extends PlaySpec with MockitoSugar with MongoMocks {

  val mockUserFeedRepo = mock[UserFeedRepository]

  val successWR = mockWriteResult(true)

  class Setup {
    object TestService extends UserFeedService {
      val userFeedRepository = mockUserFeedRepo
    }

    val expected = """{"feed-array":[{"_id" : "feedID1","userId" : "testUserId","sourceDetail" : {"service" : "aaa","location" : "bbb"},"eventDetail" : {"title" : "ccc","description":"ddd"},"generated":{"$date":1483358400000}},{"_id":"feedID2","userId":"testUserId","sourceDetail":{"service":"eee","location":"fff"},"eventDetail":{"title":"ggg","description":"hhh"},"generated":{"$date":1483272000000}}]}"""

    val testList =
      Some(
        List(
          FeedItem(Some("feedID1"), "testUserId", SourceDetail("aaa","bbb"), EventDetail("ccc","ddd"), DateTime.parse("2017-01-02T12:00:00Z")),
          FeedItem(Some("feedID2"), "testUserId", SourceDetail("eee","fff"), EventDetail("ggg","hhh"), DateTime.parse("2017-01-01T12:00:00Z"))
        )
      )

    val testFeedItem = FeedItem(Some("feedID1"), "testUserId", SourceDetail("aaa","bbb"), EventDetail("ccc","ddd"), DateTime.parse("2017-01-01T12:00:00Z"))

  }

  "createFeedItem" should {
    "return a boolean" when {
      "given a feeditem" in new Setup {
        when(mockUserFeedRepo.createFeedItem(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(successWR))

        val result = Await.result(TestService.createFeedItem(testFeedItem), 5.seconds)
        result mustBe false
      }
    }
  }

  "getFeedList" should {
    "return an optional JsObject" when {
      "given a userID" in new Setup {
        when(mockUserFeedRepo.getFeedItems(Matchers.any()))
          .thenReturn(Future.successful(Some(testList.get.reverse)))

        val result = Await.result(TestService.getFeedList("testUserId"), 5.seconds)
        result mustBe Some(Json.parse(expected).as[JsObject])
      }
    }
  }

  "convertToJsObject" should {
    "print the feed items in the list" in new Setup {
      val result = TestService.convertToJsObject(testList)

      result.get.as[JsObject].toString() mustBe Json.parse(expected).as[JsObject].toString()
    }
  }
}
