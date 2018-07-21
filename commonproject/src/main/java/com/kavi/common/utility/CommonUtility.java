package com.kavi.common.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.WeakHashMap;
import java.util.regex.Pattern;


import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mongo.db.MongoDBConnection;
import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;


public class CommonUtility {
	 
	private final static Logger logger = Logger.getLogger(CommonUtility.class);
	
	private static final Random RANDOM = new SecureRandom();
	
	public static final int PASSWORD_LENGTH = 8;
	
	private static boolean siteEditFlag=false;
	
	private static JsonParser parser=new JsonParser();
	
	
	private static Map<String,String> lProp=new WeakHashMap<String,String>();
	
	/**
	 * 
	 */
	public static Map<String,String> initializeEnviromentProperties(){
		if(lProp.get("image_path")==null) {
			Properties prop=new Properties();
			String file = "/environment.properties";
	   		InputStream inputStream = LDAPUtility.class.getResourceAsStream(file); 
	   		Reader reader = new InputStreamReader(inputStream);
			try {
				prop.load(reader);
				lProp.put("image_path", prop.getProperty("IMAGE_PATH"));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				reader.close();
				inputStream.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return lProp;
	}
	/**
	 * 
	 * @param obj
	 * @return
	 */
	public static String replaceNullWithEmptyString(Object obj){
		return obj==null?"":(String) obj;
	}
	
	public static boolean validateData(String param){
		if(param==null || "".equalsIgnoreCase(param))
			return false;
		else
			return true;
	}
   
	/**
	 * 
	 * @param mongoSingle
	 * @param db
	 * @param table
	 */
	public static void closeMongoConnection(MongoDBConnection mongoSingle,MongoDatabase db,MongoCollection<Document> table){
		db=null;
		table=null;
		mongoSingle.close();
	}
	
	public static void closeConnection(MongoDBConnection mongoSingle,DB db, DBCollection table){
			db=null;
			table=null;
			mongoSingle.close();
	}
	
	public static void closeConnection(MongoDBConnection mongoSingle,DB db){
		db=null;
		mongoSingle.close();
	}

	
	
 	
   public static String convertDate(Date dDate){
	  SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/YYYY"); //For whole month plz give MMM
	  if(dDate!=null){
		  String date = sdf.format(dDate);
		  return date;
	  }
	    return "";
	  
   }
   
   public static String convertDateWithTimeStamp(Date dDate){
	      SimpleDateFormat simpleDateFormat =new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		  if(dDate!=null){
			  String dateStr = simpleDateFormat.format(dDate);
			  return dateStr;
		  }
		    return "";
  }
   
   public static String convertDateTimeStamp(Date dDate){
	      SimpleDateFormat simpleDateFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm");
		  if(dDate!=null){
			  String dateStr = simpleDateFormat.format(dDate);
			  return dateStr;
		  }
		    return "";
   }
   /**
    * 
    * @param json
    * @param key
    * @return
    */
   public static void getSavedDate(String json,String key,DBObject auditObject,DBObject document){
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	try {
			JSONObject dateObj = parser.parse(json);
			dateObj=(JSONObject) dateObj.get(key);
			if(dateObj!=null){
				String savedDate=(String)dateObj.get("$date");
				savedDate=savedDate.replaceAll("T", " ");
				document.put(key, savedDate);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   }
   
   /**
    * 
    * @return
    */
	public static Object getDateAsObject(){
		
		DateTimeZone timeZoneCurrent = DateTimeZone.forID(TimeZone.getDefault().getID());
		DateTime nowZone = DateTime.now( timeZoneCurrent );
		
		DateTimeZone timeZoneChicago = DateTimeZone.forID( MongoDBConstants.TIME_ZONE );
		DateTime nowConvertZone = nowZone.withZone( timeZoneChicago);
		
		StringBuffer sb=new StringBuffer();
	 	sb.append("{");
	    sb.append("\"Date\": ");
	    sb.append("{");
	    //sb.append("{$date:");
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	    String format = formatter.format(nowConvertZone.toLocalDateTime().toDate());
	    sb.append("\"$date\": ");   
	    sb.append("\"");
	    sb.append(format+"Z");
	    sb.append("\"");
	    sb.append("}");
	    sb.append("}");
	    BasicDBObject jsonTimeObject = (BasicDBObject)JSON.parse(sb.toString());
	    return jsonTimeObject.get("Date");
	}
	
	/**
	 * 
	 * @return
	 */
	public static Object getDateAsDate(){
			DateTimeZone timeZoneCurrent = DateTimeZone.forID(TimeZone.getDefault().getID());
			DateTime nowZone = DateTime.now( timeZoneCurrent );
			DateTimeZone timeZoneChicago = DateTimeZone.forID( MongoDBConstants.TIME_ZONE );
			DateTime nowConvertZone = nowZone.withZone( timeZoneChicago);
			StringBuffer sb=new StringBuffer();
		 	sb.append("{");
		    sb.append("\"Date\": ");
		    sb.append("{");
		    //sb.append("{$date:");
		    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		    String format = formatter.format(nowConvertZone.toLocalDateTime().toDate());
		    sb.append("\"$date\": ");   
		    sb.append("\"");
		    sb.append(format+"Z");
		    sb.append("\"");
		    sb.append("}");
		    sb.append("}");
		    JSONObject dateObj=new JSONObject ();
		    try{
		    	dateObj=parser.parse(sb.toString());
		    	dateObj=parser.extractObjectFromJSON(dateObj, "Date");
		    	String dateTime=(String)dateObj.get("$date");
		    	return dateObj.get("$date");
		    	//BasicBSONObject a=new BasicBSONObject();
		    	//a.p
		    }catch (Exception e){
		     e.printStackTrace();
		    }
		    BasicDBObject jsonTimeObject = (BasicDBObject)JSON.parse(sb.toString());
			return jsonTimeObject.get("Date");
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getDateInStringFormat(){
			DateTimeZone timeZoneCurrent = DateTimeZone.forID(TimeZone.getDefault().getID());
			DateTime nowZone = DateTime.now( timeZoneCurrent );
			DateTimeZone timeZoneChicago = DateTimeZone.forID( MongoDBConstants.TIME_ZONE );
			DateTime nowConvertZone = nowZone.withZone( timeZoneChicago);
			StringBuffer sb=new StringBuffer();
		 	sb.append("{");
		    sb.append("\"Date\": ");
		    sb.append("{");
		    //sb.append("{$date:");
		    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		    String format = formatter.format(nowConvertZone.toLocalDateTime().toDate());
		    sb.append("\"$date\": ");   
		    sb.append("\"");
		    sb.append(format+"Z");
		    sb.append("\"");
		    sb.append("}");
		    sb.append("}");
		    JSONObject dateObj=new JSONObject ();
		    try{
		    	dateObj=parser.parse(sb.toString());
		    	dateObj=parser.extractObjectFromJSON(dateObj, "Date");
		    	String dateTime=(String)dateObj.get("$date");
		    	dateTime=dateTime.replaceAll("T", " ");
		    	return dateTime;
		    	//BasicBSONObject a=new BasicBSONObject();
		    	//a.p
		    }catch (Exception e){
		     e.printStackTrace();
		    }
		    
		  return "";
	    	
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getDateAsString(){
			DateTimeZone timeZoneCurrent = DateTimeZone.forID(TimeZone.getDefault().getID());
			DateTime nowZone = DateTime.now( timeZoneCurrent );
			DateTimeZone timeZoneChicago = DateTimeZone.forID( MongoDBConstants.TIME_ZONE );
			DateTime nowConvertZone = nowZone.withZone( timeZoneChicago);
		    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:sss");
		    String format = formatter.format(nowConvertZone.toLocalDateTime().toDate());
		    return format;
	    	
	}
	
	/**
	 * 
	 * @param mongodb
	 * @param operations
	 * @param key
	 * @param activityName
	 * @param description
	 */
	public static void maintainSessionActivities(MongoDatabase mongodb,String operations,String sessionkey,String activityName,String description){
		MongoCollection<Document> sessionDetails=null;
		AggregateIterable<Document> iterable = null;
		try{
			sessionDetails=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_SESSION_INFO);
			Document matchQuery = new Document();
			Document searchQuery=new Document("sessionkey",sessionkey);
			matchQuery.put("$match", searchQuery);
			iterable = sessionDetails.aggregate(Arrays.asList(matchQuery));
			//FindIterable<Document> sessioncursor = sessionDetails.find(searchQuery);
			List activitiesList=null;
			JSONArray array=new JSONArray();
			//if(sessioncursor.iterator().hasNext()){
				 //Document object=(Document)sessioncursor.iterator().next();
			for (Document row : iterable) {	
				 activitiesList = (ArrayList) row.get("activity_details");
			 	 BasicDBObject updateData = new BasicDBObject();
			 	 BasicDBObject operationData = new BasicDBObject();
				 if(activitiesList!=null && activityName.equalsIgnoreCase("activityStart")){
					 operationData.put("activity_name",operations);
					 operationData.put("activity_start_time",CommonUtility.getDateAsObject());
					 operationData.put("activity_end_time","");
					 operationData.put("activity_description",description);
					 activitiesList.add(operationData);
					 updateData.put("activity_details", activitiesList);
				 }
				 else if(activitiesList==null && activityName.equalsIgnoreCase("activityStart")){
					operationData.put("activity_name",operations);
					operationData.put("activity_start_time",CommonUtility.getDateAsObject());
					operationData.put("activity_end_time","");
					operationData.put("activity_description",description);
					array.add(operationData);	
					updateData.put("activity_details", array);
				 }
				 else if(activitiesList!=null && activityName.equalsIgnoreCase("activityEnd")){
					 searchQuery.put("activity_details.activity_end_time","" );
					 updateData.put("activity_details.$.activity_end_time",CommonUtility.getDateAsObject());
				}
				BasicDBObject command = new BasicDBObject();
				command.put("$set", updateData);
				sessionDetails.updateOne(searchQuery, command);
			 }
			
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			sessionDetails=null;
	  }
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public static int validateAuthenticationKey(String key) {
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		int code = 0;
		try {
			mongoSingle = new MongoDBConnection();
			mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
			boolean flag=false;
			if (ObjectId.isValid(key)) {
				flag=true;
				table = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_SESSION_INFO);
				Document searchQuery = new Document("_id", new ObjectId(key));
				searchQuery.put("end_time", null);
				FindIterable<Document> sessioncursor = table.find(searchQuery);
				if (sessioncursor.iterator().hasNext()) {
					code = 0;
				} else {
					code = 1001;
				}
			}
			if(flag==false) {
				table = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_SESSION_INFO);
				Document searchQuery = new Document();
				searchQuery.put("sessionkey", key);
				FindIterable<Document> sessioncursor = table.find(searchQuery);
				if (sessioncursor.iterator().hasNext()) {
					code = 0;
				} else {
					code = 1001;
				}
			}
			
		} catch (Exception e) {
			code = 2001;
		} finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, null);
			}
		}
		return code;
	}
	
	public static int validateAuthenticationJson(String sessionJson) {
		// Sample dbParamjson Structure
		/*
		 * { "sessionkey": "xxxxxxxx", "db_name": "FHR_DSS" }
		 */
		BasicDBObject sessionJsonDoc = null;
		if(sessionJson!=null) {
			sessionJsonDoc = (BasicDBObject) JSON.parse(sessionJson);
			return validateAuthenticationKey(sessionJsonDoc.getString("sessionkey"),sessionJsonDoc.getString("db_name"));
		}
		return 1001;
	}
	
	/**
	 * @param key
	 * @param dbName
	 * @return
	 */
	public static int validateAuthenticationKey(String key, String dbParamJson) {
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		int code = 0;
		String dbName = null;
		 BasicDBObject dbParamJsonDoc = null;
		try {
			if(dbParamJson!=null && dbParamJson.length()>0) {
				 dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
				 dbName = dbParamJsonDoc.getString("db_name");
			 }
			mongoSingle = new MongoDBConnection(dbParamJson);
			mongodb = mongoSingle.getMongoDB(dbName);
			code = authenticateKey(key, mongodb);
		} catch (Exception e) {
			code = 2001;
		} finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, null);
			}
		}
		return code;
	}
	
	/**
	 * @param key
	 * @param mongodb
	 * @return
	 */
	private static int authenticateKey(String key, MongoDatabase mongodb) {
		MongoCollection<Document> table = null;
		Document matchQuery = new Document();
		AggregateIterable<Document> iterable = null;
		int code = 0;
		try {
			if (key!=null) {
				table = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_SESSION_INFO);
				Document searchQuery = new Document("sessionkey", key);
				matchQuery.put("$match", searchQuery);
				searchQuery.put("end_time", null);
				iterable = table.aggregate(Arrays.asList(matchQuery));
				if (iterable!=null && iterable.first()!=null) {
					code = 0;
				} else {
					code = 1001;
				}
			} else {
				code = 1001;
			}
		} catch (Exception e) {
			code = 2001;
			return code;
		} finally {
			table = null;
		}
		return code;
	}
	
	 
	/**
	 *  
	 * @param str
	 * @return
	 */
    public static String applyCaseInsensitiveSearchForString(String str){
    	str="^"+str+"$";
    	return str;
    
    }
    /**
     * 
     * @param db
     * @param key
     * @return
     */
    public static Map<String,String> getUserAndGroupDetails(MongoDatabase mongodb,String sessionkey){
		Map<String,String>  linkedUserGroupDetails =new HashMap<String,String> ();
		Document searchQuery = null;
		MongoCollection<Document> table=null;
		try {
			if (ObjectId.isValid(sessionkey)) {
				searchQuery=new Document("_id", new ObjectId(sessionkey));
			}else {
				searchQuery=new Document("sessionkey", sessionkey);
			}
		}catch(Exception e) {
			e.printStackTrace();
			searchQuery=new Document("sessionkey", sessionkey);
		}
		
		try{
				table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_SESSION_INFO);
				FindIterable<Document> sessioncursor = table.find(searchQuery);
				if(sessioncursor.iterator().hasNext()){
					//DBObject object=sessioncursor.next();
					Document object=(Document)sessioncursor.iterator().next();
					linkedUserGroupDetails.put("userId", object.get("userId").toString());
					if(object.get("organizationName")!=null){
						linkedUserGroupDetails.put("organizationName",object.get("organizationName").toString());
					}
					if(object.get("userDisplayName")!=null){
						linkedUserGroupDetails.put("userDisplayName", object.get("userDisplayName").toString());
					}
					else{
						linkedUserGroupDetails.put("userDisplayName", object.get("userId").toString());
					}
					if(object.get("role")!=null){
						String roleName=object.get("role").toString();
						roleName=roleName.replace("[", "");
						roleName=roleName.replace("]", "");
						linkedUserGroupDetails.put("roleName", roleName);
						roleName=roleName.replace("\"", "");
						linkedUserGroupDetails.put("role", roleName);
						
					}
					linkedUserGroupDetails.put("email_id", object.get("email_id").toString());
				}
				
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			table=null;
			
	  }
		return linkedUserGroupDetails;
	}
      
    /**
     * 
     * @return
     */
    public static DateFormat getDateFormat(String formatType){
    	TimeZone tz = TimeZone.getTimeZone("UTC");
    	DateFormat df =null;
    	if(formatType.equalsIgnoreCase("month")){
    		df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    	}
    	else if(formatType.equalsIgnoreCase("monthnew")){
    		df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    	}
    	else{
    		df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");	
    	}
    	
    	df.setTimeZone(tz);
    	return df;
    }
    
    
    
    /**
	 * 
	 * @return
	 */
	public static String generateRandomPassword(){
	      String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
	      String pw = "";
	      for (int i=0; i<PASSWORD_LENGTH; i++){
	          int index = (int)(RANDOM.nextDouble()*letters.length());
	          pw += letters.substring(index, index+1);
	      }
	      return pw;
	  }
	
	
    /**
     * 
     * @param document
     * @param type
     * @return
     */
    /*public static Document getSpecificDetailsForMeasurementParameters(BasicDBObject document,String type){
     	Document selectedFields = new Document();
     	List<String> param = new ArrayList<String>();
     	if(document!=null && document.getString("deviceCategoryName").equalsIgnoreCase("Vibration")){
     		selectedFields = new Document("_id",1);
     		selectedFields.put("message_time",1);
	     	if(document.get("type") != null && ((String)document.get("type")).equalsIgnoreCase("All")){
	     		selectedFields.put("measurement_setup",1);
	     		selectedFields.put("configuration",1);
	     		selectedFields.put("status",1);
	     		selectedFields.put("pp",1);
	     		selectedFields.put("rms",1);
	     		selectedFields.put("pp_accel",1);
	     		selectedFields.put("pp_alert",1);
	     		selectedFields.put("pp_alarm",1);
	     		selectedFields.put("rms_accel",1);
	     		selectedFields.put("rms_alert",1);
	     		selectedFields.put("rms_alarm",1);
	     	}
	     	else if (document.get("type") != null){
	     		selectedFields.put(document.getString("type"),1);
	     	}
     	}
     	else if(document!=null && document.getString("deviceCategoryName").equalsIgnoreCase("Corrosion")){
     		selectedFields = new Document("_id",1);
     		selectedFields.put("message_time",1);
     		if(document.get("type") != null && ((String)document.get("type")).equalsIgnoreCase("All")){
         		selectedFields.put("device_config",1);
         		selectedFields.put("measure_config",1);
         		selectedFields.put("measurement",1);
	     	}
	     	else if (document.get("type") != null) {
	     		selectedFields.put(document.getString("type"),1);
	     	}
     	}
 		//return selectedFields;
     	return selectedFields;
     }	*/
  
    public static List<String> getSpecificDetailsForMeasurementParameters(BasicDBObject document,String type){
     	Document selectedFields = new Document();
     	List<String> param = new ArrayList<String>();
     	if(document.isEmpty() && type.equalsIgnoreCase("Vibration")){
     		param.add("device_status");
     		param.add("measurement_settings");
     		param.add("measurement_data");
     		param.add("user_parameters");	
     	}
     	else if(document.isEmpty() && type.equalsIgnoreCase("Corrosion")){
     		param.add("device_status");
     		param.add("measurement_settings");
     		param.add("measurement_data");
     		param.add("db_parameters");	
     	}
     	else if(document!=null && document.getString("deviceCategoryName").equalsIgnoreCase("Vibration")){
	     	if(document.get("type") != null && ((String)document.get("type")).equalsIgnoreCase("All")){
	     		param.add("device_status");
	     		param.add("measurement_settings");
	     		param.add("measurement_data");
	     		param.add("user_parameters");
	     	}
	     	else if (document.get("type") != null){	
	     		if(type.equalsIgnoreCase("measurement_settings")  ) {
		     		param.add("measurement_settings");
		     		param.add("user_parameters");
	     		}
	     		else {
	     			param.add(document.getString("type"));
	     		}
	     	}
     	}
     	else if(document!=null && document.getString("deviceCategoryName").equalsIgnoreCase("Corrosion")){
     		selectedFields = new Document("_id",1);
     		selectedFields.put("message_time",1);
     		if(document.get("type") != null && ((String)document.get("type")).equalsIgnoreCase("All")){
         		param.add("device_status");
	     		param.add("measurement_settings");
	     		param.add("measurement_data");
	     		param.add("db_parameters");
	     	}
	     	else if (document.get("type") != null) {
	     		if(type.equalsIgnoreCase("measurement_settings")  ) {
		     		param.add("measurement_settings");
		     		param.add("db_parameters");
	     		}
	     		else {
	     			param.add(document.getString("type"));
	     		}
	     	}
     	}
     	return param;
     }	
    
     /**
 	 * 
 	 * @param key
 	 * @return
 	 */
 	public static int validateWebServiceAuthenticationKey(String webServiceAuthenticationKey){
 		MongoDBConnection mongoSingle=null;
 		MongoDatabase mongodb=null;
 		MongoCollection<Document> table=null;
 		int code=0;
 		try{
			mongoSingle=new MongoDBConnection();
			mongodb=mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
			table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_WEB_SERVICE_AUTHENTICATION);
			Document searchQuery = new Document();
			searchQuery.put("key", webServiceAuthenticationKey);
			FindIterable<Document> sessioncursor = table.find(searchQuery);
			if(sessioncursor.iterator().hasNext()){
				code=0;
			}else{
				code=1001;
			}
 			
 		}catch(Exception e){
 			e.printStackTrace();
 			code=2001;
 		  }
 		finally{
 			if(mongoSingle!=null){
 				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
 			}
 	   }
 		return code;
 	}
 	
 	/**
 	 * 
 	 * @param fromDate
 	 * @param toDate
 	 * @param lastModifiedDate
 	 * @return
 	 */
 	public static boolean isDateInBetweenIncludingEndPoints(String fromDate,String toDate,Date lastModifiedDate){
 		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        try {
             Date min = formatter.parse(fromDate);
             Date max = formatter.parse(toDate);
             Calendar c = Calendar.getInstance();
             c.setTime(max);
             c.add(Calendar.DATE, 1);
             return lastModifiedDate.compareTo(min) >= 0 && lastModifiedDate.compareTo(c.getTime()) <=0;
        }catch(Exception e){
        	e.printStackTrace();	
        }
 		
 		return false;
 	}
 	/**
 	 * 
 	 */
 	public static Document getSpecificDetailsForDSSReport(){
 		Document selectedFields = new Document("_id",0);
 		selectedFields.put("header", 1);
 		selectedFields.put("data.0x05", 1);
 		return selectedFields;
 	}	
 	
