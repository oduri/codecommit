package com.kavi.report.utility;

import static org.boon.sort.Sort.sortBy;
import static org.boon.sort.Sorting.sort;

import java.io.Writer;
import java.math.BigDecimal;
import java.security.spec.AlgorithmParameterSpec;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.kavi.common.utility.CommonUtility;
import com.kavi.report.dataobjects.PointsDO;
import com.kavi.report.dataobjects.TemperatureDO;
import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

public class ReportUtility {

	private final static Logger logger = Logger.getLogger(ReportUtility.class);
	private final static DecimalFormat df1 = new DecimalFormat("#.####");
	private final static Double commonCorrosionMultiplier = 0.0000393701;
	private static int gatesArrayListSize = 0;
	private static Map<String, Double> checkIfGatesExist = new WeakHashMap<String, Double>();
	private static Map<String, Double> checkIfGateHeightExist = new WeakHashMap<String, Double>();
	
	private static final SimpleDateFormat initialThicknessOutputFormat = new SimpleDateFormat("MM-dd-yyyy");
	
	/**
	 * 
	 * @return
	 */
	private static Map<String, String> getOperationMap() {
		Map<String, String> lOperationMap = new HashMap<String, String>();
		lOperationMap.put("lessthan", "$lt");
		lOperationMap.put("lessthanorequalto", "$lte");
		lOperationMap.put("lessthanorequal", "$lte");
		lOperationMap.put("notequalto", "$ne");
		lOperationMap.put("notequal", "$ne");
		lOperationMap.put("greaterthan", "$gt");
		lOperationMap.put("greaterthanorequalto", "$gte");
		lOperationMap.put("greaterthanorequal", "$gte");
		lOperationMap.put("equalto", "$eq");
		lOperationMap.put("equals", "$eq");
		lOperationMap.put("notin", "$nin");
		return lOperationMap;
	}
	/**
	  * 
	  * @param iterable
	  * @param mappinglookup
	  * @param fieldArray
	  * @param fieldArray2
	  * @throws ParseException
	  */
    public static void getResultArray(AggregateIterable<Document> iterable, Map<String,Document> mappinglookup, JSONArray fieldArray, JSONArray fieldArray2) throws ParseException {
		Document newdoc = null;
		for (Document row : iterable) {
			if (row.get("initial_thickness_date_time") != null) {
				if(!row.get("initial_thickness_date_time").toString().isEmpty()) {
					row.put("initial_thickness_date_time", initialThicknessOutputFormat.format(row.get("initial_thickness_date_time")));
				}
			}
			Document resultdoc = new Document();
			Map<String,JSONArray> resultsTabMap = new HashMap<String,JSONArray> ();
			row.remove("_id");							
			Document measurement = (Document) row;
			Set<String> subtypeJsonKey = measurement.keySet();
			for (String subtypeKey : subtypeJsonKey) {
				Document mappinglookupkey = (Document) mappinglookup.get(subtypeKey);
				if(mappinglookupkey != null && !(subtypeKey.equalsIgnoreCase("device_name"))) {	
					String units = (String) mappinglookupkey.get("units");
					newdoc = new Document();
					newdoc.put("key", subtypeKey);
					if(units != null && units.length()>0) {
						newdoc.put("Description", mappinglookupkey.get("display_name")+" ["+units+"]");
					}
					else {
						newdoc.put("Description", mappinglookupkey.get("display_name"));
					}
					newdoc.put("parameter_type", mappinglookupkey.get("parameter_type"));
					newdoc.put("data_type", mappinglookupkey.get("data_type"));
					newdoc.put("Value", measurement.get(subtypeKey));
					newdoc.put("units", mappinglookupkey.get("units"));
					newdoc.put("editable_flag", mappinglookupkey.getBoolean("editable_flag")==null?false:true);
					if(resultsTabMap.get(mappinglookupkey.get("tab_name")) == null) {
						fieldArray2 = new JSONArray();
						fieldArray2.add(newdoc);
						resultsTabMap.put(mappinglookupkey.getString("tab_name"), fieldArray2);
					}
					else {
						fieldArray2 = resultsTabMap.get(mappinglookupkey.getString("tab_name"));
						fieldArray2.add(newdoc);
						resultsTabMap.put(mappinglookupkey.getString("tab_name"), fieldArray2);
					}
				}							
			}
			resultdoc.put("device_id", row.get("device_id"));
			resultdoc.put("device_name", row.get("device_name"));
			resultdoc.put("asset_name", row.get("asset_name")==null?"":row.getString("asset_name"));
			resultdoc.put("asset_id", row.get("asset_id")==null?"":row.getString("asset_id"));
			resultdoc.put("channel_id", row.getString("channel_id")==null?"":row.getString("channel_id"));
			resultdoc.put("channel_name", row.getString("channel_name")==null?"":row.getString("channel_name"));
			resultdoc.put("data",resultsTabMap);
			fieldArray.add(resultdoc);
		}	
	}
    
    /**
     * @param corrosionDoc
     */
	public static Document sortCorrosionDoc(Document corrosionDoc) {
		Document channelsDoc = null;
		List<Double> msXaxisList = null;
		List<Double> msYaxisList = null;
		List<Double> rangeXaxisList = null;
		List<Double> rangeYaxisList = null;
		Map<String,List<Double>> pointMap = null;
		if (corrosionDoc != null) {
			List<Document> timeSeriesList = (List<Document>) corrosionDoc.get("timeSeries");
			System.out.println("Doc"+timeSeriesList);
			if (CollectionUtils.isNotEmpty(timeSeriesList)) {
				for (Document document : timeSeriesList) {
					channelsDoc = (Document) document.get("channels");
					if(channelsDoc!=null) {
						msXaxisList = (List<Double>) channelsDoc.get("ms_xaxis");
						msYaxisList = (List<Double>) channelsDoc.get("ms_yaxis");
						pointMap = sortDocuments(msXaxisList,msYaxisList);
						msXaxisList = pointMap.get("xaxis");
						msYaxisList = pointMap.get("yaxis");
						channelsDoc.put("ms_xaxis", msXaxisList);
						channelsDoc.put("ms_yaxis", msYaxisList);
						rangeXaxisList = (List<Double>) channelsDoc.get("range_xaxis");
						rangeYaxisList = (List<Double>) channelsDoc.get("range_yaxis");
						pointMap = sortDocuments(rangeXaxisList,rangeYaxisList);
						rangeXaxisList = pointMap.get("xaxis");
						rangeYaxisList = pointMap.get("yaxis");
						channelsDoc.put("range_xaxis", rangeXaxisList);
						channelsDoc.put("range_yaxis", rangeYaxisList);
						document.put("channels", channelsDoc);
					}
				}
			}
		}
		return corrosionDoc;
	}
	
	private static Map<String,List<Double>> sortDocuments(List<Double> xaxisPoints, List<Double> yaxisPoints) {
		int count = 0;
		PointsDO pointsDO = null;
		List<PointsDO> pointsList = new ArrayList<PointsDO>();
		Map<String,List<Double>> pointMap = new HashMap<String,List<Double>>();
		if(CollectionUtils.isNotEmpty(xaxisPoints) && CollectionUtils.isNotEmpty(yaxisPoints) && xaxisPoints.size() == yaxisPoints.size()) {
			for(Double xPoint: xaxisPoints) {
				pointsDO = new PointsDO();
				pointsDO.setX(xPoint);
				pointsDO.setY(yaxisPoints.get(count));
				pointsList.add(pointsDO);
				count++;
			}
		}
		
		Collections.sort(pointsList,PointsDO.PointsDOComparator);
		xaxisPoints = new ArrayList<Double>();
		yaxisPoints = new ArrayList<Double>();
		if(CollectionUtils.isNotEmpty(pointsList)) {
			for(PointsDO points: pointsList) {
				xaxisPoints.add(points.getX());
				yaxisPoints.add(points.getY());
			}
		}
		pointMap.put("xaxis", xaxisPoints);
		pointMap.put("yaxis", yaxisPoints);
		return pointMap;
	}
	
