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

import models.auth.{Login, UserAccount}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.mockito.Mockito._
import org.mockito.Matchers
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.duration._
import repositories.LoginRepository

import scala.concurrent.{Await, Future}

class LoginServiceSpec extends PlaySpec with MockitoSugar with ScalaFutures {

  val mockLoginRepository = mock[LoginRepository]

  val testAccData = UserAccount(Some("testAccID"),"testFirstName","testLastName","testUserName","test@email.com","testPassword")

  class Setup {
    object TestService extends LoginService {
      val loginRepository = mockLoginRepository
    }
  }

  "getSingleUser" should {
    "return an encrypted user record if credentials are valid" in new Setup {
      when(mockLoginRepository.validateSingleUser(Matchers.eq(Login("testUserName","testPassword"))))
        .thenReturn(Future.successful(Some(testAccData)))

      val result = TestService.getSingleUser(Login("testUserName","testPassword"))
      val complete = Await.result(result, 5.seconds)

      assert(complete.endsWith("=="))
    }

    "return Unauthorised if credentials are invalid" in new Setup {
      when(mockLoginRepository.validateSingleUser(Matchers.eq(Login("testUser","testPass"))))
        .thenReturn(Future.successful(None))

      val result = TestService.getSingleUser(Login("testUser","testPass"))
      val complete = Await.result(result, 5.seconds)

      complete mustBe "Unauthorised"
    }
  }
}
