package com.github.mideo

import org.scalatra.test.scalatest._

class UsersServletTests extends ScalatraFunSuite {

  addServlet(classOf[UsersServlet], "/*")
  test("UsersServlet should return user") {

    get("/user") {
      status should equal (200)
      response.getContentType() should equal("application/json;charset=utf-8")
      body should equal("""{"name":"scalatraUser"}""")
    }

    get("/users") {
      status should equal (200)
      response.getContentType() should equal("application/json;charset=utf-8")
      body should equal("""[{"name":"scalatraUser1"},{"name":"scalatraUser2"}]""")
    }
  }

}
