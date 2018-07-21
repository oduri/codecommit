package com.kavi.services.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.simple.JSONArray;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.kavi.common.utility.CommonUtility;
import com.kavi.common.utility.MessageUtility;
import com.kavi.report.utility.ReportMetricUtility;
import com.kavi.report.utility.ReportUtility;
import com.kavi.report.utility.TimeSeriesUtility;
import com.mongo.db.MongoDBConnection;
import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

@RestController
public class ReportController {
	

	@RequestMapping("/")
	public String welcome() {
		return "Welcome to Report";
	}
	
	
	/**
	 * 
	 * @param req
	 * @param formParams
	 * @return
	 */
	@RequestMapping(value = "/getTimeSeriesDataForGas", method = RequestMethod.POST)
	public ResponseEntity getTimeSeriesDataForGas(HttpServletResponse response,String sessionkey,String json,String gasParamJson, String dbParamJson) {
		
		String deviceCategoryName = "";
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		if (sessionkey == null || "".equalsIgnoreCase(sessionkey)) {
			sb.append("\"Custom Message Generated basis Input\"");
			MessageUtility.updateMessage(sb, 5000, "sessionkey is null");
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		}
		String dbName = null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		int code = 0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		try {
			if (code == 0) {
				BasicDBObject jsonDocument = null;
				if (json != null) {
					jsonDocument = (BasicDBObject) JSON.parse(json);
				}
				deviceCategoryName = jsonDocument.getString("deviceCategoryName");
				mongoSingle = new MongoDBConnection(dbParamJson);
				if (dbName != null) {
					mongodb = mongoSingle.getMongoDB(dbName);
				} else {
					mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				}
				Map<String, String> linkedUserGroupDetails = CommonUtility
						.getUserAndGroupDetails(mongodb, sessionkey);
				Document gasDoc = ReportUtility.createCommonDocumentMethod(jsonDocument, mongodb, deviceCategoryName, linkedUserGroupDetails.get("organizationName"),gasParamJson);
				if(jsonDocument.getString("downloadType")!=null && jsonDocument.getString("downloadType").equalsIgnoreCase("file")) {
				/*	FileOutputStream fos =new FileOutputStream("download.json");
					fos.write(gasDoc.toJson().getBytes());
					fos.close();
					File file = new File("download.json");
					response.setContentType("application/octet_stream");
			        response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() +"\""));
			        response.setContentLength((int)file.length());
			        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
			        FileCopyUtils.copy(inputStream, response.getOutputStream());
			        inputStream.close();
			        sb.append("\"File Download Completed\"");
					MessageUtility.updateMessage(sb, code, "File Download Completed");
					return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);*/
				}
				else {
					return  new ResponseEntity<String>(com.mongodb.util.JSON.serialize(gasDoc),HttpStatus.OK);
				}
				
			} else if (code == 1001) {
				sb.append("\"Invalid key\"");
				MessageUtility.updateMessage(sb, code, "Session Invalid");
			} else if (code == 2001) {
				sb.append("\"MetaData Connection\"");
				MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
			}
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			sb = new StringBuilder();
			sb.append("{");
			sb.append("\"status\":");
			sb.append("\"MetaData Connection\"");
			MessageUtility.updateMessageWithErrors(sb, 2001, "MetaData Connection Failure", e);
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
	}
	
	@RequestMapping(value = "/updateComments", method = RequestMethod.POST)
	public ResponseEntity<String>  updateComments(String sessionkey,String json,String deviceCategoryName, String dbParamJson) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		
		if (sessionkey == null || "".equalsIgnoreCase(sessionkey)) {
			sb.append("\"Custom Message Generated basis Input\"");
			MessageUtility.updateMessage(sb, 5000, "sessionkey is null");
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		}
		
		String dbName = null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		MongoDBConnection mongoSingle = null;
		MongoDBConnection mongoRenoAppSingle = null;
		MongoDatabase mongodb = null;
		MongoDatabase mongoRenoAppdb = null;
		MongoCollection<Document> table = null;
		int code = 0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		try {
			if (code == 0) {
				Map<String, String> hDeviceCategoryName = TimeSeriesUtility.loadDeviceCategoryName();
				mongoRenoAppSingle = new MongoDBConnection(dbParamJson);
				mongoSingle = new MongoDBConnection(dbParamJson);
				if(dbName!=null) {
					mongoRenoAppdb = mongoRenoAppSingle.getMongoDB(dbName);
					 mongodb = mongoSingle.getMongoDB(dbName);
				 }else {
					 mongoRenoAppdb = mongoRenoAppSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
					 mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				 }
				
				Map<String, String> linkedUserGroupDetails = CommonUtility
						.getUserAndGroupDetails(mongoRenoAppdb, sessionkey);
				Document searchQuery = new Document();
				BasicDBObject jsonDocument = null;
				if (json != null) {
					jsonDocument = (BasicDBObject) JSON.parse(json);
					CommonUtility.maintainSessionActivities(mongoRenoAppdb, "updateComments", sessionkey,
							MongoDBConstants.ACTIVITY_START, "updateComments-" + jsonDocument.getString("document_id"));
					if (jsonDocument.getString("document_id") != null
							&& jsonDocument.getString("document_id").length() > 0) {
						searchQuery.put("_id", new ObjectId(jsonDocument.getString("document_id")));
					}
				}
				AggregateIterable<Document> iterable = null;
				JSONArray array = new JSONArray();
				String nowAsISO = "";
				TimeZone tz = TimeZone.getTimeZone("UTC");
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
				df.setTimeZone(tz);
				Document comments = new Document();
				Document matchQuery = new Document();
				Document headerObject = null;
				BasicDBObject updateData = new BasicDBObject();
				BasicDBObject command = new BasicDBObject();
				if (jsonDocument.getString("site_id") != null && !(deviceCategoryName.equalsIgnoreCase("Temperature"))) {
					table = mongodb.getCollection(hDeviceCategoryName.get(deviceCategoryName)
							+ jsonDocument.getString("site_id") + MongoDBConstants.MONGO_COLLECTION_SUFFIX);
					matchQuery.put("$match", searchQuery);
					iterable = table.aggregate(Arrays.asList(matchQuery));
					for (Document row : iterable) {								
						row.put(jsonDocument.getString("type"), jsonDocument.getString("cmnts"));												
						List<Document> commentsList = new ArrayList<Document>();
						Document commentsObj=new Document();
						commentsObj.put("user", linkedUserGroupDetails.get("userId"));
						commentsObj.put("date", CommonUtility.getDateAsObject());
						commentsObj.put("type", jsonDocument.getString("type"));
						if ( row.get("comments") != null) {
							commentsList = (ArrayList) row.get("comments");
							commentsObj.put(jsonDocument.getString("type"), jsonDocument.getString("cmnts"));
							commentsList.add(0, commentsObj);
							row.put("comments", commentsList);
						}
						else{
							commentsObj.put(jsonDocument.getString("type"), jsonDocument.getString("cmnts"));
							commentsList.add(commentsObj);
							row.put("comments", commentsList);
						}
							Document updateQuery = new Document("_id", new ObjectId(jsonDocument.getString("document_id")));
							updateData.putAll((Map)row);
							command.put("$set", updateData);
							table.updateOne(updateQuery, command);
						
					}
						
					CommonUtility.maintainSessionActivities(mongoRenoAppdb, "updateComments", sessionkey,
								MongoDBConstants.ACTIVITY_END, "");
				}
				if (jsonDocument.getString("site_id") != null && deviceCategoryName.equalsIgnoreCase("Temperature")) {
					searchQuery.put("_id", new ObjectId(jsonDocument.getString("document_id")));
					matchQuery.put("$match", searchQuery);
					table = mongodb.getCollection(
							hDeviceCategoryName.get(deviceCategoryName) + jsonDocument.getString("site_id"));
					iterable = table.aggregate(Arrays.asList(matchQuery));
					BasicDBObject commentsdoc = (BasicDBObject) jsonDocument.get("comments");
					List<Document> commentsList = new ArrayList<Document>();
					for (Document row : iterable) {
						headerObject = (Document) row.get("header");
						nowAsISO = df.format(headerObject.getDate("temperature_date"));
						String time = nowAsISO.substring(nowAsISO.indexOf("T") + 1, nowAsISO.length() - 1);
						nowAsISO = nowAsISO.substring(0, nowAsISO.indexOf("T"));
						if (time.contains(jsonDocument.getString("time"))) {
							Document typeObject = (Document) row.get(jsonDocument.getString("type"));
							/********* Comment Array Section Starts ******/
							comments.putAll((Map) commentsdoc);
							comments.put("user", linkedUserGroupDetails.get("userId"));
							comments.put("date", CommonUtility.getDateAsObject());
							if (typeObject != null && typeObject.get("comments") != null) {
								commentsList = (ArrayList) typeObject.get("comments");
								commentsList.add(0, comments);
								array.add(commentsList);
							} else {
								commentsList.add(comments);
							}
							/********* Comment Array Section Ends ******/
						}
					}
					Document updateQuery = new Document("_id", new ObjectId(jsonDocument.getString("document_id")));
					updateData.put(jsonDocument.getString("type"), array);
					Document doc = new Document();
					doc.put("comments", commentsList);
					updateData.put(jsonDocument.getString("type"), doc);
					updateData.put("data." + commentsdoc.getString("channel_name") + "_cmnts",
							commentsdoc.getString("temperature_cmnt"));
					command.put("$set", updateData);
					table.updateOne(updateQuery, command);
					CommonUtility.maintainSessionActivities(mongoRenoAppdb, "updateComments", sessionkey,
							MongoDBConstants.ACTIVITY_END, "");

				}
				Document doc = new Document();
				doc.put("status", "Success");
				doc.put("statusCode", 0);
				doc.put("statusMessage", "Comments Added Successfully");
				return new ResponseEntity<String>(com.mongodb.util.JSON.serialize(doc), HttpStatus.OK);

			} else if (code == 1001) {
				sb.append("\"Invalid key\"");
				MessageUtility.updateMessage(sb, code, "Session Invalid");
			} else if (code == 2001) {
				sb.append("\"MetaData Connection\"");
				MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
			}
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			sb = new StringBuilder();
			sb.append("{");
			sb.append("\"status\":");
			sb.append("\"MetaData Connection\"");
			MessageUtility.updateMessageWithErrors(sb, 2001, "MetaData Connection Failure", e);
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
			if (mongoRenoAppSingle != null) {
				CommonUtility.closeMongoConnection(mongoRenoAppSingle, mongoRenoAppdb, table);
			}
		}
	}
	
