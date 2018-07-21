package com.kavi.services.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.kavi.services.intf.EmailService;

public class AWSEmailServiceImpl implements EmailService{
	
	private String from, to,cc,bcc, subject, body, fileName, attachmentPath;
	private AmazonSimpleEmailService simpleEmailService;
	private Body htmlBody;
	
	public AWSEmailServiceImpl(AmazonSimpleEmailService simpleEmailService) {
		this.simpleEmailService = simpleEmailService;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setAttachmentPath(String attachmentPath) {
		this.attachmentPath = attachmentPath;
	}

	public EmailService withFrom(String from) {
		this.from = from;
		return this;
	}

	public EmailService withTo(String to) {
		this.to = to;
		return this;
	}

	public EmailService withSubject(String subject) {
		this.subject = subject;
		return this;
	}

	public EmailService withBody(String body) {
		this.body = body;
		return this;
	}
	
	private List<String> getToAsList(){
		return Arrays.asList(to.split(","));
	}
	

	public void sendEmail() {
		Destination destination = new Destination(getToAsList());
		SendEmailRequest request = new SendEmailRequest(from,destination,createMessage());
		simpleEmailService.sendEmail(request);
	}
	
	public void sendEmailWithHtmlContent() {
		Destination destination = new Destination(getToAsList());
		SendEmailRequest request = new SendEmailRequest(from,destination,createHtmlMessage());
		simpleEmailService.sendEmail(request);
	}
	
	private Message createHtmlMessage() {
		Message message = new Message(new Content(subject), htmlBody);
		return message;
	}
	
	
	private Message createMessage() {
		Body awsBody = new Body(new Content(body));
		Message message = new Message(new Content(subject), awsBody);
		return message;
	}

	public void sendEmailWithAttachments(Session session,boolean bccflag) throws AddressException, MessagingException, IOException {
		MimeMessage message = new MimeMessage(session);
		message.setSubject(subject, "UTF-8");
        message.setFrom(new InternetAddress(from));
        message.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(to));
        if(bccflag) {
        	 message.setRecipients(javax.mail.Message.RecipientType.BCC, InternetAddress.parse(bcc));
        }
        if(attachmentPath!=null && fileName!=null) {
	        MimeMultipart mp = new MimeMultipart();
	        BodyPart attachment = new MimeBodyPart();
	        DataSource source = new FileDataSource(attachmentPath);
	        attachment.setDataHandler(new DataHandler(source));
	        attachment.setFileName(fileName);
	        MimeBodyPart textBodyPart = new MimeBodyPart();
	        textBodyPart.setText(body);
	        mp.addBodyPart(textBodyPart);
		    mp.addBodyPart(attachment);
	        message.setContent(mp);
        }
        else {
        	message.setText(body, "UTF-8");
        }
        //Printing the email body
        PrintStream out = System.out;
        message.writeTo(out);
        
        //Sending email with attachments
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        message.writeTo(outputStream);
        RawMessage rawMessage = 
        		new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
        simpleEmailService.sendRawEmail(new SendRawEmailRequest().withRawMessage(rawMessage));
	}
	
	
	public EmailService withFileName(String fileName) {
		this.fileName=fileName;
		return this;
	}

	public EmailService withAttachmentPath(String attachmentPath) {
		this.attachmentPath=attachmentPath;
		return this;
	}

	
	public void setHtmlBody(Body htmlBody) {
		this.htmlBody=htmlBody;
	}

	public EmailService withBody(Body htmlBody) {
		this.htmlBody = htmlBody;
		return this;
	}

	public void setCc(String cc) {
		this.cc=cc;
	}

	public void setBcc(String bcc) {
		this.bcc=bcc;
	}

	public EmailService withCc(String cc) {
		this.cc=cc;
		return this;
	}

	public EmailService withBcc(String bcc) {
		this.bcc=bcc;
		return this;
	}

	


}
