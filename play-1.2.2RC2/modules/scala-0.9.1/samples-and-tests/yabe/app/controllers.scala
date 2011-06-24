package controllers

import play._
import play.mvc._
import play.libs._
import play.cache._
import play.data.validation._

import models._

object Application extends Controller {
    
    import views.Application._
    
    def index = {
        val allPosts = Post.allWithAuthorAndComments
        html.index(front = allPosts.headOption, older = allPosts.drop(1))
    }
    
    def show(id: Long) = {
        Post.byIdWithAuthorAndComments(id).map { post =>
            html.show(post, post._1.prevNext, Codec.UUID)
        } getOrElse {
            NotFound("No such Post")
        }
    }
    
    def postComment(postId:Long) = {
        val author = params.get("author")
        val content = params.get("content")
        val code = params.get("code")
        val randomID = params.get("randomID")
        Validation.required("author", author).message("Author is required")
        Validation.required("content", content).message("Content is required")
        
        println(code)
        println(Cache.get(randomID).orNull)
        
        Validation.equals("code", code, "code", Cache.get(randomID).orNull).message(
            "Invalid code. Please type it again"
        )
        if(Validation.hasErrors) {
            show(postId)
        } else {
            Comment.create(Comment(postId, author, content))
            flash += "success" -> ("Thanks for posting " + author)
            Cache.delete(randomID)
            Action(show(postId))
        }
    }
    
    def captcha(id:String) = {
        val captcha = Images.captcha
        val code = captcha.getText("#E4EAFD")
        Cache.set(id, code, "10mn")
        captcha
    }
    
}
