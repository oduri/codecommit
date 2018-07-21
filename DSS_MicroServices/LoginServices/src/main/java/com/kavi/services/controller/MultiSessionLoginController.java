package com.kavi.services.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.kavi.common.utility.CommonUtility;
import com.kavi.common.utility.LDAPUtility;
import com.kavi.common.utility.MessageUtility;
import com.kavi.services.dao.LoginDao;
import com.kavi.services.dataobjects.LdapUser;
import com.mongo.db.MongoDBConnection;
import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import com.sun.org.apache.xml.internal.security.utils.Base64;

@RestController
public class MultiSessionLoginController {
	
	/**
	 * @param userId
	 * @param userPassword
	 * @return
	 */
	@RequestMapping(value = "/multiSessionLogin", method = RequestMethod.POST)
	public ResponseEntity<String> multiSessionLogin(String userId, String userPassword) {

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		if (userId == null || "".equalsIgnoreCase(userId)) {
			sb.append("\"Custom Message Generated basis Input\"");
			MessageUtility.updateMessage(sb, 5000, "userId is null");
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} else if (userPassword == null || "".equalsIgnoreCase(userPassword)) {
			sb.append("\"Custom Message Generated basis Input\"");
			MessageUtility.updateMessage(sb, 5000, "userPassword is null");
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		}
		DirContext ldapContext = LDAPUtility.getSRVLDAPContext();

		if (ldapContext == null) {
			sb.append("\"Authentication Connection\"");
			MessageUtility.updateMessage(sb, 2000, "Authentication Server Connection Failure");
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		}else {
			userId = userId.trim();
			userPassword = userPassword.trim();
			SearchControls groupsSearchCtls = new SearchControls();
			groupsSearchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String sRetAttr[] = { "cn", "userPassword", "entryDN", "uid", "jpegPhoto", "displayName", "mail" };
			groupsSearchCtls.setReturningAttributes(sRetAttr);
			try {
				NamingEnumeration<SearchResult> userAnswer = ldapContext.search(LDAPUtility.getDirectoryName(),
						"(uid=" + userId + ")", groupsSearchCtls);
				SearchResult sr = null;
				Attributes attrs = null;
				String pwd = null;
				StringTokenizer st = null;
				LdapUser ldapUser = null;
				List<LdapUser> ldapUserList = new ArrayList<LdapUser>();
				String name = null;
				boolean validUser = false;
				String groupName = null;
				
				while (userAnswer.hasMoreElements()) {
					ldapUser = new LdapUser();
					sr = (SearchResult) userAnswer.next();
					attrs = sr.getAttributes();
					//System.out.println("Attributes: "+attrs);
					//System.out.println("pwd: "+new String((byte[]) attrs.get("userPassword").get()));
					if (attrs.get("userPassword") != null) {
						pwd = new String((byte[]) attrs.get("userPassword").get());
						System.out.println("ldap pwd: "+pwd);
						//Check if the LDAP pwd is equal to the password from the request
						if (pwd.equalsIgnoreCase(userPassword)) {
							st = new StringTokenizer((String) attrs.get("entryDN").get(), ",");
							//System.out.println("st: "+st);
							while (st.hasMoreElements()) {
								name = st.nextToken();
								//System.out.println("name: "+name);
								
								if (name.startsWith("o")) {
									ldapUser.setOrganizationName(name.substring(2, name.length())); 
								}
								
								if (name.startsWith("cn")) {
									groupName = name.substring(3, name.length()); 
									if (groupName!=null && !groupName.equalsIgnoreCase(userId)) {
										ldapUser.setGroupName(name.substring(3, name.length()));
										userId = (String) attrs.get("uid").get();
										ldapUser.setUserId(userId);
										byte bt[] = null;
										if (attrs.get("jpegPhoto") != null) {
											bt = (byte[]) attrs.get("jpegPhoto").get();
											ldapUser.setEncodedImgString(Base64.encode(bt));
										}else {
											ldapUser.setEncodedImgString("");
										}
									}
								}
								if (attrs.get("displayName") != null) {
									ldapUser.setDisplayName((String) attrs.get("displayName").get());
								} else {
									ldapUser.setDisplayName(userId);
								}
							}
							if (attrs.get("mail") != null) {
								ldapUser.setEmail((String) attrs.get("mail").get());
							}
							validUser = true;
						}
					}
					ldapUserList.add(ldapUser);
				}
				if (validUser == false) {
					sb.append("\"Invalid Credentials\"");
					MessageUtility.updateMessage(sb, 1000, "Credentials Provided are invalid");
					return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
				}
				//save the session info 
				LoginDao.saveSessionInfo(ldapUserList, sb);
				ldapContext.close();
				return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
				
			} catch (NamingException nameexceptionerr) {
				sb.append("\"Invalid Credentials\"");
				MessageUtility.updateMessage(sb, 1000, "Credentials Provided are invalid");
				return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
			}
		}
	}
	
	@RequestMapping(value = "/multiSessionLogout", method = RequestMethod.GET)
	public ResponseEntity<String> multiSessionLogout(String sessionkey,String dbParamJson) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		int code = 0;
		String dbName = null;
		Document doc = null;
		BasicDBObject dbParamJsonDoc = null;
		List<BasicDBObject> dbParamJsonDocList = new ArrayList<BasicDBObject>();
		if (dbParamJson != null) {
			dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			dbParamJsonDocList = (List<BasicDBObject>) dbParamJsonDoc.get("company");
		}
		try {
			if (CollectionUtils.isNotEmpty(dbParamJsonDocList)) {
				for (BasicDBObject dbParam : dbParamJsonDocList) {
					dbName = dbParam.getString("db_name");
					code = CommonUtility.validateAuthenticationKey(sessionkey,dbName);
					doc = new Document();
					if (code == 0) {
						mongoSingle = new MongoDBConnection();
						mongodb = mongoSingle.getMongoDB(dbName);
						CommonUtility.maintainSessionActivities(mongodb, "Logout", sessionkey,
								MongoDBConstants.ACTIVITY_START, "Logout Successfully Done");
						table = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_SESSION_INFO);
						Document searchQuery = new Document("sessionkey", sessionkey);
						Document updateData = new Document();
						updateData.put("end_time", CommonUtility.getDateAsObject());
						Document command = new Document();
						command.put("$set", updateData);
						table.updateOne(searchQuery, command);
					} else if (code == 1001) {
						doc.append("status", "Invalid key");
						doc.append("statusCode", code);
						doc.append("statusMessage", "Session Invalid");
						break;
					} else if (code == 2001) {
						doc.append("status", "MetaData Connection");
						doc.append("statusCode", code);
						doc.append("statusMessage", "MetaData Connection Failure");
						break;
					}
				}
				doc.append("status", "Success");
				doc.append("statusCode", 0);
				doc.append("statusMessage", "Logout Success");
			}else {
				doc = new Document();
				doc.append("status", "Invalid key");
				doc.append("statusCode", code);
				doc.append("statusMessage", "Session Invalid");
			}
			return new ResponseEntity<String>(doc.toJson(), HttpStatus.OK);
		} catch (Exception e) {
			doc = new Document();
			doc.append("status", "MetaData Connection");
			doc.append("statusCode", 2001);
			doc.append("statusMessage", "MetaData Connection Failure");
			return new ResponseEntity<String>(doc.toJson(), HttpStatus.OK);
		} finally {
			if (mongoSingle != null) {
				CommonUtility.maintainSessionActivities(mongodb, "logout", sessionkey, MongoDBConstants.ACTIVITY_END,
						"");
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
	}

}
