package play.jobs

import scala.actors._

import play._
import play.exceptions._

object Asyncs {

    //TODO:maybe it needs to be re-implemented to avoid using casting, but the interface is OK
    def awaitForAll[A](timeout: Long, futures: Seq[Future[A]]): Seq[Option[A]] = Futures.awaitAll(timeout, futures: _*).map(_.asInstanceOf[Option[A]])

}

/**
 * provides actor support for play, this can be used as a replacement for jobs
 */
trait PlayActor extends Actor {

    /**
    *  @param msg the message being sent
    *  @return  Future task
    */
    def !!![T](msg: Function0[T]): Future[Either[Throwable, T]] = (PlayActor !! msg).asInstanceOf[Future[Either[Throwable, T]]]

    /**
     * actor reactor loop
     */
     def act {
         loop {
             react {
                case f: Function0[_] => play.Invoker.invokeInThread(new play.Invoker.DirectInvocation() {
                  override def execute {
                    try {
                      sender ! Right(f())
                    } catch {
                      case e => val element = PlayException.getInterestingStrackTraceElement(e)
                      if (element != null) {
                        Logger.error(
                          new JavaExecutionException(
                            Play.classes.getApplicationClass(element.getClassName()), element.getLineNumber(), e
                          )

                          ,
                          "Caught in PlayActor"
                        )
                      } else {
                        Logger.error(e, "Caught in PlayActor")
                      }
                      sender ! Left(e)
                    }
                  }
                })
                case _ => sender ! Left(new Exception("Unsupported message type"))
            }
        }
    }

    start
    
}

object PlayActor extends PlayActor
