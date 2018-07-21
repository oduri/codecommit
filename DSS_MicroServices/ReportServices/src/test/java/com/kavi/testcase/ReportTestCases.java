package com.kavi.testcase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;

import com.kavi.common.utility.CommonUtility;
import com.kavi.common.utility.JSonUtility;
import com.kavi.report.dataobjects.PointsDO;
import com.kavi.report.utility.ReportUtility;
import com.kavi.services.controller.ReportController;
import com.mongo.db.MongoDBConnection;
import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;


public class ReportTestCases {
	
	private final static Logger logger = Logger.getLogger(ReportTestCases.class);
	public static void main(String args[]) 
	{
		try {
			
			//getChannelMapping("AssetMcems_1","970536179","FHR"); 
			//compareDeviceParameters("5af5a657590801027e2a86fe", "Corrosion");
			//compareDeviceParametersForDevice("5af5a657590801027e2a86fe","Corrosion");
			//compareDeviceParametersForChannel("5af5a657590801027e2a86fe","Corrosion");
			//compareDeviceParametersForDeviceAndChannel("5af5a657590801027e2a86fe","Corrosion");
		 	// getTimeSeriesData("BLF71859DR7AUUJU1992M61AUA");
		 	getTimeSeriesDataForGas("BLF71859DR7AUUJU1992M61AUA");
			//getTimeSeriesDataForTemperature("5b100a9d5f1503000657cb40");
			//groupByTestCase("5ad8ad0ef6e86a7d0a23814d");
			//getTimeSeriesDataForCorrosion("G1Q6F8VN4JMPHCJMS6BTG2EPTQ");  
			//getTimeSeriesDataForCorrosion("5b311fc8cff47e0008a0f162"); 
			//getTimeSeriesDataForCorrosionBottomChart("5b2807fed60180000654f7ff");
			//getTimeSeriesDataForVibration("5b2807fed60180000654f7ff"); 
			//getTimeSeriesDataForOldCorrosion("5ad8ad0ef6e86a7d0a23814d");
			//viewComments("5ad8ad0ef6e86a7d0a23814d");
			 //viewDeviceParameters("5ad8ad0ef6e86a7d0a23814d");
			//generateUtilizationForSite("5af5a657590801027e2a86fe");
			//updateComments("5af5efd552faff027dc8eb20");
			//testMethod("Corrosion");
			//getDeviceStatusLog("T7L1AN6AS739JI6UBBURO4HLNC");
		 	//updateCheckForNestedParameters();
			//testUpsertUserConfigParameters("R9SQC76939GBIS892QGTDB6N7B","Corrosion");
			//getLatestTimeStamp("5b02a1814cedfd027a0c1c59");
			//testPointsArraySort();
			//getDeviceParameters("5b0ee05c5f1503000657bf8c");
			//testGasStaging();
			//zeroGroup();
		} catch (Exception e) {
			e.printStackTrace();

		}
		
	}
	
	private static void zeroGroup() {
		
		Document searchQuery=new Document();
		searchQuery.put("active_status", "Yes");
		Document matchQuery = new Document();
		matchQuery.put("$match", searchQuery);	
		
		
		Document zeroGroup = new Document("$group",
			    new Document("_id",
			        new Document("plant_id", "$plant_id")
			        .append("site_id", "$site_id")
			        .append("unit_id", "$unit_id").append("asset_id","$asset_id")
			      	)
			    .append("max", new Document("$max", "$device_status")));
		System.out.println("zeroGroup...."+zeroGroup.toJson());
		System.out.println("matchQuery...."+matchQuery.toJson());
	}
	
	private static void testPointsArraySort() {
		PointsDO gen1 = new PointsDO(2345, 9876);
		PointsDO gen2 = new PointsDO(1234, 7654);
		PointsDO gen3 = new PointsDO(3456, 9999);
		PointsDO gen4 = new PointsDO(1234, 7777);
		List<PointsDO> genList = new ArrayList<PointsDO>();
		genList.add(gen1);
		genList.add(gen2);
		genList.add(gen3);
		genList.add(gen4);
		for(PointsDO gen: genList) {
			System.out.println("X: "+gen.getX()+", "+"Y: "+gen.getY());
		}
		Collections.sort(genList,PointsDO.PointsDOComparator);
		System.out.println("Array after sorting");
		for(PointsDO gen: genList) {
			System.out.println("X: "+gen.getX()+", "+"Y: "+gen.getY());
		}
	}
	/**
	 * 
	 * @param sessionkey
	 * @param deviceCategory	 
	 * 
	 */
	
