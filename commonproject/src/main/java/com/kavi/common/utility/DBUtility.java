package com.kavi.common.utility;

import org.apache.log4j.Logger;

import com.mongo.db.MongoDBConnection;
import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;


public class DBUtility {
	 
	private final static Logger logger = Logger.getLogger(DBUtility.class);
	
	
	public MongoDatabase getMongoDB(String dbParamjson, MongoDBConnection mongoSingle) {
		// Sample dbParamjson Structure
		/*
		 * { "sessionkey": "xxxxxxxx", "db_name": "FHR_DSS" }
		 */
		BasicDBObject dbParamJsonDoc = null;
		if (dbParamjson != null) {
			dbParamJsonDoc = (BasicDBObject) JSON.parse(dbParamjson);
			return mongoSingle.getMongoDB(dbParamJsonDoc.getString("db_name"));
		} else {
			return mongoSingle.getMongoDB(MongoDBConstants.APP_DB_NAME);
		}
	}
	
}
