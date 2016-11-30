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
import models.auth.{OrgAccount, UserAccount}
import org.mockito.Mockito._
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import repositories.UserRegisterRepository

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class UserRegisterServiceSpec extends PlaySpec with MockitoSugar with MongoMocks {

  val mockUserRegisterRepo = mock[UserRegisterRepository]

  val testUserAccount = UserAccount(Some("testID"),"testFirst","testLast","testUser","testEmail","testPass")
  val testOrgAccount = OrgAccount("testID","testFirst","testLast","testUser","testEmail","testPass")

  val mockSuccessWr = mockWriteResult(false)
  val mockNotSuccessWr = mockWriteResult(true)

  class Setup {
    object TestService extends UserRegisterService {
      val userRegisterRepo = mockUserRegisterRepo
    }
  }

  "UserRegisterService" should {
    "use the correct repo" in {
      UserRegisterService.userRegisterRepo mustBe UserRegisterRepository
    }

    "store the details of a new user" in new Setup {
      when(mockUserRegisterRepo.createNewUser(Matchers.eq(testUserAccount))(Matchers.any()))
        .thenReturn(Future.successful(mockSuccessWr))

      val result = TestService.storeNewUser(testUserAccount)

      result map {
        res => assert(res)
      }
    }

    "store the details of a new org user" in new Setup {
      when(mockUserRegisterRepo.createOrgUser(Matchers.eq(testOrgAccount))(Matchers.any()))
        .thenReturn(Future.successful(mockSuccessWr))

      val result = TestService.storeNewOrgUser(testOrgAccount)

      result map {
        res => assert(res)
      }
    }

    "Log an error attempting to store a new user" in new Setup {
      when(mockUserRegisterRepo.createNewUser(Matchers.eq(testUserAccount))(Matchers.any()))
        .thenReturn(Future.successful(mockNotSuccessWr))

      val result = TestService.storeNewUser(testUserAccount)

      result map {
        res => assert(!res)
      }
    }

    "Log an error attempting to store a new org user" in new Setup {
      when(mockUserRegisterRepo.createOrgUser(Matchers.eq(testOrgAccount))(Matchers.any()))
        .thenReturn(Future.successful(mockNotSuccessWr))

      val result = TestService.storeNewOrgUser(testOrgAccount)

      result map {
        res => assert(!res)
      }
    }

    "validate a registration" when {
      "given a user name" in new Setup {
        when(mockUserRegisterRepo.isUserNameInUse(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(Some(testUserAccount)))

        val result = Await.result(TestService.checkUserNameUsage("testUserName"), 5.seconds)
        result mustBe true
      }

      "given an email address" in new Setup {
        when(mockUserRegisterRepo.isEmailInUse(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(Some(testUserAccount)))

        val result = Await.result(TestService.checkEmailUsage("test@email.com"), 5.seconds)
        result mustBe true
      }
    }

    "invalidate a registration" when {
      "given a user name" in new Setup {
        when(mockUserRegisterRepo.isUserNameInUse(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(None))

        val result = Await.result(TestService.checkUserNameUsage("testUserName"), 5.seconds)
        result mustBe false
      }

      "given an email address" in new Setup {
        when(mockUserRegisterRepo.isEmailInUse(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(None))

        val result = Await.result(TestService.checkEmailUsage("test@email.com"), 5.seconds)
        result mustBe false
      }
    }
  }
}
