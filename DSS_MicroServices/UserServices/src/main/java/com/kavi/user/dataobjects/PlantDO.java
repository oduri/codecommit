package com.kavi.user.dataobjects;

import org.json.simple.JSONArray;

import com.google.gson.annotations.Expose;


public class PlantDO{
	
	@Expose
    private String id;     
	
	@Expose
    private String name;
    
	@Expose
	private String category= "plant";
    
	@Expose
    private JSONArray site;

	public JSONArray getSite() {
		return site;
	}

	public void setSite(JSONArray site) {
		this.site = site;
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