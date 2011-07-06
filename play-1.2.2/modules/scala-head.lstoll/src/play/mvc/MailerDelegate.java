package play.mvc;

import java.util.concurrent.Future;

import org.apache.commons.mail.*;

/**
 * Creates a delegate which can be used to take over play.mvc.Mailer namespace with a type
 * alias. Extending from this class means that we can avoid circular references which would
 * occur if ScalaMailer was inherited directly from @see play.mvc.Controller and we used a type alias
 * to map ScalaMailer to play.mvc.Mailer
 * This class will be removed before 1.1
 */
@Deprecated
public abstract class MailerDelegate {

    public void setSubject(String subject, Object... args) {
        Mailer.setSubject(subject, args);
    }

    public void addRecipient(Object... recipients) {
        Mailer.addRecipient(recipients);
    }

    public void addBcc(Object... bccs) {
        Mailer.addBcc(bccs);
    }

    public void addCc(Object... ccs) {
        Mailer.addCc(ccs);
    }

    public void addAttachment(EmailAttachment... attachments) {
        Mailer.addAttachment(attachments);
    }

    public void setContentType(String contentType) {
        Mailer.setContentType(contentType);
    }
    public  void setFrom(Object from) {
        Mailer.setFrom(from);
    }
    public void setReplyTo(Object replyTo) {
        Mailer.setReplyTo(replyTo);
    }

    public void setCharset(String bodyCharset) {
        Mailer.setCharset(bodyCharset);
    }

    public void addHeader(String key, String value) {
        Mailer.addHeader(key, value);
    }

    public Future<Boolean> send(Object... args) {
        return Mailer.send(args);
    }

    public static boolean sendAndWait(Object... args) throws EmailException {
        return Mailer.sendAndWait(args);
    }

}
