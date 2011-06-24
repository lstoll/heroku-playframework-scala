package controllers

import play.mvc._

object Test extends Controller {

    def tst() = {
        val v = List(1,2).map(xtf)
        v.mkString("")
    }

    private def xtf(x:Int) = {
        x.toString
    }

}