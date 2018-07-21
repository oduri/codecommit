package com.kavi.endpoint.utility;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;
import org.springframework.http.MediaType;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongo.db.MongoDBConnection;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class EndpointCommonUtility {
	 
	private final static Logger logger = Logger.getLogger(EndpointCommonUtility.class);
	
	private static final Random RANDOM = new SecureRandom();
	
	public static final int PASSWORD_LENGTH = 8;
	
	/**
	 * 
	 * @return
	 */
	public static String generateRandomId(){
	      String letters = ""+System.currentTimeMillis();
	      String randomId = "";
	      for (int i=0; i<7; i++){
	          int index = (int)(RANDOM.nextDouble()*letters.length());
	          randomId += letters.substring(index, index+1);
	      }
	      return randomId;
	}
	
	public static void closeConnection(MongoDBConnection mongoSingle,DB db, DBCollection table){
			db=null;
			table=null;
			mongoSingle.close();
	}
	
	public static void closeConnection(MongoDBConnection mongoSingle,DB db){
		db=null;
		mongoSingle.close();
	}

	
	/**
	 * 
	 * @param authCredentials
	 * @return
	 */
	public static boolean isUserAuthenticated(String authCredentials) {

		if (null == authCredentials){
			return false;
		}
		final String encodedUserPassword = authCredentials.replaceFirst("Basic"
				+ " ", "");
		String usernameAndPassword = null;
		try {
			byte[] decodedBytes = java.util.Base64.getDecoder().decode(
					encodedUserPassword);
			usernameAndPassword = new String(decodedBytes, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		final StringTokenizer tokenizer = new StringTokenizer(
				usernameAndPassword, ":");
		final String username = tokenizer.nextToken();
		final String password = tokenizer.nextToken();
		boolean authenticationStatus = "FHR".equals(username)
				&& "7a92307b-cc15-4ab9-bf82-799b3b114a2c".equals(password);
		return authenticationStatus;
	}
	
	/**
	 * 
	 * @param authCredentials
	 * @return
	 */
	public static boolean isUserAuthenticatedForTwoWay(String authCredentials) {

		if (null == authCredentials){
			return false;
		}
		final String encodedUserPassword = authCredentials.replaceFirst("Basic"
				+ " ", "");
		String usernameAndPassword = null;
		try {
			byte[] decodedBytes = java.util.Base64.getDecoder().decode(
					encodedUserPassword);
			usernameAndPassword = new String(decodedBytes, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		final StringTokenizer tokenizer = new StringTokenizer(
				usernameAndPassword, ":");
		final String username = tokenizer.nextToken();
		final String password = tokenizer.nextToken();
		boolean authenticationStatus = "two-way".equals(username)
				&& "7a92307b-cc15-4ab9-bf82-799b3b114a2c".equals(password);
		return authenticationStatus;
		
	}
}
