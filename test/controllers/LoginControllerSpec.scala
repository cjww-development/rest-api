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
import controllers.traits.auth.LoginCtrl
import play.api.test.Helpers._
import fixtures.{PayloadFixtures, RequestFixture}
import models.auth.Login
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.ws.ahc.AhcWSClient
import org.mockito.Mockito._
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import services.LoginService

import scala.concurrent.Future

class LoginControllerSpec extends PlaySpec with OneAppPerSuite with RequestFixture with PayloadFixtures with MockitoSugar {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val ws = AhcWSClient()

  val mockLoginService = mock[LoginService]

  val testLogin = Login("testUserName","testPassword")
  val testInvalidLogin = Login("testUser","testPassword")

  class Setup {
    class TestController extends LoginCtrl {
      val loginService = mockLoginService
    }

    val testController = new TestController
  }

  "Sending a GET request to userLogin" should {
    "return a forbidden if no appID is found" in new Setup {
      val result = testController.userLogin()(forbiddenRequest)

      status(result.run()) mustBe FORBIDDEN
    }

    "return an unauthorised if the users credentials can't be validated" in new Setup {

      when(mockLoginService.getSingleUser(Matchers.eq(testInvalidLogin)))
        .thenReturn(Future.successful("Unauthorised"))

      val result = testController.userLogin()(payloadRequest(invalidLoginPayload))

      status(result) mustBe UNAUTHORIZED
    }

    "return an ok with the details to hold in the session if the user is successful validated" in new Setup {

      when(mockLoginService.getSingleUser(Matchers.eq(testLogin)))
        .thenReturn(Future.successful(testLogin.toString))

      val result = testController.userLogin()(payloadRequest(validLoginPayload))

      status(result) mustBe OK
    }
  }
}
