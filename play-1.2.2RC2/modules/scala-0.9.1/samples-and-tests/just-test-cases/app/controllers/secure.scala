package controllers

import play.mvc._

trait Secure extends Controller {

    @Before
    def saySecure {
        println("passed in secure")
    }

}