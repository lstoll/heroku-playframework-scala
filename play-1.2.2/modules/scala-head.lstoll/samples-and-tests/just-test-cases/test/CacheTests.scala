import play.test._

import org.junit._
import org.scalatest.junit._
import org.scalatest._
import org.scalatest.matchers._

import play.cache._

class CacheTests extends UnitTestCase with ShouldMatchersForJUnit {

    @Before def setUp = Cache.clear()

    @Test def useTheCacheAPI {

        Cache.get[String]("yop") should be (None)
        Cache.set("yop", "Coucou")
        Cache.get[String]("yop") should be (Some("Coucou"))

        // Test wrong type (a warn log should be produced as well)
        Cache.get[Int]("yop") should be (None)

        Cache.get[Seq[Int]]("coco") should be (None)
        Cache.get[Seq[Int]]("coco", "1s") {
            for(i <- 0 to 5) yield i
        } should be (Seq(0,1,2,3,4,5))
        Cache.get[String]("coco") should be (None)
        Cache.get[String](null) should be (None)
        Cache.get[Seq[Int]]("coco") should be (Some(Seq(0,1,2,3,4,5)))

        // Wait a moment
        Thread.sleep(1100)
        Cache.get[Seq[Int]]("coco") should be (None)

    }
 @Test def useTheCacheAPIAsync {

   val workingTime=150 //time the async "job" takes to excute
   val nbRequests=100 //number of total requests
   val cacheTime=1000;val cacheTimePattern=cacheTime/1000+"s";
   val tolerenceWindow=cacheTime/100+"s";
   val nbCycles=7 //number of times cache will expire
   val frequency=(cacheTime*nbCycles)/nbRequests //time between requests
   val rqsForCycle=nbRequests/nbRequests //number of requests before cache expires
   val unreachableCycles=(workingTime/frequency)/rqsForCycle //last caching cycles that won't be reached because of working time
   case class TestCase(sent: Int,sentOn:Long,got:Int,gotOn:Long){def elapsed=gotOn-sentOn}
   def getInt(i:Int):Int=
     Cache.getAsync("coco",cacheTimePattern,tolerenceWindow){
          Thread.sleep(workingTime); i }(_=>true)
   def current()={System.currentTimeMillis()}
   val result=(1 to nbRequests).map(i=> {Thread.sleep(frequency);TestCase(i,current(),getInt(i),current())})

   result.filter(_.elapsed>=workingTime).length should be (1) //only first evaluation is blocking
   (result.map(_.got).distinct.length>unreachableCycles) should be (true) //yet the cache have been refreshed the number of reachable cycles
   /*println(result.zip(result.tail).foldLeft(Seq.empty[String])((l,p)=>(l++Seq.fill(p._1.elapsed.intValue+1)("'"))++Seq.fill((p._2.sent-p._1.got).intValue)("_")).flatten.mkString)*/

 }
  @Test def aCrashScenario {

    Cache.getAsync("akey","1s","10s")("initial value")(_=>true)
    Thread.sleep(1000)
    Range(1,100).map(_=>Cache.getAsync[String]("akey","1s","10s","1s"){throw new Exception()}(_=>true))
    Thread.sleep(1000)
    Cache.getAsync("akey","1s","10s")("end value")(_=>true)
    Thread.sleep(100)
    Cache.getAsync("akey","1s","10s")("end value")(_=>true) should be ("end value")
  }

}

