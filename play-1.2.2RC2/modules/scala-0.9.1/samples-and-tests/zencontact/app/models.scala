package models

import play.db.anorm._
import play.db.anorm.defaults._

import play.data.validation.Annotations._

import java.util._

// ~~~ Contact

case class Contact(
    id: Pk[Long],
    @Required firstname: String,
    @Required name: String,
    @Required birthdate: Date,
    @Email email: Option[String]
)

object Contact extends Magic[Contact]