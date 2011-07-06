package models

import play.db.jpa._
import play.db.jpa.Annotations._
import play.data.validation.Annotations._

import java.util._


@Entity class Contact(
    @Required var firstname: String,
    @Required var name: String,
    @Required var birthdate: Date,
    @Required @Email var email: String,
    @Required @ManyToOne var company: Company
) extends Model {
    override def toString = name + " " + firstname
}

@Entity class Company(
    @Required var name: String,
    @Lob @MaxSize(1000) var address: String
) extends Model {
    override def toString = name

    def asUser = new User("xx", "xx", "xx")
}
