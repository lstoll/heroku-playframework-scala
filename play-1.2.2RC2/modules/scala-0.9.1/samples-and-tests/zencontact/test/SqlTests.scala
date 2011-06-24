import play.test._
import play.db.anorm._

import models._

import org.scalatest.{FlatSpec,BeforeAndAfterEach}
import org.scalatest.matchers.ShouldMatchers
import scala.collection.mutable.Stack

class SqlTests extends UnitFlatSpec with ShouldMatchers with BeforeAndAfterEach {
    
    override def beforeEach() {
        Fixtures.deleteDatabase()
        Yaml[List[Contact]]("test-data.yml").foreach {
            Contact.create(_)
        }
    }
 
    it should "provide a Plain Old SQL API" in {
        
       SQL("select * from Contact").first() should not be (None)
       
       val thrown = evaluating { SQL("select * from Contact").single() } should produce [RuntimeException]
       thrown.getMessage should equal ("end of input expected")
       
       SQL("select * from Contact").list().length should be (4)
       
    }
    
    it should "provide a Map API" in {        
        
        val emails = SQL("select * from Contact")
                        .list()
                        .map { _.asMap("CONTACT.EMAIL") }
                        .collect { case Some(o) => o }
        
        emails should be (List("hello@warry.fr", "guillaume.bort@gmail.com", "contact@japan.com"))
        
    }
    
    it should "work with pattern matching" in {       
        
        case class ContactInformations(id:Long, name:String, email:String)
        
        val contacts = SQL("select * from Contact").list() collect {
            case Row(id:Long, name:String, _, _, Some(email:String)) => ContactInformations(id, name, email)
        }
        
        contacts.length should be (3)
        contacts should be (List(
            ContactInformations(18, "Dantec", "hello@warry.fr"),
            ContactInformations(57, "Bort", "guillaume.bort@gmail.com"),
            ContactInformations(106, "鈴木", "contact@japan.com")
        ))
        
    }
    
    it should "provide a parser combinator API" in {
        
        import SqlParser._
        
        val firstname ~ lastname = SQL("select * from Contact").parse( 'FIRSTNAME.of[String] ~< 'NAME.of[String] )        
        firstname should be ("Maxime")
        lastname should be ("Dantec")
        
        SQL("select count(*) from contact where email is not null").as( scalar[Long] ) should be (3)

    }
    
    it should "work with Magic parsers" in {
        
        import play.db.anorm.defaults._
        
        val miniContact = Magic[MiniContact]().using('CONTACT)
        
        val contacts = SQL("select * from Contact").as( miniContact* )
        
        contacts should be (List(
            MiniContact(18,"Dantec",Some("hello@warry.fr")), 
            MiniContact(57,"Bort",Some("guillaume.bort@gmail.com")), 
            MiniContact(89,"Drobi",None), 
            MiniContact(106,"鈴木",Some("contact@japan.com"))
        ))
        
    }
    
}

case class MiniContact(id:Long, name:String, email:Option[String])