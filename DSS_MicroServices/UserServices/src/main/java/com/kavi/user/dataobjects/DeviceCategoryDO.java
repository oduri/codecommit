package com.kavi.user.dataobjects;

import org.json.simple.JSONArray;

import com.google.gson.annotations.Expose;


public class DeviceCategoryDO{
	
	@Expose
    private String id;     
	
	@Expose
    private String name;
    
	@Expose
    private String type;
    
	@Expose
    private String category = "device_category";
	
	private String asset_id;
	
	@Expose
	private JSONArray device;

	

	public String getAsset_id() {
		return asset_id;
	}

	public JSONArray getDevice() {
		return device;
	}

	

	public void setAsset_id(String asset_id) {
		this.asset_id = asset_id;
	}

	public void setDevice(JSONArray device) {
		this.device = device;
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
	
	 public int hashCode(){
	        int hashcode = 0;
	        //hashcode = price*20;
	        hashcode += name.hashCode();
	        return hashcode;
	    }
	     
    public boolean equals(Object obj){
        if (obj instanceof DeviceCategoryDO) {
        	DeviceCategoryDO pp = (DeviceCategoryDO) obj;
            return (pp.id.equals(this.id) && pp.name == this.name && pp.asset_id==this.asset_id);
        } else {
            return false;
        }
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}