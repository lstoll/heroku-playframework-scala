package play.test

import java.io._
import play.mvc.Http._
import play.test.{FunctionalTest=>F}
import java.util.{Map=>JMap}
import org.scalatest.{Spec,WordSpec,FlatSpec,FunSuite, FeatureSpec}

/**
 * wraps around FunctionalTest's browser specific methods
 */
trait Browser {
    
    //TODO: remove these wrappers after play-core refactoring
    val APPLICATION_X_WWW_FORM_URLENCODED = F.APPLICATION_X_WWW_FORM_URLENCODED
    val MULTIPART_FORM_DATA = F.MULTIPART_FORM_DATA
    def GET(url: String) = F.GET(url)
    def DELETE(url: String) = F.DELETE(url)
    def PUT(url: String, contenttype: String, body: String) = F.PUT(url, contenttype, body)
    def PUT(request: Request, url: String, contenttype: String, body: String) = F.PUT(request, url, contenttype, body)
    def DELETE(request: Request, url: String) = F.DELETE(request, url)
    def makeRequest(request: Request, response:Response)=F.makeRequest(request,response)
    def makeRequest(request: Request) = F.makeRequest(request)
    def newResponse() = F.newResponse()
    def newRequest() = F.newRequest()
    def POST(request: Request, url: String, parameters: JMap[String,String], files: JMap[String, File]) = F.POST(request, url,parameters,files)
    def POST(url: String, parameters: JMap[String,String], files: JMap[String, File]) = F.POST(url,parameters,files)
    def POST(url: String, contenttype: String, body: InputStream) = F.POST(url,contenttype,body)
    def POST(request: Request, url: String, contenttype: String, body: String)= F.POST(request,url,contenttype,body)
    def POST(url: String, contenttype: String, body: String) = F.POST(url,contenttype,body)
    def POST(request: Request, url: String)= F.POST(request,url)
    def POST(url: String) = F.POST(url)
    def GET(request: Request, url: String) = F.GET(request,url)
    def getContent(response: Response) = F.getContent(response)

}

object Browser extends Browser

/**
 * wraps around FunctionalTest's junit asserts 
 */
trait Assertions {
    
    //TODO:these wrappers will disappear after play-core refactoring
    def assertHeaderEquals(headerName: String, value: String, response: Response) = F.assertHeaderEquals(headerName,value,response)
    def assertContentType(contentType: String, response: Response) = F.assertContentType(contentType,response)
    def assertCharset(charset: String, response: Response) = F.assertCharset(charset,response)
    def assertContentMatch(pattern: String, response: Response) = F.assertContentMatch(pattern, response)
    def assertContentEquals(content: String, response: Response) = F.assertContentEquals(content, response)
    def assertStatus(status: Int, response: Response) = F.assertStatus(status,response)
    def assertIsNotFound(response: Response) = F.assertIsNotFound(response)
    def assertIsOk(response: Response) = F.assertIsOk(response)
  
}

object Assertions extends Assertions

trait Matchers  {
    
    implicit def addTestMethods(response: Response) = new RichTestResponse(response)
    
    private[test] class  RichTestResponse(response: Response) {
        def shouldBeOk() = Assertions.assertIsOk(response)
        def shouldNotBeFound() = Assertions.assertIsNotFound(response)
        def statusShouldBe(status: Int) = Assertions.assertStatus(status,response)
        def contentShouldBe(content: String) = Assertions.assertContentEquals(content,response)
        def contentShouldMatch(pattern: String) = Assertions.assertContentMatch(pattern, response)
        def charsetShouldBe(charset: String) =  Assertions.assertCharset(charset, response)
        def contentTypeShouldBe (contentType: String) = Assertions.assertContentType(contentType, response)
        def headerShouldBe (header: Tuple2[String,String]) =Assertions.assertHeaderEquals(header._1,header._2, response)
    } 
    
}

object Matchers extends Matchers

trait FunctionalTestCase extends FunctionalTest with Browser with Assertions
trait FunctionalSpec extends FunctionalTest with Spec with Browser with Matchers 
trait FunctionalWordSpec extends FunctionalTest with WordSpec with Browser with Matchers
trait FunctionalFlatSpec extends FunctionalTest with FlatSpec with Browser with Matchers
trait FunctionalFeatureSpec extends FunctionalTest with FeatureSpec with Browser with Matchers

trait UnitTestCase extends UnitTest
trait UnitSpec extends UnitTest with Spec
trait UnitWordSpec extends UnitTest with WordSpec
trait UnitFlatSpec extends UnitTest with FlatSpec
trait UnitFeatureSpec extends UnitTest with FeatureSpec
trait UnitFunSuite extends UnitTest with FunSuite 
