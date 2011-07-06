package play.mvc.results

import play.mvc.ControllerDelegate
import play.mvc.Http

case class Template(template: Option[String] = None, args: Map[String,Any] = Map()) extends Result {

    val delegate = {
        import scala.collection.JavaConversions._
        ControllerDelegate.renderTemplateForScala(template.orNull, args.asInstanceOf[Map[String,AnyRef]])
    }

    def apply(request: Http.Request , response:Http.Response) {
        delegate.apply(request, response) 
    }

}
