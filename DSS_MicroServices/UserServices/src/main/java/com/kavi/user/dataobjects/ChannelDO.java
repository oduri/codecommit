package com.kavi.user.dataobjects;

import com.google.gson.annotations.Expose;


public class ChannelDO{
	
	@Expose
    private String id;     
	
	@Expose
    private String name;
    
	private String device_category_id;
	
	private String deviceId; 
	
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

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	
}