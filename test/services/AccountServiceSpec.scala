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
import models.account.{AccountSettings, UpdatedPassword, UserProfile}
import models.auth.UserAccount
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import org.mockito.Mockito._
import org.mockito.Matchers
import play.api.libs.json.Json
import repositories.AccountDetailsRepository
import security.JsonSecurity

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class AccountServiceSpec extends PlaySpec with OneAppPerSuite with MockitoSugar with MongoMocks {

  val mockAccountDetailsRepo = mock[AccountDetailsRepository]

  val testData = UserProfile("testFirstName","testLastName","testUserName","test@email.com", None, None)
  val testAccData = UserAccount(Some("testAccID"),"testFirstName","testLastName","testUserName","test@email.com","testPassword", None)


  val successUWR = mockUpdateWriteResult(false)
  val failedUWR = mockUpdateWriteResult(true)

  class Setup {
    object TestService extends AccountService {
      val accountDetailsRepo = mockAccountDetailsRepo
    }
  }

  "getAccount" should {
    "return an optional string" when {
      "given a userID" in new Setup {
        when(mockAccountDetailsRepo.getAccount(Matchers.any()))
          .thenReturn(Future.successful(Some(testAccData)))

        val result = Await.result(TestService.getAccount("testAccID"), 5.seconds)
        result mustBe JsonSecurity.encryptModel(testAccData)
      }

      "given a userID but it cant find an account" in new Setup {
        when(mockAccountDetailsRepo.getAccount(Matchers.any()))
          .thenReturn(Future.successful(None))

        val result = Await.result(TestService.getAccount("testAccID"), 5.seconds)
        result mustBe None
      }
    }
  }

  "updateProfileInformation" should {
    "return false" in new Setup {
      when(mockAccountDetailsRepo.updateAccountData(Matchers.any()))
        .thenReturn(Future.successful(successUWR))

      val result = Await.result(TestService.updateProfileInformation(testData), 5.seconds)
      result mustBe false
    }
  }

  "updatePassword" should {
    "return an InvalidOldPassword" when {
      "the previous password in the set does not match the password in mongo" in new Setup {
        when(mockAccountDetailsRepo.findPassword(Matchers.any()))
          .thenReturn(Future.successful(false))

        val set = UpdatedPassword("testUserId","testOldPassword","testNewPassword")

        val result = Await.result(TestService.updatePassword(set), 5.seconds)
        result mustBe InvalidOldPassword
      }
    }

    "return a PasswordUpdate" when {
      "the password has been updated" in new Setup {
        when(mockAccountDetailsRepo.findPassword(Matchers.any()))
          .thenReturn(Future.successful(true))

        when(mockAccountDetailsRepo.updatePassword(Matchers.any()))
          .thenReturn(Future.successful(successUWR))

        val set = UpdatedPassword("testUserId","testOldPassword","testNewPassword")

        val result = Await.result(TestService.updatePassword(set), 5.seconds)
        result mustBe PasswordUpdate(false)
      }
    }
  }

  "updateSettings" should {
    "return an UpdatedSettingsResponse" when {
      "given an AccountSettings" in new Setup {
        when(mockAccountDetailsRepo.updateSettings(Matchers.any()))
          .thenReturn(Future.successful(successUWR))

        val settings = AccountSettings("testUserId", Map("displayName" -> "testValue"))

        val result = Await.result(TestService.updateSettings(settings), 5.seconds)
        result mustBe UpdatedSettingsSuccess
      }
    }
  }
}