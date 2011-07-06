package play.mvc.results

import play.mvc.Http
import play.mvc.Http
import java.util.concurrent.Future

class ScalaWaitFor(tasks: Future[_]) extends Result {

    val delegate = new play.Invoker.Suspend(tasks)

    Http.Request.current().isNew = false
  
    def apply(request: Http.Request , response:Http.Response) {
        throw delegate
    }

}
