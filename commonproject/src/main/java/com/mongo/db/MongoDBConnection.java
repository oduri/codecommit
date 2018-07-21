package com.mongo.db;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.codec.binary.StringUtils;

import com.mongo.db.constants.MongoDBConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

public class MongoDBConnection {
	
	private  MongoClient mongoClient;
	private  DB db;
	private  MongoDatabase mongodb;
	private static Properties prop = new Properties(); 
	public MongoDBConnection()
	{
	    try {
		    	 
	       if(mongoClient==null){
	    	   String file = "/environment.properties";
		   	   InputStream inputStream = getClass().getResourceAsStream(file); 
		   	   Reader reader = new InputStreamReader(inputStream);
			   prop.load(reader);
			   if(prop.getProperty("MONGO_AUTH_ENABLE").equalsIgnoreCase("false")) {
				   ServerAddress addr = new ServerAddress(prop.getProperty("HOST_NAME"), Integer.parseInt(prop.getProperty("PORT_NUMBER")));
		    	   mongoClient = new MongoClient(addr);
		    	      
			   }else {
				   ServerAddress addr = new ServerAddress(prop.getProperty("HOST_NAME"), Integer.parseInt(prop.getProperty("PORT_NUMBER")));
				   MongoCredential credential = MongoCredential.createCredential(
						   prop.getProperty("MONGODB_USER_NAME"), prop.getProperty("MONGODB_APP_DB"), prop.getProperty("MONGODB_USER_PASSWORD").toCharArray());
				   MongoClientOptions options = new MongoClientOptions.Builder().socketKeepAlive(true).build();
				   mongoClient = new MongoClient(addr, Arrays.asList(credential),options);
			   }
			   inputStream.close();
			   reader.close();
	    	}
	    }	

	    catch(Exception e)
	    {
	    	e.printStackTrace();	
	        //System.err.println(e.getMessage());
	    }
	}
	/**
	 * 
	 * @param dbjson
	 */
	public MongoDBConnection(String dbjson)
	{
		
	    try {
	    	BasicDBObject jsonDocument = null;
			if (dbjson != null) {
				jsonDocument = (BasicDBObject) JSON.parse(dbjson);
				mongoClient = getMongoClient(jsonDocument);
			} 
			else {
				mongoClient = getMongoClient(jsonDocument);
			}
	        
	    }	

	    catch(Exception e)
	    {
	    	e.printStackTrace();	
	        //System.err.println(e.getMessage());
	    }
	}
	
	/**
	 * 
	 * @return
	 */
	private MongoClient getMongoClient(BasicDBObject jsonDocument) {
		try {
			    if(mongoClient==null){
		    	   String file = "/environment.properties";
			   	   InputStream inputStream = getClass().getResourceAsStream(file); 
			   	   Reader reader = new InputStreamReader(inputStream);
				   prop.load(reader);
				   if(prop.getProperty("MONGO_AUTH_ENABLE").equalsIgnoreCase("false")) {
					   ServerAddress addr = new ServerAddress(prop.getProperty("HOST_NAME"), Integer.parseInt(prop.getProperty("PORT_NUMBER")));
			    	   mongoClient = new MongoClient(addr);
				   }else {
					   String mongodb=jsonDocument.getString("organizationName")==null?prop.getProperty("MONGODB_APP_DB"):jsonDocument.getString("organizationName")+"_DSS";
					   ServerAddress addr = new ServerAddress(prop.getProperty("HOST_NAME"), Integer.parseInt(prop.getProperty("PORT_NUMBER")));
					   MongoCredential credential = MongoCredential.createCredential(
							   prop.getProperty("MONGODB_USER_NAME"), mongodb, prop.getProperty("MONGODB_USER_PASSWORD").toCharArray());
					   MongoClientOptions options = new MongoClientOptions.Builder().socketKeepAlive(true).build();
					   mongoClient = new MongoClient(addr, Arrays.asList(credential),options);
				   }
				  inputStream.close();
				  reader.close();
			   }
		 }catch(Exception e) {
			 e.printStackTrace();
		 }
		return mongoClient;
	}
	
	public void close(){
		mongoClient.close();
	}
	public DB getDB(){
		if(db==null){
			db=mongoClient.getDB(MongoDBConstants.APP_DB_NAME);
			 
		}
		return db;
	}
	/**
	 * 
	 * @param databaseName
	 * @return
	 */
	public MongoDatabase getMongoDB(String databaseName){
		if(mongodb==null){
			mongodb = mongoClient.getDatabase(databaseName);
		}else if(!StringUtils.equals(databaseName, mongodb.getName())) {
			mongodb = mongoClient.getDatabase(databaseName);
		}
		return mongodb;
	}
}
