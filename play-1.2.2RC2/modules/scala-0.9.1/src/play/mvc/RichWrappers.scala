package play.mvc

import scala.xml.NodeSeq
import scala.io.Source

import java.io.InputStream
import java.util.concurrent.Future

import play.mvc.Http._
import play.mvc.Scope._
import play.data.validation.Validation
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesSupport
import play.classloading.enhancers.ControllersEnhancer.ControllerSupport

/**
 * utility class to provider an easier way to render arguments
 */
private[mvc] class RichRenderArgs(val renderArgs: RenderArgs) {
    
    def +=(variable: Tuple2[String, Any]) = {
        renderArgs.put(variable._1, variable._2)
        this
    }
    
    def apply(key: String) = {
        renderArgs.data.containsKey(key) match {
            case true => Some(renderArgs.get(key))
            case false => None
        }
    }
}

/**
 * utility class to provider an easier way to flash arguments
 */
private[mvc] class RichFlash(val flash: Flash) {
    
    def +=(variable: Tuple2[String, String]) = {
        flash.put(variable._1, variable._2)
        this
    }
    
    def apply(key: String) = {
        flash.contains(key) match {
            case true => Some(flash.get(key))
            case false => None
        }
    }
}

/**
 * utility class to provide some extra syntatic sugar while dealing with a session
 */
private[mvc] class RichSession(val session: Session) {
    
    def +=(variable: Tuple2[String, String]) = {
        session.put(variable._1, variable._2)
        this
    }
    
    def apply(key: String) = {
        session.contains(key) match {
            case true => Some(session.get(key))
            case false => None
        }
    }
}

private[mvc] class RichValidation(val validation: Validation) {
    
    def hasErrors = Validation.hasErrors()
    
}

/**
* utility class to provide some extra syntatic sugar while dealing with Response objects
*/
private[mvc] class RichResponse(val response: Response) {

    val ContentTypeRE = """[-a-zA-Z]+/[-a-zA-Z]+""".r

    def <<<(x: String) {
        x match {
            case ContentTypeRE() => response.contentType = x
            case _ => response.print(x)
        }
    }

    def <<<(header: Header) {
        response.setHeader(header.name, header.value())
    }

    def <<<(header: Tuple2[String, String]) {
        response.setHeader(header._1, header._2)
    }

    def <<<(status: Int) {
        response.status = status
    }

    def <<<(xml: scala.xml.NodeSeq) {
        response.print(xml)
    }
}


