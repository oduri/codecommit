package com.kavi.services.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.kavi.common.constants.CommonConstants;
import com.kavi.common.utility.CommonUtility;
import com.kavi.common.utility.MessageUtility;
import com.kavi.endpoint.dataobjects.StatusDO;
import com.kavi.endpoint.utility.EndpointCommonUtility;
import com.mongo.db.MongoDBConnection;
import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;

@RestController
public class EndpointController {

	private final static Logger logger = Logger.getLogger(EndpointController.class);
	
	@RequestMapping("/")
	public String welcome() {
		return "Welcome to MicroEndpointService";
	}
	
	/**
	 * 
	 * @param userId
	 * @param userPassword
	 * @param organizationName
	 * @return
	 */
	@RequestMapping(value = "/postMessageResponse", method = RequestMethod.POST)
	public Object postMessageResponse(@RequestHeader(value="Authorization") String authString, @RequestHeader HttpHeaders headers,@RequestBody String str) {
		StatusDO status=new StatusDO();
        if(!EndpointCommonUtility.isUserAuthenticated(authString)){
        	status.setCode(1001);
        	status.setStatus("User not authenticated");
        	return status;
        }
        Document responseDoc=Document.parse(str);
        String messageId=null;
        MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		try {
			Document updateData=new Document();
			 List  headerList=(ArrayList)responseDoc.get("Header");
			 if(headerList!=null && headerList.size()>0){
				for(int iterate=0;iterate<headerList.size();iterate++){
					/*LinkedHashMap<String, String> lMap=(LinkedHashMap<String, String>) headerList.get(iterate);
					messageId=lMap.get("MessageID");*/
					Document doc=(Document)headerList.get(iterate);
					messageId=doc.getString("MessageID");
					break;
				}
			 }
			/*List<Document> headerList=(ArrayList<Document>)responseDoc.get("Header");
			if(headerList!=null && headerList.size()>0){
				for(Document headerDoc:headerList){
					logger.info("headerDoc..."+headerDoc);
					break;
				}
			}*/
			if(messageId==null) {
				messageId=responseDoc.getString("MessageID");
			}
			if(messageId==null){
	        	status.setCode(1001);
	        	status.setStatus("Message Id is Empty");
	        	return status;
			}
			mongoSingle = new MongoDBConnection();
			mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
			table = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_MDSS_ENDPOINT);
			
			updateData.put("response", responseDoc);
			Document object = new Document();
			object.put("response_received_date",new java.util.Date());
			updateData.put("audit", object);
			table.insertOne(updateData);
			/*BasicDBObject command = new BasicDBObject();
			Document updateQuery = new Document();
			updateQuery.put("request.message_id", messageId);
			updateData.put("response", responseDoc);
			command.put("$set", updateData);
			table.updateOne(updateQuery, command);*/
			
			status.setCode(201);
	    	status.setStatus("SUCCESS");
		}catch (Exception e) {
			e.printStackTrace();
			status.setCode(1001);
        	status.setStatus("Error in Database Connection");
			return status;
		} finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
        
