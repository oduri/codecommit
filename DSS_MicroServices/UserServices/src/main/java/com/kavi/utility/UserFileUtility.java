package com.kavi.utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.json.simple.JSONArray;

import com.amazonaws.auth.BasicAWSCredentials;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kavi.common.constants.CommonConstants;
import com.kavi.common.utility.S3Utility;
import com.kavi.user.dataobjects.AssetDO;
import com.kavi.user.dataobjects.CompanyDO;
import com.kavi.user.dataobjects.DeviceCategoryDO;
import com.kavi.user.dataobjects.DeviceDO;
import com.kavi.user.dataobjects.HierarchyDO;
import com.kavi.user.dataobjects.PlantDO;
import com.kavi.user.dataobjects.SiteDO;
import com.kavi.user.dataobjects.UnitDO;


public class UserFileUtility {
	 
	private final static Logger logger = Logger.getLogger(UserFileUtility.class);
	
	/**
	 * 
	 * @param uploadedInputStream
	 * @param uploadedFileLocation
	 */
	public static void writeToFile(InputStream uploadedInputStream,
		String uploadedFileLocation) {
		OutputStream out=null;
		try {
			out = new FileOutputStream(new File(
					uploadedFileLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			uploadedInputStream.close();
			deleteFile(uploadedFileLocation);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
   }
	
	/**
	 * 
	 * @param inputStream
	 * @param fileName
	 */
	public static void convertBinaryToHex(InputStream inputStream,String fileName,String s3BucketName){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		byte[] content = new byte[ 1024 ];  
		int bytesRead = -1;  
	    try {
			while( ( bytesRead = inputStream.read( content ) ) != -1 ) {  
			     baos.write( content, 0, bytesRead );  
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	    createHexFile(fileName,baos,s3BucketName);
	    
	}
	
	/**
	 * 
	 * @param fileName
	 * @param message
	 * @param s3BucketName
	 */
	private static void createHexFile(String fileName,ByteArrayOutputStream message,String s3BucketName) {
		  byte[] bArray=message.toByteArray();
	      String hexString=new String();
	      for(byte byt:bArray){
	    	  hexString+=toHexString(byt);
	      }
	     BasicAWSCredentials credentials = new BasicAWSCredentials(CommonConstants.S3_ACCESS_KEY,CommonConstants.S3_SECRET_KEY);
	     S3Utility.writeFileToS3(fileName, credentials, hexString,s3BucketName);
	}
	
	/**
	 * 
	 * @param b
	 * @return
	 */
	public static String toHexString(byte b) {
	    return String.format("%02X", b);
    }
	
   /**
    * 	
    * @param fileLocation
    */
   private static void deleteFile(String fileLocation){
	   File f=new File(fileLocation);
	   f.deleteOnExit();
   }
   /**
    * 
    * @param mapList
    */
   private static JSONArray iterateMapAndFindDuplicates(Map<String,List<String>> mapList){

		  Set<String> uniqueKeys=mapList.keySet();
		  JSONArray listDuplicates = new JSONArray();
		  for(String key:uniqueKeys){
		  Set<String> set = findDuplicates(mapList.get(key));
		  String dupValue="";
		  if(set.size()>0){
			  Document printDuplicates = new Document();
			  printDuplicates.put("key",key);
			  dupValue="";
			  for (String str : set) {
				  dupValue=dupValue+str+",";
			  }
			  printDuplicates.put("duplicates",dupValue.substring(0,dupValue.lastIndexOf(",")));
			  listDuplicates.add(printDuplicates);
		  }
	    }
		return listDuplicates;
    }
   /**
    * 
    * @param input
    * @return
    */
   private static Set<String> findDuplicates(List<String> input) {
	    List<String> copy = new ArrayList<String>(input);
	    for (String value : new HashSet<String>(input)) {
	        copy.remove(value);
	    }
	    return new HashSet<String>(copy);
	}
   /**
    * 
    * @return
    */
   public static LinkedHashMap<String,String> readExcelFileAndConstructJson(String fileLocation,String companyName){
	   LinkedHashMap<String,String>  lMap=new LinkedHashMap<String,String>();
	  // XSSFWorkbook workbook=null;
	   try {
		   FileInputStream file =new FileInputStream(fileLocation);
		   XSSFWorkbook	workbook = new XSSFWorkbook(file);
			
			XSSFSheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();
			Row row;
			int count=0;
			int iterate=0;
			Map<String,PlantDO> mapPlantDO=new LinkedHashMap<String,PlantDO>();
			Map<String,SiteDO> mapSiteDO=new LinkedHashMap<String,SiteDO>();
			Map<String,UnitDO> mapUnitDO=new LinkedHashMap<String,UnitDO>();
			Map<String,AssetDO> mapAssetDO=new LinkedHashMap<String,AssetDO>();
			List<DeviceCategoryDO> lDeviceCategoryDO=new ArrayList<DeviceCategoryDO>();
			List<AssetDO> lAssetDO=new ArrayList<AssetDO>();
			List<DeviceDO> lDeviceDO=new ArrayList<DeviceDO>();
			Map<String,DeviceCategoryDO> mapDeviceCategoryDO=new LinkedHashMap<String,DeviceCategoryDO>();
			Map<String,DeviceDO> mapDeviceDO=new LinkedHashMap<String,DeviceDO>();
			Set<DeviceCategoryDO> mySet = new HashSet<DeviceCategoryDO>();
			String message="";
			while (rowIterator.hasNext()) {
				row = rowIterator.next();
				iterate=0;
				if(count>0 && row!=null && row.getCell(iterate)!=null){
					//for(int iterate=0;iterate<row.getPhysicalNumberOfCells();iterate++){
						PlantDO plantDO=new PlantDO();
						plantDO.setId(row.getCell(iterate).toString());
						plantDO.setName(row.getCell(iterate+1).toString());
						mapPlantDO.put(plantDO.getId(), plantDO);
						
						SiteDO siteDO=new SiteDO();
						siteDO.setId(row.getCell(iterate+2).toString());
						siteDO.setName(row.getCell(iterate+3).toString());
						siteDO.setPlant_id(plantDO.getId());
						mapSiteDO.put(siteDO.getId(), siteDO);
						
						UnitDO unitDO=new UnitDO();
						unitDO.setId(row.getCell(iterate+4).toString());
						unitDO.setName(row.getCell(iterate+5).toString());
						unitDO.setSite_id(siteDO.getId());
						mapUnitDO.put(unitDO.getId(), unitDO);
						
						AssetDO assetDO=new AssetDO();
						assetDO.setId(row.getCell(iterate+6).toString());
						assetDO.setName(row.getCell(iterate+7).toString());
						assetDO.setUnit_id(unitDO.getId());
						mapAssetDO.put(assetDO.getId(), assetDO);
						lAssetDO.add(assetDO);
						
						DeviceCategoryDO deviceCategoryDO=new DeviceCategoryDO();
						deviceCategoryDO.setId(row.getCell(iterate+8).toString());
						deviceCategoryDO.setName(row.getCell(iterate+9).toString());
						message=row.getCell(iterate+9).toString();
						deviceCategoryDO.setType(Character.toUpperCase(message.charAt(0)) + message.substring(1));
						deviceCategoryDO.setAsset_id(assetDO.getId());
						mySet.add(deviceCategoryDO);
						mapDeviceCategoryDO.put(deviceCategoryDO.getId(), deviceCategoryDO);
						
						DeviceDO deviceDO=new DeviceDO();
						deviceDO.setId(row.getCell(iterate+10).toString());
						deviceDO.setName(row.getCell(iterate+11).toString());
						deviceDO.setDevice_category_id(deviceCategoryDO.getId());
						mapDeviceDO.put(deviceDO.getId(), deviceDO);
						deviceDO.setAssetId(assetDO.getId());
						lDeviceDO.add(deviceDO);
						//System.out.println(row.getCell(iterate));
					//}
				}
				count=count+1;
			}
			/*mapList.put("plant",plantList);
			mapList.put("site",siteList);
			mapList.put("unit",unitList);
			mapList.put("asset",assetList);
			mapList.put("deviceCategory",deviceCategoryList);
			mapList.put("device",deviceList);
			//JSONArray array=iterateMapAndFindDuplicates(mapList);*/
			lDeviceCategoryDO.addAll(mySet);
			//deleteFile(fileLocation);
			if(lDeviceCategoryDO.size()>0){
		     for(DeviceCategoryDO deviceCategoryDO:lDeviceCategoryDO){
			 	deviceCategoryDO.setDevice(constructDeviceCategoryTreeForObject(deviceCategoryDO.getAsset_id(),lDeviceDO,deviceCategoryDO));
		    }
			
		    for(Object key : mapAssetDO.keySet()) {
		       AssetDO assetDO=(AssetDO)mapAssetDO.get(key);
			   assetDO.setDevice_category(constructAssetTreeForObject(assetDO.getId(),lDeviceCategoryDO,assetDO));
		    }
			for(Object key : mapUnitDO.keySet()) {
				UnitDO unitDO=(UnitDO)mapUnitDO.get(key);
				unitDO.setAsset(constructUnitTreeForObject(key.toString(),mapAssetDO,mapUnitDO));
			}
			
			for(Object key : mapSiteDO.keySet()) {
				SiteDO siteDO=(SiteDO)mapSiteDO.get(key);
				siteDO.setUnit(constructSiteTreeForObject(key.toString(),mapUnitDO,mapSiteDO));
			}
			List<PlantDO> listPlantDO=new ArrayList<PlantDO>();
			for(Object key : mapPlantDO.keySet()) {
				PlantDO plantDO=(PlantDO)mapPlantDO.get(key);
				plantDO.setSite(constructPlantTreeForObject(key.toString(),mapSiteDO));
				listPlantDO.add(plantDO);
				
			}
			
			CompanyDO companyDO=new CompanyDO();
			companyDO.setId(companyName);
			companyDO.setName(companyName);
			companyDO.setDisplay_name(companyName);
			companyDO.setPlantDO(listPlantDO);
			GsonBuilder builder = new GsonBuilder();  
			builder.excludeFieldsWithoutExposeAnnotation();  
			Gson gson = builder.create();
			lMap.put("company_tree", gson.toJson(companyDO));
				//return gson.toJson(companyDO);
			}else{
				lMap.put("error",new JSONArray().toString());
		   }
			file.close();
			workbook.close();
	   } catch (Exception e) {
			e.printStackTrace();
			
	   }
	   
	   return lMap;
   }
   
   /**
  	 * 
  	 * @param id
  	 * @param mapSiteDO
  	 * @return
  	 */
   public static JSONArray constructDeviceCategoryTreeForObject(String parentId,List<DeviceDO> lDeviceDO,DeviceCategoryDO deviceCategoryDO ){
  	 JSONArray array=new JSONArray();
  	 for(DeviceDO deviceDO:lDeviceDO){
  		 if(deviceDO.getAssetId().equalsIgnoreCase(parentId)){
  			 array.add(deviceDO); 
  		 }
  	 }
    
    return array;
   }
    
    
   /**
	 * 
	 * @param id
	 * @param mapSiteDO
	 * @return
	 */
  public static JSONArray constructDeviceCategoryTreeForObject(String parentId,Map<String,DeviceDO> dataObject,Map<String,DeviceCategoryDO> deviceCategoryDO ){
 	 JSONArray array=new JSONArray();
 	 String deviceCategoryId="";
 	 for(Object key : dataObject.keySet()) {
 		DeviceDO deviceDO=(DeviceDO)dataObject.get(key);
 		 if(deviceDO.getDevice_category_id().equalsIgnoreCase(parentId)){
 			deviceCategoryId=deviceDO.getDevice_category_id();
 			array.add(deviceDO);
 		 }
 	 }
 	 if(deviceCategoryId!=null && deviceCategoryId.length()>0){
 		deviceCategoryDO.get(deviceCategoryId).setDevice(array);
 	 }
 	 return array;
  }
  
  /**
	 * 
	 * @param id
	 * @param mapSiteDO
	 * @return
	 */
 public static JSONArray constructAssetTreeForObject(String parentId,List<DeviceCategoryDO> lDeviceCategoryDO,AssetDO assetDO ){
	 JSONArray array=new JSONArray();
	 for(DeviceCategoryDO deviceCategoryDO:lDeviceCategoryDO){
		 if(deviceCategoryDO.getAsset_id().equalsIgnoreCase(parentId)){
			 array.add(deviceCategoryDO);
		 }
	 }
	 return array;
 }
 
  
  /**
 	 * 
 	 * @param id
 	 * @param mapSiteDO
 	 * @return
 	 */
   public static JSONArray constructAssetTreeForObject(String parentId,Map<String,DeviceCategoryDO> dataObject,Map<String,AssetDO> assetDO ){
  	 JSONArray array=new JSONArray();
  	 String assetId="";
  	 for(Object key : dataObject.keySet()) {
  		DeviceCategoryDO deviceCategoryDO=(DeviceCategoryDO)dataObject.get(key);
  		 if(deviceCategoryDO.getAsset_id().equalsIgnoreCase(parentId)){
  			assetId=deviceCategoryDO.getAsset_id();
  			array.add(deviceCategoryDO);
  		 }
  	 }
  	 if(assetId!=null && assetId.length()>0){
  		assetDO.get(assetId).setDevice_category(array);
  	 }
  	 return array;
   }
   
  /**
	 * 
	 * @param id
	 * @param mapSiteDO
	 * @return
	 */
  public static JSONArray constructUnitTreeForObject(String parentId,Map<String,AssetDO> dataObject,Map<String,UnitDO> unitDO ){
 	 JSONArray array=new JSONArray();
 	 String unitId="";
 	 for(Object key : dataObject.keySet()) {
 		AssetDO assetDO=(AssetDO)dataObject.get(key);
 		 if(assetDO.getUnit_id().equalsIgnoreCase(parentId)){
 			unitId=assetDO.getUnit_id();
 			 array.add(assetDO);
 		 }
 	 }
 	 if(unitId!=null && unitId.length()>0){
 		unitDO.get(unitId).setAsset(array);
 	 }
 	 return array;
  }
  
	/**
 	 * 
 	 * @param id
 	 * @param mapSiteDO
 	 * @return
 	 */
   public static JSONArray constructSiteTreeForObject(String parentId,Map<String,UnitDO> dataObject,Map<String,SiteDO> siteDO ){
  	 JSONArray array=new JSONArray();
  	 String siteId="";
  	 for(Object key : dataObject.keySet()) {
  		UnitDO UnitDO=(UnitDO)dataObject.get(key);
  		 if(UnitDO.getSite_id().equalsIgnoreCase(parentId)){
  			 siteId=UnitDO.getSite_id();
  			 array.add(UnitDO);
  		 }
  	 }
  	 if(siteId!=null && siteId.length()>0){
  		siteDO.get(siteId).setUnit(array);
  	 }
  	 return array;
   }
   
  	/**
  	 * 
  	 * @param id
  	 * @param mapSiteDO
  	 * @return
  	 */
    public static JSONArray constructPlantTreeForObject(String parentId,Map<String,SiteDO> dataObject){
   	 JSONArray array=new JSONArray();
   	 for(Object key : dataObject.keySet()) {
   		 SiteDO siteDO=(SiteDO)dataObject.get(key);
   		 if(siteDO.getPlant_id().equalsIgnoreCase(parentId)){
   			 array.add(siteDO);
   		 }
   	 }
   	 return array;
    }
	/**
	 * 
	 * @param fileLocation
	 * @param hierarchyDO
	 */
    public static void createExcelFile(String fileLocation,List<HierarchyDO> listHierarchyDO){
    	try{
    		XSSFWorkbook workbook = new XSSFWorkbook();
    		XSSFSheet sheet = workbook.createSheet("Location");
    		int rowNum=0;
    		Row row;
    		if(listHierarchyDO.size()>0){
    			for(HierarchyDO hierarchyDO:listHierarchyDO){
    				row = sheet.createRow(rowNum);
    				Cell cell = row.createCell(0);
    				cell.setCellValue(hierarchyDO.getPlantId());
    				cell = row.createCell(1);
    				cell.setCellValue(hierarchyDO.getPlantName());
    				cell = row.createCell(2);
    				cell.setCellValue(hierarchyDO.getSiteId());
    				cell = row.createCell(3);
    				cell.setCellValue(hierarchyDO.getSiteName());
    				cell = row.createCell(4);
    				cell.setCellValue(hierarchyDO.getUnitId());
    				cell = row.createCell(5);
    				cell.setCellValue(hierarchyDO.getUnitName());
    				cell = row.createCell(6);
    				cell.setCellValue(hierarchyDO.getAssetId());
    				cell = row.createCell(7);
    				cell.setCellValue(hierarchyDO.getAssetName());
    				cell = row.createCell(8);
    				cell.setCellValue(hierarchyDO.getDeviceCategoryId());
    				cell = row.createCell(9);
    				cell.setCellValue(hierarchyDO.getDeviceCategoryName());
    				cell = row.createCell(10);
    				cell.setCellValue(hierarchyDO.getDeviceId());
    				cell = row.createCell(11);
    				cell.setCellValue(hierarchyDO.getDeviceName());
    				rowNum=rowNum+1;
    			}
    			logger.info("fileLocation...."+fileLocation);
    			 FileOutputStream outputStream = new FileOutputStream(fileLocation);
    	         workbook.write(outputStream);
    	         outputStream.close();
    	         workbook.close();
    		}
    		workbook.close();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
}


	
	
