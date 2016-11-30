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

package controllers

import controllers.traits.auth.RegisterCtrl
import controllers.auth.RegisterController
import fixtures.PayloadFixtures
import models.auth.{OrgAccount, UserAccount}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.test.FakeRequest
import services.UserRegisterService
import play.api.test.Helpers._
import akka.actor._
import akka.stream._
import config.ConfigurationStrings
import play.api.libs.ws.ahc._

import scala.concurrent.Future

class UserRegisterControllerSpec extends PlaySpec with OneAppPerSuite with MockitoSugar with PayloadFixtures with ScalaFutures with ConfigurationStrings {

  val mockUserRegisterService = mock[UserRegisterService]

  val testAccData = UserAccount(Some("testAccID"),"testFirstName","testLastName","testUserName","test@email.com","testPassword")
  val testOrgAccData = OrgAccount("testAccID","testFirstName","testLastName","testUserName","test@email.com","testPassword")

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val ws = AhcWSClient()

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
      val result = testController.createUserAccount()(FakeRequest().withHeaders(CONTENT_TYPE -> TEXT))

      status(result.run()) mustBe FORBIDDEN
    }

    "return a not found if the payload cannot be decrypted into the specified case class" in new Setup {
      val result =
        testController.createUserAccount()(FakeRequest()
          .withHeaders("appID" -> AUTH_ID, CONTENT_TYPE -> TEXT)
          .withBody(invalidPayload))

      status(result) mustBe BAD_REQUEST
    }

    "return an internal server error if the payload cannot be saved into mongo" in new Setup {
      when(mockUserRegisterService.storeNewUser(Matchers.any()))
        .thenReturn(Future.successful(true))

      val result =
        testController.createUserAccount()(FakeRequest()
          .withHeaders("appID" -> AUTH_ID, CONTENT_TYPE -> TEXT)
          .withBody(validUserAccountPayload))

      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "return a created if the payload is stored in mongo" in new Setup {
      when(mockUserRegisterService.storeNewUser(Matchers.any()))
        .thenReturn(Future.successful(false))

      val result =
        testController.createUserAccount()(FakeRequest()
          .withHeaders("appID" -> AUTH_ID, CONTENT_TYPE -> TEXT)
          .withBody(validUserAccountPayload))

      status(result) mustBe CREATED
    }
  }

  "POSTing to the createOrgAccount" should {
    "return a forbidden if appID cannot be found in the header" in new Setup {
      val result = testController.createOrgAccount()(FakeRequest()
        .withHeaders(CONTENT_TYPE -> TEXT))

      status(result.run()) mustBe FORBIDDEN
    }

    "return a not found if the payload cannot be decrypted into the specified case class" in new Setup {
      val result =
        testController.createOrgAccount()(FakeRequest()
          .withHeaders("appID" -> AUTH_ID, CONTENT_TYPE -> TEXT)
          .withBody(invalidPayload))

      status(result) mustBe BAD_REQUEST
    }

    "return an internal server error if the payload cannot be saved into mongo" in new Setup {
      when(mockUserRegisterService.storeNewOrgUser(Matchers.eq(testOrgAccData)))
        .thenReturn(Future.successful(true))

      val result =
        testController.createOrgAccount()(FakeRequest()
          .withHeaders("appID" -> AUTH_ID, CONTENT_TYPE -> TEXT)
          .withBody(validOrgAccountPayload))

      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "return a created if the payload is stored in mongo" in new Setup {
      when(mockUserRegisterService.storeNewOrgUser(Matchers.eq(testOrgAccData)))
        .thenReturn(Future.successful(false))

      val result =
        testController.createOrgAccount()(FakeRequest()
          .withHeaders("appID" -> AUTH_ID, CONTENT_TYPE -> TEXT)
          .withBody(validOrgAccountPayload))

      status(result) mustBe CREATED
    }
  }

  "checkUserNameUsage" should {
    "return a FORBIDDEN if appID cannot be found in the header" in new Setup {
      val result = testController.checkUserNameUsage()(FakeRequest()
        .withHeaders(CONTENT_TYPE -> TEXT))

      status(result.run()) mustBe FORBIDDEN
    }

    "return a OK with the usage" in new Setup {
      when(mockUserRegisterService.checkUserNameUsage(Matchers.eq("testUserName")))
        .thenReturn(Future.successful(false))

      val result = testController.checkUserNameUsage()(FakeRequest()
        .withHeaders(CONTENT_TYPE -> TEXT, "appID" -> AUTH_ID)
        .withBody(encUserName))

      status(result) mustBe OK
    }
  }

  "checkEmailUsage" should {
    "return a FORBIDDEN if appID cannot be found in the header" in new Setup {
      val result = testController.checkEmailUsage()(FakeRequest()
        .withHeaders(CONTENT_TYPE -> TEXT))

      status(result.run()) mustBe FORBIDDEN
    }

    "return a OK with the usage" in new Setup {
      when(mockUserRegisterService.checkEmailUsage(Matchers.eq("testUserName")))
        .thenReturn(Future.successful(false))

      val result = testController.checkEmailUsage()(FakeRequest()
        .withHeaders(CONTENT_TYPE -> TEXT, "appID" -> AUTH_ID)
        .withBody(encUserName))

      status(result) mustBe OK
    }
  }
}
