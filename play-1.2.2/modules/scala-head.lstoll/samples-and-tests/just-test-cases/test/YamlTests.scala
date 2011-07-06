import play.test._

import org.junit._
import org.scalatest.junit._
import org.scalatest._
import org.scalatest.matchers._

package models.yip {

    case class User(id: Int, name: String, email: Option[String], age: Option[Int])
    
}

class YamlTests extends UnitFlatSpec with ShouldMatchers {

    import models.yip._
    
    it should "extract case classes from Yaml" in {
        
        val user = Yaml[User]("yaml1.yml")
        
        user.id should be (8)
        user.name should be ("Guillaume")
        user.email should be (null) // I know it's very bad
        user.age should be (null)
         
    }
    
    it should "extract case classes from Yaml with Option[T]" in {
        
        val user = Yaml[User]("yaml2.yml")
        
        user.id should be (8)
        user.name should be ("Guillaume")
        user.email should be (Some("gbo@gmail.com")) 
        user.age should be (None)
         
    }
    
    it should "load a list from Yaml" in {
        
        val users = Yaml[List[User]]("yaml3.yml")
        
        users.size should be (3)
        
        users(0).id should be (8)
        users(0).name should be ("Guillaume")
        users(0).email should be (Some("gbo@gmail.com")) 
        users(0).age should be (None)

        users(1).id should be (9)
        users(1).name should be ("Guillaume")
        users(1).email should be (None) 
        users(1).age should be (None)
        
        users(2).id should be (10)
        users(2).name should be ("Guillaume")     
        users(2).email should be (Some("gbo@gmail.com")) 
        users(2).age should be (Some(88))   
        
    }
    
    it should "load a map from Yaml" in {
        
        import scala.collection.JavaConversions._ 
        import java.util.{Map => JMap}   
        
        val m = Yaml[Map[String,JMap[String,Any]]]("yaml4.yml") // only first level collections are Scala List & Map for now
        
        m("City")("name") should be ("Paris")
        m("City")("code") should be (75)
        m("Country")("name") should be ("France")
        
    }
    
}