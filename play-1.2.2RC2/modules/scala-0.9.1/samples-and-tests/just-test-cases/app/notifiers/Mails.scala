package notifiers

import play.mvc._
import models._
import play.Play
import org.apache.commons.mail.EmailAttachment

class Mails extends Mailer {
   def welcome(user: User) {
      setSubject("Welcome %s", user.fullname)
      addRecipient(user.email)
      setFrom("Me <me@me.com>")
      val attachment = new EmailAttachment()
      attachment.setPath(Play.getFile("rules.pdf").toString)
      addAttachment(attachment)
      send(user)
   }

  def lostPassword(user: User) {
      val newpassword = user.password
      setFrom("Robot <robot@thecompany.com>")
      setSubject("Your password has been reset")
      addRecipient(user.email)
      send(user, newpassword)
   }
}

