package com.kavi.rule.dataobjects;


public class RuleDO{
	
	private String userId;
	private String userEmail;
	private String userPhone;
	
	private String ruleId;
	private String ruleName;
	private String ruleDescription;
	private String groupName;
    private String groupOperator;
	private String parentGroup;
	private String dataType;
	private String compoundDetails;
	
	public String getRuleId() {
		return ruleId;
	}
	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public String getRuleDescription() {
		return ruleDescription;
	}
	public void setRuleDescription(String ruleDescription) {
		this.ruleDescription = ruleDescription;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getGroupOperator() {
		return groupOperator;
	}
	public void setGroupOperator(String groupOperator) {
		this.groupOperator = groupOperator;
	}
	public String getParentGroup() {
		return parentGroup;
	}
	public void setParentGroup(String parentGroup) {
		this.parentGroup = parentGroup;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getCompoundDetails() {
		return compoundDetails;
	}
	public void setCompoundDetails(String compoundDetails) {
		this.compoundDetails = compoundDetails;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public String getUserPhone() {
		return userPhone;
	}
	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}
   
}