package com.kavi.testcase;

import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import com.kavi.services.controller.RuleController;

public class RuleTestCases {

	private final static Logger logger = Logger.getLogger(RuleTestCases.class);

	public static void main(String args[]) {
		try {
			//listRuleAttributes("5a81c048f6e86a4bed1793ff","Corrosion");
			//listNotificationGroups("59b190cadf308114fcccc281");
			//upsertNotificationGroups("5a81c048f6e86a4bed1793ff");
			//listRules("59b972ccdf308114fcdf8225");
			//getRules("5ad64f44f6e86a7d0aaecfeb");
			upsertRules("60SQKIINDUVQG8HSU3UPL6O1QJ");
		} catch (Exception e) {
			e.printStackTrace();

		}
	}
	/**
	 * 	
	 * @param sessionkey
	 * @param deviceCategoryName
	 */
	private static void listRuleAttributes(String sessionkey,String deviceCategoryName) {
		RuleController controller=new RuleController();
		ResponseEntity<String> response= controller.listRuleAttributes(sessionkey,deviceCategoryName, null,null);
		System.out.println("response..."+response.getStatusCode());
		System.out.println("response..."+response.getBody());
	}
	
	private static void upsertRules(String sessionkey) {
		RuleController controller=new RuleController();
		String json = "{\"general\":{\"rule_type\":\"public\",\"name\":\"Test_Rule_69\",\"description\":\"Test_Rule_69\",\"count\":2,\"hours\":4},\"condition\":[{\"general\":{\"name\":\"condition_1\",\"description\":\"test Description\",\"id\":\"condition_2\"},\"condition_specification\":{\"group_operator\":\"and\",\"rule_element\":[],\"group_name\":\"Group 1\",\"parent_group\":\"\",\"datatype\":\"compound\"}}],\"action\":[{\"general\":{\"action_type\":\"Notification\",\"name\":\"action_1\",\"description\":\"Notification\",\"id\":\"action_1\"},\"action_parameters\":{\"action_parameters\":[{\"notification_channel\":[\"email\"],\"group_id\":\"test_3565181\"}]}}],\"rule_filter\":{\"site\":[{\"id\":\"LisleMolex\",\"name\":\"Lisle\",\"unit\":[{\"id\":\"Power Station\",\"name\":\"Power Station\",\"asset\":[{\"id\":\"Fin_Fan\",\"name\":\"Fin Fan\",\"device_category\":[{\"id\":\"Corrosion\",\"name\":\"Corrosion\",\"device\":[{\"id\":\"DL0100\",\"name\":\"DL0100\",\"channel\":[{\"id\":\"DL0100_10016\",\"name\":\"Power Station 1\"}]}]}]}]}]}],\"company_id\":\"FHR\",\"device_category\":\"Corrosion\"}}";
		String type = "activate";
		String notificationGroupId = "test_3565181";
		String ruleName = "Test_Rule_69";
		String ruleId = "";
		String notificationTopicName = "";
		String notificationType = "email";
		String dbParamJson = "{\"organizationName\":\"FHR\",\"userRole\":\"Administrator\",\"db_name\":\"FHR_DSS\",\"has_site_access\":true,\"display_name\":null,\"selected\":true}";
		ResponseEntity<String> response= controller.upsertRules(sessionkey, json, type, notificationGroupId, ruleName, ruleId, notificationTopicName, notificationType, dbParamJson);
		System.out.println("response..."+response.getStatusCode());
		System.out.println("response..."+response.getBody());
	}
	
	
}
