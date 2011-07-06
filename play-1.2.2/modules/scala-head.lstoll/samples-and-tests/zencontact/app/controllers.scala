package controllers

import play._
import play.mvc._
import play.data.validation._

import java.util.Date

import models._

import play.db.anorm._

object Application extends Controller {
    
    import views.Application._
    
    def index = {
        html.index(new Date)
    }
    
    def list = {
        html.list(Contact.find("order by name, firstname ASC").list())
    }
       
    def edit(id: Option[Long]) = {
        html.edit {
            id.flatMap( id => Contact.find("id={id}").onParams(id).first() )
        }
    }
    
    def save(id: Option[Long]) = {
        val contact = params.get("contact", classOf[Contact])
        Validation.valid("contact", contact) // We need a more powerful way to achieve this
        if(Validation.hasErrors) {
            html.edit(Some(contact))
        } else {
            id match {
                case Some(id) => Contact.update(contact)
                case None => Contact.create(contact)
            }
            Action(list)
        }
    }
    
    def delete(id: Long) = {
        Contact.delete("id={id}").onParams(id).executeUpdate()
        Action(list)
    }
    
}

