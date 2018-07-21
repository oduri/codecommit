package com.kavi.user.dataobjects;

import org.json.simple.JSONArray;

import com.google.gson.annotations.Expose;


public class UnitDO{
	
	@Expose
    private String id;     
	
	@Expose
    private String name;
    
	@Expose
    private String category = "";
	
    private String site_id;
	
	@Expose
    private JSONArray asset;

	

	public String getSite_id() {
		return site_id;
	}


	public void setSite_id(String site_id) {
		this.site_id = site_id;
	}

	public JSONArray getAsset() {
		return asset;
	}

	public void setAsset(JSONArray asset) {
		this.asset = asset;
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