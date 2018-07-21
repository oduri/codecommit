package com.kavi.testcase;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.http.ResponseEntity;

import com.kavi.services.controller.TicketController;
import com.kavi.services.controller.UserController;
import com.kavi.user.dataobjects.TicketResponseDO;

public class UserTestCases {
	
	private final static Logger logger = Logger.getLogger(UserTestCases.class);
	public static void main(String args[]) 
	{
		//getLocationTreeForUser("58ffa3ffac7177588791c9f5");
		//testUpsertTicketObject();
		//getLocationTreeHierarchyForUser("5ade2694f6e86a20cb76e74f");//My Local machine
		getLocationTreeHierarchyForUser("4Q88B69FINJ3MQQHK1UKDQL08");
		//getLocationTreeHierarchy("user2");
		//loadAllUsersFromLDAP("59a96cd1df308114fc11e234");
		//recursiveTestForLocationHierarchy("rohit");
		//uploadLocationHierarchy();
		//updateLocationHierarchyObject("591c88d7a3916c4552b56b65");
		//previewLocationTreeHierarchy("5a96cf81f6e86a4beda5e8da","Complete");
		//
		//dupCheck();
		//upsertLDAPObject("590cd220917b761c7ca6ec0e");
		//loadAllRolesFromLDAP("5995d369df308114fc2928c1");
		//getAllSitesForLocation("Koch Ag");
		// upsertLocationHierarchyObject();
		//editLocationTreeHierarchy("5ab3c952f6e86a422f3f14ce");
		//editLocationTreeHierarchy("5aba6bb8f6e86a422f160fdd");
		//previewLocationTreeHierarchy("5a96cf81f6e86a4beda5e8da","Complete");
		//previewLocationTreeHierarchy("5a792f8bf6e86a4bed22ecc8","Complete");
		//getLocationTreeHierarchyForAdminUser("5a96cf81f6e86a4beda5e8da");
		//downloadTemplate("591c80e3a3916c455252ccd7");
		//checkDuplicateObjectId("591c80e3a3916c455252ccd7","unit1","unit");
		//downloadExistingLocationHierarchy("591c80e3a3916c455252ccd7");
		//checkThumbNail();
		//checkImg();
		//uploadDeviceImages();
		//uploadFile();
		//deleteFile("C:\\Users\\Saravanan\\Sample.bin");
		//updateOrganizationName("595e91718b58eb05de3adb94","Koch ag"); 
		//updateSite("5aabc011f2c4e9e4029f839b");
		//getSitesFromTempTable("bhanumathi.ramaiah@kaviglobal.com","koch_ag");
		//checkDuplicateObjectsAndGenerateId("5abaa6c1f6e86a422f286787");
	}
	/**
	 * 
	 * @param sessionkey
	 */
	private static void getLocationTreeHierarchyForUser(String sessionkey) {
		UserController user=new UserController();
		Document doc=new Document();
		doc.put("organizationName","koch_ag");
		doc.put("db_name","koch_ag_DSS");
		doc.put("has_site_access",false);
		doc.put("user_role","Administrator");
		ResponseEntity<String> response =user.getLocationTreeHierarchyForUser(sessionkey,doc.toJson());
		System.out.println("response..."+response.getStatusCode());
		System.out.println("response..."+response.getBody());
	}
	
	private static void testUpsertTicketObject() {
		TicketController ticketController = new TicketController();
		//TicketDO ticketDO = new TicketDO("test1","High","test module","test description");
		Document doc=new Document();
		doc.put("title","test title");
		doc.put("severity","High");
		doc.put("module","test module");
		doc.put("description","test description");
		TicketResponseDO ticketResponseDO = ticketController.upsertTicketObject("5b1a581273836f2794ae5e00", doc.toJson(),"", null,null);
		System.out.println("Response: "+ticketResponseDO);
	}

}
