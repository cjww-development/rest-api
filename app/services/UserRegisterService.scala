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

import models.auth.{OrgAccount, UserAccount}
import play.api.Logger
import repositories.UserRegisterRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object UserRegisterService extends UserRegisterService {
  val userRegisterRepo = UserRegisterRepository
}

trait UserRegisterService {

  val userRegisterRepo : UserRegisterRepository

  def storeNewUser(userAccount : UserAccount) : Future[Boolean] = {
    userRegisterRepo.createNewUser(userAccount) map {
      wr =>
        if(wr.hasErrors) {
          // $COVERAGE-OFF$
          Logger.error(s"[UserRegisterRepo] - [createNewUser] : There was a problem creating a new user - ${wr.errmsg}")
          // $COVERAGE-ON$
        }
        wr.hasErrors
    }
  }

  def storeNewOrgUser(orgAccount: OrgAccount) : Future[Boolean] = {
    userRegisterRepo.createOrgUser(orgAccount) map {
      wr =>
        if(wr.hasErrors) {
          // $COVERAGE-OFF$
          Logger.error(s"[UserRegisterRepo] - [createOrgUser] : There was a problem creating a new org user = ${wr.errmsg}")
          // $COVERAGE-ON$
        }
        wr.hasErrors
    }
  }

  def checkUserNameUsage(username : String) : Future[Boolean] = {
    userRegisterRepo.isUserNameInUse(username) map {
      case Some(account) => true
      case None => false
    }
  }

  def checkEmailUsage(email : String) : Future[Boolean] = {
    userRegisterRepo.isEmailInUse(email) map {
      case Some(account) => true
      case None => false
    }
  }
}
