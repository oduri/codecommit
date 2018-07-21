package com.kavi.services.controller;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import javax.mail.Session;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kavi.common.utility.CommonUtility;
import com.kavi.common.utility.FileUtility;
import com.kavi.common.utility.MailUtility;
import com.kavi.common.utility.SESUtility;
import com.kavi.services.impl.AWSEmailServiceImpl;
import com.kavi.services.intf.EmailService;
import com.kavi.user.dataobjects.TicketResponseDO;
import com.mongo.db.MongoDBConnection;
import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

@RestController
public class TicketController {
	
	private static Properties prop = new Properties();
	/**
	 * 
	 * @param sessionkey
	 * @param json
	 * @param mode
	 * @param file
	 * @return
	 */
	
	@RequestMapping(value = "/upsertTicketObject", method = RequestMethod.POST)
	public TicketResponseDO upsertTicketObject (String sessionkey, String json, String mode,@RequestParam(value="file",required=false) MultipartFile file,String dbParamJson) {
		System.out.println("inside upsertTicketObject");
		String dbName= null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		int code=0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		MongoDatabase mongodb = null;
		TicketResponseDO ticketResponseDO = null;
		MongoCollection<Document> ticketTable = null;
		MongoDBConnection mongoSingle=null;
		String fileName = null;
		String uploadedFileLocation = null;
		try {
			if(code==0) {
				//save the ticket object
				mongoSingle=new MongoDBConnection(dbParamJson);
				if (dbName != null) {
					mongodb = mongoSingle.getMongoDB(dbName);
				} else {
					mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				}
				ticketTable = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_SUPPORT_TICKET);
				Map<String,String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
				Document document = Document.parse(json);
				String toEmail = linkedUserGroupDetails.get("email_id");
				if(file!=null) {
					fileName = file.getOriginalFilename();
					document.put("fileName", fileName);
				}
				document.put("email_id",toEmail);
				System.out.println("doc..." + document);
				CommonUtility.updateAuditObject(document, linkedUserGroupDetails, mode);
				ticketTable.insertOne(document);
				ObjectId objectId = (ObjectId)document.get("_id");
				String ticketId = "TCK"+objectId.toString();
				String title = (String)document.get("title");
				String severity = (String)document.get("severity");
				String module = (String)document.get("module");
				String description="";
				if(document.get("description")!=null) {
					description = (String)document.get("description");
				}
				TicketController.initializeProperties();
				//System.out.println("title..." + title);
				if(fileName!=null) {
				fileName = ticketId+"-"+fileName;
				//writing the image file to the file location
				uploadedFileLocation = prop.getProperty("TICKET_UPLOAD_LOCATION")+"/"+fileName;
				FileUtility.writeToFile(file.getInputStream(), uploadedFileLocation);
				}
				//Send the notification mail to the User
				String userName = linkedUserGroupDetails.get("userDisplayName");
				String subject = "Support ticket has been created by : "+userName;
				String body = getNotificationMailBody(userName,ticketId,title,severity,module,description);
				boolean flag=false;
				String bcc = prop.getProperty(MongoDBConstants.BCC_EMAIL);
				System.out.println("bcc: "+bcc+", "+prop.getProperty(MongoDBConstants.SES_FLAG_ENABLE));
				if(prop.getProperty(MongoDBConstants.SES_FLAG_ENABLE)!=null && prop.getProperty(MongoDBConstants.SES_FLAG_ENABLE).equalsIgnoreCase("true")) {
					System.out.println("SES");
					SESUtility sesUtil = new SESUtility();
					Properties properties=sesUtil.getProperties();
					properties.setProperty("mail.transport.protocol", "aws");
					properties.setProperty("mail.aws.user", properties.getProperty("aws.sesAccessKey"));
					properties.setProperty("mail.aws.password",  properties.getProperty("aws.sesSecretKey"));
			        Session mailSession = Session.getInstance(properties);
					EmailService emailService = new AWSEmailServiceImpl(sesUtil.createSimpleEmailService());
					if(uploadedFileLocation==null) {
						emailService.withFrom("no-reply@msyte.io")
						.withTo(toEmail)
						.withBcc(bcc)
						.withSubject(subject)
						.withBody(body)
						.withFileName(null)
						.withAttachmentPath(null)
						.sendEmailWithAttachments(mailSession,true);
						 flag = true;
					}else {
						emailService.withFrom("no-reply@msyte.io")
						.withTo(toEmail)
						.withBcc(bcc)
						.withSubject(subject)
						.withBody(body)
						.withFileName(fileName)
						.withAttachmentPath(uploadedFileLocation)
						.sendEmailWithAttachments(mailSession,true);
						flag = true;
					}
				}else {
					System.out.println("SMTP");
					flag=MailUtility.sendNotificationEmail(toEmail,subject,body,uploadedFileLocation,fileName,bcc);
				}
				if(flag) {
					ticketResponseDO = new TicketResponseDO();
					ticketResponseDO.setStatus("Success");
					ticketResponseDO.setStatusCode(code);
					ticketResponseDO.setStatusMessage("The Ticket has been created and the user: "+toEmail+" has been notified with an email");
					ticketResponseDO.setTicketId(ticketId);
				}else {
					ticketResponseDO = new TicketResponseDO();
					ticketResponseDO.setStatus("attachment path"+uploadedFileLocation);
					ticketResponseDO.setStatusCode(5001);
					ticketResponseDO.setStatusMessage("Mail Communication Failure for email id "+toEmail);
					ticketResponseDO.setTicketId("N/A");
					Document searchQuery = new Document("_id",objectId);
					ticketTable.deleteOne(searchQuery);
				}
				//Build the response Object
				
			}else if(code==1001){
				ticketResponseDO = new TicketResponseDO();
				ticketResponseDO.setStatus("Invalid Key");
				ticketResponseDO.setStatusCode(code);
				ticketResponseDO.setStatusMessage("Session Invalid");
			}else if(code==2001) {
				ticketResponseDO = new TicketResponseDO();
				ticketResponseDO.setStatus("MetaData Connection");
				ticketResponseDO.setStatusCode(code);
				ticketResponseDO.setStatusMessage("MetaData Connection Failure");
			}
		}catch(Exception e) {
			e.printStackTrace();
			ticketResponseDO = new TicketResponseDO();
			ticketResponseDO.setStatus("MetaData Connection");
			ticketResponseDO.setStatusCode(2001);
			ticketResponseDO.setStatusMessage("MetaData Connection Failure"+e.getMessage());
			return ticketResponseDO;
		}finally {
			if(mongoSingle!=null){
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, ticketTable);
			}
		}
		return ticketResponseDO;
	}
	/**
	 * 
	 * @param userName
	 * @param ticketId
	 * @return
	 */
	private String getNotificationMailBody(String userName, String ticketId, String title, String severity, String module,String description) {
		StringBuilder sb = new StringBuilder();
		sb.append("The Support ticket has been created with the following information:");
	    sb.append("\n");
	    sb.append("\n");
	    sb.append("Created by: "+userName);
	    sb.append("\n");
	    sb.append("\n");
	    sb.append("Ticket Id: "+ticketId);
	    sb.append("\n");
	    sb.append("\n");
	    sb.append("Title: "+title);
	    sb.append("\n");
	    sb.append("\n");
	    sb.append("Severity: "+severity);
	    sb.append("\n");
	    sb.append("\n");
	    sb.append("Module: "+module);
	    sb.append("\n");
	    sb.append("\n");
	    sb.append("Description: "+description);
	    sb.append("\n");
	    sb.append("\n");
		return sb.toString();
	}
	
	/**
	 * 
	 * @return
	 */
	private static Properties initializeProperties(){
		String file = "/environment.properties";
   		InputStream inputStream = TicketController.class.getResourceAsStream(file); 
   		Reader reader = new InputStreamReader(inputStream);
		try {
			prop.load(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			reader.close();
			inputStream.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return prop;
	}

}
