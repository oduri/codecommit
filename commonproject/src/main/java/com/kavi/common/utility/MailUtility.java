package com.kavi.common.utility;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.kavi.common.constants.CommonConstants;


public class MailUtility {
	
	 private final static Logger logger = Logger.getLogger(MailUtility.class);
	
	 /**
	  * 
	  * @param object
	  * @param emailId
	  * @return
	  */
	 public  static boolean sendEmail(String userId,String temporaryPassword,String emailId){
		//Properties props = System.getProperties();
		 Properties props = new Properties();
		try {
		
		     //props.setProperty("mail.smtp.host", CommonConstants.SMTP_LOCAL_HOST_NAME);
		     //props.setProperty("mail.smtp.host", "localhost");
		     //props.setProperty("mail.smtp.host", "127.0.0.1");
		     //props.setProperty("mail.smtp.port", "25");
		    // props.put("mail.smtp.auth", "false");
		     //props.put("mail.debug", "true");
		     
		     /*props.setProperty("mail.smtp.host", CommonConstants.SMTP_HOST_NAME);
		     props.setProperty("mail.smtp.socketFactory.class", CommonConstants.SSL_FACTORY);
		     props.setProperty("mail.smtp.socketFactory.fallback", "false");
		     props.setProperty("mail.smtp.port", "465");
		     props.setProperty("mail.smtp.socketFactory.port", "465");
		     props.put("mail.smtp.auth", "true");
		     props.put("mail.store.protocol", "pop3");
		     props.put("mail.transport.protocol", "smtp");
		     
		     Session session = Session.getDefaultInstance(props, 
		                              new Authenticator(){
		                                 protected PasswordAuthentication getPasswordAuthentication() {
		                                    return new PasswordAuthentication(CommonConstants.SMTP_AUTH_USER, CommonConstants.SMTP_AUTH_PWD);
		                                 }});*/
			
		    props.setProperty("mail.smtp.socketFactory.fallback", "false");
			props.put("mail.smtp.host", CommonConstants.SMTP_HOST_SERVER);
			props.setProperty("mail.smtp.port", "25");
			
		    Session session = Session.getDefaultInstance(props,null); 
		    
		    StringBuilder sb = MailUtility.getForgotEmailBody(userId, temporaryPassword);
			    
		     return MailUtility.sendEmail(session, emailId,"********** Temporary Password**********"+new java.util.Date(), sb.toString());
			
			/*MimeMessage message=new MimeMessage(session);
		    message.setFrom(new InternetAddress(CommonConstants.SMTP_AUTH_USER));
		    StringBuilder sb = new StringBuilder();
		    sb.append("You have requested a temporary password for the application. Please find the credentials below. "
		    		+ "If you have any questions or problems, write to us at "+CommonConstants.SMTP_MAIL_ID);
		    sb.append("\n");
		    sb.append("\n");
		    sb.append("your user id: "+userId);
		    sb.append("\n");
		    sb.append("\n");
		    sb.append("your temporary password: "+temporaryPassword);
		    sb.append("\n");
		    sb.append("\n");
		    sb.append("Note: If you did not request your password for your account, ignore this message.");
		    sb.append("\n");
		    sb.append("\n");
		    message.setText(sb.toString());
			message.setSubject("********** Temporary Password**********"+new java.util.Date());
			Address[] toAddress = new Address[] {
                     new InternetAddress(emailId)};
				 
			message.addRecipients(Message.RecipientType.TO,toAddress);  
			Transport.send(message);*/
				
		} catch ( Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	
	 
	 public static StringBuilder getForgotEmailBody(String userId, String temporaryPassword) {
		 StringBuilder sb = new StringBuilder();
		    sb.append("You have requested a temporary password for the application. Please find the credentials below. "
		    		+ "If you have any questions or problems, write to us at "+CommonConstants.SMTP_MAIL_ID);
		    sb.append("\n");
		    sb.append("\n");
		    sb.append("your user id: "+userId);
		    sb.append("\n");
		    sb.append("\n");
		    sb.append("your temporary password: "+temporaryPassword);
		    sb.append("\n");
		    sb.append("\n");
		    sb.append("Note: If you did not request your password for your account, ignore this message.");
		    sb.append("\n");
		    sb.append("\n");
		 return sb;
	 }
	 
	 /**
	  * 
	  * @param object
	  * @param emailId
	  * @return
	  */
	 public  static boolean sendEmailToSupportTeam(Document doc){
		//Properties props = System.getProperties();
		 Properties props = new Properties();
		try {
			
		    props.setProperty("mail.smtp.socketFactory.fallback", "false");
			props.put("mail.smtp.host", CommonConstants.SMTP_HOST_SERVER);
			props.setProperty("mail.smtp.port", "25");
		    Session session = Session.getDefaultInstance(props,null); 
		    
		    StringBuilder sb = new StringBuilder();
		    sb.append(doc.getString("message"));
		    sb.append("\n");
		    sb.append("\n");
		    sb.append(doc.getString("body"));
		    sb.append("\n");
		    sb.append("\n");
		    return MailUtility.sendEmailToMultiplePersons(session, doc.getString("emailAddresses"),"********** "+doc.getString("subject")+" **********"+new java.util.Date(), sb.toString());
				
		} catch ( Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	 
	/**
	 * 
	 * @param session
	 * @param toEmail
	 * @param subject
	 * @param body
	 * @return
	 */
		public static boolean sendEmailToMultiplePersons(Session session, String addresses, String subject, String body){
			try
		    {
		      MimeMessage msg = new MimeMessage(session);
		      //set message headers
		      msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
		      msg.addHeader("format", "flowed");
		      msg.addHeader("Content-Transfer-Encoding", "8bit");

		      msg.setFrom(new InternetAddress(CommonConstants.SMTP_AUTH_USER, "No-Reply"));

		      msg.setReplyTo(InternetAddress.parse(CommonConstants.SMTP_AUTH_USER, false));
		      msg.setSubject(subject, "UTF-8");
		      msg.setContent(body, "text/html; charset=utf-8");
		      msg.setSentDate(new java.util.Date());
		      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(addresses));
	    	  Transport.send(msg);  

		      return true;
		    }
		    catch (Exception e) {
		      e.printStackTrace();
		    }
			return false;
		}
		
	 /**
		 * Utility method to send simple HTML email
		 * @param session
		 * @param toEmail
		 * @param subject
		 * @param body
		 */
		public static boolean sendEmail(Session session, String toEmail, String subject, String body){
			try
		    {
		      MimeMessage msg = new MimeMessage(session);
		      //set message headers
		      msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
		      msg.addHeader("format", "flowed");
		      msg.addHeader("Content-Transfer-Encoding", "8bit");

		      msg.setFrom(new InternetAddress(CommonConstants.SMTP_AUTH_USER, "No-Reply"));

		      msg.setReplyTo(InternetAddress.parse(CommonConstants.SMTP_AUTH_USER, false));

		      msg.setSubject(subject, "UTF-8");

		      msg.setText(body, "UTF-8");

		      msg.setSentDate(new java.util.Date());
		      
		      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
	    	  Transport.send(msg);  

		      return true;
		    }
		    catch (Exception e) {
		      e.printStackTrace();
		    }
			return false;
		}
		
		/**
		 * @param toEmail
		 * @param subject
		 * @param body
		 * @return
		 */
		public static boolean sendNotificationEmail(String toEmail, String subject, String body,String attachementPath,String fileName,String adminEmail) {
			Properties props = new Properties();
			try
		    {
				props.setProperty("mail.smtp.socketFactory.fallback", "false");
				props.put("mail.smtp.host", CommonConstants.SMTP_HOST_SERVER);
				props.setProperty("mail.smtp.port", "25");
			    Session session = Session.getDefaultInstance(props,null); 
				/* props.setProperty("mail.smtp.host", "smtp.gmail.com");
			     props.setProperty("mail.smtp.socketFactory.class", CommonConstants.SSL_FACTORY);
			     props.setProperty("mail.smtp.socketFactory.fallback", "false");
			     props.setProperty("mail.smtp.port", "465");
			     props.setProperty("mail.smtp.socketFactory.port", "465");
			     props.put("mail.smtp.auth", "true");
			     props.put("mail.store.protocol", "pop3");
			     props.put("mail.transport.protocol", "smtp");
			     
			     Session session = Session.getDefaultInstance(props, 
			                              new Authenticator(){
			                                 protected PasswordAuthentication getPasswordAuthentication() {
			                                    return new PasswordAuthentication("saravanan.periyasamy@kaviglobal.com", "aaaaaa");
			                                 }});*/
			    MimeMessage msg = new MimeMessage(session);
			    msg.setFrom(new InternetAddress(CommonConstants.SMTP_AUTH_USER, "No-Reply"));
			    msg.setReplyTo(InternetAddress.parse(CommonConstants.SMTP_AUTH_USER, false));
			    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
			    msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(adminEmail, false));
		        msg.setSubject(subject);

		        if(attachementPath!=null && fileName!=null) {
		        Multipart multipart = new MimeMultipart();

		        MimeBodyPart textBodyPart = new MimeBodyPart();
		        textBodyPart.setText(body);

		        MimeBodyPart attachmentBodyPart= new MimeBodyPart();
		        DataSource source = new FileDataSource(attachementPath); 
		        attachmentBodyPart.setDataHandler(new DataHandler(source));
		        attachmentBodyPart.setFileName(fileName); 
		        multipart.addBodyPart(textBodyPart);  
		        multipart.addBodyPart(attachmentBodyPart);
		        msg.setContent(multipart);
		        }else {
		        	msg.setText(body, "UTF-8");
		        }
		        Transport.send(msg);
			    return true; 
		    }
		    catch (Exception e) {
		      e.printStackTrace();
		      return false;
		    }
		}
		
		public static void main(String args[]) {
			sendNotificationEmail("saravanan.periyasamy@kaviglobal.com", "test", "test", "C:\\Users\\sarav\\Molex\\MolexMicroServices\\ticket\\RuleGeneration.vm","RuleGeneration.vm","saravanan.periyasamy@kaviglobal.com");
		}
}