    	return status;	    
	}
	
	/**
	 * 
	 * @param sessionkey
	 * @return
	 */
	@RequestMapping(value = "/upsertTwoWayCommunication", method = RequestMethod.POST)
	public Object upsertTwoWayCommunication(@RequestHeader(value="Authorization") String authString,@RequestBody String str) {
		StatusDO status=new StatusDO();
        if(!EndpointCommonUtility.isUserAuthenticatedForTwoWay(authString)){
        	status.setCode(1001);
        	status.setStatus("User not authenticated");
        	return status;
        }
        Document responseDoc=Document.parse(str);
        MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		try {
			 SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS'Z'");
			 dateFormatLocal.setTimeZone(TimeZone.getTimeZone("UTC"));
			 if(responseDoc.get("objectId")==null){
	        	status.setCode(1001);
	        	status.setStatus("Object Id is Empty");
	        	return status;
			}
			mongoSingle = new MongoDBConnection();
			mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
			table = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_TWO_WAY_COMMUNICATION);
			BasicDBObject searchQuery = new BasicDBObject("_id",new ObjectId(responseDoc.getString("objectId")));
			responseDoc.remove("objectId");
			responseDoc.put("status_datetime",dateFormatLocal.format(new Date()));
			table.updateOne(searchQuery, Updates.addToSet("status", responseDoc));
			
			status.setCode(201);
	    	status.setStatus("SUCCESS");
		}catch (Exception e) {
			e.printStackTrace();
			status.setCode(1001);
        	status.setStatus("Error in Database Connection");
			return status;
		} finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
        
    	return status;
	}
	
	/**
	 * 
	 * @param file
	 * @param webServiceAuthenticationKey
	 * @param redirectAttributes
	 * @return
	 */
	
	//@PostMapping("/uploadFile")
	@RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
	public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,String webServiceAuthenticationKey,RedirectAttributes redirectAttributes) {
			int code=0;
			code=CommonUtility.validateWebServiceAuthenticationKey(webServiceAuthenticationKey);
			MongoDBConnection mongoSingle=null;
			MongoDatabase mongodb=null;
			MongoCollection<Document> table=null;
			BasicDBObject fileUploadObject=new BasicDBObject();
			StringBuilder sb=new StringBuilder();
		    sb.append("{");
			sb.append("\"status\":");
			try{
				if(code==0){
				java.util.Date date= new java.util.Date();
				long time = date.getTime();
				AWSCredentials credentials = new BasicAWSCredentials(
						CommonConstants.S3_ACCESS_KEY,CommonConstants.S3_SECRET_KEY);  
				// create a client connection based on credentials
				AmazonS3 s3client = new AmazonS3Client(credentials);
				ObjectMetadata metadata = new ObjectMetadata();
				InputStream uploadedInputStream=null;
				try{
					s3client.putObject(new PutObjectRequest("fhr-messages/data-share", time+"_"+file.getName(),
							file.getInputStream(),metadata));
					fileUploadObject.put("status", "Success");
					fileUploadObject.put("statusCode", 0);
					fileUploadObject.put("statusMessage", "FileUploadSuccess");
					fileUploadObject.put("objectId","fhr-messages/data-share/"+time+"_"+file.getName());
					redirectAttributes.addFlashAttribute(fileUploadObject.toJson());
				}catch(Exception e){
					fileUploadObject.put("status", "Success");
					fileUploadObject.put("statusCode", 0);
					fileUploadObject.put("statusMessage",e.getMessage());
					fileUploadObject.put("objectId","N/A");
				}
				finally{
					try {
						if(uploadedInputStream!=null) {
							uploadedInputStream.close();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			  }
				else if(code==1001){
					 sb.append("\"Invalid key\"");
					 MessageUtility.updateMessage(sb, code, "Session Invalid");
					 return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
				}
				else if(code==2001){
				     sb.append("\"MetaData Connection\"");
					 MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
					 return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
				}
			}catch(Exception e){
				 e.printStackTrace();
				 sb=new StringBuilder();
			     sb.append("{");
				 sb.append("\"status\":");
				 sb.append("\"MetaData Connection\"");
				 MessageUtility.updateMessageWithErrors(sb, 2001, "MetaData Connection Failure",e);
				 return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
			}
			finally{
				if(mongoSingle!=null){
					CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
				}
			}
			return new ResponseEntity<String>(fileUploadObject.toString(),HttpStatus.OK);

		}
	
	@RequestMapping(value = "/downloadFiles", method = RequestMethod.GET)
	public ResponseEntity downloadFiles(HttpServletResponse response,String webServiceAuthenticationKey,String fromDate,String toDate, String deviceId,
			String downloadType){
		int code=0;
		code=CommonUtility.validateWebServiceAuthenticationKey(webServiceAuthenticationKey);
		StringBuilder sb=new StringBuilder();
	    sb.append("{");
		sb.append("\"status\":");
		MongoDBConnection mongoSingle=null;
		MongoDatabase mongodb=null;
		MongoCollection<Document> table=null;
		AggregateIterable<Document> iterable =null;
		try{
			if(code==0){
				 List<String> deviceIdList = Arrays.asList(deviceId.split("\\s*,\\s*"));
				 mongoSingle=new MongoDBConnection();
				 mongodb=mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				 table=mongodb.getCollection(MongoDBConstants.T_STAGING);
				 if(fromDate.indexOf(":")<0){
					 fromDate=fromDate+" 00:00";
				 }
				 if(toDate.indexOf(":")<0){
					 toDate=toDate+" 23:59";
				 }
				 SimpleDateFormat simpleDateFormat = new SimpleDateFormat ("MM/dd/yyyy hh:mm");
				 Date queryFromDate = simpleDateFormat.parse(fromDate);
				 Document searchQuery = new Document();
				 Calendar cal = Calendar.getInstance();
				 cal.setTime(queryFromDate);
				 	
				 int year=cal.get(Calendar.YEAR);
				 int monthNumber=cal.get(Calendar.MONTH);
				 int dateNumber=cal.get(Calendar.DAY_OF_MONTH);
				 monthNumber+=1;
				 
				 Date queryToDate = simpleDateFormat.parse(toDate);
				 cal = Calendar.getInstance();
				 cal.setTime(queryToDate);
				 int toYear=cal.get(Calendar.YEAR);
				 int toMonthNumber=cal.get(Calendar.MONTH);
				 int toDateNumber=cal.get(Calendar.DAY_OF_MONTH);
				 toMonthNumber+=1;
				 int startHourTime=0;
				 int startMinuteTime=0;
				 int endHourTime=0;
				 int endMinuteTime=0;
				 if(fromDate.indexOf(":")>0){
					 fromDate=fromDate.substring(10,fromDate.length());
					 fromDate=fromDate.trim();
					 String[] split=fromDate.split(":");
					 startHourTime=Integer.parseInt(split[0]==null?"0":split[0]);
					 startMinuteTime=Integer.parseInt(split[1]==null?"0":split[1]);
				 }
				 if(toDate.indexOf(":")>0){
					 toDate=toDate.substring(10,toDate.length());
					 toDate=toDate.trim();
					 String[] split=toDate.split(":");
					 endHourTime=Integer.parseInt(split[0]==null?"0":split[0]);
					 endMinuteTime=Integer.parseInt(split[1]==null?"0":split[1]); 
				 }
				 searchQuery.put("header.temperature_date",new BasicDBObject("$gte", 
						 new DateTime(year, monthNumber, dateNumber, startHourTime, startMinuteTime,DateTimeZone.UTC).toDate()).
						 append("$lte",new DateTime(toYear, toMonthNumber, toDateNumber, endHourTime, endMinuteTime,DateTimeZone.UTC).toDate()));
				 
				 BasicDBObject inQuery = new BasicDBObject();
				 inQuery.put("$in", deviceIdList);
				 searchQuery.put("header."+MongoDBConstants.DEVICE_ID, inQuery);
				 Document matchQuery = new Document();
				 matchQuery.put("$match", searchQuery);
				 BasicDBObject sortQuery=new BasicDBObject();
				 sortQuery.put("header."+MongoDBConstants.DEVICE_ID, MongoDBConstants.MIN_ORDER_BY);
				 sortQuery.put("header.measurementId", MongoDBConstants.MIN_ORDER_BY);
				 sortQuery.put("header.temperature_date", MongoDBConstants.MIN_ORDER_BY);
				 BasicDBObject sortQueryObject=new BasicDBObject("$sort",sortQuery);
				 
				 Document projectQuery = new Document();
				 Document project = new Document();
			     project.putAll(CommonUtility.getSpecificDetailsForDSSReport());
			     projectQuery.put("$project", project);
			    
			     StringBuffer csv=new StringBuffer();
				 csv.append("DeviceId");
				 csv.append(",");
				 csv.append("MeasureId");
				 csv.append(",");
				 csv.append("DateTime");
				 csv.append(",");
				 csv.append("FileName");
				 csv.append(",");
				 for(int iterate=1;iterate<=10;iterate++){
					csv.append("C"+iterate);
					csv.append(",");	
				 }
				csv.append("\n");
				DateFormat df = CommonUtility.getDateFormat("monthnew");
				iterable = table.aggregate(Arrays.asList(matchQuery,sortQueryObject,projectQuery));
				for (Document row : iterable) {
					 Document header=(Document)row.get("header");
					 csv.append(header.get("device_id"));
					 csv.append(",");
					 csv.append(header.get("measurementId"));
					 csv.append(",");
					 csv.append(df.format(header.getDate("temperature_date")));
					 csv.append(",");
					 csv.append(header.get("bucketKey"));
					 csv.append(",");
					 Document data=(Document)row.get("data");
					 if(data.get("0x05") instanceof List){
						 List<Document> list=(ArrayList<Document>)data.get("0x05");
						 if(list!=null && list.size()>0){
						 	 for(int iterate=0;iterate<list.size();iterate++){
						 		 csv.append(list.get(iterate));
						 		 csv.append(",");
						 	 }
						 }
					 }
					 csv.append("\n");
				 }
				if(downloadType.equalsIgnoreCase("data")){
					return new ResponseEntity<String>(csv.toString(), HttpStatus.OK);
				}else if(downloadType.equalsIgnoreCase("file")){
					FileOutputStream fos =new FileOutputStream("download.csv");
					fos.write(csv.toString().getBytes());
					fos.close();
					File file = new File("download.csv");
					response.setContentType("application/octet_stream");
			        response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() +"\""));
			        response.setContentLength((int)file.length());
			        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
			        FileCopyUtils.copy(inputStream, response.getOutputStream());
			        inputStream.close();
					
				}
			}else if(code==1001){
				sb.append("\"Invalid webServiceAuthenticationKey\"");
				MessageUtility.updateMessage(sb, code, "Session Invalid");
			}
			else if(code==2001){
			    sb.append("\"MetaData Connection\"");
				MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
			}
		}catch(Exception e){
			sb=new StringBuilder();
			sb.append("{");
			sb.append("\"status\":");
			sb.append("\"Error in Communicating WebService\"");
			MessageUtility.updateMessageWithErrors(sb, 9001, "Error in Communicating WebService",e);
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		}
		finally{
			if(mongoSingle!=null){
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
		return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
   }
	
	
	/**
	 * 
	 * @param req
	 * @param formParams
	 * @return
	 */
	@RequestMapping(value = "/testMemoryLeak", method = RequestMethod.POST)
	public ResponseEntity<String> testMemoryLeak() {
		 return new ResponseEntity<String>("welcome", HttpStatus.OK);
	
	}
	
	/**
	 * 
	 * @param req
	 * @param formParams
	 * @return
	 */
	@RequestMapping(value = "/testMemoryLeakInLinkedHashMap", method = RequestMethod.POST)
	public ResponseEntity<String> testMemoryLeakInLinkedHashMap() {
		LinkedHashMap<String,String> lMap=new LinkedHashMap<String,String>();
		lMap.put("welcome", "welcome");
		lMap.put("welcome1", "welcome");
		lMap.put("welcome2", "welcome");
		lMap.put("welcome3", "welcome");
		
		
		return new ResponseEntity<String>("welcomemap", HttpStatus.OK);
	
  }
	
}
