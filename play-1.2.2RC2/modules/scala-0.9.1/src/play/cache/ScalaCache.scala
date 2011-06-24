package play.cache

import play.libs.Time._
import scala.actors.Actor._
import scala.actors._

/**
 * Extends the Cache API with two scala specific methods, this is made public via type alias
 */
private[cache] object ScalaCache extends CacheDelegate {

    import akka.actor._
    import akka.actor.Actor._
    import akka.config._
    import akka.config.Supervision._


    class CacheActor extends Actor {
        def receive = {
            case CacheMessage(key, expiration, window, waitForEvaluation,f,isDesireable) => {
                val flagWhileCaching = "___" + key
                getFromCache1(flagWhileCaching).getOrElse {
                    set(flagWhileCaching, Caching(), waitForEvaluation);
                    get[Any](key,expiration,window)(f())(isDesireable)
                    play.Logger.info("asynchronously recached: "+ key)
                    _impl.delete(flagWhileCaching)
                }
            }
            case unknown =>  play.Logger.warn( "received unknown message: "+unknown)
        }

        override def postRestart (reason: Throwable): Unit =
            play.Logger.warn(reason, "cache actor restarted after crash ...");
    }

    private lazy val cacheActor = {
        val actor = actorOf[CacheActor]
        Supervisor(
            SupervisorConfig(
                OneForOneStrategy(List(classOf[Exception]), 10, 10000),
                Supervise(actor,Permanent) :: Nil) )
        actor
    }

    case class CacheMessage[A](key: String,
                               expiration: String,
                               window: String,
                               waitForEvaluation: String = "10s",
                               f:()=>A,
                               isDesireable:A => Boolean)
    case class Caching()

    private def prefixed(key: String) = "__" + key

    private def getFromCache[T](key: String) = getFromCache1(key).map(_.asInstanceOf[T])

    private def getFromCache1(key: String): Option[_] = {
        import scala.util.control.Exception._
        catching(classOf[java.io.InvalidClassException])
            .either(Option( _impl.get(key)))
            .left.map( e => { delete(key); play.Logger.warn(e.getMessage()); e } )
            .right.toOption.flatMap(identity)
    }

    /**
    * Retrieves value from Cache based on the type parameter
    * @param key the name of the cache key
    * @param return either the value or None
    */
    def get[T](key: String)(implicit m: ClassManifest[T]): Option[T] = {
        if (key == null) None
        val v = _impl.get(key)
        if (v == null) {
            None
        } else if (m.erasure.isAssignableFrom(v.asInstanceOf[AnyRef].getClass)) {
            Some(v.asInstanceOf[T])
        } else {
            play.Logger.warn("Found a value in cache for key '%s' of type %s where %s was expected",
                             key,
                             v.asInstanceOf[AnyRef].getClass.getName,
                             m.erasure.getName)
            None
        }
    }


    /**
    * Retrieves value from Cache based on the type parameter
    * @param key the name of the cache key
    * @param return either the value or None
    * @param expiration expiration period
    */
    def get[T](key: String, expiration: String)(getter: => T): T = {
       getFromCache[T](key) match {
            case Some(x) => x
            case None => {
                val r = getter
                set(key, r, expiration)
                r
            }
        }
    }

    // Refactor this, you need to have a specific type to avoid implicits conflicts
    object Instances {
        implicit def isDesirable[A](o: Option[A]): Boolean = o.isDefined
        implicit def isDesirableSeq[A, B[X] <: Seq[X]](seq: B[A]): Boolean = seq.nonEmpty
    }

    /**
    * Retrieves a key in async fashion
    * @param key cache key
    * @param expiration experiation period
    * @param window
    * @param waitForEvaluation
    * @return parameterized type
    */
    def getAsync[A](key: String,
                    expiration: String,
                    window: String,
                    waitForEvaluation: String = "10s")(getter: => A)(implicit isDesirable: A => Boolean): A = {

        getFromCache[A](key).getOrElse(
            getFromCache[A](prefixed(key)).map(v => {
                cacheActor ! CacheMessage(key, expiration, window, waitForEvaluation, () => getter,isDesirable); v
            }
        ).getOrElse(get(key,expiration,window)(getter)(isDesirable)))

    }

    /**
    * Retrieves key if it's not in cache it populates cache using the the passed in getter
    * @param key
    * @param experiation
    * @param window
    * @param getter
    * @return parameterized type
    */
    def get[A](key: String,
               expiration: String,
               window: String)(getter: => A)(implicit isDesirable: A => Boolean): A = {

        val cacheIt = (v: A) => {
            set(prefixed(key), v, parseDuration(expiration) + parseDuration(window) + "s")
            set(key, v, expiration)
            v
        }

        getFromCache[A](key).getOrElse({
            val result = getter;
            if (isDesirable(result)) {
                cacheIt(result)
            } else {
                getFromCache[A](prefixed(key)).map({v=> set(key, v, "2min");v}).getOrElse(result)
            }
        })

    }

}
