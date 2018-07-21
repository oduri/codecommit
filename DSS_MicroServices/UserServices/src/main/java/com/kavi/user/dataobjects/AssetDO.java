package com.kavi.user.dataobjects;

import org.json.simple.JSONArray;

import com.google.gson.annotations.Expose;


public class AssetDO{
	
	@Expose
    private String id;     
	
	@Expose
    private String name;
    
	private String unit_id;
	
	@Expose
	private JSONArray device_category;
	
	@Expose
    private String category = "asset";

	public String getUnit_id() {
		return unit_id;
	}


	public void setUnit_id(String unit_id) {
		this.unit_id = unit_id;
	}

	public JSONArray getDevice_category() {
		return device_category;
	}

	public void setDevice_category(JSONArray device_category) {
		this.device_category = device_category;
	}


	public String getId() {
		return id;
	}


	public String getName() {
		return name;
	}


	public void setId(String id) {
		this.id = id;
	}


	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}