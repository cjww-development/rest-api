/*
* Copyright 2017 HM Revenue & Customs
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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import controllers.traits.account.{AccountDetailsCtrl, UserFeedCtrl}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.ws.ahc.AhcWSClient
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserFeedService

class UserFeedControllerSpec extends PlaySpec with OneAppPerSuite with MockitoSugar {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val ws = AhcWSClient()

  val mockUserFeedService = mock[UserFeedService]

  class Setup {
    class TestController extends UserFeedCtrl {
      val userFeedService = mockUserFeedService
    }

    val testController = new TestController()
  }

  "createEvent" should {
    "return FORBIDDEN" when {
      "getting without an appID" in new Setup {
        val result = testController.createEvent()(FakeRequest().withHeaders(CONTENT_TYPE -> "text/plain")).run()
        status(result) mustBe FORBIDDEN
      }
    }
  }

  "retrieveFeed" should {
    "return FORBIDDEN" when {
      "getting without an appID" in new Setup {
        val result = testController.retrieveFeed()(FakeRequest().withHeaders(CONTENT_TYPE -> "text/plain")).run()
        status(result) mustBe FORBIDDEN
      }
    }
  }
}
