import play.test._
import play.db.DB

import org.junit._
import org.scalatest.junit._
import org.scalatest._
import org.scalatest.matchers._

import com.novocode.squery.session._
import com.novocode.squery.combinator._
import com.novocode.squery.combinator.TypeMapper._
import com.novocode.squery.combinator.basic.BasicDriver.Implicit._
import com.novocode.squery.session.Database.threadLocalSession
import com.novocode.squery.simple.StaticQueryBase._
/* THIS IS TEMP DISABLED UNTIL SCALAQUERY 2.8RC2 is OUT
class TryingScalaQueryTests extends UnitFlatSpec with ShouldMatchers {
    
    DB.execute("DROP TABLE IF EXISTS users") 
    
    "ScalaQuery" should "run with play" in {    
        
        Database.forDataSource(DB.datasource) withSession {
            
            Users.createTable
            Users.insertAll(
                (0, "Guillaume", Some("Bort")),
                (1, "Bob", None),
                (2, "Chuck", Some("Norris"))
            )
            
            val users = ( for(u <- Users) yield u ).list
            
            users.size should be (3)
            users(0) should be (0, "Guillaume", Some("Bort"))
            users(1) should be (1, "Bob", None)
            
            val users2 = ( for(u <- Users) yield u.first ~ u.last.orElse(Some("Unknown")) ).list
            
            users2.size should be (3)
            users2(0) should be ("Guillaume", Some("Bort"))
            users2(1) should be ("Bob", Some("Unknown"))
            
            val usersWithLastname = ( for(u <- Users if u.last isNotNull) yield u.id ).list
            usersWithLastname.size should be (2)
            usersWithLastname(0) should be (0)
            usersWithLastname(1) should be (2)
            
            (Users where (_.first is "Guillaume") selectStatement) should be ("SELECT t1.id,t1.first,t1.last FROM users t1 WHERE (t1.first='Guillaume')")
            
            val guillaume = Users where (_.first is "Guillaume") first()
            guillaume should be (0, "Guillaume", Some("Bort"))
        }
        
    }
    
}

object Users extends Table[(Int, String, Option[String])]("users") {
    
    def id = column[Int]("id", O PrimaryKey, O NotNull)
    def first = column[String]("first", O DBType "varchar(64)")
    def last = column[Option[String]]("last")
    def * = id ~ first ~ last
   
}
*/