/**
 * 
 * @param document
 * @param type
 * @return
 */
public static Document getSpecificDetailsForCompareDeviceParameters(BasicDBObject document){
 	Document selectedFields = new Document();
 	
 	if(document!=null && document.getString("deviceCategoryName").equalsIgnoreCase("Vibration")){
 		selectedFields = new Document();
 	  /*selectedFields.put("measurement_setup",1);
     	selectedFields.put("configuration",1);
     	selectedFields.put("status",1);*/
 		selectedFields.put("device_status",1);
 		selectedFields.put("measurement_data",1);
 		selectedFields.put("measurement_settings",1);
 		selectedFields.put("user_parameters",1);
 	}
 	else if(document!=null && document.getString("deviceCategoryName").equalsIgnoreCase("Corrosion")){
 		selectedFields = new Document();
 		selectedFields.put("device_status",1);
 		selectedFields.put("measurement_data",1);
 		selectedFields.put("measurement_settings",1);
 		selectedFields.put("db_parameters",1);
 	}
		return selectedFields;
 }	

public static Document updateAuditObject(Document document,Map<String, String> linkedUserGroupDetails,String type){
	 
	 if(type.equalsIgnoreCase("update") || type.equalsIgnoreCase("delete")){
		 document.put("audit.modified_by",linkedUserGroupDetails.get("userId"));
		 document.put("audit.modified_date",new java.util.Date());
	 }

	 else{
		 Document object = new Document();
		 object.put("created_by", linkedUserGroupDetails.get("userId"));
		 object.put("created_date",new java.util.Date());
		 object.put("modified_by",linkedUserGroupDetails.get("userId"));
		 object.put("modified_date",new java.util.Date());
		 document.put("audit", object);
	 }
	 return document;
 }

