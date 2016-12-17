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
import models.account.{AccountSettings, UpdatedPassword, UserProfile}
import models.auth.UserAccount
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import org.mockito.Mockito._
import org.mockito.Matchers

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class AccountDetailsRepositorySpec extends PlaySpec with OneAppPerSuite with MockitoSugar with MongoMocks {

  val mockMongoConnector = mock[MongoConnector]

  val successUWR = mockUpdateWriteResult(false)
  val failedUWR = mockUpdateWriteResult(true)

  val testData = UserProfile("testFirstName", "testLastName", "testUserName", "test@email.com", None, None)

  class Setup {
    object TestRepository extends AccountDetailsRepository {
      val mongoConnector = mockMongoConnector
    }
  }

  "updateAccountData" should {
    "return an UpdateWriteResult" when {
      "given a user profile and is successful" in new Setup {
        when(mockMongoConnector.update(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(successUWR))

        val result = Await.result(TestRepository.updateAccountData(testData), 5.seconds)
        result mustBe successUWR
      }

      "given a user profile and failed" in new Setup {
        when(mockMongoConnector.update(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(failedUWR))

        val result = Await.result(TestRepository.updateAccountData(testData), 5.seconds)
        result mustBe failedUWR
      }
    }
  }

  "findPassword" should {
    "return false" when {
      "a matching user cannot be found" in new Setup {
        when(mockMongoConnector.read[UserAccount](Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(None))

        val set = UpdatedPassword("testUserId", "testOldPassword", "testNewPassword")

        val result = Await.result(TestRepository.findPassword(set), 5.seconds)
        result mustBe false
      }

      "a matching users password does not match the old password from the set" in new Setup {
        val user = UserAccount(Some("testUserId"), "testFirstName","testLastName","testUserName","testEmail","testPassword", None)
        val set = UpdatedPassword("testUserId", "testOldPassword", "testNewPassword")

        when(mockMongoConnector.read[UserAccount](Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(Some(user)))

        val result = Await.result(TestRepository.findPassword(set), 5.seconds)
        result mustBe false
      }
    }

    "return true" when {
      "a matching user is found and their password the old password from the set" in new Setup {
        val user = UserAccount(Some("testUserId"), "testFirstName","testLastName","testUserName","testEmail","testPassword", None)
        val set = UpdatedPassword("testUserId", "testPassword", "testNewPassword")

        when(mockMongoConnector.read[UserAccount](Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(Some(user)))

        val result = Await.result(TestRepository.findPassword(set), 5.seconds)
        result mustBe true
      }
    }
  }

  "updatePassword" should {
    "return an UpdateWriteResult" when {
      "given a password set" in new Setup {
        when(mockMongoConnector.update(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(successUWR))

        val set = UpdatedPassword("testUserId", "testPassword", "testNewPassword")

        val result = Await.result(TestRepository.updatePassword(set), 5.seconds)
        result mustBe successUWR
      }
    }
  }

  "updateSettings" should {
    "return an UpdateWriteResult" when {
      "given an AccountSetting" in new Setup {
        when(mockMongoConnector.update(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(successUWR))

        val settings = AccountSettings("testUserId", Map("displayName" -> "testValue"))

        val result = Await.result(TestRepository.updateSettings(settings), 5.seconds)
        result mustBe successUWR
      }
    }
  }
}
