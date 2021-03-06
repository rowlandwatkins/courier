package courier

import javax.mail.{ Message, Session => MailSession, Transport }
import javax.mail.internet.MimeMessage
import scala.concurrent.{ ExecutionContext, future }

object Mailer {
  def apply(host: String, port: Int): Session.Builder =
    Mailer().session.host(host).port(port)
}

case class Mailer(
  _session: MailSession = Defaults.session) {
  def session = Session.Builder(this)

  def apply(e: Envelope)(implicit ec: ExecutionContext) = {
    val msg = new MimeMessage(_session) {
      e.subject.map(setSubject(_))
      setFrom(e.from)
      e.to.foreach(addRecipient(Message.RecipientType.TO, _))
      e.cc.foreach(addRecipient(Message.RecipientType.CC, _))
      e.bcc.foreach(addRecipient(Message.RecipientType.BCC, _))
      e.contents match {
        case Text(txt, charset) => setText(txt, charset.displayName)
        case mp @ Multipart(_) => setContent(mp.parts)
      }
    }
    future {
      Transport.send(msg)
    }
  }
}
