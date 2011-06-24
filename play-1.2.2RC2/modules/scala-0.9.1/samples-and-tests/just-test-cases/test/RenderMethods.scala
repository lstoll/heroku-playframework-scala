import org.junit._
import play.test._
import play.mvc._
import play.mvc.Http._
import models._

class RenderMethodsTest extends FunctionalTestCase with Matchers{

    @Test
    def testFirstRenderJSON {
        var response = GET("/application/json1")
        response shouldBeOk()
        response contentTypeShouldBe("application/json")
        response charsetShouldBe("utf-8")
        response contentShouldBe("{'name':'guillaume'}")
    }
    
    @Test
    def testSecondRenderJSON {
        var response = GET("/application/json2") 
        assertIsOk(response)
        assertContentType("application/json", response)
        assertCharset("utf-8", response)
        assertContentEquals("{\"email\":\"guillaume@gmail.com\",\"password\":\"12e\",\"fullname\":\"Guillaume\",\"isAdmin\":false,\"id\":0}", response)
    }

    @Test
    def testFirstRender {
        var response = GET("/application/simpleNameBinding")
        assertIsOk(response)
        assertContentType("text/html", response)
        assertCharset("utf-8", response)
        assertContentEquals("<h1>Yop</h1>", response)
    }
    
    @Test
    def testSecondRender {
        var response = GET("/application/complexNameBinding")
        assertIsOk(response)
        assertContentType("text/html", response)
        assertCharset("utf-8", response)
        assertContentEquals("<h1>Yop</h1>", response)
    }
    
    @Test
    def bugWithSomePrivateMethods {
        var response = GET("/test/tst")
        assertIsOk(response)
    }
   
    @Test
    def testActionChainging{ 
      val response = GET("/Application/goJojo")
      assertStatus(302, response)
    }

    @Test
    def testRenderHtml{
      val response = GET("/Application/helloWorld") 
      response contentTypeShouldBe ("text/html")
      response shouldBeOk()
      response contentShouldBe ("<h1>Hello world</h1>")
    }
    @Test 
    def testDirectRendering {
      val response = GET("/Application/hello?name=peter")
      response contentTypeShouldBe ("text/html")
      response shouldBeOk()
      response contentShouldBe ("<h1>Hello peter!</h1>")
    }

    @Test
    def testElvis {
      val response = GET("/Application/elvis")
      response contentTypeShouldBe ("text/html")
      response shouldBeOk()
      response contentShouldBe ("boo")
      
    }

    @Test 
    def testUrlCaller{
      val response = GET("/Application/urlcall")
      response contentTypeShouldBe ("text/html")
      response shouldBeOk()
      response contentShouldBe ("yes")
    }

    @Test
    def testARM {
      val response = GET("/Application/arm")
      response contentTypeShouldBe ("text/html")
      response shouldBeOk()
      response contentShouldBe ("true")
    }
    
    @Test
    def testCatchAnnotation {
      val response = GET("/CatchAnnotation/index")
      response contentTypeShouldBe ("text/html")
      response shouldBeOk()
      response contentShouldBe ("Oops, got / by zero")
    }
    
    @Test
    def testReverseByName {
      val response = GET("/Application/reverseByName")
      response contentTypeShouldBe ("text/plain")
      response shouldBeOk()
      response contentShouldBe ("GET /application/anotherindex?nimp=19 (/application/anotherindex?nimp=19)")
    }

}
