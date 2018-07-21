package com.kavi.twoway.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Properties;

import org.bson.Document;

import com.kavi.common.utility.LDAPUtility;

public class TwowayCommUtility {
	
	private static Properties prop = new Properties(); 
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static Document getSpecificDetailsForTakeMeasurement(String type){
		Document selectedFields = new Document();
		if(type.equalsIgnoreCase("take_measurement")){
			selectedFields.put("device_id",1);
			selectedFields.put("db_parameters.ip_address",1);
	 		selectedFields.put("db_parameters.ip_port",1);
		}
		return selectedFields;
	}
	
	/**
	 * 
	 * @param message
	 * @return
	 */
	public static String getId(String message) {
		String id = new String();
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.reset();
			m.update(message.getBytes());
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			id = bigInt.toString(16);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}
	/**
	 * 
	 */
	public static String getKafkaURL(){
		String file = "/environment.properties";
   		InputStream inputStream = LDAPUtility.class.getResourceAsStream(file); 
   		Reader reader = new InputStreamReader(inputStream);
		try {
			prop.load(reader);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			reader.close();
			inputStream.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return prop.getProperty("KAFKA_URL");
	}
}
