
/**
 * @author kavi
 *
 */
package com.mongo.db.constants;

public class MongoDBConstants{
	
	public static final String SEPARATOR="-";
	
	public static final Integer MAX_ORDER_BY=-1;
	
	public static final Integer MIN_ORDER_BY=1;
	
	public static final Integer LIMIT_ROW=1;
	
	public static final Integer LIMIT_MAX_ROW=10;
	
	public static final String ACTIVITY_START="activityStart";
	
	public static final String ACTIVITY_END="activityEnd";
	
	public static boolean trueflag=true;
	
	public static boolean falseflag=false;
	
	public static final String TIME_ZONE="America/Chicago";
	
	public static final String DEVICE_ID="device_id";
	
	public static final String HARDWARE_ID="hardware_id";
	
	public static final String ASSET_ID = "asset_id";
	
	public static final String DEVICEID="deviceId";
	
	public static final String CHANNEL_ID = "channel_id";
	
	public static final String PARAMETER_TYPE="parameter_type";
	
	public static final String DEVICE="device";
	
	public static final String CHANNEL="channel";
	
	public static final String SES_FLAG_ENABLE = "SES_FLAG_ENABLE";
	
	public static final String BCC_EMAIL = "BCC_EMAIL";
	
	/***********Local machine*************/
	/*public static final String HOST_NAME="52.3.111.124";
	public static final String APP_DB_NAME="FHR_DSS";	
	public static final Integer PORT_NUMBER=27017;*/
	/***********Local machine*************/
	
	/***********Molex UAT  Deployment*************/
/*	public static final String HOST_NAME="40.0.1.199";
	public static final String APP_DB_NAME="FHR_DSS";	
	public static final Integer PORT_NUMBER=27017;*/
	/***********Molex UAT Deployment*************/
	
	/***********Molex machine*************/
	public static final String HOST_NAME="30.0.1.114";
	//public static final String HOST_NAME="127.0.1.114";
	public static final String APP_DB_NAME="FHR_DSS";
	public static final Integer PORT_NUMBER=27017;
	/***********Molex machine*************/
	public static final String T_STAGING="t_LisleMolex";
	
	public static final String USER_NAME="myTester";
	public static final String USER_PASSWORD="xyz123";
	
	public static final String MONGO_COLLECTION_SESSION_INFO="session_info";
	
	public static final String MONGO_COLLECTION_LOCATION_HIERARCHY="location_hierarchy";
	
	public static final String MONGO_COLLECTION_LOCATION_HIERARCHY_TEMP="location_hierarchy_temp";
	
	public static final String MONGO_COLLECTION_DEVICE_STATUS_LOG="device_status_log";
	
	public static final String MONGO_COLLECTION_UI_CONFIG="ui_config";
	
	public static final String MONGO_COLLECTION_PARSER="message_parser";
	
	public static final String MONGO_COLLECTION_WEB_SERVICE_AUTHENTICATION="web_service_authentication";
	
	public static final String MONGO_COLLECTION_DEVICE_PARAM_LATEST ="device_parameter_latest";
	
	public static final String MONGO_COLLECTION_DEVICE_PARAM_MAPPING = "device_parameter_mapping";
	
	public static final String MONGO_COLLECTION_RULE_MEASURES = "rule_measures";
	
	public static final String MONGO_COLLECTION_RULE = "rule";
	
	public static final String MONGO_COLLECTION_NOTIFICATION_GROUP = "notification_group";
	
	public static final String MONGO_COLLECTION_USER_DEFINED_PARAM_LOG ="user_defined_parameter_log";

	public static final String MONGO_COLLECTION_TWO_WAY_COMMUNICATION ="two_way_communication";
	
	public static final String MONGO_COLLECTION_MDSS_ENDPOINT="mdss_endpoint";
	
	public static final String MONGO_COLLECTION_REPORT="pdf_report";
	
	public static final String MONGO_COLLECTION_REPORT_CONFIG="pdf_report_config";
	
	public static final String MONGO_COLLECTION_ASSET_SUMMARY_STATISTICS="asset_summary_statistics";
	
	public static final String MONGO_COLLECTION_SUPPORT_TICKET = "support_ticket";
	
	public static final String MONGO_COLLECTION_SUFFIX="";
	
	
}