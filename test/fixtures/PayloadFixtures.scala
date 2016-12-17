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

package fixtures

import models.account.UserProfile
import security.JsonSecurity

trait PayloadFixtures {
  lazy val invalidPayload = "7eqO/a356L5bEcP0NCK5QoblZJmy4RZgzi9FbaWvkbs="


  lazy val validUserAccountPayload =
    "el76E6N/MRiD8mQknEb4DhpzyCxOOMOGmrrmDgYNAkcVlJhUc7eXbqqDiAnmfmzG+wLE/Gq17b9cnrpiEeR3rEP+ktkCdDGLMcUzcEUKc+pbtzkFc0EAB/2ahFZM0VWjRusnE3mmkVk/e25y+EEs3cgJO0tqRcx26Xt7kSWz+9nWfmxrUYDkAcjasNL7rehiRryCE74/XNowkfP17F66ZQ=="
  lazy val validOrgAccountPayload =
    "2m1oEtWLRtIinCfWtQ8ateRtRxRj/whU1lmL3ciZ8j1pvB3JlLRiWRKnFLFma25zmZ/FWVmKRZw8hv5WRHeqx45Sk6ipLEGmR1BM98FahOpusKrBBv2bSiFlew4bfTDpKdCEJS+8ymBoVjJxaC5/lJZcPevTWqxeEl13TX42ZY342sXpC22fUZuppirlFB2m++jHsSam4YGc9qxeayN5AA=="


  lazy val validLoginPayload =
    "KWeL116d7p7f1H/rMucm20P2dgCjsMnKlF9BMXWXgD06hMTOXVWlH23qhShNllRmq4lUaalTAcf05En1NJBhTA=="

  lazy val invalidLoginPayload =
    "KWeL116d7p7f1H/rMucm27uIgqZuc4xA+uHjYxCZHY/fwoAlOT8d7m/7ozAwkutilfiKbxb2dARYDVeN7bEGtQ=="

  lazy val encUserName = JsonSecurity.encryptModel[String]("testUserName").get

  lazy val validUserProfilePayload = JsonSecurity.encryptModel[UserProfile](UserProfile("testFirst","testLast","testUser","testEmail", None, None)).get
  lazy val validUserProfileModel = UserProfile("testFirst","testLast","testUser","testEmail", None, None)
}
