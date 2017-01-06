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
import models.account.FeedItem
import play.api.Logger
import play.api.libs.json.JsObject
import play.api.mvc.{Action, AnyContent}
import security.JsonSecurity
import services.UserFeedService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait UserFeedCtrl extends BackController {
  val userFeedService : UserFeedService

  def createEvent() : Action[String] = Action.async(parse.text) {
    implicit request =>
      authOpenAction {
        case Authorised =>
          decryptRequest[FeedItem] {
            fi =>
              userFeedService.createFeedItem(fi) map {
                case true => InternalServerError
                case false => Ok
              }
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }

  def retrieveFeed() : Action[String] = Action.async(parse.text) {
    implicit request =>
      authOpenAction {
        case Authorised =>
          decryptRequest[String] {
            dec =>
              userFeedService.getFeedList(dec) map {
                resp => resp.isDefined match {
                  case true => Ok(JsonSecurity.encryptModel[JsObject](resp.get).get)
                  case false => NotFound
                }
              }
          }
        case NotAuthorised => Future.successful(Forbidden)
      }
  }
}
