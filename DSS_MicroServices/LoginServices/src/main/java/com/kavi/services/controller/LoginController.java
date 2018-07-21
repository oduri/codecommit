package com.kavi.services.controller;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.kavi.common.constants.CommonConstants;
import com.kavi.common.utility.CommonUtility;
import com.kavi.common.utility.LDAPUtility;
import com.kavi.common.utility.MailUtility;
import com.kavi.common.utility.MessageUtility;
import com.kavi.services.utility.LoginUtility;
import com.mongo.db.MongoDBConnection;
import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

@RestController
public class LoginController {

	@RequestMapping("/")
	public String welcome() {
		return "Welcome to MicroLoginService";
	}
	
	
	/**
	 * 
	 * @param req
	 * @param formParams
	 * @return
	 */
	@RequestMapping(value = "/forgotPassword", method = RequestMethod.POST)
	public ResponseEntity<String> forgotPassword(String userId) {

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		boolean validUser = false;
		Map<String, String> entryDNMap = new HashMap<String, String>();
		DirContext ldapContext = LDAPUtility.getSRVLDAPContext();
		if (ldapContext == null) {
			sb.append("\"Authentication Connection\"");
			MessageUtility.updateMessage(sb, 2000, "Authentication Server Connection Failure");
			return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		} else {
			String entryDN = "";
			MessageDigest digest = null;
			String emailId = "";
			try {
				SearchControls groupsSearchCtls = new SearchControls();
				groupsSearchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				String sRetAttr[] = { "entryDN", "uid", "mail" };
				groupsSearchCtls.setReturningAttributes(sRetAttr);
				NamingEnumeration userAnswer = null;
				userAnswer = ldapContext.search(LDAPUtility.getDirectoryName(), "(uid=" + userId + ")",
						groupsSearchCtls);
				while (userAnswer.hasMoreElements()) {
					SearchResult sr = (SearchResult) userAnswer.next();
					Attributes attrs = sr.getAttributes();
					entryDN = attrs.get("entryDN").get().toString();
					if (attrs.get("mail") != null) {
						emailId = attrs.get("mail").get().toString();
					}
					entryDNMap.put(entryDN, emailId);
					System.out.println("entryDN: " + entryDN);
					validUser = true;
				}
			} catch (NamingException nameexceptionerr) {
				sb.append("\"Invalid Credentials\"");
				MessageUtility.updateMessage(sb, 1000, "Credentials Provided are invalid");
				return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
			}
			if (emailId == null || emailId.length() <= 0 && validUser) {
				sb.append("\"EmailId is not registered for the user\"");
				MessageUtility.updateMessage(sb, 1000, "EmailId is not registered for " + userId);
				return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
			}
			if (validUser) {
				System.out.println("Valid user");
				String generateTempPassword = CommonUtility.generateRandomPassword();
				String tmpPwdWithSalt = generateTempPassword + "Molex_Dss";
				StringBuilder pwdBuilder = null;
				try {
					digest = MessageDigest.getInstance("MD5");
					digest.update(tmpPwdWithSalt.getBytes("UTF8"));
					byte[] bytes = digest.digest();
					pwdBuilder = new StringBuilder();
					for (int i = 0; i < bytes.length; i++) {
						pwdBuilder.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				// String md5Password = Base64.encode(digest.digest());
				// String generateMD5Password = "{MD5}" + md5Password;
				String generateMD5Password = pwdBuilder.toString();
				Attribute mod0 = new BasicAttribute("userPassword", generateMD5Password);
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod0);
				boolean flag = false;
				Properties prop = LoginUtility.initializeProperties();
				if (prop.getProperty(MongoDBConstants.SES_FLAG_ENABLE) != null
						&& prop.getProperty(MongoDBConstants.SES_FLAG_ENABLE).equalsIgnoreCase("true")) {
					flag = LoginUtility.sendMailViaSES(userId, generateTempPassword, emailId);
					System.out.println("If mail" + generateTempPassword);
				} else {
					System.out.println("Else mail" + generateTempPassword);
					flag = MailUtility.sendEmail(userId, generateTempPassword, emailId);
				}
				System.out.println("Flag: " + flag);
				if (flag) {
					try {
						if (!entryDNMap.isEmpty()) {
							for (Map.Entry<String, String> entry : entryDNMap.entrySet()) {
								ldapContext.modifyAttributes(entry.getKey(), mods);
							}
						}
					} catch (NamingException nameexceptionerr) {

					}
					sb.append("\"Password is sent to your registered email id\"");
					MessageUtility.updateMessage(sb, 0, "Password is sent to your registered email id");
					return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
				} else {
					sb.append("\"Error in Sending Mail\"");
					MessageUtility.updateMessage(sb, 7000,
							"Password could not be sent please contact " + CommonConstants.SMTP_MAIL_ID);
					return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
				}

			} else {
				sb.append("\"This user id is not registered in Molex\"");
				MessageUtility.updateMessage(sb, 8000, "This user id is not registered in Molex");
				return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);

			}
		}
	}
	
