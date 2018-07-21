package com.kavi.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;

import com.kavi.common.utility.CommonUtility;
import com.kavi.user.dataobjects.HierarchyDO;
import com.kavi.user.dataobjects.ObjectDO;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

public class UserUtility {


	private final static Logger logger = Logger.getLogger(UserUtility.class);
	
	private static Properties prop = new Properties();
	
	
	public static Properties initializeProperties(){
		String file = "/environment.properties";
   		InputStream inputStream = UserUtility.class.getResourceAsStream(file); 
   		Reader reader = new InputStreamReader(inputStream);
		try {
			prop.load(reader);
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
		return prop;
	}
	
	/**
	 * 
	 * @param userId
	 * @param organizationName
	 * @param table
	 * @param mongodb
	 * @return
	 */
	
	public static Document getLocationTreeHierarchyForUser(String userId,String organizationName,MongoCollection<Document> table,MongoDatabase mongodb){
		 Map<String, Integer> llatestDeviceStatus=new HashMap<String, Integer>();
		 Document searchQuery = new Document();
		 searchQuery.put("$search", Pattern.compile(CommonUtility.applyCaseInsensitiveSearchForString(userId), Pattern.CASE_INSENSITIVE).toString());
		 Document textQuery = new Document();
		 textQuery.put("company.id", organizationName);
		 textQuery.put("$text", searchQuery);
		 FindIterable<Document> sessioncursor = table.find(textQuery);
		 MongoCursor<Document> itr=sessioncursor.iterator();
		 Document object=null;
		 Document companyObject=new Document();
		 while(itr.hasNext()){
			 object=(Document)itr.next();
			 object.remove("_id");
			 Document companyIdObject=(Document)object.get("company");
			 if(companyIdObject!=null){
				 llatestDeviceStatus=CommonUtility.getLatestDeviceStatus(mongodb, companyIdObject.getString("id")) ;
			 }
			 companyObject=CommonUtility.traverseObject(object,userId,llatestDeviceStatus,true);
			 
		}//end of while
		itr.close();
		return companyObject;
	}
    
   /**
    * 
    * @param grantUserId
    * @param denyUserId
    * @return
    */
    public static Document convertUserToSecurityObject(String grantUserId,String denyUserId){
    	List<String> lUser=new ArrayList<String>();
    	JSONArray grantArray=new JSONArray();
    	JSONArray denyArray=new JSONArray();
    	if(grantUserId!=null && grantUserId.trim().length()>0){
    		lUser=Arrays.asList(grantUserId.split(","));
    		for(int iterate=0;iterate<lUser.size();iterate++){
    			Document doc=new Document();
    			doc.put("uid", lUser.get(iterate));
    			grantArray.add(doc);
    		}
    	}else{
    		lUser=new ArrayList<String>();
    	}
    	if(denyUserId!=null && denyUserId.trim().length()>0){
    		lUser=Arrays.asList(denyUserId.split(","));
    		for(int iterate=0;iterate<lUser.size();iterate++){
    			Document doc=new Document();
    			doc.put("uid", lUser.get(iterate));
    			denyArray.add(doc);
    		}
    	}else{
    		lUser=new ArrayList<String>();
    	}
    	Document grantDenyDoc=new Document();
    	grantDenyDoc.put("grant", grantArray);
    	grantDenyDoc.put("deny", denyArray);
    	Document userObject=new Document();
    	userObject.put("user", grantDenyDoc);
    	return userObject;
    }
    /**
     * 
     * @return
     */
    private static Map<String,String> lObjectHierarchy(){
    	Map<String,String> unwindObjectHierarchy=new HashMap<String,String>();
    	unwindObjectHierarchy.put("plant_id","$company.plant");
    	unwindObjectHierarchy.put("site_id","$company.plant.site");
    	unwindObjectHierarchy.put("unit_id","$company.plant.site.unit");
    	unwindObjectHierarchy.put("asset_id","$company.plant.site.unit.asset");
    	unwindObjectHierarchy.put("device_category_id","$company.plant.site.unit.asset.device_category");
    	unwindObjectHierarchy.put("device_id","$company.plant.site.unit.asset.device_category.device");
    	return unwindObjectHierarchy;
    }
    

    
    /**
     * 
     * @return
     */
    private static Map<String,String> searchObjectHierarchy(){
    	Map<String,String> unwindObjectHierarchy=new HashMap<String,String>();
    	unwindObjectHierarchy.put("plant_id","company.plant.id");
    	unwindObjectHierarchy.put("site_id","company.plant.site.id");
    	unwindObjectHierarchy.put("unit_id","company.plant.site.unit.id");
    	unwindObjectHierarchy.put("asset_id","company.plant.site.unit.asset.id");
    	unwindObjectHierarchy.put("device_category_id","company.plant.site.unit.asset.device_category.id");
    	unwindObjectHierarchy.put("device_id","company.plant.site.unit.asset.device_category.device.id");
    	return unwindObjectHierarchy;
    }
    
    /**
     * 
     * @return
     */
    private static Map<String,String> addObjectHierarchy(){
    	Map<String,String> addObjectHierarchy=new HashMap<String,String>();
    	addObjectHierarchy.put("plant_id","company.plant");
    	addObjectHierarchy.put("site_id","company.plant.site");
    	addObjectHierarchy.put("unit_id","company.plant.site.unit");
    	addObjectHierarchy.put("asset_id","company.plant.site.unit.asset");
    	addObjectHierarchy.put("device_category_id","company.plant.site.unit.asset.device_category");
    	addObjectHierarchy.put("device_id","company.plant.site.unit.asset.device_category.device");
    	
    	return addObjectHierarchy;
    }
    
    /**
     * 
     * @param searchQueryJson
     * @return
     */
    public static List<Document> getUnwindDocuments(BasicDBObject searchQueryJson,Document updObject){
    	Map<String,String> unwindObjectHierarchy=lObjectHierarchy();
    	Map<String,String> searchObjectHierarchy=searchObjectHierarchy();
    	Map<String,String> addObjectHierarchy=addObjectHierarchy();
    	List<Document> listDocument=new ArrayList<Document>();
    	Set<String> keys=searchQueryJson.keySet();
    	String lastKey="";
    	for(String key:keys){
    		lastKey=key;
    		for (Map.Entry<String,String> entry : searchObjectHierarchy.entrySet()) {
    		Document searchQuery=new Document();
    		if(entry.getKey().equalsIgnoreCase(key)){
    		searchQuery.put(searchObjectHierarchy.get(key), searchQueryJson.get(key));
    		}
    		Document matchQuery = new Document();
    		matchQuery.put("$match", searchQuery);
    		Document unwindDoc=new Document();
    		unwindDoc.put("$unwind",unwindObjectHierarchy.get(entry.getKey()));
    		listDocument.add(unwindDoc);
    		listDocument.add(matchQuery);
    		if(key.equalsIgnoreCase(entry.getKey()))break;
    	   }
    	}
    	Document object=new Document();
		object.put(addObjectHierarchy.get(lastKey),updObject);
		Document addFields=new Document();
		addFields.put("$addFields", object);
		listDocument.add(addFields);
		
		/*Document projectFields=new Document();
		projectFields.put("company.plant", 1);
		Document projectQuery = new Document();
		projectQuery.put("$project", projectFields);
		listDocument.add(projectQuery);*/
    	/*Document outCollection=new Document();
		outCollection.put("$out", MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY_TEMP);
		listDocument.add(outCollection);*/
    	return listDocument;
    	
    }
    
   /**
    * 
    * @param object
    * @param userId
    * @return
    */
    public static Document updateObject(Document object, Document updObject,String objectId,String type,String subtype) {
    	Set<String> keys = object.keySet();
        for (String key : keys) {
        	Object value = object.get(key);
            if (value instanceof List) {
            	traverseArray((List) value,updObject,objectId,type,subtype);
            } else if (value instanceof Document) {
                object.put(key, updateObject((Document) value,updObject,objectId,type,subtype));
            }
        }
        return object;
    }
    
    
    /**
     * 
     * @param array
     * @param updObject
     * @param type
     */
    private static void traverseArray(List<Document> array, Document updObject,String objectId,String type,String subtype) {
        Object[] objects = array.toArray();
        int i, length = objects.length;
        for (i = 0; i < length; i++) {
            Object object = objects[i];
            if (object instanceof Document) {
            	Document document = (Document) object;
            	if(document.getString("id")!=null && document.getString("id").equalsIgnoreCase(objectId)){
            		if(type.equalsIgnoreCase("unit") && subtype.equalsIgnoreCase("delete")){
            			document.remove("x_coordinates");
            			document.remove("y_coordinates");
            		}
            		else if(type.equalsIgnoreCase("asset") && subtype.equalsIgnoreCase("deleteAssetImage")){
            			document.remove("image_id");
            			document.remove("image_location");
            			document.remove("image_unit");
            			document.remove("image_width");
            			document.remove("thumb_image_id");
            		}
            		else if(type.equalsIgnoreCase("site") && subtype.equalsIgnoreCase("deleteSiteImage")){
            			document.remove("image_id");
            			document.remove("image_location");
            			document.remove("image_unit");
            			document.remove("image_width");
            			document.remove("thumb_image_id");
            		}
            		else{
            			document.putAll((Map)updObject);
            		}
            	}else{
            		updateObject(document, updObject,objectId,type,subtype);
            	}
            }
        }
    }
    
   /**
    * 
    * @param type
    * @param subtype
    * @param linkedUserGroupDetails
    * @param json
    * @param table
    * @return
    */
    public static BasicDBObject updateLocationHierarchyObject(String type,String subtype,String json,String parentObjectId,
    		Map<String, String> linkedUserGroupDetails,MongoCollection<Document> table){
    	BasicDBObject fileUploadObject=new BasicDBObject();
    	Document searchQuery=new Document();
		searchQuery.put("company.id", linkedUserGroupDetails.get("organizationName"));
		searchQuery.put("edit_user_id",linkedUserGroupDetails.get("userId"));
		BasicDBObject jsonDocument=null;
		Document updObject=new Document();
		if(subtype.equalsIgnoreCase("security")){
			jsonDocument = (BasicDBObject) JSON.parse(json);
			updObject=convertUserToSecurityObject(jsonDocument.getString("user_grant_access"),jsonDocument.getString("user_deny_access"));
		}else{
			jsonDocument = (BasicDBObject) JSON.parse(json);
			updObject.putAll((Map)jsonDocument);
		}
		FindIterable<Document> sessioncursor = table.find(searchQuery);
		if(sessioncursor.iterator().hasNext()){
			Document object=(Document)sessioncursor.iterator().next();
			updateObject(object, updObject, parentObjectId,type,subtype);
			BasicDBObject command = new BasicDBObject();
			command.put("$set", object);
			table.updateOne(searchQuery,command);
			fileUploadObject.put("status", "Success");
			fileUploadObject.put("statusCode", 0);
			fileUploadObject.put("statusMessage", "Location Hierarchy Updated Successfully");
			fileUploadObject.put("company_id", object.get("_id").toString());
		}
		else{
			fileUploadObject.put("status", "Failure");
			fileUploadObject.put("statusCode", 5000);
			fileUploadObject.put("statusMessage", "Failed in Updating Location Hierarchy");
		}
		return fileUploadObject;
    }
    
    /**
     * 
     * @param type
     * @param subtype
     * @param linkedUserGroupDetails
     * @param json
     * @param table
     * @return
     */
     public static BasicDBObject upsertLocationHierarchyObject(String type,String subtype,String json,String parentObjectId,Map<String, String> linkedUserGroupDetails,
    		 MongoCollection<Document> table,String objectId){
     	BasicDBObject fileUploadObject=new BasicDBObject();
     	Document searchQuery=new Document("_id",new ObjectId(objectId));
 		searchQuery.put("company.id", linkedUserGroupDetails.get("organizationName"));
 		BasicDBObject jsonDocument=null;
 		Document updObject=new Document();
 		jsonDocument = (BasicDBObject) JSON.parse(json);
 		jsonDocument.put("category", type);
 		updObject.putAll((Map)jsonDocument);
 		FindIterable<Document> sessioncursor = table.find(searchQuery);
 		if(sessioncursor.iterator().hasNext()){
 			Document object=(Document)sessioncursor.iterator().next();
 			object.put("modified_date", new java.util.Date());
			upsertObject(object, updObject, parentObjectId,type,subtype);
 			BasicDBObject command = new BasicDBObject();
 			command.put("$set", object);
 			table.updateOne(searchQuery,command);
 			fileUploadObject.put("status", "Success");
 			fileUploadObject.put("statusCode", 0);
 			fileUploadObject.put("statusMessage", "Location Hierarchy Upserted Successfully");
 			fileUploadObject.put("company_id", object.get("_id").toString());
 		}
 		else{
 			fileUploadObject.put("status", "Failure");
 			fileUploadObject.put("statusCode", 5000);
 			fileUploadObject.put("statusMessage", "Failed in Upserted Location Hierarchy");
 		}
 		return fileUploadObject;
     }
     
     /**
      * 
      * @param object
      * @param userId
      * @return
      */
      public static Document upsertObject(Document object, Document updObject,String parentObjectId,String type,String subtype) {
      	Set<String> keys = object.keySet();
          for (String key : keys) {
           	Object value = object.get(key);
          	  if (value instanceof List) {
              	traverseUpsertArray((List) value,updObject,parentObjectId,type,subtype);
              } else if (value instanceof Document) {
                  object.put(key, upsertObject((Document) value,updObject,parentObjectId,type,subtype));
              }
          }
          return object;
      }
      
      /**
       * 
       * @param array
       * @param updObject
       * @param objectId
       * @param type
       * @param subtype
       */
      private static void traverseUpsertArray(List<Document> array, Document updObject,String parentObjectId,String type,String subtype) {
          Object[] objects = array.toArray();
          int i, length = objects.length;
          for (i = 0; i < length; i++) {
              Object object = objects[i];
              if (object instanceof Document) {
              	Document document = (Document) object;
              	if(document!=null &&  parentObjectId.equalsIgnoreCase(document.getString("id")) && subtype.equalsIgnoreCase("add")){
              		List<Document> elementArray=(ArrayList<Document>)document.get(type);
              		if(elementArray!=null && elementArray.size()>0){
              			elementArray.add(updObject);
              		}else{
              			elementArray=new ArrayList<Document>();
              			elementArray.add(updObject);
              			document.put(type, elementArray);
              		}
              	}
              	else if(document!=null && document.getString("id")!=null && document.getString("id").equalsIgnoreCase(updObject.getString("asset_id")) && type.equalsIgnoreCase("channel") && subtype.equalsIgnoreCase("delete")){
          				List<Document> deviceCategoryArray=(ArrayList<Document>)document.get("device_category");
                  		if(deviceCategoryArray!=null && deviceCategoryArray.size()>0){
                  			for(int deviceCategoryArrayIterate=0;deviceCategoryArrayIterate<deviceCategoryArray.size();deviceCategoryArrayIterate++){
                  				Document deviceCategoryDoc=(Document)deviceCategoryArray.get(deviceCategoryArrayIterate);
                  				List<Document> deviceArray=(ArrayList<Document>)deviceCategoryDoc.get("device");
                          		if(deviceArray!=null && deviceArray.size()>0){
                          			for(int deviceArrayIterate=0;deviceArrayIterate<deviceArray.size();deviceArrayIterate++){
                          				JSONArray tempArray=new JSONArray();
                          				Document deviceDoc=(Document)deviceArray.get(deviceArrayIterate);
                          				List<Document> channelElementArray=(ArrayList<Document>)deviceDoc.get(type); //channel
        	              				if(channelElementArray!=null && channelElementArray.size()>0){
        	              					for(int channelElementArrayIterate=0;channelElementArrayIterate<channelElementArray.size();channelElementArrayIterate++){
        	                      				Document doc=(Document)channelElementArray.get(channelElementArrayIterate);
        	                      				if(!doc.get("id").equals(updObject.get("id"))){
        	                      					tempArray.add(doc);
        	                      				}
        	                      			}
        	              					deviceDoc.put(type, tempArray);
        	              				}
                          			}
                          	 }//end of device array
                  		  }
                  	   }//end of if device category array
              			
              	}
              	
              	else if(document!=null && document.getString("id")!=null && document.getString("id").equalsIgnoreCase(updObject.getString("asset_id")) && type.equalsIgnoreCase("channel") && subtype.equalsIgnoreCase("edit")){
          				List<Document> deviceCategoryArray=(ArrayList<Document>)document.get("device_category");
                  		if(deviceCategoryArray!=null && deviceCategoryArray.size()>0){
                  			for(int deviceCategoryArrayIterate=0;deviceCategoryArrayIterate<deviceCategoryArray.size();deviceCategoryArrayIterate++){
                  				Document deviceCategoryDoc=(Document)deviceCategoryArray.get(deviceCategoryArrayIterate);
                  				List<Document> deviceArray=(ArrayList<Document>)deviceCategoryDoc.get("device");
                          		if(deviceArray!=null && deviceArray.size()>0){
                          			for(int deviceArrayIterate=0;deviceArrayIterate<deviceArray.size();deviceArrayIterate++){
                          				JSONArray tempArray=new JSONArray();
                          				Document deviceDoc=(Document)deviceArray.get(deviceArrayIterate);
                          				List<Document> channelElementArray=(ArrayList<Document>)deviceDoc.get(type); //channel
        	              				if(channelElementArray!=null && channelElementArray.size()>0){
        	              					for(int channelElementArrayIterate=0;channelElementArrayIterate<channelElementArray.size();channelElementArrayIterate++){
        	                      				Document doc=(Document)channelElementArray.get(channelElementArrayIterate);
        	                      				if(doc.getString("id").equals(updObject.get("id"))){
        	                      					tempArray.remove(doc);
        	                      					updObject.remove("device_id");
        	                      					updObject.remove("asset_id");
        	                      					tempArray.add(updObject);
        	                      				}else{
        	                      					tempArray.add(doc);
        	                      				}
        	                      			}
        	              					deviceDoc.put(type, tempArray);
        	              				}
                          			}
                          	 }//end of device array
                  		  }
                  	   }//end of if device category array
              			
              	}
              	else if(document!=null && parentObjectId.equalsIgnoreCase(document.getString("id")) && subtype.equalsIgnoreCase("delete")){
              		List<Document> elementArray=(ArrayList<Document>)document.get(type);
              		if(elementArray!=null && elementArray.size()>0){
              			JSONArray tempArray=new JSONArray();
              			for(int iterate=0;iterate<elementArray.size();iterate++){
              				Document doc=(Document)elementArray.get(iterate);
              				if(!doc.get("id").equals(updObject.get("id"))){
              					tempArray.add(doc);
              				}
              			}
              			document.put(type, tempArray);
              		}
              	}
              	else if(document!=null && parentObjectId.equalsIgnoreCase(document.getString("id")) && subtype.equalsIgnoreCase("edit")){
              		List<Document> elementArray=(ArrayList<Document>)document.get(type);
              		if(elementArray!=null && elementArray.size()>0){
              			JSONArray tempArray=new JSONArray();
              			for(int iterate=0;iterate<elementArray.size();iterate++){
              				Document doc=(Document)elementArray.get(iterate);
              				if(doc.get("id").equals(updObject.get("id"))){
              					tempArray.remove(doc);
              					tempArray.add(updObject);
              				}else{
              					tempArray.add(doc);
              				}
              				
              			}
              			document.put(type, tempArray);
              		}
              	}
              	else if(document!=null && parentObjectId.equalsIgnoreCase(document.getString("id")) && subtype.equalsIgnoreCase("editchannel")){
              		List<Document> elementArray=(ArrayList<Document>)document.get("device");
              		if(elementArray!=null && elementArray.size()>0){
              			for(int iterate=0;iterate<elementArray.size();iterate++){
              				JSONArray channelArray=new JSONArray();
                  			Document doc=(Document)elementArray.get(iterate);
              				if(doc.get(type)!=null && doc.get("id").equals(updObject.get("device_id"))){
	              				List<Document> channelElementArray=(ArrayList<Document>)doc.get(type);
	              				if(channelElementArray!=null && channelElementArray.size()>0){
	              					for(int channelIterate=0;channelIterate<channelElementArray.size();channelIterate++){
	              						Document channeldoc=(Document)channelElementArray.get(channelIterate);
	              						if(channeldoc.get("id").equals(updObject.get("id"))){
	                      					channelArray.remove(channeldoc);
	                      					updObject.remove("device_id");
	                      					updObject.remove("asset_id");
	                      					channelArray.add(updObject);
	                      				}else{
	                      					channelArray.add(channeldoc);
	                      				}
	              					}
	              					doc.put(type, channelArray);
	              				}
              				}
              			}
              		}
              	}
              	else if(document!=null && parentObjectId.equalsIgnoreCase(document.getString("id")) && subtype.equalsIgnoreCase("addChannel")){
              		List<Document> elementArray=(ArrayList<Document>)document.get("device");
              		if(elementArray!=null && elementArray.size()>0){
              			for(int iterate=0;iterate<elementArray.size();iterate++){
              				JSONArray channelArray=new JSONArray();
                  			Document doc=(Document)elementArray.get(iterate);
              				if(doc.get(type)!=null && doc.get("id").equals(updObject.get("device_id"))){
	              				List<Document> channelElementArray=(ArrayList<Document>)doc.get(type);
	              				updObject.remove("device_id");
              					updObject.remove("asset_id");
              					channelElementArray.add(updObject);
	              				/*if(channelElementArray!=null && channelElementArray.size()>0){
	              					for(int channelIterate=0;channelIterate<channelElementArray.size();channelIterate++){
	              						Document channeldoc=(Document)channelElementArray.get(channelIterate);
	              						if(channeldoc.get("id").equals(updObject.get("id"))){
	                      					channelArray.remove(channeldoc);
	                      					updObject.remove("device_id");
	                      					updObject.remove("category");
	                      					channelArray.add(updObject);
	                      				}else{
	                      					channelArray.add(channeldoc);
	                      				}
	              					}
	              					doc.put(type, channelArray);
	              				}*/
              				}
              			}
              		}
              	}
              	
              	else if(document!=null && parentObjectId.equalsIgnoreCase(document.getString("id")) && subtype.equalsIgnoreCase("editunit")){
              		List<Document> elementArray=(ArrayList<Document>)document.get(type);
              		if(elementArray!=null && elementArray.size()>0){
              			JSONArray tempArray=new JSONArray();
              			for(int iterate=0;iterate<elementArray.size();iterate++){
              				Document doc=(Document)elementArray.get(iterate);
              				if(doc.get("id").equals(updObject.get("id"))){
              					//tempArray.remove(doc);
              					//tempArray.add(updObject);
              					doc.put("name",updObject.get("name"));
              					doc.put("x_coordinates",updObject.get("x_coordinates"));
              					doc.put("y_coordinates",updObject.get("y_coordinates"));
              				}
              				
              			}
              			//document.put(type, tempArray);
              		}
              	}
              	else{
              		upsertObject(document, updObject,parentObjectId,type,subtype);
              	}
              }
          }
      }
      
    /**
     * 
     * @return
     */
    public static Document getSelectedFieldsForLocation(){
    	Document selectedFields = new Document("_id",0);
    	selectedFields.put("company.id",1);
    	selectedFields.put("company.name",1);
    	selectedFields.put("company.plant.site.id",1);
    	selectedFields.put("company.plant.site.name",1);
    	return selectedFields;
    	
    }
    
    /**
	 * 
	 * @param document
	 * @return
	 */
	 public static String checkAllSiteImagesUploaded(Document document){
		 String str="COMPLETE";
		 String overallStatus="COMPLETE";
		 List<Document> plantArray=(ArrayList<Document>)document.get("plant");
		 JSONArray inCompleteSiteArray=new JSONArray();
		 JSONArray inCompleteUnitArray=new JSONArray();
		 if(plantArray!=null && plantArray.size()>0){
			 for(int iterate=0;iterate<plantArray.size();iterate++){
				 Document doc=(Document)plantArray.get(iterate);
				 List<Document> siteArray=(ArrayList<Document>)doc.get("site");
				 for(int siteIterate=0;siteIterate<siteArray.size();siteIterate++){
					Document siteArrayDoc=(Document)siteArray.get(siteIterate);
					if(siteArrayDoc.get("image_id")==null){
						str="IN_COMPLETE";
						overallStatus="IN_COMPLETE";
						inCompleteSiteArray.add(siteArrayDoc);
					}
					checkAllUnitCoordinatesAvailable(siteArrayDoc,document,inCompleteUnitArray);
					//siteArrayDoc.remove("unit");
				 }
			 }
		 }
		 document.put("site_status", str);
		 document.put("in_complete_site_list", inCompleteSiteArray);
		 if(inCompleteUnitArray.size()>0){
			 overallStatus="IN_COMPLETE";
			 document.put("unit_status", "IN_COMPLETE");
			 document.put("in_complete_unit_list", inCompleteUnitArray);
		 }else{
			 document.put("unit_status", "COMPLETE");
			 document.put("in_complete_unit_list", inCompleteUnitArray);
		 }
		 return overallStatus;
	 }
	 
	 /**
	  * 
	  * @param siteArrayDoc
	  * @param document
	  * @param inCompleteUnitArray
	  */
	 public static void checkAllUnitCoordinatesAvailable(Document siteArrayDoc,Document document,JSONArray inCompleteUnitArray){
		 	List<Document> unitArray=(ArrayList<Document>)siteArrayDoc.get("unit");
		 	if(unitArray!=null && unitArray.size()>0){
				for(int unitIterate=0;unitIterate<unitArray.size();unitIterate++){
					Document unitArrayDoc=(Document)unitArray.get(unitIterate);
					if(unitArrayDoc!=null && unitArrayDoc.get("x_coordinates")==null){
						//unitArrayDoc.remove("asset");
						inCompleteUnitArray.add(unitArrayDoc);
					}
				}
		 	}else if(unitArray==null){
		 		Document unitArrayDoc=new Document();
		 		inCompleteUnitArray.add(unitArrayDoc);
		 	}
			
	 }
	 
	
	/**
     * 
     * @return
     */
    public static Document getSelectedFieldsForHierarchyLocation(){
    	Document selectedFields = new Document("_id",0);
    	selectedFields.put("company.plant.id",1);
    	selectedFields.put("company.plant.name",1);
    	selectedFields.put("company.plant.site.id",1);
    	selectedFields.put("company.plant.site.name",1);
    	selectedFields.put("company.plant.site.unit.id",1);
    	selectedFields.put("company.plant.site.unit.name",1);
    	selectedFields.put("company.plant.site.unit.asset.id",1);
    	selectedFields.put("company.plant.site.unit.asset.name",1);
    	selectedFields.put("company.plant.site.unit.asset.device_category.id",1);
    	selectedFields.put("company.plant.site.unit.asset.device_category.name",1);
    	selectedFields.put("company.plant.site.unit.asset.device_category.device.id",1);
    	selectedFields.put("company.plant.site.unit.asset.device_category.device.name",1);
    	
    	return selectedFields;
    	
    }
    /**
     * 
     * @param doc
     * @param type
     * @param hierarchyDO
     */
    public static void mapObject(Document object,String type,HierarchyDO hierarchyDO){
    	if(type.equalsIgnoreCase("plant")){
    		hierarchyDO.setPlantId(object.getString("id"));
			hierarchyDO.setPlantName(object.getString("name"));
    	}
    	else if(type.equalsIgnoreCase("site")){
    		 hierarchyDO.setSiteId(object.getString("id"));
			 hierarchyDO.setSiteName(object.getString("name"));
    	}
    	else if(type.equalsIgnoreCase("unit")){
    		 hierarchyDO.setUnitId(object.getString("id"));
			 hierarchyDO.setUnitName(object.getString("name"));
    	}
    	else if(type.equalsIgnoreCase("asset")){
    		hierarchyDO.setAssetId(object.getString("id"));
			hierarchyDO.setAssetName(object.getString("name"));
    	}
    	else if(type.equalsIgnoreCase("device_category")){
    		hierarchyDO.setDeviceCategoryId(object.getString("id"));
			hierarchyDO.setDeviceCategoryName(object.getString("name"));
    	}
    	else if(type.equalsIgnoreCase("device")){
    		hierarchyDO.setDeviceId(object.getString("id"));
			hierarchyDO.setDeviceName(object.getString("name"));
    	}
    }
    
    /**
	 * 
	 * @param objectType
	 * @param json
	 * @return
	 */
	public static List<Document>  getObjectHierarchy(String objectType,BasicDBObject jsonDocument,String companyId){
		Map<String,String> unwindObjectHierarchy=new HashMap<String,String>();
		Map<String,String> searchObjectHierarchy=searchObjectHierarchy();
		Map<String,String> searchQueryMap=new HashMap<String,String>();
		searchQueryMap.put("plant", "company.plant.name");
		searchQueryMap.put("site", "company.plant.site.name");
		searchQueryMap.put("unit", "company.plant.site.unit.name");
		searchQueryMap.put("asset", "company.plant.site.unit.asset.name");
		searchQueryMap.put("device_category", "company.plant.site.unit.asset.device_category.name");
		searchQueryMap.put("device", "company.plant.site.unit.asset.device_category.device.name");
		searchQueryMap.put("channel", "company.plant.site.unit.asset.device_category.device.channel.name");
		
		if(objectType.equalsIgnoreCase("site")){
			unwindObjectHierarchy.put("plant_id","$company.plant");
			unwindObjectHierarchy.put("site_id","$company.plant.site");
		}
		else if(objectType.equalsIgnoreCase("unit")){
			unwindObjectHierarchy.put("plant_id","$company.plant");
			unwindObjectHierarchy.put("site_id","$company.plant.site");
			unwindObjectHierarchy.put("unit_id","$company.plant.site.unit");
	    	
		}
		else if(objectType.equalsIgnoreCase("asset")){
			unwindObjectHierarchy.put("plant_id","$company.plant");
			unwindObjectHierarchy.put("site_id","$company.plant.site");
			unwindObjectHierarchy.put("unit_id","$company.plant.site.unit");
			unwindObjectHierarchy.put("asset_id","$company.plant.site.unit.asset");
		}
		else if(objectType.equalsIgnoreCase("device_category")){
			unwindObjectHierarchy.put("plant_id","$company.plant");
			unwindObjectHierarchy.put("site_id","$company.plant.site");
			unwindObjectHierarchy.put("unit_id","$company.plant.site.unit");
			unwindObjectHierarchy.put("asset_id","$company.plant.site.unit.asset");
			unwindObjectHierarchy.put("device_category_id","$company.plant.site.unit.asset.device_category");
		}
		else if(objectType.equalsIgnoreCase("device")){
			unwindObjectHierarchy.put("plant_id","$company.plant");
			unwindObjectHierarchy.put("site_id","$company.plant.site");
			unwindObjectHierarchy.put("unit_id","$company.plant.site.unit");
			unwindObjectHierarchy.put("asset_id","$company.plant.site.unit.asset");
			unwindObjectHierarchy.put("device_category_id","$company.plant.site.unit.asset.device_category");
			unwindObjectHierarchy.put("device_id","$company.plant.site.unit.asset.device_category.device");
		}
		else if(objectType.equalsIgnoreCase("channel")){
			unwindObjectHierarchy.put("plant_id","$company.plant");
			unwindObjectHierarchy.put("site_id","$company.plant.site");
			unwindObjectHierarchy.put("unit_id","$company.plant.site.unit");
			unwindObjectHierarchy.put("asset_id","$company.plant.site.unit.asset");
			unwindObjectHierarchy.put("device_category_id","$company.plant.site.unit.asset.device_category");
			unwindObjectHierarchy.put("device_id","$company.plant.site.unit.asset.device_category.device");
			unwindObjectHierarchy.put("channel_id","$company.plant.site.unit.asset.device_category.device.channel");
		}
		
    	List<Document> listDocument=new ArrayList<Document>();
    	Set<String> keys=unwindObjectHierarchy.keySet();
    	int count=keys.size();
    	int finalCount=0;
    	Document searchQuery=new Document();
    	searchQuery.put("version_control.active_flag", true);
    	searchQuery.put("company.id", companyId);
    	Document matchQuery = new Document();
    	matchQuery.put("$match", searchQuery);
    	
    	listDocument.add(matchQuery);
    	for(String key:keys){
    		matchQuery = new Document();
    		searchQuery=new Document();
    		finalCount=finalCount+1;
    		if(finalCount==count){
    			List<Document> sourceObj = new ArrayList<Document>();
    			BasicDBObject query=(BasicDBObject)jsonDocument.get("query");
    			if(query!=null){
    				Set<String> jsonKey=query.keySet();
    				for(String jsonIterateKey:jsonKey){
    					sourceObj.add(new Document(jsonIterateKey,query.get(jsonIterateKey)));
    				}
    			}
				searchQuery.put("$or", sourceObj);
    		}else{
    			searchQuery.put(searchObjectHierarchy.get(key), jsonDocument.getString(key));
    		}
			matchQuery.put("$match", searchQuery);
    		Document unwindDoc=new Document();
    		unwindDoc.put("$unwind",unwindObjectHierarchy.get(key));
    		listDocument.add(unwindDoc);
    		listDocument.add(matchQuery);
    	}
    	
    	return listDocument;
		
	}
		
	/**
	 * 
	 * @param userId
	 * @param document
	 * @param llatestDeviceStatus
	 * @return
	 */
	public static Document filterLocationTreeHierarchyForSiteAdmin(String userId,Document document,Map<String,ObjectDO> lSiteInfoDocuments){
		Document companyObject=(Document)document.get("company");
		JSONArray siteArray=new JSONArray();
		JSONArray plantArray=new JSONArray();
		boolean isSiteFlag=false;
		if(companyObject!=null){
			List<Document> plantObjectList  =(ArrayList)companyObject.get("plant");
			 if(plantObjectList!=null && plantObjectList.size()>0){
		     plantArray=new JSONArray();
			 for(int plantIterate=0;plantIterate<plantObjectList.size();plantIterate++){
				 	Document plantObject=(Document)plantObjectList.get(plantIterate);
				 	if(plantObject!=null && plantObject.get("site")!=null){
				 	 List<Document> siteObjectList=(ArrayList)plantObject.get("site");		
				 	 siteArray=new JSONArray();
				 	 for(int siteIterate=0;siteIterate<siteObjectList.size();siteIterate++){
				 		Document siteObject=(Document)siteObjectList.get(siteIterate);
				 		Document siteUserObject=(Document)siteObject.get("user");
						 if(CommonUtility.isUserRoleExistsInGrant(userId, siteUserObject,"siteadmin")){
							 if(lSiteInfoDocuments.get(siteObject.getString("id"))!=null){
								 document=new Document();
								 document.put("status", "Duplicate Edit Found");
								 document.put("statusCode", 7001);
								 document.put("statusMessage", lSiteInfoDocuments.get(siteObject.getString("id")).getUserName()+ " is already performing an edit");
								 return document;
								 
							 }else{
								 siteArray.add(siteObject);
							 }
						}
				 	}//end of for site
				 	if(siteArray.size()>0){
				 		plantObject.put("site", siteArray);
				 		isSiteFlag=true;
				 	}
				  }//end of if for plant
				 if(siteArray.size()>0){
					 plantArray.add(plantObject);
					 isSiteFlag=true;
				 }
			 	}//end of  for plant
			   companyObject.put("plant",plantArray);
			 }
		}
		Document companyIdObject=new Document(); 
		if(companyObject.get("id")!=null){ 
			document.put("company", companyObject);
		}
		if(isSiteFlag==false){
			document.put("status", "Success");
			document.put("statusCode", 5001);
			document.put("statusMessage", "User doesn't have permission to edit sites");
			return document;
		}
		return document;
	}
	
	
	/**
	 * 
	 * @param iterable
	 * @return
	 */
	public static Map<ObjectDO,Document> getSiteObjectFromTempTable(MongoCollection<Document> table,String objectId){
		Document searchQuery = new Document("_id", new ObjectId(objectId));
		BasicDBObject uwindPlant=new BasicDBObject("$unwind","$company.plant");
		BasicDBObject uwindSite=new BasicDBObject("$unwind","$company.plant.site");
		Document matchQuery = new Document();
		matchQuery.put("$match", searchQuery);
		Document projectQuery = new Document();
		Document project = new Document();
    	project.put("company_id","$company.id");
    	project.put("plant_id","$company.plant.id");
    	project.put("company_name","$company.name");
    	project.put("plant_name","$company.plant.name");
    	project.put("site_id", "$company.plant.site.id");
    	project.put("site_name", "$company.plant.site.name");
    	project.put("site","$company.plant.site");
    	projectQuery.put("$project", project);
		Map<ObjectDO,Document> lSiteMapDocuments=new HashMap<ObjectDO,Document>();
		AggregateIterable<Document> iterable=table.aggregate(Arrays.asList(matchQuery,uwindPlant,uwindSite,projectQuery));
		for (Document row : iterable) {
    		ObjectDO objectDO=new ObjectDO();
    		objectDO.setCompanyId(row.getString("company_id"));
    		objectDO.setPlantId(row.getString("plant_id"));
    		objectDO.setSiteId(row.getString("site_id"));
    		lSiteMapDocuments.put(objectDO,(Document)row.get("site"));
    	}
		return lSiteMapDocuments;
	}
	/**
	 * 
	 * @param table
	 * @param userId
	 * @param organizationName
	 * @return
	 */
	public static Map<String,ObjectDO> getSitesFromTempTable(MongoCollection<Document> table,String userId,String organizationName,String userName){
		Map<String,ObjectDO> lSiteInfoDocuments=new HashMap<String,ObjectDO>();
		Document searchQuery = new Document();
		Document ninQuery = new Document();
		//JSONArray array=new JSONArray();
		//array.add(userId);
		ninQuery.put("$ne", userId);
		searchQuery.put("edit_user_id", ninQuery);
		searchQuery.put("company.id", organizationName);
		BasicDBObject uwindPlant=new BasicDBObject("$unwind","$company.plant");
		BasicDBObject uwindSite=new BasicDBObject("$unwind","$company.plant.site");
		Document matchQuery = new Document();
		matchQuery.put("$match", searchQuery);
		Document projectQuery = new Document();
		Document project = new Document();
    	project.put("company_id","$company.id");
    	project.put("plant_id","$company.plant.id");
    	project.put("company_name","$company.name");
    	project.put("plant_name","$company.plant.name");
    	project.put("site_id", "$company.plant.site.id");
    	project.put("site_name", "$company.plant.site.name");
    	project.put("site","$company.plant.site");
    	project.put("edit_user_id",1);
    	project.put("edit_user_display_name",1);
    	projectQuery.put("$project", project);
    	AggregateIterable<Document> iterable=table.aggregate(Arrays.asList(matchQuery,uwindPlant,uwindSite,projectQuery));
		for (Document row : iterable) {
			ObjectDO objectDO=new ObjectDO();
			objectDO.setUserId(row.getString("edit_user_id"));
			objectDO.setSiteId(row.getString("site_id"));
			objectDO.setUserName(row.getString("edit_user_display_name"));
			lSiteInfoDocuments.put(objectDO.getSiteId(),objectDO);
		}
    	return lSiteInfoDocuments;
	}
	
	/**
	 * 
	 * @param lSiteMapDocuments
	 * @param companyObject
	 * @return 
	 */
	public static Document mergeLocationHierarchyDocuments(Map<ObjectDO,Document> lSiteMapDocuments,Document locationHierarchyObject){
		Document companyObject=(Document)locationHierarchyObject.get("company");
		ObjectDO matchObjectDO=null;
		Document doc=new Document();
		if(companyObject!=null){
			List<Document> plantObjectList  =(ArrayList)companyObject.get("plant");
			 if(plantObjectList!=null && plantObjectList.size()>0){
		     for(int plantIterate=0;plantIterate<plantObjectList.size();plantIterate++){
		    	    Document plantObject=(Document)plantObjectList.get(plantIterate);
				 	if(plantObject!=null && plantObject.get("site")!=null){
				 	 List<Document> siteObjectList=(ArrayList)plantObject.get("site");		
				 	 for(int siteIterate=0;siteIterate<siteObjectList.size();siteIterate++){	
				 		Document siteObject=(Document)siteObjectList.get(siteIterate);
				 		matchObjectDO=new ObjectDO();
				 		matchObjectDO.setCompanyId(companyObject.getString("id"));
					 	matchObjectDO.setPlantId(plantObject.getString("id"));
			    	    matchObjectDO.setSiteId(siteObject.getString("id"));
			    	    doc=matchSiteDocuments(lSiteMapDocuments,matchObjectDO);
					 	if(doc!=null){
					 		siteObjectList.remove(siteIterate);
					 		siteObjectList.add(siteIterate,doc);
					 	}
				 	 }
				 }
			 }
		  }
		}
		return companyObject;
	}
	
	/**
	 * 
	 * @param lSiteMapDocuments
	 * @return
	 */
	public static Document matchSiteDocuments(Map<ObjectDO,Document> lSiteMapDocuments,ObjectDO matchObjectDO){
		for(ObjectDO objectDO:lSiteMapDocuments.keySet()){
			if(objectDO.getCompanyId().equalsIgnoreCase(matchObjectDO.getCompanyId()) 
					&& objectDO.getPlantId().equalsIgnoreCase(matchObjectDO.getPlantId()) && objectDO.getSiteId().equalsIgnoreCase(matchObjectDO.getSiteId())){
				return lSiteMapDocuments.get(objectDO);
			}
    	}
		return null;
	}
	

}
