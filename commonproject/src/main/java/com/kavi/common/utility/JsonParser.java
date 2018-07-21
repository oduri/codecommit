package com.kavi.common.utility;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class JsonParser {
	
	 
	JSONParser jsonParser = new JSONParser();

	
	public ArrayList<String> extractValueFromJSONAsList(JSONObject obj, String key){
		ArrayList<String> alList=new ArrayList<String>();
		
		String objStruct[]=key.split("\\.");
		
		for(int i=0;i<objStruct.length;i++){
			alList.add(extractValueFromJSON(obj,objStruct[i]));
		}
		
		return alList;
	}
	
	public Long extractLongValueFromJSON(JSONObject obj,String tag){
		Long itrValue=0L;
		if(null != obj.get(tag)){
			itrValue=(Long)obj.get(tag);
		}
		return itrValue;
	}
	
	public JSONObject parse(String jsonData) throws Exception{
		return (JSONObject)jsonParser.parse(jsonData);
	}
	
	public Iterator<JSONObject> getIteratorFromArray(JSONArray array){
		 
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> iter = array.iterator();
		
		return iter;
	}
	
	public String extractValueFromJSON(JSONObject obj,String tag){
		String itrValue=new String();
		
		if(null != obj.get(tag)){
			itrValue=(String)obj.get(tag);
		}
		return itrValue;
	}
	
	public JSONObject extractObjectFromJSON(JSONObject jobStructure,String parseKey){
		JSONObject obj=new JSONObject();
		
		String objStruct[]=parseKey.split("\\.");
		obj=jobStructure;
		
		for(int i=0;i<objStruct.length;i++){
			obj = (JSONObject) obj.get(objStruct[i]);
		}
		
		return obj;
	}

	public JSONArray extractArrayFromJSON(JSONObject jobStructure,String parseKey){
		JSONArray arr=new JSONArray();
		JSONObject obj=new JSONObject();
		
		String objStruct[]=parseKey.split("\\.");
		obj=jobStructure;
		
		for(int i=0;i<objStruct.length;i++){
			if(i== objStruct.length-1){
				arr=(JSONArray) obj.get(objStruct[i]);
			} else {
				obj = (JSONObject) obj.get(objStruct[i]);
			}
		}
		
		return arr;
	}
	
	public String extractValueFromNestedJSON(JSONObject jobStructure,String parseKey){
		String res=new String();
		JSONObject obj=new JSONObject();
		
		String objStruct[]=parseKey.split("\\.");
		obj=jobStructure;
		
		for(int i=0;i<objStruct.length;i++){
			if(i== objStruct.length-1){
				if(null!=obj.get(objStruct[i])){
					if("java.lang.Boolean".equals(obj.get(objStruct[i]).getClass().getName())){
						res=(Boolean) obj.get(objStruct[i])+"";
					}else if("java.lang.Long".equals(obj.get(objStruct[i]).getClass().getName())){
						res=(Long) obj.get(objStruct[i])+"";
					}
					else {
						res=(String) obj.get(objStruct[i]);
					}
				}
			} else {
				if(null!=obj.get(objStruct[i])){
					obj = (JSONObject) obj.get(objStruct[i]);
				}
			}
		}
		
		return res;
	}
	
	


}
