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
import mocks.MongoMocks
import models.auth.{OrgAccount, UserAccount}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import reactivemongo.api.commands.WriteResult

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class UserRegisterRepositorySpec extends PlaySpec with OneAppPerSuite with MockitoSugar with MongoMocks with MongoCollections {

  val mockConnector = mock[MongoConnector]

  val testUserData = UserAccount(None, "testFirstName", "testLastName", "testUserName", "test@email.com", "testPassword", None)
  val testOrgData = OrgAccount("testOAccId", "testOrgName", "testOrgUserName", "testInitials", "test@email.com", "testPassword")

  val mockWR : WriteResult = mockWriteResult(false)

  class Setup {
    object TestRepository extends UserRegisterRepository {
      val mongoConnector = mockConnector
    }
  }

  "UserRegisterRepository" should {
    "use the correct mongoConnector" in {
      UserRegisterRepository.mongoConnector mustBe MongoConnector
    }
  }

  "POSTing a new user" should {
    "create a new user account" in new Setup {
      when(mockConnector.create[UserAccount](Matchers.eq(USER_ACCOUNTS), Matchers.any())(Matchers.eq(UserAccount.format)))
        .thenReturn(Future.successful(mockWR))

      val result = TestRepository.createNewUser(testUserData)

      val complete = Await.result(result, 5.seconds)
      assert(!complete.hasErrors)
    }

    "create a new org account" in new Setup {
      when(mockConnector.create[OrgAccount](Matchers.eq(ORG_ACCOUNTS), Matchers.eq(testOrgData))(Matchers.eq(OrgAccount.format)))
        .thenReturn(Future.successful(mockWR))

      val result = Await.result(TestRepository.createOrgUser(testOrgData), 5.seconds)
      result.hasErrors mustBe false
    }
  }

  "Validating a registration" should {
    "return an optional account" when {
      "validating a user name" in new Setup {
        when(mockConnector.read[UserAccount](Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(Some(testUserData)))

        val result = Await.result(TestRepository.isUserNameInUse("testUserName"), 5.seconds)
        result mustBe Some(testUserData)
      }

      "validating a email" in new Setup {
        when(mockConnector.read[UserAccount](Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(Some(testUserData)))

        val result = Await.result(TestRepository.isEmailInUse("test@email.com"), 5.seconds)
        result mustBe Some(testUserData)
      }
    }
  }
}
