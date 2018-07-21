package com.kavi.testcase;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.springframework.http.ResponseEntity;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.kavi.common.utility.CommonUtility;
import com.kavi.common.utility.S3Utility;
import com.kavi.common.utility.SESUtility;
import com.kavi.reportgenerator.dataobjects.ReportGeneratorDO;
import com.kavi.reportgenerator.dataobjects.ReportGeneratorTableDO;
import com.kavi.reportgenerator.utility.ReportGeneratorUtility;
import com.kavi.services.controller.ReportGenerationController;
import com.kavi.services.impl.AWSEmailServiceImpl;
import com.kavi.services.intf.EmailService;
import com.mongo.db.MongoDBConnection;
import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;	

public class ReportGeneratorTestCases {
	
	
	private final static Logger logger = Logger.getLogger(ReportGeneratorTestCases.class);
	public static void main(String args[]) 
	{
		try {
			//generatePDFAndSendEmail();
			//getReportConfig();
			//updateTest();
			//generatePDF();
			generatePDFLocalTestCase();
			//getDate();
			//timeCheck();
			//createJsonObject();
			//testMail();
			
		} catch (Exception e) {
			e.printStackTrace();

		}
	}
	
	private static void testMail() {
		SESUtility sesUtil = new SESUtility();
		EmailService emailService;
		try {
			emailService = new AWSEmailServiceImpl(sesUtil.createSimpleEmailService());
			emailService.withFrom("no-reply@msyte.io")
			.withTo("saravanan.periyasamy@kaviglobal.com")
			.withSubject("Test AWS Email without Attachments")
			.withBody(new Body()
	                  .withHtml(new Content()
	                      .withCharset("UTF-8").withData("<b>hai</b>")))
		    .sendEmailWithHtmlContent();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void generatePDFLocalTestCase() {
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		try{
			
			BasicDBObject jsonDocument=new BasicDBObject();
			jsonDocument.put("company_id","FHR");
			jsonDocument.put("email_id", "saravanan.periyasamy@kaviglobal.com");
			jsonDocument.put("date","07/07/2017");
			jsonDocument.put("time","07/17");
			jsonDocument.put("timezone","CDT");
			//jsonDocument.put("site_id","pinebend");
			jsonDocument.put("site_id","LisleMolex");
			//jsonDocument.put("site_id","Brewton");
			//jsonDocument.put("site_id","ENG");
			String filterType="site";
			mongoSingle = new MongoDBConnection();
			mongodb = mongoSingle.getMongoDB("FHR_DSS");
			Document pdfReportConfigObject=ReportGeneratorUtility.getReportConfig(mongodb);
			/*
			DateTimeZone timeZoneCurrent = DateTimeZone.forID(TimeZone.getDefault().getID());
			DateTime nowZone = DateTime.now( timeZoneCurrent );
			DateTimeZone timeZoneChicago = DateTimeZone.forID( "CST");
			DateTime nowConvertZone = nowZone.withZone( timeZoneChicago);
		    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm a");
		    String format = formatter.format(nowConvertZone.toLocalDateTime().toDate());
		    System.out.println("format..."+format);
		    System.out.println("timeZoneChicago..."+timeZoneChicago.getProvider().getZone("America/Chicago"));*/
			/*Date today = new Date();
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm a");
			df.setTimeZone(TimeZone.getTimeZone("EST"));
			String time = df.format(today);
			System.out.println(time);
			df.setTimeZone(TimeZone.getTimeZone("CST"));
			time = df.format(today);
			System.out.println(time);
*/
			
			//System.out.println("..."+ReportGeneratorUtility.updateFooterDateTime("CST", "MM/dd/yyyy HH:mm a"));
			JSONArray array=new JSONArray();
			table=mongodb.getCollection( MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY);
			/**********Local Test Case starts*********/
			//Document doc=ReportGeneratorUtility.getTimeSeriesData(mongodb, "LisleMolex","970470347");
			//System.out.println("doc..."+doc.toJson());
			//LinkedHashMap<String,Integer> lLatestDeviceStatus=CommonUtility.getLatestDeviceStatus(mongodb, jsonDocument.getString("company_id"));
			//System.out.println("lLatestDeviceStatus...."+lLatestDeviceStatus);
			
			Map<String,Integer> lLatestDeviceStatus=CommonUtility.getLatestDeviceStatus(mongodb, jsonDocument.getString("company_id"));
			Document locationHierarchyObject=ReportGeneratorUtility.filterLocationHierarchyDocument(filterType, jsonDocument, table,lLatestDeviceStatus,"Vibration");
			List<Document> unitDoc=(List<Document>) locationHierarchyObject.get("unit");
			if(unitDoc.size()<=0){
				System.out.println("Welcome");
			}else{
			List<ReportGeneratorTableDO> listReportGeneratorDO= ReportGeneratorUtility.groupUnitAssetNameWithStatus(locationHierarchyObject);
			org.jsoup.nodes.Document siteTemplateDoc=ReportGeneratorUtility.loadTemplate("c:\\users\\sarav\\html\\SiteInfo.html");
			Map<String,Document> lDeviceTransactionDocument=ReportGeneratorUtility.getMappingDocument(mongodb, pdfReportConfigObject);
				
			//Document row=ReportGeneratorUtility.filterDocumentByAssetId(lDeviceTransactionDocument,"InletCompressor_3170","962540347");
			//System.out.println("row..."+row);
			Document templateObject=(Document)pdfReportConfigObject.get("template");
			Document s3ConfigObject=(Document)pdfReportConfigObject.get("s3_config");
			templateObject.put("pdf_output_path", "c:\\users\\sarav\\html\\");
			templateObject.put("template_microservice_path", "c:\\users\\sarav\\html\\");
			templateObject.put("communicate_export_server",true);
			templateObject.put("high_export_server_url","http://highcharts-elb-a2fd68439b6b38eb.elb.us-east-1.amazonaws.com/");
			
			
			 ReportGeneratorDO pdfReportGeneratorDO=new ReportGeneratorDO();
			 pdfReportGeneratorDO.setUserId("saravanan.periyasamy@kaviglobal.com");
			 pdfReportGeneratorDO.setEmailId(jsonDocument.getString("email_id"));
			 pdfReportGeneratorDO.setJsonDocument(jsonDocument);
			 jsonDocument.remove("email_id");
			 pdfReportGeneratorDO.setQuery(jsonDocument.toJson());
			 pdfReportGeneratorDO.setFileName("report_local_test");
			 pdfReportGeneratorDO.setDb(mongodb);
			 pdfReportGeneratorDO.setTable(table);
			 pdfReportGeneratorDO.setMongoSingle(mongoSingle);
			 pdfReportGeneratorDO.setLocationHierarchObject(locationHierarchyObject);
			 pdfReportGeneratorDO.setlDeviceTransactionDocument(lDeviceTransactionDocument);
			 pdfReportGeneratorDO.setReportConfigObject(pdfReportConfigObject);
			 pdfReportGeneratorDO.setReportId("5abbee26f6e86a422fa2bca8");
			 BasicAWSCredentials credentials = new BasicAWSCredentials(s3ConfigObject.getString("access_key"),s3ConfigObject.getString("secret_key"));
			 AmazonS3 s3Client=S3Utility.establishConnectionToS3(credentials);
			 pdfReportGeneratorDO.setS3Client(s3Client);
			 //ReportGeneratorUtility.downloadChart(pdfReportGeneratorDO, templateObject, "pie_chart",siteTemplateDoc);
			 StringBuffer getAssetContent=ReportGeneratorUtility.generateTableContentsForAsset(listReportGeneratorDO, siteTemplateDoc, pdfReportGeneratorDO, templateObject);	
			 ReportGeneratorUtility.convertHtmlToPDF(getAssetContent, templateObject.getString("template_microservice_path")+"test_local.pdf", templateObject.getString("template_microservice_path"));
			}
			/**********Local Test Case ends*********/
			
		}catch(Exception e){
			e.printStackTrace();
			
		}finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
	}

	
	
}
