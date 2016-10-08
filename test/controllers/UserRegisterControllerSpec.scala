/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import controllers.traits.userregister.RegisterCtrl
import controllers.userregister.RegisterController
import fixtures.PayloadFixtures
import models.auth.{OrgAccount, UserAccount}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.mvc.Headers
import play.api.test.FakeRequest
import services.UserRegisterService
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserRegisterControllerSpec extends PlaySpec with OneAppPerSuite with MockitoSugar with PayloadFixtures {

  val mockUserRegisterService = mock[UserRegisterService]

  val testAccData = UserAccount("testAccID","testFirstName","testLastName","testUserName","test@email.com","testPassword")
  val testOrgAccData = OrgAccount("testAccID","testFirstName","testLastName","testUserName","test@email.com","testPassword")

  class Setup {
    class TestController extends RegisterCtrl {
      val userRegisterService = mockUserRegisterService
    }

    val testController = new TestController
  }

  "RegisterCtrl" should {
    "use the correct service" in {
      val controller = new RegisterController
      controller.userRegisterService mustBe UserRegisterService
    }
  }

  "POSTing to the createUserAccount" should {
    "return a forbidden if appID cannot be found in the header" in new Setup {
      val result = testController.createUserAccount()(FakeRequest())

      result.map {
        res => res.header.status mustBe FORBIDDEN
      }
    }

    "return a not found if no request body can be found" in new Setup {
      val result = testController.createUserAccount()(FakeRequest().withHeaders("appID" -> "FAKE_APP_ID"))

      result.map {
        res => res.header.status mustBe NOT_FOUND
      }
    }

    "return a bad request if the payload cannot be decrypted into the specified case class" in new Setup {
      val result =
        testController.createUserAccount()(FakeRequest()
          .withHeaders("appID" -> "FAKE_APP_ID")
          .withBody(invalidPayload))

      result.map {
        res => res.header.status mustBe BAD_REQUEST
      }
    }

    "return an internal server error if the payload cannot be saved into mongo" in new Setup {
      when(mockUserRegisterService.storeNewUser(Matchers.eq(testAccData)))
        .thenReturn(Future.successful(true))

      val result =
        testController.createUserAccount()(FakeRequest()
          .withHeaders("appID" -> "FAKE_APP_ID")
          .withBody(validUserAccountPayload))

      result.map {
        res => res.header.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return a created if the payload is stored in mongo" in new Setup {
      when(mockUserRegisterService.storeNewUser(Matchers.eq(testAccData)))
        .thenReturn(Future.successful(false))

      val result =
        testController.createUserAccount()(FakeRequest()
          .withHeaders("appID" -> "FAKE_APP_ID")
          .withBody(validUserAccountPayload))

      result.map {
        res => res.header.status mustBe CREATED
      }
    }
  }

  "POSTing to the createOrgAccount" should {
    "return a forbidden if appID cannot be found in the header" in new Setup {
      val result = testController.createOrgAccount()(FakeRequest())

      result.map {
        res => res.header.status mustBe FORBIDDEN
      }
    }

    "return a not found if no request body can be found" in new Setup {
      val result = testController.createOrgAccount()(FakeRequest().withHeaders("appID" -> "FAKE_APP_ID"))

      result.map {
        res => res.header.status mustBe NOT_FOUND
      }
    }

    "return a bad request if the payload cannot be decrypted into the specified case class" in new Setup {
      val result =
        testController.createOrgAccount()(FakeRequest()
          .withHeaders("appID" -> "FAKE_APP_ID")
          .withBody(invalidPayload))

      result.map {
        res => res.header.status mustBe BAD_REQUEST
      }
    }

    "return an internal server error if the payload cannot be saved into mongo" in new Setup {
      when(mockUserRegisterService.storeNewOrgUser(Matchers.eq(testOrgAccData)))
        .thenReturn(Future.successful(true))

      val result =
        testController.createOrgAccount()(FakeRequest()
          .withHeaders("appID" -> "FAKE_APP_ID")
          .withBody(validUserAccountPayload))

      result.map {
        res => res.header.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return a created if the payload is stored in mongo" in new Setup {
      when(mockUserRegisterService.storeNewOrgUser(Matchers.eq(testOrgAccData)))
        .thenReturn(Future.successful(false))

      val result =
        testController.createOrgAccount()(FakeRequest()
          .withHeaders("appID" -> "FAKE_APP_ID")
          .withBody(validUserAccountPayload))

      result.map {
        res => res.header.status mustBe CREATED
      }
    }
  }
}