/***
 * 
 * @param groupName
 * @return
 */
 public static String generateNotificationGroupId(String groupName){
      String letters = ""+System.currentTimeMillis();
      String randomId = "";
      for (int i=0; i<7; i++){
          int index = (int)(RANDOM.nextDouble()*letters.length());
          randomId += letters.substring(index, index+1);
      }
      groupName=groupName.toLowerCase();
      groupName=groupName.replace(" ","_");
      return groupName+"_"+randomId;
  }

	/**
	 * 
	 * @param message
	 * @return
	 */
	public static String getId(String message) {
		String id = new String();
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.reset();
			m.update(message.getBytes());
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			id = bigInt.toString(16);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}
	
	public static Document getSpecificDetailsForTakeMeasurement(String type){
		Document selectedFields = new Document();
		if(type.equalsIgnoreCase("take_measurement")){
			selectedFields.put("device_id",1);
			selectedFields.put("db_parameters.ip_address",1);
	 		selectedFields.put("db_parameters.ip_port",1);
		}
		return selectedFields;
	}
	
	/**
	 * 
	 * @param mongodb
	 * @param sessionkey
	 * @param organizationName
	 */
	public static void updateOrganizationName(MongoDatabase mongodb,String sessionkey,String organizationName){
		   Document searchQuery=new Document("_id",new ObjectId(sessionkey));
		   Document object=new Document();
		   object.put("organizationName",organizationName);
		   BasicDBObject command = new BasicDBObject();
		   command.put("$set", object);
		   MongoCollection<Document> table =mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_SESSION_INFO);
		   table.updateOne(searchQuery,command);
	}
	
	/**
	 * 
	 * @return
	 */
	public static String generateRandomId(){
	      String letters = ""+System.currentTimeMillis();
	      String randomId = "";
	      for (int i=0; i<7; i++){
	          int index = (int)(RANDOM.nextDouble()*letters.length());
	          randomId += letters.substring(index, index+1);
	      }
	      return randomId;
	}
	
	/**
	 * 
	 * @param userId
	 * @param table
	 * @param organizationName
	 * @return
	 */
	public static Document getLocationTreeHierarchyForUser(String userId,MongoCollection<Document> table,String organizationName,MongoDatabase mongodb){
		 Map<String, Integer> llatestDeviceStatus=new HashMap<String, Integer>();
		 Document searchQuery = new Document();
		 searchQuery.put("$search", Pattern.compile(CommonUtility.applyCaseInsensitiveSearchForString(userId), Pattern.CASE_INSENSITIVE).toString());
		 Document textQuery = new Document();
		 textQuery.put("company.id", organizationName);
		 textQuery.put("version_control.active_flag",true);
		 textQuery.put("$text", searchQuery);
		 FindIterable<Document> sessioncursor = table.find(textQuery);
		 JSONArray plantArray=new JSONArray();
		 JSONArray siteArray=new JSONArray();
		 JSONArray unitArray=new JSONArray();
		 JSONArray assetArray=new JSONArray();
		 JSONArray deviceCategoryArray=new JSONArray();
		 JSONArray deviceArray=new JSONArray();
		 MongoCursor<Document> itr=sessioncursor.iterator();
		 Document object=null;
		 boolean plantFlag=false;
		 boolean siteFlag=false;
		 boolean unitFlag=false;
		 boolean companyFlag=false;
		 boolean assetFlag=false;
		 boolean deviceCategoryFlag=false;
		 boolean deviceFlag=false;
		 Document companyObject=new Document();
		 Document companyIdObject=new Document();
		 siteEditFlag=false;
		 while(itr.hasNext()){
			 object=(Document)itr.next();
			 companyObject=(Document)object.get("company");
			 if(companyObject!=null){
				 llatestDeviceStatus=getLatestDeviceStatus(mongodb, companyObject.getString("id")) ;
			 }
			 if(companyObject!=null){
				  plantArray=new JSONArray();
				  if(isUserExistsInDeny(userId, companyObject)==false){
					 companyObject.remove("user");
					 List<Document> plantObjectList  =(ArrayList)companyObject.get("plant");
					 if(plantObjectList!=null && plantObjectList.size()>0){
					 for(int plantIterate=0;plantIterate<plantObjectList.size();plantIterate++){
						 Document plantObject=(Document)plantObjectList.get(plantIterate);
						 Document plantUserObject=(Document)plantObject.get("user");
						 if(isUserExistsInGrant(userId, plantUserObject)){
							 plantFlag=true;
						 }
						 //plantObject.remove("user");
						 if(isUserExistsInDeny(userId, plantUserObject)==false  && plantObject!=null && plantObject.get("site")!=null){
							  // Site
							  List<Document> siteObjectList=(ArrayList)plantObject.get("site");
							  siteArray=new JSONArray();
							  siteEditFlag=false;
							  if(siteObjectList!=null && siteObjectList.size()>0){
							  for(int siteIterate=0;siteIterate<siteObjectList.size();siteIterate++){
									 Document siteObject=(Document)siteObjectList.get(siteIterate);
									 Document siteUserObject=(Document)siteObject.get("user");
									 if(isUserExistsInGrant(userId, siteUserObject)){
										 siteFlag=true;
									 }
									 if(isUserRoleExistsInGrant(userId, siteUserObject,"siteadmin")){
								        	siteEditFlag=true;
								     }
									 //siteObject.remove("user");
									 // Unit
									 if(isUserExistsInDeny(userId, siteUserObject)==false  && siteObject!=null && siteObject.get("unit")!=null){
										 List<Document> unitObjectList=(ArrayList)siteObject.get("unit");
										 unitArray=new JSONArray();
										 if(unitObjectList!=null && unitObjectList.size()>0){
											  for(int unitIterate=0;unitIterate<unitObjectList.size();unitIterate++){
												  Document unitObject=(Document)unitObjectList.get(unitIterate);
												  Document unitUserObject=(Document)unitObject.get("user");
												  if(isUserExistsInGrant(userId, unitUserObject) || siteFlag || plantFlag ){
														 unitFlag=true;
												   }
												   //unitObject.remove("user");
												   //Asset Starts
												   if(isUserExistsInDeny(userId, unitUserObject)==false  && unitObject!=null && 
														   unitObject.get("asset")!=null){
													   List<Document> assetObjectList=(ArrayList)unitObject.get("asset");
													   assetArray=new JSONArray();
													   if(assetObjectList!=null && assetObjectList.size()>0){
														  for(int assetIterate=0;assetIterate<assetObjectList.size();assetIterate++){
															  Document assetObject=(Document)assetObjectList.get(assetIterate);
															  Document assetUserObject=(Document)assetObject.get("user");
															  if(isUserExistsInGrant(userId, assetUserObject) || siteFlag || plantFlag || unitFlag ){
																	 assetFlag=true;
																	 assetObject.remove("user");
																	 assetArray.add(assetObject);
															   }
															  //assetObject.remove("user");
															  assetFlag=false;
														  }//end of for asset
													   }//end of if asset
													   if(assetArray.size()>0){
														   unitObject.put("asset", assetArray);
														   unitArray.add(unitObject);
													   }
												   }
												   //Asset Ends
												   unitFlag=false;
											  }//end of for unit
										 }//end of if unit
										 siteObject.put("unit", unitArray);
										 if(siteFlag || plantFlag ||assetArray.size()>0 ){
											 siteArray.add(siteObject);
										 }
									 }//end of if unit check
									 siteFlag=false;
							    }//end of for site 
							    if(siteArray.size()>0 || unitArray.size()>0 || assetArray.size()>0){
							       plantObject.put("edit_admin_flag", siteEditFlag);
							       plantArray.add(plantObject);
							    }
							  plantObject.put("site", siteArray);
						   }//end of if site null check
						 }
						  plantFlag=false;
					 }//end of for plant
					 companyObject.put("plant", plantArray);
					
				   }//end of if plant null check
				 }
			 }
		}//end of while
		if(companyObject.get("id")!=null){ 
			companyIdObject.put("company", companyObject);
			companyIdObject=traverseObject(companyIdObject,userId,llatestDeviceStatus,true);
			//companyIdObject.put("edit_admin_flag",siteEditFlag);
		}
		itr.close();
		return companyIdObject;
	  }
	
	
	 /**
     * 
     * @return
     */
    public static Map<String,Integer>  getLatestDeviceStatus(MongoDatabase mongodb,String companyId){
    	
    	Map<String,Integer>  latestDeviceStatus =new HashMap<String,Integer> ();
    	MongoCollection<Document> table=null;
		try{
//			table=mongodb.getCollection(companyId+"_"+MongoDBConstants.MONGO_COLLECTION_DEVICE_STATUS_LOG);			
			table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_DEVICE_STATUS_LOG);	
			
			/*Document channelGroup = new Document("$group",
				    new Document("_id",
				        new Document("plant_id", "$plant_id")
				        .append("site_id", "$site_id")
				        .append("unit_id", "$unit_id").append("asset_id","$asset_id").append("device_id", "$device_id").append("channel_id","$channel_id")
				      	)
				    .append("max", new Document("$max", "$device_status")));
			
			Document deviceGroup = new Document("$group",
				    new Document("_id",
				        new Document("plant_id", "$plant_id")
				        .append("site_id", "$site_id")
				        .append("unit_id", "$unit_id").append("asset_id","$asset_id").append("device_id", "$device_id")
				      	)
				    .append("max", new Document("$max", "$device_status")));
			
			
			Document zeroGroup = new Document("$group",
				    new Document("_id",
				        new Document("plant_id", "$plant_id")
				        .append("site_id", "$site_id")
				        .append("unit_id", "$unit_id").append("asset_id","$asset_id")
				      	)
				    .append("max", new Document("$max", "$device_status")));
			*/
			
			Document channelGroup = new Document("$group",
				    new Document("_id",
				        new Document("plant_id", "$plant_id")
				        .append("site_id", "$site_id")
				        .append("unit_id", "$unit_id").append("asset_id","$asset_id").append("channel_id","$channel_id")
				      	)
				    .append("max", new Document("$max", "$channel_status")));
			
			
			
			
			Document zeroGroup = new Document("$group",
				    new Document("_id",
				        new Document("plant_id", "$plant_id")
				        .append("site_id", "$site_id")
				        .append("unit_id", "$unit_id").append("asset_id","$asset_id")
				      	)
				    .append("max", new Document("$max", "$channel_status")));
			
			
			Document firstGroup = new Document("$group",
				    new Document("_id",
				        new Document("plant_id", "$_id.plant_id")
				        .append("site_id", "$_id.site_id")
				        .append("unit_id", "$_id.unit_id"))
				    .append("max", new Document("$max", "$max")));
			
			Document secondGroup = new Document("$group",
				    new Document("_id",
				        new Document("plant_id", "$_id.plant_id")
				        .append("site_id", "$_id.site_id"))
				    .append("max", new Document("$max", "$max"))); 
			
			Document thirdGroup = new Document("$group",
				    new Document("_id",
				        new Document("plant_id", "$_id.plant_id"))
				    .append("max", new Document("$max", "$max"))); 
			
			Document searchQuery=new Document();
			searchQuery.put("active_status", "Yes");
			Document matchQuery = new Document();
			matchQuery.put("$match", searchQuery);	
			
			AggregateIterable<Document> iterable=table.aggregate(Arrays.asList(matchQuery,channelGroup));
			for (Document row : iterable) {
				Document rowId=(Document)row.get("_id");
				latestDeviceStatus.put(rowId.getString("channel_id"),row.getInteger("max",0));
			}
			/*iterable=table.aggregate(Arrays.asList(matchQuery,deviceGroup));
			for (Document row : iterable) {
				Document rowId=(Document)row.get("_id");
				latestDeviceStatus.put(rowId.getString("device_id"),row.getInteger("max",0));
			}
			*/
			iterable=table.aggregate(Arrays.asList(matchQuery,zeroGroup));
			int unitMax=0;
			int assetMax=0;
			for (Document row : iterable) {
				Document rowId=(Document)row.get("_id");
				if(latestDeviceStatus.get(rowId.getString("unit_id"))!=null){
					unitMax=latestDeviceStatus.get(rowId.getString("unit_id"));
					if(row.getInteger("max",0)>unitMax){
						latestDeviceStatus.put(rowId.getString("unit_id"),row.getInteger("max",0));
					}else{
						latestDeviceStatus.put(rowId.getString("unit_id"),unitMax);
					}
				}else{
					latestDeviceStatus.put(rowId.getString("unit_id"),row.getInteger("max",0));
				}
				if(latestDeviceStatus.get(rowId.getString("asset_id"))!=null){
					assetMax=latestDeviceStatus.get(rowId.getString("asset_id"));
					if(row.getInteger("max",0)>assetMax){
						latestDeviceStatus.put(rowId.getString("asset_id"),row.getInteger("max",0));
					}else{
						latestDeviceStatus.put(rowId.getString("asset_id"),assetMax);
					}
				}
				else{
					latestDeviceStatus.put(rowId.getString("asset_id"),row.getInteger("max",0));
				}
			}
			iterable=table.aggregate(Arrays.asList(matchQuery,zeroGroup,firstGroup,secondGroup));
			for (Document row : iterable) {
				Document rowId=(Document)row.get("_id");
				latestDeviceStatus.put(rowId.getString("site_id"),row.getInteger("max",0));
			}
			iterable=table.aggregate(Arrays.asList(matchQuery,zeroGroup,firstGroup,secondGroup,thirdGroup));
			for (Document row : iterable) {
				Document rowId=(Document)row.get("_id");
				latestDeviceStatus.put(rowId.getString("plant_id"),row.getInteger("max",0));
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			table=null;
			
	  }
		return latestDeviceStatus;
    }
    
	/**
	 * 
	 * @param userId
	 * @param object
	 * @return
	 */
	public static boolean isUserExistsInGrant(String userId,Document object){
		if(object!=null && object.get("grant")!=null){
			 List<Document> list=(ArrayList)object.get("grant");
			 if(list!=null && list.size()>0){
				 for(int iterate=0;iterate<list.size();iterate++){
					 Document grantObject=(Document)list.get(iterate);
					 if(grantObject.getString("uid")!=null && grantObject.getString("uid").equalsIgnoreCase(userId)){
						 return true;
					 }
				 }
			 }
		}
			 return false;
    }
	
	
	/**
	 * 
	 * @param userId
	 * @param object
	 * @return
	 */
	public static boolean isUserExistsInDeny(String userId,Document object){
		 if (object!=null && object.containsKey("user")) {
	     Document userObject = (Document) object.get("user");
	     if(userObject!=null && userObject.get("deny")!=null){
			 List<Document> list=(ArrayList)userObject.get("deny");
			 if(list!=null && list.size()>0){
				 for(int iterate=0;iterate<list.size();iterate++){
					 Document denyObject=(Document)list.get(iterate);
					 if(denyObject.getString("uid")!=null && denyObject.getString("uid").equalsIgnoreCase(userId)){
						 return true;
					 }
				 }
			   }
			 }
		 }
		 return false;
	 }
	
	 /**
	 * 
	 * @param userId
	 * @param object
	 * @param role
	 * @return
	 */
	public  static boolean isUserRoleExistsInGrant(String userId,Document object,String role){
		if(object!=null && object.get("grant")!=null){
			 List<Document> list=(ArrayList)object.get("grant");
			 if(list!=null && list.size()>0){
				 for(int iterate=0;iterate<list.size();iterate++){
					 Document grantObject=(Document)list.get(iterate);
					 if(grantObject.getString("uid")!=null && grantObject.getString("uid").equalsIgnoreCase(userId) && (role.equalsIgnoreCase(grantObject.getString("role"))
							 || role.equalsIgnoreCase(grantObject.getString("role")))){
						 return true;
					 }
				 }
			 }
		}
			 return false;
    }
	
	
	/**
     * traverse object
     *
     * @param object
     * @param outputArray
     */
    public static Document traverseObject(Document object, String userId,Map<String, Integer> llatestDeviceStatus, boolean deleteUserFlag) {
    	Document objectUpd=new Document();
    	if (CommonUtility.isUserExistsInDeny(userId,object)) {
        	return new Document();
        }
        Set<String> keys = object.keySet();
        for (String key : keys) {
        	Object value = object.get(key);
            if (value instanceof List) {
            	traverseArray((List) value, userId,llatestDeviceStatus,deleteUserFlag);
            } else if (value instanceof Document) {
                object.put(key, traverseObject((Document) value, userId,llatestDeviceStatus,deleteUserFlag));
            }else if(value instanceof String){
            	if(llatestDeviceStatus.get(object.get("id"))!=null){
            		//object.put("status", llatestDeviceStatus.get(object.get("id")));
            		objectUpd.put("status", llatestDeviceStatus.get(object.get("id")));
            	}
            }
        }
        object.putAll((Map)objectUpd);
        if(CommonUtility.isUserRoleExistsInGrant(userId, object,"siteadmin") && siteEditFlag==false){
        	siteEditFlag=true;
        }
        if(deleteUserFlag){
        	object.remove("user");
        }
        return object;
    }
    
    
    /**
	 * 
	 * @param array
	 * @param userId
	 * @throws Exception
	 */
    private static void traverseArray(List<Document> array, String userId,Map<String, Integer> llatestDeviceStatus,boolean deleteUserFlag) {
        Object[] objects = array.toArray();
        int i, length = objects.length;
        for (i = 0; i < length; i++) {
            Object object = objects[i];
            if (object instanceof Document) {
            	Document document = (Document) object;
            	 if (isUserExistsInDeny(userId,document)) {
                     array.remove(object);
                 } else {
                     traverseObject(document, userId,llatestDeviceStatus,deleteUserFlag);
                 }
            }
            
        }
    }
    
    public static String generateSessionId() {
    	int length = 32;
    	SecureRandom random = new SecureRandom();
    	BigInteger bigInteger = new BigInteger(130, random);
		String sessionId = String.valueOf(bigInteger.toString(length));
		return sessionId.toUpperCase();
    }

}
