package com.kavi.services.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.kavi.common.utility.CommonUtility;
import com.kavi.common.utility.MessageUtility;
import com.kavi.twoway.utility.TwowayCommUtility;
import com.mongo.db.MongoDBConnection;
import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

@RestController
public class TwowayCommController {

	@RequestMapping("/")
	public String welcome() {
		return "Welcome to MicroTwowayCommService";
	}
	
	@RequestMapping(value = "/requestTwowayCommunicationData", method = RequestMethod.POST)
	public ResponseEntity<String>  requestTwowayCommunicationData(String sessionkey,String category,String json,String dbParamJson) {
		
		StringBuilder sb = new StringBuilder();
		String dbName= null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		}
		sb.append("{");
		sb.append("\"status\":");
		if (sessionkey == null || "".equalsIgnoreCase(sessionkey)) {
			sb.append("\"Custom Message Generated basis Input\"");
			MessageUtility.updateMessage(sb, 5000, "sessionkey is null");
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		}
		String topicName = "twoway";
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		MongoCollection<Document> paramLogTable = null;
		Iterable<Document> iterable = null;
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS'Z'");
		dateFormatLocal.setTimeZone(TimeZone.getTimeZone("UTC"));
		mongoSingle = new MongoDBConnection(dbParamJson);
		int code = 0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		String queueStatus = "";
		try {
			if (code == 0) {
				BasicDBObject jsonDocument = null;
				if (json != null) {
					jsonDocument = (BasicDBObject) JSON.parse(json);
				}
				if (dbName != null) {
					mongodb = mongoSingle.getMongoDB(dbName);
				} else {
					mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				}
				table = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_TWO_WAY_COMMUNICATION);
				paramLogTable = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_USER_DEFINED_PARAM_LOG);
				Map<String, String> linkedUserGroupDetails = CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
				Document updateQuery = new Document();
				JSONArray statusArray = new JSONArray();
				BasicDBObject inQuery = new BasicDBObject();
				BasicDBObject project = new BasicDBObject();
				Document searchQuery = new Document();
				Document projectQuery = new Document();
				Document matchQuery = new Document();
				ArrayList<Document> device_list_info = new ArrayList<Document>();
				Document device_info = new Document();
				Document statusDoc = new Document();
				Properties configProperties = new Properties();
	            configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,TwowayCommUtility.getKafkaURL());
	            configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.ByteArraySerializer");
	            configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
				String message_id = TwowayCommUtility.getId("twc"+new Date().getTime());
				inQuery.put("$in", jsonDocument.get("device_list"));
				searchQuery.put(MongoDBConstants.DEVICE_ID, inQuery);
				searchQuery.put("active_flag", true);
				matchQuery.put("$match", searchQuery);
				project.putAll(TwowayCommUtility.getSpecificDetailsForTakeMeasurement(category));
				projectQuery.put("$project", project);
				iterable = paramLogTable.aggregate(Arrays.asList(matchQuery, projectQuery));
				for (Document row : iterable) {
					Document db_parameters = (Document) row.get("db_parameters");
					device_info = new Document();
					device_info.put("message_id",message_id);
					device_info.put("category", category);
					device_info.put("device_id", row.get("device_id"));
					device_info.put("ip_address", db_parameters.get("ip_address"));
					device_info.put("ip_port", db_parameters.get("ip_port"));
					device_info.put("overall_status", "open");
					device_info.put("requested_by", linkedUserGroupDetails.get("userId"));
					device_list_info.add(device_info);
				}
				table.insertMany(device_list_info);
				Document queueDoc = new Document();
				queueDoc.put("result", device_list_info);
				
				Producer producer = new KafkaProducer<String, String>(configProperties);
	            ProducerRecord<String, String> rec = new ProducerRecord<String, String>(topicName, queueDoc.toJson());
	            org.apache.kafka.clients.producer.RecordMetadata obj;
					try {
						obj = (RecordMetadata) producer.send(rec).get();
						if (obj.checksum() > 1) {
							queueStatus = "QueueSuccess";
							producer.close();
						}
					} catch (Exception e) {
						queueStatus = "QueueFailure";
						searchQuery = new Document();
						searchQuery.put("message_id", message_id);
						table.deleteMany(searchQuery);
						Document resultDoc = new Document();
						resultDoc.put("status", "Failure");
						resultDoc.put("statusCode", 5000);
						resultDoc.put("statusMessage", "Push to Kafka Queue Failed");
						producer.close();
						return new ResponseEntity<String>(resultDoc.toJson(),HttpStatus.OK);
					}
				
				updateQuery.put("message_id", message_id);
				BasicDBObject updateData = new BasicDBObject();
				BasicDBObject command = new BasicDBObject();
				updateData.put("requested_date", dateFormatLocal.format(new Date()));
				statusDoc.put("application", "webservice");
				statusDoc.put("status_cmnts", queueStatus);
				statusDoc.put("status_datetime", dateFormatLocal.format(new Date()));
				statusArray.add(statusDoc);				
				updateData.put("status",statusArray);
				command.put("$set", updateData);
				table.updateMany(updateQuery, command);
				
				Document resultDoc = new Document();
				resultDoc.put("status", "Success");
				resultDoc.put("statusCode", code);
				resultDoc.put("statusMessage", "Two way communication for "+category+" has been sent for execution");
				producer.close();
				return new ResponseEntity<String>(resultDoc.toJson(),HttpStatus.OK);
				
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
