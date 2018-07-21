package com.kavi.services.controller;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.apache.log4j.Logger;
import org.bson.BSONObject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.kavi.rule.dataobjects.RuleDO;
import com.kavi.common.utility.CommonUtility;
import com.kavi.common.utility.MessageUtility;
import com.kavi.rule.utility.RuleUtility;
import com.mongo.db.MongoDBConnection;
import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

@RestController
public class RuleController {

	private final static Logger logger = Logger.getLogger(RuleController.class);
	
	@RequestMapping("/")
	public String welcome() {
		return "Welcome to MicroRuleService";
	}
	
	/**
	 * 
	 * @param req
	 * @param formParams
	 * @return
	 */

	@RequestMapping(value = "/listRuleAttributes", method = RequestMethod.POST)
	public ResponseEntity<String> listRuleAttributes(String sessionkey,String deviceCategoryName,String json,String dbParamJson) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		String dbName= null;
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
		mongoSingle = new MongoDBConnection(dbParamJson);
		MongoCollection<Document> mappingtable = null;
		if (dbName != null) {
			mongodb = mongoSingle.getMongoDB(dbName);
		} else {
			mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
		}
		int code = 0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		try {
			Document measuresList = null;
			if (code == 0) {
				BasicDBObject jsonDocument = null;
				JSONArray fieldArray = new JSONArray();
				if (json != null) {
					jsonDocument = (BasicDBObject) JSON.parse(json);
				}
				if (deviceCategoryName != null) {
					Document searchQuery = new Document();
					Document matchQuery = new Document();
					Document mappingDoc = new Document();
					AggregateIterable<Document> mappingiterable = null;
					mappingtable = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_DEVICE_PARAM_MAPPING);
					BasicDBObject unwindMapping = new BasicDBObject("$unwind","$mapping");
					searchQuery.put("device_category", deviceCategoryName+"_ui");
					searchQuery.put("mapping.notification_display_flag",true);
					matchQuery.put("$match", searchQuery);
					mappingiterable = mappingtable.aggregate(Arrays.asList(unwindMapping,matchQuery));
					for (Document row : mappingiterable) {
						measuresList = (Document) row.get("mapping");
						String units = (String) measuresList.get("units");
							mappingDoc = new Document();
							mappingDoc.append("json_path", measuresList.get("id"));
							//mappingDoc.append("ui_display_name", measuresList.get("display_name"));
							if(units != null && units.length()>0) {
								mappingDoc.append("ui_display_name", measuresList.get("display_name")+" ["+units+"]");
							}
							else {
								mappingDoc.append("ui_display_name", measuresList.get("display_name"));
							}
							mappingDoc.append("data_type", measuresList.get("data_type"));
							//mappingDoc.append("unit", measuresList.get("units"));
							 mappingDoc.append("parameter_type", measuresList.get("parameter_type"));
							fieldArray.add(mappingDoc);
					}
				}
				Document doc = new Document();
				doc.put("status", "Success");
				doc.put("statusCode", code);
				doc.put("statusMessage", "listRuleAttributes done");
				doc.put("result", fieldArray);
				return new ResponseEntity<String>(doc.toJson(), HttpStatus.OK);
			} else if (code == 1001) {
				sb.append("\"Invalid key\"");
				MessageUtility.updateMessage(sb, code, "Session Invalid");
			} else if (code == 2001) {
				sb.append("\"MetaData Connection\"");
				MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
			}
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} catch (Exception e) {
			sb = new StringBuilder();
			sb.append("{");
			sb.append("\"status\":");
			sb.append("\"MetaData Connection\"");
			MessageUtility.updateMessageWithErrors(sb, 2001, "MetaData Connection Failure", e);
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, mappingtable);
			}
		}
	}

	@RequestMapping(value = "/listNotificationGroups", method = RequestMethod.POST)
	public ResponseEntity<String> listNotificationGroups(String sessionkey,String dbParamJson) {
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
		mongoSingle = new MongoDBConnection(dbParamJson);
		MongoCollection<Document> table = null;
		if (dbName != null) {
			mongodb = mongoSingle.getMongoDB(dbName);
		} else {
			mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
		}
		int code = 0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		try {
			if (code == 0) {
				JSONArray array = new JSONArray();
				AggregateIterable<Document> iterable = null;
				table = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_NOTIFICATION_GROUP);
				iterable = table.aggregate(Arrays.asList());
				for (Document row : iterable) {
					row.put("document_id", row.get("_id").toString());
					row.remove("_id");
					//row.remove("topics");
					array.add(row);
				}
				Document doc = new Document();
				doc.put("status", "Success");
				doc.put("statusCode", code);
				doc.put("statusMessage", "listNotificationGroups done");
				doc.put("result", array);
				return new ResponseEntity<String>(doc.toJson(), HttpStatus.OK);
			} else if (code == 1001) {
				sb.append("\"Invalid key\"");
				MessageUtility.updateMessage(sb, code, "Session Invalid");
			} else if (code == 2001) {
				sb.append("\"MetaData Connection\"");
				MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
			}
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} catch (Exception e) {
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
	
	@RequestMapping(value = "/upsertNotificationGroups", method = RequestMethod.POST)
	public ResponseEntity<String> upsertNotificationGroups(String sessionkey,String json,String dbParamJson) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		String statusMessage = "";
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
		mongoSingle = new MongoDBConnection(dbParamJson);
		MongoCollection<Document> table = null;
		Document auditDoc = new Document();
		if (dbName != null) {
			mongodb = mongoSingle.getMongoDB(dbName);
		} else {
			mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
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
				Map<String, String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
				CommonUtility.updateAuditObject(auditDoc, linkedUserGroupDetails, jsonDocument.getString("type"));
				jsonDocument.put("audit", auditDoc.get("audit"));
				Document updateQuery = new Document();
				BasicDBObject updateData = new BasicDBObject();
				BasicDBObject command = new BasicDBObject();				
				BasicDBObject object = new BasicDBObject();
				table = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_NOTIFICATION_GROUP);
				Integer objectexists = 0;	
				if (jsonDocument.getString("type").equalsIgnoreCase("create") ) {
					objectexists = RuleUtility.objectExists("notificationGroup",jsonDocument.getString("group_name"),mongodb);	
					if(objectexists == 0) {
						jsonDocument.put("group_id", CommonUtility.generateNotificationGroupId(jsonDocument.getString("group_name")));
						jsonDocument.remove("type");
						table.insertOne(new Document(jsonDocument));
						statusMessage = "Notification Group created successfully";
					}
					else {
						statusMessage = jsonDocument.getString("group_name") + " notification group already exists";
						code = 5001;
						
					}
				}
				if (jsonDocument.getString("type") != null && jsonDocument.getString("type").equalsIgnoreCase("edit") ) {
					/*String old_name = jsonDocument.getString("old_name");
					if (old_name != null && old_name.length() >0 && !(old_name.equalsIgnoreCase(jsonDocument.getString("group_name")))) {
						objectexists = RuleUtility.objectExists("notificationGroup",jsonDocument.getString("group_name"),mongodb);	
						if(objectexists == 0) {*/
							updateQuery.put("_id", new ObjectId(jsonDocument.getString("document_id")));
							jsonDocument.remove("type");
							jsonDocument.remove("document_id");
							updateData.putAll((Map) jsonDocument);
							command.put("$set", updateData);
							table.updateOne(updateQuery, command);
							statusMessage = "Notification Group updated successfully";
				/*		}
					}*/
					
				}
				if (jsonDocument.getString("type") != null && jsonDocument.getString("type").equalsIgnoreCase("delete") ) {
					object.put("_id", new ObjectId(jsonDocument.getString("document_id")));
					table.deleteOne(new Document(object));
					statusMessage = "Notification Group deleted successfully";
				}
				Document doc = new Document();
				doc.put("status", "Success");
				doc.put("statusCode", code);
				doc.put("statusMessage", statusMessage);
				return new ResponseEntity<String>(doc.toJson(), HttpStatus.OK);
			} else if (code == 1001) {
				sb.append("\"Invalid key\"");
				MessageUtility.updateMessage(sb, code, "Session Invalid");
			} else if (code == 2001) {
				sb.append("\"MetaData Connection\"");
				MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
			}
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} catch (Exception e) {
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
	

	@RequestMapping(value = "/listRules", method = RequestMethod.POST)
	public ResponseEntity<String> listRules(String sessionkey,String deviceCategoryName,String json,String dbParamJson) {
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
		mongoSingle = new MongoDBConnection(dbParamJson);
		MongoCollection<Document> table = null;
		if (dbName != null) {
			mongodb = mongoSingle.getMongoDB(dbName);
		} else {
			mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
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
				Map<String, String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
				JSONArray array = new JSONArray();
				Document searchQuery = new Document();
				Document matchQuery = new Document();
				AggregateIterable<Document> iterable = null;
				table = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_RULE);
				searchQuery.put("rule_filter.company_id",linkedUserGroupDetails.get("organizationName"));
				searchQuery.put("rule_filter.device_category", deviceCategoryName);
				if (json != null) {
					BasicDBObject inQuery = new BasicDBObject();
					inQuery.put("$in", jsonDocument.get("siteId"));
					searchQuery.put("rule_filter.site.id", inQuery);
				}
				if (deviceCategoryName != null && !(linkedUserGroupDetails.get("role").equalsIgnoreCase("Administrator")) ) {
					List<Document> sourceObj = new ArrayList<Document>();
					sourceObj.add(new Document("audit.created_by", linkedUserGroupDetails.get("userId")));
					sourceObj.add(new Document("audit.modified_by", linkedUserGroupDetails.get("userId")));
					searchQuery.put("$or", sourceObj);
				}
				matchQuery.put("$match", searchQuery);
				iterable = table.aggregate(Arrays.asList(matchQuery));
				DateFormat df = CommonUtility.getDateFormat("Month");
				for (Document row : iterable) {
					Document ruleDoc = new Document();
					Document generalDoc = (Document) row.get("general");
					Document auditDoc = (Document) row.get("audit");
					ruleDoc.put("rule_id", row.get("_id").toString());
					ruleDoc.put("rule_name", generalDoc.get("name"));
					ruleDoc.put("rule_description", generalDoc.get("description"));
					ruleDoc.put("active_flag", generalDoc.get("active_flag"));
					ruleDoc.put("created_by", auditDoc.get("created_by"));
					ruleDoc.put("created_date", df.format(auditDoc.get("created_date")));
					array.add(ruleDoc);
				}
				Document doc = new Document();
				doc.put("status", "Success");
				doc.put("statusCode", code);
				doc.put("statusMessage", "listRules done");
				doc.put("result", array);
				return new ResponseEntity<String>(doc.toJson(), HttpStatus.OK);
			} else if (code == 1001) {
				sb.append("\"Invalid key\"");
				MessageUtility.updateMessage(sb, code, "Session Invalid");
			} else if (code == 2001) {
				sb.append("\"MetaData Connection\"");
				MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
			}
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} catch (Exception e) {
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

	@RequestMapping(value = "/getRules", method = RequestMethod.POST)
	public ResponseEntity<String>  getRules(String sessionkey,String ruleId,String dbParamJson) {
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
		mongoSingle = new MongoDBConnection(dbParamJson);
		MongoCollection<Document> table = null;
		if (dbName != null) {
			mongodb = mongoSingle.getMongoDB(dbName);
		} else {
			mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
		}
		int code = 0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		try {
			if (code == 0) {
				JSONArray array = new JSONArray();
				Document searchQuery = new Document();
				Document matchQuery = new Document();
				AggregateIterable<Document> iterable = null;
				table = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_RULE);
				searchQuery.put("general.id",ruleId);
				matchQuery.put("$match", searchQuery);
				Document outputDoc = new Document();
				iterable = table.aggregate(Arrays.asList(matchQuery));
				List<RuleDO> parentFinder=new ArrayList<RuleDO>();
				for (Document row : iterable) {
					//array.add(row);
					DBObject jsonDocument = (BasicDBObject) JSON.parse(row.toJson());
					BasicDBList conditionList = (BasicDBList) jsonDocument.get("condition");
					if(conditionList!=null && conditionList.size()>0){
						for(int conditionIterate = 0; conditionIterate<conditionList.size(); conditionIterate++) 
			            { 
							BasicDBObject dbObject=(BasicDBObject) conditionList.get(conditionIterate);
							BasicDBList groupDetailsList=(BasicDBList) dbObject.get("group_details");
							Map<String,BasicDBList> hCompoundDetailsArray=new WeakHashMap<String,BasicDBList>();
							Map<String,RuleDO> hGroupDetails=new WeakHashMap<String,RuleDO>();
							Map<String,String> lGroupTree= new WeakHashMap<String,String>();
				 			if(groupDetailsList!=null && groupDetailsList.size()>0){
								for(int iterate = 0; iterate<groupDetailsList.size(); iterate++) 
					            { 
									String groupName=(String) ((BSONObject) groupDetailsList.get(iterate)).get("group_name");
									RuleDO oRulesDO=new RuleDO();
									oRulesDO.setGroupName(groupName);
									oRulesDO.setGroupOperator((String) ((BSONObject) groupDetailsList.get(iterate)).get("group_operator"));
									oRulesDO.setParentGroup((String) ((BSONObject) groupDetailsList.get(iterate)).get("parent_group"));
									oRulesDO.setDataType((String) ((BSONObject) groupDetailsList.get(iterate)).get("datatype"));
									BasicDBList compoundObject = (BasicDBList) ((BSONObject) groupDetailsList.get(iterate)).get("rule_element");
									hCompoundDetailsArray.put(groupName, compoundObject);
									hGroupDetails.put(groupName, oRulesDO);
									if(oRulesDO.getParentGroup()==null|| "".equalsIgnoreCase(oRulesDO.getParentGroup())){
										parentFinder.add(oRulesDO);
									}
									else{
										lGroupTree.put(groupName,oRulesDO.getParentGroup());
									}
					            }
								BasicDBObject ruleObject=RuleUtility.constructRuleDetails(parentFinder,lGroupTree,hCompoundDetailsArray,hGroupDetails,dbObject);
								//array.add(ruleObject);
								outputDoc.put("condition",ruleObject);
								outputDoc.put("general",jsonDocument.get("general"));
								outputDoc.put("action",jsonDocument.get("action"));
								outputDoc.put("rule_filter",jsonDocument.get("rule_filter"));
								outputDoc.put("audit",jsonDocument.get("audit"));
								array.add(outputDoc);
							}
			            }
					}
				}
				
				
				Document doc = new Document();
				doc.put("status", "Success");
				doc.put("statusCode", code);
				doc.put("statusMessage", "getRules done");
				doc.put("result", array);
				return new ResponseEntity<String>(doc.toJson(), HttpStatus.OK);
			} else if (code == 1001) {
				sb.append("\"Invalid key\"");
				MessageUtility.updateMessage(sb, code, "Session Invalid");
			} else if (code == 2001) {
				sb.append("\"MetaData Connection\"");
				MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
			}
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} catch (Exception e) {
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
	
	@RequestMapping(value = "/upsertRules", method = RequestMethod.POST)
	public ResponseEntity<String>  upsertRules(String sessionkey,String json,String type,String notificationGroupId,
			String ruleName,String ruleId,String notificationTopicName,String notificationType,String dbParamJson) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		String dbName = null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		MongoDBConnection mongoSingle = null;
		String statusMessage = "";
		MongoDatabase mongodb = null;
		mongoSingle = new MongoDBConnection(dbParamJson);
		MongoCollection<Document> table = null;
		if (dbName != null) {
			mongodb = mongoSingle.getMongoDB(dbName);
		} else {
			mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
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
				Document resultdoc = new Document();
				Document doc = new Document();
				Document updateQuery = new Document();
				BasicDBObject command = new BasicDBObject();
				BasicDBObject object = new BasicDBObject();
				AggregateIterable<Document> iterable = null;
				String existingNotificationGroupId = "";
				Integer objectexists = 0;	
				if (json != null && json.length() > 0 ) {
					jsonDocument = (BasicDBObject) JSON.parse(json);
					RuleUtility.generateGroupDefinitionArray(jsonDocument);
					doc.putAll((Map)jsonDocument);
				}
				Map<String, String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
				CommonUtility.updateAuditObject(doc, linkedUserGroupDetails, type);
				table = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_RULE);
				if (type.equalsIgnoreCase("create") ) {
					if (ruleId == null || ruleId.trim().equalsIgnoreCase("")) {
					objectexists = RuleUtility.objectExists("rule",ruleName,mongodb);	
						if(objectexists == 0) {
							table.insertOne(doc);
							ObjectId id = (ObjectId) doc.get("_id");
							updateQuery.put("_id", id);
							ruleId = id.toString();
							Document generalUpdate=new Document();
							generalUpdate.put("general.id", id.toString());
							command.put("$set", generalUpdate);
							table.updateOne(updateQuery, command);
							statusMessage = "Rule created successfully";
						}
						else {
							statusMessage = ruleName + " already exists";
							code = 5001;
						}
					}
					else {
						updateQuery.put("general.id", ruleId);
						command.put("$set", doc);
						table.updateOne(updateQuery, command);
						statusMessage = "Rule updated successfully";
					}
					RuleUtility.upsertRuleIdForNotificationGroup(ruleId, notificationGroupId, mongodb);
				}
				if (type.equalsIgnoreCase("activate") ) {
					Document generalUpdate=new Document();
					ObjectId id = null;
					if (ruleId == null || ruleId.trim().equalsIgnoreCase("")) {
						objectexists = RuleUtility.objectExists("rule",ruleName,mongodb);	
						if(objectexists == 0) {
							table.insertOne(doc);
							id = (ObjectId) doc.get("_id");
							updateQuery.put("_id", id);
							ruleId = id.toString();
							generalUpdate.put("general.id", ruleId);
							generalUpdate.put("general.notification_group_id", notificationGroupId);
							command.put("$set", generalUpdate);
							table.updateOne(updateQuery, command);
							if(notificationType.equalsIgnoreCase("text") || notificationType.equalsIgnoreCase("both")) {
								RuleUtility.upsertTopicForNotificationGroup(notificationGroupId, ruleId, mongodb, notificationType,ruleId);
							}
							generalUpdate.put("general.active_flag", true);
							generalUpdate.put("general.notification_type", notificationType);
							command.put("$set", generalUpdate);
							table.updateOne(updateQuery, command);
							statusMessage = "Rule created successfully";
						}
						else {
							statusMessage = ruleName + " already exists";
							code = 5001;
						}
					}
					else {
						updateQuery.put("general.id", ruleId);
						BasicDBObject general = (BasicDBObject) doc.get("general");
						general.put("active_flag", true);
						general.put("notification_group_id", notificationGroupId);
						general.put("notification_type", notificationType);
						command.put("$set", doc);
						table.updateOne(updateQuery, command);
						RuleUtility.upsertTopicForNotificationGroup(notificationGroupId, ruleId, mongodb, notificationType,ruleId);
						statusMessage = "Rule activated successfully";
					}
					if("email".equalsIgnoreCase(notificationType)){
						RuleUtility.deleteTopicFromNotificationGroup(notificationTopicName, notificationGroupId, mongodb, ruleId);
					}
					//statusMessage = "Rule updated successfully";
					RuleUtility.upsertRuleIdForNotificationGroup(ruleId, notificationGroupId, mongodb);
					
				}
				if (type.equalsIgnoreCase("deactivate")) {
					updateQuery.put("general.id", ruleId);
					Document generalUpdate=new Document();
					generalUpdate.put("general.active_flag", false);
					command.put("$set", generalUpdate);
					table.updateOne(updateQuery, command);
					statusMessage = "Rule deactivated successfully";
				}
				if (type.equalsIgnoreCase("delete")) {
					object.put("general.id", ruleId);
					RuleUtility.deleteTopicFromNotificationGroup(notificationTopicName, notificationGroupId, mongodb,ruleId);
					table.deleteOne(new Document(object));
					statusMessage = "Rule deleted successfully";
				}
				resultdoc.put("status", "Success");
				resultdoc.put("statusCode", code);
				resultdoc.put("statusMessage", statusMessage);
				resultdoc.put("ruleId",ruleId);
				return new ResponseEntity<String>(resultdoc.toJson(),HttpStatus.OK);
			} else if (code == 1001) {
				sb.append("\"Invalid key\"");
				MessageUtility.updateMessage(sb, code, "Session Invalid");
			} else if (code == 2001) {
				sb.append("\"MetaData Connection\"");
				MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
			}
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} catch (Exception e) {
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
