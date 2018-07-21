package com.kavi.reportgenerator.utility;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;	
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.allcolor.yahp.cl.converter.CHtmlToPdfFlyingSaucerTransformer;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.commons.codec.digest.DigestUtils;
//import org.allcolor.yahp.cl.converter.CHtmlToPdfFlyingSaucerTransformer;
//import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.json.simple.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.kavi.common.utility.CommonUtility;
import com.kavi.common.utility.S3Utility;
import com.kavi.reportgenerator.dataobjects.ReportGeneratorDO;
import com.kavi.reportgenerator.dataobjects.ReportGeneratorTableDO;
import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class ReportGeneratorUtility {
	 
	private final static Logger logger = Logger.getLogger(ReportGeneratorUtility.class);
	
	private static Map<String,Integer> lStatusCode=getStatusCode();
	
	private static DateFormat currentDateTimeFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

	private static DateFormat convertCurrentDateTimeToUtc = new SimpleDateFormat("MM/dd/yyyy hh:mm a z");

	private static DateFormat convertUtcToFinalDateTime = new SimpleDateFormat("MM/dd/yyyy hh:mm a ");

	private static DateFormat originalDateTime = new SimpleDateFormat("MM/dd/yyyy hh:mm a z");
	
	private static Map<String,String> lDivIdNavigation=new HashMap<String,String>();
	
	/**
	 * 
	 * @param requestUrl
	 * @param payload
	 * @param outputPath
	 */
	private static String downloadChartFromHighChartExportServer(String requestUrl, String payload,String outputPath,String filterType) {
		try {
			System.out.println("downloadChartFromHighChartExportServer...."+requestUrl);
			URL url = new URL(requestUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
			writer.write(payload);
			writer.close();
			Map map =connection.getHeaderFields();
			String fileName="";
			if(map.get("Content-Disposition")!= null) {
				String raw = map.get ( "Content-Disposition" ).toString ();
                if ( raw != null && raw.indexOf ( "=" ) != -1 )
                {
                    fileName = raw.split ( "=" )[1]; // getting value after '='
                    fileName = fileName.replaceAll ( "\"", "" ).replaceAll ( "]", "" );
                }
			}
			InputStream input = connection.getInputStream();
			byte[] buffer = new byte[4096];
			int n;
			OutputStream output = new FileOutputStream( new File(outputPath+filterType+"_"+fileName) );
			while ((n = input.read(buffer)) != -1) 	{
			    output.write(buffer, 0, n);
			}
			input.close();
			output.close();
			connection.disconnect();
			return outputPath+filterType+"_"+fileName;
		} catch (Exception e) {
			System.out.println("downloadChartFromHighChartExportServer Exception...."+e.getMessage());
			e.getMessage();
			throw new RuntimeException(e.getMessage());
		}
		
	}

	 /**
	  * 
	  * @param assetId
	  * @param deviceId
	  * @param table
	  * @param companyId
	  * @return
	  */
 	public static Document filterLocationHierarchyDocument(String type,BasicDBObject jsonDocument,MongoCollection<Document> table,Map<String,Integer> lLatestDeviceStatus,String deviceCategoryType){
 		Map<String,String> unwindObjectHierarchy=lObjectHierarchy(type);
 		List<Document> listDocument=new ArrayList<Document>();
 		Document projectQuery=new Document();
		Document  project=new Document();
		for (Map.Entry<String,String> entry : unwindObjectHierarchy.entrySet()) {
			Document unwindDoc=new Document();
    		unwindDoc.put("$unwind",unwindObjectHierarchy.get(entry.getKey()));
    		project.put("company.name",1);
    		project.put(entry.getKey()+".id",1);
    		project.put(entry.getKey()+".unit",1);
    		project.put(entry.getKey()+".asset",1);
    		project.put(entry.getKey()+".name",1);
    		project.put(entry.getKey()+".category",1);
    		project.put(entry.getKey()+".channels",1);
    		project.put(entry.getKey()+".device_category",1);
    		listDocument.add(unwindDoc);
		}		
		Document searchQuery=new Document();
		searchQuery.put("company.id",jsonDocument.getString("company_id"));
		if(type!=null && type.equalsIgnoreCase("site")){
			searchQuery.put("company.plant.site.id", jsonDocument.getString("site_id"));
 	    } 
		if(type!=null && type.equalsIgnoreCase("plant")){
			searchQuery.put("company.plant.id", jsonDocument.getString("plant_id"));
 	    }
		Document matchQuery=new Document();
		searchQuery.put("version_control.active_flag", true);
		matchQuery.put("$match",searchQuery);
		projectQuery.put("$project", project);
		listDocument.add(matchQuery);
		listDocument.add(projectQuery);
		AggregateIterable<Document>  iterable = table.aggregate(listDocument);
		Document finalObject=new Document();
		String companyName="";
		for (Document row : iterable) {
			if(type!=null && type.equalsIgnoreCase("plant")){
				Document object=(Document)row.get("company");
				companyName=object.getString("name");
				finalObject=(Document)object.get("plant");
			}
			if(type!=null && type.equalsIgnoreCase("site")){
				Document object=(Document)row.get("company");
				companyName=object.getString("name");
				object=(Document)object.get("plant");
				finalObject=(Document)object.get("site");
			}
		}
		/***************New Approach***********/
		List<Document> unitListDoc=(List<Document>) finalObject.get("unit");
		JSONArray finalArray=new JSONArray();
		if(unitListDoc!=null && unitListDoc.size()>0){
			JSONArray unitArray=null;
			for(Document unitDoc:unitListDoc){
				List<Document> assetListDoc=(List<Document>) unitDoc.get("asset");
				JSONArray assetArray=new JSONArray();
				if(assetListDoc!=null && assetListDoc.size()>0){
					for(int assetListDocIterate=0;assetListDocIterate< assetListDoc.size();assetListDocIterate++){
						Document assetDoc=(Document)assetListDoc.get(assetListDocIterate);
						List<Document> deviceCategoryListDoc=(List<Document>) assetDoc.get("device_category");
						if(deviceCategoryListDoc!=null && deviceCategoryListDoc.size()>0){
							for(int deviceCategoryDocIterate=0;deviceCategoryDocIterate< deviceCategoryListDoc.size();deviceCategoryDocIterate++){
								Document deviceCategoryDoc=(Document)deviceCategoryListDoc.get(deviceCategoryDocIterate);
								if(deviceCategoryDoc.getString("type").equalsIgnoreCase("Vibration")){
									Document unitTempDoc=new Document();
									unitTempDoc.put("id", unitDoc.getString("id"));
									unitTempDoc.put("name", unitDoc.getString("name"));
									unitTempDoc.put("category", "unit");
									assetArray=new JSONArray();
									assetArray.add(assetDoc);
									unitTempDoc.put("asset", assetArray);
									finalArray.add(unitTempDoc);
								}
							}
						}
					}
				}
			}
		}
		
		Document finalSiteObject=new Document();
		finalSiteObject.put("id", finalObject.getString("id"));
		finalSiteObject.put("name", finalObject.getString("name"));
		finalSiteObject.put("category", "site");
		finalSiteObject.put("unit", finalArray);
		finalSiteObject=traverseObject(finalSiteObject, lLatestDeviceStatus);
		finalSiteObject.put("company_name", companyName);
		return finalSiteObject;
		/***************New Approach***********/
		
		/*finalObject=traverseObject(finalObject, lLatestDeviceStatus);
		finalObject.put("company_name", companyName);
		return finalObject*/
	}
 	
 	
 	
   /**
    * 	
    * @param type
    * @return
    */
   private static Map<String,String> lObjectHierarchy(String type){
	 Map<String,String> unwindObjectHierarchy=new HashMap<String,String>();
	 if(type!=null && type.equalsIgnoreCase("plant")){
		  	unwindObjectHierarchy.put("company.plant","$company.plant");
     }
	 else if(type!=null && type.equalsIgnoreCase("site")){
	  	unwindObjectHierarchy.put("company.plant","$company.plant");
	  	unwindObjectHierarchy.put("company.plant.site","$company.plant.site");
	  }
	 return unwindObjectHierarchy;
   }
   
   /**
	 * 
	 * @param array
	 * @param userId
	 * @throws Exception
	 */
   private static void traverseArray(List<Document> array,Map<String, Integer> lLatestDeviceStatus) {
       Object[] objects = array.toArray();
       int i, length = objects.length;
       for (i = 0; i < length; i++) {
           Object object = objects[i];
           if (object instanceof Document) {
           	Document document = (Document) object;
           	traverseObject(document,lLatestDeviceStatus);
           }
       }
   }
   
   /**
    * 
    * @param object
    * @param userId
    * @param llatestDeviceStatus
    * @return
    */
   private static Document traverseObject(Document object, Map<String, Integer> llatestDeviceStatus) {
   	Document objectUpd=new Document();
    Set<String> keys = object.keySet();
       for (String key : keys) {
       	Object value = object.get(key);
           if (value instanceof List) {
           	traverseArray((List) value, llatestDeviceStatus);
           } else if (value instanceof Document) {
               object.put(key, traverseObject((Document) value,llatestDeviceStatus));
           }else if(value instanceof String){
           	if(llatestDeviceStatus.get(object.get("id"))!=null){
           		//object.put("status", llatestDeviceStatus.get(object.get("id")));
           		objectUpd.put("status", llatestDeviceStatus.get(object.get("id")));
           	}
           }
       }
       object.putAll((Map)objectUpd);
       object.remove("user");
       return object;
   }
   
   /**
    * 
    * @param htmlFileName
    * @param outputpdfFileName
    * @return
    */
   /*public static boolean convertHtmlToPDF(StringBuffer htmlContents,String outputpdfFileName,String path){
	   try{
			 	CYaHPConverter converter = new CYaHPConverter();
			    File fout = new File(outputpdfFileName);
			    FileOutputStream out = new FileOutputStream(fout);
			    //String htmlContents = new String(Files.readAllBytes(Paths.get(templateName)), StandardCharsets.UTF_8);
			    Map<String,String> properties = new HashMap<String,String>();
			    List headerFooterList = new ArrayList();
			    properties.put(IHtmlToPdfTransformer.PDF_RENDERER_CLASS,
			                   IHtmlToPdfTransformer.FLYINGSAUCER_PDF_RENDERER);
			    converter.convertToPdf(htmlContents.toString(),
			                IHtmlToPdfTransformer.A4P,
			                headerFooterList,
			                "file:///opt",
			                out,
			                properties);
			    
			    out.flush();
			    out.close();
			   FileOutputStream fos =new FileOutputStream(new File(path+"generatedOutput.html"));
			   fos.write(htmlContents.toString().getBytes());
			   fos.close();
		       CHtmlToPdfFlyingSaucerTransformer converter=new CHtmlToPdfFlyingSaucerTransformer();
				File fout = new File(outputpdfFileName);
			    FileOutputStream out = new FileOutputStream(fout);
			    Map properties = new HashMap();
			    List headerFooterList = new ArrayList();
			    
			    properties.put(IHtmlToPdfTransformer.PDF_RENDERER_CLASS,
			                   IHtmlToPdfTransformer.FLYINGSAUCER_PDF_RENDERER);
			    
			    //properties.put(IHtmlToPdfTransformer.FOP_TTF_FONT_PATH, "/usr/share/fonts/stix");
			    
		        InputStream inputStream=new FileInputStream(new File(path+"generatedOutput.html"));	
			    converter.transform(inputStream, "file:///temp", IHtmlToPdfTransformer.A4L, headerFooterList, properties,out);
			    out.flush();
			    out.close();
			    
			    return true;

		}catch(Exception e){
			e.printStackTrace();
		}
	   
	   return false;
   }*/
   /**
    * 
    * @param pdfReportGeneratorDO
    * @param type
    * @param comments
    * @param statusMessage
    * @param size
    * @return
    */
   public static String insertOrUpdateReport(ReportGeneratorDO pdfReportGeneratorDO,String type,String comments,String statusMessage,long size){
	    MongoCollection<Document> table=pdfReportGeneratorDO.getDb().getCollection(MongoDBConstants.MONGO_COLLECTION_REPORT);
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS'Z'");
		dateFormatLocal.setTimeZone(TimeZone.getTimeZone("UTC"));
		if(type!=null && type.equalsIgnoreCase("Insert")){
			 JSONArray statusArray = new JSONArray();
			 Document reportObject=new  Document();
			 Document statusDoc=new  Document();
			 Document generalObject=new Document();
			 generalObject.put("file_name",pdfReportGeneratorDO.getFileName());
			 generalObject.put("email_id",pdfReportGeneratorDO.getEmailId());
			 generalObject.put("query",pdfReportGeneratorDO.getQuery());
			 generalObject.put("requested_date", dateFormatLocal.format(new Date()));
			 reportObject.put("general", generalObject);
			 statusDoc.put("status_cmnts", comments);
			 statusDoc.put("status_msg", statusMessage);
			 statusDoc.put("application", "ui");
			 statusArray.add(statusDoc);				
			 reportObject.put("status",statusArray);
			 table.insertOne(reportObject);
			 ObjectId objectId = (ObjectId)reportObject.get("_id");
			 Document updateQuery=new Document("_id",new ObjectId(objectId.toString()));
			 Document updateData=new Document();
			 Document command = new Document();
			 updateData.put("general.id",objectId.toString());
			 updateData.put("current_status", comments);
			 updateData.put("status_msg",statusMessage);
			 command.put("$set", updateData);
			 table.updateOne(updateQuery, command);
			 return objectId.toString();
		}
		else if(type!=null && type.equalsIgnoreCase("Update")){
			
			Document searchQuery = new Document("_id",new ObjectId(pdfReportGeneratorDO.getReportId()));
			Document statusDoc=new  Document();
			statusDoc.put("status_cmnts", comments);
			statusDoc.put("status_msg", statusMessage);
			statusDoc.put("application", "thread");
			Document general=new Document();
		  	general.put("current_status",comments);
		  	general.put("status_msg",statusMessage);
		  	Document update = new Document().append("$set",general);
		  	Document addToSet = new Document("$addToSet", new Document("status",statusDoc));
			update.putAll((Map)addToSet);
			table.updateOne(searchQuery, update);
		}
	
		else if(type!=null && (type.equalsIgnoreCase("UpdateFileSize") || type.equalsIgnoreCase("MailError"))){
			Document searchQuery = new Document("_id",new ObjectId(pdfReportGeneratorDO.getReportId()));
			Document statusDoc=new  Document();
			statusDoc.put("status_cmnts", comments);
			statusDoc.put("status_msg", statusMessage);
			statusDoc.put("application", "thread");
			BasicDBObject general=new BasicDBObject();
		  	general.put("general.file_size",FileUtils.byteCountToDisplaySize(size));
		  	general.put("general.file_size_bytes",size);
		  	general.put("current_status",comments);
		  	general.put("status_msg",statusMessage);
		  	Document update = new Document().append("$set",general);
		  	Document addToSet = new Document("$addToSet", new BasicDBObject("status",statusDoc));
			update.putAll((Map)addToSet);
			table.updateOne(searchQuery, update);
		}
		
		else if(type!=null && type.equalsIgnoreCase("Error")){
			Document searchQuery = new Document("_id",new ObjectId(pdfReportGeneratorDO.getReportId()));
			Document statusDoc=new  Document();
			statusDoc.put("status_cmnts", comments);
			statusDoc.put("status_msg", statusMessage);
			statusDoc.put("application", "thread");
			BasicDBObject general=new BasicDBObject();
		  	general.put("current_status",comments);
		  	general.put("status_msg",statusMessage);
		  	Document update = new Document().append("$set",general);
		  	Document addToSet = new Document("$addToSet", new BasicDBObject("status",statusDoc));
			update.putAll((Map)addToSet);
			table.updateOne(searchQuery, update);
		}
		return "";
	  }
   
   /**
    * 
    * @param db
    * @param bucketName
    * @param bucketKeyPath
    * @param type
    * @param reportId
    * @param url
    * @param hour
    */
	public static void updateS3infoReport(MongoDatabase db,String bucketName,String bucketKeyPath,String type,String reportId,String url,int hour){
		MongoCollection<Document> table=db.getCollection(MongoDBConstants.MONGO_COLLECTION_REPORT);
		if(type!=null && type.equalsIgnoreCase("UpdateS3Info")){
			Document searchQuery = new Document("_id",new ObjectId(reportId));
			BasicDBObject s3_info=new BasicDBObject();
			s3_info.put("s3_info.link",url);
			s3_info.put("s3_info.bucket_name",bucketName);
			s3_info.put("s3_info.bucket_key_path",bucketKeyPath);
			s3_info.put("s3_info.link_valid",hour);
			s3_info.put("s3_info.created_date", new java.util.Date());
			Document update = new Document().append("$set",s3_info);
		    table.updateOne(searchQuery, update);
		  }
		}
		
   
	/**
	 * 
	 * @param db
	 * @param bucketName
	 * @param htmlContent
	 * @param type
	 * @param reportId
	 */
	public static void updateHtmlContent(MongoDatabase db,String htmlContent,String type,String reportId){
		MongoCollection<Document> table=db.getCollection(MongoDBConstants.MONGO_COLLECTION_REPORT);
		if(type!=null && type.equalsIgnoreCase("htmlContent")){
			Document searchQuery = new Document("_id",new ObjectId(reportId));
			BasicDBObject htmlContentInfo=new BasicDBObject();
			htmlContentInfo.put("html_content.content",htmlContent);
			Document update = new Document().append("$set",htmlContentInfo);
		    table.updateOne(searchQuery, update);
		  }
		}
		
	
    /**
     *  
     * @param mongodb
     * @return
     */
    public static Document getReportConfig(MongoDatabase mongodb){
    	MongoCollection<Document> table =mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_REPORT_CONFIG);
    	Document searchQuery = new Document();
		FindIterable<Document> sessioncursor = table.find(searchQuery);
		Document reportObject=new Document();
		if(sessioncursor.iterator().hasNext()){
			reportObject=(Document)sessioncursor.iterator().next();
		}
		return reportObject;
    		
    } 
	   
   
    /**
	 * 
	 * @return
	 */
	public static String getTimeStamp(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		return "report_"+sdf.format(timestamp);
			
	}
	/**
	 * 
	 * @param doc
	 * @return
	 */
	public static List<ReportGeneratorTableDO> groupUnitAssetNameWithStatus(Document doc){
		List<ReportGeneratorTableDO> listReportGeneratorTableDO=new ArrayList<ReportGeneratorTableDO>();
		Map<Integer,String> lStatusMap=new HashMap<Integer,String>();
		ReportGeneratorTableDO reportGeneratorTableDO=new ReportGeneratorTableDO();
		List<Document> unitListDoc=(ArrayList<Document>) doc.get("unit");
		if(unitListDoc!=null && unitListDoc.size()>0){
			for(Document unitDoc:unitListDoc){
				reportGeneratorTableDO=new ReportGeneratorTableDO();
				reportGeneratorTableDO.setSiteId(doc.getString("id"));
				reportGeneratorTableDO.setSiteName(doc.getString("name"));
				reportGeneratorTableDO.setCompanyName(doc.getString("company_name"));
				reportGeneratorTableDO.setUnitName(unitDoc.getString("name"));
				List<Document> assetListDoc=(ArrayList<Document>) unitDoc.get("asset");
				reportGeneratorTableDO.setAssetList(assetListDoc);
				lStatusMap=new HashMap<Integer,String>();
				for(Document assetDoc:assetListDoc){
					if(assetDoc.get("status")==null && lStatusMap.get(assetDoc.getInteger("status"))==null){
						String assetName="";
						if(lStatusMap.get(-1)!=null){
							assetName=lStatusMap.get(-1)+","+assetDoc.getString("name");
						}else{
							assetName=assetDoc.getString("name");
						}
						lStatusMap.put(-1, assetName);
					}
					else if(assetDoc.get("status")==null && lStatusMap.get(-1)!=null){
						String assetName=lStatusMap.get(assetDoc.getInteger("status"))+","+assetDoc.getString("name");
						lStatusMap.put(-1, assetName);
					}
					
					else if(assetDoc.get("status")!=null && lStatusMap.get(assetDoc.getInteger("status"))==null){
						String assetName=assetDoc.getString("name");
						lStatusMap.put(assetDoc.getInteger("status"), assetName);
					}
					else if(lStatusMap.get(assetDoc.getInteger("status"))!=null){
						String assetName=lStatusMap.get(assetDoc.getInteger("status"))+","+assetDoc.getString("name");
						lStatusMap.put(assetDoc.getInteger("status"), assetName);
					}
				}
				reportGeneratorTableDO.setlStatusMap(lStatusMap);
				listReportGeneratorTableDO.add(reportGeneratorTableDO);
			}
		}
		
		return listReportGeneratorTableDO;
	}
	/**
	 * 
	 * @param listReportGeneratorDO
	 * @return
	 */
	public static StringBuffer generateTableContentsForAsset(List<ReportGeneratorTableDO> listReportGeneratorTableDO){
		String element=new String();
		StringTokenizer token=null;
		Map<Integer,String>  lStatusMapDocuments=getStatusInfoMappingDocuments();
		StringBuffer sb=new StringBuffer();
		sb.append("<table width='800' cellspacing='0' cellpadding='0'>");
		sb.append("<tr>");
		sb.append("<td>");
		String generateDiv="homeScreen";
		int iterate=0;
		for(ReportGeneratorTableDO reportGeneratorTableDO:listReportGeneratorTableDO){
			sb.append("<table width='100%' border='1' cellspacing='0' cellpadding='10'>");
			addNewLine(sb);
			sb.append("<tr>");
			sb.append("<td colspan='2'>Unit Name:"+reportGeneratorTableDO.getUnitName()+"</td>");
			sb.append("</tr>");
			for(Integer intData:reportGeneratorTableDO.getlStatusMap().keySet()){
				sb.append("<tr>");
				sb.append("<td>"+lStatusMapDocuments.get(intData)+"</td>");
				token = new StringTokenizer(reportGeneratorTableDO.getlStatusMap().get(intData),",");
				sb.append("<td>");
				while (token.hasMoreTokens()) {
					element=token.nextToken();
					sb.append("<a href='#"+generateDiv+iterate+"'>"+element+"</a>&nbsp;");
					iterate=iterate+1;
				}
				sb.append("</td>");
				sb.append("</tr>");
			}
			sb.append("</table>");
			addNewLine(sb);
		}
		addNewLine(sb);
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");
		generateAssetPage(listReportGeneratorTableDO,sb,null,null,null);
		return sb;
	}
	
	/**
	 * 
	 * @param text
	 * @param character
	 * @return
	 */
	private static void updateElementName(String text,String character,String searchString,String replaceString,Element obj,boolean prefixStringFlag){
		String prefixString="";
		if(prefixStringFlag){
			prefixString=text.substring(0,text.indexOf(":")+1);
			
		}
		String getText=text.substring(text.indexOf(character)+1,text.length());
		if(getText.equalsIgnoreCase(searchString)){
			if(prefixStringFlag){
				obj.text(prefixString+replaceString);
			}else{
				obj.text("");
				obj.append(replaceString);
			}
		}
	}
	
	/**
	 * 
	 * @param text
	 * @param character
	 * @return
	 */
	private static void updateElementName(String text,String character,Element td,boolean prefixStringFlag,Document row,BasicDBObject jsonDocument){
		String prefixString="";
		if(prefixStringFlag){
			prefixString=text.substring(0,text.indexOf(":")+1);
		}
		String key=text.substring(text.indexOf(character)+1,text.length());
		if(prefixStringFlag){
			if(row.get(key) instanceof Date){
				String data=convertDateTimeToDifferentTimeZone(row.get(key).toString(), jsonDocument.getString("timezone"));
				td.text(prefixString+data);
			}else{
				td.text(prefixString+row.get(key));
			}
		}else{
			td.text("");
			if(row.get(key) instanceof Date){
				String data=convertDateTimeToDifferentTimeZone(row.get(key).toString(), jsonDocument.getString("timezone"));
				td.text(data);
			}else{
				td.append(""+row.get(key));
			}
		}
	}
	/**
	 * 
	 * @param text
	 * @param character
	 * @param searchString
	 * @param replaceString
	 * @param td
	 * @param prefixStringFlag
	 */
	private static void updateHyperLink(String text,String character,String searchString,String replaceString,Element td,boolean prefixStringFlag){
		String prefixString="";
		if(prefixStringFlag){
			prefixString=text.substring(0,text.indexOf(":")+1);
			
		}
		String getText=text.substring(text.indexOf(character)+1,text.length());
		if(getText.equalsIgnoreCase(searchString)){
			if(prefixStringFlag){
				td.text(prefixString+replaceString);
			}else{
				td.text(replaceString);
			}
		}
	}
	
	
	/**
	 * 
	 * @param listReportGeneratorDO
	 * @return
	 */
	public static StringBuffer generateTableContentsForAsset(List<ReportGeneratorTableDO> listReportGeneratorTableDO,org.jsoup.nodes.Document siteTemplateDoc,
			ReportGeneratorDO pdfReportGeneratorDO,Document templateObject){
		System.out.println("generateTableContentsForAsset method called");
		lDivIdNavigation=new HashMap<String,String>();
		Map<Integer,String>  lStatusMapDocuments=getStatusInfoMappingDocuments();
		System.out.println("lStatusMapDocuments..."+lStatusMapDocuments);
		StringBuffer sb=new StringBuffer();
		StringBuffer sbupd=new StringBuffer();
		StringTokenizer token=null;
		String element=null;
		StringBuffer hyperLink=new StringBuffer();
		Map<Integer,String> lTableContentMap=new HashMap<Integer,String>();
		int iterate=0;
		Map<String,String> lHeaderMap=new HashMap<String,String>();
		for(ReportGeneratorTableDO reportGeneratorTableDO:listReportGeneratorTableDO){
			System.out.println("reportGeneratorTableDO...."+reportGeneratorTableDO);
			lHeaderMap.put("@siteName",reportGeneratorTableDO.getSiteName());
			lHeaderMap.put("@companyName",reportGeneratorTableDO.getCompanyName());
			updateElement(lHeaderMap, "siteCaption", siteTemplateDoc);
			for(Integer intData:reportGeneratorTableDO.getlStatusMap().keySet()){
				System.out.println("intData...."+intData);
				lTableContentMap.put(intData, "status"+intData);
				for (Element table : siteTemplateDoc.select("table#status"+intData)){
					System.out.println("table...."+table);
					  for (Element row : table.select("tr")) {
						   System.out.println("row..."+row);
						    Elements tds = row.select("td");
						    Elements tdElements = tds.select("td");
						    for(Element td:tdElements){
						      if(td.select("a[href]").isEmpty()){
						    	  if(td.text().contains("unitName")){
						    		  updateElementName(td.text(), "@", "unitName",reportGeneratorTableDO.getUnitName(),td,true);
						    	  }
						    	  if(td.id().contains("id")){
						    		  token = new StringTokenizer(reportGeneratorTableDO.getlStatusMap().get(intData),",");
										while (token.hasMoreTokens()) {
											element=token.nextToken();
											//hyperLink.append("<a href='#"+generateDiv+iterate+"'>"+element+"</a>&nbsp;");
											hyperLink.append("<a id='"+element+"'>"+element+"</a>&nbsp;");
											iterate=iterate+1;
											td.append(""+hyperLink);
										} 
										//updateElementName(td.text(), "@", "repeatLink",hyperLink.toString(),td,false);
										//hyperLink=new StringBuffer();
						    	  }
						      }
						 }
				     }
					  hyperLink=new StringBuffer();
				}//end of table
			}
		}
		 	
		/************Remove other table object starts if not rendered ****/
		Set<Integer> tableSet=lTableContentMap.keySet();
		Set<Integer> statusMapSet  =lStatusMapDocuments.keySet();
		statusMapSet.removeAll(tableSet);
		if(statusMapSet.size()>0){
			for(Integer intData:statusMapSet){
				siteTemplateDoc.select("table#status"+intData).remove();
			}
		}
		/************Remove other table object ends if not rendered****/
		updateImagePath(null, "siteHeaderImage", templateObject, siteTemplateDoc,"updateSiteHeaderImage",templateObject.getString("template_microservice_path")+"/images/"+"molex-logo.jpg");
		System.out.println("after update image path...");
		downloadChart(pdfReportGeneratorDO, templateObject, "pie_chart",siteTemplateDoc);
		System.out.println("downloadChart ended...");
		updateFooterDateTime(templateObject,siteTemplateDoc,pdfReportGeneratorDO);
		System.out.println("updateFooterDateTime ended...");
		siteTemplateDoc.append(generateAssetPage(listReportGeneratorTableDO,sb,pdfReportGeneratorDO,
				templateObject,pdfReportGeneratorDO.getDb()).toString());
		updateHyperLinkAttributeInSiteInfoTable(templateObject, siteTemplateDoc, pdfReportGeneratorDO,lDivIdNavigation);
		System.out.println("updateHyperLinkAttributeInSiteInfoTable ended...");
		sbupd.append(siteTemplateDoc.toString());
		return sbupd;
	}
	/**
	 * 
	 * @param templateObject
	 * @param siteTemplateDoc
	 * @param pdfReportGeneratorDO
	 * @param lDivIdNavigation
	 */
	private static void updateHyperLinkAttributeInSiteInfoTable(Document templateObject,org.jsoup.nodes.Document siteTemplateDoc,ReportGeneratorDO pdfReportGeneratorDO,
			Map<String,String> lDivIdNavigation){
		Set<String> divIdNavigation=lDivIdNavigation.keySet();
		System.out.println("updateHyperLinkAttributeInSiteInfoTable called and lDivIdNavigation....."+lDivIdNavigation);
		for(String divIdKey:divIdNavigation){
			siteTemplateDoc.select("#"+divIdKey).attr("href","#"+lDivIdNavigation.get(divIdKey));
		}
	}
	/**
	 * 
	 * @param timeZone
	 * @param format
	 */
	 private static void updateFooterDateTime(Document templateObject,org.jsoup.nodes.Document siteTemplateDoc,ReportGeneratorDO pdfReportGeneratorDO){
		 BasicDBObject document=pdfReportGeneratorDO.getJsonDocument();
		 Map<String,String>  lFooterDateTime =new HashMap<String,String>();
		 
		 if(document.get("date")!=null){
			 lFooterDateTime.put("@Date", document.getString("date"));
			 lFooterDateTime.put("@Time",document.getString("time")+ " "+document.getString("timezone"));
		 }else{
			 Date today = new Date();
			 DateFormat df = new SimpleDateFormat(templateObject.getString("date_format"));
			 df.setTimeZone(TimeZone.getTimeZone(templateObject.getString("time_zone")));
			 String format = df.format(today);
			 lFooterDateTime.put("@Date", format.substring(0, 10));
			 if(format.length()>10){
		    	lFooterDateTime.put("@Time", format.substring(10, format.length())+ " "+templateObject.getString("time_zone"));
			 }
		    else{
		    	lFooterDateTime.put("@Time", format.substring(10, format.length())+" "+templateObject.getString("time_zone"));
		    }
		 }
	    updateElement(lFooterDateTime, "footer", siteTemplateDoc);
	    
	}
	/**
	 * 
	 * @param deviceCategoryType
	 * @param mongodb
	 * @param siteId
	 * @param assetId
	 * @param deviceId
	 * @param type
	 * @param templateDoc
	 */
	private static void populateRmsVelocity(String deviceCategoryType,MongoDatabase mongodb,String siteId,String assetId,String deviceId,org.jsoup.nodes.Document templateDoc,Document templateObject){
		Map<String,String> lObjectMap=new HashMap<String,String>();
		DateFormat df = new SimpleDateFormat(templateObject.getString("date_format"));
		df.setTimeZone(TimeZone.getTimeZone(templateObject.getString("time_zone")));
		lObjectMap.put("@DayLabel",templateObject.getString("label_for_days"));
		Calendar cal = df.getCalendar().getInstance();
		lObjectMap.put("@DateTo",df.format(cal.getTime()));
		cal.add(Calendar.DAY_OF_MONTH,templateObject.getInteger("days"));
		lObjectMap.put("@DateFrom",df.format(cal.getTime()));
		updateElement(lObjectMap, "rmsvelocity", templateDoc);
	}
	/**
	 * 
	 * @param lObjectMap
	 * @param objectId
	 * @param siteTemplateDoc
	 */
	private static void updateElement(Map<String,String> lObjectMap,String objectId,org.jsoup.nodes.Document templateDoc ){
		  for (Element element : templateDoc.select("#"+objectId)) { 
			  for(Entry<String,String> entry:lObjectMap.entrySet()){
			   element.text(element.text().replaceAll(entry.getKey(), lObjectMap.get(entry.getKey())));
			  }
		 }
	}
	
	/**
	 * 
	 * @param listReportGeneratorTableDO
	 * @param sb
	 * @param pdfReportGeneratorDO
	 * @param templateObject
	 * @param mongodb
	 * @return
	 */
	private static StringBuffer generateAssetPage(List<ReportGeneratorTableDO> listReportGeneratorTableDO,StringBuffer sb,
			ReportGeneratorDO pdfReportGeneratorDO,Document templateObject,MongoDatabase mongodb){
		String siteId=null;
		int iterate=0;
		String divName="homeScreen";
		int countIterate=0;
		Map<String, Document>lDeviceTransactionDocument=pdfReportGeneratorDO.getlDeviceTransactionDocument();
		for(ReportGeneratorTableDO reportGeneratorTableDO:listReportGeneratorTableDO){
			siteId=reportGeneratorTableDO.getSiteId();
			List<Document> assetList=reportGeneratorTableDO.getAssetList();
			for(Document assetDoc:assetList){
				List<Document> deviceCategoryList=(ArrayList)assetDoc.get("device_category");
				//generateDivContentForAsset(assetDoc,sb,divName+iterate,templateObject);
				countIterate=0;
				iterate=iterate+1;
				for(Document deviceCategoryDoc:deviceCategoryList){
					if(deviceCategoryDoc.getString("type")!=null && deviceCategoryDoc.getString("type").equalsIgnoreCase("Vibration")){
						List<Document> deviceListDoc=(ArrayList)deviceCategoryDoc.get("device");
						for(Document deviceDoc:deviceListDoc){
							Document deviceMappingRow=filterDocumentByAssetId(lDeviceTransactionDocument, assetDoc.getString("id"), deviceDoc.getString("id"));
							//if(deviceMappingRow!=null && deviceDoc.getString("id").equalsIgnoreCase("970470347") ){
							if(deviceMappingRow!=null){
								System.out.println("deviceDoc..."+deviceDoc);
								org.jsoup.nodes.Document assetTemplateDoc=loadTemplate(templateObject.getString("template_microservice_path")+"AssetInfo.html");
								updatePageBreakForAsset(assetDoc, sb, divName+countIterate, templateObject, assetTemplateDoc);
								populateRmsVelocity(deviceCategoryDoc.getString("type"),mongodb, siteId, 
										assetDoc.getString("id"), deviceDoc.getString("id"),assetTemplateDoc,templateObject);
								updateImagePath(deviceDoc, "deviceImage", templateObject, assetTemplateDoc,"Device",null);
								updateAssetTableInfo(assetTemplateDoc, deviceMappingRow, pdfReportGeneratorDO.getJsonDocument());
								generateTopBottomChartForAssetPage(pdfReportGeneratorDO, templateObject, "vibration_chart", assetTemplateDoc,siteId,
										deviceDoc.getString("id"),pdfReportGeneratorDO.getJsonDocument(),divName+countIterate,assetDoc);
								String commentBuffer=getComments(deviceCategoryDoc.getString("type"),mongodb, siteId, assetDoc.getString("id"), deviceDoc.getString("id"),"rms_vel_z_cmnts",pdfReportGeneratorDO.getJsonDocument(),templateObject.getInteger("days"));
								if(commentBuffer.toString().length()>0){
									assetTemplateDoc.select("table#comments").append(commentBuffer);
										
								}else{
									assetTemplateDoc.select("table#comments").remove();
									assetTemplateDoc.select("#comments-title").remove();
									assetTemplateDoc.select("table#comments-table").remove();
									
								}
								sb.append(assetTemplateDoc.toString());
								countIterate=countIterate+1;
							}
							
						}
					}
				}
				//closeDiv(sb);
			}
			
		}
		System.out.println("generate AssetPage Ended");
		addNewLine(sb);
		return sb;
	}
	
	/**
	 * 
	 * @param assetTemplateDoc
	 * @param deviceMappingRow
	 */
	private static void updateAssetTableInfo(org.jsoup.nodes.Document assetTemplateDoc,Document deviceMappingRow,BasicDBObject jsonDocument){
		for (Element table : assetTemplateDoc.select("table#assetInfo")){
			  for (Element row : table.select("tr")) {
				    Elements tds = row.select("td");
				    Elements tdElements = tds.select("td");
				    for(Element td:tdElements){
				      if(td.select("a[href]").isEmpty()){
				    	  if(td.text().contains("@")){
				    		  updateElementName(td.text(), "@", td, true, deviceMappingRow,jsonDocument);
				    	  }
				      }	
				 }
		     }
		}
		
	}
	/**
	 * 
	 * @param jsonObject
	 * @param pdfReportGeneratorDO
	 * @param templateObject
	 * @param type
	 */
	public static void downloadChart(ReportGeneratorDO pdfReportGeneratorDO,Document templateObject,String type,org.jsoup.nodes.Document  siteTemplateDoc){
		System.out.println("downloadChart called..."+templateObject.getBoolean("communicate_export_server"));
		if(templateObject.getBoolean("communicate_export_server")){
			
			Document reportConfigObject=pdfReportGeneratorDO.getReportConfigObject();
			String json=reportConfigObject.toJson();
			String fileName="";
			Document pieChart=generatePieChart("unit", pdfReportGeneratorDO.getLocationHierarchObject(), reportConfigObject);
			fileName=downloadChartFromHighChartExportServer(templateObject.getString("high_export_server_url"), pieChart.toJson(), 
				templateObject.getString("pdf_output_path")+"/output/"+pdfReportGeneratorDO.getReportId()+"/","unit");
			updateImagePath(null, "unitPieChartImage", templateObject, siteTemplateDoc,"updatePieChartForSite",fileName);
			writeJsonToFile("unit.json", templateObject, pdfReportGeneratorDO, pieChart);
			pdfReportGeneratorDO.setReportConfigObject(Document.parse(json));
		    Document pieChartAsset=generatePieChart("asset", pdfReportGeneratorDO.getLocationHierarchObject(), pdfReportGeneratorDO.getReportConfigObject());
		 	fileName=downloadChartFromHighChartExportServer(templateObject.getString("high_export_server_url"), pieChartAsset.toJson(), 
					templateObject.getString("pdf_output_path")+"/output/"+pdfReportGeneratorDO.getReportId()+"/","asset");
			updateImagePath(null, "assetPieChartImage", templateObject, siteTemplateDoc,"updatePieChartForSite",fileName);
			writeJsonToFile("asset.json", templateObject, pdfReportGeneratorDO, pieChartAsset);
			pdfReportGeneratorDO.setReportConfigObject(Document.parse(json));
			Document pieChartDevice=generatePieChart("device", pdfReportGeneratorDO.getLocationHierarchObject(), pdfReportGeneratorDO.getReportConfigObject());
			fileName=downloadChartFromHighChartExportServer(templateObject.getString("high_export_server_url"), pieChartDevice.toJson(), 
					templateObject.getString("pdf_output_path")+"/output/"+pdfReportGeneratorDO.getReportId()+"/","device");
			updateImagePath(null, "devicePieChartImage", templateObject, siteTemplateDoc,"updatePieChartForSite",fileName);
			writeJsonToFile("device.json", templateObject, pdfReportGeneratorDO, pieChartDevice);
			
		
		}
	}
	
	private static void writeJsonToFile(String fileName,Document templateObject,ReportGeneratorDO pdfReportGeneratorDO,Document chart){
		try {
			/*FileOutputStream fos = new FileOutputStream(new File(templateObject.getString("pdf_output_path")+"output/"+pdfReportGeneratorDO.getReportId()+"/"+fileName));
			fos.write(com.mongodb.util.JSON.serialize(chart).getBytes());
			fos.close();*/
			if(templateObject.getBoolean("store_file_s3")!=null && templateObject.getBoolean("store_file_s3")) {
				Document s3ConfigObject=(Document) pdfReportGeneratorDO.getReportConfigObject().get("s3_config");
				if(s3ConfigObject!=null) {
				  //String data=com.mongodb.util.JSON.serialize(chart);
				  writeStringToS3(s3ConfigObject.getString("bucket_name")+"/"+s3ConfigObject.getString("suffix"), pdfReportGeneratorDO.getReportId()+"/"+fileName, chart.toJson(), pdfReportGeneratorDO.getS3Client());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param bucket
	 * @param key
	 * @param jsonData
	 * @param s3Client
	 * @return
	 */
	private static PutObjectResult writeStringToS3(String bucket, String key, String jsonData, AmazonS3 s3Client) {
	    ObjectMetadata meta = new ObjectMetadata();
	    meta.setContentMD5(new String(com.amazonaws.util.Base64.encode(DigestUtils.md5(jsonData))));
	    meta.setContentLength(jsonData.length());
	    InputStream stream = new ByteArrayInputStream(jsonData.getBytes(StandardCharsets.UTF_8));
	    return s3Client.putObject(bucket, key, stream, meta);
	}
	
	/**
	 * 
	 * @param assetDoc
	 * @param sb
	 * @param divId
	 * @param templateObject
	 */
	private static void generateDivContentForAsset(Document assetDoc,StringBuffer sb,String divId,Document templateObject){
		sb.append("<div id='"+divId+"' style='page-break-before:always' >");
		sb.append("<p align='center'>"+assetDoc.getString("name")+"</p>");
		if(assetDoc.getString("image_location")!=null){
			//sb.append("<p align='right'><img src="+templateObject.getString("asset_image_path")+assetDoc.getString("image_location")+"/"+assetDoc.getString("thumb_image_id")+"></img></p>");
		}
		addNewLine(sb);
		
	}
	/**
	 * 
	 * @param assetDoc
	 * @param sb
	 * @param divId
	 * @param templateObject
	 * @param templateDoc
	 */
	private static void updatePageBreakForAsset(Document assetDoc,StringBuffer sb,String divId,Document templateObject,org.jsoup.nodes.Document templateDoc){
		templateDoc.select("#auto").attr("id",divId);
		Map<String, String> lObjectMap=new HashMap<String,String>();
		lObjectMap.put("@assetName",assetDoc.getString("name"));
		updateElement(lObjectMap, "assettitle", templateDoc);
		
	}
	
	/**
	 * 
	 * @param doc
	 * @param imageId
	 * @param templateObject
	 * @param templateDoc
	 * @param htmlType
	 * @param filePath
	 */
	private static void updateImagePath(Document doc,String imageId,Document templateObject,org.jsoup.nodes.Document templateDoc,String htmlType,String filePath){
		if(htmlType.equalsIgnoreCase("Asset") && doc.getString("image_location")!=null){
		 templateDoc.select("#"+imageId).attr("src",templateObject.getString("image_path")+doc.getString("image_location")+"/device_images/"+doc.getString("thumb_image_id"));
		}
		else if(htmlType.equalsIgnoreCase("Device")){
			System.out.println("updateImagePath called..."+doc);
			if(doc.getString("image_location")!=null){
				//templateDoc.select("#"+imageId).attr("src",templateObject.getString("image_path")+doc.getString("image_location")+"/device_images/"+doc.getString("thumb_image_id"));
				templateDoc.select("#"+imageId).attr("src",templateObject.getString("image_path")+doc.getString("image_location")+"/"+doc.getString("thumb_image_id"));
			}else{
				templateDoc.select("#"+imageId).remove();
			}
		}
		else if(htmlType.equalsIgnoreCase("updatePieChartForSite")){
			templateDoc.select("#"+imageId).attr("src",filePath);
		}
		else if(htmlType.equalsIgnoreCase("updateChart")){
			templateDoc.select("#"+imageId).attr("src",filePath);
		}
		else if(htmlType.equalsIgnoreCase("updateSiteHeaderImage")){
			templateDoc.select("#"+imageId).attr("src",filePath);
		}
	}
	
	
	/**
	 * 
	 * @param lDeviceTransactionDocument
	 * @param assetId
	 * @param deviceId
	 * @return
	 */
	public static Document filterDocumentByAssetId(Map<String, Document> lDeviceTransactionDocument,String assetId,String deviceId){
		for(String str:lDeviceTransactionDocument.keySet()){
			Document row=lDeviceTransactionDocument.get(str);
			if(row!=null && row.getString("asset_id")!=null){
				if(assetId.equalsIgnoreCase(row.getString("asset_id")) && deviceId.equalsIgnoreCase(row.getString("device_id"))){
					return row;
				}
			}
		}
		return null;
	}
	
	
	
	private static void closeDiv(StringBuffer sb){
		sb.append("</div>");
	}
	
	
	/**
	 * 
	 * @param templatePath
	 * @return
	 */
	public static org.jsoup.nodes.Document loadTemplate(String templatePath){
		org.jsoup.nodes.Document doc=null;
		try {	
				File input = new File(templatePath);
				doc = Jsoup.parse(input, "UTF-8");
				
		}catch(Exception e){
			e.printStackTrace();
		}
		return doc;
	}
	
	
	/**
	 * 
	 * @param sb
	 */
	private static void addNewLine(StringBuffer sb){
		//sb.append("\n");
		
	}
	/**
	 * 
	 * @return
	 */
	private static Map<Integer,String> getStatusInfoMappingDocuments(){
		Map<Integer,String> lStatusInfoMap=new HashMap<Integer,String>();
		lStatusInfoMap.put(0, "Assets in Ok");
		lStatusInfoMap.put(1, "Assets in Alert");
		lStatusInfoMap.put(2, "Assets in Alarm");
		lStatusInfoMap.put(-1, "Assets in unknown");
		return lStatusInfoMap;
		
	}
	/**
	 * 
	 * @param mongodb
	 * @param reportConfigObject
	 * @return
	 */
	public static Map<String,Document> getMappingDocument(MongoDatabase mongodb,Document pdfReportConfigObject){
		Map<String,Document> lDeviceTransactionDocument=new HashMap<String,Document>();
		Document projectFields =new Document();
		List<Document> deviceMappingDocument=(ArrayList<Document>)pdfReportConfigObject.get("device_parameter_mapping");
		for(Document doc:deviceMappingDocument){
			projectFields.put(doc.getString("object_id"), 1);
			projectFields.put("device_name", 1);
		}
		MongoCollection<Document> table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_DEVICE_PARAM_LATEST);
		Document searchQuery=new Document();
		Document matchQuery = new Document();
		matchQuery.put("$match", searchQuery);	
		Document projectQuery=new Document();
		projectQuery.put("$project", projectFields);
		AggregateIterable<Document> iterable=table.aggregate(Arrays.asList(matchQuery,projectQuery));
		for (Document row : iterable) {
			lDeviceTransactionDocument.put(row.get("_id").toString(), row);
		}
		return lDeviceTransactionDocument;
    } 
	  
	/**
	 * 
	 * @param deviceCategoryType
	 * @param mongodb
	 * @param siteId
	 * @param assetId
	 * @param deviceId
	 * @param type
	 * @param jsonDocument
	 * @param days
	 * @return
	 */
	public static String getComments(String deviceCategoryType,MongoDatabase mongodb,String siteId,String assetId,String deviceId,String type,BasicDBObject jsonDocument,int days){
		HashMap<String, String> hDeviceCategoryName = new HashMap<String, String>();
		hDeviceCategoryName.put("Vibration", "v_");
		MongoCollection<Document> table=mongodb.getCollection(hDeviceCategoryName.get(deviceCategoryType)+siteId+MongoDBConstants.MONGO_COLLECTION_SUFFIX);
		Document searchQuery=new Document();
		searchQuery.put("device_id", deviceId);
		searchQuery.put("asset_id", assetId);	
		searchQuery.put("comments.type",type);
		Calendar toCal = Calendar.getInstance();
		Calendar fromCal = Calendar.getInstance();
		fromCal.add(Calendar.DAY_OF_MONTH,days);
		int year = fromCal.get(Calendar.YEAR);
		int monthNumber = fromCal.get(Calendar.MONTH);
		int dateNumber = fromCal.get(Calendar.DAY_OF_MONTH);
		monthNumber += 1;
		int toYear = toCal.get(Calendar.YEAR);
		int toMonthNumber = toCal.get(Calendar.MONTH);
		int toDateNumber = toCal.get(Calendar.DAY_OF_MONTH);
		toMonthNumber += 1;
		searchQuery.put("message_time",
				new BasicDBObject("$gte",
						new DateTime(year, monthNumber, dateNumber, 0, 0, DateTimeZone.UTC)
								.toDate()).append("$lte",
										new DateTime(toYear, toMonthNumber, toDateNumber, 23, 59,
												DateTimeZone.UTC).toDate()));
		Document matchQuery = new Document();
		matchQuery.put("$match", searchQuery);	
		BasicDBObject unwindComments = new BasicDBObject("$unwind","$comments");
		
		
		Document projectQuery=new Document();
		Document projectFields=new Document();
		projectFields.put("comments", 1);
		projectQuery.put("$project", projectFields);
		AggregateIterable<Document> iterable=table.aggregate(Arrays.asList(unwindComments,matchQuery,projectQuery));
		StringBuffer comments=new StringBuffer();
		for(Document row:iterable){
			comments.append("<tr>");
			row.remove("_id");
			Document commentObject=(Document)row.get("comments");
			if(commentObject!=null){
				comments.append("<td>"+commentObject.getString(type)+"</td>");
				comments.append("<td>"+commentObject.getString("user")+"</td>");
				if(commentObject.get("date") instanceof Date){
					comments.append("<td>"+convertDateTimeToDifferentTimeZone(commentObject.get("date").toString(), jsonDocument.getString("timezone"))+"</td>");
				}
			}
			comments.append("</tr>");
			
		}
		return comments.toString();
		
	}
	
	/**
     * 
     * @return
     */
    public static Map<String,Integer>  getLatestDeviceStatus(MongoDatabase mongodb,String companyId){
    	
    	Map<String,Integer>  latestDeviceStatus =new HashMap<String,Integer> ();
    	MongoCollection<Document> table=null;
		try{
//			table=mongodb.getCollection(companyId+"_"+MongoDBConstants.MONGO_COLLECTION_DEVICE_STATUS_LOG);			
			table=mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_DEVICE_STATUS_LOG);			
			Document deviceGroup = new Document("$group",
				    new Document("_id",
				        new Document("plant_id", "$plant_id")
				        .append("site_id", "$site_id")
				        .append("unit_id", "$unit_id").append("asset_id","$asset_id").append("device_id", "$device_id")
				      	)
				    .append("max", new Document("$max", "$device_status")));
			
			
			Document zeroGroup = new Document("$group",
				    new Document("_id",
				        new Document("plant_id", "$plant_id")
				        .append("site_id", "$site_id")
				        .append("unit_id", "$unit_id").append("asset_id","$asset_id")
				      	)
				    .append("max", new Document("$max", "$device_status")));
			
			
			
			Document firstGroup = new Document("$group",
				    new Document("_id",
				        new Document("plant_id", "$_id.plant_id")
				        .append("site_id", "$_id.site_id")
				        .append("unit_id", "$_id.unit_id"))
				    .append("max", new Document("$max", "$max")));
			
			Document secondGroup = new Document("$group",
				    new Document("_id",
				        new Document("plant_id", "$_id.plant_id")
				        .append("site_id", "$_id.site_id"))
				    .append("max", new Document("$max", "$max"))); 
			
			Document thirdGroup = new Document("$group",
				    new Document("_id",
				        new Document("plant_id", "$_id.plant_id"))
				    .append("max", new Document("$max", "$max"))); 
			
			Document searchQuery=new Document();
			searchQuery.put("active_status", "Yes");
			Document matchQuery = new Document();
			matchQuery.put("$match", searchQuery);	
			
			AggregateIterable<Document> iterable=table.aggregate(Arrays.asList(matchQuery,deviceGroup));
			for (Document row : iterable) {
				Document rowId=(Document)row.get("_id");
				latestDeviceStatus.put(rowId.getString("device_id"),row.getInteger("max",0));
			}
			
			iterable=table.aggregate(Arrays.asList(matchQuery,zeroGroup));
			int unitMax=0;
			int assetMax=0;
			for (Document row : iterable) {
				Document rowId=(Document)row.get("_id");
				if(latestDeviceStatus.get(rowId.getString("unit_id"))!=null){
					unitMax=latestDeviceStatus.get(rowId.getString("unit_id"));
					if(row.getInteger("max",0)>unitMax){
						latestDeviceStatus.put(rowId.getString("unit_id"),row.getInteger("max",0));
					}else{
						latestDeviceStatus.put(rowId.getString("unit_id"),unitMax);
					}
				}else{
					latestDeviceStatus.put(rowId.getString("unit_id"),row.getInteger("max",0));
				}
				if(latestDeviceStatus.get(rowId.getString("asset_id"))!=null){
					assetMax=latestDeviceStatus.get(rowId.getString("asset_id"));
					if(row.getInteger("max",0)>assetMax){
						latestDeviceStatus.put(rowId.getString("asset_id"),row.getInteger("max",0));
					}else{
						latestDeviceStatus.put(rowId.getString("asset_id"),assetMax);
					}
				}
				else{
					latestDeviceStatus.put(rowId.getString("asset_id"),row.getInteger("max",0));
				}
			}
			iterable=table.aggregate(Arrays.asList(matchQuery,zeroGroup,firstGroup,secondGroup));
			for (Document row : iterable) {
				Document rowId=(Document)row.get("_id");
				latestDeviceStatus.put(rowId.getString("site_id"),row.getInteger("max",0));
			}
			iterable=table.aggregate(Arrays.asList(matchQuery,zeroGroup,firstGroup,secondGroup,thirdGroup));
			for (Document row : iterable) {
				Document rowId=(Document)row.get("_id");
				latestDeviceStatus.put(rowId.getString("plant_id"),row.getInteger("max",0));
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			table=null;
			
	  }
		return latestDeviceStatus;
    }
    
    /******************getTimeSeriesData *************/
    /**
     * 
     * @param pdfReportGeneratorDO
     * @param templateObject
     * @param type
     * @param assetTemplateDoc
     */
    private static void generateTopBottomChartForAssetPage(ReportGeneratorDO pdfReportGeneratorDO,Document templateObject,String type,org.jsoup.nodes.Document assetTemplateDoc,
    		String siteId,String deviceId,BasicDBObject jsonDocument,String mainDivId,Document assetDoc){
    	
    	String fileName="";
    	if(templateObject.getBoolean("communicate_export_server")){
    		Document reportConfigObject=pdfReportGeneratorDO.getReportConfigObject();
        	String json=reportConfigObject.toJson();
        	Document doc=Document.parse(json);
        	Document topChart=(Document)doc.get(type);
        	Document infile=(Document)topChart.get("infile");
			List<Document> seriesList=(List<Document>)infile.get("series");
			Document timeSeriesData=getTimeSeriesDataForVibration(pdfReportGeneratorDO.getDb(), siteId, deviceId, templateObject.getInteger("days"),jsonDocument,templateObject);
			List<Document> dataList=(List<Document>) timeSeriesData.get("data");
			if(dataList!=null && dataList.size()>0){
				writeJsonToFile("timeSeriesData"+siteId+"_"+deviceId+".json", templateObject, pdfReportGeneratorDO, timeSeriesData);
				for(Document seriesListDoc: seriesList){
					 seriesListDoc.put("data", timeSeriesData.get("data"));
				}
				Document yAxis=(Document)infile.get("yAxis");
				yAxis.put("max", timeSeriesData.getDouble("yAxisMaxForVibration"));
				List<Document> plotLinesList=(List<Document>)yAxis.get("plotLines");
				Document plotLineConfig=(Document)timeSeriesData.get("plotlineconfig");
				for(Document plotLinesDoc: plotLinesList){
					if(plotLineConfig!=null && plotLineConfig.get(plotLinesDoc.getString("id"))!=null){
						String data=""+plotLineConfig.get(plotLinesDoc.getString("id"));
						plotLinesDoc.put("value", Double.parseDouble(data));
						Document plotLinesLabelDoc=(Document)plotLinesDoc.get("label");
						if(plotLinesLabelDoc.getBoolean("display",false)){
							plotLinesLabelDoc.put("text", plotLinesLabelDoc.get("text")+""+Double.parseDouble(data));
						}else{
							plotLinesLabelDoc.remove("text");
						}
					}
				}
				fileName=downloadChartFromHighChartExportServer(templateObject.getString("high_export_server_url"), com.mongodb.util.JSON.serialize(topChart), 
	    				templateObject.getString("pdf_output_path")+"/output/"+pdfReportGeneratorDO.getReportId()+"/","topChart"+siteId+"_"+deviceId);
				
				updateImagePath(null, "topChart", templateObject, assetTemplateDoc,"updateChart",fileName);
	    		writeJsonToFile("topChart"+siteId+"_"+deviceId+".json", templateObject, pdfReportGeneratorDO, topChart);
	    		
	    		
	    		json=reportConfigObject.toJson();
	        	doc=Document.parse(json);
	        	Document bottomChart=(Document)doc.get("spectrum_chart");
	        	infile=(Document)bottomChart.get("infile");
				seriesList=(List<Document>)infile.get("series");
				for(Document seriesListDoc: seriesList){
					 seriesListDoc.put("data", timeSeriesData.get("bottomGraphArray"));
				}
				
				fileName=downloadChartFromHighChartExportServer(templateObject.getString("high_export_server_url"), com.mongodb.util.JSON.serialize(bottomChart), 
	    				templateObject.getString("pdf_output_path")+"/output/"+pdfReportGeneratorDO.getReportId()+"/","bottomChart"+siteId+"_"+deviceId);
				updateImagePath(null, "bottomChart", templateObject, assetTemplateDoc,"updateChart",fileName);
	    		writeJsonToFile("bottomChart"+siteId+"_"+deviceId+".json", templateObject, pdfReportGeneratorDO, topChart);
	    		if(lDivIdNavigation.get(assetDoc.getString("name"))==null){
	    			lDivIdNavigation.put(assetDoc.getString("name"), mainDivId);
	    		}
			}else{
				assetTemplateDoc.select("#bottomChart").remove();
				assetTemplateDoc.select("#topChart").remove();
				assetTemplateDoc.select("#rmsvelocityspectrum").remove();
				assetTemplateDoc.select("#rmsvelocity").remove();
				assetTemplateDoc.select("#rmsvelocity-subtitle").remove();
				assetTemplateDoc.select("#assetinfotable").remove();
				assetTemplateDoc.select("#"+mainDivId).remove();
			}
    	}
    	
    }
    /**
     * 
     * @param mongodb
     * @param deviceId
     * @param siteId
     * @return
     */
    public static Document getTimeSeriesDataForVibration(MongoDatabase mongodb,String siteId,String deviceId,int days,BasicDBObject jsonDocument,Document templateObject){
    	DateFormat df = CommonUtility.getDateFormat("Year");
    	JSONArray timeSeriesArray = new JSONArray();
    	Document projectFields=new Document();
    	MongoCollection<Document> paramLogTable = null;
    	//projectFields.put("rms_accel_z",1);
    	//projectFields.put("rms_accel_z_cmnts",1);
    	projectFields.put("rms_vel_z",1);
    	projectFields.put("z_vel_fft_x_axis",1);
    	projectFields.put("z_vel_fft",1);
    	projectFields.put("rms_vel_z_cmnts",1);
    	projectFields.put("message_time",1);
    	projectFields.put("device_id",1);
		Document projectQuery=new Document();
		projectQuery.put("$project", projectFields);
    	Calendar toCal = Calendar.getInstance();
    	Calendar fromCal = Calendar.getInstance();
    	fromCal.add(Calendar.DAY_OF_MONTH,days);
		Document matchQuery = new Document();
		Document searchQuery = new Document();
    	MongoCollection<Document> table=mongodb.getCollection("v_"+siteId+MongoDBConstants.MONGO_COLLECTION_SUFFIX);
    	AggregateIterable<Document>  iterable=null;
    	AggregateIterable<Document>  paramLogiterable=null;
    	int year = fromCal.get(Calendar.YEAR);
		int monthNumber = fromCal.get(Calendar.MONTH);
		int dateNumber = fromCal.get(Calendar.DAY_OF_MONTH);
		monthNumber += 1;
		int toYear = toCal.get(Calendar.YEAR);
		int toMonthNumber = toCal.get(Calendar.MONTH);
		int toDateNumber = toCal.get(Calendar.DAY_OF_MONTH);
		toMonthNumber += 1;
		searchQuery.put(MongoDBConstants.DEVICE_ID, deviceId);
    	searchQuery.put("message_time",
				new BasicDBObject("$gte",
						new DateTime(year, monthNumber, dateNumber, 0, 0, DateTimeZone.UTC)
								.toDate()).append("$lte",
										new DateTime(toYear, toMonthNumber, toDateNumber, 23, 59,
												DateTimeZone.UTC).toDate()));
    	matchQuery.put("$match", searchQuery);
    	Document sortQueryObject=new Document();
    	Document sortQuery = new Document();
		sortQuery.put("message_time", MongoDBConstants.MIN_ORDER_BY);
		sortQueryObject.put("$sort",sortQuery);
    	iterable = table.aggregate(Arrays.asList(matchQuery,sortQueryObject,projectQuery));
    	String nowAsISO="";
    	JSONArray dataArray=new JSONArray();
    	JSONArray dataTempArray=new JSONArray();
    	JSONArray rmsArray=new JSONArray();
    	List<Double> lDoubleList=new ArrayList<Double>();
    	Document bottomObj=new Document();
		for (Document row : iterable) {
			nowAsISO = df.format(row.getDate("message_time"));
			row.put("yearMonthDay", nowAsISO.substring(0, nowAsISO.indexOf("T")));
			row.put("time",
					nowAsISO.substring(nowAsISO.indexOf("T") + 1, nowAsISO.length() - 1));
			row.put("doc_id", row.get("_id").toString());
			
			dataArray.add(row.get("rms_vel_z"));
			/**********************/
			rmsArray=new JSONArray();
			
			rmsArray.add(convertUTCToDifferentTimeZoneInMilliSeconds(nowAsISO, jsonDocument.getString("timezone")));
			//rmsArray.add(row.getDate("message_time").getTime());
			//rmsArray.add(nowAsISO);
			//rmsArray.add(row.get("_id").toString());
			rmsArray.add(row.get("rms_vel_z"));
			lDoubleList.add(row.getDouble("rms_vel_z"));
			dataTempArray.add(rmsArray);
			row.remove("rms_vel_z");
			row.remove("_id");
			row.remove("message_time");
			bottomObj=new Document();
			bottomObj.put("z_vel_fft_x_axis", row.get("z_vel_fft_x_axis"));
			bottomObj.put("z_vel_fft", row.get("z_vel_fft"));
			timeSeriesArray.add(row);
			
		}
		
		JSONArray bottomGraphArray=new JSONArray();
		rmsArray=new JSONArray();
		Document doc = new Document();
		doc.put("status", "Success");
		doc.put("statusCode", 0);
		doc.put("statusMessage", "getTimeSeriesData");
		doc.put("data", dataTempArray);
		JSONArray array=new JSONArray();
		List xAxisObj=(ArrayList) bottomObj.get("z_vel_fft_x_axis");
		List yAxisObj=(ArrayList) bottomObj.get("z_vel_fft");
		if(xAxisObj!=null && xAxisObj.size()>0){
			for(int iterate=0;iterate<xAxisObj.size();iterate++){
				rmsArray.add(xAxisObj.get(iterate));
				if(yAxisObj.get(iterate)!=null){
					rmsArray.add(yAxisObj.get(iterate));
				}else{
					rmsArray.add(0.0);
				}
				bottomGraphArray.add(rmsArray);
				rmsArray=new JSONArray();
			}
		}
		doc.put("bottomGraphArray", bottomGraphArray);
		doc.put("timeSeries", timeSeriesArray);
		Document searchQueryLog = new Document();
		Document matchQueryLog = new Document();
		Document projectQueryLog = new Document();
		paramLogTable = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_USER_DEFINED_PARAM_LOG);
		searchQueryLog.put("device_id",deviceId);
		searchQueryLog.put("active_flag", true);
		matchQueryLog.put("$match", searchQueryLog);
		projectFields=new Document();
		projectFields.put("alarm","$db_parameters.rms_vel_alarm");
		projectFields.put("alert","$db_parameters.rms_vel_alert");
		projectQueryLog.put("$project", projectFields);
		paramLogiterable = paramLogTable.aggregate(Arrays.asList(matchQueryLog, projectQueryLog));
		for (Document row : paramLogiterable) {
			if(row.get("alarm") instanceof String){
				lDoubleList.add(1.0);
				lDoubleList.add(2.0);
			}
			else if(row.get("alarm") instanceof Integer){
				lDoubleList.add(1.0);
				lDoubleList.add(2.0);
			}
			else{
				lDoubleList.add(row.getDouble("alarm"));
				
				
				
			if(row.get("alert") instanceof Integer){
				    Integer alert = (Integer) row.get("alert");
				    Double alertVal = new Double((double) alert);
				    lDoubleList.add((Double) alertVal);
		    }else{
		    	
			  lDoubleList.add(row.getDouble("alert"));
			}
				

				
			}
			doc.put("plotlineconfig",row);
		}
		Double yAxisMaxForVibration=calculateYAxisMaxPointsForVibrationChart(lDoubleList,templateObject);
		doc.put("yAxisMaxForVibration", yAxisMaxForVibration);
		return doc;
    }
    /******************getTimeSeriesData *************/
    
    private static Map<Integer,String>  getStatusDescription(){
    	Map<Integer,String> lStatusDescription=new HashMap<Integer,String>();
    	lStatusDescription.put(0, "Ok");
    	lStatusDescription.put(-1, "unknown");
    	lStatusDescription.put(1, "Alert");
    	lStatusDescription.put(2, "Alarm");
    	return lStatusDescription;
    }
    
    
    private static Map<String,Integer>  getStatusCode(){
    	Map<String,Integer> lStatusDescription=new HashMap<String,Integer>();
    	lStatusDescription.put("Ok",0);
    	lStatusDescription.put("unknown",-1);
    	lStatusDescription.put("Alert",1);
    	lStatusDescription.put("Alarm",2);
    	return lStatusDescription;
    }
    /**
     * 
     * @param lDoubleList
     * @return
     */
    private static Double calculateYAxisMaxPointsForVibrationChart(List<Double> lDoubleList,Document templateObject){
    	if(lDoubleList!=null && lDoubleList.size()>0){
	    	double currentMax = 0.0;
	    	for(int dListIterate=0;dListIterate<lDoubleList.size();dListIterate++){
	    		if(lDoubleList.get(dListIterate)!=null) {
					if(currentMax < lDoubleList.get(dListIterate)){
						currentMax = lDoubleList.get(dListIterate);
					}
	    		}
			}
	    	return currentMax=currentMax+templateObject.getDouble("max_axis");
    	}
    	return 1.0;
    }
  /**
   * 
   * @param type
   * @param locationHierarchyObject
   * @param statusArray
   * @param pdfReportConfigObject
 * @return 
   */
    public static Document generatePieChart(String type,Document locationHierarchyObject,Document reportConfigObject ){
    	
    	Map<Integer,Integer> lMap=new HashMap<Integer, Integer>();
    	int count=0;
    	if(type!=null && type.equalsIgnoreCase("unit")){
    		List<Document> listDocument=(List<Document>) locationHierarchyObject.get("unit");
    		for(Document doc:listDocument){
    			if(doc.get("status")==null){
    				if(lMap.get(-1)!=null){
    					count=lMap.get(-1)+1;
    					lMap.put(-1, count);
    				}else{
    					lMap.put(-1, 1);	
    			    }
    			}
    			else  if(lMap.get(doc.get("status"))!=null){
    				count=lMap.get(doc.getInteger("status"))+1;
    				lMap.put(doc.getInteger("status"), count);
    			}else{
    				lMap.put(doc.getInteger("status"), 1);	
    			}
    		}
    	}
    	
    	if(type!=null && type.equalsIgnoreCase("asset")){
    		List<Document> listDocument=(List<Document>) locationHierarchyObject.get("unit");
    		for(Document unitDoc:listDocument){
    			List<Document> assetListDoc=(List<Document>) unitDoc.get("asset");
    			for(Document assetDoc:assetListDoc){
    				if(assetDoc.get("status")==null){
        				if(lMap.get(-1)!=null){
        					count=lMap.get(-1)+1;
        					lMap.put(-1, count);
        				}else{
        					lMap.put(-1, 1);	
        			    }
        			}
        			else if(lMap.get(assetDoc.get("status"))!=null){
        				count=lMap.get(assetDoc.getInteger("status"))+1;
        				lMap.put(assetDoc.getInteger("status"), count);
        			}else{
        				lMap.put(assetDoc.getInteger("status"), 1);	
        			}
    			}
    		}
    	}
    	if(type!=null && type.equalsIgnoreCase("device")){
    		List<Document> listDocument=(List<Document>) locationHierarchyObject.get("unit");
    		for(Document unitDoc:listDocument){
    			List<Document> assetListDoc=(List<Document>) unitDoc.get("asset");
    			for(Document assetDoc:assetListDoc){
    				List<Document> deviceCategoryListDoc=(List<Document>) assetDoc.get("device_category");
    				for(Document deviceCategoryDoc:deviceCategoryListDoc){
    					List<Document> deviceListDoc=(List<Document>) deviceCategoryDoc.get("device");
    					for(Document deviceDoc:deviceListDoc){
    						if(deviceDoc.get("status")==null){
    	        				if(lMap.get(-1)!=null){
    	        					count=lMap.get(-1)+1;
    	        					lMap.put(-1, count);
    	        				}else{
    	        					lMap.put(-1, 1);	
    	        			    }
    	        			}
    	        			else if(lMap.get(deviceDoc.get("status"))!=null){
    	        				count=lMap.get(deviceDoc.getInteger("status"))+1;
    	        				lMap.put(deviceDoc.getInteger("status"), count);
    	        			}else{
    	        				lMap.put(deviceDoc.getInteger("status"), 1);	
    	        			}
    					}
    				}
    			}
    		}
    	}
    	
    	JSONArray newPieDataList=new JSONArray();
    	Document pieChart=(Document)reportConfigObject.get("pie_chart");
		 if(pieChart!=null){
			 Document infile=(Document)pieChart.get("infile");
			 List<Document> seriesList=(List<Document>)infile.get("series");
			 for(Document seriesListDoc: seriesList){
				 List<Document> dataList=(List<Document>)seriesListDoc.get("data");
				 for(Document dataDoc: dataList){
					 if(lMap.get(lStatusCode.get(dataDoc.get("name")))!=null){
						 dataDoc.put("y", lMap.get(lStatusCode.get(dataDoc.get("name"))));
						 dataDoc.put("name", dataDoc.get("y"));
						 newPieDataList.add(dataDoc);					 
					}
					 
				 }
				 if(newPieDataList.size()>0){
					 seriesListDoc.put("data", newPieDataList);
					 newPieDataList=new JSONArray();
				 }
			 }
		 }
		 return pieChart;
    }
    
    /**
     * 
     * @param outputPath
     */
    public static void deleteFolder(String outputPath){
    	try {
			FileUtils.deleteDirectory(new File(outputPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    /**
     * 
     * @param currentDateTime
     * @param finalTimeZone
     * @return
     */
    private static String convertDateTimeToDifferentTimeZone(String currentDateTime, String finalTimeZone) {
		Date dateTime;
		String getCurrentTimeZoneOffset = "";
		Boolean isFinalTimeZoneValid = false;
		try {
			dateTime = currentDateTimeFormat.parse(currentDateTime);
			convertCurrentDateTimeToUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
			if (finalTimeZone.contains("DT")) {
				getCurrentTimeZoneOffset = getOffsetValuesForDaylightSavingsTime(finalTimeZone);
				if (!getCurrentTimeZoneOffset.equalsIgnoreCase("N/A")) {
					convertUtcToFinalDateTime.setTimeZone(TimeZone.getTimeZone("GMT" + getCurrentTimeZoneOffset));
					isFinalTimeZoneValid = true;
				} else {
					isFinalTimeZoneValid = false;
				}
			} else {
				convertUtcToFinalDateTime.setTimeZone(TimeZone.getTimeZone(finalTimeZone));
				isFinalTimeZoneValid = true;
			}
			if (isFinalTimeZoneValid) {
				return convertUtcToFinalDateTime.format(dateTime)+" "+finalTimeZone;
			} else {
				return "Cannot Convert to TimeZone";
			}
		} catch (java.text.ParseException e) {
			e.printStackTrace();
			return "Error Parsing Date " + e.getMessage();
		}

	}

	private static String getOffsetValuesForDaylightSavingsTime(String finalTimeZone) {
		String getFinalTimeZoneOffset = "";
		if (finalTimeZone.equalsIgnoreCase("EDT")) {
			getFinalTimeZoneOffset = "-04:00";
		} else if (finalTimeZone.equalsIgnoreCase("CDT")) {
			getFinalTimeZoneOffset = "-05:00";
		} else if (finalTimeZone.equalsIgnoreCase("MDT")) {
			getFinalTimeZoneOffset = "-06:00";
		} else if (finalTimeZone.equalsIgnoreCase("PDT")) {
			getFinalTimeZoneOffset = "-07:00";
		} else {
			getFinalTimeZoneOffset = "N/A";
		}
		return getFinalTimeZoneOffset;
	}
    /**
     * 
     * @param currentDateTime
     * @param finalTimeZone
     * @return
     */
	public static Long convertUTCToDifferentTimeZoneInMilliSeconds(String currentDateTime, String finalTimeZone) {
		DateFormat standardizeISOToUTC = new SimpleDateFormat("MM/dd/yyyy hh:mm a z");
		DateFormat convertUtcTimeToFinalDateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");	
		String getTimeZoneFromCurrentDateTime = "";
		DateTime dateTime;		
		String getCurrentTimeZoneOffset = "";
		Boolean isFinalTimeZoneValid = false;
		long dateTimeInMilliSeconds = 0L;	
		long offsetValue=0L;
		String dateTimeAfterTimeZoneAdj="";
		org.joda.time.format.DateTimeFormatter isoDateTimeParser = ISODateTimeFormat.dateTimeNoMillis();
		try {
			Calendar cal = javax.xml.bind.DatatypeConverter.parseDateTime(currentDateTime);
			getTimeZoneFromCurrentDateTime = cal.getTimeZone().getDisplayName();
			dateTime = isoDateTimeParser.parseDateTime(currentDateTime);
			standardizeISOToUTC.setTimeZone(TimeZone.getTimeZone(getTimeZoneFromCurrentDateTime));
			standardizeISOToUTC.format(dateTime.toDate());		
		//	System.out.println(
					//"Input DateTime in standardized format..." + standardizeISOToUTC.format(dateTime.toDate()));
			if (finalTimeZone.contains("DT")) {
				getCurrentTimeZoneOffset = getOffsetValuesForDaylightSavingsTime(finalTimeZone);
				if (!getCurrentTimeZoneOffset.equalsIgnoreCase("N/A")) {
					convertUtcTimeToFinalDateTime.setTimeZone(TimeZone.getTimeZone("GMT" + getCurrentTimeZoneOffset));
					isFinalTimeZoneValid = true;
					offsetValue = TimeZone.getTimeZone("GMT" + getCurrentTimeZoneOffset).getRawOffset();
				} else {
					System.out.println("Invalid Time Zone!");
					isFinalTimeZoneValid = false;
				}
			} else {
				convertUtcTimeToFinalDateTime.setTimeZone(TimeZone.getTimeZone(finalTimeZone));		
				offsetValue = TimeZone.getTimeZone(finalTimeZone).getRawOffset();
				isFinalTimeZoneValid = true;
				System.out.println("welcome");
			}
			if (isFinalTimeZoneValid) {
				dateTimeAfterTimeZoneAdj = convertUtcTimeToFinalDateTime.format(dateTime.toDate());
				DateTimeZone zone = DateTimeZone.forID(DateTimeZone.forOffsetMillis(convertUtcTimeToFinalDateTime.getTimeZone().getRawOffset())+"");
				String input = dateTimeAfterTimeZoneAdj.replace( "/", "-" ).replace( " ", "T" );
				DateTime dateTime1 = new DateTime(input, zone);
				//System.out.println(offsetValue);
				dateTimeInMilliSeconds = dateTime1.getMillis();
			} else {
				dateTimeInMilliSeconds = 0L;
			}
		} catch (Exception e) {
			e.printStackTrace();
			dateTimeInMilliSeconds = 0L;
		}

		return dateTimeInMilliSeconds;

	}
	 /**
	    * 
	    * @param htmlFileName
	    * @param outputpdfFileName
	    * @return
	    */
	   public static boolean convertHtmlToPDF(StringBuffer htmlContents,String outputpdfFileName,String path){
		   System.out.println("convertHtmlToPDF ....called"+htmlContents);
		   try{
				 	/*CYaHPConverter converter = new CYaHPConverter();
				    File fout = new File(outputpdfFileName);
				    FileOutputStream out = new FileOutputStream(fout);
				    //String htmlContents = new String(Files.readAllBytes(Paths.get(templateName)), StandardCharsets.UTF_8);
				    Map<String,String> properties = new HashMap<String,String>();
				    List headerFooterList = new ArrayList();
				    properties.put(IHtmlToPdfTransformer.PDF_RENDERER_CLASS,
				                   IHtmlToPdfTransformer.FLYINGSAUCER_PDF_RENDERER);
				    converter.convertToPdf(htmlContents.toString(),
				                IHtmlToPdfTransformer.A4P,
				                headerFooterList,
				                "file:///opt",
				                out,
				                properties);
				    
				    out.flush();
				    out.close();*/
				   FileOutputStream fos =new FileOutputStream(new File(path+"generatedOutput.html"));
				   fos.write(htmlContents.toString().getBytes());
				   fos.close();
			       CHtmlToPdfFlyingSaucerTransformer converter=new CHtmlToPdfFlyingSaucerTransformer();
					File fout = new File(outputpdfFileName);
				    FileOutputStream out = new FileOutputStream(fout);
				    Map properties = new HashMap();
				    List headerFooterList = new ArrayList();
				    
				    properties.put(IHtmlToPdfTransformer.PDF_RENDERER_CLASS,
				                   IHtmlToPdfTransformer.FLYINGSAUCER_PDF_RENDERER);
				    
				    //properties.put(IHtmlToPdfTransformer.FOP_TTF_FONT_PATH, "/usr/share/fonts/stix");
				    
			        InputStream inputStream=new FileInputStream(new File(path+"generatedOutput.html"));	
				    converter.transform(inputStream, "file:///temp", IHtmlToPdfTransformer.A4L, headerFooterList, properties,out);
				    out.flush();
				    inputStream.close();
				    out.close();
				    System.out.println("convertHtmlToPDF ....done");
					  
				    return true;
			   

			}catch(Exception e){
				e.printStackTrace();
			}
		   
		   return false;
	   }
}
	
	
