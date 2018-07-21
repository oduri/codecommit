package com.kavi.user.dataobjects;

import com.google.gson.annotations.Expose;


public class DeviceDO{
	
	@Expose
    private String id;     
	
	@Expose
    private String name;

	@Expose
    private String category = "device";
	
	private String device_category_id;
	
	private String assetId; 
	
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

	public String getDevice_category_id() {
		return device_category_id;
	}

	public void setDevice_category_id(String device_category_id) {
		this.device_category_id = device_category_id;
	}

	public String getAssetId() {
		return assetId;
	}

	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}
	
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}