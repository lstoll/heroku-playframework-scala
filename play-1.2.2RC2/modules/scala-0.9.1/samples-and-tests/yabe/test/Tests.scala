import play._
import play.test._
 
import org.scalatest._
import org.scalatest.junit._
import org.scalatest.matchers._
 
class BasicTests extends UnitFlatSpec with ShouldMatchers with BeforeAndAfterEach {
    
    import models._    
    import play.db.anorm._
    
    override def beforeEach() {
        Fixtures.deleteDatabase()
    }

    it should "create and retrieve a User" in {

        User.create(User(NotAssigned, "bob@gmail.com", "secret", "Bob", false))

        val bob = User.find(
            "email={email}").on("email" -> "bob@gmail.com"
        ).first()

        bob should not be (None)
        bob.get.fullname should be ("Bob")

    }
    
    it should "connect a User" in {

        User.create(User(NotAssigned, "bob@gmail.com", "secret", "Bob", false))

        User.connect("bob@gmail.com", "secret") should not be (None)
        User.connect("bob@gmail.com", "badpassword") should be (None)
        User.connect("tom@gmail.com", "secret") should be (None)

    }
    
    import java.util.{Date}
    
    it should "create a Post" in {

        User.create(User(Id(1), "bob@gmail.com", "secret", "Bob", false))     
        Post.create(Post(NotAssigned, "My first post", "Hello!", new Date, 1))

        Post.count().single() should be (1)

        val posts = Post.find("author_id={id}").on("id" -> 1).as(Post*)

        posts.length should be (1)

        val firstPost = posts.headOption

        firstPost should not be (None)
        firstPost.get.author_id should be (1)
        firstPost.get.title should be ("My first post")
        firstPost.get.content should be ("Hello!")

    }
    
    it should "retrieve Posts with author" in {

        User.create(User(Id(1), "bob@gmail.com", "secret", "Bob", false)) 
        Post.create(Post(NotAssigned, "My 1st post", "Hello world", new Date, 1))

        val posts = Post.allWithAuthor

        posts.length should be (1)

        val (post,author) = posts.head

        post.title should be ("My 1st post")
        author.fullname should be ("Bob")
    }
    
    it should "support Comments" in {

        User.create(User(Id(1), "bob@gmail.com", "secret", "Bob", false))  
        Post.create(Post(Id(1), "My first post", "Hello world", new Date, 1))
        Comment.create(Comment(NotAssigned, "Jeff", "Nice post", new Date, 1))
        Comment.create(Comment(NotAssigned, "Tom", "I knew that !", new Date, 1))

        User.count().single() should be (1)
        Post.count().single() should be (1)
        Comment.count().single() should be (2)

        val Some( (post,author,comments) ) = Post.byIdWithAuthorAndComments(1)

        post.title should be ("My first post")
        author.fullname should be ("Bob")
        comments.length should be (2)
        comments(0).author should be ("Jeff")
        comments(1).author should be ("Tom")

    }
    
    it should "load a complex graph from Yaml" in {

        Yaml[List[Any]]("data.yml").foreach { 
            _ match {
                case u:User => User.create(u)
                case p:Post => Post.create(p)
                case c:Comment => Comment.create(c)
            }
        }

        User.count().single() should be (2)
        Post.count().single() should be (3)
        Comment.count().single() should be (3)

        User.connect("bob@gmail.com", "secret") should not be (None)
        User.connect("jeff@gmail.com", "secret") should not be (None)
        User.connect("jeff@gmail.com", "badpassword") should be (None)
        User.connect("tom@gmail.com", "secret") should be (None)

        val allPostsWithAuthorAndComments = Post.allWithAuthorAndComments

        allPostsWithAuthorAndComments.length should be (3) 

        val (post,author,comments) = allPostsWithAuthorAndComments(2)
        post.title should be ("About the model layer")
        author.fullname should be ("Bob")
        comments.length should be (2)

        // We have a referential integrity error error
        User.delete("email={email}")
            .on("email"->"bob@gmail.com").executeUpdate().isLeft should be (true)

        Post.delete("author_id={id}")
            .on("id"->1).executeUpdate().isRight should be (true)

        User.delete("email={email}")
            .on("email"->"bob@gmail.com").executeUpdate().isRight should be (true)

        User.count().single() should be (1)
        Post.count().single() should be (1)
        Comment.count().single() should be (0)

    }
 
}