	/**
	 * 
	 * @param req
	 * @param formParams
	 * @return
	 */
	@RequestMapping(value = "/changePassword", method = RequestMethod.POST)
	public ResponseEntity<String> changePassword(String changePassword,String oldPassword,String sessionkey,String type,String dbParamJson) {

		StringBuilder sb=new StringBuilder();
	    sb.append("{");
		sb.append("\"status\":");
		String dbName = null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		List<String> entryDNList = new ArrayList<String>();
	    boolean validUser=false;

    	 if(oldPassword==null || "".equalsIgnoreCase(oldPassword)){
				sb.append("\"Custom Message Generated basis Input\"");
				MessageUtility.updateMessage(sb, 5000, "oldPassword is null");
				return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		 }
		 else if(sessionkey==null || "".equalsIgnoreCase(sessionkey)){
			 sb.append("\"Custom Message Generated basis Input\"");
			 MessageUtility.updateMessage(sb, 5000, "sessionkey is null");
			 return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		 }
    	int code=0;
    	if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		DirContext ldapContext = LDAPUtility.getSRVLDAPContext();
		if(ldapContext==null){
				sb.append("\"Authentication Connection\"");
				//sb.append("}");
				MessageUtility.updateMessage(sb, 2000, "Authentication Server Connection Failure");
				return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		 }
		else
		{
			oldPassword=oldPassword.trim();
		  	MongoDBConnection mongoSingle=null;
			MongoDatabase mongodb=null;
			MongoCollection<Document> table=null;
		  	SearchControls groupsSearchCtls = new SearchControls();
	        groupsSearchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);  
	        String sRetAttr[]={"cn","userPassword","entryDN","uid","jpegPhoto","displayName"};
	        groupsSearchCtls.setReturningAttributes(sRetAttr);
	        MessageDigest digest = null;
	        if(code==0)
			{	
		        try
		        {	
		        	 mongoSingle=new MongoDBConnection(dbParamJson);
		        	 if(dbName!=null) {
						 mongodb = mongoSingle.getMongoDB(dbName);
					 }else {
					 mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
					 }
					 Map<String, String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
					 NamingEnumeration userAnswer= null;
					 String organizationName=linkedUserGroupDetails.get("organizationName");
					 if(organizationName!=null && organizationName.length()>0){
						 //userAnswer=ldapContext.search("o="+organizationName+","+LDAPUtility.getDirectoryName(), "(uid="+linkedUserGroupDetails.get("userId")+")",groupsSearchCtls);
						 userAnswer=ldapContext.search(LDAPUtility.getDirectoryName(), "(uid=" + linkedUserGroupDetails.get("userId") + ")",groupsSearchCtls);
					 }
					 String entryDN="";
		        	 while (userAnswer.hasMoreElements()) {
			        	  SearchResult sr = (SearchResult)userAnswer.next();
			        	  Attributes attrs = sr.getAttributes();
			        	  entryDN=attrs.get("entryDN").get().toString();
			        	  entryDNList.add(entryDN);
			        	  System.out.println("entryDN: "+entryDN);
			        	  if(attrs.get("userPassword")!=null){
			        	      /* try {
			        	    	   digest= MessageDigest.getInstance("MD5");
			        	    	   digest.update(oldPassword.getBytes("UTF8"));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			        	       String md5Password = Base64.encode(digest.digest());
			        	       oldPassword="{MD5}" + md5Password;
*/			        	       
			        		  String pwd = new String((byte[]) attrs.get("userPassword").get());
			        		 if(pwd.equalsIgnoreCase(oldPassword)){
			        			  validUser=true;
			        		  }
			        	  }
			        }//end of while
					if (validUser == false) {
						sb.append("\"Old password does not match\"");
						MessageUtility.updateMessage(sb, 6000, "Old password does not match");
						return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
					} else {
						if (changePassword == null || "".equalsIgnoreCase(changePassword)) {
							sb.append("\"Password Match\"");
							MessageUtility.updateMessage(sb, code, "Password Match");
							return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
						} else {
							/*
							 * try { digest= MessageDigest.getInstance("MD5");
							 * digest.update(changePassword.getBytes("UTF8")); } catch (Exception e) { //
							 * TODO Auto-generated catch block e.printStackTrace(); } String md5Password =
							 * Base64.encode(digest.digest()); changePassword="{MD5}" + md5Password;
							 */
							Attribute mod0 = new BasicAttribute("userPassword", changePassword);
							ModificationItem[] mods = new ModificationItem[1];
							mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod0);
							if (CollectionUtils.isNotEmpty(entryDNList)) {
								for (String dn : entryDNList) {
									ldapContext.modifyAttributes(dn, mods);
								}
							}
							sb.append("\"Password Changed Successfully\"");
							MessageUtility.updateMessage(sb, code, "Password Changed Successfully");
							return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
						}
					}
			   	 }catch(NamingException nameexceptionerr){
			   		sb.append("\"Invalid Credentials\"");
				    MessageUtility.updateMessage(sb, 1000, "Credentials Provided are invalid");
					return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
			    }finally{
					if(mongoSingle!=null){
						CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
					}
			    }
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
	 }
	
	/**
	 * 
	 * @param req
	 * @param formParams
	 * @return
	 */
	@RequestMapping(value = "/testLDAPConnection", method = RequestMethod.POST)
	public ResponseEntity<String> testLDAPConnection() {
		DirContext ldapContext = null;
		try {
			String conn_type = "simple";
			Hashtable<String, String> environment = new Hashtable<String, String>();
			environment.put(Context.SECURITY_AUTHENTICATION, "DIGEST-MD5");
			environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			environment.put(Context.PROVIDER_URL, "ldap://OpenLDAP-56c0403a463a6be3.elb.us-east-1.amazonaws.com:389");
			environment.put(Context.SECURITY_AUTHENTICATION, conn_type);
			environment.put(Context.SECURITY_PRINCIPAL, "cn=admin,dc=molex,dc=com");
			environment.put(Context.SECURITY_CREDENTIALS, "Kavi@123");
			environment.put("java.naming.ldap.attributes.binary", "tokenGroups");
			ldapContext = new InitialDirContext(environment);
			ldapContext.close();
			
		} catch (NamingException e) {
			return new ResponseEntity<String>("Exception is"+e.getMessage(),HttpStatus.OK);
		}
		return new ResponseEntity<String>("ldapconnectionestablished", HttpStatus.OK);
		
	}
}
