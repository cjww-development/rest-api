[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Build Status](https://travis-ci.org/cjww-development/rest-api.svg?branch=master)](https://travis-ci.org/cjww-development/rest-api)

cjww-rest-api-mongo
===================

Scala Play! rest api for CJWW microservices

There are several demonstration files available in this template.

How to run
==========

```````````````
sbt run
```````````````

This will start the application on port **9973**

Routes
======

| Path                                                                               | Supported Methods | Description  |
| ---------------------------------------------------------------------------------- | ------------------| ------------ |
|```/cjww-rest-api/create-new-user```                                                |       POST        | creates a new user account |
|```/cjww-rest-api/create-new-org-user```                                            |       POST        | creates a new org user account |

###GET /cjww-rest-api/create-new-user

    Responds with:

| Status        |
|:--------------|
| 200           |
| 403           |
| 400           |
| 404           |
| 500           |

###GET /cjww-rest-api/create-new-org-user

    Responds with:

| Status        |
|:--------------|
| 200           |
| 403           |
| 400           |
| 404           |
| 500           |

