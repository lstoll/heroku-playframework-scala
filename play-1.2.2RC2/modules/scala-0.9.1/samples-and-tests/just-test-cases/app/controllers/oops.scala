package xcontrollers

import controllers._
import play._
import play.mvc._

import java.util._
import models._

import play.data.validation.Annotations._

trait Defaults extends Controller {
    
 @Before
 def setDefaults {
   renderArgs += "appTitle" -> configuration("app.title")
   renderArgs += "appBaseline" -> configuration("app.baseline")
   renderArgs += "email" -> session.get("username")
  }
  
}


/**
 * Created by IntelliJ IDEA.
 * User: Arnaud
 * Date: 22 mars 2010
 * Time: 18:53:36
 */
object Urls extends Defaults {
 def index = Template

 def form(id: Long) = {
   //if (!Secure.Security.isConnected()) Secure.login
   val code = "4"
   //checkOwner(page)
   if (code != null) {
     var map = new java.util.HashMap[String, Object]
     map.put("url", code)
     renderArgs += "urlsite" -> Router.reverse("Pages.show", map).url
   }
   Template('code -> code)
 }

 def list = {
   //val pages: Collection[Page] = user.pages
   val pages = "Page.all.fetch"
   Template('pages -> pages)
 }

 def show(id: String) = {
   val code = "Code findByCode id"
   Template('code -> code)
 }

 /*
  Save an url
 */
 def save(id: Long, @Required url: String) = {
   validation.hasErrors match {
     case true => if (request isAjax) Error("Invalid Value") else Template("@Application.index", 'url -> url)
     case false =>
       var code = null
       if (id != 0) {
         // edit code
       } else {
         // new code
       }
       if (request.isAjax()) Text(code)
       show(code)
   }
 }

 def delete(id: Long) {
   
 }


 private[xcontrollers] def getUser: User= {
   return renderArgs.get("user", classOf[User])
 }

 private[xcontrollers] def checkOwner(page: Any): Unit = {
 }
 
}
