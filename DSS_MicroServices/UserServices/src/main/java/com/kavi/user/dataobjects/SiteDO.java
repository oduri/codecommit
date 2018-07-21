package com.kavi.user.dataobjects;

import org.json.simple.JSONArray;

import com.google.gson.annotations.Expose;




public class SiteDO{
	
	@Expose
    private String id;     
	
	@Expose
    private String name;
    
    private String plant_id;
    
    @Expose
    private JSONArray unit;
    
    @Expose
    private String category = "site";
    
	public JSONArray getUnit() {
		return unit;
	}

	public void setUnit(JSONArray unit) {
		this.unit = unit;
	}

	

	public String getPlant_id() {
		return plant_id;
	}


	public void setPlant_id(String plant_id) {
		this.plant_id = plant_id;
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