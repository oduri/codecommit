package com.kavi.services.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.kavi.common.constants.CommonConstants;
import com.kavi.common.utility.CommonUtility;
import com.kavi.common.utility.LDAPUtility;
import com.kavi.common.utility.MailUtility;
import com.kavi.common.utility.MessageUtility;
import com.kavi.common.utility.SESUtility;
import com.kavi.commonservice.dataobjects.StatusDO;
import com.kavi.services.impl.AWSEmailServiceImpl;
import com.kavi.services.intf.EmailService;
import com.mongo.db.MongoDBConnection;
import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.org.apache.xml.internal.security.utils.Base64;

@RestController
public class CommonController {

	@RequestMapping("/")
	public String welcome() {
		return "Welcome to MicroCommonServices";
	}
	
	
	/**
	 * 
	 * @param userId
	 * @param userPassword
	 * @param organizationName
	 * @return
	 */
	@RequestMapping(value = "/testLogin", method = RequestMethod.POST)
	public ResponseEntity<String> testLogin(String userId,String userPassword,String organizationName) {
		
		StringBuilder sb=new StringBuilder();
	    sb.append("{");
		sb.append("\"status\":");
		
		Map<String,String> mapMatchGroups=new WeakHashMap<String,String>();
	    boolean validUser=false;
    	String userDisplayName="";
    	String mailAddress="";
    	String generatedMD5Password="";
    	String base64String="";
    	JSONArray organizationArray=new JSONArray();
    	
    	 if(userId==null || "".equalsIgnoreCase(userId)){
				sb.append("\"Custom Message Generated basis Input\"");
				MessageUtility.updateMessage(sb, 5000, "userId is null");
				return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		 }
		 else if(userPassword==null || "".equalsIgnoreCase(userPassword)){
			 sb.append("\"Custom Message Generated basis Input\"");
			  MessageUtility.updateMessage(sb, 5000, "userPassword is null");
			  return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		 }
    	MongoDBConnection mongoSingle=null;
		MongoDatabase mongodb=null;
		MongoCollection<Document> table=null;
		DirContext ldapContext = LDAPUtility.getSRVLDAPContext();
		Map<String,String> lEnvironmentMap=CommonUtility.initializeEnviromentProperties();
		if(ldapContext==null){
				sb.append("\"Authentication Connection\"");
				//sb.append("}");
				MessageUtility.updateMessage(sb, 2000, "Authentication Server Connection Failure");
				return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		 }
		else
		{
			userId=userId.trim();
		  	userPassword=userPassword.trim();
			String sessionkey="";
		  	SearchControls groupsSearchCtls = new SearchControls();
	        groupsSearchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);  
	        String sRetAttr[]={"cn","userPassword","entryDN","uid","jpegPhoto","displayName","mail"};
	        groupsSearchCtls.setReturningAttributes(sRetAttr);
	        try
	        {	
	        	 mongoSingle=new MongoDBConnection();
				 mongodb=mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				 NamingEnumeration userAnswer= null;
				 if(organizationName!=null && organizationName.length()>0){
					 userAnswer=ldapContext.search("o="+organizationName+","+LDAPUtility.getDirectoryName(), "(uid="+userId+")",groupsSearchCtls);
				 }else{
					 userAnswer= ldapContext.search(LDAPUtility.getDirectoryName(), "(uid="+userId+")",groupsSearchCtls);
				 }
	        	 while (userAnswer.hasMoreElements()) {
		        	  SearchResult sr = (SearchResult)userAnswer.next();
		        	  Attributes attrs = sr.getAttributes();
		        	  if(attrs.get("userPassword")!=null){
		        		  /*MessageDigest digest = null;
		        	       try {
		        	    	   digest= MessageDigest.getInstance("MD5");
		        	    	   System.out.println("Password we get from client: "+userPassword);
							digest.update(userPassword.getBytes("UTF8"));
						} catch (Exception e) {
							e.printStackTrace();
						}*/
		        	    /*String md5Password = Base64.encode(digest.digest());
		        	    generatedMD5Password="{MD5}" + md5Password;
		        	    System.out.println("Generated Password: "+generatedMD5Password);*/
		        	    
		        	    String pwd = new String((byte[]) attrs.get("userPassword").get());
		        	    System.out.println("Password from LDAP: "+pwd);
		        		if(pwd.equalsIgnoreCase(userPassword)){
		        			  StringTokenizer st=new StringTokenizer((String)attrs.get("entryDN").get(),",");
		        				while(st.hasMoreElements()){
		        						String groupName="";
		        						String name=st.nextToken();
		        						if(name.startsWith("o")){
		        							organizationName=name.substring(2,name.length());
		        							organizationArray.add(organizationName);
		        						}
		        						if(name.startsWith("cn")){
		        						 groupName=name.substring(3,name.length());
		        						 if(!groupName.equalsIgnoreCase(userId)){
		        							 mapMatchGroups.put(groupName,userPassword);
		        							 userId=(String)attrs.get("uid").get();
		        							 byte bt[] = null;
		        							 if(attrs.get("jpegPhoto")!=null){
		        							 bt = (byte[]) attrs.get("jpegPhoto").get();
		        							 base64String = Base64.encode(bt);
		        							 }
		        						 }
		        					 }
		        						if(attrs.get("displayName")!=null){
		        						userDisplayName=(String)attrs.get("displayName").get();
		        						}else{
		        							userDisplayName=userId;
		        						}
		        				 }
		        				if(attrs.get("mail")!=null){
		        					mailAddress=(String)attrs.get("mail").get();
		        				}
		        				
		        			  validUser=true;
		        		  }
		        	  }
		        	  
		        }//end of while
	        	if(validUser==false){
 		        	sb.append("\"Invalid Credentials\"");
 		  	      	MessageUtility.updateMessage(sb, 1000, "Credentials Provided are invalid");
 		  	      	return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
 		       }
	        	if(mapMatchGroups.size()>0 && mapMatchGroups!=null){
	        			JSONArray mapMatchGroup=new JSONArray();
 				   		Iterator it = mapMatchGroups.entrySet().iterator();
 				   		String userRole="";
 				   		while (it.hasNext()) 
 				   		{
 				   			Map.Entry pair = (Map.Entry)it.next();
 				   			mapMatchGroup.add(pair.getKey());
 				   			userRole=(String)pair.getKey();
 				   		}
						Document document = new Document();
						document.put("userId", userId);
						document.put("organizationName", organizationArray.get(0));
						document.put("organization", organizationArray);
						document.put("userDisplayName", userDisplayName);
						if(mailAddress!=null && mailAddress.length()>0){
							document.put("email_id", mailAddress);
						}else{
							document.put("email_id", userId);
						}
						document.put("role",mapMatchGroup);
						document.put("start_time",CommonUtility.getDateAsObject());
						table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_SESSION_INFO);
						table.insertOne(document);
						ObjectId objectId = (ObjectId)document.get("_id");
						sessionkey=objectId.toString();
						CommonUtility.maintainSessionActivities(mongodb,"Login", sessionkey, MongoDBConstants.ACTIVITY_START,"Login Successfully Done");
						sb.append("\"Success\"");
		    			sb.append(",");
						sb.append("\"sessionkey\":");
	    				sb.append("\""+objectId.toString()+"\"");
	    				sb.append(",");
	    				sb.append("\"userId\":");
	    				sb.append("\""+userId+"\"");
	    				sb.append(",");
	    				sb.append("\"userDisplayName\":");
	    				sb.append("\""+userDisplayName+"\"");
	    				sb.append(",");
	    				sb.append("\"userRole\":");
	    				sb.append("\""+userRole+"\"");
	    				sb.append(",");
	    				sb.append("\"organizationName\":");
	    				sb.append("\""+organizationArray.get(0)+"\"");
	    				sb.append(",");
	    				sb.append("\"displayImage\":");
	    				sb.append("\""+base64String.replaceAll("\n", "")+"\"");
	    				sb.append(",");
	    				if(organizationArray.size()>0){
	    					sb.append("\"organization\":");
		    				sb.append(organizationArray);
		    				sb.append(",");
	    				}
	    				Map<String, String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
						table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY);
						//New Implemenatation Started
						List<Document> listCompanyObject=new ArrayList<Document>();
						for(int iterate=0;iterate<organizationArray.size();iterate++){
							Document companyObject=CommonUtility.getLocationTreeHierarchyForUser(linkedUserGroupDetails.get("userId"),table,
									organizationArray.get(iterate).toString(),mongodb);
							if(companyObject.get("company")!=null){
								listCompanyObject.add(companyObject);
							}
						}
						if(listCompanyObject.size()>0){
							
							sb.append("\"has_site_access\":");
							sb.append(true);
							sb.append(",");
							Document company=(Document) listCompanyObject.get(0).get("company");
							CommonUtility.updateOrganizationName(mongodb, sessionkey, company.get("id").toString());
							sb.append("\"organizationName\":");
		    				sb.append("\""+company.get("id")+"\"");
		    				sb.append(",");
							sb.append("\"display_name\":");
							sb.append("\""+company.get("display_name")+"\"");
							sb.append(",");
						}
						else{
							sb.append("\"has_site_access\":");
							sb.append(false);
							sb.append(",");
						}
						//New Implemenatation Ended
						
						//Old Implementation started
						/*Document companyObject=CommonUtility.getLocationTreeHierarchyForUser(linkedUserGroupDetails.get("userId"),table,linkedUserGroupDetails.get("organizationName"),mongodb);
						if(companyObject.get("company")!=null){
							sb.append("\"has_site_access\":");
							sb.append(true);
							sb.append(",");
							Document company=(Document) companyObject.get("company");
							sb.append("\"display_name\":");
							sb.append("\""+company.get("display_name")+"\"");
							sb.append(",");
						}else{
							sb.append("\"has_site_access\":");
							sb.append(false);
							sb.append(",");
						}*/
						//Old Implementation Ended
	    				sb.append("\"encodeType\":");
	    				sb.append("\"base64\"");
	    				sb.append(",");
	    				sb.append("\"image_path\":");
	    				sb.append("\""+lEnvironmentMap.get("image_path")+"\"");
	    				MessageUtility.updateMessage(sb, 0, "User Authenticated and Session created");
	    				CommonUtility.maintainSessionActivities(mongodb,"Login", sessionkey, MongoDBConstants.ACTIVITY_END,"");
	    				ldapContext.close();
						return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
	    	    }
		   	 }catch(NamingException nameexceptionerr){
		   		sb.append("\"Invalid Credentials\"");
			    MessageUtility.updateMessage(sb, 1000, "Credentials Provided are invalid");
				return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		    }
	        finally{
	        	if(mongoSingle!=null){
	    			CommonUtility.closeMongoConnection(mongoSingle,mongodb,table);
	    			mapMatchGroups.clear();
	    			lEnvironmentMap.clear();
	    		}
	        }
		 }
		
		sb.append("\"Invalid Credentials\"");
	    MessageUtility.updateMessage(sb, 1000, "Credentials Provided are invalid");
		return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
	    
	}
	/**
	 * 
	 * @param json
	 * @return
	 */
	
	@RequestMapping(value = "/sendEMailToSupportTeam", method = RequestMethod.POST)
	public Object sendEMailToSupportTeam(@RequestHeader(value="Authorization") String authString, @RequestHeader HttpHeaders headers,@RequestBody String json) {
         Properties prop = initializeProperties();
		StatusDO status=new StatusDO();
		try {
			 if(!isUserAuthenticated(authString)){
		        	status.setCode(1001);
		        	status.setStatus("User not authenticated");
		        	return status;
		        }
			 
			Document responseDoc=Document.parse(json);
			if(prop.getProperty(MongoDBConstants.SES_FLAG_ENABLE)!=null && prop.getProperty(MongoDBConstants.SES_FLAG_ENABLE).equalsIgnoreCase("true")) {
				SESUtility sesUtil = new SESUtility();
				Properties properties=sesUtil.getProperties();
				properties.setProperty("mail.transport.protocol", "aws");
				properties.setProperty("mail.aws.user", properties.getProperty("aws.sesAccessKey"));
				properties.setProperty("mail.aws.password",  properties.getProperty("aws.sesSecretKey"));
				EmailService emailService = new AWSEmailServiceImpl(sesUtil.createSimpleEmailService());
				StringBuilder sb = new StringBuilder();
			    sb.append(responseDoc.getString("message"));
			    sb.append("\n");
			    sb.append("\n");
			    sb.append(responseDoc.getString("body"));
			    sb.append("\n");
			    sb.append("\n");
			    
				emailService.withFrom(CommonConstants.SMTP_AUTH_USER)
					.withTo(responseDoc.getString("emailAddresses"))
					.withSubject(responseDoc.getString("subject"))
					.withBody(new Body()
			                  .withHtml(new Content()
			                      .withCharset("UTF-8").withData(sb.toString())))
					.sendEmailWithHtmlContent();
			}else {
				MailUtility.sendEmailToSupportTeam(responseDoc);
			}
			status.setCode(201);
	    	status.setStatus("Mail Sent Successfully");
		}catch (Exception e) {
			e.printStackTrace();
			status.setCode(1001);
        	status.setStatus(e.getMessage());
			return status;
		} finally {
			
		}
        
    	return status;
    }
	
	private static Properties initializeProperties(){
		Properties prop = new Properties();
		String file = "/environment.properties";
   		InputStream inputStream = CommonController.class.getResourceAsStream(file); 
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
	
	/**
	 * 
	 * @param authCredentials
	 * @return
	 */
	private static boolean isUserAuthenticated(String authCredentials) {

		if (null == authCredentials){
			return false;
		}
		final String encodedUserPassword = authCredentials.replaceFirst("Basic"
				+ " ", "");
		String usernameAndPassword = null;
		try {
			byte[] decodedBytes = java.util.Base64.getDecoder().decode(
					encodedUserPassword);
			usernameAndPassword = new String(decodedBytes, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		final StringTokenizer tokenizer = new StringTokenizer(
				usernameAndPassword, ":");
		final String username = tokenizer.nextToken();
		final String password = tokenizer.nextToken();
		boolean authenticationStatus = "Jenkins-User".equals(username)
				&& "7a92307b-cc15-4ab9-bf82-799b3b114a2c".equals(password);
		return authenticationStatus;
	}
	
}