	private static void getLatestTimeStamp(String sessionkey) {
		Document dbobject=new Document();
		JSONArray array=new JSONArray();
		array.add("67241222");
		array.add("67241221");
		dbobject.put("device_id",array);
		dbobject.put("site_id","buffalo");
		dbobject.put("asset_id","asset_2272598");
		dbobject.put("deviceCategoryName","Gas");
		System.out.println("dbobject..."+dbobject.toJson());
		ReportController controller = new ReportController();
		ResponseEntity<String> response =controller.getLatestTimeStamp(sessionkey, dbobject.toJson(),null);
		System.out.println("Response Body: "+response.getBody());
	}
	private static void testUpsertUserConfigParameters(String sessionkey, String deviceCategory) {
		//String deviceJson = "{\"deviceJsonDoc\":[{\"device_id\":\"970536179\",\"device_parameters.gps_longitude\":3.5}]}";
		//String channelJson = "{\"channelJsonDoc\":[{\"device_id\":\"970536179\",\"channel_parameters.channel_1.part_material_velocity\":14.0},{\"device_id\":\"970536179\",\"channel_parameters.channel_2.part_material_velocity\":14.0}]}";
		
		String deviceJson="{}";
		String channelJson="{\"channelJsonDoc\":[{\"channel_3.temperature_coefficient\":1,\"device_id\":\"970536179\",\"asset_id\":\"AssetMcems_1\",\"channel_3.allowable_minimum_thickness\":0.25,\"channel_3.initial_thickness\":0.928,\"channel_3.gate_a_start\":50,\"channel_3.gate_a_length\":10,\"channel_3.gate_a_height\":90,\"channel_3.gate_b_start\":84.1,\"channel_3.gate_b_length\":6,\"channel_3.gate_b_height\":90,\"channel_3.thickness_algorithm\":\"Max Peak\",\"channel_3.channel_name\":\"970536179 3\",\"channel_3.max_y_axis\":null,\"channel_3.thickness\":0,\"channel_3.corrosion_rate_st\":0,\"channel_3.corrosion_rate_lt\":0}]}";
		//String deviceJson = "{\"deviceJsonDoc\":[{\"device_id\":\"970536179\",\"part_material_type\":\"Test_25\"}]}";
		//String channelJson = "{\"channelJsonDoc\":[{\"device_id\":\"970536179\",\"channel_1.part_material_velocity\":23.0},{\"device_id\":\"970536179\",\"channel_2.part_material_velocity\":23.0}]}";
		ReportController controller = new ReportController();
		ResponseEntity<String> response = controller.upsertUserConfigParameters(sessionkey, deviceCategory, deviceJson, channelJson,null);
		System.out.println("Response Body: "+response.getBody());
	}
	
	/**
	 * 
	 */
	private static void testGasStaging() {
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		try{
			
			mongoSingle = new MongoDBConnection();
			mongodb = mongoSingle.getMongoDB("FHR_DSS");
			table=mongodb.getCollection("g_staging");
			Document searchQuery=new Document();
			searchQuery.put("db_insert_time",
					new BasicDBObject("$gte",new DateTime(2018, 05, 21, 17, 34, DateTimeZone.UTC).toDate()).append("$lte",
											new DateTime(2018, 05, 25, 23, 59,
													DateTimeZone.UTC).toDate()));
			
			String[] listKey= {"31","32","39","3A","41","42","49","4A","51","52","59","5A","61","62","69","6A"};
			//String[] listKey= {"31"};
			boolean applyUnwind=true;
			boolean flag=false;
			String tempkey="";
			for(String key:listKey) {
				tempkey="0x"+key;
				key="data."+tempkey;
				System.out.println("scanning started for "+key);
				System.out.println("scanning started for "+tempkey);
				flag=false;
				Document searchEmptyQuery=new Document();
				searchEmptyQuery.put("$exists", true);
				searchEmptyQuery.put("$eq", "");
				
				Document matchQuery=new Document();
				matchQuery.put("$match",searchQuery);
				BasicDBObject unwindData = new BasicDBObject("$unwind", "$"+key);
				
				Document dataQuery=new Document();
				Document applySearchEmptyQuery=new Document();
				applySearchEmptyQuery.put(key,searchEmptyQuery);
				dataQuery.put("$match",applySearchEmptyQuery);
				AggregateIterable<Document> iterable=null;
				if(applyUnwind) {
					iterable=table.aggregate(Arrays.asList(matchQuery, unwindData,dataQuery));
				}else {
					iterable=table.aggregate(Arrays.asList(matchQuery,dataQuery));
				}
				
				for(Document row:iterable) {
					//System.out.println("row..."+row.toJson());
					Document data=(Document) row.get("data");
					if(data.get(tempkey) instanceof String) {
						System.out.println("row..."+row.toJson());
						flag=true;
						break;
					}
					
				}
				System.out.println("scanning ended for "+key+" and empty string found "+flag);
			}
			
		}catch(Exception e){
			e.printStackTrace();
			
		}finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
	}
	
	/**
	 * 
	 */
	private static void updateCheckForNestedParameters() {
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		try{
			
			mongoSingle = new MongoDBConnection();
			mongodb = mongoSingle.getMongoDB("FHR_DSS");
			table=mongodb.getCollection("test");
			Document updateQuery = new Document("_id", new ObjectId("5ae8c4ea0d7e4ee766c29f4c"));
			BasicDBObject updateData = new BasicDBObject();
			BasicDBObject command = new BasicDBObject();
			updateData.put("channel_parameters.channel_1.thickness_algorithm","abcedf");
			command.put("$set", updateData);
			table.updateOne(updateQuery, command);
			
		}catch(Exception e){
			e.printStackTrace();
			
		}finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
	}
	