	@RequestMapping(value = "/viewComments", method = RequestMethod.POST)
	public ResponseEntity<String> viewComments(String sessionkey,String json,String deviceCategoryName,String dbParamJson) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		if (sessionkey == null || "".equalsIgnoreCase(sessionkey)) {
			sb.append("\"Custom Message Generated basis Input\"");
			MessageUtility.updateMessage(sb, 5000, "sessionkey is null");
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		}
		String dbName= null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		MongoDBConnection mongoSingle = null;
		MongoDBConnection mongoRenoAppSingle = null;
		MongoDatabase mongodb = null;
		MongoDatabase mongoRenoAppdb = null;
		MongoCollection<Document> table = null;
		int code = 0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		try {
			if (code == 0) {
				Map<String, String> hDeviceCategoryName = TimeSeriesUtility.loadDeviceCategoryName();
				mongoRenoAppSingle = new MongoDBConnection(dbParamJson);
				mongoSingle = new MongoDBConnection(dbParamJson);
				if (dbName != null) {
					mongoRenoAppdb = mongoRenoAppSingle.getMongoDB(dbName);
					mongodb = mongoSingle.getMongoDB(dbName);
				} else {
					mongoRenoAppdb = mongoRenoAppSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
					mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				}
				Document matchYearMonthQuery = new Document();
				Document searchQuery = new Document();
				BasicDBObject jsonDocument = null;
				if (json != null) {
					jsonDocument = (BasicDBObject) JSON.parse(json);
					if (jsonDocument.getString("document_id") != null
							&& jsonDocument.getString("document_id").length() > 0) {
						searchQuery.put("_id", new ObjectId(jsonDocument.getString("document_id")));
					}
					
				}

				Document matchQuery = new Document();
				BasicDBObject project = new BasicDBObject();
				Document projectQuery = new Document();
				JSONArray array = new JSONArray();
				String nowAsISO = "";
				DateFormat df = CommonUtility.getDateFormat("month");
				
				if (jsonDocument.getString("site_id") != null && !(deviceCategoryName.equalsIgnoreCase("Temperature"))) {
					table = mongodb.getCollection(hDeviceCategoryName.get(deviceCategoryName)
							+ jsonDocument.getString("site_id") + MongoDBConstants.MONGO_COLLECTION_SUFFIX);
					BasicDBObject unwindComments = new BasicDBObject("$unwind","$comments");
					if(deviceCategoryName.equalsIgnoreCase("Vibration")) {
						//searchQuery.put("comments.type", jsonDocument.getString("type")+"_"+jsonDocument.getString("parameter"));
						searchQuery.put("comments.type", jsonDocument.getString("type"));
						System.out.println("Vib search query: "+searchQuery.toJson());
					}
					else  {
						searchQuery.put("comments.type", jsonDocument.getString("type"));
					}
					matchQuery.put("$match", searchQuery);
					AggregateIterable<Document> iterable = null;
					iterable = table.aggregate(
							Arrays.asList(unwindComments, matchQuery));
					
					for (Document row : iterable) {
						System.out.println("Doc: "+row.toJson());
						Document comments = (Document) row.get("comments");
						if (comments != null) {
							nowAsISO = df.format(comments.getDate("date"));
							comments.put("date", nowAsISO);
							array.add(comments);
						}
					}
				}
				if (jsonDocument.getString("site_id") != null && deviceCategoryName.equalsIgnoreCase("Temperature")) {
					searchQuery.put("header."+MongoDBConstants.DEVICE_ID, jsonDocument.getString("device_id"));
					matchQuery.put("$match", searchQuery);
					table = mongodb.getCollection(
							hDeviceCategoryName.get(deviceCategoryName) + jsonDocument.getString("site_id"));
					BasicDBObject unwindTemperatureComments = new BasicDBObject("$unwind",
							"$temperature_comments.comments");
					project.put(jsonDocument.getString("type") + "." + "comments" + "." + "temperature_cmnt", 1);
					project.put(jsonDocument.getString("type") + "." + "comments" + "." + "channel_name", 1);
					project.put(jsonDocument.getString("type") + "." + "comments" + "." + "user", 1);
					project.put(jsonDocument.getString("type") + "." + "comments" + "." + "date", 1);
					projectQuery.put("$project", project);
					Document channelQueryDoc = new Document();
					Document matchChannelQueryDoc = new Document();
					channelQueryDoc.put(jsonDocument.getString("type") + ".comments." + "channel_name",
							jsonDocument.getString("parameter"));
					matchChannelQueryDoc.put("$match", channelQueryDoc);
					AggregateIterable<Document> iterable = null;
					iterable = table.aggregate(Arrays.asList(matchQuery, unwindTemperatureComments, matchYearMonthQuery,
							matchChannelQueryDoc, projectQuery));

					for (Document row : iterable) {
						Document tempCommentsObject = (Document) row.get(jsonDocument.getString("type"));
						if (tempCommentsObject != null) {
							Document commentsObject = (Document) tempCommentsObject.get("comments");
							if (commentsObject != null) {
								nowAsISO = df.format(commentsObject.getDate("date"));
								commentsObject.put("date", nowAsISO);
								array.add(commentsObject);
							}
						}
					}
				}
				Document doc = new Document();
				doc.put("status", "Success");
				doc.put("statusCode", 0);
				doc.put("statusMessage", "ViewComments Sent");
				doc.put("comments", array);
				return new ResponseEntity<String>(com.mongodb.util.JSON.serialize(doc),HttpStatus.OK);

			} else if (code == 1001) {
				sb.append("\"Invalid key\"");
				MessageUtility.updateMessage(sb, code, "Session Invalid");
			} else if (code == 2001) {
				sb.append("\"MetaData Connection\"");
				MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
			}
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			sb = new StringBuilder();
			sb.append("{");
			sb.append("\"status\":");
			sb.append("\"MetaData Connection\"");
			MessageUtility.updateMessageWithErrors(sb, 2001, "MetaData Connection Failure", e);
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
			if (mongoRenoAppSingle != null) {
				CommonUtility.closeMongoConnection(mongoRenoAppSingle, mongoRenoAppdb, table);
			}
		}
	}
	/**
	 * 
	 * @param req
	 * @param formParams
	 * @return
	 */
	@RequestMapping(value = "/getTimeSeriesData", method = RequestMethod.POST)
	public ResponseEntity<String> getTimeSeriesData(String sessionkey,String json,String gasParamJson,String dbParamJson) {
		
		String deviceCategoryName = "";
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		if (sessionkey == null || "".equalsIgnoreCase(sessionkey)) {
			sb.append("\"Custom Message Generated basis Input\"");
			MessageUtility.updateMessage(sb, 5000, "sessionkey is null");
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		}
		String dbName = null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		MongoCollection<Document> paramLogTable = null;
		int code = 0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		try {
			if (code == 0) {
				BasicDBObject jsonDocument = null;
				if (json != null) {
					jsonDocument = (BasicDBObject) JSON.parse(json);
				}
				deviceCategoryName = jsonDocument.getString("deviceCategoryName");
				HashMap<String, String> hDeviceCategoryName = new HashMap<String, String>();
				hDeviceCategoryName.put("Vibration", "v_");
				hDeviceCategoryName.put("Temperature", "t_");
				hDeviceCategoryName.put("Corrosion", "c_");
				hDeviceCategoryName.put("Gas", "g_");
				hDeviceCategoryName.put("Temperature_new", "t_");
				mongoSingle = new MongoDBConnection(dbParamJson);
				if (dbName != null) {
					mongodb = mongoSingle.getMongoDB(dbName);
				} else {
					mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				}
				Map<String, String> linkedUserGroupDetails = CommonUtility
						.getUserAndGroupDetails(mongodb, sessionkey);
				
				Document yearMonthQuery = new Document();
				Document matchYearMonthQuery = new Document();
				Document searchQuery = new Document();
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");

				BasicDBObject project = new BasicDBObject();
				JSONArray array = new JSONArray();
				JSONArray timeSeriesArray = new JSONArray();
				Document projectQuery = new Document();
				Document matchQuery = new Document();
				AggregateIterable<Document> iterable = null;
				AggregateIterable<Document> paramLogiterable = null;
				Document searchQueryLog = new Document();
				Document matchQueryLog = new Document();
				BasicDBObject projectLog = new BasicDBObject();
				Document projectQueryLog = new Document();

				DateFormat df = CommonUtility.getDateFormat("Year");
				String nowAsISO = "";
				Document dataObject = null;
				Document headerObject = null;
				Document gates = null;
				Document thresholds = null;
			
				ArrayList<Double> x_accel_fft_x_axis = new ArrayList<Double>();
				ArrayList<Double> x_accel_fft_x_axis_rpm = new ArrayList<Double>();
				ArrayList<Double> x_accel_fft_x_axis_orders = new ArrayList<Double>();
				ArrayList<Double> y_accel_fft_x_axis = new ArrayList<Double>();
				ArrayList<Double> y_accel_fft_x_axis_rpm = new ArrayList<Double>();
				ArrayList<Double> y_accel_fft_x_axis_orders = new ArrayList<Double>();
				ArrayList<Double> z_accel_fft_x_axis = new ArrayList<Double>();
				ArrayList<Double> z_accel_fft_x_axis_rpm = new ArrayList<Double>();
				ArrayList<Double> z_accel_fft_x_axis_orders = new ArrayList<Double>();
				
				ArrayList<Double> x_vel_fft_x_axis = new ArrayList<Double>();
				ArrayList<Double> x_vel_fft_x_axis_rpm = new ArrayList<Double>();
				ArrayList<Double> x_vel_fft_x_axis_orders = new ArrayList<Double>();
				ArrayList<Double> y_vel_fft_x_axis = new ArrayList<Double>();
				ArrayList<Double> y_vel_fft_x_axis_rpm = new ArrayList<Double>();
				ArrayList<Double> y_vel_fft_x_axis_orders = new ArrayList<Double>();
				ArrayList<Double> z_vel_fft_x_axis = new ArrayList<Double>();
				ArrayList<Double> z_vel_fft_x_axis_rpm = new ArrayList<Double>();
				ArrayList<Double> z_vel_fft_x_axis_orders = new ArrayList<Double>();
								
				String thickness_algorithm = "";				 		
				paramLogTable = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_USER_DEFINED_PARAM_LOG);				
				if (jsonDocument != null && jsonDocument.getString("site_id") != null && (deviceCategoryName.equalsIgnoreCase("Vibration")
						|| deviceCategoryName.equalsIgnoreCase("Corrosion"))) {	
					
					if (deviceCategoryName.equalsIgnoreCase("Corrosion")){	
						String channelId=jsonDocument.getString("channel_id")==null?jsonDocument.getString("channels"):jsonDocument.getString("channel_id");
						Document corrosionDoc = ReportUtility.createCommonDocumentMethod(jsonDocument, mongodb, deviceCategoryName, linkedUserGroupDetails.get("organizationName"),null);
						Document uiParametersDoc=ReportUtility.getUIParametersForCorrosion(paramLogTable, deviceCategoryName, jsonDocument,channelId);
						corrosionDoc.putAll((Map)uiParametersDoc);
						//sort ms_xaxis,ms_yaxis & range_xaxis,range_yaxis
						//ReportUtility.sortCorrosionDoc(corrosionDoc);
						return  new ResponseEntity<String>(com.mongodb.util.JSON.serialize(corrosionDoc),HttpStatus.OK);
					}
					else if (deviceCategoryName.equalsIgnoreCase("Vibration")){	
						String channelId=jsonDocument.getString("channel_id")==null?jsonDocument.getString("channels"):jsonDocument.getString("channel_id");
						Document vibrationDoc = ReportUtility.createCommonDocumentMethod(jsonDocument, mongodb, deviceCategoryName, linkedUserGroupDetails.get("organizationName"),null);
						//Document uiParametersDoc=ReportUtility.getUIParametersForCorrosion(paramLogTable, deviceCategoryName, jsonDocument,channelId);
						//vibrationDoc.putAll((Map)uiParametersDoc);
						//sort ms_xaxis,ms_yaxis & range_xaxis,range_yaxis
						//ReportUtility.sortCorrosionDoc(corrosionDoc);
						return  new ResponseEntity<String>(com.mongodb.util.JSON.serialize(vibrationDoc),HttpStatus.OK);
					}
					else if(deviceCategoryName.equalsIgnoreCase("Vibration_Old")){
					project.putAll(TimeSeriesUtility.getSpecificDetailsForTimeSeriesReport(jsonDocument, "get"));
					projectQuery.put("$project", project);
					table = mongodb.getCollection(hDeviceCategoryName.get(deviceCategoryName) + jsonDocument.getString("site_id") + MongoDBConstants.MONGO_COLLECTION_SUFFIX);
					
					if (jsonDocument.getString("year_month_day") != null 
							&& jsonDocument.getString("year_month_day").length() > 0) {
						if (jsonDocument.getString("document_id") != null
								&& jsonDocument.getString("document_id").length() > 0) {
							searchQuery.put("_id", new ObjectId(jsonDocument.getString("document_id")));
							matchQuery.put("$match", searchQuery);
							iterable = table.aggregate(
									Arrays.asList(matchQuery, projectQuery));
						} 
						else {
							searchQuery.put(MongoDBConstants.DEVICE_ID, jsonDocument.getString("device_id"));
							
							simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
							Date yearMonthDate = simpleDateFormat.parse(jsonDocument.getString("year_month_day"));
							Calendar cal = Calendar.getInstance();
							cal.setTime(yearMonthDate);
							int year = cal.get(Calendar.YEAR);
							int monthNumber = cal.get(Calendar.MONTH);
							int dateNumber = cal.get(Calendar.DAY_OF_MONTH);
							monthNumber += 1;
							String[] split = jsonDocument.getString("time").split(":");
							yearMonthQuery.put("message_time",
									new BasicDBObject("$eq",
											new DateTime(year, monthNumber, dateNumber, Integer.valueOf(split[0]),
													Integer.valueOf(split[1]), Integer.valueOf(split[2]), DateTimeZone.UTC)
															.toDate()));
							matchYearMonthQuery.put("$match", yearMonthQuery);
							matchQuery.put("$match", searchQuery);
							iterable = table.aggregate(
									Arrays.asList(matchQuery, matchYearMonthQuery, projectQuery));
						}
						for (Document row : iterable) {
							nowAsISO = df.format(row.getDate("message_time"));
							row.put("yearMonthDay", nowAsISO.substring(0, nowAsISO.indexOf("T")));
							row.put("time",
									nowAsISO.substring(nowAsISO.indexOf("T") + 1, nowAsISO.length() - 1));
							row.remove("message_time");							
							
							if (deviceCategoryName.equalsIgnoreCase("Vibration")){								
								if(jsonDocument.getString("x")!=null){
									x_accel_fft_x_axis = (ArrayList<Double>)row.get("x_accel_fft_x_axis");
									if(x_accel_fft_x_axis != null) {
										for (int iterate=0; iterate<x_accel_fft_x_axis.size(); iterate++) {
											x_accel_fft_x_axis_rpm.add(x_accel_fft_x_axis.get(iterate)*60);
											x_accel_fft_x_axis_orders.add(x_accel_fft_x_axis.get(iterate)/500);
										}
									}
									row.put("x_accel_fft_x_axis_rpm", x_accel_fft_x_axis_rpm);
									row.put("x_accel_fft_x_axis_orders", x_accel_fft_x_axis_orders);
									Double x_accel_fft_peak_x_axis = (Double)row.get("x_accel_fft_peak_x_axis");
									if(x_accel_fft_peak_x_axis != null) {
										row.put("x_accel_fft_peak_x_axis_rpm",x_accel_fft_peak_x_axis*60);
										row.put("x_accel_fft_peak_x_axis_orders",x_accel_fft_peak_x_axis/500);
									}
									x_vel_fft_x_axis = (ArrayList<Double>)row.get("x_vel_fft_x_axis");
									if(x_vel_fft_x_axis != null) {
										for (int iterate=0; iterate<x_vel_fft_x_axis.size(); iterate++) {
											x_vel_fft_x_axis_rpm.add(x_vel_fft_x_axis.get(iterate)*60);
											x_vel_fft_x_axis_orders.add(x_vel_fft_x_axis.get(iterate)/500);
										}
									}
									row.put("x_vel_fft_x_axis_rpm", x_vel_fft_x_axis_rpm);
									row.put("x_vel_fft_x_axis_orders", x_vel_fft_x_axis_orders);
									Double x_vel_fft_peak_x_axis = (Double)row.get("x_vel_fft_peak_x_axis");
									if(x_vel_fft_peak_x_axis!=null){
										row.put("x_vel_fft_peak_x_axis_rpm",x_vel_fft_peak_x_axis*60);
										row.put("x_vel_fft_peak_x_axis_orders",x_vel_fft_peak_x_axis/500);
									}
								}
								
								if(jsonDocument.getString("y")!=null){
									y_accel_fft_x_axis = (ArrayList<Double>)row.get("y_accel_fft_x_axis");
									if(y_accel_fft_x_axis != null) {
										for (int iterate=0; iterate<y_accel_fft_x_axis.size(); iterate++) {
											y_accel_fft_x_axis_rpm.add(y_accel_fft_x_axis.get(iterate)*60);
											y_accel_fft_x_axis_orders.add(y_accel_fft_x_axis.get(iterate)/500);
										}
									}
									row.put("y_accel_fft_x_axis_rpm", y_accel_fft_x_axis_rpm);
									row.put("y_accel_fft_x_axis_orders", y_accel_fft_x_axis_orders);
									Double y_accel_fft_peak_x_axis = (Double)row.get("y_accel_fft_peak_x_axis");
									if(y_accel_fft_peak_x_axis != null) {
										row.put("y_accel_fft_peak_x_axis_rpm",y_accel_fft_peak_x_axis*60);
										row.put("y_accel_fft_peak_x_axis_orders",y_accel_fft_peak_x_axis/500);
									}
									y_vel_fft_x_axis = (ArrayList<Double>)row.get("y_vel_fft_x_axis");
									if(y_vel_fft_x_axis != null) {
										for (int iterate=0; iterate<y_vel_fft_x_axis.size(); iterate++) {
											y_vel_fft_x_axis_rpm.add(y_vel_fft_x_axis.get(iterate)*60);
											y_vel_fft_x_axis_orders.add(y_vel_fft_x_axis.get(iterate)/500);
										}
									}
									row.put("y_vel_fft_x_axis_rpm", y_vel_fft_x_axis_rpm);
									row.put("y_vel_fft_x_axis_orders", y_vel_fft_x_axis_orders);
									Double y_vel_fft_peak_x_axis = (Double)row.get("y_vel_fft_peak_x_axis");
									if(y_vel_fft_peak_x_axis!= null) {
										row.put("y_vel_fft_peak_x_axis_rpm",y_vel_fft_peak_x_axis*60);
										row.put("y_vel_fft_peak_x_axis_orders",y_vel_fft_peak_x_axis/500);
									}
								}
								
								if(jsonDocument.getString("z")!=null){
									z_accel_fft_x_axis = (ArrayList<Double>)row.get("z_accel_fft_x_axis");
									if(z_accel_fft_x_axis != null) {
										for (int iterate=0; iterate<z_accel_fft_x_axis.size(); iterate++) {
											z_accel_fft_x_axis_rpm.add(z_accel_fft_x_axis.get(iterate)*60);
											z_accel_fft_x_axis_orders.add(z_accel_fft_x_axis.get(iterate)/500);
										}
									}
									row.put("z_accel_fft_x_axis_rpm", z_accel_fft_x_axis_rpm);
									row.put("z_accel_fft_x_axis_orders", z_accel_fft_x_axis_orders);
									Double z_accel_fft_peak_x_axis = (Double)row.get("z_accel_fft_peak_x_axis");
									if(z_accel_fft_peak_x_axis != null) {
										row.put("z_accel_fft_peak_x_axis_rpm",z_accel_fft_peak_x_axis*60);
										row.put("z_accel_fft_peak_x_axis_orders",z_accel_fft_peak_x_axis/500);
									}
									z_vel_fft_x_axis = (ArrayList<Double>)row.get("z_vel_fft_x_axis");
									if(z_vel_fft_x_axis != null) {
										for (int iterate=0; iterate<z_vel_fft_x_axis.size(); iterate++) {
											z_vel_fft_x_axis_rpm.add(z_vel_fft_x_axis.get(iterate)*60);
											z_vel_fft_x_axis_orders.add(z_vel_fft_x_axis.get(iterate)/500);
										}
									}
									row.put("z_vel_fft_x_axis_rpm", z_vel_fft_x_axis_rpm);
									row.put("z_vel_fft_x_axis_orders", z_vel_fft_x_axis_orders);
									Double z_vel_fft_peak_x_axis = (Double)row.get("z_vel_fft_peak_x_axis");
									if(z_vel_fft_peak_x_axis != null) {
										row.put("z_vel_fft_peak_x_axis_rpm",z_vel_fft_peak_x_axis*60);
										row.put("z_vel_fft_peak_x_axis_orders",z_vel_fft_peak_x_axis/500);
									}
								}
							}
							
							Document messageUpdObject=new Document();
							messageUpdObject.put("message_interval",row);
							messageUpdObject.put("doc_id", row.get("_id").toString());
							
							if(gates != null){
								messageUpdObject.putAll((Map)gates);
							}
							
							row.remove("_id");
							timeSeriesArray.add(messageUpdObject);
							array = new JSONArray();
							
						}
					} else {					
						searchQuery.put(MongoDBConstants.DEVICE_ID, jsonDocument.getString("device_id"));
						searchQuery.put("message_time",new BasicDBObject("$gte",ReportUtility.frameQueryForDate(jsonDocument, "from_date")).append("$lte",ReportUtility.frameQueryForDate(jsonDocument, "to_date")));
						matchQuery.put("$match", searchQuery);
						iterable = table.aggregate(Arrays.asList(matchQuery, projectQuery));
						array = new JSONArray();
						for (Document row : iterable) {
							thresholds = new Document();
							nowAsISO = df.format(row.getDate("message_time"));
							row.put("yearMonthDay", nowAsISO.substring(0, nowAsISO.indexOf("T")));
							row.put("time",
									nowAsISO.substring(nowAsISO.indexOf("T") + 1, nowAsISO.length() - 1));
							row.remove("message_time");
							
							array.add(row);
							Document messageUpdObject=new Document();
							messageUpdObject.put("message_interval",array);
							messageUpdObject.put("doc_id", row.get("_id").toString());
							row.remove("_id");
							
							timeSeriesArray.add(messageUpdObject);
							array = new JSONArray();
							
							if (deviceCategoryName.equalsIgnoreCase("Corrosion")){
								thickness_algorithm = (String)row.get("thickness_algorithm");
								if (thickness_algorithm != null && thickness_algorithm.equalsIgnoreCase("maxpeak")) {
									row.put("thickness",row.get("thickness_maxpeak"));
									row.put("temperature_corrected_thickness",row.get("temperature_corrected_thickness_maxpeak"));
								}
								if (thickness_algorithm != null && thickness_algorithm.equalsIgnoreCase("flank")) {
									row.put("thickness",row.get("thickness_flank"));
									row.put("temperature_corrected_thickness",row.get("temperature_corrected_thickness_flank"));
								}
								if (thickness_algorithm != null && thickness_algorithm.equalsIgnoreCase("zerocrossing")) {
									row.put("thickness",row.get("thickness_zerocrossing"));
									row.put("temperature_corrected_thickness",row.get("temperature_corrected_thickness_zerocrossing"));
								}
								if(row.get("probe_temperature") instanceof String) {
									row.remove("probe_temperature");
								}
								row.remove("thickness_maxpeak");
								row.remove("thickness_flank");
								row.remove("thickness_zerocrossing");
								row.remove("temperature_corrected_thickness_maxpeak");
								row.remove("temperature_corrected_thickness_flank");
								row.remove("temperature_corrected_thickness_zerocrossing");
								row.remove("db_parameters");
							}							
						}
						if(timeSeriesArray.size() > 0) {
							Document object=(Document)timeSeriesArray.get(timeSeriesArray.size()-1);
							object.put("latest", true);
						}

						searchQueryLog.put(MongoDBConstants.DEVICE_ID, jsonDocument.getString("device_id"));
						searchQueryLog.put("active_flag", true);
						matchQueryLog.put("$match", searchQueryLog);
						projectLog.putAll(ReportUtility.getTopChartParameters(deviceCategoryName));
						paramLogiterable = paramLogTable.aggregate(Arrays.asList(matchQueryLog, projectQueryLog));
						projectQueryLog.put("$project", projectLog);
						if (deviceCategoryName.equalsIgnoreCase("Corrosion") || deviceCategoryName.equalsIgnoreCase("Vibration")){
							for (Document paramLogrow : paramLogiterable) {
								thresholds = paramLogrow;
							}
						}
					}
					
					Document doc = new Document();
					doc.put("status", "Success");
					doc.put("statusCode", 0);
					doc.put("statusMessage", "getTimeSeriesData");
					doc.put("timeSeries", timeSeriesArray);
					if(jsonDocument.getString("from_date") != null && thresholds != null){
						doc.putAll((Map)thresholds);
					}					
					return  new ResponseEntity<String>(com.mongodb.util.JSON.serialize(doc),HttpStatus.OK);
				  }
				}
				if (jsonDocument.getString("site_id") != null && deviceCategoryName.equalsIgnoreCase("Temperature_Old")) {
					String channels = "";
					project.putAll(TimeSeriesUtility.getSpecificDetailsForTimeSeriesReport(jsonDocument, "get"));
					projectQuery.put("$project", project);
					
					table = mongodb.getCollection(
							hDeviceCategoryName.get(deviceCategoryName) + jsonDocument.getString("site_id"));
					if (jsonDocument.getString("year_month_day") != null
							&& jsonDocument.getString("year_month_day").length() > 0) {
						
						if (jsonDocument.getString("document_id") != null
								&& jsonDocument.getString("document_id").length() > 0) {
							searchQuery.put("_id", new ObjectId(jsonDocument.getString("document_id")));
						}
						else {
							searchQuery.put("header."+MongoDBConstants.DEVICE_ID, jsonDocument.getString("device_id"));
						}
						
						matchQuery.put("$match", searchQuery);
						simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
						Date yearMonthDate = simpleDateFormat.parse(jsonDocument.getString("year_month_day"));
						Calendar cal = Calendar.getInstance();
						cal.setTime(yearMonthDate);
						int year = cal.get(Calendar.YEAR);
						int monthNumber = cal.get(Calendar.MONTH);
						int dateNumber = cal.get(Calendar.DAY_OF_MONTH);
						monthNumber += 1;
						String[] split = jsonDocument.getString("time").split(":");
						yearMonthQuery.put("header.temperature_date",
								new BasicDBObject("$eq",
										new DateTime(year, monthNumber, dateNumber, Integer.valueOf(split[0]),
												Integer.valueOf(split[1]), Integer.valueOf(split[2]), DateTimeZone.UTC)
														.toDate()));
						matchYearMonthQuery.put("$match", yearMonthQuery);
						iterable = table.aggregate(Arrays.asList(matchQuery, matchYearMonthQuery, projectQuery));
					} else {
						searchQuery.put("header."+MongoDBConstants.DEVICE_ID, jsonDocument.getString("device_id"));
						Date fromDate = simpleDateFormat.parse(jsonDocument.getString("from_date"));

						Calendar cal = Calendar.getInstance();
						cal.setTime(fromDate);

						int year = cal.get(Calendar.YEAR);
						int monthNumber = cal.get(Calendar.MONTH);
						int dateNumber = cal.get(Calendar.DAY_OF_MONTH);
						monthNumber += 1;

						Date toDate = simpleDateFormat.parse(jsonDocument.getString("to_date"));						

						cal = Calendar.getInstance();
						cal.setTime(toDate);

						int toYear = cal.get(Calendar.YEAR);
						int toMonthNumber = cal.get(Calendar.MONTH);
						int toDateNumber = cal.get(Calendar.DAY_OF_MONTH);
						toMonthNumber += 1;
						searchQuery.put("header.temperature_date",
								new BasicDBObject("$gte",
										new DateTime(year, monthNumber, dateNumber, 0, 0, DateTimeZone.UTC)
												.toDate()).append("$lte",
														new DateTime(toYear, toMonthNumber, toDateNumber, 23, 59,
																DateTimeZone.UTC).toDate()));
						matchQuery.put("$match", searchQuery);
						iterable = table.aggregate(Arrays.asList(matchQuery, projectQuery));
					}
					for (Document row : iterable) {
						headerObject = (Document) row.get("header");
						headerObject.put("doc_id", row.get("_id").toString());
						nowAsISO = df.format(headerObject.getDate("temperature_date"));
						headerObject.put("yearMonthDay", nowAsISO.substring(0, nowAsISO.indexOf("T")));
						headerObject.put("time", nowAsISO.substring(nowAsISO.indexOf("T") + 1, nowAsISO.length() - 1));
						headerObject.remove("temperature_date");
						dataObject = (Document) row.get("data");
						List<String> arrayDocument = (ArrayList) dataObject.get("0x05");
						for (int iterate = 0; iterate < arrayDocument.size(); iterate++) {
							channels = "c" + (iterate + 1);
							headerObject.put(channels, Double.parseDouble(arrayDocument.get(iterate)));
							if (dataObject.get(channels + "_cmnts") != null) {
								headerObject.put(channels + "_cmnts", dataObject.get(channels + "_cmnts"));
							}
						}
						row.remove("_id");
						array.add(headerObject);
					}
					Document doc = new Document();
					doc.put("status", "Success");
					doc.put("statusCode", 0);
					doc.put("statusMessage", "getTimeSeriesData");
					doc.put("timeSeries", array);
										
					return  new ResponseEntity<String>(com.mongodb.util.JSON.serialize(doc),HttpStatus.OK);
				}
				if (jsonDocument.getString("site_id") != null && deviceCategoryName.equalsIgnoreCase("Temperature")) {
					Document temperatureDoc = ReportUtility.createCommonDocumentMethod(jsonDocument, mongodb, deviceCategoryName, linkedUserGroupDetails.get("organizationName"),null);
					return  new ResponseEntity<String>(com.mongodb.util.JSON.serialize(temperatureDoc),HttpStatus.OK);
				}
				if (jsonDocument.getString("site_id") != null && deviceCategoryName.equalsIgnoreCase("Gas")){
					Document gasDoc = ReportUtility.createCommonDocumentMethod(jsonDocument, mongodb, deviceCategoryName, linkedUserGroupDetails.get("organizationName"),gasParamJson);
					return  new ResponseEntity<String>(com.mongodb.util.JSON.serialize(gasDoc),HttpStatus.OK);
				}
				
			} else if (code == 1001) {
				sb.append("\"Invalid key\"");
				MessageUtility.updateMessage(sb, code, "Session Invalid");
			} else if (code == 2001) {
				sb.append("\"MetaData Connection\"");
				MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
			}
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} catch (

		Exception e) {
			e.printStackTrace();
			sb = new StringBuilder();
			sb.append("{");
			sb.append("\"status\":");
			sb.append("\"MetaData Connection\"");
			MessageUtility.updateMessageWithErrors(sb, 2001, "MetaData Connection Failure", e);
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
	}
	
	@RequestMapping(value = "/getDeviceParameters", method = RequestMethod.POST)
	public ResponseEntity<String> getDeviceParameters(String sessionkey,String json,String dbParamJson) {

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		String deviceCategoryName = "";
		if (sessionkey == null || "".equalsIgnoreCase(sessionkey)) {
			sb.append("\"Custom Message Generated basis Input\"");
			MessageUtility.updateMessage(sb, 5000, "sessionkey is null");
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		}
		String dbName = null;
		 if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		MongoCollection<Document> mappingtable = null;
		int code = 0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		try {
			if (code == 0) {
				BasicDBObject jsonDocument = null;
				if (json != null) {
					jsonDocument = (BasicDBObject) JSON.parse(json);
				}
				deviceCategoryName = jsonDocument.getString("deviceCategoryName");
				HashMap<String, String> hDeviceCategoryName = new HashMap<String, String>();
				hDeviceCategoryName.put("Vibration", "v_");
				hDeviceCategoryName.put("Temperature", "t_");
				hDeviceCategoryName.put("Corrosion", "c_");
				hDeviceCategoryName.put("Gas", "g_");

				HashMap<String, String> hDataBaseName = new HashMap<String, String>();
				hDataBaseName.put("v_", MongoDBConstants.APP_DB_NAME);
				hDataBaseName.put("t_", MongoDBConstants.APP_DB_NAME);
				hDataBaseName.put("c_", MongoDBConstants.APP_DB_NAME);
				hDataBaseName.put("g_", MongoDBConstants.APP_DB_NAME);

				mongoSingle = new MongoDBConnection(dbParamJson);
				if (dbName != null) {
					mongodb = mongoSingle.getMongoDB(dbName);
				} else {
					mongodb = mongoSingle.getMongoDB(hDataBaseName.get(hDeviceCategoryName.get(deviceCategoryName)));
				}
				
				Document yearMonthQuery = new Document();
				Document matchYearMonthQuery = new Document();
				Document searchQuery = new Document();
				if (jsonDocument != null) {
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
					if (jsonDocument.getString("document_id") != null
							&& jsonDocument.getString("document_id").length() > 0) {
						searchQuery.put("_id", new ObjectId(jsonDocument.getString("document_id")));
					}
					if (jsonDocument.getString("year_month_day") != null) {
						simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
						Date yearMonthDate = simpleDateFormat.parse(jsonDocument.getString("year_month_day"));
						Calendar cal = Calendar.getInstance();
						cal.setTime(yearMonthDate);
						int year = cal.get(Calendar.YEAR);
						int monthNumber = cal.get(Calendar.MONTH);
						int dateNumber = cal.get(Calendar.DAY_OF_MONTH);
						monthNumber += 1;
						String[] split = jsonDocument.getString("time").split(":");
						yearMonthQuery.put("message_time",
								new BasicDBObject("$eq",
										new DateTime(year, monthNumber, dateNumber, Integer.valueOf(split[0]),
												Integer.valueOf(split[1]), Integer.valueOf(split[2]), DateTimeZone.UTC)
														.toDate()));
						matchYearMonthQuery.put("$match", yearMonthQuery);
					}
				}

				AggregateIterable<Document> mappingiterable = null;
				Document mappingsearchQuery = new Document();
				Document mappingmatchQuery = new Document();	
				mappingtable = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_DEVICE_PARAM_MAPPING);
				if(deviceCategoryName.equalsIgnoreCase("Vibration")) {
					mappingsearchQuery.put("device_category", jsonDocument.get("deviceCategoryName")+"_ui");
				}
				else if(deviceCategoryName.equalsIgnoreCase("Corrosion")) {
					mappingsearchQuery.put("device_category", jsonDocument.get("deviceCategoryName")+"_ui");
				}
				mappingmatchQuery.put("$match", mappingsearchQuery);
				mappingiterable = mappingtable.aggregate(Arrays.asList(mappingmatchQuery));
				
				if (jsonDocument.getString("site_id") != null) { 
						searchQuery.put(MongoDBConstants.DEVICE_ID, jsonDocument.getString("device_id"));
						table = mongodb.getCollection(hDeviceCategoryName.get(deviceCategoryName)
								+ jsonDocument.getString("site_id") + MongoDBConstants.MONGO_COLLECTION_SUFFIX);
						Document matchQuery = new Document();
						matchQuery.put("$match", searchQuery);
						AggregateIterable<Document> iterable = null;
						Document messageObject = new Document();
						if (jsonDocument.getString("year_month_day") != null
								&& jsonDocument.getString("year_month_day").length() > 0) {
							iterable = table.aggregate(Arrays.asList(matchQuery,matchYearMonthQuery
									));
							for (Document row : iterable) {
								row.put("doc_id", row.get("_id").toString());
								row.remove("_id");
								messageObject.putAll((Map)row);
							}
						}
				
						
						if (deviceCategoryName.equalsIgnoreCase("Vibration")) {
							 Map<String,String> lResultOrder = new HashMap<String,String> ();
							 Map<String,JSONArray> linkedResultMap = new HashMap<String,JSONArray> ();
							 List<Document> lMappUIDocuments = ReportUtility.getMappingDocuments(mappingiterable,jsonDocument.getString("type"),messageObject,deviceCategoryName);
							 List<Document> lMappUIDocumentsSort = ReportUtility.sortDocumentArray(lMappUIDocuments, "tab_order", "parameters_order");
							 List<Document> removeList = new ArrayList<Document>();
							 for (int iterate = 0; iterate < lMappUIDocumentsSort.size(); iterate++){
								 	Document doc = lMappUIDocumentsSort.get(iterate);
									if(!(doc.get("device_param_display") == null) && doc.get("device_param_display").equals(false)) {
										removeList.add(doc);
									}
							 }
							 lMappUIDocumentsSort.removeAll(removeList);
							 Document doc = new Document();
								doc.put("status", "Success");
								doc.put("statusCode", code);
								doc.put("statusMessage", "getDeviceParameters");
								doc.put("result", lMappUIDocumentsSort);
								JSONArray resultsTabArray = new JSONArray();
								TreeMap<String,JSONArray> resultsTabMap = new TreeMap<String,JSONArray> ();
								for(int iterate = 0; iterate < lMappUIDocuments.size(); iterate++) {
									Document resultDoc = (Document)lMappUIDocuments.get(iterate);
										if ( (Boolean)resultDoc.get("results_display") != null && (Boolean)resultDoc.get("results_display") != false) {	
											String id = (Integer)resultDoc.get("results_order")+"_"+(String)resultDoc.get("group_name");
											lResultOrder.put((String)resultDoc.get("group_name"), id);
									if(resultsTabMap.get(id) == null) {
										resultsTabArray = new JSONArray();
										resultsTabArray.add(resultDoc);
										resultsTabMap.put(id, resultsTabArray);
									}
									else {
										resultsTabArray = resultsTabMap.get(id);
										resultsTabArray.add(resultDoc);
										resultsTabMap.put(id, resultsTabArray);
									}
									}
								}
								for (Map.Entry<String, JSONArray> entry : resultsTabMap.entrySet()){
									   linkedResultMap.put(entry.getKey().substring(entry.getKey().indexOf("_")+1), entry.getValue());
								}
								doc.put("resultTab", linkedResultMap);						
							return new ResponseEntity<String>(com.mongodb.util.JSON.serialize(doc),HttpStatus.OK);
						}
						 else if (deviceCategoryName.equalsIgnoreCase("Corrosion")) {
							 Map<String,String> lResultOrder = new HashMap<String,String> ();
							 Map<String,JSONArray> linkedResultMap = new HashMap<String,JSONArray> ();
							 List<Document> lMappUIDocuments = ReportUtility.getMappingDocuments(mappingiterable,jsonDocument.getString("type"),messageObject,deviceCategoryName);
							 List<Document> lMappUIDocumentsSort = ReportUtility.sortDocumentArray(lMappUIDocuments, "tab_order", "parameters_order");
							 List<Document> removeList = new ArrayList<Document>();
							 for (int iterate = 0; iterate < lMappUIDocumentsSort.size(); iterate++){
								 	Document doc = lMappUIDocumentsSort.get(iterate);
									if(!(doc.get("device_param_display") == null) && doc.get("device_param_display").equals(false)) {
										removeList.add(doc);
									}
							 }
							 lMappUIDocumentsSort.removeAll(removeList);
							 Document doc = new Document();
								doc.put("status", "Success");
								doc.put("statusCode", code);
								doc.put("statusMessage", "getDeviceParameters");
								doc.put("result", lMappUIDocumentsSort);
								JSONArray resultsTabArray = new JSONArray();
								TreeMap<String,JSONArray> resultsTabMap = new TreeMap<String,JSONArray> ();
								for(int iterate = 0; iterate < lMappUIDocuments.size(); iterate++) {
									Document resultDoc = (Document)lMappUIDocuments.get(iterate);
										if ( (Boolean)resultDoc.get("results_display") != null && (Boolean)resultDoc.get("results_display") != false) {	
											String id = (Integer)resultDoc.get("results_order")+"_"+(String)resultDoc.get("group_name");
											lResultOrder.put((String)resultDoc.get("group_name"), id);
									if(resultsTabMap.get(id) == null) {
										resultsTabArray = new JSONArray();
										resultsTabArray.add(resultDoc);
										resultsTabMap.put(id, resultsTabArray);
									}
									else {
										resultsTabArray = resultsTabMap.get(id);
										resultsTabArray.add(resultDoc);
										resultsTabMap.put(id, resultsTabArray);
									}
									}
								}
								 for (Map.Entry<String, JSONArray> entry : resultsTabMap.entrySet()){
									   linkedResultMap.put(entry.getKey().substring(entry.getKey().indexOf("_")+1), entry.getValue());
								   }
								doc.put("resultTab", linkedResultMap);
								return new ResponseEntity<String>(com.mongodb.util.JSON.serialize(doc),HttpStatus.OK);
						 }
				}

			} else if (code == 1001) {
				sb.append("\"Invalid key\"");
				MessageUtility.updateMessage(sb, code, "Session Invalid");
			} else if (code == 2001) {
				sb.append("\"MetaData Connection\"");
				MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
			}
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			sb = new StringBuilder();
			sb.append("{");
			sb.append("\"status\":");
			sb.append("\"MetaData Connection\"");
			MessageUtility.updateMessageWithErrors(sb, 2001, "MetaData Connection Failure", e);
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
	}
	/**
	 * 
	 * @param req
	 * @param formParams
	 * @return
	 */
	@RequestMapping(value = "/compareDeviceParameters", method = RequestMethod.POST)
	public ResponseEntity<String> compareDeviceParameters(String sessionkey,String devicejson,String channeljson,String deviceCategoryName,String dbParamJson) {

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		String dbName = null;
		if (dbParamJson != null && dbParamJson.length() > 0) {
			BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			dbName = dbParamJsonDoc.getString("db_name");
		}
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		MongoCollection<Document> mappingtable = null;
		int code = 0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		try {
			if (code == 0) {
				mongoSingle = new MongoDBConnection(dbParamJson);
				if (dbName != null) {
					mongodb = mongoSingle.getMongoDB(dbName);
				} else {
					mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				}
				BasicDBObject jsonDocument = new BasicDBObject();
				BasicDBObject channelJsonDocument = new BasicDBObject();
				BasicDBObject assetInQuery = null;
				BasicDBObject inQuery = new BasicDBObject();
				Document searchQuery = new Document();
				
				if (devicejson != null && devicejson.length()>3) {
					assetInQuery = new BasicDBObject();
					jsonDocument = (BasicDBObject) JSON.parse(devicejson);
					inQuery.put("$in", jsonDocument.get("compare_device_list"));
					if(jsonDocument.get("compare_asset_list")!=null) {
					  assetInQuery.put("$in", jsonDocument.get("compare_asset_list"));
					  searchQuery.put(MongoDBConstants.ASSET_ID, assetInQuery);
					}
					searchQuery.put(MongoDBConstants.DEVICE_ID, inQuery);
				}
				if (channeljson != null && channeljson.length()>3) {
					channelJsonDocument = (BasicDBObject) JSON.parse(channeljson);
				}
				/*if (channeljson != null && channeljson.length()>3) {
					inQuery = new BasicDBObject();
					assetInQuery = new BasicDBObject();
					channelJsonDocument = (BasicDBObject) JSON.parse(channeljson);
					inQuery.put("$in", channelJsonDocument.get("compare_channel_list"));
					if(channelJsonDocument.get("compare_asset_list")!=null) {
					  assetInQuery.put("$in", channelJsonDocument.get("compare_asset_list"));
					  searchQuery.put(MongoDBConstants.ASSET_ID, assetInQuery);
					}
					if(channelJsonDocument.get("compare_channel_list")!=null) {
					  searchQuery.put(MongoDBConstants.CHANNEL_ID, inQuery);
					}
				}*/
				Document projectQuery = new Document();
				Document matchQuery = new Document();
				BasicDBObject project = new BasicDBObject();
				
				jsonDocument.remove("compare_device_list");
				jsonDocument.remove("compare_asset_list");
				matchQuery.put("$match", searchQuery);				
				Set<String> projectKeys = jsonDocument.keySet();
				for (String key:projectKeys) {
					project.append(key, new BasicDBObject("$ifNull",Arrays.asList("$"+key, "")));
				}
				project.put("device_name",new BasicDBObject("$ifNull",Arrays.asList("$device_name", "")));
				projectQuery.put("$project", project);
				table = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_DEVICE_PARAM_LATEST);				
				Map<String,Document> mappinglookup = new HashMap<String,Document>();
				AggregateIterable<Document> iterable = null;
				AggregateIterable<Document> mappingiterable = null;
				Document mappingsearchQuery = new Document();
				Document mappingmatchQuery = new Document();
				iterable = table.aggregate(Arrays.asList(matchQuery, projectQuery));

				JSONArray fieldArray = new JSONArray();
				JSONArray resultDeviceArray = new JSONArray();
				JSONArray resultChannelArray = new JSONArray();
				Document newdoc = new Document();
				Document resultdoc = new Document();
				JSONArray fieldArray2 = new JSONArray();
				Document measurement = new Document();
				Map<String,JSONArray> resultsTabMap = new HashMap<String,JSONArray> ();
				
				//Get Mapping details
				mappingtable = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_DEVICE_PARAM_MAPPING);
				if(deviceCategoryName.equalsIgnoreCase("Vibration")) {
						mappingsearchQuery.put("device_category", deviceCategoryName+"_ui");
				}
				else if(deviceCategoryName.equalsIgnoreCase("Corrosion")) {
					mappingsearchQuery.put("device_category", deviceCategoryName+"_ui");
				}
				else if(deviceCategoryName.equalsIgnoreCase("Gas")) {
					mappingsearchQuery.put("device_category", deviceCategoryName+"_ui");
				}
				mappingmatchQuery.put("$match", mappingsearchQuery);
				mappingiterable = mappingtable.aggregate(Arrays.asList(mappingmatchQuery));

				if(deviceCategoryName.equalsIgnoreCase("Vibration1")) {
					mappinglookup = ReportUtility.getCompareDeviceMappingDocuments(mappingiterable);
					for (Document row : iterable) {
						resultdoc = new Document();
						resultsTabMap = new HashMap<String,JSONArray> ();
						row.remove("_id");
						measurement = (Document) row;
						Set<String> subtypeJsonKey = measurement.keySet();
						for (String subtypeKey : subtypeJsonKey) {
							Document mappinglookupkey = (Document) mappinglookup.get(subtypeKey);
							if(mappinglookupkey != null && !(subtypeKey.equalsIgnoreCase("device_name"))) {	
								String units = (String) mappinglookupkey.get("units");
								newdoc = new Document();
								newdoc.put("key", subtypeKey);
								if(units != null && units.length()>0) {
									newdoc.put("Description", mappinglookupkey.get("display_name")+" ["+units+"]");
								}
								else {
									newdoc.put("Description", mappinglookupkey.get("display_name"));
								}
								newdoc.put("parameter_type", mappinglookupkey.get("parameter_type"));
								newdoc.put("data_type", mappinglookupkey.get("data_type"));
								newdoc.put("Value", measurement.get(subtypeKey));
								newdoc.put("units", mappinglookupkey.get("units"));
								newdoc.put("editable_flag", mappinglookupkey.getBoolean("editable_flag")==null?false:true);
								if(resultsTabMap.get(mappinglookupkey.get("tab_name")) == null) {
									fieldArray2 = new JSONArray();
									fieldArray2.add(newdoc);
									resultsTabMap.put(mappinglookupkey.getString("tab_name"), fieldArray2);
								}
								else {
									fieldArray2 = resultsTabMap.get(mappinglookupkey.getString("tab_name"));
									fieldArray2.add(newdoc);
									resultsTabMap.put(mappinglookupkey.getString("tab_name"), fieldArray2);
								}
							}
						}
						resultdoc.put("device_id", row.get("device_id"));
						resultdoc.put("device_name", row.get("device_name"));
						resultdoc.put("asset_name", row.get("asset_name")==null?"":row.getString("asset_name"));
						resultdoc.put("asset_id", row.get("asset_id")==null?"":row.getString("asset_id"));
						resultdoc.put("channel_id", row.getString("channel_id")==null?"":row.getString("channel_id"));
						resultdoc.put("channel_name", row.getString("channel_name")==null?"":row.getString("channel_name"));
						resultdoc.put("data",resultsTabMap);
						fieldArray.add(resultdoc);
					}
				}
				
				else if(deviceCategoryName.equalsIgnoreCase("Gas")) {
					mappinglookup = ReportUtility.getCompareDeviceMappingDocuments(mappingiterable);
					for (Document row : iterable) { 
						resultdoc = new Document();
						resultsTabMap = new HashMap<String,JSONArray> ();
						row.remove("_id");
						measurement = (Document) row;
						Set<String> subtypeJsonKey = measurement.keySet();
						for (String subtypeKey : subtypeJsonKey) {
							Document mappinglookupkey = (Document) mappinglookup.get(subtypeKey);
							if(mappinglookupkey != null && !(subtypeKey.equalsIgnoreCase("device_name"))) {	
								String units = (String) mappinglookupkey.get("units");
								newdoc = new Document();
								newdoc.put("key", subtypeKey);
								if(units != null && units.length()>0) {
									newdoc.put("Description", mappinglookupkey.get("display_name")+" ["+units+"]");
								}
								else {
									newdoc.put("Description", mappinglookupkey.get("display_name"));
								}
								newdoc.put("parameter_type", mappinglookupkey.get("parameter_type"));
								newdoc.put("data_type", mappinglookupkey.get("data_type"));
								newdoc.put("Value", measurement.get(subtypeKey));
								newdoc.put("units", mappinglookupkey.get("units"));
								newdoc.put("editable_flag", mappinglookupkey.getBoolean("editable_flag")==null?false:true);
								if(resultsTabMap.get(mappinglookupkey.get("tab_name")) == null) {
									fieldArray2 = new JSONArray();
									fieldArray2.add(newdoc);
									resultsTabMap.put(mappinglookupkey.getString("tab_name"), fieldArray2);
								}
								else {
									fieldArray2 = resultsTabMap.get(mappinglookupkey.getString("tab_name"));
									fieldArray2.add(newdoc);
									resultsTabMap.put(mappinglookupkey.getString("tab_name"), fieldArray2);
								}
							}
						}
						
						resultdoc.put("device_id", row.get("device_id"));
						resultdoc.put("device_name", row.get("device_name"));
						resultdoc.put("asset_name", row.get("asset_name")==null?"":row.getString("asset_name"));
						resultdoc.put("asset_id", row.get("asset_id")==null?"":row.getString("asset_id"));
						resultdoc.put("channel_id", row.getString("channel_id")==null?"":row.getString("channel_id"));
						resultdoc.put("channel_name", row.getString("channel_name")==null?"":row.getString("channel_name"));
						resultdoc.put("data",resultsTabMap);
						fieldArray.add(resultdoc);
					}
				}
				else if(deviceCategoryName.equalsIgnoreCase("Corrosion") || deviceCategoryName.equalsIgnoreCase("Vibration")) {	
					mappinglookup = ReportUtility.getCompareDeviceMappingDocuments(mappingiterable);
					table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_DEVICE_PARAM_LATEST);
					projectKeys = jsonDocument.keySet();
					if(projectKeys.size()>0) {
						project = new BasicDBObject();
						searchQuery = new Document();
						//searchQuery.put(MongoDBConstants.ASSET_ID, assetInQuery);
						searchQuery.put(MongoDBConstants.DEVICE_ID, inQuery);
						searchQuery.put("parameter_type", "device");
						jsonDocument.remove("compare_device_list");
						jsonDocument.remove("compare_asset_list");
						matchQuery=new Document();
						matchQuery.put("$match", searchQuery);
						projectQuery=new Document();
						for (String key:projectKeys) {
							project.append(key, new BasicDBObject("$ifNull",Arrays.asList("$"+key, "")));
						}
						projectQuery.put("$project", project);
						iterable = table.aggregate(Arrays.asList(matchQuery, projectQuery));
						ReportUtility.getResultArray(iterable,mappinglookup,resultDeviceArray,fieldArray2);
					}
					projectKeys = channelJsonDocument.keySet();
					if(projectKeys.size()>0) {
						inQuery = new BasicDBObject();
						inQuery.put("$in", channelJsonDocument.get("compare_channel_list"));
						searchQuery = new Document();
						searchQuery.put("channel_id", inQuery);
						searchQuery.put("parameter_type", "channel");
						channelJsonDocument.remove("compare_channel_list");
						channelJsonDocument.remove("compare_asset_list");
						matchQuery=new Document();
						matchQuery.put("$match", searchQuery);
						project = new BasicDBObject();
						projectQuery=new Document();
						for (String key:projectKeys) {
							project.append(key, new BasicDBObject("$ifNull",Arrays.asList("$"+key, "")));
						}
						projectQuery.put("$project", project);
						iterable = table.aggregate(Arrays.asList(matchQuery, projectQuery));					
						ReportUtility.getResultArray(iterable,mappinglookup,resultChannelArray,fieldArray2);
					}
				}
								
				Document doc = new Document();
				doc.put("status", "Success");
				doc.put("statusCode", code);
				doc.put("statusMessage", "compareDeviceParameters done"); 
				if(deviceCategoryName.equalsIgnoreCase("Corrosion") || deviceCategoryName.equalsIgnoreCase("Vibration")) {
					doc.put("deviceArray", resultDeviceArray);
					doc.put("channelArray", resultChannelArray);
				}else {
					doc.put("deviceArray", fieldArray);
					doc.put("channelArray", new JSONArray());
				}
				return new ResponseEntity<String>(com.mongodb.util.JSON.serialize(doc),HttpStatus.OK);

			} else if (code == 1001) {
				sb.append("\"Invalid key\"");
				MessageUtility.updateMessage(sb, code, "Session Invalid");
			} else if (code == 2001) {
				sb.append("\"MetaData Connection\"");
				MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
			}
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			sb = new StringBuilder();
			sb.append("{");
			sb.append("\"status\":");
			sb.append("\"MetaData Connection\"");
			MessageUtility.updateMessageWithErrors(sb, 2001, "MetaData Connection Failure", e);
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
	}
	@RequestMapping(value = "/viewDeviceParameters", method = RequestMethod.POST)
	public ResponseEntity<String> viewDeviceParameters(String sessionkey,String json,String dbParamJson) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		String device_category = "";
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		String dbName = null;
		if (dbParamJson != null && dbParamJson.length() > 0) {
			BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			dbName = dbParamJsonDoc.getString("db_name");
		}
		
		int code = 0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		try {
			if (code == 0) {
				BasicDBObject jsonDocument = null;
				if (json != null) {
					jsonDocument = (BasicDBObject) JSON.parse(json);
				}
				mongoSingle = new MongoDBConnection(dbParamJson);
				if (dbName != null) {
					mongodb = mongoSingle.getMongoDB(dbName);
				} else {
					mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				}

				Document searchQuery = new Document();
				Document matchQuery = new Document();

				table = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_DEVICE_PARAM_MAPPING);
				AggregateIterable<Document> iterable = null;
				device_category = (String) jsonDocument.get("deviceCategoryName");
				if(device_category.equalsIgnoreCase("Corrosion") || device_category.equalsIgnoreCase("Vibration") || device_category.equalsIgnoreCase("Gas")) {
					searchQuery.put("device_category", device_category+"_ui");
					matchQuery.put("$match", searchQuery);
					iterable = table.aggregate(Arrays.asList(matchQuery));
				}
				
				Document doc = new Document();
				BasicDBObject resultDocument = new BasicDBObject();
				if(device_category.equalsIgnoreCase("Vibration")) {
					resultDocument = ReportUtility.groupMappingDocuments(iterable, true, "tab_name");
				}
				else if(device_category.equalsIgnoreCase("Corrosion")) {
					   resultDocument = ReportUtility.groupMappingDocuments(iterable, true, "tab_name");
				}
				else if(device_category.equalsIgnoreCase("Gas")) {
					resultDocument = ReportUtility.groupMappingDocuments(iterable, true, "tab_name");
				}
				
				doc.put("status", "Success");
				doc.put("statusCode", code);
				doc.put("statusMessage", "viewDeviceParameters done");
				doc.put("result", resultDocument);				
				return new ResponseEntity<String>(com.mongodb.util.JSON.serialize(doc),HttpStatus.OK);
			} else if (code == 1001) {
				sb.append("\"Invalid key\"");
				MessageUtility.updateMessage(sb, code, "Session Invalid");
			} else if (code == 2001) {
				sb.append("\"MetaData Connection\"");
				MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
			}
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} catch (

		Exception e) {
			e.printStackTrace();
			sb = new StringBuilder();
			sb.append("{");
			sb.append("\"status\":");
			sb.append("\"MetaData Connection\"");
			MessageUtility.updateMessageWithErrors(sb, 2001, "MetaData Connection Failure", e);
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/upsertUserConfigParameters", method = RequestMethod.POST)
	public ResponseEntity<String> upsertUserConfigParameters(String sessionkey,String deviceCategoryName,String devicejson, String channeljson,String dbParamJson) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		Document mappingsearchQuery = null;
		Document mappingmatchQuery = null;
		AggregateIterable<Document> mappingiterable = null;
		MongoCollection<Document> paramLatestTable = null;
		MongoCollection<Document> paramLogTable = null;
		MongoCollection<Document> mappingtable = null;
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy/MM/dd");
		dateFormatLocal.setTimeZone(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("MM/dd/yyyy");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));		
		String dbName = null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		int code = 0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		
		String response = null;
		
		//decrypt the json values
		/*devicejson = ReportUtility.decrypt(devicejson);
		channeljson = ReportUtility.decrypt(channeljson);
		System.out.println("Device Json: "+devicejson);
		System.out.println("Channel Json: "+channeljson);*/
		try {
			if (code == 0) {
				BasicDBObject deviceJsonDoc = null;
				BasicDBObject channelJsonDoc = null;
				List<BasicDBObject> deviceJsonDocList = new ArrayList<BasicDBObject>();
				List<BasicDBObject> channelJsonDocList = new ArrayList<BasicDBObject>();
				if(devicejson!=null) {
					deviceJsonDoc = (BasicDBObject) JSON.parse(devicejson);
					deviceJsonDocList=(ArrayList<BasicDBObject>)deviceJsonDoc.get("deviceJsonDoc");
				}
				if(channeljson!=null) {
					channelJsonDoc = (BasicDBObject) JSON.parse(channeljson);
					channelJsonDocList=(List<BasicDBObject>) channelJsonDoc.get("channelJsonDoc");
				}
				mongoSingle = new MongoDBConnection(dbParamJson);
				if(dbName!=null) {
					 mongodb = mongoSingle.getMongoDB(dbName);
				 }else {
				 mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				 }
				
				paramLatestTable = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_DEVICE_PARAM_LATEST);
				paramLogTable = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_USER_DEFINED_PARAM_LOG);
				mappingtable = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_DEVICE_PARAM_MAPPING);
				Map<String, String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
				mappingsearchQuery = new Document();
				mappingmatchQuery = new Document();
				mappingsearchQuery.put("device_category", deviceCategoryName+"_ui");
				mappingmatchQuery.put("$match", mappingsearchQuery);
				mappingiterable = mappingtable.aggregate(Arrays.asList(mappingmatchQuery));
				if (CollectionUtils.isNotEmpty(deviceJsonDocList)) {
					ReportUtility.upsertDeviceJsonParameters(deviceJsonDocList, deviceCategoryName, mappingiterable, paramLatestTable, paramLogTable, linkedUserGroupDetails);
				}
				if(CollectionUtils.isNotEmpty(channelJsonDocList)) {
					ReportUtility.upsertDeviceChannelParameters(channelJsonDocList, deviceCategoryName, mappingiterable, paramLatestTable, paramLogTable, linkedUserGroupDetails);
				}
				Document doc = new Document();
				doc.put("status", "Success");
				doc.put("statusCode", code);
				doc.put("statusMessage", "Config parameters updated successfully");	
				/*response = com.mongodb.util.JSON.serialize(doc);
				response = ReportUtility.encrypt(response);*/
				return new ResponseEntity<String>(com.mongodb.util.JSON.serialize(doc),HttpStatus.OK);
			}
		}catch(Exception e) {
			e.printStackTrace();
			sb = new StringBuilder();
			sb.append("{");
			sb.append("\"status\":");
			sb.append("\"MetaData Connection\"");
			MessageUtility.updateMessageWithErrors(sb, 2001, "MetaData Connection Failure", e);
			/*response = ReportUtility.encrypt(sb.toString());*/
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		}
		finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, paramLogTable);
			}
			
		}
		//response = ReportUtility.encrypt(sb.toString());
		return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
	}

	
	/**
	 * 
	 * @param fromDate
	 * @param toDate
	 * @param deviceId
	 * @param downloadType
	 * @return
	 */
	/*@RequestMapping(value = "/downloadTemperatureFile", method = RequestMethod.GET)
	//@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public ResponseEntity downloadTemperatureFile(HttpServletResponse response,String webServiceAuthenticationKey,String fromDate,String toDate, String deviceId,
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
				 mongoSingle=new MongoDBConnection();
				 mongodb=mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				 table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY);
				 List<TemperatureDO> lTemperatureDO=ReportUtility.getSiteCollectionFromLocationHierarchyTable(table, "FHR",deviceId);
				 DateFormat df = CommonUtility.getDateFormat("monthnew");
					
				 if(lTemperatureDO.size()<=0){
					 sb.append("\"No Matching Device Found\"");
					 MessageUtility.updateMessage(sb, 7002, "No Matching Device Found");
				 }
				 if(fromDate.indexOf(":")<0){
					 fromDate=fromDate+" 00:00";
				 }
				 if(toDate.indexOf(":")<0){
					 toDate=toDate+" 23:59";
				 }
				 SimpleDateFormat simpleDateFormat = new SimpleDateFormat ("MM/dd/yyyy hh:mm");
				 Date queryFromDate = simpleDateFormat.parse(fromDate);
				 //Document searchQuery = new Document("_id",new ObjectId("5aa81c550cd7c7148057c42e"));
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
				 BasicDBObject sortQuery=new BasicDBObject();
				 sortQuery.put(MongoDBConstants.DEVICE_ID, MongoDBConstants.MIN_ORDER_BY);
				 sortQuery.put("measurement_id", MongoDBConstants.MIN_ORDER_BY);
				 sortQuery.put("measurement_date", MongoDBConstants.MIN_ORDER_BY);
				 BasicDBObject sortQueryObject=new BasicDBObject("$sort",sortQuery);
				 ByteArrayOutputStream csvResult = new ByteArrayOutputStream();
				 Writer outputWriter = new OutputStreamWriter(csvResult);
				 CsvWriterSettings settings = new CsvWriterSettings();
				 settings.setHeaders("AssetID", "DeviceID", "MeasureID", "DateTime", "C1","C2","C3","C4","C5","C6","C7","C8","C9","C10");
				 Document matchQuery = new Document();
				 boolean skipHeader=false;
				 for(TemperatureDO temperatureDO:lTemperatureDO){
					table=mongodb.getCollection(temperatureDO.getCollectionName());
					searchQuery.put("asset_id", temperatureDO.getAssetId());
					searchQuery.put("device_id", temperatureDO.getDeviceId());
					searchQuery.put("measurement_date",new BasicDBObject("$gte", 
							 new DateTime(year, monthNumber, dateNumber, startHourTime, startMinuteTime,DateTimeZone.UTC).toDate()).
							 append("$lte",new DateTime(toYear, toMonthNumber, toDateNumber, endHourTime, endMinuteTime,DateTimeZone.UTC).toDate()));
					matchQuery.put("$match", searchQuery);
					iterable = table.aggregate(Arrays.asList(matchQuery,sortQueryObject));
					for (Document row : iterable) {
						ReportUtility.mapTemperature(row,settings,outputWriter,skipHeader,df);
						skipHeader=true;
					}
					searchQuery=new Document();
					matchQuery = new Document();
				 } 
				if(downloadType.equalsIgnoreCase("data")){
					if(csvResult.toString().length()<=0){
						ReportUtility.generateEmptyHeaders(settings, outputWriter);
						return Response.status(200).entity(csvResult.toString()).build();
					}else{
						return Response.status(200).entity(csvResult.toString()).build();
					}
				}else if(downloadType.equalsIgnoreCase("file")){
					FileOutputStream fos  =new FileOutputStream("download.csv");
					if(csvResult.toString().length()<=0){
						ReportUtility.generateEmptyHeaders(settings, outputWriter);
					}
					fos.write(csvResult.toString().getBytes());
					fos.close();
					File file = new File("download.csv");
					ResponseBuilder builder = Response.ok(file);
				    builder.header("Content-Disposition", "attachment; filename=" + file.getName());
				    Response response = builder.build();
				    return response;
				}
				 outputWriter.close();
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
  }*/
	
	@RequestMapping(value = "/getDeviceStatusLog", method = RequestMethod.POST)
	public ResponseEntity<String> getDeviceStatusLog(String sessionkey,String json,String advancedFilterJson,String companyId,String timezone,String dbParamJson) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		String dbName = null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		
		if (sessionkey == null || "".equalsIgnoreCase(sessionkey)) {
			sb.append("\"Custom Message Generated basis Input\"");
			MessageUtility.updateMessage(sb, 5000, "sessionkey is null");
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		}
		if (companyId == null || "".equalsIgnoreCase(companyId)) {
			sb.append("\"Custom Message Generated basis Input\"");
			MessageUtility.updateMessage(sb, 5000, "companyId is null");
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		}
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		int code = 0;
		boolean jsonAvaialableFlag = false;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		JSONArray array = new JSONArray();
		try {
			if (code == 0) {
				BasicDBObject jsonDocument = null;
				Document searchQuery = new Document();
				AggregateIterable<Document> iterable = null;
				if (json != null && json.length() > 3) {
					jsonDocument = (BasicDBObject) JSON.parse(json);
					searchQuery.putAll(jsonDocument);
					Set<String> keys = jsonDocument.keySet();
					if (keys != null) {
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
						for (String str : keys) {
							Object obj = jsonDocument.get(str);
							if (obj instanceof List) {
								BasicDBObject inQuery = new BasicDBObject();
								inQuery.put("$in", jsonDocument.get(str));
								searchQuery.put(str, inQuery);
							}

							else if (str.endsWith("time")) {
								String date = jsonDocument.getString(str).substring(0,
										jsonDocument.getString(str).lastIndexOf(" "));
								date = date.trim();
								Date yearMonthDate = simpleDateFormat.parse(date);
								Calendar cal = Calendar.getInstance();
								cal.setTime(yearMonthDate);
								int year = cal.get(Calendar.YEAR);
								int monthNumber = cal.get(Calendar.MONTH);
								int dateNumber = cal.get(Calendar.DAY_OF_MONTH);
								monthNumber += 1;
								String time = jsonDocument.getString(str).substring(date.length() + 1,
										jsonDocument.getString(str).length());
								String[] split = time.split(":");
								searchQuery.put(str, new BasicDBObject("$eq",
										new DateTime(year, monthNumber, dateNumber, Integer.valueOf(split[0]),
												Integer.valueOf(split[1]), Integer.valueOf(split[2]), DateTimeZone.UTC)
														.toDate()));

							} else {
								searchQuery.put(str, jsonDocument.get(str));
							}
						}
					}
					jsonAvaialableFlag = true;
				} else {
					jsonDocument = new BasicDBObject();
					jsonAvaialableFlag = false;
				}
				Document matchQuery = new Document();
				matchQuery.put("$match", searchQuery);
				if (advancedFilterJson != null && advancedFilterJson.length() > 0) {
					matchQuery = new Document();
					matchQuery.put("$match", ReportUtility.frameFilterQuery(advancedFilterJson));
					jsonAvaialableFlag = true;
				}
				mongoSingle = new MongoDBConnection(dbParamJson);
				 if(dbName!=null) {
					 mongodb = mongoSingle.getMongoDB(dbName);
				 }else {
				 mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				 }
				Document doc = new Document();
				Map<Integer,String> reportDesc = new HashMap<Integer,String> ();
				BasicDBObject unwindPage = new BasicDBObject("$unwind", "$page");
				table = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_UI_CONFIG);			
				Document projectQuery = new Document();
				Document project = new Document();
				project.put("page.ui_label", 1);
				project.put("page.report_label", 1);
				project.put("_id", 0);
				projectQuery.put("$project", project);
				iterable = table.aggregate(Arrays.asList(unwindPage, projectQuery));
				for (Document row : iterable) {
					Document page = (Document) row.get("page");
					List<Document> uiLabel = new ArrayList<Document>();
					uiLabel = (ArrayList) page.get("ui_label");					
					if(uiLabel != null) {					
					doc.putAll((Map) page);
					} 
					if (page.get("report_label") != null) {
						ArrayList<Document> report_label = (ArrayList<Document>)page.get("report_label");
						if(report_label!=null && report_label.size()>0){
							for(Document reportLabelDoc:report_label){
								reportDesc.put(reportLabelDoc.getInteger("device_status"),reportLabelDoc.getString("device_status_description"));
							}
						}
					}
				}
				table = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_DEVICE_STATUS_LOG);
				BasicDBObject sortQuery = new BasicDBObject();
				sortQuery.put("device_status_description", MongoDBConstants.MIN_ORDER_BY);
				sortQuery.put("etl_time", MongoDBConstants.MAX_ORDER_BY);
				BasicDBObject sortQueryObject = new BasicDBObject("$sort", sortQuery);
				if (jsonAvaialableFlag) {
					iterable = table.aggregate(Arrays.asList(matchQuery, sortQueryObject));
				} else {
					iterable = table.aggregate(Arrays.asList(sortQueryObject));
				}
				String nowAsISO = "";
				DateFormat df = CommonUtility.getDateFormat("month");
				if(timezone==null) {
					timezone="EST";
				}
				for (Document row : iterable) {
					System.out.println("row..."+row.get("_id"));
					nowAsISO = df.format(row.getDate("etl_time"));
					row.put("etl_time", nowAsISO);
					if (row.get("event_from_time") != null) {
						nowAsISO = df.format(row.getDate("event_from_time"));
						row.put("event_from_time", nowAsISO);
					}
					if (row.get("event_to_time") != null && row.get("event_to_time") != "") {
						nowAsISO = df.format(row.getDate("event_to_time"));
						row.put("event_to_time", nowAsISO);
					}
					if (row.get("first_reported_time") != null) {
						//nowAsISO = df.format(row.getDate("first_reported_time"));
						//row.put("first_reported_time_new", nowAsISO);
						System.out.println(row.getDate("first_reported_time"));
						row.put("first_reported_time", ReportUtility.formatDateToString(row.getDate("first_reported_time"), "MM/dd/yyyy HH:mm:ss", timezone));
					}
					if (row.get("last_reported_time") != null) {
						//nowAsISO = df.format(row.getDate("last_reported_time"));
						//row.put("last_reported_time", nowAsISO);
						row.put("last_reported_time", ReportUtility.formatDateToString(row.getDate("last_reported_time"), "MM/dd/yyyy HH:mm:ss", timezone));
					}
					if(row.get("channel_status") instanceof Integer) {
						row.put("channel_status_description",reportDesc.get(row.getInteger("channel_status")));
					}else if(row.get("channel_status") instanceof Double){
						 Integer status=(int) Double.parseDouble(""+row.getDouble("channel_status"));
						row.put("channel_status_description",reportDesc.get(status));
					}
					row.remove("_id");
					array.add(row);
				}
				
				doc.put("status", "Success");
				doc.put("statusCode", 0);
				doc.put("statusMessage", "getDeviceStatusLog");
				doc.put("result", array);
				return new ResponseEntity<String>(com.mongodb.util.JSON.serialize(doc),HttpStatus.OK);
				
			} else if (code == 1001) {
				sb.append("\"Invalid key\"");
				MessageUtility.updateMessage(sb, code, "Session Invalid");
			} else if (code == 2001) {
				sb.append("\"MetaData Connection\"");
				MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
			}
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			sb = new StringBuilder();
			sb.append("{");
			sb.append("\"status\":");
			sb.append("\"MetaData Connection\"");
			MessageUtility.updateMessageWithErrors(sb, 2001, "MetaData Connection Failure", e);
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
	}
	
	 /**
	  * 
	  * @param sessionkey
	  * @param json
	  * @return
	  */
	 
	 @RequestMapping(value = "/generateUtilizationForSite", method = RequestMethod.POST)
	 public ResponseEntity<String> generateUtilizationForSite(String sessionkey,String json,String dbParamJson) {
		 
		 StringBuilder sb=new StringBuilder();
	     sb.append("{");
		 sb.append("\"status\":");
		 String dbName = null;
		 if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		 MongoDBConnection mongoSingle = null;
		 MongoDatabase mongodb = null;
		 MongoCollection<Document> table=null;
		 int code=0;	
		 try{
			 if(json!=null && json.length()>0){
				 BasicDBObject jsonDocument = null;
				 jsonDocument = (BasicDBObject) JSON.parse(json);
				if (dbName != null) {
					code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
				} else {
					code = CommonUtility.validateAuthenticationKey(sessionkey);
				}
				 if(code==0){
					 mongoSingle = new MongoDBConnection(dbParamJson);
					 if(dbName!=null) {
						 mongodb = mongoSingle.getMongoDB(dbName);
					 }else {
					 mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
					 }
					 table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_ASSET_SUMMARY_STATISTICS);
					 List<Document> resultList=new ArrayList<Document>();
					 AggregateIterable<Document> iterable=table.aggregate(ReportMetricUtility.frameQueryForUtilization(jsonDocument));
					 for(Document row:iterable) {
						 row.put("from_date", jsonDocument.getString("from_date"));
						 row.put("to_date", jsonDocument.getString("to_date"));
						 System.out.println(row.getInteger("totalCount"));
						 row.put("utilization_percentage", row.getDouble("utilPercentageSum")/row.getInteger("totalCount"));
						 row.remove("utilPercentageSum");
						 row.remove("totalCount");
						 resultList.add(row);
					 }
					 Document status=new Document();
				     status.put("status", "Success");
				     status.put("statusCode", code);
				     status.put("statusMessage", "generateUtilizationForSite Report Sent");
				     status.put("result", resultList);
				     return new ResponseEntity<String>(status.toJson(), HttpStatus.OK);
				}
				else if(code==1001){
				   sb.append("\"Invalid key\"");
				   MessageUtility.updateMessage(sb, code, "Session Invalid");
				}
				else if(code==2001){
				   sb.append("\"MetaData Connection\"");
				   MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
				}
			  return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
			 }
		 }catch(Exception e){
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
		 sb.append("\"Custom Message Generated basis Input\"");
		 MessageUtility.updateMessage(sb, 5000, "json is null");
		 return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
	 }
	 
	 /**
	  *  
	  * @param sessionkey
	  * @param json
	  * @return
	  */
	 @RequestMapping(value = "/getMaxLatestTimeStamp", method = RequestMethod.POST)
	 public ResponseEntity<String>  getMaxLatestTimeStamp(String sessionkey,String json,String dbParamJson) {
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			sb.append("\"status\":");
			String dbName = null;
			 if(dbParamJson!=null && dbParamJson.length()>0) {
				 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
				 dbName = dbParamJsonDoc.getString("db_name");
			 }
			if (sessionkey == null || "".equalsIgnoreCase(sessionkey)) {
				sb.append("\"Custom Message Generated basis Input\"");
				MessageUtility.updateMessage(sb, 5000, "sessionkey is null");
				return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
			}

			MongoDBConnection mongoSingle = null;
			MongoDatabase mongodb = null;
			MongoCollection<Document> table = null;
			int code = 0;
			if (dbName != null) {
				code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
			} else {
				code = CommonUtility.validateAuthenticationKey(sessionkey);
			}
			try {
				if (code == 0) {
					mongoSingle = new MongoDBConnection(dbParamJson);
					 if(dbName!=null) {
						 mongodb = mongoSingle.getMongoDB(dbName);
					 }else {
					 mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
					 }
					Map<String, String> hDeviceCategoryName = TimeSeriesUtility.loadDeviceCategoryName();
					String deviceCategoryName=null;
					BasicDBObject jsonDocument=null;
					if (json != null) {
						jsonDocument = (BasicDBObject) JSON.parse(json);
						deviceCategoryName=jsonDocument.getString("deviceCategoryName");
					}
					Document projQuery=new Document();
					
					Document searchQuery=new Document();
					searchQuery.put("asset_id", jsonDocument.getString("asset_id"));
					Document inQuery=new Document();
					inQuery.put("$in", jsonDocument.get("device_id"));
					searchQuery.put("device_id", inQuery);
					Document sortQuery=new Document();
					if("Temperature".equalsIgnoreCase(deviceCategoryName)) {
						sortQuery.put("measurement_date", MongoDBConstants.MAX_ORDER_BY);
						projQuery.append("_id", 0);
						projQuery.append("measurement_date", 1);
						
					}else {
						sortQuery.put("message_time", MongoDBConstants.MAX_ORDER_BY);
						projQuery.append("_id", 0);
						projQuery.append("message_time", 1);
					}
					Document sortQueryObject=new Document();
					sortQueryObject.put("$sort",sortQuery);
					table = mongodb.getCollection(hDeviceCategoryName.get(deviceCategoryName)
							+ jsonDocument.getString("site_id") + MongoDBConstants.MONGO_COLLECTION_SUFFIX);
					Document object=new Document();
					FindIterable<Document> timeCursor = table.find(searchQuery).projection(projQuery).sort(sortQuery).limit(1);
					if(timeCursor.iterator().hasNext()){
						object=(Document)timeCursor.iterator().next();
						if("Temperature".equalsIgnoreCase(deviceCategoryName)) {
							object.put("message_time", object.get("measurement_date"));
							object.remove("measurement_date");
						}
						return new ResponseEntity<String>(com.mongodb.util.JSON.serialize(object), HttpStatus.OK);
					}else {
						object.put("status", "No Data Found");
						object.put("message_time", "N/A");
						object.put("statusCode", 101);
						object.put("statusMessage", "No Data Found");
						return new ResponseEntity<String>(com.mongodb.util.JSON.serialize(object), HttpStatus.OK);
					}
				} else if (code == 1001) {
					sb.append("\"Invalid key\"");
					MessageUtility.updateMessage(sb, code, "Session Invalid");
				} else if (code == 2001) {
					sb.append("\"MetaData Connection\"");
					MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
				}
				return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				sb = new StringBuilder();
				sb.append("{");
				sb.append("\"status\":");
				sb.append("\"MetaData Connection\"");
				MessageUtility.updateMessageWithErrors(sb, 2001, "MetaData Connection Failure", e);
				return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
			} finally {
				if (mongoSingle != null) {
					CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
				}
				
			}
	}
	 
	 /**
	  *  
	  * @param sessionkey
	  * @param json
	  * @return
	  */
	 @RequestMapping(value = "/getLatestTimeStamp", method = RequestMethod.POST)
	 public ResponseEntity<String>  getLatestTimeStamp(String sessionkey,String json,String dbParamJson) {
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			sb.append("\"status\":");
			
			String dbName = null;
			if(dbParamJson!=null && dbParamJson.length()>0) {
				 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
				 dbName = dbParamJsonDoc.getString("db_name");
			 }
			if (sessionkey == null || "".equalsIgnoreCase(sessionkey)) {
				sb.append("\"Custom Message Generated basis Input\"");
				MessageUtility.updateMessage(sb, 5000, "sessionkey is null");
				return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
			}

			MongoDBConnection mongoSingle = null;
			MongoDatabase mongodb = null;
			MongoCollection<Document> table = null;
			int code = 0;
			if (dbName != null) {
				code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
			} else {
				code = CommonUtility.validateAuthenticationKey(sessionkey);
			}
			try {
				if (code == 0) {
					mongoSingle = new MongoDBConnection(dbParamJson);
					 if(dbName!=null) {
						 mongodb = mongoSingle.getMongoDB(dbName);
					 }else {
					 mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
					 }
					Map<String, String> hDeviceCategoryName = TimeSeriesUtility.loadDeviceCategoryName();
					String deviceCategoryName=null;
					BasicDBObject jsonDocument=null;
					if (json != null) {
						jsonDocument = (BasicDBObject) JSON.parse(json);
						deviceCategoryName=jsonDocument.getString("deviceCategoryName");
					}
					Document dateConversion = new Document(); 
					Document dateFormatDoc = new Document();
					Document timeConversion = new Document();
					Document timeConversionDoc = new Document();
					Document matchQuery=new Document();
					Document searchQuery=new Document();
					searchQuery.put("asset_id", jsonDocument.getString("asset_id"));
					Document inQuery=new Document();
					inQuery.put("$in", jsonDocument.get("device_id"));
					searchQuery.put("device_id", inQuery);
					matchQuery.put("$match", searchQuery);
					Document sortQuery=new Document();
					Document groupByFields=new Document();
					Document groupDocument=new Document();
					if("Temperature".equalsIgnoreCase(deviceCategoryName)) {
						//sortQuery.put("measurement_date", MongoDBConstants.MAX_ORDER_BY);
						sortQuery.put("measurement_date", MongoDBConstants.MIN_ORDER_BY);
						dateFormatDoc.put("format", "%Y-%m-%d");
						dateFormatDoc.put("date", "$measurement_date");
						timeConversionDoc.put("format", "%H:%M:%S");
						timeConversionDoc.put("date", "$measurement_date");
						groupByFields.put("asset_id", "$asset_id");
						groupByFields.put("device_id", "$device_id");
						Document messageObject=new Document();
						messageObject.put("$last", "$measurement_date");
						groupDocument.put("measurement_date", messageObject);
						groupDocument.put("_id", groupByFields);
						
					}else {
						//sortQuery.put("message_time", MongoDBConstants.MAX_ORDER_BY);
						sortQuery.put("message_time", MongoDBConstants.MIN_ORDER_BY);
						dateFormatDoc.put("format", "%Y-%m-%d");
						dateFormatDoc.put("date", "$message_time");
						timeConversionDoc.put("format", "%H:%M:%S");
						timeConversionDoc.put("date", "$message_time");
						groupByFields.put("asset_id", "$asset_id");
						groupByFields.put("device_id", "$device_id");
						Document messageObject=new Document();
						messageObject.put("$last", "$message_time");
						groupDocument.put("message_time", messageObject);
						groupDocument.put("_id", groupByFields);
					}
					Document sortQueryObject=new Document();
					sortQueryObject.put("$sort",sortQuery);
					Document projectQuery=new Document();
					Document projectFields=new Document();
					dateConversion.put("$dateToString", dateFormatDoc);
					projectFields.put("last_reported_date", dateConversion);
					
					timeConversion.put("$dateToString", timeConversionDoc);
					projectFields.put("time", timeConversion);
					projectFields.put("asset_id", "$_id.asset_id");
					projectFields.put("device_id", "$_id.device_id");
					
					projectQuery.put("$project", projectFields);
					Document limitQueryObject=new Document();
					limitQueryObject.put("$limit",1);
					
					table = mongodb.getCollection(hDeviceCategoryName.get(deviceCategoryName)
							+ jsonDocument.getString("site_id") + MongoDBConstants.MONGO_COLLECTION_SUFFIX);
					Document groupQuery=new Document();
					
					/*Document latestGroupFields=new Document();
					latestGroupFields.put("$first", "$$ROOT");
					groupDocument.put("latest", latestGroupFields);*/
					groupQuery.put("$group", groupDocument);
					System.out.println("groupByFields...."+groupQuery.toJson());
					
					//AggregateIterable<Document> iterable=table.aggregate(Arrays.asList(sortQueryObject,groupQuery,matchQuery,projectQuery)).allowDiskUse(true);
					AggregateIterable<Document> iterable=table.aggregate(Arrays.asList(sortQueryObject,matchQuery,groupQuery,projectQuery)).allowDiskUse(true);
					//AggregateIterable<Document> iterable=table.aggregate(Arrays.asList(matchQuery,sortQueryObject,projectQuery));
					Document doc = new Document();
					doc.put("status", "Success");
					doc.put("statusCode", 0);
					doc.put("statusMessage", "getLatestTimeStamp sent successfully");
				    List<Document> list=new ArrayList<Document>();
					for(Document row:iterable) {
						row.remove("_id");
						Document deviceDoc=new Document();
						deviceDoc.put("last_reported_time", row.getString("last_reported_date")+","+row.getString("time"));
						row.remove("last_reported_date");
						row.remove("time");
						deviceDoc.putAll((Map)row);
						list.add(deviceDoc);
					}
					if(list.size()>0) {
						doc.put("result", list);
					}else {
						doc.put("status", "No Data Found");
						doc.put("last_reported_time", "N/A");
						doc.put("statusCode", 101);
						doc.put("statusMessage", "No Data Found");
						doc.put("result", list);
					}
					return new ResponseEntity<String>(com.mongodb.util.JSON.serialize(doc), HttpStatus.OK);
				} else if (code == 1001) {
					sb.append("\"Invalid key\"");
					MessageUtility.updateMessage(sb, code, "Session Invalid");
				} else if (code == 2001) {
					sb.append("\"MetaData Connection\"");
					MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
				}
				return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				sb = new StringBuilder();
				sb.append("{");
				sb.append("\"status\":");
				sb.append("\"MetaData Connection\"");
				MessageUtility.updateMessageWithErrors(sb, 2001, "MetaData Connection Failure", e);
				return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
			} finally {
				if (mongoSingle != null) {
					CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
				}
				
			}
		}
	 
		
	
}
