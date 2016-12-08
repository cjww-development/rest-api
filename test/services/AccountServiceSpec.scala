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
import models.account.UserProfile
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import org.mockito.Mockito._
import org.mockito.Matchers
import repositories.AccountDetailsRepository

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class AccountServiceSpec extends PlaySpec with OneAppPerSuite with MockitoSugar with MongoMocks {

  val mockAccountDetailsRepo = mock[AccountDetailsRepository]

  val testData = UserProfile("testFirstName","testLastName","testUserName","test@email.com")

  val successUWR = mockUpdateWriteResult(false)
  val failedUWR = mockUpdateWriteResult(true)

  class Setup {
    object TestService extends AccountService {
      val accountDetailsRepo = mockAccountDetailsRepo
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
}