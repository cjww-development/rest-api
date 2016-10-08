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

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class UserRegisterRepositorySpec extends PlaySpec with OneAppPerSuite with MockitoSugar with MongoMocks with MongoCollections {

  val mockConnector = mock[MongoConnector]

  val testUserData = UserAccount("testAccId", "testFirstName", "testLastName", "testUserName", "test@email.com", "testPassword")
  val testOrgData = OrgAccount("testOAccId", "testOrgName", "testOrgUserName", "testInitials", "test@email.com", "testPassword")

  val mockWR : WriteResult = mockWriteResult()

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
      when(mockConnector.create[UserAccount](Matchers.eq(USER_ACCOUNTS), Matchers.eq(testUserData))(Matchers.eq(UserAccount.format)))
        .thenReturn(Future.successful(mockWR))

      val result = TestRepository.createNewUser(testUserData)

      result map {
        result => assert(!result.hasErrors)
      }
    }

    "create a new org account" in new Setup {
      when(mockConnector.create[OrgAccount](Matchers.eq(ORG_ACCOUNTS), Matchers.eq(testOrgData))(Matchers.eq(OrgAccount.format)))
        .thenReturn(Future.successful(mockWR))

      val result = TestRepository.createOrgUser(testOrgData)

      result map {
        result => assert(!result.hasErrors)
      }
    }
  }
}
