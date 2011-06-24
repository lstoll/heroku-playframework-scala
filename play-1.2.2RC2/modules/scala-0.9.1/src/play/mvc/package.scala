import play.mvc.Scope._
import play.mvc.Http._
import play.mvc.results._

/**
* stuffing a few scala specific helpers into play.mvc namespace
*
*/
package play {

    import mvc.{ScalaController, ScalaMailer, RichSession, RichResponse, RichRenderArgs}

    package object mvc {

        // -- HELPERS

        def header(name: String, value: String) = new Header(name, value)
        def header(h: Tuple2[String, String]) = new Header(h._1, h._2)

        // -- STATUS CODE

        val OK              = 200
        val CREATED         = 201
        val ACCEPTED        = 202
        val NO_CONTENT      = 204
        val FORBIDDEN       = 403
        val NOT_FOUND       = 404
        val ERROR           = 500

        // -- TYPES REDEFINITION

        type Controller = ScalaController
        type Mailer = ScalaMailer

    }

}