	/**
	 * 
	 * 
	 */
	
	public static List<Document> testMethod(String deviceCategoryName){
		
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		try{
			
			mongoSingle = new MongoDBConnection();
			mongodb = mongoSingle.getMongoDB("FHR_DSS");
			table=mongodb.getCollection("user_defined_parameter_log");
			BasicDBObject dbobject=new BasicDBObject ();
			dbobject.put("asset_id","AssetMcems_1");
			JSONArray inArray=new JSONArray();
			inArray.add("970536179");
			dbobject.put("device_id",inArray);
			dbobject.put("site_id","LisleMolex");
			dbobject.put("from_date","04/23/2018");	
			dbobject.put("to_date","04/26/2018");
			dbobject.put("type","All");
			dbobject.put("sub_type","All");
			dbobject.put("deviceCategoryName","Corrosion");		
			Document finalDoc=ReportUtility.getUIParametersForCorrosion(table, deviceCategoryName, dbobject,"970536179_1");
			System.out.println("final..."+finalDoc.toJson());
		}catch(Exception e){
			
			
		}finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
		return null;
	}
	
	/**
	 * 
	 * 
	 */
	
	private static void generateUtilizationForSite(String sessionkey) {
		BasicDBObject jsonDocument=new BasicDBObject();
		jsonDocument.put("site_id", "LisleMolex");
		jsonDocument.put("from_date", "04/01/2018");
		jsonDocument.put("to_date", "05/31/2018");
		jsonDocument.put("period", "overallnew"); //overall or month or day
		JSONArray array=new JSONArray();
		//array.add("Vibration");This field is optional
		array.add("InletCompressor_3170");
		//jsonDocument.put("device_category", array);
		jsonDocument.put("asset_id", array); //This field is optional
		ReportController controller=new ReportController();
		System.out.println("request...." + jsonDocument);
		ResponseEntity<String> response =controller.generateUtilizationForSite(sessionkey, jsonDocument.toJson(),null);
		System.out.println("response..."+response.getStatusCode());
		System.out.println("response..."+response.getBody());
	}
	
	private static void getDeviceStatusLog(String sessionkey) {
		ReportController controller=new ReportController();
		BasicDBObject jsonDocument=new BasicDBObject();
		//JSONObject jsonData=JSonUtility.getJSONObject("c:\\users\\sarav\\devicestatuslog.json");
		String json = "{\"filter_condition\":[{\"column\":\"plant_id\",\"operator\":\"equals\",\"type\":\"string\",\"value1\":\"Refinery\"},{\"column\":\"site_id\",\"operator\":\"in\",\"type\":\"string\",\"value1\":[\"LisleMolex\"]},{\"column\":\"unit_id\",\"operator\":\"in\",\"type\":\"string\",\"value1\":[\"Lab\",\"Power Station\"]},{\"column\":\"device_category\",\"operator\":\"in\",\"type\":\"string\",\"value1\":[\"Vibration\"]},{\"column\":\"active_status\",\"operator\":\"equal\",\"type\":\"string\",\"value1\":\"Yes\"}]}";
		String dbParamJson = "{\"organizationName\":\"FHR\",\"userRole\":\"Administrator\",\"db_name\":\"FHR_DSS\",\"has_site_access\":true,\"display_name\":null,\"selected\":true}";
		ResponseEntity<String> response =controller.getDeviceStatusLog(sessionkey, jsonDocument.toJson(), json,"FHR","PST",dbParamJson);
		System.out.println("response..."+response.getStatusCode());
		System.out.println("response..."+response.getBody());
	}
	private static void viewComments(String sessionkey) {
		
		ReportController report=new ReportController();
		Document dbobject=new Document();
		dbobject.put("device_id","970536179");
		dbobject.put("site_id","LisleMolex");
		dbobject.put("year_month_day","2018-04-27");
		dbobject.put("time","05:30:01");
		dbobject.put("document_id","5af0c33c6224bc6addf050a0");
		dbobject.put("type","thickness_cmnts");
		JSONArray array=new JSONArray();
		array.add("970536179_1");
		dbobject.put("channels",array);

		ResponseEntity<String> response = report.viewComments(sessionkey, dbobject.toJson(),"Corrosion",null);
		System.out.println("response..."+response.getStatusCode());
		System.out.println("response..."+response.getBody());
	}
	/**
	 * 
	 * @param assetId
	 * @param deviceId
	 * @param companyId
	 * @return
	 */
	
