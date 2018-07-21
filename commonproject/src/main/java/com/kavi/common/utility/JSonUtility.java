package com.kavi.common.utility;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSonUtility {
	
	/***
	 * 
	 * @return getJSONObject
	 */

	public static JSONObject getJSONObject(String saFileName) {
		JSONObject jsonObjectStructure = null;
		try {
			// read the json file
			FileReader reader = new FileReader(saFileName);
			JSONParser jsonParser = new JSONParser();
			jsonObjectStructure = (JSONObject) jsonParser.parse(reader);
			// jsonObjectStructure = (JSONObject)
			// jsonObjectStructure.get(JSONConstants.JSON_ROOT_JOB_TAG_NAME);
			reader.close();
			return jsonObjectStructure;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return jsonObjectStructure;
	}
	
	public static JSONObject getStringJSONObject(String jsonObject){
		JSONObject jsonObjectStructure=null;
		try{
   			// read the json file
			//FileReader reader = new FileReader(saFileName);
			JSONParser jsonParser = new JSONParser();
			jsonObjectStructure = (JSONObject) jsonParser.parse(jsonObject);
			//jsonObjectStructure = (JSONObject) jsonObjectStructure.get(JSONConstants.JSON_ROOT_JOB_TAG_NAME);
			return jsonObjectStructure;
		}
		catch(ParseException e){
			jsonObjectStructure=null;
		  }
		
		return jsonObjectStructure;
	}
}
