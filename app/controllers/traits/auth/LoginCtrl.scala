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
import models.auth.Login
import play.api.mvc.Action
import services.LoginService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait LoginCtrl extends BackController {

  val loginService : LoginService

  def userLogin : Action[String] = Action.async(parse.text) {
    implicit request =>
      authOpenAction {
        case Authorised =>
          decryptRequest[Login] {
            loginData =>
              loginService.getSingleUser(loginData) map {
                case unauthorised @ "Unauthorised" => Unauthorized(unauthorised)
                case payload => Ok(payload)
              }
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }

}