	private static void updateComments(String sessionkey){
		ReportController report=new ReportController();
		try {		
			Document dbobject=new Document();		
			
			dbobject.put("device_id","970470347");
			dbobject.put("document_id","5af1b696c0681a2272d8b85a");			
			dbobject.put("deviceCategoryName","Vibration");
			//dbobject.put("type","device_configuration");
			dbobject.put("type","rms_vel_z_cmnts");
			dbobject.put("cmnts","HELLO SARAV");
			//dbobject.put("parameterType","parameterType");
			dbobject.put("site_id","LisleMolex");
			
			/*dbobject.put("document_id","58dad11b05f6f82ab836d049");
			dbobject.put("year_month_day","2017-03-30");
			dbobject.put("time","08:59:00");
			dbobject.put("x","x");*/
			
		
			
			ResponseEntity<String> response = report.updateComments(sessionkey, dbobject.toJson(),"Vibration",null);
			System.out.println("response..."+response.getStatusCode());
			System.out.println("response..."+response.getBody());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/*
	 * 
	 */
	
	public static void getDeviceParameters(String sessionkey) {
		ReportController report=new ReportController();
		try {		
			Document dbobject=new Document();		
			
			dbobject.put("device_id","DL0100");
			dbobject.put("document_id","5b0484550cd7c722d0369339");
			dbobject.put("year_month_day","2018-05-22");
			dbobject.put("time","13:00:43:00");
			dbobject.put("deviceCategoryName","Corrosion");
			//dbobject.put("type","device_configuration");
			dbobject.put("type","all");
			//dbobject.put("parameterType","parameterType");
			dbobject.put("site_id","pinebend");
			
			/*dbobject.put("document_id","58dad11b05f6f82ab836d049");
			dbobject.put("year_month_day","2017-03-30");
			dbobject.put("time","08:59:00");
			dbobject.put("x","x");*/
			
		
			
			ResponseEntity<String> response = report.getDeviceParameters(sessionkey, dbobject.toJson(),null);
			System.out.println("response..."+response.getStatusCode());
			System.out.println("response..."+response.getBody());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    private static List<Document> groupByTestCase(String sessionkey){
		
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		try{
			
			mongoSingle = new MongoDBConnection();
			mongodb = mongoSingle.getMongoDB("FHR_DSS");
			table=mongodb.getCollection("gas_messages_temp");
			Document pushDocument=new Document();
			pushDocument.put("channel_id", "$channel_id");
			pushDocument.put("gas_codes", "$gas");
			pushDocument.put("gas_full_scale_multiplier", "$gas_full_scale_multiplier");
			pushDocument.put("gas_sensor_status_flags", "$gas_sensor_status_flags");
			pushDocument.put("doc_id", "$oid");
			pushDocument.put("comment", "$comment");
			
			Document groupByQuery=new Document();
			groupByQuery.put("asset_id", "$asset_id");
			groupByQuery.put("device_id", "$device_id");
			groupByQuery.put("message_time", "$message_time");
			
			Document projectQuery=new Document();
			Document project=new Document();
			project.put("asset_id", "$_id.asset_id");
			project.put("device_id", "$_id.device_id");
			//project.put("message_time", "$_id.message_time");
			
			Document dateConversion=new Document();
			Document dateFormatDoc=new Document();
			dateFormatDoc.put("format", "%Y-%m-%d");
			dateFormatDoc.put("date", "$_id.message_time");
			dateConversion.put("$dateToString",dateFormatDoc);
			project.put("yearMonthDay",dateConversion);
			
			Document timeConversion=new Document();
			Document timeConversionDoc=new Document();
			timeConversionDoc.put("format", "%H:%M:%S");
			timeConversionDoc.put("date", "$_id.message_time");
			timeConversion.put("$dateToString",timeConversionDoc);
			project.put("time",timeConversion);
			project.put("channels", "$channels");
			project.put("_id", 0);
			projectQuery.put("$project", project);
			
			Document group = new Document("$group",
				    new Document("_id",groupByQuery
				      	)
				    .append("channels", new Document("$push", pushDocument)));
			
			AggregateIterable<Document> iterable=table.aggregate(Arrays.asList(group,projectQuery));
			List<Document> timeSeriesArray=new ArrayList<Document>();
			for(Document row:iterable) {
				timeSeriesArray.add(row);
			}
			
		}catch(Exception e){
			
			
		}finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
		return null;
	}
	/**
	 * 
	 * @param assetId
	 * @param deviceId
	 * @return
	 */
	public static List<Document> getChannelMapping(String assetId,String deviceId,String companyId){
		
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		try{
			
			mongoSingle = new MongoDBConnection();
			mongodb = mongoSingle.getMongoDB("FHR_DSS");
			table=mongodb.getCollection("location_hierarchy");
			List<String> lDeviceId=new ArrayList<String>();
			lDeviceId.add(deviceId);
			Document finalDoc=ReportUtility.getChannelMapping(assetId, lDeviceId, table,companyId);
			System.out.println("final..."+finalDoc.toJson());
		}catch(Exception e){
			
			
		}finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
		return null;
	}
	/**
	 * 
	 * @param sessionkey
	 * @param deviceCategoryName
	 */
	private static void compareDeviceParameters(String sessionkey,String deviceCategoryName) {
		ReportController report=new ReportController();
		Document dbobject=new Document();
		JSONArray inArray=new JSONArray();
		inArray.add("AssetMcems_1");
		dbobject.put("compare_asset_list",inArray);
		inArray=new JSONArray();
		inArray.add("970536179_1");
		inArray.add("970536179_2");
		inArray.add("970536179_3");
		dbobject.put("compare_channel_list",inArray);
		dbobject.put("channel_id",1);
		dbobject.put("channel_name",1);
		
		Document deviceObject=new Document();
		Document channelObject=new Document();
		//String deviceJson = "{\"compare_asset_list\":[\"AssetMcems_1\"],\"compare_device_list\":[\"970536179\"],\"device_id\":1,\"device_name\":1,\"initial_thickness_date_time\":1}";
		//String channelJson = "{}";
		//String str="{\"compare_asset_list\":[\"AssetMcems_1\"],\"compare_device_list\":[\"970536179\"],\"device_id\":1,\"device_name\":1,\"ip_address\":1,\"sw_version\":1,\"hardware_id\":1,\"part_material_velocity\":1}";
		String devicejson="{\"compare_asset_list\":[\"AssetMcems_1\"],\"compare_device_list\":[\"970536179\"],\"device_id\":1,\"device_name\":1,\"location_tag_number\":1}";
		String channeljson="{\"compare_asset_list\":[\"AssetMcems_1\"],\"compare_channel_list\":[\"970536179_1\",\"970536179_2\",\"970536179_3\",\"970536179_4\"],\"channel_id\":1,\"channel_name\":1,\"tml_id\":1}";
		ResponseEntity<String> response= report.compareDeviceParameters(sessionkey, devicejson, channeljson,deviceCategoryName,null);
		System.out.println("response..."+response.getStatusCode());
		System.out.println("response..."+response.getBody());
	}
	
	
	/**
	 * @param sessionkey
	 */
	
	public static void viewDeviceParameters(String sessionkey) {
		ReportController report=new ReportController();
		
		try {
						
			Document dbobject=new Document();
			dbobject.put("deviceCategoryName","Gas");			
			
			ResponseEntity<String> response= report.viewDeviceParameters(sessionkey, dbobject.toJson(),null);
			System.out.println("response..."+response.getStatusCode());
			System.out.println("response..."+response.getBody());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 
	 * @param sessionkey
	 */
	private static void getTimeSeriesDataForCorrosion(String sessionkey) {
		ReportController report=new ReportController();
		try {
			
			//Top graph Temperature
			Document dbobject=new Document();
			dbobject.put("asset_id","AssetMcems_1");
			//JSONArray inArray=new JSONArray();
			//inArray.add("970536179");
			//inArray.add("test_device");
			dbobject.put("device_id","970530941");
			//dbobject.put("device_id","99745874");
			//dbobject.put("document_id","5af0bad86224bc5c61bc2871");
			dbobject.put("site_id","LisleMolex");
			dbobject.put("from_date","06/10/2018");	
			dbobject.put("to_date","06/21/2018");
			dbobject.put("type","All");
			dbobject.put("sub_type","All");
			dbobject.put("deviceCategoryName","Corrosion");			
			
			dbobject.put("channels","970530941_3");
			//dbobject.put("channels","970536179_4");
			//dbobject.put("document_id", "5af079c56224bc50ea188072");
			String json="{\"from_date\":\"04/26/2018\",\"to_date\":\"06/26/2018\",\"type\":\"All\",\"sub_type\":\"All\",\"site_id\":\"LisleMolex\",\"deviceCategoryName\":\"Corrosion\",\"device_id\":\"970536179\",\"channels\":\"970536179_1\",\"asset_id\":\"AssetMcems_1\"}";
			String dbParamJson="{\"organizationName\":\"FHR\",\"userRole\":\"Administrator\",\"db_name\":\"FHR_DSS\",\"has_site_access\":true,\"display_name\":null,\"selected\":true}";
			
			ResponseEntity<String> response = report.getTimeSeriesData(sessionkey, json,null,dbParamJson);
			System.out.println("response..."+response.getStatusCode());
			System.out.println("response..."+response.getBody());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param sessionkey
	 */
	private static void getTimeSeriesDataForCorrosionBottomChart(String sessionkey) {
		ReportController report=new ReportController();
		try {
			
			//Top graph Temperature
			/*Document dbobject=new Document();
			dbobject.put("document_id","5aff343b0cd7c71c18efe347");
			dbobject.put("channel_id","970536179_1");
			dbobject.put("deviceCategoryName","Corrosion");
			dbobject.put("site_id","LisleMolex");*/
			String json = "{\"device_id\":\"970536179\",\"document_id\":\"5aff343b0cd7c71c18efe347\",\"year_month_day\":\"2018-05-18\",\"time\":\"19:00:01:00\",\"site_id\":\"LisleMolex\",\"deviceCategoryName\":\"Corrosion\",\"thickness\":\"thickness\",\"channel_id\":\"970536179_1\"}";
			ResponseEntity<String> response = report.getTimeSeriesData(sessionkey, json,null,null);
			System.out.println("response..."+response.getStatusCode());
			System.out.println("response..."+response.getBody());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param sessionkey
	 */
	private static void getTimeSeriesDataForVibration(String sessionkey) {
		ReportController report=new ReportController();
		try {
			//Top graph Temperature
			Document dbobject=new Document();
			dbobject.put("device_id","970470347");
			dbobject.put("asset_id","AssetVibNew");
			dbobject.put("site_id","LisleMolex");
			dbobject.put("from_date","06/01/2018");	
			dbobject.put("to_date","06/18/2018");
			dbobject.put("type","All");
			dbobject.put("sub_type","All");
			dbobject.put("deviceCategoryName","Vibration");			
			//array.add("970536179_1");
			dbobject.put("channels","970470347_1");
			//dbobject.put("document_id", "5af079c56224bc50ea188072");
			
			//String json = "{\"device_id\": \"970558714\",\"document_id\": \"5ab1202b0cd7c709ac22814f\",\"year_month_day\": \"2018-03-20\",\"time\": \"13:58:58:00\",\"site_id\": \"ENG\",\"deviceCategoryName\": \"Vibration\",\"z\": \"z\"}";
			String json = "{\"device_id\": \"970558714\",\"document_id\": \"5ab024da0cd7c70488a54c5a\",\"year_month_day\": \"2018-03-20\",\"time\": \"13:58:58:00\",\"site_id\": \"ENG\",\"deviceCategoryName\": \"Vibration\",\"y\": \"y\"}";
			
			ResponseEntity<String> response = report.getTimeSeriesData(sessionkey, dbobject.toJson(),null,null);
			System.out.println("response..."+response.getStatusCode());
			System.out.println("response..."+response.getBody());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param sessionkey
	 */
	private static void getTimeSeriesDataForOldCorrosion(String sessionkey) {
		try {
			
			MongoDBConnection mongoSingle = null;
			MongoDatabase mongodb = null;
			MongoCollection<Document> table = null;
			try {
				mongoSingle = new MongoDBConnection();
				mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
				table=mongodb.getCollection("c_test");
				Document projectFields=new Document();
				projectFields.put("ascan_1", 1);
				//projectFields.put("xascan", 1);
				Document projectQuery=new Document();
				projectQuery.put("$project",projectFields);
				Document matchQuery = new Document();
				Document searchQuery = new Document();
				searchQuery.put("_id", new ObjectId("5aec6aed6224bc4feb1d6e6f"));
				matchQuery.put("$match", searchQuery);
				AggregateIterable<Document> iterable = table.aggregate(
						Arrays.asList(matchQuery, projectQuery));
				for(Document row:iterable) {
					//System.out.println("dbObject..."+row.toJson());
					System.out.println("output (SHELL) = " + row.toJson(new JsonWriterSettings(JsonMode.SHELL)));
					System.out.println("output (STRICT) = " + row.toJson(new JsonWriterSettings(JsonMode.STRICT)));
					System.out.println("output (JSON) = " + com.mongodb.util.JSON.serialize(row));
				}
				
				//ResponseEntity<String> response = report.getTimeSeriesData(sessionkey, dbobject.toJson());
				//System.out.println("response..."+response.getStatusCode());
				//System.out.println("response..."+response.getBody());
			
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
			
			/*//Bottom graph Temperature
			ReportController report=new ReportController();
		
			Document dbobject=new Document();
			dbobject.put("document_id","5aec6aed6224bc4feb1d6e6f");
			//inArray.add("test_device");
			dbobject.put("device_id","97458");
			//dbobject.put("device_id","99745874");
			dbobject.put("site_id","test");
			dbobject.put("year_month_day","2018-05-04");
			dbobject.put("time","09:14:04:00");
			dbobject.put("thickness","thickness");
			dbobject.put("deviceCategoryName","Corrosion");
		
			ResponseEntity<String> response = report.getTimeSeriesData(sessionkey, dbobject.toJson());
			System.out.println("response..."+response.getStatusCode());
			System.out.println("response..."+response.getBody());*/
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	private static void getTimeSeriesData(String sessionkey) {
		ReportController report=new ReportController();
		try {
			
			Document dbobject=new Document();
			JSONArray inArray=new JSONArray();
			JSONArray channelArray=new JSONArray();
			inArray.add("67241223");
			channelArray.add("67241223_1");
			//dbobject.put("device_id","67241221");
			dbobject.put("device_id",inArray);
			dbobject.put("asset_id","asset_0286815");
			//dbobject.put("site_id","gas_messages_temp");
			dbobject.put("site_id","buffalo");
			dbobject.put("from_date","06/19/2018");
			dbobject.put("to_date","06/19/2018");
			dbobject.put("type","All");
			dbobject.put("sub_type","All");
			dbobject.put("deviceCategoryName","Gas");
			dbobject.put("channels",channelArray);
			System.out.println("dbobject..."+dbobject.toJson());
			String gasParamJson = "{ \"gasParamJson\":[\"Temperature\",\"Humidity\",\"mV\"] }";
			ResponseEntity<String> response = report.getTimeSeriesData(sessionkey, dbobject.toJson(),gasParamJson,null);
			System.out.println("response..."+response.getStatusCode());
			System.out.println("response..."+response.getBody());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 */
	private static void getTimeSeriesDataForGas(String sessionkey) {
		ReportController report=new ReportController();
		try {
			Document dbobject=new Document();
			JSONArray inArray=new JSONArray();
			JSONArray channelArray=new JSONArray();
			inArray.add("67241223");
			channelArray.add("67241223_1");
			//dbobject.put("device_id","67241221");
			dbobject.put("device_id",inArray);
			dbobject.put("asset_id","asset_0286815");
			dbobject.put("period","minute");
			//dbobject.put("site_id","gas_messages_temp");
			dbobject.put("site_id","buffalo");
			//dbobject.put("from_date","12/12/2017");
			dbobject.put("from_date","06/19/2018");
			dbobject.put("to_date","06/19/2018");
			//2018-06-18T19:00:00Z, 06/18/2018
			//dbobject.put("from_date","12/01/2017");
			//dbobject.put("to_date","06/14/2018");
			dbobject.put("type","All");
			dbobject.put("sub_type","All");
			dbobject.put("deviceCategoryName","gasold");
			dbobject.put("channels",channelArray);
			System.out.println("dbobject..."+dbobject.toJson());
			String gasParamJson = "{ \"gasParamJson\":[\"Temperature\",\"Humidity\",\"mV\"] }";
			ResponseEntity<String> response = report.getTimeSeriesDataForGas(null,sessionkey, dbobject.toJson(),gasParamJson,null);
			//System.out.println("response..."+response.getStatusCode());
			System.out.println("response..."+response.getBody());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	/**
	 * 
	 * @param sessionkey
	 */
	private static void getTimeSeriesDataForTemperature(String sessionkey) {
		ReportController report=new ReportController();
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		try {
			mongoSingle = new MongoDBConnection();
			mongodb = mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
			//Top graph Temperature
			Document dbobject=new Document();
			dbobject.put("asset_id","35_PRV");
			JSONArray inArray=new JSONArray();
			inArray.add("99745874");
			//inArray.add("test_device");
			dbobject.put("device_id",inArray);
			//dbobject.put("device_id","99745874");
			dbobject.put("site_id","Greenbay");
			dbobject.put("from_date","02/22/2018-17:55:05");
			dbobject.put("to_date","03/01/2018-17:55:05");
			dbobject.put("type","All");
			dbobject.put("sub_type","All");
			dbobject.put("deviceCategoryName","Temperature");
			JSONArray array=new JSONArray();
			//array.add("67241219_3");
			dbobject.put("channels",array);

			ResponseEntity<String> response = report.getTimeSeriesData(sessionkey, dbobject.toJson(),null,null);
			System.out.println("response..."+response.getStatusCode());
			System.out.println("response..."+response.getBody());
			/*List<String> deviceId =new ArrayList<String>();
			deviceId.add("99745874");
			table = mongodb.getCollection("location_hierarchy");
			ReportUtility.getChannelMapping("Trap_1102", deviceId, table, "FHR");*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
		}
		
	}
	
	/**
	 * 
	 * @param sessionkey
	 * @param deviceCategoryName
	 */
	private static void compareDeviceParametersForDevice(String sessionkey,String deviceCategoryName) {
		
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		try{
			
			mongoSingle = new MongoDBConnection();
			mongodb = mongoSingle.getMongoDB("FHR_DSS");
			table=mongodb.getCollection("device_parameter_latest_new");
			
			Document jsonDocument=new Document();
			JSONArray inArray=new JSONArray();
			inArray.add("970536179");
			jsonDocument.put("compare_device_list",inArray);
			//dbobject.put("channel_length",1);
			jsonDocument.put("gps_latitude",1);
			jsonDocument.put("device_id",1);
			
			
			Document searchQuery = new Document();
			Document projectQuery = new Document();
			Document matchQuery = new Document();
			BasicDBObject project = new BasicDBObject();
			BasicDBObject inQuery = new BasicDBObject();
			inQuery.put("$in", jsonDocument.get("compare_device_list"));
			searchQuery.put(MongoDBConstants.DEVICE_ID, inQuery);
			searchQuery.put("parameter_type", "device");
			jsonDocument.remove("compare_device_list");
			matchQuery.put("$match", searchQuery);
			//project.putAll((Map) jsonDocument);
			Set<String> projectKeys = jsonDocument.keySet();
			for (String key:projectKeys) {
				project.append(key, new BasicDBObject("$ifNull",Arrays.asList("$"+key, "")));
			}
			project.put("device_name",new BasicDBObject("$ifNull",Arrays.asList("$device_name", "")));
			projectQuery.put("$project", project);
			AggregateIterable<Document>  iterable = table.aggregate(Arrays.asList(matchQuery, projectQuery));
			for(Document row:iterable) {
				System.out.println("row..."+row.toJson());
			}
			
		}catch(Exception e){
			
			
		}finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
		
	}
	
	/**
	 * 
	 * @param sessionkey
	 * @param deviceCategoryName
	 */
	private static void compareDeviceParametersForChannel(String sessionkey,String deviceCategoryName) {
		
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		try{
			
			mongoSingle = new MongoDBConnection();
			mongodb = mongoSingle.getMongoDB("FHR_DSS");
			table=mongodb.getCollection("device_parameter_latest_new");
			Document channelJson=new Document();
			JSONArray inArray=new JSONArray();
			inArray.add("970536179_1");
			channelJson.put("compare_channel_list",inArray);
			channelJson.put("external_temperature",1);
			channelJson.put("channel_id",1);
			Document searchQuery = new Document();
			Document projectQuery = new Document();
			Document matchQuery = new Document();
			BasicDBObject project = new BasicDBObject();
			BasicDBObject inQuery = new BasicDBObject();
			inQuery.put("$in", channelJson.get("compare_channel_list"));
			searchQuery.put("channel_id", inQuery);
			searchQuery.put("parameter_type", "channel");
			channelJson.remove("compare_channel_list");
			matchQuery.put("$match", searchQuery);
			//project.putAll((Map) jsonDocument);
			Set<String> projectKeys = channelJson.keySet();
			for (String key:projectKeys) {
				project.append(key, new BasicDBObject("$ifNull",Arrays.asList("$"+key, "")));
			}
			projectQuery.put("$project", project);
			
			AggregateIterable<Document>  iterable = table.aggregate(Arrays.asList(matchQuery, projectQuery));
			for(Document row:iterable) {
				System.out.println("row..."+row.toJson());
			}
			
		}catch(Exception e){
			
			
		}finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
		
	}
	
	
	/**
	 * 
	 * @param sessionkey
	 * @param deviceCategoryName
	 */
	private static void compareDeviceParametersForDeviceAndChannel(String sessionkey,String deviceCategoryName) {
		MongoDBConnection mongoSingle = null;
		MongoDatabase mongodb = null;
		MongoCollection<Document> table = null;
		try{
			
			mongoSingle = new MongoDBConnection();
			mongodb = mongoSingle.getMongoDB("FHR_DSS");
			table=mongodb.getCollection("device_parameter_latest_new");
			List<Document> sourceObj = new ArrayList<Document>();
			
			Document channelJson=new Document();
			JSONArray inArray=new JSONArray();
			inArray.add("970536179_1");
			channelJson.put("compare_channel_list",inArray);
			channelJson.put("external_temperature",1);
			channelJson.put("channel_id",1);
			Document andSearchQuery = new Document();
			Document projectQuery = new Document();
			Document matchQuery = new Document();
			BasicDBObject project = new BasicDBObject();
			BasicDBObject inQuery = new BasicDBObject();
			inQuery.put("$in", channelJson.get("compare_channel_list"));
			andSearchQuery.put("channel_id", inQuery);
			andSearchQuery.put("parameter_type", "channel");
			sourceObj.add(andSearchQuery);
			channelJson.remove("compare_channel_list");
			//project.putAll((Map) jsonDocument);
			Set<String> projectKeys = channelJson.keySet();
			for (String key:projectKeys) {
				project.append(key, new BasicDBObject("$ifNull",Arrays.asList("$"+key, "")));
			}
			
			//for device
			
			Document jsonDocument=new Document();
			inArray=new JSONArray();
			inArray.add("970536179");
			jsonDocument.put("compare_device_list",inArray);
			//dbobject.put("channel_length",1);
			jsonDocument.put("gps_latitude",1);
			jsonDocument.put("device_id",1);
			inQuery = new BasicDBObject();
			inQuery.put("$in", jsonDocument.get("compare_device_list"));
			andSearchQuery = new Document();
			andSearchQuery.put(MongoDBConstants.DEVICE_ID, inQuery);
			andSearchQuery.put("parameter_type", "device");
			sourceObj.add(andSearchQuery);
			jsonDocument.remove("compare_device_list");
			
			Document searchQuery = new Document();
			searchQuery.put("$or",sourceObj);
			matchQuery.put("$match", searchQuery);
			projectKeys = jsonDocument.keySet();
			for (String key:projectKeys) {
				project.append(key, new BasicDBObject("$ifNull",Arrays.asList("$"+key, "")));
			}
			project.append("parameter_type", 1);
			projectQuery.put("$project", project);
			
			
			AggregateIterable<Document>  iterable = table.aggregate(Arrays.asList(matchQuery, projectQuery));
			for(Document row:iterable) {
				System.out.println("row..."+row.toJson());
			}
			
		}catch(Exception e){
			
			
		}finally {
			if (mongoSingle != null) {
				CommonUtility.closeMongoConnection(mongoSingle, mongodb, table);
			}
		}
		
	}
	
}
