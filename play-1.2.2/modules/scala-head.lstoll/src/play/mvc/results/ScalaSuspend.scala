package play.mvc.results

import play.mvc.Http
import play.mvc.Http

class ScalaSuspend(num: Int) extends Result {

    def this(s: String) = this(1000 * play.libs.Time.parseDuration(s))

    val delegate = new play.Invoker.Suspend(num)

    Http.Request.current().isNew = false

    def apply(request: Http.Request , response:Http.Response) {
        throw delegate
    }

}
