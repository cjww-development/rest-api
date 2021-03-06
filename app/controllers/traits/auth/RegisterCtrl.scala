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

package controllers.traits.auth

import config.{Authorised, BackController, NotAuthorised}
import models.auth.{OrgAccount, UserAccount}
import play.api.mvc.Action
import services.UserRegisterService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait RegisterCtrl extends BackController {

  val userRegisterService : UserRegisterService

  def createUserAccount : Action[String] = Action.async(parse.text) {
    implicit request =>
      authOpenAction {
        case Authorised =>
          decryptRequest[UserAccount] {
            accData =>
              userRegisterService.storeNewUser(accData) map {
                case true => InternalServerError
                case false => Created
              }
          }
        case NotAuthorised => Future.successful(Forbidden)
     }
  }

  def createOrgAccount : Action[String] = Action.async(parse.text) {
    implicit request =>
      authOpenAction {
        case Authorised =>
          decryptRequest[OrgAccount] {
            orgAccData =>
              userRegisterService.storeNewOrgUser(orgAccData) map {
                case true => InternalServerError
                case false => Created
              }
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }

  //TODO : TEST THESE CONTROLLERS
  def checkUserNameUsage : Action[String] = Action.async(parse.text) {
    implicit request =>
      authOpenAction {
        case Authorised =>
          decryptRequest[String] {
            username =>
              userRegisterService.checkUserNameUsage(username) map {
                usage => Ok(usage.toString)
              }
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }

  def checkEmailUsage : Action[String] = Action.async(parse.text) {
    implicit request =>
      authOpenAction {
        case Authorised =>
          decryptRequest[String] {
            email =>
              userRegisterService.checkEmailUsage(email) map {
                usage => Ok(usage.toString)
              }
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }
}
