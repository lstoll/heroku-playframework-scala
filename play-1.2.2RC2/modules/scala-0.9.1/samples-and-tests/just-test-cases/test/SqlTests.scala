import play.test._

import org.junit._
import org.scalatest.junit._
import org.scalatest._
import org.scalatest.matchers._

import play.db.anorm._
import SqlParser._ 
import defaults._

// Constructors with unspported type of parameter won't be picked
case class Task(ids:Option[List[Int]],comment:String){
  def this(ids:Option[List[Int]])=this(ids,"no comment.")
  def this(comment:String)=this(None,comment)
}
import Row._
object Task extends Magic[Task]
case class Student(id:String)
object Student extends Magic[Student]

class SqlTests extends UnitTestCase with ShouldMatchersForJUnit {
  def meta(items:(String,(Boolean,Class[_]))*)=MetaData(items.toList.map(i=>MetaDataItem(i._1,i._2._1,i._2._2.getName)))

  @Test def useTheMagicParser { 
      val metaData=meta("TASK.COMMENT"->(true,classOf[String]),
                        "TASK.NAME"->(false,classOf[String]))
   
      val in= StreamReader(Stream.range(1,2).map(i => MockRow(List("comment no:"+i, "nameb"),metaData)))
     // commit((str("COMMENT1")))* (in) should be (Error(ColumnNotFound("COMMENT1").toString,in))

      val Error(msg,next) = commit((str("COMMENT1")))* (in)
      next should be (PError(ColumnNotFound("COMMENT1").toString,in).next)
      
      ((str("COMMENT1")) ?)(in).get should be (None)

      (str("COMMENT1"))+ (in) should be (PFailure(ColumnNotFound("COMMENT1").toString,in))


      commit((str("COMMENT1")))* (in) should be (PError(ColumnNotFound("COMMENT1").toString,in))

      val inWithNull= StreamReader(Stream(MockRow(List(null, "nameb"),metaData)))

      (((str("COMMENT1"))*) (in)).get should be (List())

      str("TASK.COMMENT")+ (inWithNull) should be (
        PFailure(UnexpectedNullableFound("TASK.COMMENT").toString,inWithNull))

      str("COMMENT")+ (inWithNull) should be (
        PFailure(UnexpectedNullableFound("TASK.COMMENT").toString,inWithNull))

          import Magic._
      (Task* (in)).get should be(Success(List(Task(None,"comment no:1")),in).get)
      ((Task)+ (in)).get should be((Success(List(Task(None,"comment no:1")),in).get))

      

      val metaData1=meta("TASK.COMMENT"->(false,classOf[String]),
                        "TASK.NAME"->(false,classOf[String]))
      val in1= StreamReader(Stream.range(1,100).map(i => MockRow(List("comment no:"+i, "nameb"),metaData1)))

      import Row._
     commit((Task) +)(in1).get should be(
        List.range(1,100).map(i=>new Task("comment no:"+i)))
  }

  @Test def testNullables {
    import play.db.anorm.Row._
     val metaData=meta("PERSON.ID"->(true,classOf[java.lang.Integer]))
     val in= StreamReader(Stream.range(1,100).map(i=>MockRow(List( if(i % 2 ==0) i else null),metaData)))
     println(commit((get[Option[Int]]("PERSON.ID")) +)(in) )
  }
 
  @Test def testNullInNonNullable {
    import play.db.anorm.Row._
     val metaData=meta("PERSON.ID"->(false,classOf[java.lang.Integer]))
     val in= StreamReader(Stream.range(1,2).map(i=>MockRow(List(null),metaData)))
     println(commit((get[Int]("PERSON.ID")) +)(in) )
  }
 
  @Test def useSqlParserForGroupBys {
    val metaData=meta("PERSON.ID"->(false,classOf[Int]),
                      "PERSON.NAME"->(false,classOf[String]),
                      "PERSON.AGE"->(true,classOf[Int]),
                      "COMMENT.ID"->(false,classOf[Int]),
                      "COMMENT.TEXT"->(false,classOf[String]))
   
    val in= Stream.range(1,4)
                  .flatMap(i => 
                    Stream.range(1,4)
                           .map(j =>
                             MockRow(List(i, "person"+i, 13, j, "comment"+j),metaData)))
    // manual groupBy
   
    val groupByPerson=spanM(by=int("PERSON.ID"),(str("COMMENT.TEXT")))* ;
    groupByPerson(StreamReader(in)).get should be (
      List.fill(3)(List.range(1,4).map("comment"+_)))
    
    // "magical" groupBy
    import Magic._
    val parsePeople = Person ~< (Person spanM Comment) ^^ 
                      {case  p ~ cs  => p.copy(comments=cs) } *;

    ( parsePeople (StreamReader(in)) get ) should be (
      List.range(1,4).map(
        i=> Person(i, "person"+i, Seq.range(1,4).map(
              j=> Comment(j,"comment"+j) ))))

  }
  
  @Test def useSomeMagicSql{
    
     play.db.DB.execute("DROP TABLE IF EXISTS Task")
      play.db.DB.execute("DROP TABLE IF EXISTS Student") 
     play.db.DB.execute("""CREATE TABLE Task 
                           (Id char(60) NOT NULL,
                            Comment char(60) NOT NULL) """)
         play.db.DB.execute("""CREATE TABLE Student 
                           (Id char(60) NOT NULL,
                            Task_Id char(60) NOT NULL) """)

    play.db.DB.execute("""insert into Task Values('1','some comment')""")
    play.db.DB.execute("""insert into Student Values('1','1')""")
    Task.find("where id={id}").on("id" -> 1).first() should be (Some(new Task("some comment")))
    Task.find().list() should be (List(new Task("some comment")))
    Task.find().list() should be (List(new Task("some comment")))
    Task.find("where id={id}").on("id" -> 3).first() should be (None)
    SQL("select * from Task join Student on Task.id=Student.Task_Id").as(Task ~< Student) should be (SqlParser.~(new Task("some comment"),Student("1")))
  }

