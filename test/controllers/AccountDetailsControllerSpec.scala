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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import config.ConfigurationStrings
import controllers.traits.account.AccountDetailsCtrl
import fixtures.PayloadFixtures
import org.mockito.Mockito._
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.ws.ahc.AhcWSClient
import play.api.test.FakeRequest
import services.AccountService
import play.api.test.Helpers._

import scala.concurrent.Future

class AccountDetailsControllerSpec extends PlaySpec with OneAppPerSuite with MockitoSugar with ConfigurationStrings with PayloadFixtures {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val ws = AhcWSClient()

  val mockAccountService = mock[AccountService]

  class Setup {
    class TestController extends AccountDetailsCtrl {
      val accountService = mockAccountService
    }

    val testController = new TestController
  }

  "updateProfileInformation" should {
    "return FORBIDDEN" when {
      "POSTing without an appID" in new Setup {
        val result = testController.updateProfileInformation()(FakeRequest().withHeaders(CONTENT_TYPE -> "text/plain")).run()
        status(result) mustBe FORBIDDEN
      }
    }

    "return a bad request" when {
      "given an invalid encrypted value" in new Setup {
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> "text/plain", "appID" -> AUTH_ID).withBody("invalidEncValue")
        val result = testController.updateProfileInformation()(request)
        status(result) mustBe BAD_REQUEST
      }
    }

    "return an OK" when {
      "the given data has been validated and been used to update the user profile" in new Setup {
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> "text/plain", "appID" -> AUTH_ID).withBody(validUserProfilePayload)

        when(mockAccountService.updateProfileInformation(Matchers.eq(validUserProfileModel)))
          .thenReturn(Future.successful(false))

        val result = testController.updateProfileInformation()(request)
        status(result) mustBe OK
      }
    }

    "return an INTERNAL SERVER ERROR" when {
      "the given data has been validated but updating the user profile has failed" in new Setup {
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> "text/plain", "appID" -> AUTH_ID).withBody(validUserProfilePayload)

        when(mockAccountService.updateProfileInformation(Matchers.eq(validUserProfileModel)))
          .thenReturn(Future.successful(true))

        val result = testController.updateProfileInformation()(request)
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "updateUserPassword" should {
    "return FORBIDDEN" when {
      "POSTing without an appID" in new Setup {
        val result = testController.updateUserPassword()(FakeRequest().withHeaders(CONTENT_TYPE -> "text/plain")).run()
        status(result) mustBe FORBIDDEN
      }
    }

    "return a bad request" when {
      "given an invalid encrypted value" in new Setup {
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> "text/plain", "appID" -> AUTH_ID).withBody("invalidEncValue")
        val result = testController.updateUserPassword()(request)
        status(result) mustBe BAD_REQUEST
      }
    }
  }

  "updateUserSettings" should {
    "return FORBIDDEN" when {
      "POSTing without an appID" in new Setup {
        val result = testController.updateUserSettings()(FakeRequest().withHeaders(CONTENT_TYPE -> "text/plain")).run()
        status(result) mustBe FORBIDDEN
      }
    }

    "return a bad request" when {
      "given an invalid encrypted value" in new Setup {
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> "text/plain", "appID" -> AUTH_ID).withBody("invalidEncValue")
        val result = testController.updateUserSettings()(request)
        status(result) mustBe BAD_REQUEST
      }
    }
  }
}
