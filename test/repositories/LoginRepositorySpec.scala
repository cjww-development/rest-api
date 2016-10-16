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
import models.auth.{Login, UserAccount}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.mockito.Mockito._
import org.mockito.Matchers
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.duration._
import reactivemongo.bson.BSONDocument

import scala.concurrent.{Await, Future}

class LoginRepositorySpec extends PlaySpec with MockitoSugar with MongoCollections with ScalaFutures {

  val mockMongoConnector = mock[MongoConnector]

  val testAccData = UserAccount("testAccID","testFirstName","testLastName","testUserName","test@email.com","testPassword")

  class Setup {
    object TestRepository extends LoginRepository {
      val mongoConnector = mockMongoConnector
    }
  }

  "getSingleUser" should {
    "return a user record if credentials are correct" in new Setup {
      when(mockMongoConnector.read[UserAccount](
        Matchers.eq(USER_ACCOUNTS),
        Matchers.eq(BSONDocument("userName" -> testAccData.userName, "password" -> testAccData.password)))(Matchers.any()))
        .thenReturn(Future.successful(Some(testAccData)))

      val result = TestRepository.validateSingleUser(Login("testUserName","testPassword"))
      val complete = Await.result(result, 5.seconds)

      complete.get.userName mustBe "testUserName"
      complete.get.password mustBe "testPassword"
    }

    "return none if credentials are not correct" in new Setup {
      when(mockMongoConnector.read[UserAccount](
        Matchers.eq(USER_ACCOUNTS),
        Matchers.eq(BSONDocument("userName" -> testAccData.userName, "password" -> testAccData.password)))(Matchers.any()))
        .thenReturn(Future.successful(None))

      val result = TestRepository.validateSingleUser(Login("testUserName","testPassword"))
      val complete = Await.result(result, 5.seconds)

      complete mustBe None
    }
  }
}
