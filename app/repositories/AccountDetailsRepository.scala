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
import models.account.{UpdatedPassword, UserProfile}
import models.auth.UserAccount
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.bson._
import play.api.Logger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object AccountDetailsRepository extends AccountDetailsRepository {
  val mongoConnector = MongoConnector
}

trait AccountDetailsRepository extends MongoCollections {
  val mongoConnector : MongoConnector

  def updateAccountData(userProfile: UserProfile) : Future[UpdateWriteResult] = {
    val selector = BSONDocument("userName" -> userProfile.userName)
    val updatedData = BSONDocument("$set" -> BSONDocument("firstName" -> userProfile.firstName,"lastName" -> userProfile.lastName,"email" -> userProfile.email))
    mongoConnector.update(USER_ACCOUNTS, selector, updatedData) map {
      res =>
        // $COVERAGE-OFF$
        if(res.hasErrors) Logger.error(s"[AccountDetailsRepository] - [updateAccountData] : Update failed - reason : ${res.errmsg.get}")
        // $COVERAGE-ON$
        res
    }
  }

  def findPassword(passwordSet : UpdatedPassword) : Future[Boolean] = {
    val old = passwordSet.previousPassword
    mongoConnector.read[UserAccount](USER_ACCOUNTS, BSONDocument("_id" -> passwordSet.userId, "password" -> passwordSet.previousPassword)) map {
      case Some(account) => account.password match {
        case `old` => true
        case _ => false
      }
      case _ => false
    }
  }

  def updatePassword(passwordSet : UpdatedPassword) : Future[UpdateWriteResult] = {
    val selector = BSONDocument("_id" -> passwordSet.userId)
    val updatedData = BSONDocument("$set" -> BSONDocument("password" -> passwordSet.newPassword))
    mongoConnector.update(USER_ACCOUNTS, selector, updatedData) map {
      res =>
        // $COVERAGE-OFF$
        if(res.hasErrors) Logger.error(s"[AccountDetailsRepository] - [updatePassword] : Update failed - reason : ${res.errmsg.get}")
        // $COVERAGE-ON$
        res
    }
  }
}