  @Test def trySomeMagicSqlFailure{
    
     play.db.DB.execute("DROP TABLE IF EXISTS Task")
      play.db.DB.execute("DROP TABLE IF EXISTS Student") 
     play.db.DB.execute("""CREATE TABLE Task 
                           (Id char(60) NOT NULL,
                            Comment1 char(60) NOT NULL) """)
         play.db.DB.execute("""CREATE TABLE Student 
                           (Id char(60) NOT NULL,
                            Task_Id char(60) NOT NULL) """)

    play.db.DB.execute("""insert into Task Values('1','some comment')""")
    play.db.DB.execute("""insert into Student Values('1','1')""")
   
    val t = evaluating { Task.find("where id={id}").on("id" -> 1).first() } should produce [RuntimeException] 
    t.getMessage should equal ("ColumnNotFound(Task.comment)")

    val thrown = evaluating { Task.find().list() } should produce [RuntimeException]
    thrown.getMessage should equal ("ColumnNotFound(Task.comment)")

    val thrown1 = evaluating {
      SQL("select * from Task join Student on Task.id=Student.Task_Id")
        .as(Task ~< Student)
      }  should produce [RuntimeException]
    thrown1.getMessage should equal ("ColumnNotFound(Task.comment)")
  }  

  @Test def testAlternate(){
    play.db.DB.execute("DROP TABLE IF EXISTS Post")

    play.db.DB.execute("""CREATE TABLE Post 
                       (Id char(60) NOT NULL,
                       Type char(60) NOT NULL,
                       Title char(60) NOT NULL,
                       URL char(200)  DEFAULT 'non' NOT Null,
                       Body char(360) DEFAULT 'non' NOT Null) """)
      
    play.db.DB.execute("""insert into Post Values('1','Link','zengularity','http://www.zengularity.com','non')""")
    play.db.DB.execute("""insert into Post Values('1','Text','Functional Web','non','It rocks!')""")
    play.db.DB.execute("""insert into Post Values('1','Text','Functional Web','non','It rocks!')""")
    import Row._
     play.db.anorm.Sql.sql("select * from Post")
              .as( 'TYPE.is("Text") ~> Text |
                   'TYPE.is("Link") ~> Link +) should be (
                     List(Link("1","zengularity","http://www.zengularity.com"),
                          Text("1","Functional Web","It rocks!"),
                          Text("1","Functional Web","It rocks!")))

  }
  @Test def insertAnEntity(){
    play.db.DB.execute("DROP TABLE IF EXISTS User2")
      
    play.db.DB.execute("""CREATE TABLE User2 
                       (Id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY ,
                       Address char(60) NOT NULL,
                       Name char(60) NOT NULL) """)
    User2.create(User2(NotAssigned,"Paul","Address")).right.get should be (User2(Id(1),"Paul","Address"))
  }

  @Test def batchInsertAnEntity(){
    play.db.DB.execute("DROP TABLE IF EXISTS User2")
      
    play.db.DB.execute("""CREATE TABLE User2 
                       (Id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY ,
                       Address char(60) NOT NULL,
                       Name char(60) NOT NULL) """)
    
   SQL("insert into User2 (Address, Name) values( {address}, {name})")
      .addBatch("address" -> "sss","name" -> "name1")
      .addBatch("address" -> "sss","name" -> "name1").execute()

   SQL("select * from User2")().toList.length should be(2)

  }


  @Test def updateAnEntity(){
    play.db.DB.execute("DROP TABLE IF EXISTS User2")
      
    play.db.DB.execute("""CREATE TABLE User2 
                       (Id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY ,
                       Address char(60) NOT NULL,
                       Name char(60) NOT NULL) """)
    val newUser:User2 = User2.create(User2(NotAssigned,"Paul","Address")).right.get 
    User2.update(newUser.copy(name="new name")) should be ()
  }

  @Test def count(){
    play.db.DB.execute("DROP TABLE IF EXISTS User2")
      
    play.db.DB.execute("""CREATE TABLE User2 
                       (Id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY ,
                       Address char(60) NOT NULL,
                       Name char(60) NOT NULL) """)

    play.db.DB.execute("""insert into User2 (Address, Name) Values('address','Paul')""")
    play.db.DB.execute("""insert into User2 (Address, Name) Values('address','Paul')""")
    User2.count().single() should be (2)
  }

}

case class User2(id:Pk[Long],name:String,address:String)
import Row._
object User2 extends Magic[User2]


abstract class Post

  case class Link(id:String,title:String,url:String) extends Post
  object Link extends Magic[Link](Some("POST"))
  case class Text(id:String,title:String,body:String) extends Post
  object Text extends Magic[Text](Some("POST"))
case class Person(id: Int,name:String,comments:Seq[Comment]) {
  def this(id:Int,name:String)=this(id,name,List())
}
object Person extends Magic[Person]
case class Comment(id: Int,text:String) 
object Comment extends Magic[Comment] 

