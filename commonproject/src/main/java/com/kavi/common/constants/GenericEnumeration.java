package com.kavi.common.constants;

public enum GenericEnumeration {
	
	ACTIVE("active");
	
	private String statusCode;

	private GenericEnumeration(String code) {
		statusCode = code;
	}

	public String getStatusCode() {
		return statusCode;
	}
}
