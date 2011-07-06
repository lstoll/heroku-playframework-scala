package play.mvc

import results._
import scala.xml.NodeSeq
import scala.io.Source
//import scala.collection.JavaConversions._

import java.io.InputStream
import java.util.concurrent.Future

import play.mvc.Http._
import play.mvc.Scope._
import play.data.validation.Validation
import play.classloading.enhancers.LocalvariablesNamesEnhancer.{LocalVariablesSupport, LocalVariablesNamesTracer}
import play.classloading.enhancers.ControllersEnhancer.ControllerSupport
import play.WithEscape

/**
 *
 * Represents a Scala based Controller
 */
abstract class ScalaController extends ControllerDelegate with LocalVariablesSupport with ControllerSupport {

    /**
     * implicit definition to provider an easier way to render arguments
     */
    implicit def richRenderArgs(x: RenderArgs) = new RichRenderArgs(x)

    /**
     * implicit definition to provider an easier way to flash arguments
     */
    implicit def richFlash(x: Flash) = new RichFlash(x)

    /**
     * implicit definition to provide some extra syntactic sugar while dealing with Response objects
     */
    implicit def richResponse(x: Response) = new RichResponse(x)

    /**
     * implicit definition to to provide some extra syntactic sugar while dealing with a sessions
     */
    implicit def richSession(x: Session) = new RichSession(x)

    /**
     * implicit definition to to provide some extra syntactic sugar while dealing with validation
     */
    implicit def richValidation(x: Validation) = new RichValidation(x)

    // -- Responses

    def Ok                                              = new Ok()
    def Created                                         = new Status(201)
    def Accepted                                        = new Status(202)
    def NoContent                                       = new Status(204)
    def NotModified                                     = new NotModified()
    def NotModified(etag: String)                       = new NotModified(etag)
    def Forbidden                                       = new Forbidden("Forbidden")
    def Forbidden(why: String)                          = new Forbidden(why)
    def NotFound                                        = new NotFound("Not found")
    def NotFound(why: String)                           = new NotFound(why)
    def NotFound(method: String, path: String)          = new NotFound(method, path)
    def Error                                           = new Error("Internal server error")
    def Error(why: String)                              = new Error(why)
    def Error(status: Int, why: String)                 = new Error(status, why)
    def BadRequest                                      = new BadRequest()
    def Unauthorized                                    = new Unauthorized("Secure")
    def Unauthorized(area: String)                      = new Unauthorized(area)
    def Html(html: Any)                                 = new RenderHtml( if(html != null) html.toString else "" )
    def Xml(document: org.w3c.dom.Document)             = new RenderXml(document)
    def Xml(xml: Any)                                   = new RenderXml( if(xml != null) xml.toString else "<empty/>" )
    def Json(json: String)                              = new RenderJson(json)
    def Json(o: Any)                                    = new RenderJson(new com.google.gson.Gson().toJson(o))
    def Text(content: Any)                              = new RenderText(if(content != null) content.toString else "")
    def Redirect(url: String)                           = new Redirect(url)
    def Redirect(url: String, permanent: Boolean)       = new Redirect(url, permanent)
    def Template                                        = new Template()
    def Template(args: (Symbol, Any)*)                  = new Template(args =  ScalaControllerCompatibility.argsToParams(args: _*))
    def Template(name: String, args: (Symbol, Any)*)    = new Template(template = Some(name), args = ScalaControllerCompatibility.argsToParams(args: _*))
    def Action(action: => Any)                          = new ScalaAction(action)
    def Continue                                        = new NoResult()


    @deprecated def Suspend(s: String)                  = new ScalaSuspend(s)
    @deprecated def Suspend(t: Int)                     = new ScalaSuspend(t)
    @deprecated def WaitFor(tasks: Future[_])           = new ScalaWaitFor(tasks)


    /**
     * @returns a play request object
     */
    implicit def request = Request.current()

    /**
     * @returns a play response object
     */
    implicit def response = Response.current()

    /**
     * @returns a session object
     */
    implicit def session = Session.current()

    /**
     * @returns a flash object
     */
    implicit def flash = Flash.current()

    /**
     * @returns parameters
     */
    implicit def params = Params.current()

    /**
     * @returns render argument object
     */
    implicit def renderArgs = RenderArgs.current()

    /**
     * @returns Validation
     */
    implicit def validation = Validation.current()

    implicit def validationErrors:Map[String,play.data.validation.Error] = {
        import scala.collection.JavaConverters._
        Map.empty[String,play.data.validation.Error] ++ Validation.errors.asScala.map( e => (e.getKey, e) )
    }

    def reverse(action: => Any): play.mvc.Router.ActionDefinition = {
        val actionDefinition = reverse()
        action
        actionDefinition
    }

    def templateExists(name: String) = ControllerDelegate.templateExists(name)

}

object ScalaControllerCompatibility {

    def argsToParams(args: (Symbol,Any)*): Map[String,Any] = Map(args:_*).collect {
        case (key, value) => (key.name, value)
    }

}


case class Promise[A]()
abstract class Awaited[E,T]{
  
  def flatMap[U](f: T => Awaited[_,U]) : Awaited[_,U] ={
   this match {
      case Done(t) => f(t)
      case a@Await(p,g) =>  Await(p, (e:E) =>(g(e).flatMap(f)))
    }
  }

}
case class Await[E,T](p:Promise[E],f: E => Awaited[_,T]) extends Awaited[E,T]
case class Done[E,T](t:T) extends Awaited[E,T]
