package com.kavi.rule.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.bson.BSONObject;
import org.bson.Document;
import org.json.simple.JSONArray;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.kavi.common.constants.CommonConstants;
import com.kavi.common.utility.CommonUtility;
import com.kavi.rule.dataobjects.RuleDO;
import com.mongo.db.MongoDBConnection;
import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;

public class RuleUtility {
	
	private final static Logger logger = Logger.getLogger(RuleUtility.class);
	
	/**
	 * 
	 * @param groupId
	 * @param mongodb
	 * @param notificationType
	 * @return
	 */
	private static List<RuleDO> getGroupMemberDetails(String notificationGroupId,MongoDatabase mongodb,String notificationType){
		
		MongoCollection<Document> table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_NOTIFICATION_GROUP);
		List<RuleDO> listRuleDO=new ArrayList<RuleDO>();	
		Document searchQuery = new Document();
		searchQuery.put("group_id", notificationGroupId); //TODO Need Index
		FindIterable<Document> sessioncursor = table.find(searchQuery);
		if(sessioncursor.iterator().hasNext()){
			Document object=(Document)sessioncursor.iterator().next();
			List<Document> groupMemberList  =(ArrayList)object.get("group_members");
			if(groupMemberList!=null && groupMemberList.size()>0){
				for(int groupMemberIterate=0;groupMemberIterate<groupMemberList.size();groupMemberIterate++){
					Document grpMemberObject=(Document)groupMemberList.get(groupMemberIterate);
					if("text".equalsIgnoreCase(notificationType) || "both".equalsIgnoreCase(notificationType)){
						if(grpMemberObject.getString("phone")!=null && grpMemberObject.getString("phone").length()>0){
							RuleDO ruleDO=new RuleDO();
							ruleDO.setUserId(grpMemberObject.getString("user_id"));
							ruleDO.setUserPhone(grpMemberObject.getString("phone"));
							listRuleDO.add(ruleDO);
						}
					}
				/*	else if("email".equalsIgnoreCase(notificationType)){
						RuleDO ruleDO=new RuleDO();
						ruleDO.setUserId(grpMemberObject.getString("user_id"));
						ruleDO.setUserEmail(grpMemberObject.getString("user_email"));
						listRuleDO.add(ruleDO);
					
					}
					else{
						RuleDO ruleDO=new RuleDO();
						ruleDO.setUserId(grpMemberObject.getString("user_id"));
						ruleDO.setUserPhone(grpMemberObject.getString("phone"));
						ruleDO.setUserEmail(grpMemberObject.getString("user_email"));
						listRuleDO.add(ruleDO);
					}*/
				}
			}
			
		}
		return listRuleDO;
		
	}
	
	
	/**
	 * 
	 * @param topicName
	 * @param listRuleDO
	 * @param notificationType
	 */
	public static String createTopic(String s3AccessKey,String s3SecretAccessKey,String topicName,List<RuleDO> listRuleDO,String notificationType){
		AWSCredentials credentials = new BasicAWSCredentials(
				s3AccessKey, 
				s3SecretAccessKey);  
		AmazonSNSClient snsClient = new AmazonSNSClient(credentials);	
		
		CreateTopicRequest createTopicRequest = new CreateTopicRequest(topicName);
		CreateTopicResult createTopicResult = snsClient.createTopic(createTopicRequest);
		DeleteTopicRequest deleteTopicRequest = new DeleteTopicRequest(createTopicResult.getTopicArn());
		snsClient.deleteTopic(deleteTopicRequest);
		createTopicResult = snsClient.createTopic(createTopicRequest);
		for(RuleDO ruleDO:listRuleDO){
			if("text".equalsIgnoreCase(notificationType) || "both".equalsIgnoreCase(notificationType)){
				if(ruleDO.getUserPhone() !=null && ruleDO.getUserPhone().length()>0){
				SubscribeRequest subRequest = new SubscribeRequest(createTopicResult.getTopicArn(), "sms", ruleDO.getUserPhone());
				snsClient.subscribe(subRequest);
				}
			}
		/*	else if("email".equalsIgnoreCase(notificationType)){
				SubscribeRequest subRequest = new SubscribeRequest(createTopicResult.getTopicArn(), "email", ruleDO.getUserEmail());
				snsClient.subscribe(subRequest);
			}*/
		}
		return createTopicResult.getTopicArn();
		
	}
	
	/**
	 * 
	 * @param jsonObject
	 * @return
	 */
	  public static JSONArray generateGroupDefinitionArray(DBObject jsonObject){
	 	 
		JSONArray finalArray=new JSONArray();
		JSONArray array=new JSONArray();
	 	BasicDBList conditionList=(BasicDBList) jsonObject.get("condition");
	 	if(conditionList!=null && conditionList.size()>0){
	 		for(int iterate=0;iterate<conditionList.size();iterate++){
	 			BasicDBObject dbObject=(BasicDBObject) conditionList.get(iterate);
	 			BasicDBObject definitionObject=(BasicDBObject) dbObject.get("condition_specification");
	 			//definitionObject=(BasicDBObject)definitionObject.get("definition");
	 			array=new JSONArray();
	 		 	displayCompoundDetails(definitionObject,"rule_element",array);
	 		 	dbObject.put("group_details", array);
	 		 	dbObject.removeField("condition_specification");
	 		 	finalArray.add(dbObject);
	 		 	/*BasicDBObject basicObject= new BasicDBObject();
	 		 	basicObject.put("condition",array);
	 		 	finalArray.add(basicObject);*/
	 		 
	 		}
			
	 	}
	 	return finalArray;
	 }
	 
	  private static  void displayCompoundDetails(DBObject dbobject,String element,JSONArray array){
			BasicDBList comp_properties = (BasicDBList) dbobject.get(element);
			BasicDBObject document=new BasicDBObject();
			document.put("group_name", dbobject.get("group_name"));
			document.put("group_operator", dbobject.get("group_operator"));
			document.put("parent_group", dbobject.get("parent_group"));
			document.put("datatype",dbobject.get("datatype"));
			JSONArray compArray=new JSONArray();
			for(int iterate = 0; iterate<comp_properties.size(); iterate++){ 
			String dataElementId=(String) ((BSONObject) comp_properties.get(iterate)).get("definition_item_id");
				if(dataElementId!=null){
				compArray.add(comp_properties.get(iterate));
				}
			}
			document.put(element,compArray);
			array.add(document);
			if(comp_properties!=null){
				for(int iterate = 0; iterate<comp_properties.size(); iterate++){ 
					DBObject obj1=(DBObject) ((BSONObject) comp_properties.get(iterate));
					if(obj1.get(element)!=null){
						displayCompoundDetails(obj1,element,array);
	         	}	
	         }
		   }
	 }
	
	  
	  /**
	   * 
	   * @param alParentDO
	   * @param alChildDO
	   * @param lGroupTree
	   * @param hCompoundDetailsArray
	   * @param hGroupDetails
	   * @param obj
	   * @return
	   */
	  public static BasicDBObject constructRuleDetails(List<RuleDO> alParentDO,
	 			Map<String,String> lGroupTree,Map<String,BasicDBList> hCompoundDetailsArray,Map<String,RuleDO> hGroupDetails,BasicDBObject obj)
	 	{
	 	    obj.removeField("group_details");
	 		for(RuleDO objDO:alParentDO) 
	 		{		
	 				obj.put("group_name", objDO.getGroupName());
	 				obj.put("parent_group", objDO.getParentGroup());
	 				obj.put("group_operator", objDO.getGroupOperator());
	 				obj.put("datatype", objDO.getDataType());
	 				BasicDBList compoundList=hCompoundDetailsArray.get(objDO.getGroupName());
	 				obj.put("rule_element", compoundList);
	 		}
	 		Set<String> groupKeys = lGroupTree.keySet();
	 		for(String childGroup:groupKeys){
	 			findCompoundDetailsGroupName(obj,"rule_element",null,childGroup,hCompoundDetailsArray,hGroupDetails,lGroupTree.get(childGroup));
	 		}
	 		modifyJson(obj);
	 		return obj;
	 		
	 	}
	  
	  private static  void findCompoundDetailsGroupName(DBObject dbobject,String element,JSONArray array,String childGroup,
	 			Map<String,BasicDBList> hCompoundDetailsArray,Map<String,RuleDO> hGroupDetails,String parentGroup){
	 		
	 		if(dbobject.get("group_name").toString().equalsIgnoreCase(parentGroup)){
	 			BasicDBList compoundList=hCompoundDetailsArray.get(parentGroup);
	 			RuleDO oRulesDO=hGroupDetails.get(childGroup);
	 			BasicDBObject childObj=new BasicDBObject();
	 			childObj.put("group_operator",oRulesDO.getGroupOperator());
	 			childObj.put("group_name",oRulesDO.getGroupName());
	 			childObj.put("parent_group",oRulesDO.getParentGroup());
	 			childObj.put("datatype",oRulesDO.getDataType());
	 			childObj.put("rule_element",hCompoundDetailsArray.get(childGroup));
	 			compoundList.add(childObj);
	 			dbobject.put("rule_element",compoundList);
	 			//lGroupTree.remove(childGroup);
	 		}
	 		BasicDBList comp_properties = (BasicDBList) dbobject.get(element);
	 		if(comp_properties!=null){
	 			for(int iterate = 0; iterate<comp_properties.size(); iterate++) 
	          { 
	 				DBObject obj1=(DBObject) ((BSONObject) comp_properties.get(iterate));
	 				if(obj1.get(element)!=null){
	 					findCompoundDetailsGroupName(obj1,element,array,childGroup,hCompoundDetailsArray,hGroupDetails,parentGroup);
	          	}
	 				
	          }
	 		}
	 	}
	    private static void modifyJson(DBObject obj){
	 		
	 		BasicDBObject searchDocument=new BasicDBObject();
	 		searchDocument.put("group_operator", obj.get("group_operator"));
	 		searchDocument.put("group_name", obj.get("group_name"));
	 		searchDocument.put("parent_group", obj.get("parent_group"));
	 		searchDocument.put("datatype", obj.get("datatype"));
	 		/*if(getChangeElementName()!=null){
	 		//searchDocument.put("compound_details", obj.get("rule_element"));
	 		 searchDocument.put("rule_element", obj.get("rule_element"));
	 		}*/
	 		searchDocument.put("rule_element", obj.get("rule_element"));
	 		obj.put("condition_specification", searchDocument);
	 		obj.removeField("rule_element");
	 		obj.removeField("group_operator");
	 		obj.removeField("group_name");
	 		obj.removeField("parent_group");
	 		obj.removeField("datatype");
	 	
	   }
	    
	public static void main(String args[]){
		
		//createTopicForNotificationGroup("engineering_group","test_rule");
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		mongoSingle = new MongoDBConnection();
		MongoCollection<Document> table = null;
		mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
		try{
			//updateNotificationGroup("arn:aws:sns:us-east-1:880111024226:engineering_group_test_rule","engineering_group",mongodb);
			//updateTopicNameForRule("59b0657652e4f654cc5195e9", "arn:aws:sns:us-east-1:880111024226:engineering_group_test_rule", mongodb);
			//deleteTopicFromNotificationGroup("arn:aws:sns:us-east-1:880111024226:engineering_group_test_rule", "vibration_alarm_group_0123456", mongodb);
			//upsertTopicForNotificationGroup(notificationGroupId, ruleName, mongodb, notificationType, ruleId);
			//deleteTopicFromNotificationGroup("arn:aws:sns:us-east-1:880111024226:vibration_alerts_5594229_59c17571df308114fc7cda32","vibration_alerts_5594229",mongodb);
			
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
		
		/*BasicDBObject jsonDocument = null; 
		JSONObject rule = JSonUtility.getJSONObject("rule_test.json");
		jsonDocument = (BasicDBObject) JSON.parse(rule.toString());
		logger.info(generateGroupDefinitionArray(jsonDocument));*/
	}
	
	/**
	 * 
	 * @param generatedTopicName
	 * @param groupId
	 * @param mongodb
	 */
	public  static void deleteTopicFromNotificationGroup(String generatedTopicName,String notificationGroupId,MongoDatabase mongodb,String ruleId){
		if(generatedTopicName!=null && generatedTopicName.length()>0){
			MongoCollection<Document> table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_NOTIFICATION_GROUP);
			Document searchQuery = new Document();
			searchQuery.put("group_id", notificationGroupId);
			Document update = new Document().append("topics", new Document().append( "topics_name", generatedTopicName));
			table.updateOne(searchQuery, new BasicDBObject("$pull", update));
			AWSCredentials credentials = new BasicAWSCredentials(
					CommonConstants.S3_SNS_ACCESS_KEY, 
					CommonConstants.S3_SNS_SECRET_ACCESS_KEY);  
			AmazonSNSClient snsClient = new AmazonSNSClient(credentials);	
			DeleteTopicRequest deleteTopicRequest = new DeleteTopicRequest(generatedTopicName);
			snsClient.deleteTopic(deleteTopicRequest);
		}
		MongoCollection<Document> table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_NOTIFICATION_GROUP);
		Document searchQuery = new Document();
		searchQuery.put("group_id", notificationGroupId);
		Document update = new Document().append("dependencies", new Document().append( "rule_id", ruleId));
		table.updateOne(searchQuery, new BasicDBObject("$pull", update));
		
	}
	
	/**
	 * 
	 * @param generatedTopicName
	 * @param groupId
	 * @param mongodb
	 */
	private static void updateNotificationGroup(String generatedTopicName,String notificationGroupId,MongoDatabase mongodb){
		
		Document searchQuery = new Document();
		searchQuery.put("group_id", notificationGroupId);
		MongoCollection<Document> table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_NOTIFICATION_GROUP);
		Document doc = new Document().append("topics_name", generatedTopicName);
		//table.updateOne(searchQuery, Updates.addToSet("topics", doc));
		table.updateOne(searchQuery, Updates.addToSet("topics", doc));
		
	}
	
	/**
	 * 
	 * @param ruleId
	 * @param notificationGroupId
	 * @param mongodb
	 */
	public static void upsertRuleIdForNotificationGroup(String ruleId,String notificationGroupId,MongoDatabase mongodb){
		
		Document searchQuery = new Document();
		searchQuery.put("group_id", notificationGroupId);
		MongoCollection<Document> table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_NOTIFICATION_GROUP);
		Document doc = new Document().append("rule_id", ruleId);
		//table.updateOne(searchQuery, Updates.addToSet("topics", doc));
		table.updateOne(searchQuery, Updates.addToSet("dependencies", doc));
		
	}
	
	/**
	 * 
	 * @param ruleId
	 * @param notificationGroupId
	 * @param mongodb
	 */
	private static void updateTopicNameForRule(String ruleId,String generatedTopicName,MongoDatabase mongodb,String notificationGroupId){
		
		Document searchQuery = new Document();
		searchQuery.put("general.id", ruleId);
		MongoCollection<Document> table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_RULE);
		Document generalUpdate=new Document();
		generalUpdate.put("general.topic_name", generatedTopicName);
		//generalUpdate.put("general.notification_group_id", notificationGroupId);
		BasicDBObject command = new BasicDBObject();
		command.put("$set", generalUpdate);
		table.updateOne(searchQuery, command);
		
	}
	
	
	/**
	 * 
	 * @param ruleId
	 * @param generatedTopicName
	 * @param mongodb
	 * @param notificationGroupId
	 */
	private static void updateTopicNameForRule(String ruleId,JSONArray jsonArrayTopicName,MongoDatabase mongodb){
		
		Document searchQuery = new Document();
		searchQuery.put("general.id", ruleId);
		MongoCollection<Document> table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_RULE);
		//table.updateOne(searchQuery, Updates.addToSet("topics", doc));
		table.updateOne(searchQuery, Updates.addEachToSet("topics", jsonArrayTopicName));
		
	}
	
	/**
	 * 
	 * @param groupId
	 * @param ruleName
	 * @param mongodb
	 * @param notificationType
	 */
	public  static void upsertTopicForNotificationGroup(String notificationGroupId,String ruleName,MongoDatabase mongodb,String notificationType,String ruleId){
		List<RuleDO> listRuleDO=getGroupMemberDetails(notificationGroupId,mongodb,notificationType);
		if(listRuleDO.size()>0){
			String generatedTopicName=createTopic(CommonConstants.S3_SNS_ACCESS_KEY,CommonConstants.S3_SNS_SECRET_ACCESS_KEY,notificationGroupId+"_"+ruleName.replaceAll(" ", "_"), listRuleDO, notificationType);
			updateNotificationGroup(generatedTopicName, notificationGroupId, mongodb);
			updateTopicNameForRule(ruleId, generatedTopicName, mongodb,notificationGroupId);
		}
		//TODO 
		/*JSONArray jsonArrayTopicName=new JSONArray();
		List<String> notificationList = Arrays.asList(notificationGroupId.split(","));
		for(String strNotificationGroup:notificationList){
			listRuleDO=getGroupMemberDetails(strNotificationGroup,mongodb,notificationType);
			updateNotificationGroup(generatedTopicName, strNotificationGroup, mongodb);
			Document doc=new Document();
			doc.put("notification_group_id", strNotificationGroup);
			doc.put("topic_name", generatedTopicName);
			jsonArrayTopicName.add(doc);
			
		}
		updateTopicNameForRule(ruleId, jsonArrayTopicName, mongodb);*/
		
	}
	
	/**
	 * 
	 * @param objectType
	 * @param objectName
	 * @param mongodb
	 */
	public static Integer objectExists(String objectType, String objectName, MongoDatabase mongodb) {
		MongoCollection<Document> table = null;
		Integer code = 0;
		Document searchQuery = new Document();
		Document matchQuery = new Document();
		AggregateIterable<Document> iterable = null;
		Map<String,String> lMapMongoCollection=new WeakHashMap<String,String>();
		lMapMongoCollection.put("rule", MongoDBConstants.MONGO_COLLECTION_RULE);
		lMapMongoCollection.put("notificationGroup", MongoDBConstants.MONGO_COLLECTION_NOTIFICATION_GROUP);
		table = mongodb.getCollection(lMapMongoCollection.get(objectType));
		if(objectType.equalsIgnoreCase("rule")) {
			searchQuery.put("general.name", Pattern.compile(CommonUtility.applyCaseInsensitiveSearchForString(objectName), Pattern.CASE_INSENSITIVE));
		}
		if(objectType.equalsIgnoreCase("notificationGroup")) {
			searchQuery.put("group_name", Pattern.compile(CommonUtility.applyCaseInsensitiveSearchForString(objectName), Pattern.CASE_INSENSITIVE));
		}
		matchQuery.put("$match", searchQuery);
		iterable = table.aggregate(Arrays.asList(matchQuery));
		for (Document row : iterable) {
			code = -1;
		}
		return code;
	}
}
