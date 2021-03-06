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

import models.account.{AccountSettings, UpdatedPassword, UserProfile}
import models.auth.UserAccount
import repositories.AccountDetailsRepository
import security.JsonSecurity

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

sealed trait UpdatedPasswordResponse
case object InvalidOldPassword extends UpdatedPasswordResponse
case class PasswordUpdate(success : Boolean) extends UpdatedPasswordResponse

sealed trait UpdatedSettingsResponse
case object UpdatedSettingsSuccess extends UpdatedSettingsResponse
case object UpdatedSettingsFailed extends UpdatedSettingsResponse

object AccountService extends AccountService {
  val accountDetailsRepo = AccountDetailsRepository
}

trait AccountService {

  val accountDetailsRepo : AccountDetailsRepository

  def getAccount(userID : String) : Future[Option[String]] = {
    accountDetailsRepo.getAccount(userID) map {
      acc => acc.isDefined match {
        case false => None
        case true => JsonSecurity.encryptModel[UserAccount](acc.get)
      }
    }
  }

  def updateProfileInformation(userProfile: UserProfile) : Future[Boolean] = {
    accountDetailsRepo.updateAccountData(userProfile) map {
      _.hasErrors
    }
  }

  def updatePassword(passwordSet : UpdatedPassword) : Future[UpdatedPasswordResponse] = {
    accountDetailsRepo.findPassword(passwordSet) flatMap {
      case false => Future.successful(InvalidOldPassword)
      case true => accountDetailsRepo.updatePassword(passwordSet) map {
        updated => PasswordUpdate(updated.hasErrors)
      }
    }
  }

  def updateSettings(accountSettings : AccountSettings) : Future[UpdatedSettingsResponse] = {
    accountDetailsRepo.updateSettings(accountSettings) map {
      _.hasErrors match {
        case true => UpdatedSettingsFailed
        case false => UpdatedSettingsSuccess
      }
    }
  }
}
