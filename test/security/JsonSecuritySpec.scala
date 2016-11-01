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

package security

import models.auth.{Login, OrgAccount, UserAccount}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json

class JsonSecuritySpec extends PlaySpec {

  class Setup {
    object TestSec extends JsonSecurity

    case class TestModel(data : String)
    implicit val format = Json.format[TestModel]
    val testData = TestModel("TestString")
    val testAccData = UserAccount(Some("testAccID"),"testFirstName","testLastName","testUserName","test@email.com","testPassword")
    val testOrgAccData = OrgAccount("testAccID","testFirstName","testLastName","testUserName","test@email.com","testPassword")
    val testLogin = Login("testUserName","testPassword")
  }

  "Enc and Dec a model" should {
    "return the same as what was input" in new Setup {
      val enc = TestSec.encryptModel[TestModel](testData)
      val dec = TestSec.decryptInto[TestModel](enc.get)
      assert(dec.get == testData)
      assert(dec.get.data == testData.data)
    }

    "return a live system UserAccount model" in new Setup {
      val enc = TestSec.encryptModel[UserAccount](testAccData)
      val dec = TestSec.decryptInto[UserAccount](enc.get)
      assert(dec.get == testAccData)
      assert(dec.get.email == testAccData.email)
    }

    "return a live system OrgAccount model" in new Setup {
      val enc = TestSec.encryptModel[OrgAccount](testOrgAccData)
      val dec = TestSec.decryptInto[OrgAccount](enc.get)
      assert(dec.get == testOrgAccData)
      assert(dec.get.email == testAccData.email)
    }

    "return a live system Login model" in new Setup {
      val enc = TestSec.encryptModel[Login](testLogin)
      val dec = TestSec.decryptInto[Login](enc.get)
      assert(dec.get == testLogin)
      assert(dec.get.userName == testLogin.userName)
      assert(dec.get.password == testLogin.password)
    }

    "return none if input cannot be decrypted" in new Setup {
      val dec = TestSec.decryptInto[TestModel]("invalidTestData")
      dec mustBe None
    }
  }
}
