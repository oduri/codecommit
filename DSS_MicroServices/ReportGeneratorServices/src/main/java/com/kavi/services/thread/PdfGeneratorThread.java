package com.kavi.services.thread;

import java.io.File;
import java.util.List;

import org.bson.Document;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.kavi.common.utility.CommonUtility;
import com.kavi.common.utility.S3Utility;
import com.kavi.reportgenerator.dataobjects.ReportGeneratorDO;
import com.kavi.reportgenerator.dataobjects.ReportGeneratorTableDO;
import com.kavi.reportgenerator.utility.ReportGeneratorMailUtility;
import com.kavi.reportgenerator.utility.ReportGeneratorUtility;

public class PdfGeneratorThread extends Thread {
	
	private Document pdfReportConfigObject;
	private Document s3ConfigObject;
	private Document emailConfigObject;
	private	ReportGeneratorDO pdfReportGeneratorDO;
	private Document templateObject;
	private String reportId;
	private String fileName;
	private long size=0L;
	private String emailId;

	/**
	 * 
	 * @param pdfReportGeneratorDO
	 */
	public PdfGeneratorThread(ReportGeneratorDO pdfReportGeneratorDO,Document pdfReportConfigObject){
		this.pdfReportGeneratorDO=pdfReportGeneratorDO;
		this.pdfReportConfigObject=pdfReportConfigObject;
		this.s3ConfigObject=(Document) pdfReportConfigObject.get("s3_config");
		this.emailConfigObject=(Document)pdfReportConfigObject.get("email_config");
		this.templateObject=(Document)pdfReportConfigObject.get("template");
		this.reportId=pdfReportGeneratorDO.getReportId();
		this.emailId=pdfReportGeneratorDO.getEmailId();
		this.fileName=pdfReportGeneratorDO.getFileName();
	}
	
