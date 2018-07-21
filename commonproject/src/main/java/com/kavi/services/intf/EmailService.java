package com.kavi.services.intf;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;

import com.amazonaws.services.simpleemail.model.Body;

public interface EmailService {
	
	public void setFrom(String from);
	public void setTo(String to);
	public void setCc(String cc);
	public void setBcc(String bcc);
	public void setSubject(String subject);
	public void setBody(String body);
	public void setFileName(String fileName);
	public void setAttachmentPath(String attachmentPath);
	public void setHtmlBody(Body htmlBody);
	public void sendEmail();
	public void sendEmailWithHtmlContent();
	public void sendEmailWithAttachments(Session session,boolean bccflag) throws AddressException, MessagingException, IOException;
	EmailService withFrom(String from);
	EmailService withTo(String to);
	EmailService withCc(String cc);
	EmailService withBcc(String bcc);
	EmailService withSubject(String subject);
	EmailService withBody(String htmlBody);
	EmailService withBody(Body htmlBody);
	EmailService withFileName(String fileName);
	EmailService withAttachmentPath(String attachmentPath);

}
