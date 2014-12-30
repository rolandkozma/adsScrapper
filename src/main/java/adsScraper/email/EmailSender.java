package adsScraper.email;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adsScraper.dto.MinimumAdsDetailDto;

@Stateless
public class EmailSender {
	private static final Logger LOG = LoggerFactory.getLogger(EmailSender.class);

	@Resource(mappedName = "java:jboss/mail/Default")
	private Session mailSession;

	@Asynchronous
	public void sendEmail(List<MinimumAdsDetailDto> allFoundAds) {
		try {
			MimeMessage m = new MimeMessage(mailSession);
			Address from = new InternetAddress("kozma_roland@yahoo.com");
			Address[] to = new InternetAddress[]{new InternetAddress("kozma_roland@yahoo.com")};

			m.setFrom(from);
			m.setRecipients(Message.RecipientType.TO, to);
			m.setSubject("Apartments");
			m.setSentDate(new java.util.Date());
			m.setContent("Mail sent from JBoss AS 7", "text/plain");
			Transport.send(m);
		} catch (MessagingException e) {
			LOG.warn(e.getMessage(), e);
		}
	}
}