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

import connectors.MongoConnector
import mocks.MongoMocks
import models.account.{EventDetail, FeedItem, SourceDetail}
import org.joda.time.DateTime
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import org.mockito.Mockito._
import org.mockito.Matchers

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class UserFeedRepositorySpec extends PlaySpec with MockitoSugar with OneAppPerSuite with MongoMocks {

  val mockMongoConnector = mock[MongoConnector]

  val writeResult = mockWriteResult(true)

  val feedItem =
    FeedItem(
      Some("testFeedID"),
      "testUserID",
      SourceDetail(
        "testService",
        "testLocation"
      ),
      EventDetail(
        "testTitle",
        "testDesc"
      ),
      DateTime.now()
    )

  val feedList = Some(List(feedItem))

  class Setup {
    object TestRepository extends UserFeedRepository {
      val mongoConnector = mockMongoConnector
    }
  }

  "createFeedItem" should {
    "return a WriteResult" when {
      "given a feedItem" in new Setup {
        when(mockMongoConnector.create[FeedItem](Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(writeResult))

        val result = Await.result(TestRepository.createFeedItem(feedItem), 5.seconds)
        result mustBe writeResult
      }
    }
  }

  "getFeedItems" should {
    "return an optional list of feed items" when {
      "given a userID" in new Setup {
        when(mockMongoConnector.readBulk[FeedItem](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(feedList))

        val result = Await.result(TestRepository.getFeedItems("testUserID"), 5.seconds)
        result mustBe feedList
      }
    }
  }
}