	@SuppressWarnings("unchecked")
	public void run() {
		try{
			
			ReportGeneratorUtility.insertOrUpdateReport(pdfReportGeneratorDO, "Update", "Processing","Success", 0L);
			int hour=0;
			/********Old approach starts*******/
			//StringBuffer htmlContents=ReportGeneratorUtility.generateTableContentsForAsset(listReportGeneratorDO);
			//ReportGeneratorUtility.updateHtmlContent(pdfReportGeneratorDO.getDb(), htmlContents,"htmlContent",reportId);
			/*********old approach ends******/
			BasicAWSCredentials credentials = new BasicAWSCredentials(s3ConfigObject.getString("access_key"),s3ConfigObject.getString("secret_key"));
			AmazonS3 s3Client=S3Utility.establishConnectionToS3(credentials);
			pdfReportGeneratorDO.setS3Client(s3Client);
			File generateDirectory=new File(templateObject.getString("pdf_output_path")+"output/"+pdfReportGeneratorDO.getReportId());
			generateDirectory.mkdir();
			List<ReportGeneratorTableDO> listReportGeneratorDO= ReportGeneratorUtility.groupUnitAssetNameWithStatus(pdfReportGeneratorDO.getLocationHierarchObject());
			org.jsoup.nodes.Document siteTemplateDoc=ReportGeneratorUtility.loadTemplate(templateObject.getString("template_microservice_path")+"SiteInfo.html");
			StringBuffer htmlContents=ReportGeneratorUtility.generateTableContentsForAsset(listReportGeneratorDO,siteTemplateDoc,
					pdfReportGeneratorDO,templateObject);
			String fileExtension=".pdf";
			String outputpdfFileName=generateDirectory.getPath()+"/"+pdfReportGeneratorDO.getFileName()+fileExtension;
			boolean flag=ReportGeneratorUtility.convertHtmlToPDF(htmlContents, outputpdfFileName,templateObject.getString("pdf_output_path")+"output/"+pdfReportGeneratorDO.getReportId()+"/");
			ReportGeneratorUtility.updateHtmlContent(pdfReportGeneratorDO.getDb(), htmlContents.toString(),"htmlContent",reportId);
			if(flag){
				String bucketName=s3ConfigObject.get("bucket_name").toString();
				String suffix=s3ConfigObject.get("suffix").toString();
				flag=S3Utility.writeFileToS3(credentials, bucketName, suffix, pdfReportGeneratorDO.getFileName()+fileExtension, 
						generateDirectory.getPath()+"/"+fileName+fileExtension);
				boolean mailflag=false;
				if(flag){
					  if(s3ConfigObject.get("link_expire_hour")!=null ){
						  hour=Integer.parseInt(s3ConfigObject.get("link_expire_hour").toString());
						  if(hour==0){
							  hour=1;
						  }
					  }
					  String url=S3Utility.generatePreSignedURL(bucketName, suffix+"/"+fileName+fileExtension,hour);
					  ReportGeneratorUtility.updateS3infoReport(pdfReportGeneratorDO.getDb(), bucketName, suffix+"/"+fileName+fileExtension, "UpdateS3Info", reportId,url,hour);
					  if(templateObject.getBoolean("email_flag_enable_link")){
						  if(templateObject.getBoolean("ses_mail_flag")) {
							  mailflag=ReportGeneratorMailUtility.sendToSESMail(emailId,  url,hour,emailConfigObject,pdfReportGeneratorDO);
						  }
						  else {
							  mailflag=ReportGeneratorMailUtility.sendEmailWithLink(emailId, url, hour, emailConfigObject, pdfReportGeneratorDO);
						  }
						  if(mailflag==false){
							  ReportGeneratorUtility.insertOrUpdateReport(pdfReportGeneratorDO, "MailError", "Error in Sending Email","Error", size);
						  }else{
							  ReportGeneratorUtility.insertOrUpdateReport(pdfReportGeneratorDO, "UpdateFileSize", "Completed","Success", size);
						  }
					  }else{
						  ReportGeneratorUtility.insertOrUpdateReport(pdfReportGeneratorDO, "UpdateFileSize", "Completed","Success", size);
					  }
					 
				 }else{
					 if(templateObject.getBoolean("ses_mail_flag")) {
						 mailflag=ReportGeneratorMailUtility.sendToSESMail(emailId,  "Please contact administrator Error in writing to S3",
					    		 hour,emailConfigObject,pdfReportGeneratorDO);
					 }else {
						 mailflag=ReportGeneratorMailUtility.sendEmailWithLink(emailId,  "Please contact administrator Error in writing to S3",
					    		 hour,emailConfigObject,pdfReportGeneratorDO);
					 }
				     if(mailflag==false){
				    	 ReportGeneratorUtility.insertOrUpdateReport(pdfReportGeneratorDO, "MailError", "Error in Sending Email","Error", size);
				     }else{
				    	 ReportGeneratorUtility.insertOrUpdateReport(pdfReportGeneratorDO, "UpdateFileSize", "Completed","Success", size);
				     }
				 }
				
			}else{
				ReportGeneratorUtility.insertOrUpdateReport(pdfReportGeneratorDO, "Error", "Error in Generating PDF","Error", size);
			}
			if(templateObject.getBoolean("store_file_locally")==false){
				ReportGeneratorUtility.deleteFolder(templateObject.getString("pdf_output_path")+"output/"+pdfReportGeneratorDO.getReportId());
			}
		 }catch(Exception e){
			 e.printStackTrace();
			 ReportGeneratorUtility.insertOrUpdateReport(pdfReportGeneratorDO, "Error", "Exception Occured",e.getMessage(), 0L);
		 }
		finally{
			if(pdfReportGeneratorDO.getMongoSingle()!=null){
				CommonUtility.closeMongoConnection(pdfReportGeneratorDO.getMongoSingle(), pdfReportGeneratorDO.getDb(), pdfReportGeneratorDO.getTable());
			}
		}
	}
	
	public static void main(String args[]){
		BasicAWSCredentials credentials = new BasicAWSCredentials("aaa","dddddd");
		//generatePreSignedURL(credentials);
		//String uploadFilePath="C:\\Users\\sarav\\Downloads\\doc1.txt";
		//String uploadFileName="doc1.txt";
	    //S3Utility.writeFileToS3(credentials, "dss-file-share","/report/tml_report", uploadFileName,uploadFilePath);
		String s3FileName="test";
		String fileName="test";
		String fileExtension=".html";
	    String bucketName="tml-reduction-report";
	    String suffix="report";
	    String content="welcome";
	    S3Utility.writeFileToS3(credentials, bucketName, "report","test.html" , "c:\\users\\sarav\\downloads\\test.html");
		//S3Utility.saveContentToS3(credentials, bucketName, "report/"+fileName+fileExtension, content);
		String url=S3Utility.generatePreSignedURL(bucketName, suffix+"/"+fileName+fileExtension,1);
		System.out.println("url..."+url); 
	}
	
}
