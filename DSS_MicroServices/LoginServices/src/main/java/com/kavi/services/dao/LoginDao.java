package com.kavi.services.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;

import com.kavi.common.utility.CommonUtility;
import com.kavi.common.utility.MessageUtility;
import com.kavi.services.dataobjects.LdapUser;
import com.kavi.services.utility.LoginUtility;
import com.mongo.db.MongoDBConnection;
import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class LoginDao {
	
	private static MongoDBConnection mongoSingle = null;
	
	public static void saveSessionInfo(List<LdapUser> ldapUserList, StringBuilder sb) {
		mongoSingle = new MongoDBConnection();
		JSONArray roleArr = null;
		JSONArray organizationArray = null;
		JSONArray companyArray = new JSONArray();
		Document document = null;
		String dbName = null;
		ObjectId objectId = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> sessionInfoTable = null;
		List<Document> listCompanyObject = null;
		Document companyDoc = null;
		LdapUser ldapUser = null;
		organizationArray = new JSONArray();
		String sessionkey = CommonUtility.generateSessionId();
		try {
		if (CollectionUtils.isNotEmpty(ldapUserList)) {
			for (LdapUser user : ldapUserList) {
				if (user.getGroupName() != null) {
					dbName = user.getOrganizationName() + "_DSS";
					System.out.println("dbName: "+dbName);
					mongodb = mongoSingle.getMongoDB(dbName);
					System.out.println("mongodb: "+mongodb.getName()+", "+mongodb.toString());
					ldapUser = user;
					roleArr = new JSONArray();
					roleArr.add(user.getGroupName());
					organizationArray.add(user.getOrganizationName());
					document = new Document();
					document.put("userId", user.getUserId());
					document.put("organizationName", user.getOrganizationName());
					document.put("organization", organizationArray);
					document.put("userDisplayName", user.getDisplayName());
					document.put("sessionkey", sessionkey);
					if (user.getEmail() != null && user.getEmail().length() > 0) {
						document.put("email_id", user.getEmail());
					} else {
						document.put("email_id", user.getUserId());
					}
					document.put("role", roleArr);
					document.put("start_time", CommonUtility.getDateAsObject());
					sessionInfoTable = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_SESSION_INFO);
					sessionInfoTable.insertOne(document);
					objectId = (ObjectId) document.get("_id");
					user.setObjectId(objectId.toString());
					CommonUtility.maintainSessionActivities(mongodb, "Login", sessionkey,
							MongoDBConstants.ACTIVITY_START, "Login Successfully Done");
					listCompanyObject = getCompanyObject(mongodb, user,user.getOrganizationName());
					companyDoc = new Document();
					//companyDoc.append("sessionkey", user.getSessionKey());
					companyDoc.append("organizationName", user.getOrganizationName());
					companyDoc.append("userRole", user.getGroupName());
					companyDoc.append("db_name", dbName);
					if (listCompanyObject.size() > 0) {
						Document company = (Document) listCompanyObject.get(0).get("company");
						CommonUtility.updateOrganizationName(mongodb, user.getObjectId(), company.get("id").toString());
						companyDoc.append("has_site_access", true);
						companyDoc.append("display_name", company.get("display_name"));
					}else {
						companyDoc.append("has_site_access", false);
					}
					companyArray.add(companyDoc);
					CommonUtility.maintainSessionActivities(mongodb, "Login", sessionkey, MongoDBConstants.ACTIVITY_END,
							"");
				}
			}
			LoginUtility.updateLoginResponseObject(companyArray, sessionkey, sb, ldapUser, organizationArray);
			MessageUtility.updateMessage(sb, 0, "User Authenticated and Session created");
		}
		}catch(Exception e) {
			e.printStackTrace();
			sb.append("\"Invalid Credentials\"");
			MessageUtility.updateMessage(sb, 1000, "Credentials Provided are invalid");
		}finally {
		if (mongoSingle != null) {
			CommonUtility.closeMongoConnection(mongoSingle, mongodb, sessionInfoTable);
		}
		}
	}
	
	public static List<Document> getCompanyObject(MongoDatabase mongodb, LdapUser ldapUser, JSONArray organizationArray){
		Map<String, String> linkedUserGroupDetails = CommonUtility.getUserAndGroupDetails(mongodb,
				ldapUser.getObjectId());
		MongoCollection<Document> locationHierarchyTable = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY);
		List<Document> listCompanyObject = new ArrayList<Document>();
		Document companyObject = null;
		for (int i = 0; i < organizationArray.size(); i++) {
			companyObject = CommonUtility.getLocationTreeHierarchyForUser(
					linkedUserGroupDetails.get("userId"), locationHierarchyTable, organizationArray.get(i).toString(),
					mongodb);
			System.out.println("Company object: "+companyObject.toJson());
			if (companyObject.get("company") != null) {
				listCompanyObject.add(companyObject);
			}
		}
		return listCompanyObject;
	}
	
	private static List<Document> getCompanyObject(MongoDatabase mongodb, LdapUser ldapUser, String organizationName){
		Map<String, String> linkedUserGroupDetails = CommonUtility.getUserAndGroupDetails(mongodb,
				ldapUser.getObjectId());
		MongoCollection<Document> locationHierarchyTable = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY);
		List<Document> listCompanyObject = new ArrayList<Document>();
		Document companyObject = null;
		companyObject = CommonUtility.getLocationTreeHierarchyForUser(
					linkedUserGroupDetails.get("userId"), locationHierarchyTable, organizationName,
					mongodb);
		System.out.println("Company object: "+companyObject.toJson());
		Document companyObjectNew=(Document)companyObject.get("company");
		if (companyObjectNew != null) {
			    JSONArray array=(JSONArray)companyObjectNew.get("plant");
			    if(array.size()>0) {
			     listCompanyObject.add(companyObject);
			    }
		 }
		return listCompanyObject;
	}

}
