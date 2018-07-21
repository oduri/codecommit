package com.kavi.reportgenerator.utility;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.kavi.common.constants.CommonConstants;
import com.kavi.common.utility.SESUtility;
import com.kavi.reportgenerator.dataobjects.ReportGeneratorDO;
import com.kavi.services.impl.AWSEmailServiceImpl;
import com.kavi.services.intf.EmailService;


public class ReportGeneratorMailUtility {
	
	 private final static Logger logger = Logger.getLogger(ReportGeneratorMailUtility.class);
	
	 
		/**
		 * 
		 * @param session
		 * @param toEmail
		 * @param subject
		 * @param body
		 * @return
		 */
		public static boolean sendEmailWithLink(String addresses,String link, int hour,Document emailConfigObject,ReportGeneratorDO pdfGeneratorDO){
		Properties props = new Properties();
			try
		    {
				Session session=null;
				if(emailConfigObject.getString("smtp_auth_pwd")!=null && emailConfigObject.getString("smtp_auth_pwd").length()>0){
					 props.setProperty("mail.smtp.host", emailConfigObject.getString("smtp_host_name"));
					 props.setProperty("mail.smtp.socketFactory.class", CommonConstants.SSL_FACTORY);
					 props.setProperty("mail.smtp.socketFactory.fallback", "false");
					 props.setProperty("mail.smtp.port", "465");
					 props.setProperty("mail.smtp.socketFactory.port", "465");
					 props.put("mail.smtp.auth", "true");
					 props.put("mail.store.protocol", "pop3");
					 props.put("mail.transport.protocol", "smtp");
					 session = Session.getDefaultInstance(props, 
					                          new Authenticator(){
					                             protected PasswordAuthentication getPasswordAuthentication() {
					                                return new PasswordAuthentication(emailConfigObject.getString("smtp_auth_user"),emailConfigObject.getString("smtp_auth_pwd"));
				 }});
				
				}else{
					props.setProperty("mail.smtp.socketFactory.fallback", "false");
					props.put("mail.smtp.host", emailConfigObject.getString("smtp_host_server"));
					props.setProperty("mail.smtp.port", "25");
					session = Session.getDefaultInstance(props,null);
				}
				String emailContent=emailConfigObject.getString("content");
				emailContent=emailContent.replace("@anchor", "<a href='"+link+"'>link</a>" );
				String successFooter=emailConfigObject.getString("success_footer");
				if(successFooter!=null && successFooter.length()>0){
					successFooter=successFooter.replace("@X", ""+hour);
				}
				String failureFooter=emailConfigObject.getString("failure_footer");
				
			    StringBuilder  sb = new StringBuilder ();
			    sb.append("<html>");
				sb.append("<meta http-equiv='content-type' content='text/html; charset=UTF-8'/>");
				sb.append("");
				if(hour==1){
					sb.append(emailContent);
					sb.append("<br/>");
					sb.append(successFooter +" hour");
				}else if(hour>1){
					sb.append(emailContent);
					sb.append("<br/>");
					sb.append(successFooter + " hours");
				}else{
					sb.append(link);
					sb.append("<br/>");
					sb.append(failureFooter);
				}
				sb.append("</html>");
				String subject=emailConfigObject.getString("subject");
				subject=subject.replace("@fileName", pdfGeneratorDO.getFileName());
				MimeMessage msg = new MimeMessage(session);
			    msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
			    msg.addHeader("format", "flowed");
			    msg.addHeader("Content-Transfer-Encoding", "8bit");
			    msg.setFrom(new InternetAddress(emailConfigObject.getString("smtp_auth_user"), "No-Reply"));
			    msg.setReplyTo(InternetAddress.parse(emailConfigObject.getString("smtp_auth_user"), false));
			    msg.setSubject(subject, "UTF-8");
			    msg.setContent(sb.toString(), "text/html; charset=utf-8");
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
		 * 
		 * @param addresses
		 * @param link
		 * @param hour
		 * @param emailConfigObject
		 * @param pdfGeneratorDO
		 * @return
		 */
		public static boolean sendToSESMail(String addresses,String link, int hour,Document emailConfigObject,ReportGeneratorDO pdfGeneratorDO){
			SESUtility sesUtil = new SESUtility();
			try {
				EmailService emailService = new AWSEmailServiceImpl(sesUtil.createSimpleEmailService());
			    String emailContent=emailConfigObject.getString("content");
				emailContent=emailContent.replace("@anchor", "<a href='"+link+"'>link</a>" );
				String successFooter=emailConfigObject.getString("success_footer");
				if(successFooter!=null && successFooter.length()>0){
					successFooter=successFooter.replace("@X", ""+hour);
				}
				String failureFooter=emailConfigObject.getString("failure_footer");
				
			    StringBuilder  sb = new StringBuilder ();
			    sb.append("<html>");
				sb.append("<meta http-equiv='content-type' content='text/html; charset=UTF-8'/>");
				sb.append("");
				if(hour==1){
					sb.append(emailContent);
					sb.append("<br/>");
					sb.append(successFooter +" hour");
				}else if(hour>1){
					sb.append(emailContent);
					sb.append("<br/>");
					sb.append(successFooter + " hours");
				}else{
					sb.append(link);
					sb.append("<br/>");
					sb.append(failureFooter);
				}
				String subject=emailConfigObject.getString("subject");
				subject=subject.replace("@fileName", pdfGeneratorDO.getFileName());
				 emailService.withFrom(emailConfigObject.getString("smtp_auth_user"))
				.withTo(addresses)
				.withSubject(subject)
				.withBody(new Body()
		                  .withHtml(new Content()
		                      .withCharset("UTF-8").withData(sb.toString())))
		        .sendEmailWithHtmlContent();
				 return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
		}
	
		
}