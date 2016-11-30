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
import models.auth.{OrgAccount, UserAccount}
import play.api.Logger
import play.api.libs.json.OFormat
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object UserRegisterRepository extends UserRegisterRepository {
  val mongoConnector = MongoConnector
}

trait UserRegisterRepository extends MongoCollections {

  val mongoConnector : MongoConnector

  def createNewUser(newUser : UserAccount)(implicit format : OFormat[UserAccount]) : Future[WriteResult] = {
    mongoConnector.create[UserAccount](USER_ACCOUNTS, newUser.withAccountID)
  }

  def createOrgUser(newOrgUser : OrgAccount)(implicit format : OFormat[OrgAccount]) : Future[WriteResult] = {
    mongoConnector.create[OrgAccount](ORG_ACCOUNTS, newOrgUser)
  }

  def isUserNameInUse(username : String)(implicit format: OFormat[UserAccount]) : Future[Option[UserAccount]] = {
    mongoConnector.read[UserAccount](USER_ACCOUNTS, BSONDocument("userName" -> username)) map {
      res =>
        // $COVERAGE-OFF$
        if(res.isDefined) Logger.info(s"[UserRegisterRepository] - [isUserNameInUse] The user name $username is already in use on this system")
        // $COVERAGE-ON$
        res
    }
  }

  def isEmailInUse(email : String)(implicit format: OFormat[UserAccount]) : Future[Option[UserAccount]] = {
    mongoConnector.read[UserAccount](USER_ACCOUNTS, BSONDocument("email" -> email)) map {
      res =>
        // $COVERAGE-OFF$
        if(res.isDefined) Logger.info(s"[UserRegisterRepository] - [isEmailInUse] The email address $email is already in use on this system")
        // $COVERAGE-ON$
        res
    }
  }
}
