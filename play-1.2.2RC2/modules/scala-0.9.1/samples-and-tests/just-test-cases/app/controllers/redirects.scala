package controllers

import play.mvc._

object Youpi extends Controller with Services {
    
    def index = "Youpi"
    
    def index2 = coucou
    
    def index3 = Action(coucou)
    
    def coucou = "COUCOU"
    
    def index4 = Action(kiki)
    
    def index5 = kiki
    
}

trait Services {
    
    def kiki = "KIKI"
    
}