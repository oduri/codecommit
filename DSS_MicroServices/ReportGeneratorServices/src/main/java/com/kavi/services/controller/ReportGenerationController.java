package com.kavi.services.controller;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.kavi.common.utility.CommonUtility;
import com.kavi.common.utility.MessageUtility;
import com.kavi.reportgenerator.dataobjects.ReportGeneratorDO;
import com.kavi.reportgenerator.utility.ReportGeneratorUtility;
import com.kavi.services.thread.PdfGeneratorThread;
import com.mongo.db.MongoDBConnection;
import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

@RestController
public class ReportGenerationController {

	@RequestMapping("/")
	public String welcome() {
		return "Welcome to ReportGenerationController";
	}
	 
	 @RequestMapping(value = "/generatePDF", method = RequestMethod.POST)
	 public ResponseEntity<String> generatePDF(String sessionkey,String userId,String json,String filterType,String dbParamJson) {
		 
		 StringBuilder sb=new StringBuilder();
	     sb.append("{");
		 sb.append("\"status\":");
		 MongoDBConnection mongoSingle = null;
		 MongoDatabase mongodb = null;
		 String dbName = null;
		 MongoCollection<Document> table=null;
		 int code=0;	
		 try{
			 if(dbParamJson!=null && dbParamJson.length()>0) {
				 BasicDBObject dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamJson);
				 dbName = dbParamJsonDoc.getString("db_name");
			 }
			 if(json!=null && json.length()>0){
				 BasicDBObject jsonDocument = null;
				 jsonDocument = (BasicDBObject) JSON.parse(json);
				 if(dbName!=null) {
				 code=CommonUtility.validateAuthenticationKey(sessionkey, dbParamJson);
				 }else {
					 code=CommonUtility.validateAuthenticationKey(sessionkey);
				 }
				 if(code==0){
					 mongoSingle = new MongoDBConnection(dbParamJson);
					 if(dbName!=null) {
						 mongodb = mongoSingle.getMongoDB(dbName);
					 }else {
					 mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
					 }
					 Document pdfReportConfigObject=ReportGeneratorUtility.getReportConfig(mongodb);
					 if(pdfReportConfigObject.get("s3_config")==null){
						  sb.append("\"S3 Config Empty\"");
						  MessageUtility.updateMessage(sb, 6001, "S3 Config Empty");
						  return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
					 }
					 /**********Local Test Case starts*********/
					/* LinkedHashMap<String,Integer> lLatestDeviceStatus=CommonUtility.getLatestDeviceStatus(mongodb, jsonDocument.getString("company_id"));
					 Document finalObject=ReportGeneratorUtility.filterLocationHierarchyDocument(filterType, jsonDocument, table,lLatestDeviceStatus);
					 List<ReportGeneratorTableDO> listReportGeneratorDO= ReportGeneratorUtility.groupUnitAssetNameWithStatus(finalObject);
					 org.jsoup.nodes.Document siteTemplateDoc=ReportGeneratorUtility.loadTemplate("c:\\users\\sarav\\html\\SiteInfo.html");
					 LinkedHashMap<String,Document> lDeviceTransactionDocument=ReportGeneratorUtility.getMappingDocument(mongodb, pdfReportConfigObject);
					 Document templateObject=(Document)pdfReportConfigObject.get("template");
					 templateObject.put("template_path", "c:\\users\\sarav\\html\\");
					 StringBuffer getAssetContent=ReportGeneratorUtility.generateTableContentsForAsset(listReportGeneratorDO,siteTemplateDoc,lDeviceTransactionDocument,templateObject);
					 */
					 /**********Local Test Case ends*********/
					 /*** Previous Version********/
					 /*List<ReportGeneratorTableDO> listReportGeneratorDO= ReportGeneratorUtility.groupUnitAssetNameWithStatus(finalObject);
					 StringBuffer getAssetContent=ReportGeneratorUtility.generateTableContentsForAsset(listReportGeneratorDO);
					 System.out.println("getAssetContent....."+getAssetContent);*/
					 /*** Previous Version********/
					 //System.out.println("finalObject..."+finalObject.toJson());
					 /* Thread Version Commented  started*/
					 /**************Server Deployment Starts ********/
					 Map<String, String> linkedUserGroupDetails =CommonUtility.getUserAndGroupDetails(mongodb, sessionkey);
					 if(linkedUserGroupDetails.get("email_id")==null){
						 sb.append("\"Invalid Email Id\"");
						 MessageUtility.updateMessage(sb, 6001, "EmailId is not registered");
						 return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
					 }
					 table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY);
					 Map<String,Integer> lLatestDeviceStatus=ReportGeneratorUtility.getLatestDeviceStatus(mongodb, jsonDocument.getString("company_id"));
					 Document locationHierarchyObject=ReportGeneratorUtility.filterLocationHierarchyDocument(filterType, jsonDocument, table,lLatestDeviceStatus,"Vibration");
					 List<Document> unitDoc=(List<Document>) locationHierarchyObject.get("unit");
					 if(unitDoc.size()<=0){
						  Document status=new Document();
						  status.put("report_id","N/A");
						  status.put("status", "Success");
						  status.put("statusCode", code);
						  status.put("statusMessage","There is no Vibration category available for this site:"+jsonDocument.getString("site_id"));
						  return new ResponseEntity<String>(status.toJson(), HttpStatus.OK);
					 }
					 Map<String,Document> lDeviceTransactionDocument=ReportGeneratorUtility.getMappingDocument(mongodb, pdfReportConfigObject);
					 ReportGeneratorDO pdfReportGeneratorDO=new ReportGeneratorDO();
					 pdfReportGeneratorDO.setUserId(userId);
					 pdfReportGeneratorDO.setEmailId(linkedUserGroupDetails.get("email_id"));
					 jsonDocument.remove("email_id");
					 pdfReportGeneratorDO.setQuery(jsonDocument.toJson());
					 pdfReportGeneratorDO.setFileName(jsonDocument.getString("filename"));
					 pdfReportGeneratorDO.setDb(mongodb);
					 pdfReportGeneratorDO.setTable(table);
					 pdfReportGeneratorDO.setMongoSingle(mongoSingle);
					 pdfReportGeneratorDO.setReportConfigObject(pdfReportConfigObject);
					 pdfReportGeneratorDO.setLocationHierarchObject(locationHierarchyObject);
					 pdfReportGeneratorDO.setlDeviceTransactionDocument(lDeviceTransactionDocument);
					 pdfReportGeneratorDO.setJsonDocument(jsonDocument);
					 String reportId=ReportGeneratorUtility.insertOrUpdateReport(pdfReportGeneratorDO, "Insert", "Submitted","Success", 0L);
					 pdfReportGeneratorDO.setReportId(reportId); 
					 PdfGeneratorThread thread=new PdfGeneratorThread(pdfReportGeneratorDO,pdfReportConfigObject);
					 thread.start();
					 
					 /**************Server Deployment ends ********/
					 /* Thread Commented ended*/
					 
					 Document status=new Document();
				     status.put("report_id",reportId);
				     status.put("status", "Success");
				     status.put("statusCode", 0);
				     //status.put("fileName");
				     status.put("statusMessage", "A link to download the report has been sent to your email address");
				     return new ResponseEntity<String>(status.toJson(), HttpStatus.OK);
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
				//CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
	  }
		 sb.append("\"Custom Message Generated basis Input\"");
		 MessageUtility.updateMessage(sb, 5000, "json is null");
		 return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
	 }
	 
}
