package com.kavi.endpoint.dataobjects;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ErrorMessage{
	
	@JsonProperty("Code")
	private int code;
	@JsonProperty("Message")
	private String message;
	
	@JsonProperty("developerMessage")
	private String developerMessage;
	
	private int status;
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getDeveloperMessage() {
		return developerMessage;
	}
	public void setDeveloperMessage(String developerMessage) {
		this.developerMessage = developerMessage;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	
	
	
	
	
}
