package com.kavi.services.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.json.simple.JSONArray;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.kavi.common.utility.CommonUtility;
import com.kavi.common.utility.FileUtility;
import com.kavi.common.utility.LDAPUtility;
import com.kavi.common.utility.MessageUtility;
import com.kavi.user.dataobjects.ObjectDO;
import com.kavi.utility.UserFileUtility;
import com.kavi.utility.UserUtility;
import com.mongo.db.MongoDBConnection;
import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.util.JSON;

@RestController
public class UserController {
	
	private static Properties prop = UserUtility.initializeProperties();
	
	@RequestMapping("/")
	public String welcome() {
		return "Welcome to MicroUserService";
	}
	
	@RequestMapping(value = "/loadAllRolesFromLDAP", method = RequestMethod.GET)
	public ResponseEntity<String> loadAllRolesFromLDAP(String sessionkey,String dbParamJson) {
		StringBuilder sb=new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		String dbName= null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		int code=0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		MongoDBConnection mongoSingle=null;
		MongoDatabase mongodb=null;
		try{
		   if(code==0){
			   mongoSingle=new MongoDBConnection(dbParamJson);
			   if (dbName != null) {
					mongodb = mongoSingle.getMongoDB(dbName);
				} else {
					mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				}
			   Map<String,String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
			   Document doc=new Document();
			   doc.put("status","Success");
			   doc.put("statusCode",0);
			   doc.put("statusMessage","loadAllRolesFromLDAP Object Sent");
			   doc.put("roles", LDAPUtility.getAllSRVRoles(linkedUserGroupDetails.get("organizationName")));
			   return new ResponseEntity<String>(doc.toJson(), HttpStatus.OK);
			   
		    }else{
		    	Document doc=new Document();
			    doc.put("status","Invalid key");
				doc.put("statusCode",code);
				doc.put("status","Session Invalid");
				return new ResponseEntity<String>(doc.toJson(), HttpStatus.OK);
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
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, null);
			}
	    }
	}
	/**
	 * 
	 * @param sessionkey
	 * @return
	 */
	@RequestMapping(value = "/loadAllUsersFromLDAP", method = RequestMethod.GET)
	public ResponseEntity<String> loadAllUsersFromLDAP(String sessionkey,String dbParamJson) {
		StringBuilder sb=new StringBuilder();
		String dbName= null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		sb.append("{");
		sb.append("\"status\":");
		int code=0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		MongoDBConnection mongoSingle=null;
		MongoDatabase mongodb=null;
		try{
		   if(code==0){
			   mongoSingle=new MongoDBConnection(dbParamJson);
			   if (dbName != null) {
					mongodb = mongoSingle.getMongoDB(dbName);
				} else {
					mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				}
			   Map<String,String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
			   Document doc=new Document();
			   doc.put("status","Success");
			   doc.put("statusCode",0);
			   doc.put("statusMessage","loadAllUsersFromLDAP Object Sent");
			   doc.put("users", LDAPUtility.getAllUsersForSRVOrganization(linkedUserGroupDetails.get("organizationName")));
			   doc.put("userscontact", LDAPUtility.getAllUsersContactForSRVOrganization(linkedUserGroupDetails.get("organizationName")));
			   return new ResponseEntity<String>(doc.toJson(), HttpStatus.OK);
		    }else{
		    	Document doc=new Document();
			    doc.put("status","Invalid key");
				doc.put("statusCode",code);
				doc.put("status","Session Invalid");
				return new ResponseEntity<String>(doc.toJson(), HttpStatus.OK);
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
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, null);
			}
	    }
	}
	/**
	 * 
	 * @param sessionkey
	 * @return
	 */
	@RequestMapping(value = "/getLocationTreeHierarchyForUser", method = RequestMethod.GET)
	public ResponseEntity<String> getLocationTreeHierarchyForUser(String sessionkey,String dbParamJson) {
		StringBuilder sb=new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		MongoDBConnection mongoSingle=null;
		MongoDatabase mongodb=null;
		MongoCollection<Document> table=null;
		String dbName= null;
		int code=0;
		mongoSingle = new MongoDBConnection(dbParamJson);
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		if (dbName != null) {
			mongodb = mongoSingle.getMongoDB(dbName);
		} else {
			mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
		}
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		try{
			    if(code==0)
				{
					Map<String,String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
					table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY);
					Document document=new Document();
					//Document companyObject=CommonUtility.getLocationTreeHierarchyForUser(linkedUserGroupDetails.get("userId"),linkedUserGroupDetails.get("organizationName"),table,mongodb);
					Document companyObject=CommonUtility.getLocationTreeHierarchyForUser(linkedUserGroupDetails.get("userId"),table,linkedUserGroupDetails.get("organizationName"),mongodb);
					Document companyObjectNew=(Document)companyObject.get("company");
					if (companyObjectNew != null) {
						    JSONArray array=(JSONArray)companyObjectNew.get("plant");
						    if(array.size()>0) {
						    	document.put("status", "Success");
								document.put("statusCode", 0);
								document.put("statusMessage", "getLocationTreeHierarchyForUser Details Sent");
								document.putAll((Map)companyObject);
						    }
						    else{
								document.put("status", "Failure");
								document.put("statusCode", 5001);
								document.put("statusMessage", "No Location Found");
							}
					}
					else{
						document.put("status", "Failure");
						document.put("statusCode", 5001);
						document.put("statusMessage", "No Location Found");
					}
					//document.putAll((Map)CommonUtility.getLocationTreeHierarchyForUser(linkedUserGroupDetails.get("userId"), table));
					return new ResponseEntity<String>(document.toJson(), HttpStatus.OK);
				}else if(code==1001){
					 sb.append("\"Invalid key\"");
					 MessageUtility.updateMessage(sb, code, "Session Invalid");
				}
				else if(code==2001){
				     sb.append("\"MetaData Connection\"");
					 MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
				}
			   return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
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
	 }
	
	
	@RequestMapping(value = "/getLocationTreeHierarchyForAdminUser", method = RequestMethod.GET)
	public ResponseEntity<String> getLocationTreeHierarchyForAdminUser(String sessionkey,String dbParamJson) {
		StringBuilder sb=new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		MongoDBConnection mongoSingle=null;
		MongoDatabase mongodb=null;
		MongoCollection<Document> table=null;
		String dbName= null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		mongoSingle = new MongoDBConnection(dbParamJson);
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
		try{
			if(code==0)
			{
				Map<String,String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
				table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY_TEMP);
				Document searchQuery=new Document();
				searchQuery.put("company.id", linkedUserGroupDetails.get("organizationName")); 
				searchQuery.put("edit_user_id",linkedUserGroupDetails.get("userId"));
				FindIterable<Document> sessioncursor = table.find(searchQuery);
				Document document=new Document();
				Map<String, Integer> llatestDeviceStatus=new WeakHashMap<String,Integer> ();
				if(sessioncursor.iterator().hasNext()){
					Document object=(Document)sessioncursor.iterator().next();
					document.put("status", "Success");
					document.put("statusCode", 0);
					document.put("statusMessage", "getLocationTreeHierarchyForAdminUser Details Sent");
					//llatestDeviceStatus=CommonUtility.getLatestDeviceStatus(mongodb, linkedUserGroupDetails.get("organizationName"));
					object=CommonUtility.traverseObject(object,linkedUserGroupDetails.get("userId"),llatestDeviceStatus,false);
					document.putAll((Map)object);
				}else{
					document.put("status", "Failure");
					document.put("statusCode", 5001);
					document.put("statusMessage", "No Location Found");
				}
				//document.putAll((Map)CommonUtility.getLocationTreeHierarchyForUser(linkedUserGroupDetails.get("userId"), table));
				return new ResponseEntity<String>(document.toJson(), HttpStatus.OK);
			}else if(code==1001){
				 sb.append("\"Invalid key\"");
				 MessageUtility.updateMessage(sb, code, "Session Invalid");
			}
			else if(code==2001){
			     sb.append("\"MetaData Connection\"");
				 MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
			}
		   return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
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
	 }
	@PostMapping("/uploadLocationHierarchy")
	public ResponseEntity<String>  uploadLocationHierarchy(
			@RequestParam("file") MultipartFile file,String sessionkey,String dbParamJson)  {
			StringBuilder sb=new StringBuilder();
			String dbName= null;
			if(dbParamJson!=null && dbParamJson.length()>0) {
				 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
				 dbName = dbParamJsonDoc.getString("db_name");
			 }
		    sb.append("{");
			sb.append("\"status\":");
			int code=0;
			if (dbName != null) {
				code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
			} else {
				code = CommonUtility.validateAuthenticationKey(sessionkey);
			}
			MongoDBConnection mongoSingle=null;
			MongoDatabase mongodb=null;
			MongoCollection<Document> table=null;
			try{
				if(code==0){
					mongoSingle=new MongoDBConnection(dbParamJson);
					if (dbName != null) {
						mongodb = mongoSingle.getMongoDB(dbName);
					} else {
						mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
					}
					String fileName=file.getOriginalFilename();
					BasicDBObject fileUploadObject=new BasicDBObject();
					if(fileName.endsWith(".xlsx")){
					String uploadedFileLocation = UserController.class.getProtectionDomain().getCodeSource().getLocation().getPath()
							+fileName;
					FileUtility.writeToFile(file.getInputStream(), uploadedFileLocation);
					Map<String,String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
					Map<String,String>  lMap=UserFileUtility.readExcelFileAndConstructJson(uploadedFileLocation,linkedUserGroupDetails.get("organizationName"));
					if(lMap.get("company_tree")!=null){
						table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY_TEMP);
						Document document=new Document();
						Document searchQuery=new Document();
						searchQuery.put("company.id", linkedUserGroupDetails.get("organizationName"));
						FindIterable<Document> sessioncursor = table.find(searchQuery);
						if(sessioncursor.iterator().hasNext()){
							Document object=(Document)sessioncursor.iterator().next();
							object.put("type", "new");
							BasicDBObject command = new BasicDBObject();
							BasicDBObject updateData=new BasicDBObject();
							updateData.put("company",JSON.parse(lMap.get("company_tree")));
							command.put("$set", updateData);
							table.updateOne(searchQuery, command);
							fileUploadObject.put("status", "Success");
							fileUploadObject.put("statusCode", 0);
							fileUploadObject.put("statusMessage", "Location Hierarchy Updated Successfully");
							fileUploadObject.put("company_id", object.get("_id").toString());
						}else{
							document.put("company", JSON.parse(lMap.get("company_tree")));
							table.insertOne(document);
							ObjectId objectId = (ObjectId)document.get("_id");
							fileUploadObject.put("status", "Success");
							fileUploadObject.put("statusCode", 0);
							fileUploadObject.put("statusMessage", "Location Hierarchy Saved Successfully");
							fileUploadObject.put("company_id", objectId.toString());
						}
						
					}else{
						fileUploadObject.put("status", "Failure");
						fileUploadObject.put("statusCode", 5000);
						fileUploadObject.put("statusMessage", "Excel file upload failure");
						fileUploadObject.put("error", JSON.parse(lMap.get("error")));
					}
				}
				else{
					fileUploadObject.put("status", "Invalid File");
					fileUploadObject.put("statusCode", 5000);
					fileUploadObject.put("statusMessage", "Please upload only excel file");
				}
					return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
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
  }
	
	/**
	 * 
	 * @param uploadedInputStream
	 * @param fileDetail
	 * @param sessionkey
	 * @return
	 */
	//@PostMapping("/uploadSiteImages")
	@RequestMapping(value = "/uploadSiteImages", method = RequestMethod.POST)
	public ResponseEntity<String> uploadSiteImages(
			@RequestParam("file") MultipartFile file, String sessionkey, String siteid, String json,String dbParamJson)  {
			StringBuilder sb=new StringBuilder();
			String dbName= null;
			if(dbParamJson!=null && dbParamJson.length()>0) {
				 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
				 dbName = dbParamJsonDoc.getString("db_name");
			 }
		    sb.append("{");
			sb.append("\"status\":");
			int code=0;
			if (dbName != null) {
				code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
			} else {
				code = CommonUtility.validateAuthenticationKey(sessionkey);
			}
			code=CommonUtility.validateAuthenticationKey(sessionkey);
			MongoDBConnection mongoSingle=null;
			MongoDatabase mongodb=null;
			MongoCollection<Document> table=null;
			BufferedImage resizeImg=null;
			try{
				if(code==0){
					mongoSingle=new MongoDBConnection(dbParamJson);
					if (dbName != null) {
						mongodb = mongoSingle.getMongoDB(dbName);
					} else {
						mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
					}
					Map<String,String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
					String fileName=linkedUserGroupDetails.get("organizationName")+"_"+siteid+".jpg";
					BasicDBObject fileUploadObject=new BasicDBObject();
					String uploadedFileLocation = prop.getProperty("SITE_UPLOAD_LOCATION")+"/"+fileName;
					String thumbuploadedFileLocation = prop.getProperty("SITE_UPLOAD_LOCATION")+"/"+"t"+"_"+fileName;
					FileUtility.writeToFile(file.getInputStream(), uploadedFileLocation);
					File f = new File(prop.getProperty("SITE_UPLOAD_LOCATION")+"/"+fileName);
					BufferedImage img = ImageIO.read(f); // load image	
					
				    int maxWidth = 1280;
				    int maxHeight = 603;
				    
				    if(img.getWidth() <= maxWidth && img.getHeight() <= maxHeight){
				    	resizeImg = img;
				    }
				    
				    if(img.getWidth() > maxWidth){
				    	resizeImg = Scalr.resize(img,Method.ULTRA_QUALITY,
				    			Mode.FIT_TO_WIDTH,
			                    maxWidth, maxHeight,
			                    Scalr.OP_ANTIALIAS);
				    }
				    if(resizeImg != null) {
					    if(resizeImg.getHeight() > maxHeight){
					    	resizeImg = Scalr.resize(resizeImg,Method.ULTRA_QUALITY,
					    			Mode.FIT_EXACT,
				                    maxWidth, maxHeight,
				                    Scalr.OP_ANTIALIAS);
					    }
				    } 
				    if(resizeImg == null){
				    	if(img.getHeight() > maxHeight){
					    	resizeImg = Scalr.resize(img,Method.ULTRA_QUALITY,
					    			Mode.FIT_EXACT,
				                    maxWidth, maxHeight,
				                    Scalr.OP_ANTIALIAS);
					    }
				    }
				    
				    ByteArrayOutputStream os = new ByteArrayOutputStream();
				    ImageIO.write(resizeImg,"jpg", os);
				    InputStream is = new ByteArrayInputStream(os.toByteArray());
				    FileUtility.writeToFile(is, uploadedFileLocation);
				    				    
					BufferedImage thumbImg = Scalr.resize(resizeImg,Method.ULTRA_QUALITY,
		                    Mode.FIT_TO_HEIGHT,
		                    100, 100,
		                    Scalr.OP_ANTIALIAS);
					File thumbFileLoc = new File(thumbuploadedFileLocation);
					ImageIO.write(thumbImg, "jpg", thumbFileLoc);
					fileUploadObject.put("status", "Success");
					fileUploadObject.put("statusCode", code);
					fileUploadObject.put("statusMessage", "uploadSiteImages Success");
					fileUploadObject.put("fileUploadLocation", uploadedFileLocation);
					fileUploadObject.put("fileUploadThumbLocation", thumbuploadedFileLocation);
					fileUploadObject.put("fileName", fileName);
					fileUploadObject.put("thumbNailName", "t_"+fileName);
					
					Document doc=new Document();
					doc.put("image_id", fileName);
					doc.put("thumb_image_id", "t_"+fileName);
					doc.put("image_location", prop.getProperty("SITE_UPLOAD_DIR_LOCATION"));
					doc.put("image_height", resizeImg.getHeight());
					doc.put("image_width", resizeImg.getWidth());
					doc.put("image_unit", "pixels");
					
					if(json.length()>0){
						BasicDBObject jsonDocument=(BasicDBObject) JSON.parse(json);
						doc.putAll((Map)jsonDocument);
					}
					
					table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY_TEMP);
					UserUtility.updateLocationHierarchyObject("site", "site", doc.toJson(), siteid, linkedUserGroupDetails, table);
					img.flush();
					thumbImg.flush();
					return new ResponseEntity<String>(fileUploadObject.toString(), HttpStatus.OK);
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
		}catch(Exception e){
			 sb=new StringBuilder();
		     sb.append("{");
			 sb.append("\"status\":");
			 sb.append("\"MetaData Connection\"");
			 MessageUtility.updateMessageWithErrors(sb, 2001, "MetaData Connection Failure",e);
			 return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		  }
		finally{
			CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
	  }	
  }
  
	/**
	 *  
	 * @param uploadedInputStream
	 * @param fileDetail
	 * @param sessionkey
	 * @return
	 */
	//@PostMapping("/uploadDeviceImages")
	@RequestMapping(value = "/uploadDeviceImages", method = RequestMethod.POST)
	public ResponseEntity<String> uploadDeviceImages(
			@RequestParam("file") MultipartFile file, String sessionkey, String plantid, String siteid, 
			String unitid, String assetid, String deviceid, String json,String dbParamJson)  {
			StringBuilder sb=new StringBuilder();
			String dbName= null;
			if(dbParamJson!=null && dbParamJson.length()>0) {
				 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
				 dbName = dbParamJsonDoc.getString("db_name");
			 }
		    sb.append("{");
			sb.append("\"status\":");
			int code=0;
			if (dbName != null) {
				code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
			} else {
				code = CommonUtility.validateAuthenticationKey(sessionkey);
			}
			MongoDBConnection mongoSingle=null;
			MongoDatabase mongodb=null;
			MongoCollection<Document> table=null;
			BufferedImage resizeImg=null;
			try{
				if(code==0){
					mongoSingle=new MongoDBConnection(dbParamJson);
					if (dbName != null) {
						mongodb = mongoSingle.getMongoDB(dbName);
					} else {
						mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
					}
					Map<String,String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
					String fileName=linkedUserGroupDetails.get("organizationName")+"_"+plantid+"_"+siteid+"_"+unitid+"_"+deviceid+".jpg";
					BasicDBObject fileUploadObject=new BasicDBObject();
					String uploadedFileLocation =prop.getProperty("ASSET_UPLOAD_LOCATION")+"/"+plantid+"/"+siteid+"/"+assetid+"/device_images/"+fileName;
					String thumbuploadedFileLocation =prop.getProperty("ASSET_UPLOAD_LOCATION")+"/"+plantid+"/"+siteid+"/"+assetid+"/device_images/"+"t"+"_"+fileName;
					File directory = new File(prop.getProperty("ASSET_UPLOAD_LOCATION")+"/"+plantid+"/"+siteid+"/"+assetid+"/device_images");
					
					if (!directory.exists()) {
						 directory.mkdirs();
					 }
					FileUtility.writeToFile(file.getInputStream(), uploadedFileLocation);
					File f = new File(directory+"/"+fileName);
					BufferedImage img = ImageIO.read(f); // load image	
					
				    int maxHeight = 600;
				    int maxThumbnailHeight = 300;
				    
				    resizeImg = img;
				    if(img.getHeight() > maxHeight){
				    	resizeImg = Scalr.resize(img, Method.ULTRA_QUALITY, Mode.FIT_TO_HEIGHT, maxHeight, Scalr.OP_ANTIALIAS);
				    }
				    				    
				    ByteArrayOutputStream os = new ByteArrayOutputStream();
				    ImageIO.write(resizeImg,"jpg", os);
				    InputStream is = new ByteArrayInputStream(os.toByteArray());
				    FileUtility.writeToFile(is, uploadedFileLocation);
				    
					BufferedImage thumbImg = Scalr.resize(resizeImg, Method.ULTRA_QUALITY, Mode.FIT_TO_HEIGHT, maxThumbnailHeight, Scalr.OP_ANTIALIAS);
					File thumbFileLoc = new File(thumbuploadedFileLocation);
					ImageIO.write(thumbImg, "jpg", thumbFileLoc);
					fileUploadObject.put("status", "Success");
					fileUploadObject.put("statusCode", code);
					fileUploadObject.put("statusMessage", "uploadDeviceImages Success");
					fileUploadObject.put("DeviceFileUploadLocation", plantid+"/"+siteid+"/"+assetid+"/device_images");
					fileUploadObject.put("DeviceFileUploadThumbLocation", plantid+"/"+siteid+"/"+assetid+"/device_images");
					fileUploadObject.put("DeviceFileName", fileName);
					fileUploadObject.put("DeviceThumbNailName", "t_"+fileName);
					
					Document doc=new Document();
					doc.put("image_id", fileName);
					doc.put("thumb_image_id", "t_"+fileName);
					doc.put("image_location", plantid+"/"+siteid+"/"+assetid);
					doc.put("image_height", resizeImg.getHeight());
					doc.put("image_width", resizeImg.getWidth());
					doc.put("image_unit", "pixels");
					
					if(json != null && json.length()>0){
						BasicDBObject jsonDocument=(BasicDBObject) JSON.parse(json);
						doc.putAll((Map)jsonDocument);
					}
					
					table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY_TEMP);
					UserUtility.updateLocationHierarchyObject("device", "device", doc.toJson(), deviceid, linkedUserGroupDetails, table);
					img.flush();
					thumbImg.flush();
					return new ResponseEntity<String>(fileUploadObject.toString(), HttpStatus.OK);
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
		}catch(Exception e){
			 sb=new StringBuilder();
		     sb.append("{");
			 sb.append("\"status\":");
			 sb.append("\"MetaData Connection\"");
			 MessageUtility.updateMessageWithErrors(sb, 2001, "MetaData Connection Failure",e);
			 return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		  }
		finally{
			CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
	  }	
  }
	
	
	/**
	 * 
	 * @param file
	 * @param sessionkey
	 * @param plantid
	 * @param siteid
	 * @param unitid
	 * @param assetid
	 * @param deviceid
	 * @param json
	 * @return
	 */
	//@PostMapping("/uploadAssetImages")
	@RequestMapping(value = "/uploadAssetImages", method = RequestMethod.POST)
	public ResponseEntity<String> uploadAssetImages(
			@RequestParam("file") MultipartFile file, String sessionkey, String plantid, String siteid, 
			String unitid, String assetid, String deviceid, String json,String dbParamJson)  {
			StringBuilder sb=new StringBuilder();
			String dbName= null;
			if(dbParamJson!=null && dbParamJson.length()>0) {
				 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
				 dbName = dbParamJsonDoc.getString("db_name");
			 }
		    sb.append("{");
			sb.append("\"status\":");
			int code=0;
			if (dbName != null) {
				code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
			} else {
				code = CommonUtility.validateAuthenticationKey(sessionkey);
			}
			MongoDBConnection mongoSingle=null;
			MongoDatabase mongodb=null;
			MongoCollection<Document> table=null;
			BufferedImage resizeImg=null;
			try{
				if(code==0){
					mongoSingle=new MongoDBConnection(dbParamJson);
					if (dbName != null) {
						mongodb = mongoSingle.getMongoDB(dbName);
					} else {
						mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
					}
					Map<String,String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
					String fileName=linkedUserGroupDetails.get("organizationName")+"_"+plantid+"_"+siteid+"_"+unitid+"_"+assetid+".jpg";
					BasicDBObject fileUploadObject=new BasicDBObject();
					String uploadedFileLocation =prop.getProperty("ASSET_UPLOAD_LOCATION")+"/"+plantid+"/"+siteid+"/"+assetid+"/"+fileName;
					String thumbuploadedFileLocation =prop.getProperty("ASSET_UPLOAD_LOCATION")+"/"+plantid+"/"+siteid+"/"+assetid+"/"+"t"+"_"+fileName;
					File directory = new File(prop.getProperty("ASSET_UPLOAD_LOCATION")+"/"+plantid+"/"+siteid+"/"+assetid);
					if (!directory.exists()) {
						 directory.mkdirs();
					 }
					FileUtility.writeToFile(file.getInputStream(), uploadedFileLocation);
					File f = new File(directory+"/"+fileName);
					BufferedImage img = ImageIO.read(f); // load image	
					
				    int maxHeight = 600;
				    int maxThumbnailHeight = 300;
				    
				    resizeImg = img;
				    if(img.getHeight() > maxHeight){
				    	resizeImg = Scalr.resize(img, Method.ULTRA_QUALITY, Mode.FIT_TO_HEIGHT, maxHeight, Scalr.OP_ANTIALIAS);
				    }
				    				    
				    ByteArrayOutputStream os = new ByteArrayOutputStream();
				    ImageIO.write(resizeImg,"jpg", os);
				    InputStream is = new ByteArrayInputStream(os.toByteArray());
				    FileUtility.writeToFile(is, uploadedFileLocation);
				    
					BufferedImage thumbImg = Scalr.resize(resizeImg, Method.ULTRA_QUALITY, Mode.FIT_TO_HEIGHT, maxThumbnailHeight, Scalr.OP_ANTIALIAS);
					File thumbFileLoc = new File(thumbuploadedFileLocation);
					ImageIO.write(thumbImg, "jpg", thumbFileLoc);
					fileUploadObject.put("status", "Success");
					fileUploadObject.put("statusCode", code);
					fileUploadObject.put("statusMessage", "uploadAssetImages Success");
					fileUploadObject.put("AssetFileUploadLocation", plantid+"/"+siteid+"/"+assetid);
					fileUploadObject.put("AssetFileUploadThumbLocation", plantid+"/"+siteid+"/"+assetid);
					fileUploadObject.put("AssetFileName", fileName);
					fileUploadObject.put("AssetThumbNailName", "t_"+fileName);
					
					Document doc=new Document();
					doc.put("image_id", fileName);
					doc.put("thumb_image_id", "t_"+fileName);
					doc.put("image_location", plantid+"/"+siteid+"/"+assetid);
					doc.put("image_height", resizeImg.getHeight());
					doc.put("image_width", resizeImg.getWidth());
					doc.put("image_unit", "pixels");
					
					if(json != null && json.length()>0){
						BasicDBObject jsonDocument=(BasicDBObject) JSON.parse(json);
						doc.putAll((Map)jsonDocument);
					}
					
					table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY_TEMP);
					UserUtility.updateLocationHierarchyObject("asset", "asset", doc.toJson(), assetid, linkedUserGroupDetails, table);
					img.flush();
					thumbImg.flush();
					return new ResponseEntity<String>(fileUploadObject.toString(), HttpStatus.OK);
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
		}catch(Exception e){
			 sb=new StringBuilder();
		     sb.append("{");
			 sb.append("\"status\":");
			 sb.append("\"MetaData Connection\"");
			 MessageUtility.updateMessageWithErrors(sb, 2001, "MetaData Connection Failure",e);
			 return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		  }
		finally{
			CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
	  }		
  }
	/**
	 * 
	 * @param file
	 * @param sessionkey
	 * @param plantid
	 * @param siteid
	 * @param unitid
	 * @param assetid
	 * @param deviceid
	 * @param channelid
	 * @param json
	 * @return
	 */
	//@PostMapping("/uploadChannelImages")
	@RequestMapping(value = "/uploadChannelImages", method = RequestMethod.POST)
	public ResponseEntity<String> uploadChannelImages(
			@RequestParam("file") MultipartFile file, String sessionkey, String plantid, String siteid, 
			String unitid, String assetid, String deviceid,String channelid, String json,String dbParamJson)  {
			StringBuilder sb=new StringBuilder();
			String dbName= null;
			if(dbParamJson!=null && dbParamJson.length()>0) {
				 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
				 dbName = dbParamJsonDoc.getString("db_name");
			 }
		    sb.append("{");
			sb.append("\"status\":");
			int code=0;
			if (dbName != null) {
				code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
			} else {
				code = CommonUtility.validateAuthenticationKey(sessionkey);
			}
			MongoDBConnection mongoSingle=null;
			MongoDatabase mongodb=null;
			MongoCollection<Document> table=null;
			BufferedImage resizeImg=null;
			try{
				if(code==0){
					mongoSingle=new MongoDBConnection(dbParamJson);
					if (dbName != null) {
						mongodb = mongoSingle.getMongoDB(dbName);
					} else {
						mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
					}
					Map<String,String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
					String fileName=linkedUserGroupDetails.get("organizationName")+"_"+plantid+"_"+siteid+"_"+unitid+"_"+deviceid+channelid+".jpg";
					BasicDBObject fileUploadObject=new BasicDBObject();
					String uploadedFileLocation =prop.getProperty("ASSET_UPLOAD_LOCATION")+"/"+plantid+"/"+siteid+"/"+assetid+"/channel_images/"+fileName;
					String thumbuploadedFileLocation =prop.getProperty("ASSET_UPLOAD_LOCATION")+"/"+plantid+"/"+siteid+"/"+assetid+"/channel_images/"+"t"+"_"+fileName;
					File directory = new File(prop.getProperty("ASSET_UPLOAD_LOCATION")+"/"+plantid+"/"+siteid+"/"+assetid+"/channel_images");
					if (!directory.exists()) {
						 directory.mkdirs();
					 }
					FileUtility.writeToFile(file.getInputStream(), uploadedFileLocation);
					File f = new File(directory+"/"+fileName);
					BufferedImage img = ImageIO.read(f); // load image	
					
				    int maxHeight = 600;
				    int maxThumbnailHeight = 300;
				    
				    resizeImg = img;
				    if(img.getHeight() > maxHeight){
				    	resizeImg = Scalr.resize(img, Method.ULTRA_QUALITY, Mode.FIT_TO_HEIGHT, maxHeight, Scalr.OP_ANTIALIAS);
				    }
				    				    
				    ByteArrayOutputStream os = new ByteArrayOutputStream();
				    ImageIO.write(resizeImg,"jpg", os);
				    InputStream is = new ByteArrayInputStream(os.toByteArray());
				    FileUtility.writeToFile(is, uploadedFileLocation);
				    
					BufferedImage thumbImg = Scalr.resize(resizeImg, Method.ULTRA_QUALITY, Mode.FIT_TO_HEIGHT, maxThumbnailHeight, Scalr.OP_ANTIALIAS);
					File thumbFileLoc = new File(thumbuploadedFileLocation);
					ImageIO.write(thumbImg, "jpg", thumbFileLoc);
					fileUploadObject.put("status", "Success");
					fileUploadObject.put("statusCode", code);
					fileUploadObject.put("statusMessage", "uploadChannelImage Success");
					fileUploadObject.put("ChannelFileUploadLocation", plantid+"/"+siteid+"/"+assetid+"/channel_images");
					fileUploadObject.put("ChannelFileUploadThumbLocation", plantid+"/"+siteid+"/"+assetid+"/channel_images");
					fileUploadObject.put("ChannelFileName", fileName);
					fileUploadObject.put("ChannelThumbNailName", "t_"+fileName);
					
					Document doc=new Document();
					doc.put("image_id", fileName);
					doc.put("thumb_image_id", "t_"+fileName);
					doc.put("image_location", plantid+"/"+siteid+"/"+assetid+"/channel_images");
					doc.put("image_height", resizeImg.getHeight());
					doc.put("image_width", resizeImg.getWidth());
					doc.put("image_unit", "pixels");
					
					if(json != null && json.length()>0){
						BasicDBObject jsonDocument=(BasicDBObject) JSON.parse(json);
						doc.putAll((Map)jsonDocument);
					}
					
					table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY_TEMP);
					UserUtility.updateLocationHierarchyObject("channel", "channel", doc.toJson(), channelid, linkedUserGroupDetails, table);
					img.flush();
					thumbImg.flush();
					return new ResponseEntity<String>(fileUploadObject.toString(), HttpStatus.OK);
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
			}catch(Exception e){
				 sb=new StringBuilder();
			     sb.append("{");
				 sb.append("\"status\":");
				 sb.append("\"MetaData Connection\"");
				 MessageUtility.updateMessageWithErrors(sb, 2001, "MetaData Connection Failure",e);
				 return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
			  }
			finally{
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
		  }			
  }
	
	@RequestMapping(value = "/updateLocationHierarchyObject", method = RequestMethod.POST)
	public ResponseEntity<String> updateLocationHierarchyObject(String json,String sessionkey,String type,String subtype,String objectId,String dbParamJson) {
			StringBuilder sb=new StringBuilder();
			String dbName= null;
			if(dbParamJson!=null && dbParamJson.length()>0) {
				 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
				 dbName = dbParamJsonDoc.getString("db_name");
			 }
		    sb.append("{");
			sb.append("\"status\":");
			int code=0;
			if (dbName != null) {
				code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
			} else {
				code = CommonUtility.validateAuthenticationKey(sessionkey);
			}
			MongoDBConnection mongoSingle=null;
			MongoDatabase mongodb=null;
			MongoCollection<Document> table=null;
			try{
				if(code==0){
					mongoSingle=new MongoDBConnection(dbParamJson);
					if (dbName != null) {
						mongodb = mongoSingle.getMongoDB(dbName);
					} else {
						mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
					}
					table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY_TEMP);
					Map<String,String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
					if(json.length()>0){
						return new ResponseEntity<String>(UserUtility.updateLocationHierarchyObject(type, subtype, json, objectId, linkedUserGroupDetails, table).toString(),HttpStatus.OK);
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
			
  }

	@RequestMapping(value = "/upsertLocationHierarchyObject", method = RequestMethod.POST)
	public ResponseEntity<String> upsertLocationHierarchyObject(String json,String sessionkey,String type,String subtype,
			String parentObjectId,String objectId,String dbParamJson) {
			String dbName = null;
			if (dbParamJson != null && dbParamJson.length() > 0) {
				BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
				dbName = dbParamJsonDoc.getString("db_name");
			}
			StringBuilder sb=new StringBuilder();
		    sb.append("{");
			sb.append("\"status\":");
			int code=0;
			if (dbName != null) {
				code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
			} else {
				code = CommonUtility.validateAuthenticationKey(sessionkey);
			}
			MongoDBConnection mongoSingle=null;
			MongoDatabase mongodb=null;
			MongoCollection<Document> table=null;
			try{
				if(code==0){
					mongoSingle=new MongoDBConnection(dbParamJson);
					if (dbName != null) {
						mongodb = mongoSingle.getMongoDB(dbName);
					} else {
						mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
					}
					table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY_TEMP);
					Map<String,String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
					if(json.length()>0){
						return new ResponseEntity<String>(UserUtility.upsertLocationHierarchyObject(type, subtype, json, parentObjectId, linkedUserGroupDetails, table,objectId).toString(),HttpStatus.OK);
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
  }
	
	/**
	 * 
	 * @param sessionkey
	 * @param type
	 * @return
	 */
	@RequestMapping(value = "/previewLocationTreeHierarchy", method = RequestMethod.GET)
	public ResponseEntity<String> previewLocationTreeHierarchy(String sessionkey,String type,String objectId,String dbParamJson) {
		StringBuilder sb=new StringBuilder();
		String dbName= null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		sb.append("{");
		sb.append("\"status\":");
		MongoDBConnection mongoSingle=null;
		MongoDatabase mongodb=null;
		MongoCollection<Document> table=null;
		int code=0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		try{
			 if(code==0)
				{
					mongoSingle=new MongoDBConnection(dbParamJson);
					if (dbName != null) {
						mongodb = mongoSingle.getMongoDB(dbName);
					} else {
						mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
					}
					table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY_TEMP);
					Map<String,String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
					Document document=new Document();
					Document searchQuery = new Document();
					searchQuery.put("company.id", linkedUserGroupDetails.get("organizationName")); 
					searchQuery = new Document("_id",new ObjectId(objectId));
					FindIterable<Document> sessioncursor = table.find(searchQuery);
					if(sessioncursor.iterator().hasNext()){
						document=(Document)sessioncursor.iterator().next();
						document.put("overallStatus", UserUtility.checkAllSiteImagesUploaded((Document)document.get("company")));
						document.put("status", "Success");
						document.put("statusCode", 0);
						document.put("statusMessage", "previewLocationTreeHierarchy Details Sent");
						
					}else{
						document.put("status", "Success");
						document.put("statusCode", 5001);
						document.put("statusMessage", "No Preview Found");
					}
					if(type!=null && type.equalsIgnoreCase("Complete")){
						Map<ObjectDO,Document> lSiteMapDocuments=UserUtility.getSiteObjectFromTempTable(table,objectId);
						document.remove("_id");
						document.remove("type");
						document.remove("status");
						document.remove("statusCode");
						document.remove("edit_user_id");
						document.remove("statusMessage");
						document.remove("overallStatus");
						Document company=(Document) document.get("company");
						company.remove("site_status");
						company.remove("unit_status");
						company.remove("in_complete_unit_list");
						company.remove("in_complete_site_list");
						Document companyObj=new Document();
						companyObj.put("company", company);
						table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY);
						searchQuery = new Document();
						searchQuery.put("company.id", linkedUserGroupDetails.get("organizationName"));
						searchQuery.put("version_control.active_flag", true);
						/*FindIterable<Document> originalsessioncursor = table.find(searchQuery);
						if(originalsessioncursor.iterator().hasNext()){
							originalDocument=(Document)originalsessioncursor.iterator().next();
						}*/
						Document indexDocument=new Document();
						indexDocument.put("$**", "text");
						table.createIndex(indexDocument);
						
						sessioncursor = table.find(searchQuery);
						if(sessioncursor.iterator().hasNext()){
							Document companyObject=(Document)sessioncursor.iterator().next();
							
							if(companyObject.get("version_control")==null){
								Document versionControl =new Document();
								versionControl.put("version", 1);
								versionControl.put("version_date",new java.util.Date());
								versionControl.put("userId",linkedUserGroupDetails.get("userId"));
								versionControl.put("active_flag", true);
								companyObject.put("version_control",versionControl);
							}
							else{
								Document versionControl =new Document();
								Document versionControlObj=(Document)companyObject.get("version_control");
								versionControl.put("version", (versionControlObj.getInteger("version")+1));
								versionControl.put("version_date",new java.util.Date());
								versionControl.put("userId",linkedUserGroupDetails.get("userId"));
								versionControl.put("active_flag", true);
								companyObject.put("version_control",versionControl);
								
								BasicDBObject updateData = new BasicDBObject();
								updateData.put("version_control.active_flag", false);
								BasicDBObject command = new BasicDBObject();
							    command.put("$set", updateData);
								table.updateMany(searchQuery, command);
								
								Document locationHierarchyObject=UserUtility.mergeLocationHierarchyDocuments(lSiteMapDocuments, companyObject);
								companyObject.put("company",locationHierarchyObject);
								companyObject.remove("_id");
								Document insertLocationHierarchyData = new Document();
								insertLocationHierarchyData.putAll((Map)companyObject);
								table.insertOne(insertLocationHierarchyData);
							}			
							
							document= new Document();
							document.put("status", "Success");
							document.put("statusCode",code);
							document.put("statusMessage", "LocationTreeHierarchy updated successfully");
						}
						else{
							table.insertOne(document);
							ObjectId oid = (ObjectId)document.get("_id");
							document=new Document();
							document.put("status", "Success");
							document.put("statusCode", code);
							document.put("statusMessage", "LocationTreeHierarchy successfully moved to transaction table");
							document.put("companyId", oid.toString());
					     }
							table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY_TEMP);
							searchQuery = new Document("_id",new ObjectId(objectId));
							searchQuery.put("company.id", linkedUserGroupDetails.get("organizationName")); 
							table.deleteOne(searchQuery);
						
					}
					
					return new ResponseEntity<String>(document.toJson(), HttpStatus.OK);
				}else if(code==1001){
					 sb.append("\"Invalid key\"");
					 MessageUtility.updateMessage(sb, code, "Session Invalid");
				}
				else if(code==2001){
				     sb.append("\"MetaData Connection\"");
					 MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
				}
			   return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
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
	 }
	 
	/**
	 * 
	 * @param sessionkey
	 * @return
	 */
	@RequestMapping(value = "/editLocationTreeHierarchy", method = RequestMethod.GET)
	public ResponseEntity<String> editLocationTreeHierarchy(String sessionkey,String type,String objectId,String dbParamJson) {
		StringBuilder sb=new StringBuilder();
		String dbName= null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		sb.append("{");
		sb.append("\"status\":");
		MongoDBConnection mongoSingle=null;
		MongoDatabase mongodb=null;
		MongoCollection<Document> table=null;
		int code=0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		boolean locationHierarchyAvailable=false;
		try{
			if(code==0)
			{
				mongoSingle=new MongoDBConnection(dbParamJson);
				if (dbName != null) {
					mongodb = mongoSingle.getMongoDB(dbName);
				} else {
					mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				}
				Map<String,String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
				Document document=new Document();
				Document searchQuery = new Document();
				
				table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY_TEMP);
				if(type!=null && type.equalsIgnoreCase("cancel")){
					searchQuery = new Document("_id",new ObjectId(objectId));
					table.deleteOne(searchQuery);
					document.put("status", "Success");
					document.put("statusCode",0);
					document.put("statusMessage", "LocationTreeHierarchy deleted from temporary table");
					return new ResponseEntity<String>(document.toJson(), HttpStatus.OK);
				}
				Map<String,ObjectDO> lSiteInfoDocuments=UserUtility.getSitesFromTempTable(table, linkedUserGroupDetails.get("userId"), 
						linkedUserGroupDetails.get("organizationName"),linkedUserGroupDetails.get("userDisplayName"));
				
				searchQuery = new Document();
				searchQuery.put("company.id", linkedUserGroupDetails.get("organizationName")); 
				searchQuery.put("version_control.active_flag",true);
				table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY);
				FindIterable<Document> sessioncursor = table.find(searchQuery);
				if(sessioncursor.iterator().hasNext()){
					locationHierarchyAvailable=true;
					document=(Document)sessioncursor.iterator().next();
					/**********New Requirement Site Level Starts *******/
					//if(linkedUserGroupDetails.get("organizationName").equalsIgnoreCase("koch_ag")){
					document=UserUtility.filterLocationTreeHierarchyForSiteAdmin(linkedUserGroupDetails.get("userId"), document,lSiteInfoDocuments);
					//}
					/**********New Requirement Site Level Ends *******/
					if(document.get("statusCode")!=null && document.getInteger("statusCode")==7001){
						return new ResponseEntity<String>(document.toJson(), HttpStatus.OK);
					}
					else if(document.get("statusCode")!=null && document.getInteger("statusCode")==5001){
						return new ResponseEntity<String>(document.toJson(), HttpStatus.OK);
					}
				}
				else{
					document.put("status", "Success");
					document.put("statusCode", 5001);
					document.put("statusMessage", "User doesn't have permission to edit sites");
					return new ResponseEntity<String>(document.toJson(), HttpStatus.OK);
				}
				if(locationHierarchyAvailable){
					table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY_TEMP);
					Document indexDocument=new Document();
					indexDocument.put("modified_date", 1);
					IndexOptions idOptions=new IndexOptions();
					idOptions.expireAfter(1L, TimeUnit.DAYS);
					table.createIndex(indexDocument,idOptions);
					searchQuery = new Document();
					searchQuery.put("company.id", linkedUserGroupDetails.get("organizationName")); 
					searchQuery.put("edit_user_id", linkedUserGroupDetails.get("userId")); 
					sessioncursor = table.find(searchQuery);
					if(sessioncursor.iterator().hasNext()){
						Document object=(Document)sessioncursor.iterator().next();
						document= new Document();
						if(object.getString("edit_user_id")!=null && object.getString("edit_user_id").equalsIgnoreCase(linkedUserGroupDetails.get("userId"))){
							document.put("status", "Success");
							document.put("statusCode",0);
							document.put("statusMessage", "LocationTreeHierarchy already copied to temporary table");
							document.put("companyId", object.get("_id").toString());
						}else{
							document.put("status", "Success");
							document.put("statusCode",7001);
							document.put("statusMessage", "Location hierarcy is in edit mode by the user " +object.getString("edit_user_id"));
						}
					}
					else{
						document.remove("_id");
						document.put("type", "edit");
						document.put("edit_user_id", linkedUserGroupDetails.get("userId"));
						document.put("edit_user_display_name", linkedUserGroupDetails.get("userDisplayName"));
						document.put("created_date", new java.util.Date());
						document.put("modified_date", new java.util.Date());
						table.insertOne(document);
						ObjectId oid = (ObjectId)document.get("_id");
						document=new Document();
						document.put("status", "Success");
						document.put("statusCode", code);
						document.put("statusMessage", "LocationTreeHierarchy successfully copied to temporary table");
						document.put("companyId", oid.toString());
						
					}
				}else{
					document=new Document();
					document.put("status", "No Location Found");
					document.put("statusCode", 5001);
					document.put("statusMessage", "No Location Found");
				}
				  return new ResponseEntity<String>(document.toJson(), HttpStatus.OK);
			}else if(code==1001){
				 sb.append("\"Invalid key\"");
				 MessageUtility.updateMessage(sb, code, "Session Invalid");
			}
			else if(code==2001){
			     sb.append("\"MetaData Connection\"");
				 MessageUtility.updateMessage(sb, code, "MetaData Connection Failure");
			}
		   return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
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
	 }
	
	@RequestMapping(value = "/getAllSitesForLocation", method = RequestMethod.GET)
	public ResponseEntity<String> getAllSitesForLocation(String companyId,String dbParamJson) {
		StringBuilder sb=new StringBuilder();
		String dbName= null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		sb.append("{");
		sb.append("\"status\":");
		MongoDBConnection mongoSingle=null;
		MongoDatabase mongodb=null;
		MongoCollection<Document> table=null;
		try{
		    	mongoSingle=new MongoDBConnection(dbParamJson);
		    	if (dbName != null) {
					mongodb = mongoSingle.getMongoDB(dbName);
				} else {
					mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				}
				table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY);
				Document searchQuery = new Document();
				searchQuery.put("company.id", companyId);
				Document matchQuery = new Document();
				matchQuery.put("$match", searchQuery);
				
				BasicDBObject uwindPlant=new BasicDBObject("$unwind","$company.plant");
				BasicDBObject uwindSite=new BasicDBObject("$unwind","$company.plant.site");
				
				Document projectQuery = new Document();
				Document project = new Document();
		    	project.putAll(UserUtility.getSelectedFieldsForLocation());
		    	projectQuery.put("$project", project);
		    	AggregateIterable<Document> iterable=table.aggregate(Arrays.asList(matchQuery,uwindPlant,uwindSite,projectQuery));
		    	JSONArray array=new JSONArray();
		    	for (Document row : iterable) {
		    		Document siteObject=new Document();
		    		Document object=(Document)row.get("company");
		    		siteObject.put("company_id", object.get("id"));
		    		siteObject.put("company_name", object.get("name"));
		    		object=(Document) object.get("plant");
		    		object=(Document)object.get("site");
		    		siteObject.put("site_id", object.get("id"));
		    		siteObject.put("site_name", object.get("name"));
		    		array.add(siteObject);
		    	}
				Document document=new Document();
				document.put("site",array);
				document.put("status", "Success");
				document.put("statusCode", 0);
				document.put("statusMessage", "getAllSitesForLocation Details Sent");
				return new ResponseEntity<String>(document.toJson(), HttpStatus.OK);
				
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
	 }
	/**
	 * 
	 * @param sessionkey
	 * @return
	 */
	@RequestMapping(value = "/downloadTemplate", method = RequestMethod.GET)
	public ResponseEntity downloadTemplate(HttpServletResponse response,String sessionkey,String dbParamJson) {
		StringBuilder sb=new StringBuilder();
		String dbName= null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		sb.append("{");
		sb.append("\"status\":");
		int code=0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		try{	
			 if(code==0){
				 File file = null;
				 ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		         file = new File(classloader.getResource("Template.xlsx").getFile());
		         if(!file.exists()){
		             String errorMessage = "Sorry. The file you are looking for does not exist";
		             System.out.println("error message..."+errorMessage);
		             OutputStream outputStream = response.getOutputStream();
		             outputStream.write(errorMessage.getBytes(Charset.forName("UTF-8")));
		             outputStream.close();
		             sb.append("\"TemplateFile Not Avaialable\"");
					 MessageUtility.updateMessage(sb, 2001, "TemplateFile Not Avaialable");
		             return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
		         }
		         String mimeType= URLConnection.guessContentTypeFromName(file.getName());
		         if(mimeType==null){
		             System.out.println("mimetype is not detectable, will take default");
		             mimeType = "application/octet-stream";
		         }
		         response.setContentType(mimeType);
		         response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() +"\""));
		         response.setContentLength((int)file.length());
		         InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
		         //Copy bytes from source to destination(outputstream in this example), closes both streams.
		         FileCopyUtils.copy(inputStream, response.getOutputStream());
		         inputStream.close();   
			     
			 }else{
				 String errorMessage = "Sorry. The file you are looking for does not exist";
	             System.out.println("error message..."+errorMessage);
	             OutputStream outputStream = response.getOutputStream();
	             outputStream.write(errorMessage.getBytes(Charset.forName("UTF-8")));
	             outputStream.close();
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
			
	   }
		return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
	}

	@RequestMapping(value = "/checkDuplicateObjectId", method = RequestMethod.GET)
	public ResponseEntity<String> checkDuplicateObjectId(String sessionkey, String objectId,String objectType,String dbParamJson) {
		StringBuilder sb=new StringBuilder();
		String dbName= null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		sb.append("{");
		sb.append("\"status\":");
		MongoDBConnection mongoSingle=null;
		MongoDatabase mongodb=null;
		MongoCollection<Document> table=null;
		int code=0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		try{	
			 if(code==0){
				 mongoSingle=new MongoDBConnection(dbParamJson);
				if (dbName != null) {
					mongodb = mongoSingle.getMongoDB(dbName);
				} else {
					mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				}
				 table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY_TEMP);
				 Map<String,String> lQueryMap=new HashMap<String,String>();
				 lQueryMap.put("plant","company.plant.id");
				 lQueryMap.put("site","company.plant.site.id");
				 lQueryMap.put("unit","company.plant.site.unit.id");
				 lQueryMap.put("asset","company.plant.site.unit.asset.id");
				 lQueryMap.put("device_category","company.plant.site.unit.asset.device_category.id");
				 lQueryMap.put("device","company.plant.site.unit.aseet.device_category.device.id");
				 Document searchQuery = new Document();
				 if(lQueryMap.get(objectType)!=null){
					 searchQuery.put(lQueryMap.get(objectType), objectId);
				 }
				 FindIterable<Document> sessioncursor = table.find(searchQuery);
				 Document finalObject=new Document();
				 finalObject.put("system_generated_id", objectType+"_"+CommonUtility.generateRandomId());
				 if(sessioncursor.iterator().hasNext()){
						sessioncursor.iterator().next();
						finalObject.put("status", "Success");
						finalObject.put("statusCode", 5001);
						finalObject.put("statusMessage", "ObjectId already exists");
						
				 }else{
						finalObject.put("status", "Success");
						finalObject.put("statusCode", code);
						finalObject.put("statusMessage", "ObjectId Not avaialble");	
				 }
				 return new ResponseEntity<String>(finalObject.toJson(),HttpStatus.OK);	
			 }else{
			  	Document doc=new Document();
			    doc.put("status","Invalid key");
			    doc.put("statusCode",code);
			    doc.put("status","Session Invalid");
			    return new ResponseEntity<String>(doc.toJson(), HttpStatus.OK);
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
	}
	
	@RequestMapping(value = "/updateOrganizationName", method = RequestMethod.GET)
	public ResponseEntity<String>  updateOrganizationName( String sessionkey,String organizationName,String dbParamJson){
		String dbName= null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		StringBuilder sb=new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		int code=0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		MongoDBConnection mongoSingle=null;
		MongoDatabase mongodb=null;
		MongoCollection<Document> table=null;
		try{
		   if(code==0){
			   mongoSingle=new MongoDBConnection(dbParamJson);
			   if (dbName != null) {
					mongodb = mongoSingle.getMongoDB(dbName);
				} else {
					mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				}
			   CommonUtility.updateOrganizationName(mongodb,sessionkey,organizationName);
			   Document doc=new Document();
			   doc.put("status", "Success");
			   doc.put("statusCode", 0);
			   doc.put("statusMessage", "updateOrganizationName Sucess");
			   return new ResponseEntity<String>(doc.toJson(), HttpStatus.OK);
		    }else{
		    	Document doc=new Document();
			    doc.put("status","Invalid key");
				doc.put("statusCode",code);
				doc.put("status","Session Invalid");
				return new ResponseEntity<String>(doc.toJson(), HttpStatus.OK);
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
			CommonUtility.closeMongoConnection(mongoSingle, mongodb, null);
	    }
	
	}
	
	
	/**
	 * 
	 * @param sessionkey
	 * @param objectId
	 * @param objectType
	 * @return
	 */
	@RequestMapping(value = "/checkDuplicateObjectsAndGenerateId", method = RequestMethod.GET)
	public ResponseEntity<String> checkDuplicateObjectsAndGenerateId(String sessionkey,String json, String objectType,
			String sensorType,String dbParamJson) {
		StringBuilder sb=new StringBuilder();
		sb.append("{");
		sb.append("\"status\":");
		String dbName= null;
		if(dbParamJson!=null && dbParamJson.length()>0) {
			 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
			 dbName = dbParamJsonDoc.getString("db_name");
		 }
		MongoDBConnection mongoSingle=null;
		MongoDatabase mongodb=null;
		MongoCollection<Document> table=null;
		int code=0;
		if (dbName != null) {
			code = CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
		} else {
			code = CommonUtility.validateAuthenticationKey(sessionkey);
		}
		try{	
			 if(code==0){
				
				 Document finalObject=new Document();
				 if(objectType.equalsIgnoreCase("device_category")){
					 finalObject.put("system_generated_id", objectType+"_"+CommonUtility.generateRandomId());
					 finalObject.put("status", "Success");
					 finalObject.put("statusCode", code);
					 finalObject.put("statusMessage", "Object Name Not Avaialable");
					 return new ResponseEntity<String>(finalObject.toJson(), HttpStatus.OK);
				 }
				 mongoSingle=new MongoDBConnection(dbParamJson);
				 if (dbName != null) {
						mongodb = mongoSingle.getMongoDB(dbName);
					} else {
						mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
					}
				 table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY);
				 BasicDBObject jsonDocument=(BasicDBObject) JSON.parse(json);
				 Map<String,String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
					
				 if(objectType!=null && (objectType.equalsIgnoreCase("device") || objectType.equalsIgnoreCase("channel"))){
					BasicDBObject query=(BasicDBObject)jsonDocument.get("query");
					Document searchQuery=new Document();
					searchQuery.put("version_control.active_flag", true);
			    	searchQuery.put("company.id", linkedUserGroupDetails.get("organizationName"));
					Set<String> jsonKey=query.keySet();
    				for(String jsonIterateKey:jsonKey){
    					if(jsonIterateKey.contains("id")){
    						searchQuery.put(jsonIterateKey, query.get(jsonIterateKey));
    					}
    				}
    				Document matchQuery=new Document();
    				matchQuery.put("$match", searchQuery);
    				List<Document> listDocument=new ArrayList<Document>();
    				listDocument.add(matchQuery);
    				AggregateIterable<Document> iterable=table.aggregate(listDocument);
    				for (Document row : iterable) {
   					 finalObject.put("status", "Success");
   					 finalObject.put("statusCode", 5001);
   					 finalObject.put("statusMessage", "Object Id Already Exists");
   					return new ResponseEntity<String>(finalObject.toJson(), HttpStatus.OK);
   				 	} 
    				
   				 		
   				 	 finalObject.put("status", "Success");
  					 finalObject.put("statusCode", 0);
  					 finalObject.put("statusMessage", "Object Id Not avaialable");
  					 if(objectType!=null && (objectType.equalsIgnoreCase("device"))){
  					   finalObject.put("system_generated_id", query.get("company.plant.site.unit.asset.device_category.devivce.id"));
				     }
  					 if(objectType!=null && (objectType.equalsIgnoreCase("channel"))){
   					   finalObject.put("system_generated_id", query.get("company.plant.site.unit.asset.device_category.devivce.channel.id"));
 				     }
   					
  					return new ResponseEntity<String>(finalObject.toJson(), HttpStatus.OK);
   				 		
				 }
				
				 List<Document> unwindQueryDoc=UserUtility.getObjectHierarchy(objectType,jsonDocument,linkedUserGroupDetails.get("organizationName"));
				 AggregateIterable<Document> iterable=table.aggregate(unwindQueryDoc);
				 for (Document row : iterable) {
					 finalObject.put("status", "Success");
					 finalObject.put("statusCode", 5001);
					 finalObject.put("statusMessage", "Object Name Already Exists");
					return new ResponseEntity<String>(finalObject.toJson(), HttpStatus.OK);
				 }
				 if(!objectType.equalsIgnoreCase("device")){
					 finalObject.put("system_generated_id", objectType+"_"+CommonUtility.generateRandomId());
				 }
				 finalObject.put("status", "Success");
				 finalObject.put("statusCode", code);
				 finalObject.put("statusMessage", "Object Name Not Avaialable");
				return new ResponseEntity<String>(finalObject.toJson(), HttpStatus.OK);
				 
			 }else{
			  	Document doc=new Document();
			    doc.put("status","Invalid key");
			    doc.put("statusCode",code);
			    doc.put("status","Session Invalid");
			    return new ResponseEntity<String>(doc.toJson(), HttpStatus.OK);
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
	}
}
