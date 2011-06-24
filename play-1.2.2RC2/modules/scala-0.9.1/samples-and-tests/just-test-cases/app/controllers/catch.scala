package controllers

import play.mvc._

object CatchAnnotation extends Controller {
    
    def index = "Test -> " + (9/0)
    
    @Catch def oops(e: Exception) = "Oops, got " + e.getMessage
    
}