	/**
	 * 
	 * This method is used to upsert Device Json Parameters to the device_parameter_latest document and 
	 * user_defined_parameter_log document
	 * 
	 * @param deviceJsonDocList
	 * @param deviceCategoryName
	 * @param mappingiterable
	 * @param paramLatestTable
	 * @param paramLogTable
	 * @param linkedUserGroupDetails
	 * @throws ParseException
	 */
	public static void upsertDeviceJsonParameters(List<BasicDBObject> deviceJsonDocList, String deviceCategoryName, AggregateIterable<Document> mappingiterable, MongoCollection<Document> paramLatestTable,
			MongoCollection<Document> paramLogTable, Map<String, String> linkedUserGroupDetails) throws ParseException {
		
		Document searchQuery = null;
		BasicDBObject updateData = null;
		BasicDBObject setValues = null;
		Set<String> keys = null;
		Document searchQueryLog = null;
		Document matchQueryLog = null;
		Object rowId = null;
		ObjectId id = null;
		Document existingUserConfig = null;
		AggregateIterable<Document> iterable = null;
		Date currenttime = new Date();
		Boolean updateflag = false;
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy/MM/dd");
		dateFormatLocal.setTimeZone(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("MM/dd/yyyy");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		String deviceId = null;
		String assetId = null;
		for (BasicDBObject deviceJsonVal : deviceJsonDocList) {
			deviceId = (String) deviceJsonVal.get(MongoDBConstants.DEVICE_ID);
			assetId = (String) deviceJsonVal.get(MongoDBConstants.ASSET_ID);
			//remove non-editable fields
			removeNonEditableFields(mappingiterable,deviceJsonVal);
			
			//parse the request json parameters
			parseRequestParamFormaat(deviceJsonVal);
			
			searchQuery = new Document();
			searchQuery.put(MongoDBConstants.DEVICE_ID, deviceId);
			if(deviceCategoryName.equalsIgnoreCase("Corrosion")) {
			 searchQuery.put(MongoDBConstants.PARAMETER_TYPE, MongoDBConstants.DEVICE);
			}
			if(assetId!=null) {
			searchQuery.put(MongoDBConstants.ASSET_ID, assetId);
			}
			updateData = new BasicDBObject();
			setValues = new BasicDBObject();
			keys = deviceJsonVal.keySet();
			// iterate through the json keys and set the to be updated values
			if (keys != null) {
				for (String key : keys) {
					if (!key.equals(MongoDBConstants.DEVICE_ID)  && !key.equals(MongoDBConstants.ASSET_ID)) {
						//updateKey = key.split("\\.");
						//System.out.println("key: "+key);
						updateData.put(key, deviceJsonVal.get(key));
					}
				}
				System.out.println("Update data: "+updateData);
				setValues.put("$set", updateData);
				paramLatestTable.updateOne(searchQuery, setValues);
			}
			searchQueryLog = new Document();
			matchQueryLog = new Document();
			searchQueryLog.put(MongoDBConstants.DEVICE_ID, deviceId);
			searchQueryLog.put("active_flag", true);
			matchQueryLog.put("$match",searchQueryLog);
			iterable = paramLogTable.aggregate(Arrays.asList(matchQueryLog));
			for (Document row : iterable) {
				rowId=row.get("_id");
				searchQuery = new Document();
				searchQuery.put("_id",rowId);
				existingUserConfig = (Document) row.get("device_parameters");
				Set<String> deviceKeys = deviceJsonVal.keySet();
				if (deviceKeys != null) {
				/*	for (String str : deviceKeys) {
						if( (existingUserConfig!=null && existingUserConfig.get(str) == null) || (existingUserConfig!=null && deviceJsonVal.get(str) !=null && 
								!(existingUserConfig.get(str).equals(deviceJsonVal.get(str))) ) 
						) {
							row.put("active_flag", false);
							row.put("active_to", currenttime);
							row.put("updated_by", linkedUserGroupDetails.get("userId"));
							updateData = new BasicDBObject();
							updateData.put("$set", row);
							paramLogTable.updateOne(searchQuery, updateData);
							updateflag = true;
						}
					}*/
					row.put("active_flag", false);
					row.put("active_to", currenttime);
					row.put("updated_by", linkedUserGroupDetails.get("userId"));
					updateData = new BasicDBObject();
					updateData.put("$set", row);
					paramLogTable.updateOne(searchQuery, updateData);
					updateflag = true;
					if(updateflag == true){
						Document doc = new Document(row);
						doc.remove("_id");
						doc.remove("active_to");
						doc.put(MongoDBConstants.DEVICE_ID, deviceId);
						doc.put("device_category", deviceCategoryName);
						doc.put("active_flag", true);
						doc.put("active_from",  DateUtils.addMilliseconds(currenttime, 1));
						doc.put("active_to",  dateFormatLocal.parse(dateFormatLocal.format(dateFormatGmt.parse("12/31/2222 23:59:59"))));
						doc.put("updated_by", linkedUserGroupDetails.get("userId"));
						paramLogTable.insertOne(doc);
						id = (ObjectId)doc.get("_id");
					}
				}
			}
			if(id != null){
				for (Document row : iterable) {
					existingUserConfig = (Document) row.get("device_parameters")==null?(Document) row.get("db_parameters"):(Document) row.get("device_parameters");
					Set<String> devKeys = deviceJsonVal.keySet();
					if (devKeys != null) {
						for (String str : devKeys) {
							//existingUserConfig.remove(str);
							if (!str.equals(MongoDBConstants.DEVICE_ID)) {
								existingUserConfig.put(str, deviceJsonVal.get(str));
							}else {
							existingUserConfig.put(str, deviceJsonVal.get(str));
							}
							System.out.println("existingUserConfig: "+existingUserConfig.toJson());
						}
					}
					row.put("updated_by", linkedUserGroupDetails.get("userId"));
					updateData = new BasicDBObject();
					updateData.put("$set", row);
					paramLogTable.updateOne(searchQueryLog, updateData);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param channelJsonDocList
	 * @param deviceCategoryName
	 * @param mappingiterable
	 * @param paramLatestTable
	 * @param paramLogTable
	 * @param linkedUserGroupDetails
	 * @throws ParseException
	 */
	public static void upsertDeviceChannelParameters(List<BasicDBObject> channelJsonDocList, String deviceCategoryName, AggregateIterable<Document> mappingiterable, MongoCollection<Document> paramLatestTable,
			MongoCollection<Document> paramLogTable, Map<String, String> linkedUserGroupDetails) throws ParseException {

		Document searchQuery = null;
		BasicDBObject updateData = null;
		BasicDBObject setValues = null;
		Set<String> keys = null;
		String[] updateKey = null;
		Document searchQueryLog = null;
		Document matchQueryLog = null;
		Object rowId = null;
		ObjectId id = null;
		Document existingUserConfig = null;
		AggregateIterable<Document> iterable = null;
		Date currenttime = new Date();
		Boolean updateflag = false;
		String channelId = null;
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy/MM/dd");
		dateFormatLocal.setTimeZone(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("MM/dd/yyyy");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		String deviceId = null;
		String assetId = null;
		for(BasicDBObject channelJsonVal:channelJsonDocList) {
			deviceId = (String) channelJsonVal.get(MongoDBConstants.DEVICE_ID);
			assetId = (String) channelJsonVal.get(MongoDBConstants.ASSET_ID);
			System.out.println("AssetId: "+assetId);
			//remove non-editable fields
			removeNonEditableFields(mappingiterable,channelJsonVal);
			
			//parse the request json parameters
			parseRequestParamFormaat(channelJsonVal);
			
			searchQuery = new Document();
			searchQuery.put(MongoDBConstants.CHANNEL_ID, deviceId);
			searchQuery.put(MongoDBConstants.PARAMETER_TYPE, MongoDBConstants.CHANNEL);
			if(assetId!=null) {
			searchQuery.put(MongoDBConstants.ASSET_ID, assetId);
			}
			updateData = new BasicDBObject();
			channelId = deviceId;
			keys = channelJsonVal.keySet();
			//iterate through the json keys and set the to be updated values
			if(keys!=null) {
				for(String key: keys) {
					if(!key.equals(MongoDBConstants.DEVICE_ID) && !key.equals(MongoDBConstants.ASSET_ID)) {
						updateKey = key.split("\\.");
						System.out.println("updateKey: "+updateKey.length+" , "+updateKey[0]+", "+key);
						updateData.put(updateKey[1], channelJsonVal.get(key));
					}
				}
				System.out.println("Final Update Key:"+updateKey+"  "+updateData.toJson());
				channelId = channelId+"_"+updateKey[0].split("_")[1];
				System.out.println("The channel Id is: "+channelId);
				searchQuery.put(MongoDBConstants.CHANNEL_ID, channelId);
				setValues = new BasicDBObject();
				setValues.put("$set", updateData);
				paramLatestTable.updateOne(searchQuery, setValues);
			}
			searchQueryLog = new Document();
			matchQueryLog = new Document();
		searchQueryLog.put(MongoDBConstants.DEVICE_ID, deviceId);
		searchQueryLog.put("active_flag", true);
		matchQueryLog.put("$match",searchQueryLog);
		iterable = paramLogTable.aggregate(Arrays.asList(matchQueryLog));
		for (Document row : iterable) {
			rowId=row.get("_id");
			searchQuery = new Document();
			searchQuery.put("_id",rowId);
			existingUserConfig = (Document) row.get("channel_parameters");
			Set<String> deviceKeys = channelJsonVal.keySet();
			if (deviceKeys != null) {
				/*for (String str : deviceKeys) {
					
					if( (existingUserConfig!=null && existingUserConfig.get(str) == null) || (existingUserConfig!=null && channelJsonVal.get(str) !=null && 
							!(existingUserConfig.get(str).equals(channelJsonVal.get(str))) ) 
					) {
						row.put("active_flag", false);
						row.put("active_to", currenttime);
						row.put("updated_by", linkedUserGroupDetails.get("userId"));
						updateData = new BasicDBObject();
						updateData.put("$set", row);
						paramLogTable.updateOne(searchQuery, updateData);
						updateflag = true;
					}
				}*/
				row.put("active_flag", false);
				row.put("active_to", currenttime);
				row.put("updated_by", linkedUserGroupDetails.get("userId"));
				updateData = new BasicDBObject();
				updateData.put("$set", row);
				paramLogTable.updateOne(searchQuery, updateData);
				updateflag = true;
				if(updateflag == true){
					Document doc = new Document(row);
					doc.remove("_id");
					doc.remove("active_to");
					doc.put(MongoDBConstants.DEVICE_ID, deviceId);
					doc.put("device_category", deviceCategoryName);
					doc.put("active_flag", true);
					doc.put("active_from",  DateUtils.addMilliseconds(currenttime, 1));
					doc.put("active_to",  dateFormatLocal.parse(dateFormatLocal.format(dateFormatGmt.parse("12/31/2222 23:59:59"))));
					doc.put("updated_by", linkedUserGroupDetails.get("userId"));
					paramLogTable.insertOne(doc);
					id = (ObjectId)doc.get("_id");
				}
			}
		}
		System.out.println("Id value: "+id);
		if(id != null){
			Document channelDoc = null;
			for (Document row : iterable) {
				existingUserConfig = (Document) row.get("channel_parameters");
				System.out.println("existingUserConfig: before: "+existingUserConfig);
				Set<String> devKeys = channelJsonVal.keySet();
				if (devKeys != null) {
					for (String str : devKeys) {
						//existingUserConfig.remove(str);
						if(!str.equals(MongoDBConstants.DEVICE_ID) && !str.equals(MongoDBConstants.ASSET_ID)) {
							updateKey = str.split("\\.");
							System.out.println("updateKey: "+updateKey[0]+", "+updateKey[1]);
							channelDoc =(Document) existingUserConfig.get(updateKey[0]);
							channelDoc.put(updateKey[1], channelJsonVal.get(str));
							System.out.println("existingUserConfig: "+existingUserConfig.toJson());
						}else {
							existingUserConfig.put(str, channelJsonVal.get(str));
						}
					}
				}
				row.put("updated_by", linkedUserGroupDetails.get("userId"));
				updateData = new BasicDBObject();
				updateData.put("$set", row);
				paramLogTable.updateOne(searchQueryLog, updateData);
			}
		}
	}
	}
	
	/**
	 * @param mappingiterable
	 * @param jsonDoc
	 */
	private static void removeNonEditableFields(AggregateIterable<Document> mappingiterable, BasicDBObject jsonDoc) {
		List<Document> lDeviceUIMappingDoc=null;
		for (Document mappingrow : mappingiterable) {
			lDeviceUIMappingDoc = (ArrayList<Document>)mappingrow.get("mapping");
			if(lDeviceUIMappingDoc!=null && lDeviceUIMappingDoc.size()>0){
				for(Document deviceUIMappingDoc:lDeviceUIMappingDoc){
					if(deviceUIMappingDoc.getBoolean("editable_flag") == null || deviceUIMappingDoc.getBoolean("editable_flag") == false) {
						jsonDoc.remove(deviceUIMappingDoc.getString("id"));
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param jsonDoc
	 * @throws ParseException
	 */
	private static void parseRequestParamFormaat(BasicDBObject jsonDoc) throws ParseException {
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy/MM/dd");
		dateFormatLocal.setTimeZone(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("MM/dd/yyyy");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		//List<String> keysToBeRemoved = new ArrayList<String>();
		if (jsonDoc != null) {
			Set<String> jsonkeys = jsonDoc.keySet();
			for (String keystr : jsonkeys) {
				/*if (jsonDoc.getString(keystr) == null) {
					keysToBeRemoved.add(keystr);
					//jsonDoc.remove(keystr);
				}*/
				if (keystr.contains("initial_thickness_date_time") ) {
					jsonDoc.put(keystr, dateFormatLocal
							.parse(dateFormatLocal.format(dateFormatGmt.parse(jsonDoc.getString(keystr)))));
				}
				if ((keystr.contains("initial_thickness") && !keystr.contains("date_time")) || keystr.contains("temperature_coefficient")
						|| keystr.contains("temperature_calibration") || keystr.contains("gate_a_start")
						|| keystr.contains("gate_a_length") || keystr.contains("gate_b_start")
						|| keystr.contains("gate_b_length") || keystr.contains("gate_a_height")
						|| keystr.contains("gate_b_height") || keystr.contains("part_material_velocity")
						|| keystr.contains("minimum_thickness") || keystr.contains("nominal_thickness")
						|| keystr.contains("thickness_alert_threshold")
						|| keystr.contains("thickness_alarm_threshold") || keystr.contains("gps_latitude") || keystr.contains("gps_longitude")) {
					if (jsonDoc.getString(keystr) != null && jsonDoc.getString(keystr) != "" && jsonDoc.getString(keystr).length() > 0) {
						jsonDoc.put(keystr, Double.parseDouble(jsonDoc.getString(keystr)));
					}
				}
			}
			/*if(CollectionUtils.isNotEmpty(keysToBeRemoved)) {
				for(String key: keysToBeRemoved) {
					jsonDoc.remove(key);
				}
			}*/
		}
	}

	/**
	 * 
	 * @param advancedFilterJson
	 * @return
	 */
	public static BasicDBObject frameFilterQuery(String advancedFilterJson) {
		BasicDBObject filterDocument = (BasicDBObject) JSON.parse(advancedFilterJson);
		filterDocument = frameFilterCondition(filterDocument);
		return filterDocument;
	}

	/**
	 * 
	 * @param filterDocument
	 */
	private static BasicDBObject frameFilterCondition(BasicDBObject filterDocument) {
		BasicDBList filter = (BasicDBList) filterDocument.get("filter_condition");
		Map<String, String> lOperationMap = getOperationMap();
		List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
		BasicDBObject andQuery = new BasicDBObject();
		if (filter != null && filter.size() > 0) {
			for (int iterate = 0; iterate < filter.size(); iterate++) {
				BasicDBObject document = (BasicDBObject) filter.get(iterate);
				if (document.getString("operator").equalsIgnoreCase("in")) {
					BasicDBObject inQuery = new BasicDBObject();
					inQuery.put("$in", document.get("value1"));
					obj.add(new BasicDBObject(document.getString("column"), inQuery));
				} else if (document.getString("operator").equalsIgnoreCase("nin")) {
					BasicDBObject inQuery = new BasicDBObject();
					inQuery.put("$nin", document.get("value1"));
					obj.add(new BasicDBObject(document.getString("column"), inQuery));
				} else if (document.getString("operator").equalsIgnoreCase("between")) {
					if (document.getString("column").endsWith("time")) {
						try {
							SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
							Date fromDate = simpleDateFormat.parse(document.getString("value1"));

							Calendar cal = Calendar.getInstance();
							cal.setTime(fromDate);

							int year = cal.get(Calendar.YEAR);
							int monthNumber = cal.get(Calendar.MONTH);
							int dateNumber = cal.get(Calendar.DAY_OF_MONTH);
							monthNumber += 1;

							Date toDate = simpleDateFormat.parse(document.getString("value2"));

							cal = Calendar.getInstance();
							cal.setTime(toDate);

							int toYear = cal.get(Calendar.YEAR);
							int toMonthNumber = cal.get(Calendar.MONTH);
							int toDateNumber = cal.get(Calendar.DAY_OF_MONTH);
							toMonthNumber += 1;
							obj.add(new BasicDBObject(document.getString("column"),
									new BasicDBObject("$gte",
											new DateTime(year, monthNumber, dateNumber, 0, 0, DateTimeZone.UTC)
													.toDate()).append("$lte",
															new DateTime(toYear, toMonthNumber, toDateNumber, 23, 59,
																	DateTimeZone.UTC).toDate())));
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						BasicDBObject object = new BasicDBObject();
						object.put("$gte", document.get("value1"));
						object.put("$lte", document.get("value2"));
						obj.add(new BasicDBObject(document.getString("column"), object));
					}
				} else {
					if (lOperationMap.get(document.getString("operator")) != null) {
						if (document.getString("column").endsWith("time")) {
							try {
								SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
								Date fromDate = simpleDateFormat.parse(document.getString("value1"));
								Calendar cal = Calendar.getInstance();
								cal.setTime(fromDate);
								int year = cal.get(Calendar.YEAR);
								int monthNumber = cal.get(Calendar.MONTH);
								int dateNumber = cal.get(Calendar.DAY_OF_MONTH);
								monthNumber += 1;
								obj.add(new BasicDBObject(document.getString("column"), new BasicDBObject(
										lOperationMap.get(document.getString("operator")),
										new DateTime(year, monthNumber, dateNumber, 0, 0, DateTimeZone.UTC).toDate())));

							} catch (Exception e) {
								e.printStackTrace();
							}

						} else {
							BasicDBObject dbObject = new BasicDBObject();
							dbObject.put(lOperationMap.get(document.getString("operator")), document.get("value1"));
							obj.add(new BasicDBObject(document.getString("column"), dbObject));
						}
					} else {
						obj.add(new BasicDBObject(document.getString("column"), document.get("value1")));
					}
				}
			}
		}
		andQuery.put("$and", obj);
		return andQuery;

	}

	/**
	 * 
	 * @param row
	 */
	public static Document updateJsonDataType(Document doc) {
		Document updDoc = new Document();
		Set<String> jsonKey = doc.keySet();
		for (String key : jsonKey) {
			updDoc.put(key, doc.get(key));
			if (doc.get(key) instanceof Double) {
				// doc.putIfAbsent("data_type", "Double");
				updDoc.put("data_type", "Double");
			}
		}
		return updDoc;
	}

	/**
	 * 
	 * @param lDeviceUIMappingDoc
	 * @return
	 */
	public static List<Document> getMappingDocuments(AggregateIterable<Document> mappingiterable, String type,
			Document messageObject, String category) {
		DecimalFormat df1 = new DecimalFormat("#.####");
		DateFormat df3 = CommonUtility.getDateFormat("month");
		List<Document> lDeviceUIMappingDoc = new ArrayList<Document>();
		Map<String, Document> lMappUIDocuments = new HashMap<String, Document>();
		if (category.equalsIgnoreCase("Corrosion")) {
			String thickness_algorithm = "";
			thickness_algorithm = (String) messageObject.get("thickness_algorithm");
			if (thickness_algorithm == null || thickness_algorithm.equalsIgnoreCase("Device Thickness")) {
				if (messageObject.get("thickness_maxpeak") != null) {
					messageObject.remove("thickness_maxpeak");
				}
				if (messageObject.get("thickness_flank") != null) {
					messageObject.remove("thickness_flank");
				}
				if (messageObject.get("thickness_zerocrossing") != null) {
					messageObject.remove("thickness_zerocrossing");
				}
				if (messageObject.get("temperature_corrected_thickness_maxpeak") != null) {
					messageObject.remove("temperature_corrected_thickness_maxpeak");
				}
				if (messageObject.get("temperature_corrected_thickness_flank") != null) {
					messageObject.remove("temperature_corrected_thickness_flank");
				}
				if (messageObject.get("temperature_corrected_thickness_zerocrossing") != null) {
					messageObject.remove("temperature_corrected_thickness_zerocrossing");
				}
			} else if (thickness_algorithm != null && thickness_algorithm.equalsIgnoreCase("Max Peak")) {
				if (messageObject.get("thickness") != null) {
					messageObject.remove("thickness");
				}
				if (messageObject.get("thickness_flank") != null) {
					messageObject.remove("thickness_flank");
				}
				if (messageObject.get("thickness_zerocrossing") != null) {
					messageObject.remove("thickness_zerocrossing");
				}
				if (messageObject.get("temperature_corrected_thickness") != null) {
					messageObject.remove("temperature_corrected_thickness");
				}
				if (messageObject.get("temperature_corrected_thickness_flank") != null) {
					messageObject.remove("temperature_corrected_thickness_flank");
				}
				if (messageObject.get("temperature_corrected_thickness_zerocrossing") != null) {
					messageObject.remove("temperature_corrected_thickness_zerocrossing");
				}
			} else if (thickness_algorithm != null && thickness_algorithm.equalsIgnoreCase("flank")) {
				if (messageObject.get("thickness") != null) {
					messageObject.remove("thickness");
				}
				if (messageObject.get("thickness_maxpeak") != null) {
					messageObject.remove("thickness_maxpeak");
				}
				if (messageObject.get("thickness_zerocrossing") != null) {
					messageObject.remove("thickness_zerocrossing");
				}
				if (messageObject.get("temperature_corrected_thickness") != null) {
					messageObject.remove("temperature_corrected_thickness");
				}
				if (messageObject.get("temperature_corrected_thickness_maxpeak") != null) {
					messageObject.remove("temperature_corrected_thickness_maxpeak");
				}
				if (messageObject.get("temperature_corrected_thickness_zerocrossing") != null) {
					messageObject.remove("temperature_corrected_thickness_zerocrossing");
				}
			} else if (thickness_algorithm != null && thickness_algorithm.equalsIgnoreCase("zerocrossing")) {
				if (messageObject.get("thickness") != null) {
					messageObject.remove("thickness");
				}
				if (messageObject.get("thickness_maxpeak") != null) {
					messageObject.remove("thickness_maxpeak");
				}
				if (messageObject.get("thickness_flank") != null) {
					messageObject.remove("thickness_flank");
				}
				if (messageObject.get("temperature_corrected_thickness") != null) {
					messageObject.remove("temperature_corrected_thickness");
				}
				if (messageObject.get("temperature_corrected_thickness_maxpeak") != null) {
					messageObject.remove("temperature_corrected_thickness_maxpeak");
				}
				if (messageObject.get("temperature_corrected_thickness_flank") != null) {
					messageObject.remove("temperature_corrected_thickness_flank");
				}
			}
		}
		for (Document mappingrow : mappingiterable) {
			lDeviceUIMappingDoc = (ArrayList<Document>) mappingrow.get("mapping");
		}
		if (lDeviceUIMappingDoc != null && lDeviceUIMappingDoc.size() > 0) {
			for (Document deviceUIMappingDoc : lDeviceUIMappingDoc) {
				if (type != null && type.equalsIgnoreCase("All")) {
					lMappUIDocuments.put(deviceUIMappingDoc.getString("id"), deviceUIMappingDoc);
				} else if (type.equalsIgnoreCase(deviceUIMappingDoc.getString("tab_name"))) {
					lMappUIDocuments.put(deviceUIMappingDoc.getString("id"), deviceUIMappingDoc);
				}
			}
		}
		List<Document> listDocument = new ArrayList<Document>();
		Set<String> messageObjectKeys = messageObject.keySet();
		for (String key : messageObjectKeys) {
			if (lMappUIDocuments.get(key) != null) {
				Document messageDocument = new Document();
				messageDocument.putAll(lMappUIDocuments.get(key));
				if (key.equalsIgnoreCase("initial_thickness_date_time")
						&& messageObject.get("initial_thickness_date_time") != null) {
					messageObject.put(key, df3.format(messageObject.get("initial_thickness_date_time")));
				}
				Double part_material_velocity = (Double) messageObject.get("part_material_velocity");
				if (key.equalsIgnoreCase("message_time") && messageObject.get("message_time") instanceof Date) {
					messageObject.put(key, df3.format(messageObject.get("message_time")));
				}
				if (key.equalsIgnoreCase("gate_a_start") || key.equalsIgnoreCase("gate_b_start")
						|| key.equalsIgnoreCase("gate_a_length") || key.equalsIgnoreCase("gate_b_length")
						|| key.equalsIgnoreCase("peak_1_position") || key.equalsIgnoreCase("peak_2_position")) {
					messageObject.put(key,
							df1.format((Double) messageObject.get(key) * part_material_velocity * (0.001)));
				}
				if (key.equalsIgnoreCase("corrosion_rate_st") || key.equalsIgnoreCase("corrosion_rate_lt")
						|| key.equalsIgnoreCase("thickness") || key.equalsIgnoreCase("temperature_corrected_thickness")
						|| key.equalsIgnoreCase("thickness_maxpeak")
						|| key.equalsIgnoreCase("temperature_corrected_thickness_maxpeak")
						|| key.equalsIgnoreCase("thickness_flank")
						|| key.equalsIgnoreCase("temperature_corrected_thickness_flank")
						|| key.equalsIgnoreCase("thickness_zerocrossing")
						|| key.equalsIgnoreCase("temperature_corrected_thickness_zerocrossing")) {
					if(messageObject.get(key) instanceof String){
						String str = (String) messageObject.get(key);
						Double thickness = Double.parseDouble(str);
						messageObject.put(key, df1.format( thickness));
						
					}else{
						messageObject.put(key, df1.format((Double) messageObject.get(key)));
					}
				
				}
				messageDocument.put("value", (messageObject.get(key) == null || messageObject.get(key).equals("")) ? "-"
						: messageObject.get(key));
				listDocument.add(messageDocument);
			}
		}
		return listDocument;
	}

	public static List<Document> getMappingDocuments1(AggregateIterable<Document> mappingiterable, String type,
			Document messageObject, String category) {
		DecimalFormat df1 = new DecimalFormat("#.####");
		DateFormat df3 = CommonUtility.getDateFormat("month");
		List<Document> lDeviceUIMappingDoc = new ArrayList<Document>();
		Map<String, Document> lMappUIDocuments = new HashMap<String, Document>();
		if (category.equalsIgnoreCase("Corrosion")) {
			String thickness_algorithm = "";
			thickness_algorithm = (String) messageObject.get("thickness_algorithm");
			if (thickness_algorithm == null || thickness_algorithm.equalsIgnoreCase("Device Thickness")) {
				if (messageObject.get("thickness_maxpeak") != null) {
					messageObject.remove("thickness_maxpeak");
				}
				if (messageObject.get("thickness_flank") != null) {
					messageObject.remove("thickness_flank");
				}
				if (messageObject.get("thickness_zerocrossing") != null) {
					messageObject.remove("thickness_zerocrossing");
				}
				if (messageObject.get("temperature_corrected_thickness_maxpeak") != null) {
					messageObject.remove("temperature_corrected_thickness_maxpeak");
				}
				if (messageObject.get("temperature_corrected_thickness_flank") != null) {
					messageObject.remove("temperature_corrected_thickness_flank");
				}
				if (messageObject.get("temperature_corrected_thickness_zerocrossing") != null) {
					messageObject.remove("temperature_corrected_thickness_zerocrossing");
				}
			} else if (thickness_algorithm != null && thickness_algorithm.equalsIgnoreCase("maxpeak")) {
				if (messageObject.get("thickness") != null) {
					messageObject.remove("thickness");
				}
				if (messageObject.get("thickness_flank") != null) {
					messageObject.remove("thickness_flank");
				}
				if (messageObject.get("thickness_zerocrossing") != null) {
					messageObject.remove("thickness_zerocrossing");
				}
				if (messageObject.get("temperature_corrected_thickness") != null) {
					messageObject.remove("temperature_corrected_thickness");
				}
				if (messageObject.get("temperature_corrected_thickness_flank") != null) {
					messageObject.remove("temperature_corrected_thickness_flank");
				}
				if (messageObject.get("temperature_corrected_thickness_zerocrossing") != null) {
					messageObject.remove("temperature_corrected_thickness_zerocrossing");
				}
			} else if (thickness_algorithm != null && thickness_algorithm.equalsIgnoreCase("flank")) {
				if (messageObject.get("thickness") != null) {
					messageObject.remove("thickness");
				}
				if (messageObject.get("thickness_maxpeak") != null) {
					messageObject.remove("thickness_maxpeak");
				}
				if (messageObject.get("thickness_zerocrossing") != null) {
					messageObject.remove("thickness_zerocrossing");
				}
				if (messageObject.get("temperature_corrected_thickness") != null) {
					messageObject.remove("temperature_corrected_thickness");
				}
				if (messageObject.get("temperature_corrected_thickness_maxpeak") != null) {
					messageObject.remove("temperature_corrected_thickness_maxpeak");
				}
				if (messageObject.get("temperature_corrected_thickness_zerocrossing") != null) {
					messageObject.remove("temperature_corrected_thickness_zerocrossing");
				}
			} else if (thickness_algorithm != null && thickness_algorithm.equalsIgnoreCase("zerocrossing")) {
				if (messageObject.get("thickness") != null) {
					messageObject.remove("thickness");
				}
				if (messageObject.get("thickness_maxpeak") != null) {
					messageObject.remove("thickness_maxpeak");
				}
				if (messageObject.get("thickness_flank") != null) {
					messageObject.remove("thickness_flank");
				}
				if (messageObject.get("temperature_corrected_thickness") != null) {
					messageObject.remove("temperature_corrected_thickness");
				}
				if (messageObject.get("temperature_corrected_thickness_maxpeak") != null) {
					messageObject.remove("temperature_corrected_thickness_maxpeak");
				}
				if (messageObject.get("temperature_corrected_thickness_flank") != null) {
					messageObject.remove("temperature_corrected_thickness_flank");
				}
			}
		}
		for (Document mappingrow : mappingiterable) {
			lDeviceUIMappingDoc = (ArrayList<Document>) mappingrow.get("mapping");
		}
		if (lDeviceUIMappingDoc != null && lDeviceUIMappingDoc.size() > 0) {
			for (Document deviceUIMappingDoc : lDeviceUIMappingDoc) {
				if (type != null && type.equalsIgnoreCase("All")) {
					lMappUIDocuments.put(deviceUIMappingDoc.getString("id"), deviceUIMappingDoc);
				} else if (type.equalsIgnoreCase(deviceUIMappingDoc.getString("tab_name"))) {
					lMappUIDocuments.put(deviceUIMappingDoc.getString("id"), deviceUIMappingDoc);
				}
			}
		}
		
		List<Document> listDocument = new ArrayList<Document>();
		Set<String> messageObjectKeys = messageObject.keySet();
		for (String key : messageObjectKeys) {
			if (lMappUIDocuments.get(key) != null) {
				Document messageDocument = new Document();
				messageDocument.putAll(lMappUIDocuments.get(key));
				if (category.equalsIgnoreCase("Corrosion")) {
					if (key.equalsIgnoreCase("initial_thickness_date_time")
							&& messageObject.get("initial_thickness_date_time") != null) {
						messageObject.put(key, df3.format(messageObject.get("initial_thickness_date_time")));
					}
					Double part_material_velocity = (Double) messageObject.get("part_material_velocity");
					if (key.equalsIgnoreCase("message_time") && messageObject.get("message_time") instanceof Date) {
						messageObject.put(key, df3.format(messageObject.get("message_time")));
					}
					if (key.equalsIgnoreCase("gate_a_start") || key.equalsIgnoreCase("gate_b_start")
							|| key.equalsIgnoreCase("gate_a_length") || key.equalsIgnoreCase("gate_b_length")
							|| key.equalsIgnoreCase("peak_1_position") || key.equalsIgnoreCase("peak_2_position")) {
						messageObject.put(key,
								df1.format((Double) messageObject.get(key) * part_material_velocity * (0.001)));
					}
					if (key.equalsIgnoreCase("corrosion_rate_st") || key.equalsIgnoreCase("corrosion_rate_lt")
							|| key.equalsIgnoreCase("thickness")
							|| key.equalsIgnoreCase("temperature_corrected_thickness")
							|| key.equalsIgnoreCase("thickness_maxpeak")
							|| key.equalsIgnoreCase("temperature_corrected_thickness_maxpeak")
							|| key.equalsIgnoreCase("thickness_flank")
							|| key.equalsIgnoreCase("temperature_corrected_thickness_flank")
							|| key.equalsIgnoreCase("thickness_zerocrossing")
							|| key.equalsIgnoreCase("temperature_corrected_thickness_zerocrossing")) {
						messageObject.put(key, df1.format((Double) messageObject.get(key) * (25.4)));
					}
				}
				messageDocument.put("value", (messageObject.get(key) == null || messageObject.get(key).equals("")) ? "-"
						: messageObject.get(key));
				listDocument.add(messageDocument);
			}
		}
		return listDocument;
	}

	public static List<Document> sortDocumentArray(List<Document> array, String keyname1, String keyname2) {

		List<Document> jsonValues = new ArrayList<Document>();
		for (int iterate = 0; iterate < array.size(); iterate++) {
			jsonValues.add((Document) array.get(iterate));
		}
		Collections.sort(jsonValues, new Comparator<Document>() {

			@Override
			public int compare(Document lhs, Document rhs) {
				int value1 = (Integer) lhs.get(keyname1) > (Integer) rhs.get(keyname1) ? 1
						: ((Integer) lhs.get(keyname1) < (Integer) rhs.get(keyname1) ? -1 : 0);
				if (value1 == 0) {
					return Integer.compare((Integer) lhs.get(keyname2), (Integer) rhs.get(keyname2));
				}
				return Integer.compare((Integer) lhs.get(keyname1), (Integer) rhs.get(keyname1));
			}
		});
		return jsonValues;
	}

	/*
	 * @Override public int compare(Document lhs, Document rhs) { return
	 * (Integer) lhs.get(keyname) > (Integer) rhs.get(keyname) ? 1 : ((Integer)
	 * lhs.get(keyname) < (Integer) rhs.get(keyname) ? -1 : 0); //return
	 * Integer.compare((Integer) rhs.get(keyname), (Integer) lhs.get(keyname));
	 * }
	 */

	/**
	 * 
	 * @param deviceUIMappingDoc
	 * @param id
	 * @return
	 */
	public static Map<String, String> getDisplayName(Document deviceUIMappingDoc, String id) {
		Map<String, String> lDisplayNameDocuments = new HashMap<String, String>();
		Document doc = (Document) deviceUIMappingDoc.get(id);
		lDisplayNameDocuments.putAll((Map) doc);
		return lDisplayNameDocuments;
	}

	/**
	 * 
	 * @param key
	 * @param decimalFormat
	 * @param row
	 */
	public static void formatDecimal(String key, DecimalFormat decimalFormat, Document row) {
		if (row.get(key) != null) {
			Double keyvalue = (Double) row.get(key);
			if (keyvalue != null) {
				BigDecimal keyvalueRounded = new BigDecimal(keyvalue);
				row.put(key, Double.parseDouble(decimalFormat.format(keyvalueRounded)));
			}
		}
	}

	/**
	 * 
	 * @param mappingiterable
	 * @param groupBy
	 * @param key
	 * @return
	 */
	public static BasicDBObject groupMappingDocuments(AggregateIterable<Document> mappingiterable, boolean groupBy,
			String key) {
		BasicDBObject groupDoc = new BasicDBObject();
		if (groupBy) {
			List<Document> lDeviceUIMappingDoc = null;
			Map<String, JSONArray> lMap = new HashMap<String, JSONArray>();
			JSONArray array = new JSONArray();
			for (Document mappingrow : mappingiterable) {
				lDeviceUIMappingDoc = (ArrayList<Document>) mappingrow.get("mapping");
			}
			if (lDeviceUIMappingDoc != null && lDeviceUIMappingDoc.size() > 0) {
				for (Document deviceUIMappingDoc : lDeviceUIMappingDoc) {
					if (lMap.get(deviceUIMappingDoc.getString(key)) != null) {
						if (!(deviceUIMappingDoc.get("config_display_flag") == null)
								&& deviceUIMappingDoc.get("config_display_flag").equals(true)) {
							array = lMap.get(deviceUIMappingDoc.getString(key));
							Document parameterDoc = new Document();
							parameterDoc.put("key", deviceUIMappingDoc.get("id"));
							parameterDoc.put("Description", deviceUIMappingDoc.get("display_name"));
							parameterDoc.put("data_type", deviceUIMappingDoc.get("data_type"));
							parameterDoc.put("device_param_display", deviceUIMappingDoc.get("device_param_display"));
							parameterDoc.put("notification_display_flag",deviceUIMappingDoc.get("notification_display_flag"));
							parameterDoc.put("config_display_flag", deviceUIMappingDoc.get("config_display_flag"));
							parameterDoc.put("report_display_flag", deviceUIMappingDoc.get("report_display_flag"));
							parameterDoc.put("config_default", deviceUIMappingDoc.get("config_default"));
							parameterDoc.put("editable_flag",deviceUIMappingDoc.getBoolean("editable_flag") == null ? false : true);
							parameterDoc.put("parameter_type", deviceUIMappingDoc.get("parameter_type"));
							array.add(parameterDoc);
							lMap.put(deviceUIMappingDoc.getString(key), array);
						}
					} else {
						if (!(deviceUIMappingDoc.get("config_display_flag") == null)
								&& deviceUIMappingDoc.get("config_display_flag").equals(true)) {
							array = new JSONArray();
							Document parameterDoc = new Document();
							parameterDoc.put("key", deviceUIMappingDoc.get("id"));
							parameterDoc.put("Description", deviceUIMappingDoc.get("display_name"));
							parameterDoc.put("data_type", deviceUIMappingDoc.get("data_type"));
							parameterDoc.put("device_param_display", deviceUIMappingDoc.get("device_param_display"));
							parameterDoc.put("notification_display_flag",
									deviceUIMappingDoc.get("notification_display_flag"));
							parameterDoc.put("config_display_flag", deviceUIMappingDoc.get("config_display_flag"));
							parameterDoc.put("report_display_flag", deviceUIMappingDoc.get("report_display_flag"));
							parameterDoc.put("config_default", deviceUIMappingDoc.get("config_default"));
							parameterDoc.put("editable_flag",
									deviceUIMappingDoc.getBoolean("editable_flag") == null ? false : true);
							parameterDoc.put("parameter_type", deviceUIMappingDoc.get("parameter_type"));
							array.add(parameterDoc);
							lMap.put(deviceUIMappingDoc.getString(key), array);
						}
					}
				}
			}
			for (Map.Entry<String, JSONArray> entry : lMap.entrySet()) {
				Document doc = new Document();
				doc.put("id", entry.getKey());
				doc.put("data", entry.getValue());
				groupDoc.put(entry.getKey(), doc);
			}
		}
		return groupDoc;
	}

	/**
	 * 
	 * @param mappingiterable
	 * @return
	 */
	public static Map<String, Document> getCompareDeviceMappingDocuments(AggregateIterable<Document> mappingiterable) {
		List<Document> lDeviceUIMappingDoc = new ArrayList<Document>();
		Map<String, Document> lMappUIDocuments = new HashMap<String, Document>();
		for (Document mappingrow : mappingiterable) {
			lDeviceUIMappingDoc = (ArrayList<Document>) mappingrow.get("mapping");
		}
		if (lDeviceUIMappingDoc != null && lDeviceUIMappingDoc.size() > 0) {
			for (Document deviceUIMappingDoc : lDeviceUIMappingDoc) {
				lMappUIDocuments.put(deviceUIMappingDoc.getString("id"), deviceUIMappingDoc);
			}
		}
		return lMappUIDocuments;
	}

	/**
	 * 
	 * @param mappingiterable
	 * @return
	 */
	public static List<String> getDistinctTabNames(AggregateIterable<Document> mappingiterable, String key) {
		List<Document> lDeviceUIMappingDoc = new ArrayList<Document>();
		List<String> list = new ArrayList<String>();
		for (Document mappingrow : mappingiterable) {
			lDeviceUIMappingDoc = (ArrayList<Document>) mappingrow.get("mapping");
		}
		if (lDeviceUIMappingDoc != null && lDeviceUIMappingDoc.size() > 0) {
			for (Document deviceUIMappingDoc : lDeviceUIMappingDoc) {
				list.add(deviceUIMappingDoc.getString(key));
			}
		}
		List<String> deduped = list.stream().distinct().collect(Collectors.toList());
		return deduped;
	}

	/********************
	 * The below method needs to be migrated starts
	 **********/

	/**
	 * 
	 * @return
	 */
	private static Map<String, String> lObjectHierarchy() {
		Map<String, String> unwindObjectHierarchy = new TreeMap<String, String>();
		unwindObjectHierarchy.put("company.plant", "$company.plant");
		unwindObjectHierarchy.put("company.plant.site", "$company.plant.site");
		unwindObjectHierarchy.put("company.plant.site.unit", "$company.plant.site.unit");
		unwindObjectHierarchy.put("company.plant.site.unit.asset", "$company.plant.site.unit.asset");
		return unwindObjectHierarchy;
	}

	public static List<Document> doRecursive(Document row, List<Document> deviceCategoryDocument) {
		Set<String> keys = row.keySet();
		for (String key : keys) {
			Object value = row.get(key);
			if (value instanceof List && key != null && key.equalsIgnoreCase("device_category")) {
				deviceCategoryDocument = (List<Document>) value;
				return deviceCategoryDocument;
			}
			if (value instanceof Document && deviceCategoryDocument.size() <= 0) {
				deviceCategoryDocument = doRecursive((Document) value, deviceCategoryDocument);
			}
		}
		return deviceCategoryDocument;
	}

	/**
	 * 
	 * @param array
	 */
	private static Document getTemperatureDocuments(List<Document> array) {
		Document deviceCategoryDoc = new Document();
		for (int iterate = 0; iterate < array.size(); iterate++) {
			deviceCategoryDoc = (Document) array.get(iterate);
			if (deviceCategoryDoc.getString("type") != null
					&& deviceCategoryDoc.getString("type").equalsIgnoreCase("Temperature")) {
				return deviceCategoryDoc;
			}
		}
		return deviceCategoryDoc;
	}

	/**
	 * 
	 * @param temperatureDevice
	 * @return
	 */
	private static List<Document> frameChannelMappingJsonDocument(List<Document> temperatureDevice,
			List<String> lDeviceId) {
		Document channelMappingDoc = new Document();
		List<Document> channelMappingDocument = new ArrayList<Document>();
		for (int iterate = 0; iterate < temperatureDevice.size(); iterate++) {
			Document tempDoc = (Document) temperatureDevice.get(iterate);
			for (String deviceId : lDeviceId) {
				if (tempDoc.getString("id").equalsIgnoreCase(deviceId)) {
					List<Document> channelList = (List<Document>) tempDoc.get("channel");
					if (channelList != null && channelList.size() > 0) {
						for (int channelListIterate = 0; channelListIterate < channelList
								.size(); channelListIterate++) {
							channelMappingDoc = new Document();
							Document channelListDetails = channelList.get(channelListIterate);
							channelMappingDoc.put("device_id", tempDoc.get("id"));
							channelMappingDoc.put("device_channel", channelListDetails.get("id")); // id
																									// assigned
																									// to
																									// the
																									// channel
							channelMappingDoc.put("device_channel_name", channelListDetails.get("name"));
							// channelMappingDoc.put("device_channel_desc",channelListDetails.get("desc"));
							// // description assigned to the channel
							// channelMappingDoc.put("device_channel_key",tempDoc.get("id")+"_"+channelListDetails.get("name"));
							// channelMappingDoc.put("device_channel_desc",
							// tempDoc.get("name")+"_"+channelListDetails.get("name"));
							channelMappingDocument.add(channelMappingDoc);
						}
					}
				}
			}
		}
		return channelMappingDocument;
	}

	public static Document getTopChartParameters(String type) {
		Document selectedFields = new Document("_id", 0);
		if (type.equalsIgnoreCase("Corrosion")) {
			selectedFields.put("min_y_axis", "$db_parameters.min_y_axis");
			selectedFields.put("max_y_axis", "$db_parameters.max_y_axis");
		}
		if (type.equalsIgnoreCase("Vibration")) {
			selectedFields.put("pp_accel_alarm", "$db_parameters.pp_accel_alarm");
			selectedFields.put("pp_accel_alert", "$db_parameters.pp_accel_alert");
			selectedFields.put("pp_vel_alarm", "$db_parameters.pp_vel_alarm");
			selectedFields.put("pp_vel_alert", "$db_parameters.pp_vel_alert");
			selectedFields.put("rms_accel_alarm", "$db_parameters.rms_accel_alarm");
			selectedFields.put("rms_accel_alert", "$db_parameters.rms_accel_alert");
			selectedFields.put("rms_vel_alarm", "$db_parameters.rms_vel_alarm");
			selectedFields.put("rms_vel_alert", "$db_parameters.rms_vel_alert");
		}
		return selectedFields;
	}

	/**
	 * 
	 * @param assetId
	 * @param deviceId
	 * @return
	 */
	public static Document getChannelMapping(String assetId, List<String> deviceId, MongoCollection<Document> table,
			String companyId) {

		Document finalDoc = new Document();
		Map<String, String> unwindObjectHierarchy = lObjectHierarchy();
		List<Document> listDocument = new ArrayList<Document>();
		List<Document> channelMappingDocument = new ArrayList<Document>();
		try {

			Document projectQuery = new Document();
			Document project = new Document();
			for (Map.Entry<String, String> entry : unwindObjectHierarchy.entrySet()) {
				Document unwindDoc = new Document();
				unwindDoc.put("$unwind", unwindObjectHierarchy.get(entry.getKey()));
				/*
				 * project.put(entry.getKey()+".id",1);
				 * project.put(entry.getKey()+".name",1);
				 * project.put(entry.getKey()+".category",1);
				 * project.put(entry.getKey()+".channels",1);
				 * project.put(entry.getKey()+".device_category",1);
				 */
				project.put("asset.id", "$" + entry.getKey() + ".id");
				project.put("asset.name", "$" + entry.getKey() + ".name");
				project.put("asset.category", "$" + entry.getKey() + ".category");
				project.put("asset.channels", "$" + entry.getKey() + ".channels");
				project.put("asset.device_category", "$" + entry.getKey() + ".device_category");

				listDocument.add(unwindDoc);
			}
			Document searchQuery = new Document();
			searchQuery.put("company.plant.site.unit.asset.id", assetId);// change
																			// this
																			// to
																			// in
																			// query
																			// if
																			// we
																			// want
																			// to
																			// perform
																			// multiple
																			// asset
			searchQuery.put("company.id",companyId);
			searchQuery.put("version_control.active_flag",true);
			Document matchQuery = new Document();
			matchQuery.put("$match", searchQuery);
			projectQuery.put("$project", project);
			listDocument.add(matchQuery);
			listDocument.add(projectQuery);
			AggregateIterable<Document> iterable = table.aggregate(listDocument);
			List<Document> deviceCategoryDocument = new ArrayList<Document>();
			for (Document row : iterable) {
				Document asset = (Document) row.get("asset");
				deviceCategoryDocument = (ArrayList<Document>) asset.get("device_category");
				// deviceCategoryDocument=doRecursive(row,
				// deviceCategoryDocument);
				if (deviceCategoryDocument.size() > 0) {
					Document temparatureDoc = getTemperatureDocuments(deviceCategoryDocument);
					if (temparatureDoc.get("device") != null) {
						List<Document> temperatureDeviceDoc = (List<Document>) temparatureDoc.get("device");
						channelMappingDocument = frameChannelMappingJsonDocument(temperatureDeviceDoc, deviceId);
					}
				}

			}

			finalDoc.put("channel_mapping", channelMappingDocument);
			return finalDoc;
		} catch (Exception e) {

		} finally {

		}
		return finalDoc;
	}

	/******************** The below method needs to be migrated ends **********/

	/**
	 * 
	 * @param table
	 * @param userId
	 * @param organizationName
	 * @return
	 */
	public static List<TemperatureDO> getSiteCollectionFromLocationHierarchyTable(MongoCollection<Document> table,
			String organizationName, String deviceId) {
		List<String> deviceIdList = Arrays.asList(deviceId.split("\\s*,\\s*"));
		List<TemperatureDO> lSiteInfoDocuments = new ArrayList<TemperatureDO>();
		Document searchQuery = new Document();
		searchQuery.put("company.id", organizationName);
		Map<String, String> unwindObjectHierarchy = lObjectHierarchy();
		Document projectQuery = new Document();
		Document project = new Document();
		List<Document> listDocument = new ArrayList<Document>();
		for (Entry<String, String> entry : unwindObjectHierarchy.entrySet()) {
			Document unwindDoc = new Document();
			unwindDoc.put("$unwind", unwindObjectHierarchy.get(entry.getKey()));
			project.put("company.name", 1);
			project.put("site.id", "$company.plant.site.id");
			project.put("site.name", "$company.plant.site.name");
			project.put("asset", "$company.plant.site.unit.asset");
			listDocument.add(unwindDoc);
		}
		Document matchQuery = new Document();
		BasicDBObject inQuery = new BasicDBObject();
		inQuery.put("$in", deviceIdList);
		searchQuery.put("company.plant.site.unit.asset.device_category.device.id", inQuery);
		matchQuery.put("$match", searchQuery);
		projectQuery.put("$project", project);
		listDocument.add(matchQuery);
		listDocument.add(projectQuery);
		AggregateIterable<Document> iterable = table.aggregate(listDocument);
		for (Document row : iterable) {
			Document site = (Document) row.get("site");
			TemperatureDO temperatureDO = null;
			if (site != null) {
				temperatureDO = new TemperatureDO();
				temperatureDO.setSiteId(site.getString("id"));
				temperatureDO.setSiteName(site.getString("name"));
				temperatureDO.setCollectionName("t_" + temperatureDO.getSiteId() + MongoDBConstants.MONGO_COLLECTION_SUFFIX);
				Document asset = (Document) row.get("asset");
				temperatureDO.setAssetId(asset.getString("id"));
				temperatureDO.setAssetName(asset.getString("name"));
				Document deviceCategory = (Document) asset.get("device_category");
				if (deviceCategory != null) {
					Document device = (Document) deviceCategory.get("device");
					temperatureDO.setDeviceId(device.getString("id"));
					temperatureDO.setDeviceName(device.getString("name"));
				}
				/*
				 * if(lDeviceMap.get(temperatureDO.getCollectionName())==null){
				 * JSONArray array=new JSONArray();
				 * array.add(temperatureDO.getDeviceId());
				 * lDeviceMap.put(temperatureDO.getCollectionName(), array);
				 * }else{ JSONArray
				 * array=lDeviceMap.get(temperatureDO.getCollectionName());
				 * array.add(temperatureDO.getDeviceId());
				 * lDeviceMap.put(temperatureDO.getCollectionName(), array); }
				 * if(lAssetMap.get(temperatureDO.getCollectionName())==null){
				 * JSONArray array=new JSONArray();
				 * array.add(temperatureDO.getAssetId());
				 * lAssetMap.put(temperatureDO.getCollectionName(), array);
				 * }else{ JSONArray
				 * array=lAssetMap.get(temperatureDO.getCollectionName());
				 * array.add(temperatureDO.getAssetId());
				 * lAssetMap.put(temperatureDO.getCollectionName(), array); }
				 */
				lSiteInfoDocuments.add(temperatureDO);
			}
		}

		return lSiteInfoDocuments;
	}

	/**
	 * 
	 * @param settings
	 * @param outputWriter
	 */
	public static void generateEmptyHeaders(CsvWriterSettings settings, Writer outputWriter) {
		settings = new CsvWriterSettings();
		settings.setHeaders("AssetID", "DeviceID", "MeasureID", "DateTime", "C1", "C2", "C3", "C4", "C5", "C6", "C7",
				"C8", "C9", "C10");
		CsvWriter writer = new CsvWriter(outputWriter, settings);
		writer.writeHeaders();
		writer.close();
	}

	/**
	 * 
	 * @param element
	 * @param channelMapping
	 * @return
	 */
	private static String getChannelPositionByRemovingUnderScore(String element, Map<String, String> channelMapping) {
		if (element.indexOf("_") > 0) {
			return channelMapping.get(element.substring(element.indexOf("_") + 1, element.length()));
		}
		return "1";
	}

	public static Document createCommonDocumentMethod(BasicDBObject jsonDocument, MongoDatabase mongodb,
			String deviceCategoryName, String userGroupDetails,String gasParamJson) {

		MongoCollection<Document> table = null;
		BasicDBObject common_project = new BasicDBObject();
		Document searchQuery = new Document();
		BasicDBObject inQuery = new BasicDBObject();
		Document matchQuery = new Document();
		Document yearMonthQuery = new Document();
		Document matchYearMonthQuery = new Document();
		AggregateIterable<Document> iterable = null;
		Document projectQuery = new Document();
		Document finalDoc = new Document();
		ArrayList<String> dummyArrayListForDevice = new ArrayList<String>();
		ArrayList<String> dummyArrayListForChannels = new ArrayList<String>();

		if (!deviceCategoryName.isEmpty() && deviceCategoryName.equalsIgnoreCase("temperature")) {
			common_project.putAll(populateSelectedFields());
			projectQuery.put("$project", common_project);
			table = mongodb.getCollection(
					getDeviceCategoryNameShort(deviceCategoryName) + jsonDocument.getString("site_id") + MongoDBConstants.MONGO_COLLECTION_SUFFIX);
			if (jsonDocument.getString("year_month_day") != null
					&& jsonDocument.getString("year_month_day").length() > 0) {
				if (jsonDocument.getString("document_id") != null
						&& jsonDocument.getString("document_id").length() > 0) {
					searchQuery.put("_id", new ObjectId(jsonDocument.getString("document_id")));
				} else {
					searchQuery.put("asset_id", jsonDocument.getString("asset_id"));
					if (jsonDocument.get("device_id") != null
							&& ((ArrayList) jsonDocument.get("device_id")).size() > 0) {
						inQuery.put("$in", jsonDocument.get("device_id"));
						searchQuery.put(MongoDBConstants.DEVICE_ID, inQuery);
					}
				}
				matchQuery.put("$match", searchQuery);
				Map<String, Integer> getYearMonthQueryValues = populateYearMonthQuery(
						jsonDocument.getString("year_month_day"), "yyyy-MM-dd");
				String[] split = jsonDocument.getString("time").split(":");
				yearMonthQuery.put("measurement_date", new BasicDBObject("$eq",
						new DateTime(getYearMonthQueryValues.get("year"), getYearMonthQueryValues.get("monthNumber"),
								getYearMonthQueryValues.get("dateNumber"), Integer.valueOf(split[0]),
								Integer.valueOf(split[1]), Integer.valueOf(split[2]), DateTimeZone.UTC).toDate()));
				matchYearMonthQuery.put("$match", yearMonthQuery);
				iterable = table.aggregate(Arrays.asList(matchQuery, matchYearMonthQuery, projectQuery));
			} else {
				searchQuery.put("asset_id", jsonDocument.getString("asset_id"));
				if (jsonDocument.get("device_id") != null && ((ArrayList) jsonDocument.get("device_id")).size() > 0) {
					inQuery.put("$in", jsonDocument.get("device_id"));
					searchQuery.put(MongoDBConstants.DEVICE_ID, inQuery);
				}
				searchQuery.put("measurement_date",new BasicDBObject("$gte",frameQueryForDate(jsonDocument, "from_date")).append("$lte",frameQueryForDate(jsonDocument, "to_date")));
				matchQuery.put("$match", searchQuery);
				iterable = table.aggregate(Arrays.asList(matchQuery, projectQuery));

			}

			finalDoc.put("status", "Success");
			finalDoc.put("statusCode", 0);
			finalDoc.put("statusMessage", "getTimeSeriesData");
			finalDoc.put("timeSeries", constructJSONArray(iterable));
			MongoCollection<Document> locationHierachyTable = mongodb.getCollection("location_hierarchy");
			finalDoc.putAll((Map) ReportUtility.getChannelMapping(jsonDocument.getString("asset_id"),
					(ArrayList) jsonDocument.get("device_id"), locationHierachyTable, userGroupDetails));
			
		} else if (deviceCategoryName.equalsIgnoreCase("gas")) {
			table = mongodb.getCollection(
					getDeviceCategoryNameShort(deviceCategoryName) + jsonDocument.getString("site_id") + MongoDBConstants.MONGO_COLLECTION_SUFFIX);
			searchQuery.put("asset_id", jsonDocument.getString("asset_id"));
			if (jsonDocument.get("device_id") != null && ((ArrayList) jsonDocument.get("device_id")).size() > 0) {
				inQuery = new BasicDBObject();
				inQuery.put("$in", jsonDocument.get("device_id"));
				searchQuery.put(MongoDBConstants.DEVICE_ID, inQuery);
			}
			if (jsonDocument.get("channels") != null && ((ArrayList) jsonDocument.get("channels")).size() > 0) {
				inQuery = new BasicDBObject();
				inQuery.put("$in", jsonDocument.get("channels"));
				searchQuery.put("channel_id", inQuery);
			}
			searchQuery.put("message_time",new BasicDBObject("$gte",frameQueryForDate(jsonDocument, "from_date")).append("$lte",frameQueryForDate(jsonDocument, "to_date")));
			matchQuery.put("$match", searchQuery);
			finalDoc.put("status", "Success");
			finalDoc.put("statusCode", 0);
			finalDoc.put("statusMessage", "getTimeSeriesData");
			finalDoc.put("timeSeries", generateQueryForGas(matchQuery, table,gasParamJson));
			MongoCollection<Document> locationHierachyTable = mongodb.getCollection("location_hierarchy");
			finalDoc.putAll((Map) ReportUtility.getChannelMapping(jsonDocument.getString("asset_id"),
					(ArrayList) jsonDocument.get("device_id"), locationHierachyTable, userGroupDetails));

		} 
		else if (deviceCategoryName.equalsIgnoreCase("gasold")) {
			//table = mongodb.getCollection("g_"+jsonDocument.getString("site_id")+"_"+jsonDocument.getString("period"));
			table = mongodb.getCollection("g_"+jsonDocument.getString("site_id")+"_hour");
			searchQuery.put("asset_id", jsonDocument.getString("asset_id"));
			searchQuery.put("message_hour",new BasicDBObject("$gte",frameQueryForDate(jsonDocument, "from_date")).append("$lte",frameQueryForDate(jsonDocument, "to_date")));
			if (jsonDocument.get("device_id") != null && ((ArrayList) jsonDocument.get("device_id")).size() > 0) {
				inQuery = new BasicDBObject();
				inQuery.put("$in", jsonDocument.get("device_id"));
				searchQuery.put(MongoDBConstants.DEVICE_ID, inQuery);
			}
			if (jsonDocument.get("channels") != null && ((ArrayList) jsonDocument.get("channels")).size() > 0) {
				inQuery = new BasicDBObject();
				inQuery.put("$in", jsonDocument.get("channels"));
				searchQuery.put("channel_id", inQuery);
			}
			//matchQuery.put("$match", searchQuery);
			finalDoc.put("status", "Success");
			finalDoc.put("statusCode", 0);
			finalDoc.put("statusMessage", "getTimeSeriesData");
			finalDoc.put("timeSeries", generateQueryForGasNewStructure(searchQuery, table,jsonDocument.getString("period"),gasParamJson));
			MongoCollection<Document> locationHierachyTable = mongodb.getCollection(MongoDBConstants.MONGO_COLLECTION_LOCATION_HIERARCHY);
			finalDoc.putAll((Map) ReportUtility.getChannelMapping(jsonDocument.getString("asset_id"),
					(ArrayList) jsonDocument.get("device_id"), locationHierachyTable, userGroupDetails));

		}
		
		else if (deviceCategoryName.equalsIgnoreCase("Corrosion")) {
			dummyArrayListForDevice.add(jsonDocument.getString("device_id"));
			dummyArrayListForChannels.add(jsonDocument.getString("channels"));
			jsonDocument.remove("device_id");
			jsonDocument.remove("channels");
			jsonDocument.append("device_id", dummyArrayListForDevice);
			jsonDocument.append("channels",dummyArrayListForChannels);
			

			/************** TopChart ***********/
			if (jsonDocument.getString("year_month_day") == null && jsonDocument.getString("document_id") == null) {

				table = mongodb.getCollection(getDeviceCategoryNameShort(deviceCategoryName)
						+ jsonDocument.getString("site_id") + MongoDBConstants.MONGO_COLLECTION_SUFFIX);
				searchQuery.put("asset_id", jsonDocument.getString("asset_id"));
				if (jsonDocument.get("device_id") != null && ((ArrayList) jsonDocument.get("device_id")).size() > 0) {
					inQuery = new BasicDBObject();
					inQuery.put("$in", jsonDocument.get("device_id"));
					searchQuery.put(MongoDBConstants.DEVICE_ID, inQuery);
				}
				if (jsonDocument.get("channels") != null && ((ArrayList) jsonDocument.get("channels")).size() > 0) {
					inQuery = new BasicDBObject();
					inQuery.put("$in", jsonDocument.get("channels"));
					searchQuery.put("channel_id", inQuery);
				}
				searchQuery.put("message_time",new BasicDBObject("$gte",frameQueryForDate(jsonDocument, "from_date")).append("$lte",frameQueryForDate(jsonDocument, "to_date")));
				matchQuery.put("$match", searchQuery);
				finalDoc.put("status", "Success");
				finalDoc.put("statusCode", 0);
				finalDoc.put("statusMessage", "getTimeSeriesData");
				finalDoc.put("timeSeries", generateQueryForCorrosion(matchQuery, table, "top"));
				removeObjIdAndPutDocId(finalDoc, "Corrosion");
				MongoCollection<Document> locationHierachyTable = mongodb.getCollection("location_hierarchy");
				finalDoc.putAll((Map) ReportUtility.getChannelMapping(jsonDocument.getString("asset_id"),
						(ArrayList) jsonDocument.get("device_id"), locationHierachyTable, userGroupDetails));
			}
			/************** Bottom Chart *******/
			else {
				table = mongodb.getCollection(getDeviceCategoryNameShort(deviceCategoryName)
						+ jsonDocument.getString("site_id") + MongoDBConstants.MONGO_COLLECTION_SUFFIX);
				searchQuery.put("_id", new ObjectId(jsonDocument.getString("document_id")));
				matchQuery.put("$match", searchQuery);
				finalDoc.put("status", "Success");
				finalDoc.put("statusCode", 0);
				finalDoc.put("statusMessage", "getTimeSeriesData");
				finalDoc.put("timeSeries", generateQueryForCorrosion(matchQuery, table, "bottom"));
				corrosionBottomChartCalculations(finalDoc);
				MongoCollection<Document> locationHierachyTable = mongodb.getCollection("location_hierarchy");
				finalDoc.putAll((Map) ReportUtility.getChannelMapping(jsonDocument.getString("asset_id"),
						(ArrayList) jsonDocument.get("device_id"), locationHierachyTable, userGroupDetails));
				////sort ms_xaxis,ms_yaxis & range_xaxis,range_yaxis
				finalDoc = sortCorrosionDoc(finalDoc);
			}

			/************** Bottom Chart *******/

		}
		else if (deviceCategoryName.equalsIgnoreCase("Vibration")) {
			dummyArrayListForDevice.add(jsonDocument.getString("device_id"));
			dummyArrayListForChannels.add(jsonDocument.getString("channels"));
			jsonDocument.remove("device_id");
			jsonDocument.remove("channels");
			jsonDocument.append("device_id", dummyArrayListForDevice);
			jsonDocument.append("channels",dummyArrayListForChannels);			
			/************** TopChart Begins***********/
			if (jsonDocument.getString("year_month_day") == null && jsonDocument.getString("document_id") == null) {
				table = mongodb.getCollection(getDeviceCategoryNameShort(deviceCategoryName)
						+ jsonDocument.getString("site_id") + MongoDBConstants.MONGO_COLLECTION_SUFFIX);
				searchQuery.put("asset_id", jsonDocument.getString("asset_id"));
				if (jsonDocument.get("device_id") != null && ((ArrayList) jsonDocument.get("device_id")).size() > 0) {
					inQuery = new BasicDBObject();
					inQuery.put("$in", jsonDocument.get("device_id"));
					searchQuery.put(MongoDBConstants.DEVICE_ID, inQuery);
				}
				if (jsonDocument.get("channels") != null && ((ArrayList) jsonDocument.get("channels")).size() > 0) {
					inQuery = new BasicDBObject();
					inQuery.put("$in", jsonDocument.get("channels"));
					searchQuery.put("channel_id", inQuery);
				}
				searchQuery.put("message_time",new BasicDBObject("$gte",frameQueryForDate(jsonDocument, "from_date")).append("$lte",frameQueryForDate(jsonDocument, "to_date")));
				System.out.println("SearchQuery..."+searchQuery);
				matchQuery.put("$match", searchQuery);
				finalDoc.put("status", "Success");
				finalDoc.put("statusCode", 0);
				finalDoc.put("statusMessage", "getTimeSeriesData");
				List<Document> queryForVibration = generateQueryForVibration(matchQuery, table, "top",jsonDocument);
				finalDoc.put("timeSeries", queryForVibration);
				modifyTimeSeries(queryForVibration,finalDoc);
				removeObjIdAndPutDocId(finalDoc, "Vibration");
				MongoCollection<Document> locationHierachyTable = mongodb.getCollection("location_hierarchy");
				finalDoc.putAll((Map) ReportUtility.getChannelMapping(jsonDocument.getString("asset_id"),
						(ArrayList) jsonDocument.get("device_id"), locationHierachyTable, userGroupDetails));			
			}						
			/************** TopChart Ends ***********/
			/************** BottomChart Begins ***********/
			else{
				table = mongodb.getCollection(getDeviceCategoryNameShort(deviceCategoryName)
						+ jsonDocument.getString("site_id") + MongoDBConstants.MONGO_COLLECTION_SUFFIX);
				searchQuery.put("_id", new ObjectId(jsonDocument.getString("document_id")));
				matchQuery.put("$match", searchQuery);
				finalDoc.put("status", "Success");
				finalDoc.put("statusCode", 0);
				finalDoc.put("statusMessage", "getTimeSeriesData");
				finalDoc.put("timeSeries", generateQueryForVibration(matchQuery, table, "bottom", jsonDocument));
				vibrationBottomChartCalculations(finalDoc,jsonDocument);
				MongoCollection<Document> locationHierachyTable = mongodb.getCollection("location_hierarchy");
				finalDoc.putAll((Map) ReportUtility.getChannelMapping(jsonDocument.getString("asset_id"),
						(ArrayList) jsonDocument.get("device_id"), locationHierachyTable, userGroupDetails));
				////sort ms_xaxis,ms_yaxis & range_xaxis,range_yaxis
				//finalDoc = sortCorrosionDoc(finalDoc);
			}
			/************** BottomChart Ends ***********/
			
		}
		return finalDoc;
	}
	
	/**
	 * @param queryForVibration
	 * @param finalDoc
	 */
	@SuppressWarnings("unchecked")
	private static void modifyTimeSeries(List<Document> queryForVibration,Document finalDoc) {
		List<Document> channels = null;
		if(CollectionUtils.isNotEmpty(queryForVibration)) {
			for(Document doc: queryForVibration) {
				channels = (List<Document>) doc.get("channels");
				if(CollectionUtils.isNotEmpty(channels)) {
					for(Document channelDoc: channels) {
						System.out.println("pp_env_alert "+channelDoc.toJson());
						if(channelDoc.get("rms_accel_alarm")!=null)
						finalDoc.put("rms_accel_alarm", channelDoc.get("rms_accel_alarm"));
						if(channelDoc.getDouble("rms_accel_alert")!=null)
						finalDoc.put("rms_accel_alert", channelDoc.getDouble("rms_accel_alert"));
						if(channelDoc.getDouble("rms_vel_alarm")!=null)
						finalDoc.put("rms_vel_alarm", channelDoc.getDouble("rms_vel_alarm"));
						if(channelDoc.getDouble("rms_vel_alert")!=null)
						finalDoc.put("rms_vel_alert", channelDoc.getDouble("rms_vel_alert"));
						if(channelDoc.getDouble("rms_env_alarm")!=null)
						finalDoc.put("rms_env_alarm", channelDoc.getDouble("rms_env_alarm"));
						if(channelDoc.getDouble("rms_env_alert")!=null)
						finalDoc.put("rms_env_alert", channelDoc.getDouble("rms_env_alert"));
						if(channelDoc.getDouble("pp_accel_alarm")!=null)
						finalDoc.put("pp_accel_alarm", channelDoc.getDouble("pp_accel_alarm"));
						if(channelDoc.get("pp_accel_alert")!=null)
						finalDoc.put("pp_accel_alert", channelDoc.get("pp_accel_alert"));
						if(channelDoc.getDouble("pp_vel_alarm")!=null)
						finalDoc.put("pp_vel_alarm", channelDoc.getDouble("pp_vel_alarm"));
						if(channelDoc.get("pp_vel_alert")!=null)
						finalDoc.put("pp_vel_alert", channelDoc.get("pp_vel_alert"));
						if(channelDoc.get("pp_env_alert")!=null)
						finalDoc.put("pp_env_alert", channelDoc.get("pp_env_alert"));
						if(channelDoc.getDouble("pp_env_alarm")!=null)
						finalDoc.put("pp_env_alarm", channelDoc.getDouble("pp_env_alarm"));
						//removing values from the channel level
						channelDoc.remove("rms_accel_alarm");
						channelDoc.remove("rms_accel_alert");
						channelDoc.remove("rms_vel_alarm");
						channelDoc.remove("rms_vel_alert");
						channelDoc.remove("rms_env_alarm");
						channelDoc.remove("rms_env_alert");
						channelDoc.remove("pp_accel_alarm");
						channelDoc.remove("pp_accel_alert");
						channelDoc.remove("pp_vel_alarm");
						channelDoc.remove("pp_vel_alert");
						channelDoc.remove("pp_env_alert");
						channelDoc.remove("pp_env_alarm");
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param table
	 * @param deviceCategoryName
	 * @param deviceId
	 * @return
	 */
	public static Document getUIParametersForCorrosion(MongoCollection<Document> table, String deviceCategoryName,BasicDBObject jsonDocument,String channelId) {
		channelId="channel_"+channelId.substring(channelId.indexOf("_")+1, channelId.length());
		Document searchQuery = new Document();
		BasicDBObject inQuery = new BasicDBObject();
		Document matchQuery = new Document();
		AggregateIterable<Document> iterable = null;
		Document projectQuery = new Document();
		inQuery = new BasicDBObject();
		inQuery.put("$in", jsonDocument.get("device_id"));
		searchQuery.put(MongoDBConstants.DEVICE_ID, inQuery);
		searchQuery.put("active_flag",true);
		Document projectFields=new Document();
		//projectFields.put("min_y_axis","$ui_parameters."+channelId+".min_y_axis");
		//projectFields.put("max_y_axis","$ui_parameters."+channelId+".max_y_axis");
		projectFields.put("min_y_axis","$channel_parameters."+channelId+".min_y_axis");
		projectFields.put("max_y_axis","$channel_parameters."+channelId+".max_y_axis");
		projectQuery.put("$project", projectFields);
		matchQuery.put("$match", searchQuery);
		iterable = table.aggregate(Arrays.asList(matchQuery,projectQuery)); 
		 for(Document row:iterable){
			row.remove("_id");
			return row;
		 }
		return projectFields;
	}

	
	/**
	 * 
	 * @param matchQuery
	 * @param table
	 * @param type
	 * @return
	 */
	private static List<Document> generateQueryForVibration(Document matchQuery, MongoCollection<Document> table,String type,BasicDBObject jsonDocument) {		
		if (type.equalsIgnoreCase("top")) {			
			System.out.println("Top chart generateQueryForVibration");
			Document pushDocument = new Document();
			if(jsonDocument.getString("type")!=null && jsonDocument.getString("sub_type")!=null){							
				pushDocument.put("channel_id", "$channel_id");
				pushDocument.put("doc_id", "$_id");
				pushDocument.put("rms_accel", "$rms_accel");
				pushDocument.put("rms_vel", "$rms_vel");
				pushDocument.put("rms_env", "$rms_env");
				pushDocument.put("pp_accel", "$pp_accel");
				pushDocument.put("pp_vel", "$pp_vel");
				pushDocument.put("pp_env", "$pp_env");
				pushDocument.put("rms_accel_cmnts", "$rms_accel_cmnts");
				pushDocument.put("rms_vel_cmnts", "$rms_vel_cmnts");
				pushDocument.put("rms_env_cmnts", "$rms_env_cmnts");
				pushDocument.put("pp_accel_cmnts", "$pp_accel_cmnts");
				pushDocument.put("pp_vel_cmnts", "$pp_vel_cmnts");
				pushDocument.put("pp_env_cmnts", "$pp_env_cmnts");
				
				pushDocument.put("pp_accel_x", "$pp_accel_x");
				pushDocument.put("pp_accel_y", "$pp_accel_y");
				pushDocument.put("pp_accel_z", "$pp_accel_z");
				pushDocument.put("pp_vel_x", "$pp_vel_x");
				pushDocument.put("pp_vel_y", "$pp_vel_y");
				pushDocument.put("pp_vel_z", "$pp_vel_z");
				pushDocument.put("rms_accel_x", "$rms_accel_x");
				pushDocument.put("rms_accel_y", "$rms_accel_y");
				pushDocument.put("rms_accel_z", "$rms_accel_z");
				pushDocument.put("rms_vel_x", "$rms_vel_x");
				pushDocument.put("rms_vel_y", "$rms_vel_y");
				pushDocument.put("rms_vel_z", "$rms_vel_z");
			}else if(jsonDocument.getString("type").equalsIgnoreCase("rms") && jsonDocument.getString("sub_type").equalsIgnoreCase("All")){
				pushDocument.put("channel_id", "$channel_id");
				pushDocument.put("rms_accel", "$rms_accel");
				pushDocument.put("doc_id", "$_id");
				pushDocument.put("rms_vel", "$rms_vel");
				pushDocument.put("rms_env", "$rms_env");
				pushDocument.put("rms_accel_cmnts", "$rms_accel_cmnts");
				pushDocument.put("rms_vel_cmnts", "$rms_vel_cmnts");
				pushDocument.put("rms_env_cmnts", "$rms_env_cmnts");
			}
			
			pushDocument.put("rms_accel_alarm", "$rms_accel_alarm");
			pushDocument.put("rms_accel_alert", "$rms_accel_alert");
			pushDocument.put("rms_vel_alarm", "$rms_vel_alarm");
			pushDocument.put("rms_vel_alert", "$rms_vel_alert");
			pushDocument.put("rms_env_alarm", "$rms_env_alarm");
			pushDocument.put("rms_env_alert", "$rms_env_alert");
			pushDocument.put("pp_accel_alarm", "$pp_accel_alarm");
			pushDocument.put("pp_accel_alert", "$pp_accel_alert");
			pushDocument.put("pp_vel_alarm", "$pp_vel_alarm");
			pushDocument.put("pp_vel_alert", "$pp_vel_alert");
			pushDocument.put("pp_env_alarm", "$pp_env_alarm");
			pushDocument.put("pp_env_alert", "$pp_env_alert");
			pushDocument.put("temperature", "$temperature");
			
			Document groupByQuery = new Document();
			groupByQuery.put("asset_id", "$asset_id");
			groupByQuery.put("device_id", "$device_id");
			groupByQuery.put("message_time", "$message_time");
			
			Document projectQuery = new Document();
			Document project = new Document();
			project.put("asset_id", "$_id.asset_id");
			project.put("device_id", "$_id.device_id");
						
			Document dateConversion = new Document(); 
			Document dateFormatDoc = new Document();
			dateFormatDoc.put("format", "%Y-%m-%d");
			dateFormatDoc.put("date", "$_id.message_time");
			dateConversion.put("$dateToString", dateFormatDoc);
			project.put("yearMonthDay", dateConversion);
			
			Document timeConversion = new Document();
			Document timeConversionDoc = new Document();
			timeConversionDoc.put("format", "%H:%M:%S");
			timeConversionDoc.put("date", "$_id.message_time");
			timeConversion.put("$dateToString", timeConversionDoc);
			project.put("time", timeConversion);
			project.put("channels", "$channels");
			project.put("_id", 0);
			projectQuery.put("$project", project);
			Document group = new Document("$group",
					new Document("_id", groupByQuery).append("channels", new Document("$push", pushDocument)));						
			AggregateIterable<Document> iterable = table.aggregate(Arrays.asList(matchQuery,group, projectQuery));								
			List<Document> timeSeriesArray = new ArrayList<Document>();
			for (Document row : iterable) {
				
				timeSeriesArray.add(row);
			}
			System.out.println("timeSeriesArray: "+timeSeriesArray);
			sortDataBasedOnMessageTime(timeSeriesArray);			
			return timeSeriesArray;
			
		}
		if (type.equalsIgnoreCase("bottom")) {
			System.out.println("bottom chart generateQueryForVibration");
			Document pushDocument = new Document();
			pushDocument.put("channel_id", "$channel_id");
			pushDocument.put("doc_id", "$_id");
			pushDocument.put("accel_fft", "$accel_fft");
			pushDocument.put("accel_timebase", "$accel_timebase");
			pushDocument.put("accel_fft_x_axis", "$accel_fft_x_axis");
			pushDocument.put("accel_timebase_x_axis", "$accel_timebase_x_axis");
			pushDocument.put("accel_fft_peak_x_axis", "$accel_fft_peak_x_axis");
			pushDocument.put("vel_fft", "$vel_fft");
			pushDocument.put("vel_timebase", "$vel_timebase");
			pushDocument.put("vel_fft_x_axis", "$vel_fft_x_axis");
			pushDocument.put("vel_timebase_x_axis", "$vel_timebase_x_axis");
			pushDocument.put("vel_fft_peak_x_axis", "$vel_fft_peak_x_axis");
			pushDocument.put("env_fft", "$env_fft");
			pushDocument.put("env_timebase", "$env_timebase");
			pushDocument.put("env_fft_x_axis", "$env_fft_x_axis");
			pushDocument.put("env_timebase_x_axis", "$env_timebase_x_axis");
			pushDocument.put("env_fft_peak_x_axis", "$env_fft_peak_x_axis");
			if(jsonDocument.getString("x")!=null){
				pushDocument.put("x_accel_fft_x_axis", "$x_accel_fft_x_axis");
				pushDocument.put("x_accel_fft_peak_x_axis", "$x_accel_fft_peak_x_axis");
				pushDocument.put("x_vel_fft_peak_x_axis", "$x_vel_fft_peak_x_axis");
				pushDocument.put("x_vel_fft_peak_x_axis", "$x_vel_fft_peak_x_axis");
			}
			if(jsonDocument.getString("y")!=null){
				pushDocument.put("y_accel_fft_x_axis", "$y_accel_fft_x_axis");
				pushDocument.put("y_accel_fft_peak_x_axis", "$y_accel_fft_peak_x_axis");
				pushDocument.put("y_vel_fft_peak_x_axis", "$y_vel_fft_peak_x_axis");
				pushDocument.put("y_vel_fft_peak_x_axis", "$y_vel_fft_peak_x_axis");
			}
			if(jsonDocument.getString("z")!=null) {
				pushDocument.put("z_accel_fft_x_axis", "$z_accel_fft_x_axis");
				pushDocument.put("z_accel_fft_peak_x_axis", "$z_accel_fft_peak_x_axis");
				pushDocument.put("z_vel_fft_x_axis", "$z_vel_fft_x_axis");
				pushDocument.put("z_vel_fft_peak_x_axis", "$z_vel_fft_peak_x_axis");
			}
			
			Document groupByQuery = new Document();
			groupByQuery.put("asset_id", "$asset_id");
			groupByQuery.put("device_id", "$device_id");
			groupByQuery.put("message_time", "$message_time");

			Document projectQuery = new Document();
			Document project = new Document();
			project.put("asset_id", "$_id.asset_id");
			project.put("device_id", "$_id.device_id");
			// project.put("message_time", "$_id.message_time");

			Document dateConversion = new Document();
			Document dateFormatDoc = new Document();
			dateFormatDoc.put("format", "%Y-%m-%d");
			dateFormatDoc.put("date", "$_id.message_time");
			dateConversion.put("$dateToString", dateFormatDoc);
			project.put("yearMonthDay", dateConversion);

			Document timeConversion = new Document();
			Document timeConversionDoc = new Document();
			timeConversionDoc.put("format", "%H:%M:%S");
			timeConversionDoc.put("date", "$_id.message_time");
			timeConversion.put("$dateToString", timeConversionDoc);
			project.put("time", timeConversion);
			project.put("channels", "$channels");
			project.put("_id", 0);
			projectQuery.put("$project", project);

			Document group = new Document("$group",
					new Document("_id", groupByQuery).append("channels", new Document("$push", pushDocument)));
			AggregateIterable<Document> iterable = table.aggregate(Arrays.asList(matchQuery, group, projectQuery));
			List<Document> timeSeriesArray = new ArrayList<Document>();
			for (Document row : iterable) {
				timeSeriesArray.add(row);
			}
			return timeSeriesArray;
			
		}
		return null;
	}

	/***
	 * 
	 * @param matchQuery
	 * @param table
	 * @return
	 */
	private static List<Document> generateQueryForCorrosion(Document matchQuery, MongoCollection<Document> table,
			String type) {
		if (type.equalsIgnoreCase("top")) {
			Document pushDocument = new Document();
			pushDocument.put("channel_id", "$channel_id");
			pushDocument.put("doc_id", "$_id");
			pushDocument.put("thickness_maxpeak", "$thickness_maxpeak");
			pushDocument.put("thickness", "$thickness");
			pushDocument.put("temperature", "$external_temperature");
			pushDocument.put("temperature_corrected_thickness", "$temperature_corrected_thickness");
			pushDocument.put("thickness_flank", "$thickness_flank");
			pushDocument.put("temperature_corrected_thickness_flank", "$temperature_corrected_thickness_flank");
			pushDocument.put("thickness_zerocrossing", "$thickness_zerocrossing");
			pushDocument.put("temperature_corrected_thickness_zerocrossing",
					"$temperature_corrected_thickness_zerocrossing");
			pushDocument.put("thickness_algorithm", "$thickness_algorithm");
			pushDocument.put("thickness_cmnts", "$thickness_cmnts");
			pushDocument.put("tc_thickness_cmnts", "$tc_thickness_cmnts");
			pushDocument.put("thickness_maxpeakzerocrossing","$thickness_maxpeakzerocrossing");
			pushDocument.put("thickness_firstzerocrossing","$thickness_firstzerocrossing");

			

			Document groupByQuery = new Document();
			groupByQuery.put("asset_id", "$asset_id");
			groupByQuery.put("device_id", "$device_id");
			groupByQuery.put("message_time", "$message_time");

			Document projectQuery = new Document();
			Document project = new Document();
			project.put("asset_id", "$_id.asset_id");
			project.put("device_id", "$_id.device_id");
			// project.put("message_time", "$_id.message_time");
			
			Document dateConversion = new Document(); 
			Document dateFormatDoc = new Document();
			dateFormatDoc.put("format", "%Y-%m-%d");
			dateFormatDoc.put("date", "$_id.message_time");
			dateConversion.put("$dateToString", dateFormatDoc);
			project.put("yearMonthDay", dateConversion);

			Document timeConversion = new Document();
			Document timeConversionDoc = new Document();
			timeConversionDoc.put("format", "%H:%M:%S");
			timeConversionDoc.put("date", "$_id.message_time");
			timeConversion.put("$dateToString", timeConversionDoc);
			project.put("time", timeConversion);
			project.put("channels", "$channels");
			project.put("_id", 0);
			projectQuery.put("$project", project);

			Document group = new Document("$group",
					new Document("_id", groupByQuery).append("channels", new Document("$push", pushDocument)));						
			AggregateIterable<Document> iterable = table.aggregate(Arrays.asList(matchQuery,group, projectQuery));								
			List<Document> timeSeriesArray = new ArrayList<Document>();
			for (Document row : iterable) {
				timeSeriesArray.add(row);
			}
			sortDataBasedOnMessageTime(timeSeriesArray);			
			return timeSeriesArray;
		}
		if (type.equalsIgnoreCase("bottom")) {
			Document pushDocument = new Document();
			pushDocument.put("channel_id", "$channel_id");
			pushDocument.put("doc_id", "$_id");
			pushDocument.put("gate_a_height", "$gate_a_height");
			pushDocument.put("gate_a_start", "$gate_a_start");
			pushDocument.put("gate_a_length", "$gate_a_length");
			pushDocument.put("gate_b_height", "$gate_b_height");
			pushDocument.put("gate_b_start", "$gate_b_start");
			pushDocument.put("gate_b_length", "$gate_b_length");
			pushDocument.put("gate_a_height", "$gate_a_height");
			pushDocument.put("gate_a_height", "$gate_a_height");
			pushDocument.put("thickness_algorithm", "$thickness_algorithm");
			pushDocument.put("part_material_velocity", "$part_material_velocity");
			pushDocument.put("ms_yaxis", "$ascan");
			pushDocument.put("ms_xaxis", "$xascan");
			pushDocument.put("peak_1_y", "$peak_1_y");
			pushDocument.put("peak_2_y", "$peak_2_y");
			pushDocument.put("peak_1_x", "$peak_1_x");
			pushDocument.put("peak_2_x", "$peak_2_x");

			Document groupByQuery = new Document();
			groupByQuery.put("asset_id", "$asset_id");
			groupByQuery.put("device_id", "$device_id");
			groupByQuery.put("message_time", "$message_time");

			Document projectQuery = new Document();
			Document project = new Document();
			project.put("asset_id", "$_id.asset_id");
			project.put("device_id", "$_id.device_id");
			// project.put("message_time", "$_id.message_time");

			Document dateConversion = new Document();
			Document dateFormatDoc = new Document();
			dateFormatDoc.put("format", "%Y-%m-%d");
			dateFormatDoc.put("date", "$_id.message_time");
			dateConversion.put("$dateToString", dateFormatDoc);
			project.put("yearMonthDay", dateConversion);

			Document timeConversion = new Document();
			Document timeConversionDoc = new Document();
			timeConversionDoc.put("format", "%H:%M:%S");
			timeConversionDoc.put("date", "$_id.message_time");
			timeConversion.put("$dateToString", timeConversionDoc);
			project.put("time", timeConversion);
			project.put("channels", "$channels");
			project.put("_id", 0);
			projectQuery.put("$project", project);

			Document group = new Document("$group",
					new Document("_id", groupByQuery).append("channels", new Document("$push", pushDocument)));
			AggregateIterable<Document> iterable = table.aggregate(Arrays.asList(matchQuery, group, projectQuery));
			List<Document> timeSeriesArray = new ArrayList<Document>();
			for (Document row : iterable) {
				timeSeriesArray.add(row);
			}
			return timeSeriesArray;
		}
		return null;
	}
	
	/***
	 * 
	 * @param matchQuery
	 * @param table
	 * @return
	 */
	private static List<Document> generateQueryForGasNewStructure(Document matchQuery, MongoCollection<Document> table,
			String period, String gasParamJson) {
		BasicDBObject gasParamJsonDoc = null;
		BasicDBList gasParamArr = null;
		if (gasParamJson != null) {
			gasParamJsonDoc = (BasicDBObject) JSON.parse(gasParamJson);
			gasParamArr = (BasicDBList) gasParamJsonDoc.get("gasParamJson");
		}
		Document projectQuery = new Document();
		if (gasParamArr != null) {
			if (period != null && period.equalsIgnoreCase("minute")) {
				updateProjectQueryForGas(projectQuery, gasParamArr, "minute");
			} else if (period != null && period.equalsIgnoreCase("fifteenminute")) {
				updateProjectQueryForGas(projectQuery, gasParamArr, "fifteenminute");
			} else if (period != null && period.equalsIgnoreCase("hour")) {
				updateProjectQueryForGas(projectQuery, gasParamArr, "hour");
			}
			projectQuery.put("gas_codes", 1);
			projectQuery.put("message_hour", 1);
			projectQuery.put("gas_cmnts", 1);
		} else {
			if (period != null && period.equalsIgnoreCase("minute")) {
				projectQuery.put("gas_codes", 1);
				projectQuery.put("message_hour", 1);
				projectQuery.put("gas_full_scale_multiplier_per_minute", 1);
				projectQuery.put("temperature_minute", 1);
				projectQuery.put("gas_cmnts", 1);
			} else if (period != null && period.equalsIgnoreCase("fifteenminute")) {
				projectQuery.put("gas_codes", 1);
				projectQuery.put("message_hour", 1);
				projectQuery.put("gas_full_scale_multiplier_per_fifteen_minute", 1);
				projectQuery.put("temperature_per_fifteen_minute", 1);
				projectQuery.put("gas_cmnts", 1);
			} else if (period != null && period.equalsIgnoreCase("hour")) {
				projectQuery.put("gas_codes", 1);
				projectQuery.put("message_hour", 1);
				projectQuery.put("gas_full_scale_multiplier_per_hour", 1);
				projectQuery.put("temperature_per_hour", 1);
				projectQuery.put("gas_cmnts", 1);
			}
		}
		Document sortQueryObject = new Document();
		sortQueryObject.put("message_hour", MongoDBConstants.MIN_ORDER_BY);
		List<Document> timeSeriesArray = new ArrayList<Document>();
		FindIterable<Document> sessioncursor = table.find(matchQuery).projection(projectQuery).sort(sortQueryObject);
		MongoCursor<Document> itr = sessioncursor.iterator();
		while (itr.hasNext()) {
			Document object = (Document) itr.next();
			timeSeriesArray.add(object);
		}
		itr.close();
		return timeSeriesArray;
	}
	
	/**
	 * @param projectQuery
	 * @param gasParamArr
	 */
	private static void updateProjectQueryForGas(Document projectQuery,BasicDBList gasParamArr,String period){
		for(String str:gasParamArr.keySet()) {
			if("Temperature".equalsIgnoreCase(gasParamArr.get(str).toString())) {
				if("minute".equalsIgnoreCase(period)) {
					projectQuery.put("temperature_minute", 1);
				}else if("fifteenminute".equalsIgnoreCase(period)) {
					projectQuery.put("temperature_fifteenminute", 1);
				}else if("hour".equalsIgnoreCase(period)) {
					projectQuery.put("temperature_hour", 1);
				}
			}else if("Humidity".equalsIgnoreCase(gasParamArr.get(str).toString())) {
				if("minute".equalsIgnoreCase(period)) {
					projectQuery.put("humidity_full_scale_multiplier_minute", 1);
				}else if("fifteenminute".equalsIgnoreCase(period)) {
					projectQuery.put("humidity_full_scale_multiplier_fifteenminute", 1);
				}else if("hour".equalsIgnoreCase(period)) {
					projectQuery.put("humidity_full_scale_multiplier_hour", 1);
				}
			}else if("mV".equalsIgnoreCase(gasParamArr.get(str).toString())) {
				if("minute".equalsIgnoreCase(period)) {
					projectQuery.put("mv_full_scale_multiplier_minute", 1);
				}else if("fifteenminute".equalsIgnoreCase(period)) {
					projectQuery.put("mv_full_scale_multiplier_fifteenminute", 1);
				}else if("hour".equalsIgnoreCase(period)) {
					projectQuery.put("mv_full_scale_multiplier_hour", 1);
				}
			}
		}
	}
	
	/***
	 * 
	 * @param matchQuery
	 * @param table
	 * @return
	 */
	private static List<Document> generateQueryForGas(Document matchQuery, MongoCollection<Document> table,
			String gasParamJson) {
		BasicDBObject gasParamJsonDoc = null;
		BasicDBList gasParamArr = null;
		if (gasParamJson != null) {
			gasParamJsonDoc = (BasicDBObject) JSON.parse(gasParamJson);
			gasParamArr = (BasicDBList) gasParamJsonDoc.get("gasParamJson");
		}
		Document pushDocument = new Document();
		pushDocument.put("channel_id", "$channel_id");
		pushDocument.put("gas_codes", "$gas_codes");
		if (gasParamArr != null) {
			for (String str : gasParamArr.keySet()) {
				if ("Temperature".equalsIgnoreCase(gasParamArr.get(str).toString())) {
					pushDocument.put("temperature", "$temperature");
				} else if ("Humidity".equalsIgnoreCase(gasParamArr.get(str).toString())) {
					pushDocument.put("humidity_full_scale_multiplier", "$humidity_full_scale_multiplier");
				} else if ("mV".equalsIgnoreCase(gasParamArr.get(str).toString())) {
					pushDocument.put("mv_full_scale_multiplier", "$mv_full_scale_multiplier");
				}
			}
		} else {
			pushDocument.put("gas_full_scale_multiplier", "$gas_full_scale_multiplier");
		}
		pushDocument.put("temperature", "$temperature");
		pushDocument.put("doc_id", "$_id");
		pushDocument.put("gas_cmnts", "$gas_cmnts");
		Document groupByQuery = new Document();
		groupByQuery.put("asset_id", "$asset_id");
		groupByQuery.put("device_id", "$device_id");
		groupByQuery.put("message_time", "$message_time");

		Document projectQuery = new Document();
		Document project = new Document();
		project.put("asset_id", "$_id.asset_id");
		project.put("device_id", "$_id.device_id");
		project.put("message_time", "$_id.message_time");

		Document dateConversion = new Document();
		Document dateFormatDoc = new Document();
		dateFormatDoc.put("format", "%Y-%m-%d");
		dateFormatDoc.put("date", "$_id.message_time");
		dateConversion.put("$dateToString", dateFormatDoc);
		project.put("yearMonthDay", dateConversion);

		Document timeConversion = new Document();
		Document timeConversionDoc = new Document();
		timeConversionDoc.put("format", "%H:%M:%S");
		timeConversionDoc.put("date", "$_id.message_time");
		timeConversion.put("$dateToString", timeConversionDoc);
		project.put("time", timeConversion);
		project.put("channels", "$channels");
		project.put("_id", 0);
		projectQuery.put("$project", project);

		Document group = new Document("$group",
				new Document("_id", groupByQuery).append("channels", new Document("$push", pushDocument)));
		AggregateIterable<Document> iterable = table.aggregate(Arrays.asList(matchQuery, group, projectQuery))
				.allowDiskUse(true);
		List<Document> timeSeriesArray = new ArrayList<Document>();
		for (Document row : iterable) {
			timeSeriesArray.add(row);
		}
		return timeSeriesArray;
	}

	/**
	 * 
	 * @param deviceCategoryName
	 * @return
	 */
	private static String getDeviceCategoryNameShort(String deviceCategoryName) {
		HashMap<String, String> hDeviceCategoryName = new HashMap<String, String>();
		hDeviceCategoryName.put("Vibration", "v_");
		hDeviceCategoryName.put("Temperature", "t_");
		hDeviceCategoryName.put("Corrosion", "c_");
		hDeviceCategoryName.put("Mcems", "c_");
		hDeviceCategoryName.put("Gas", "g_");
		hDeviceCategoryName.put("Temperature_new", "t_");
		return hDeviceCategoryName.get(deviceCategoryName);
	}

	/**
	 * 
	 * @return
	 */
	private static Document populateSelectedFields() {
		Document selectedFields = new Document("_id", 1);
		selectedFields.put("_id", 1);
		selectedFields.put("asset_id", 1);
		selectedFields.put("device_id", 1);
		selectedFields.put("measurement_date", 1);
		selectedFields.put("temperature", 1);
		return selectedFields;
	}

	/**
	 * 
	 * @param dateTime
	 * @param format
	 * @return
	 */
	private static Map<String, Integer> populateYearMonthQuery(String dateTime, String format) {
		Map<String, Integer> yearMonthQuery = new HashMap<String, Integer>();
		Calendar cal = Calendar.getInstance();
		Date yearMonthDate = parseDateTimeFromJson(dateTime, format);

		cal.setTime(yearMonthDate);
		int year = cal.get(Calendar.YEAR);
		int monthNumber = cal.get(Calendar.MONTH);
		int dateNumber = cal.get(Calendar.DAY_OF_MONTH);
		monthNumber += 1;

		yearMonthQuery.put("year", year);
		yearMonthQuery.put("monthNumber", monthNumber);
		yearMonthQuery.put("dateNumber", dateNumber);
		return yearMonthQuery;
	}

	/**
	 * 
	 * @param dateTime
	 * @param format
	 * @return
	 */
	private static Date parseDateTimeFromJson(String dateTime, String format) {
		Date parsedDate = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		try {
			parsedDate = simpleDateFormat.parse(dateTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parsedDate;
	}

	/**
	 * 
	 * @param iterable
	 * @return
	 */
	private static JSONArray constructJSONArray(AggregateIterable<Document> iterable) {
		JSONArray array = new JSONArray();
		String nowAsISO = "";
		DateFormat df = CommonUtility.getDateFormat("Year");
		for (Document row : iterable) {
			row.put("doc_id", row.get("_id").toString());
			nowAsISO = df.format(row.getDate("measurement_date"));
			row.put("yearMonthDay", nowAsISO.substring(0, nowAsISO.indexOf("T")));
			row.put("time", nowAsISO.substring(nowAsISO.indexOf("T") + 1, nowAsISO.length() - 1));
			row.remove("measurement_date");
			row.remove("_id");
			array.add(row);
		}
		return array;
	}

	/**
	 * 
	 * @param iterable
	 * @return
	 */
	public static JSONArray constructGasJSONArray(AggregateIterable<Document> iterable) {
		JSONArray array = new JSONArray();
		String nowAsISO = "";
		DateFormat df = CommonUtility.getDateFormat("Year");
		for (Document row : iterable) {
			row.put("doc_id", row.get("_id").toString());
			nowAsISO = df.format(row.getDate("message_time"));
			row.put("yearMonthDay", nowAsISO.substring(0, nowAsISO.indexOf("T")));
			row.put("time", nowAsISO.substring(nowAsISO.indexOf("T") + 1, nowAsISO.length() - 1));
			row.remove("message_time");
			row.remove("_id");
			array.add(row);
		}
		return array;

	}

	/**
	 * 
	 * @param doc
	 */
	private static void removeObjIdAndPutDocId(Document doc, String deviceCategoryName) {
		List<Document> getTimeSeriesArray = new ArrayList<Document>();
		getTimeSeriesArray = (List<Document>) doc.get("timeSeries");
		for (Document getTimeSeries : getTimeSeriesArray) {
			List<Document> getChannelArray = new ArrayList<Document>();
			getChannelArray = (List<Document>) getTimeSeries.get("channels");
			for (Document getIndividualChannel : getChannelArray) {
				if(deviceCategoryName.equalsIgnoreCase("Corrosion")){
				if(getIndividualChannel.get("temperature") instanceof String) {
					getIndividualChannel.remove("temperature");
				}
				String thickness_algorithm = (String) getIndividualChannel.get("thickness_algorithm");
				removeUnwantedTemperatureValues(getIndividualChannel, thickness_algorithm);
				Object docId = getIndividualChannel.get("doc_id");
				String objId = docId.toString();
				getIndividualChannel.remove("doc_id");
				getIndividualChannel.append("doc_id", objId);
				}
				else if(deviceCategoryName.equalsIgnoreCase("Vibration")){
					Object docId = getIndividualChannel.get("doc_id");
					String objId = docId.toString();
					getIndividualChannel.remove("doc_id");
					getIndividualChannel.append("doc_id", objId);
				}
			}
		}
	}

	/**
	 * 
	 * @param getIndividualChannel
	 * @param thickness_algorithm
	 */
	private static void removeUnwantedTemperatureValues(Document getIndividualChannel, String thickness_algorithm) {
		if (thickness_algorithm.equalsIgnoreCase("Max Peak")) {
			Double thickness = (Double) getIndividualChannel.get("thickness_maxpeak");
			Double temperature_corrected_thickness = (Double) getIndividualChannel
					.get("temperature_corrected_thickness");
			deleteThicknessValFromFinalDoc(getIndividualChannel);
			if (thickness == null) {
				getIndividualChannel.append("thickness", null);
			} else {
				getIndividualChannel.append("thickness", thickness);
			}
			if (temperature_corrected_thickness == null) {
				getIndividualChannel.append("temperature_corrected_thickness", null);
			} else {
				getIndividualChannel.append("temperature_corrected_thickness", temperature_corrected_thickness);
			}
		}
		if (thickness_algorithm.equalsIgnoreCase("Flank")) {
			Double thickness = getIndividualChannel.getDouble("thickness_flank");
			Double temperature_corrected_thickness = getIndividualChannel
					.getDouble("temperature_corrected_thickness");
			deleteThicknessValFromFinalDoc(getIndividualChannel);
			if (thickness == null) {
				getIndividualChannel.append("thickness", null);
			} else {
				getIndividualChannel.append("thickness", thickness);
			}
			if (temperature_corrected_thickness == null) {
				getIndividualChannel.append("temperature_corrected_thickness", null);
			} else {
				getIndividualChannel.append("temperature_corrected_thickness", temperature_corrected_thickness);
			}
		}
		/*if (thickness_algorithm.equalsIgnoreCase("Zero Crossing")) {
			Double thickness = getIndividualChannel.getDouble("thickness_zerocrossing");
			Double temperature_corrected_thickness = getIndividualChannel
					.getDouble("temperature_corrected_thickness");
			deleteThicknessValFromFinalDoc(getIndividualChannel);
			if (thickness == null) {
				getIndividualChannel.append("thickness", null);
			} else {
				getIndividualChannel.append("thickness", thickness);
			}

			if (temperature_corrected_thickness == null) {
				getIndividualChannel.append("temperature_corrected_thickness", null);
			} else {
				getIndividualChannel.append("temperature_corrected_thickness", temperature_corrected_thickness);
			}
		}*/
		if (thickness_algorithm.equalsIgnoreCase("Device Thickness")) {
			Double thickness =0.0d;
			if(getIndividualChannel.get("thickness") instanceof String) {
				String getThickness = (String) getIndividualChannel.get("thickness");
				thickness = Double.parseDouble(getThickness);
			}else if(getIndividualChannel.get("thickness") instanceof Double) {
				thickness = getIndividualChannel.getDouble("thickness");
			}
			Double temperature_corrected_thickness = getIndividualChannel
					.getDouble("temperature_corrected_thickness");
			deleteThicknessValFromFinalDoc(getIndividualChannel);
			if (thickness == null) {
				getIndividualChannel.append("thickness", null);
			} else {
				getIndividualChannel.append("thickness", thickness);
			}

			if (temperature_corrected_thickness == null) {
				getIndividualChannel.append("temperature_corrected_thickness", null);
			} else {
				getIndividualChannel.append("temperature_corrected_thickness", temperature_corrected_thickness);
			}
		}
		if (thickness_algorithm.equalsIgnoreCase("First Zero Crossing")) {
			Double thickness = getIndividualChannel.getDouble("thickness_firstzerocrossing");
			Double temperature_corrected_thickness = getIndividualChannel
					.getDouble("temperature_corrected_thickness");
			deleteThicknessValFromFinalDoc(getIndividualChannel);
			if (thickness == null) {
				getIndividualChannel.append("thickness", null);
			} else {
				getIndividualChannel.append("thickness", thickness);
			}

			if (temperature_corrected_thickness == null) {
				getIndividualChannel.append("temperature_corrected_thickness", null);
			} else {
				getIndividualChannel.append("temperature_corrected_thickness", temperature_corrected_thickness);
			}
		}if (thickness_algorithm.equalsIgnoreCase("Max Peak Zero Crossing")) {
			Double thickness = getIndividualChannel.getDouble("thickness_maxpeakzerocrossing");
			Double temperature_corrected_thickness = getIndividualChannel
					.getDouble("temperature_corrected_thickness");
			deleteThicknessValFromFinalDoc(getIndividualChannel);
			if (thickness == null) {
				getIndividualChannel.append("thickness", null);
			} else {
				getIndividualChannel.append("thickness", thickness);
			}

			if (temperature_corrected_thickness == null) {
				getIndividualChannel.append("temperature_corrected_thickness", null);
			} else {
				getIndividualChannel.append("temperature_corrected_thickness", temperature_corrected_thickness);
			}
		}
	}

	/**
	 * 
	 * @param getIndividualChannel
	 */
	private static void deleteThicknessValFromFinalDoc(Document getIndividualChannel) {
		getIndividualChannel.remove("thickness_flank");
		getIndividualChannel.remove("temperature_corrected_thickness_flank");
		getIndividualChannel.remove("thickness_zerocrossing");
		getIndividualChannel.remove("temperature_corrected_thickness_zerocrossing");
		getIndividualChannel.remove("thickness_maxpeak");
		getIndividualChannel.remove("temperature_corrected_thickness_maxpeak");
		getIndividualChannel.remove("thickness_maxpeakzerocrossing");
		getIndividualChannel.remove("thickness_firstzerocrossing");
	}

	/**
	 * 
	 * @param finalDoc
	 */
	private static void corrosionBottomChartCalculations(Document doc) {
		List<Document> getTimeSeriesArray = new ArrayList<Document>();
		getTimeSeriesArray = (List<Document>) doc.get("timeSeries");
		Document removeArray = new Document();
		for (Document getTimeSeries : getTimeSeriesArray) {
			List<Document> getChannelArray = new ArrayList<Document>();
			getChannelArray = (List<Document>) getTimeSeries.get("channels");
			for (Document getIndividualChannel : getChannelArray) {

				String objId = "";
				Double fsh_1 = 0.0D;
				Object docId = null;
				Double peak_1_x = 0.0D;
				Double peak_2_x = 0.0D;
				Double ascan_peak_1_y = 0.0D;
				Double ascan_peak_2_y = 0.0D;
				Double part_material_velocity = 0.0D;
				ArrayList<Double> range_yaxis = new ArrayList<Double>();
				ArrayList<Double> range_xaxis = new ArrayList<Double>();
				ArrayList<Double> msyaxisValues = new ArrayList<Double>();
				ArrayList<Double> msxaxisValues = new ArrayList<Double>();
				ArrayList<Double> percentage_yaxis = new ArrayList<Double>();

				docId = getIndividualChannel.get("doc_id");
				ascan_peak_1_y = (Double) getIndividualChannel.get("peak_1_y");
				ascan_peak_2_y = (Double) getIndividualChannel.get("peak_2_y");
				msyaxisValues = (ArrayList<Double>) getIndividualChannel.get("ms_yaxis");
				msxaxisValues = (ArrayList<Double>) getIndividualChannel.get("ms_xaxis");
				part_material_velocity = (Double) getIndividualChannel.get("part_material_velocity");

				objId = docId.toString();
				getIndividualChannel.remove("doc_id");
				getIndividualChannel.append("doc_id", objId);

				if (part_material_velocity != null) {
					peak_1_x = (Double) getIndividualChannel.get("peak_1_x");
					peak_2_x = (Double) getIndividualChannel.get("peak_2_x");
					if (peak_1_x != null) {
						gateAndPeakCalculations(getIndividualChannel, part_material_velocity, peak_1_x,
								"peak_1_x_range");
					}
					if (peak_2_x != null) {
						gateAndPeakCalculations(getIndividualChannel, part_material_velocity, peak_2_x,
								"peak_2_x_range");
					}
					if ((msxaxisValues != null) && (checkIfGatesExists(getIndividualChannel))) {
						if (gatesArrayListSize == 4) {
							for (Map.Entry<String, Double> indGate : checkIfGatesExist.entrySet()) {
								gateAndPeakCalculations(getIndividualChannel, part_material_velocity,
										indGate.getValue(), indGate.getKey());
							}
							checkIfGatesExist.clear();
						}
						for (int iterate = 0; iterate < msxaxisValues.size(); iterate++) {
							range_xaxis.add(iterate,
									aScanRangeCalculations(part_material_velocity, msxaxisValues.get(iterate)));
						}
						gatesArrayListSize = 0;
					}
				}
				getIndividualChannel.put("range_xaxis", range_xaxis);
				getIndividualChannel.put("fsh", 100);
				if (ascan_peak_1_y != null && ascan_peak_1_y < 0) {
					ascan_peak_1_y = ascan_peak_1_y * -1;
				}
				if (ascan_peak_2_y != null && ascan_peak_2_y < 0) {
					ascan_peak_2_y = ascan_peak_2_y * -1;
				}
				if (msyaxisValues != null) {
					for (Double ind_aScan_val : msyaxisValues) {
						range_yaxis.add(Math.abs(ind_aScan_val));
					}
				}
				if ((range_yaxis != null) && (ascan_peak_1_y != null)) {  
					fsh_1 = Double.parseDouble(df1.format(ascan_peak_1_y / 0.8));
					if (fsh_1 != 0.0) {
						for (int iterate = 0; iterate < range_yaxis.size(); iterate++) {
							percentage_yaxis.add(iterate,
									aScanRPercentageCalculations(range_yaxis.get(iterate), fsh_1));
						}
					}
				}
				checkIfGateHeightExists(getIndividualChannel);
				if (checkIfGateHeightExist.containsKey("gate_a_height_hf") && (ascan_peak_1_y != null)
						&& (ascan_peak_1_y != 0.0)) {
					getIndividualChannel.put("gate_a_height_hf",
							gateHeightCalculations(ascan_peak_1_y, checkIfGateHeightExist.get("gate_a_height_hf")));
					checkIfGateHeightExist.remove("gate_a_height_hf");
				}
				if (checkIfGateHeightExist.containsKey("gate_b_height_hf") && (ascan_peak_2_y != null)
						&& (ascan_peak_2_y != 0.0)) {
					getIndividualChannel.put("gate_b_height_hf",
							gateHeightCalculations(ascan_peak_2_y, checkIfGateHeightExist.get("gate_b_height_hf")));
					checkIfGateHeightExist.remove("gate_b_height_hf");
				}
				checkIfGateHeightExist.clear();
				getIndividualChannel.put("range_yaxis", range_yaxis);
				getIndividualChannel.put("percentage_yaxis", percentage_yaxis);
				removeArray = getIndividualChannel;
			}
			
			getTimeSeries.remove("channels");
			getTimeSeries.append("channels", removeArray);
			
		}
	}
	
	/**
	 * 
	 * @param finalDoc
	 */
	private static void vibrationBottomChartCalculations(Document doc, BasicDBObject jsonDocument) {
		ArrayList<Double> x_accel_fft_x_axis = new ArrayList<Double>();
		ArrayList<Double> x_accel_fft_x_axis_rpm = new ArrayList<Double>();
		ArrayList<Double> x_accel_fft_x_axis_orders = new ArrayList<Double>();
		ArrayList<Double> y_accel_fft_x_axis = new ArrayList<Double>();
		ArrayList<Double> y_accel_fft_x_axis_rpm = new ArrayList<Double>();
		ArrayList<Double> y_accel_fft_x_axis_orders = new ArrayList<Double>();
		ArrayList<Double> z_accel_fft_x_axis = new ArrayList<Double>();
		ArrayList<Double> z_accel_fft_x_axis_rpm = new ArrayList<Double>();
		ArrayList<Double> z_accel_fft_x_axis_orders = new ArrayList<Double>();

		ArrayList<Double> x_vel_fft_x_axis = new ArrayList<Double>();
		ArrayList<Double> x_vel_fft_x_axis_rpm = new ArrayList<Double>();
		ArrayList<Double> x_vel_fft_x_axis_orders = new ArrayList<Double>();
		ArrayList<Double> y_vel_fft_x_axis = new ArrayList<Double>();
		ArrayList<Double> y_vel_fft_x_axis_rpm = new ArrayList<Double>();
		ArrayList<Double> y_vel_fft_x_axis_orders = new ArrayList<Double>();
		ArrayList<Double> z_vel_fft_x_axis = new ArrayList<Double>();
		ArrayList<Double> z_vel_fft_x_axis_rpm = new ArrayList<Double>();
		ArrayList<Double> z_vel_fft_x_axis_orders = new ArrayList<Double>();
		
		ArrayList<Double> accel_fft_x_axis = new ArrayList<Double>();
		ArrayList<Double> accel_fft_x_axis_rpm = new ArrayList<Double>();
		ArrayList<Double> accel_fft_x_axis_orders = new ArrayList<Double>();
		ArrayList<Double> vel_fft_x_axis = new ArrayList<Double>();
		ArrayList<Double> vel_fft_x_axis_rpm = new ArrayList<Double>();
		ArrayList<Double> vel_fft_x_axis_orders = new ArrayList<Double>();
		ObjectId obj = new ObjectId();

		List<Document> getTimeSeriesArray = new ArrayList<Document>();
		getTimeSeriesArray = (List<Document>) doc.get("timeSeries");
		for (Document getTimeSeries : getTimeSeriesArray) {
			List<Document> getChannelArray = new ArrayList<Document>();
			getChannelArray = (List<Document>) getTimeSeries.get("channels");
			for (Document getIndividualChannel : getChannelArray) {
				System.out.println("getIndividualChannel: " + getTimeSeries);
				obj = getIndividualChannel.getObjectId(getIndividualChannel.get("doc_id"));
				String docId = obj.get().toString();
				System.out.println("Obj: " + docId);
				getIndividualChannel.remove("doc_id");
				getIndividualChannel.append("doc_id", docId);
				if (jsonDocument.getString("x") != null) {
					x_accel_fft_x_axis = (ArrayList<Double>) getIndividualChannel.get("x_accel_fft_x_axis");
					if (x_accel_fft_x_axis != null) {
						for (int iterate = 0; iterate < x_accel_fft_x_axis.size(); iterate++) {
							x_accel_fft_x_axis_rpm.add(x_accel_fft_x_axis.get(iterate) * 60);
							x_accel_fft_x_axis_orders.add(x_accel_fft_x_axis.get(iterate) / 500);
						}
					}
					getIndividualChannel.put("x_accel_fft_x_axis_rpm", x_accel_fft_x_axis_rpm);
					getIndividualChannel.put("x_accel_fft_x_axis_orders", x_accel_fft_x_axis_orders);
					Double x_accel_fft_peak_x_axis = (Double) getIndividualChannel.get("x_accel_fft_peak_x_axis");
					if (x_accel_fft_peak_x_axis != null) {
						getIndividualChannel.put("x_accel_fft_peak_x_axis_rpm", x_accel_fft_peak_x_axis * 60);
						getIndividualChannel.put("x_accel_fft_peak_x_axis_orders", x_accel_fft_peak_x_axis / 500);
					}
					x_vel_fft_x_axis = (ArrayList<Double>) getIndividualChannel.get("x_vel_fft_x_axis");
					if (x_vel_fft_x_axis != null) {
						for (int iterate = 0; iterate < x_vel_fft_x_axis.size(); iterate++) {
							x_vel_fft_x_axis_rpm.add(x_vel_fft_x_axis.get(iterate) * 60);
							x_vel_fft_x_axis_orders.add(x_vel_fft_x_axis.get(iterate) / 500);
						}
					}
					getIndividualChannel.put("x_vel_fft_x_axis_rpm", x_vel_fft_x_axis_rpm);
					getIndividualChannel.put("x_vel_fft_x_axis_orders", x_vel_fft_x_axis_orders);
					Double x_vel_fft_peak_x_axis = (Double) getIndividualChannel.get("x_vel_fft_peak_x_axis");
					if (x_vel_fft_peak_x_axis != null) {
						getIndividualChannel.put("x_vel_fft_peak_x_axis_rpm", x_vel_fft_peak_x_axis * 60);
						getIndividualChannel.put("x_vel_fft_peak_x_axis_orders", x_vel_fft_peak_x_axis / 500);
					}
				}

				if (jsonDocument.getString("y") != null) {
					y_accel_fft_x_axis = (ArrayList<Double>) getIndividualChannel.get("y_accel_fft_x_axis");
					if (y_accel_fft_x_axis != null) {
						for (int iterate = 0; iterate < y_accel_fft_x_axis.size(); iterate++) {
							y_accel_fft_x_axis_rpm.add(y_accel_fft_x_axis.get(iterate) * 60);
							y_accel_fft_x_axis_orders.add(y_accel_fft_x_axis.get(iterate) / 500);
						}
					}
					getIndividualChannel.put("y_accel_fft_x_axis_rpm", y_accel_fft_x_axis_rpm);
					getIndividualChannel.put("y_accel_fft_x_axis_orders", y_accel_fft_x_axis_orders);
					Double y_accel_fft_peak_x_axis = (Double) getIndividualChannel.get("y_accel_fft_peak_x_axis");
					if (y_accel_fft_peak_x_axis != null) {
						getIndividualChannel.put("y_accel_fft_peak_x_axis_rpm", y_accel_fft_peak_x_axis * 60);
						getIndividualChannel.put("y_accel_fft_peak_x_axis_orders", y_accel_fft_peak_x_axis / 500);
					}
					y_vel_fft_x_axis = (ArrayList<Double>) getIndividualChannel.get("y_vel_fft_x_axis");
					if (y_vel_fft_x_axis != null) {
						for (int iterate = 0; iterate < y_vel_fft_x_axis.size(); iterate++) {
							y_vel_fft_x_axis_rpm.add(y_vel_fft_x_axis.get(iterate) * 60);
							y_vel_fft_x_axis_orders.add(y_vel_fft_x_axis.get(iterate) / 500);
						}
					}
					getIndividualChannel.put("y_vel_fft_x_axis_rpm", y_vel_fft_x_axis_rpm);
					getIndividualChannel.put("y_vel_fft_x_axis_orders", y_vel_fft_x_axis_orders);
					Double y_vel_fft_peak_x_axis = (Double) getIndividualChannel.get("y_vel_fft_peak_x_axis");
					if (y_vel_fft_peak_x_axis != null) {
						getIndividualChannel.put("y_vel_fft_peak_x_axis_rpm", y_vel_fft_peak_x_axis * 60);
						getIndividualChannel.put("y_vel_fft_peak_x_axis_orders", y_vel_fft_peak_x_axis / 500);
					}
				}

				if (jsonDocument.getString("z") != null) {
					z_accel_fft_x_axis = (ArrayList<Double>) getIndividualChannel.get("z_accel_fft_x_axis");
					if (z_accel_fft_x_axis != null) {
						for (int iterate = 0; iterate < z_accel_fft_x_axis.size(); iterate++) {
							z_accel_fft_x_axis_rpm.add(z_accel_fft_x_axis.get(iterate) * 60);
							z_accel_fft_x_axis_orders.add(z_accel_fft_x_axis.get(iterate) / 500);
						}
					}
					getIndividualChannel.put("z_accel_fft_x_axis_rpm", z_accel_fft_x_axis_rpm);
					getIndividualChannel.put("z_accel_fft_x_axis_orders", z_accel_fft_x_axis_orders);
					Double z_accel_fft_peak_x_axis = (Double) getIndividualChannel.get("z_accel_fft_peak_x_axis");
					if (z_accel_fft_peak_x_axis != null) {
						getIndividualChannel.put("z_accel_fft_peak_x_axis_rpm", z_accel_fft_peak_x_axis * 60);
						getIndividualChannel.put("z_accel_fft_peak_x_axis_orders", z_accel_fft_peak_x_axis / 500);
					}
					z_vel_fft_x_axis = (ArrayList<Double>) getIndividualChannel.get("z_vel_fft_x_axis");
					if (z_vel_fft_x_axis != null) {
						for (int iterate = 0; iterate < z_vel_fft_x_axis.size(); iterate++) {
							z_vel_fft_x_axis_rpm.add(z_vel_fft_x_axis.get(iterate) * 60);
							z_vel_fft_x_axis_orders.add(z_vel_fft_x_axis.get(iterate) / 500);
						}
					}
					getIndividualChannel.put("z_vel_fft_x_axis_rpm", z_vel_fft_x_axis_rpm);
					getIndividualChannel.put("z_vel_fft_x_axis_orders", z_vel_fft_x_axis_orders);
					Double z_vel_fft_peak_x_axis = (Double) getIndividualChannel.get("z_vel_fft_peak_x_axis");
					if (z_vel_fft_peak_x_axis != null) {
						getIndividualChannel.put("z_vel_fft_peak_x_axis_rpm", z_vel_fft_peak_x_axis * 60);
						getIndividualChannel.put("z_vel_fft_peak_x_axis_orders", z_vel_fft_peak_x_axis / 500);
					}
				}
				if (jsonDocument.getString("x") == null && jsonDocument.getString("y") == null && jsonDocument.getString("z") == null) {
					accel_fft_x_axis = (ArrayList<Double>) getIndividualChannel.get("accel_fft_x_axis");
					if (accel_fft_x_axis != null) {
						for (int iterate = 0; iterate < accel_fft_x_axis.size(); iterate++) {
							accel_fft_x_axis_rpm.add(accel_fft_x_axis.get(iterate) * 60);
							accel_fft_x_axis_orders.add(accel_fft_x_axis.get(iterate) / 500);
						}
					}
					getIndividualChannel.put("accel_fft_x_axis_rpm", accel_fft_x_axis_rpm);
					getIndividualChannel.put("accel_fft_x_axis_orders", accel_fft_x_axis_orders);
					Double accel_fft_peak_x_axis = (Double) getIndividualChannel.get("accel_fft_peak_x_axis");
					if (accel_fft_peak_x_axis != null) {
						getIndividualChannel.put("accel_fft_peak_x_axis_rpm", accel_fft_peak_x_axis * 60);
						getIndividualChannel.put("accel_fft_peak_x_axis_orders", accel_fft_peak_x_axis / 500);
					}
					vel_fft_x_axis = (ArrayList<Double>) getIndividualChannel.get("vel_fft_x_axis");
					if (vel_fft_x_axis != null) {
						for (int iterate = 0; iterate < vel_fft_x_axis.size(); iterate++) {
							vel_fft_x_axis_rpm.add(vel_fft_x_axis.get(iterate) * 60);
							vel_fft_x_axis_orders.add(vel_fft_x_axis.get(iterate) / 500);
						}
					}
					getIndividualChannel.put("vel_fft_x_axis_rpm", vel_fft_x_axis_rpm);
					getIndividualChannel.put("vel_fft_x_axis_orders", vel_fft_x_axis_orders);
					Double vel_fft_peak_x_axis = (Double) getIndividualChannel.get("vel_fft_peak_x_axis");
					if (vel_fft_peak_x_axis != null) {
						getIndividualChannel.put("vel_fft_peak_x_axis_rpm", vel_fft_peak_x_axis * 60);
						getIndividualChannel.put("vel_fft_peak_x_axis_orders", vel_fft_peak_x_axis / 500);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param doc
	 * @param part_material_velocity
	 * @param value
	 * @param keyName
	 */
	private static void gateAndPeakCalculations(Document doc, Double part_material_velocity, Double value,
			String keyName) {
		Double calculatedValue = 0.0D;
		calculatedValue = Double
				.parseDouble(df1.format(value * (part_material_velocity / 2) * commonCorrosionMultiplier));
		doc.put(keyName, calculatedValue);

	}

	/**
	 * 
	 * @param part_material_velocity
	 * @param value
	 * @return
	 */
	private static Double aScanRangeCalculations(Double part_material_velocity, Double value) {
		Double calculatedValue = 0.0D;
		calculatedValue = Double
				.parseDouble(df1.format(value * (part_material_velocity / 2) * commonCorrosionMultiplier));
		return calculatedValue;

	}

	/**
	 * 
	 * @param ascan_r_mv_value
	 * @param fsh_1
	 * @return
	 */
	private static Double aScanRPercentageCalculations(Double ascan_r_mv_value, Double fsh_1) {
		Double calculatedValue = 0.0D;
		calculatedValue = Double.parseDouble(df1.format((ascan_r_mv_value * 100) / fsh_1));
		return calculatedValue;

	}

	/**
	 * 
	 * @param ascan_peak_1_y
	 * @param gate_a_height
	 * @return
	 */
	private static Double gateHeightCalculations(Double ascan_peak_1_y, Double gate_a_height) {
		Double calculatedValue = 0.0D;
		calculatedValue = Double.parseDouble(df1.format((ascan_peak_1_y / 0.8) * (gate_a_height / 100)));
		return calculatedValue;
	}

	/**
	 * 
	 * @param doc
	 * @return
	 */
	private static Boolean checkIfGatesExists(Document doc) {
		checkIfGatesExist.put("gate_a_start_range", (Double) doc.get("gate_a_start"));
		checkIfGatesExist.put("gate_a_length_range", (Double) doc.get("gate_a_length"));
		checkIfGatesExist.put("gate_b_start_range", (Double) doc.get("gate_b_start"));
		checkIfGatesExist.put("gate_b_length_range", (Double) doc.get("gate_b_length"));
		checkIfGatesExist.values().removeIf(Objects::isNull);
		gatesArrayListSize = checkIfGatesExist.size();
		if (gatesArrayListSize > 0) {
			return true;
		} else
			return false;
	}

	/**
	 * 
	 * @param doc
	 * @return
	 */
	private static void checkIfGateHeightExists(Document doc) {
		checkIfGateHeightExist.put("gate_a_height_hf", (Double) doc.get("gate_a_height"));
		checkIfGateHeightExist.put("gate_b_height_hf", (Double) doc.get("gate_b_height"));
		checkIfGateHeightExist.values().removeIf(Objects::isNull);
	}

	
	/**
	 * 
	 * @param timeSeriesArray
	 * @return
	 */
	private static List<Document>sortDataBasedOnMessageTime(List<Document> timeSeriesArray){		
		List<JSONObject> convertListOfDocumentToListOfJson = new ArrayList<JSONObject>();
		JSONParser parser = new JSONParser();
		for (int i = 0; i < timeSeriesArray.size(); i++) {
			try {
				JSONObject docToJson = new JSONObject();				
				timeSeriesArray.get(i).append("timeInMillis", LocalDateTime.parse((timeSeriesArray.get(i).get("yearMonthDay") + " " + timeSeriesArray.get(i).get("time")).toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
			            .atZone(ZoneId.systemDefault())
			            .toInstant()
			            .toEpochMilli());
				docToJson = (JSONObject) parser.parse(com.mongodb.util.JSON.serialize(timeSeriesArray.get(i)));
				convertListOfDocumentToListOfJson.add(docToJson);
			} catch (org.json.simple.parser.ParseException e) {
				e.printStackTrace();
			}
		}		
		sort(convertListOfDocumentToListOfJson,sortBy("timeInMillis"));
		timeSeriesArray.clear();
		
		for(JSONObject eachJson:convertListOfDocumentToListOfJson){
			eachJson.remove("timeInMillis");
			Document doc = Document.parse(eachJson.toJSONString());
			timeSeriesArray.add(doc);
		}			
		
		return timeSeriesArray;
	}
  /**
   * 
   * @param jsonDocument
   * @param key
   * @return
   */
   public static Date frameQueryForDate(BasicDBObject jsonDocument,String key) {
	   String format="";
	   Date dateTime=null;
	   /*searchQuery.put("message_time", new BasicDBObject("$gte",
				new DateTime(getFromDateValues.get("year"), getFromDateValues.get("monthNumber"),
						getFromDateValues.get("dateNumber"), 0, 0, DateTimeZone.UTC).toDate()).append("$lte",
								new DateTime(getToDateValues.get("year"), getToDateValues.get("monthNumber"),
										getToDateValues.get("dateNumber"), 23, 59, DateTimeZone.UTC).toDate()));*/
	   String dateinString=jsonDocument.getString(key);
	   if(dateinString.indexOf("-")>0) {
		   dateinString=dateinString.replace("-"," ");
	   }
	   if(dateinString.indexOf(",")>0) {
		   dateinString=dateinString.replace(","," ");
	   }
	   if(jsonDocument.getString(key).indexOf(":")>0) {
		    format="MM/dd/yyyy HH:mm:ss";
			Calendar cal = Calendar.getInstance();
			Date yearMonthDate = parseDateTimeFromJson(dateinString, format);
			cal.setTime(yearMonthDate);
			int year = cal.get(Calendar.YEAR);
			int monthNumber = cal.get(Calendar.MONTH);
			int dateNumber = cal.get(Calendar.DAY_OF_MONTH);
			monthNumber += 1;
			dateTime = new DateTime(year, monthNumber,
					dateNumber, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),cal.get(Calendar.SECOND), DateTimeZone.UTC).toDate();
					
	   }
	   else {
		   
		    format="MM/dd/yyyy";
			Calendar cal = Calendar.getInstance();
			Date yearMonthDate = parseDateTimeFromJson(dateinString, format);
			cal.setTime(yearMonthDate);
			int year = cal.get(Calendar.YEAR);
			int monthNumber = cal.get(Calendar.MONTH);
			int dateNumber = cal.get(Calendar.DAY_OF_MONTH);
			monthNumber += 1;
			if(key.equalsIgnoreCase("from_date")) {
				dateTime = new DateTime(year, monthNumber,
					dateNumber, 0, 0, DateTimeZone.UTC).toDate();
			}
			else if(key.equalsIgnoreCase("to_date")) {
				 dateTime = new DateTime(year, monthNumber,
						dateNumber, 23, 59, DateTimeZone.UTC).toDate();
			}
	   }
	   return dateTime;
   }
   
   private static final String SECRET_KEY = "TW9sZXhEU1NlY3JldEtleQ==";
   
   private static SecretKeySpec secretKey = new SecretKeySpec(Base64.getDecoder().decode(SECRET_KEY), "AES");
   private static AlgorithmParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(SECRET_KEY));
   
   public static String encrypt(String strToEncrypt) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
		} catch (Exception e) {
			System.out.println("Error while encrypting: " + e.toString());
		}
		return null;
	}

	public static String decrypt(String strToDecrypt) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		} catch (Exception e) {
			System.out.println("Error while decrypting: " + e.toString());
		}
		return null;
	}
	/**
	 * 
	 * @param date
	 * @param format
	 * @param timeZone
	 * @return
	 */
	public static String formatDateToString(Date date, String format,
			String timezone) {
		if (date == null) return null;
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		if (timezone == null || "".equalsIgnoreCase(timezone.trim())) {
			timezone = Calendar.getInstance().getTimeZone().getID();
		}
		sdf.setTimeZone(TimeZone.getTimeZone(timezone));
		System.out.println("sdf...."+sdf.format(date));
		return sdf.format(date);
	}
   
}
