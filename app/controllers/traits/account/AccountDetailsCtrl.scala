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

package controllers.traits.account

import config.{Authorised, BackController, NotAuthorised}
import models.account.{AccountSettings, UpdatedPassword, UserProfile}
import play.api.mvc.Action
import services._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait AccountDetailsCtrl extends BackController {

  val accountService : AccountService

  def getAccountData : Action[String] = Action.async(parse.text) {
    implicit request =>
      authOpenAction {
        case Authorised =>
          decryptRequest[String] {
            userID =>
              accountService.getAccount(userID) map {
                case Some(acc) => Ok(acc)
                case None => InternalServerError
              }
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }

  def updateProfileInformation() : Action[String] = Action.async(parse.text) {
    implicit request =>
      authOpenAction {
        case Authorised =>
          decryptRequest[UserProfile] {
            profile =>
              accountService.updateProfileInformation(profile) map {
                case false => Ok
                case true => InternalServerError
              }
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }

  def updateUserPassword() : Action[String] = Action.async(parse.text) {
    implicit request =>
      authOpenAction {
        case Authorised =>
          decryptRequest[UpdatedPassword] {
            passwordSet =>
              accountService.updatePassword(passwordSet) map {
                case InvalidOldPassword => Conflict
                case PasswordUpdate(success) => success match {
                  case true => InternalServerError
                  case false => Ok
                }
              }
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }

  def updateUserSettings() : Action[String] = Action.async(parse.text) {
    implicit request =>
      authOpenAction {
        case Authorised =>
          decryptRequest[AccountSettings] {
            settings =>
              accountService.updateSettings(settings) map {
                case UpdatedSettingsSuccess => Ok
                case UpdatedSettingsFailed => InternalServerError
              }
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }
}
