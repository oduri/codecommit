package com.kavi.services.dataobjects;

public class LdapUser {
	
	private String displayName;
	private String email;
	private String groupName;
	private String userId;
	private String encodedImgString;
	private String organizationName;
	private String objectId;
	
	public String getOrganizationName() {
		return organizationName;
	}
	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getEncodedImgString() {
		return encodedImgString;
	}
	public void setEncodedImgString(String encodedImgString) {
		this.encodedImgString = encodedImgString;
	}
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

}
