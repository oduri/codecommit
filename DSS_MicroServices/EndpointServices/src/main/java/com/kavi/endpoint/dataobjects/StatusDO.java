package com.kavi.endpoint.dataobjects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StatusDO{
	
	@JsonProperty("Status")
	String status;
	
	@JsonProperty("Code")
	int code;
	
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	
}
