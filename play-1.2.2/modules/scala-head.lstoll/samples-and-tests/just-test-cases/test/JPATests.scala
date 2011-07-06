import play.test._

import org.junit._
import org.scalatest.junit._
import org.scalatest._
import org.scalatest.matchers._
import play.db.jpa.asScala.enrichJavaModel
import play.db.jpa.asScala
import models._

class JPATests extends UnitFlatSpec with ShouldMatchers {
    
    Fixtures.deleteAll()
    
    "Hibernate" should "save the new User" in {    
        
        new User("guillaume@toto.com", "secret", "Guillaume").save()
        
    }
    
    it should "retrieve the saved User" in {    
           
        User.connect("guillaume@toto.com", "secret") should not be (None)
        
    }
    
    it should "throw an error if we create another User with the same email" in {    
        
        evaluating { new User("guillaume@toto.com", "secret", "Guillaume2").save() } should produce [javax.persistence.PersistenceException] 
        
    }
    
}
