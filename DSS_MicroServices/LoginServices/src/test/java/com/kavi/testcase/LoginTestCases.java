package com.kavi.testcase;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.springframework.http.ResponseEntity;

import com.kavi.common.utility.CommonUtility;
import com.kavi.services.controller.LoginController;
import com.kavi.services.controller.MultiSessionLoginController;
import com.sun.org.apache.xml.internal.security.utils.Base64;


public class LoginTestCases {
	
	private final static Logger logger = Logger.getLogger(LoginTestCases.class);
	public static void main(String args[]) 
	{
		try {
			
			//testLogin("fhr","1373b90360578f26e4eac5b6ba1c41a6");
			//testMultiSessionLogin("sheeba","e4cf69391d7692b030adc5ea75775cee");
			//testMultiSessionLogin("fhr","1373b90360578f26e4eac5b6ba1c41a6");
			//testMultiSessionLogin("selva","46b120b0de70a445dbe3a9cb8329c2de");
			//testMultiSessionLogin("sara", "930d9a5e042ca9de1465533741c47060");
			//testMultiSessionLogout();
			//testLogin("sailaja","12345");
			//testLogout("5b178db8cb8ef3420cbdeada");
			changePassword("1373b90360578f26e4eac5b6ba1c41a6","46b120b0de70a445dbe3a9cb8329c2de","IQHEVRR14P17BVNKED8I41FK4B");
			//testForgotPassword("sheeba");
			//testMD5Hashing("12345");
			//testNRVLogin("fhr","Kavi@123");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void changePassword(String changePassword,String oldPassword,String sessionkey) {
		LoginController controller=new LoginController();
		ResponseEntity<String> response= controller.changePassword(changePassword, oldPassword, sessionkey, null,null);
		System.out.println("response..."+response.getStatusCode());
		System.out.println("response..."+response.getBody());
	}
	
	
	/**
	 * 
	 * @param sessionkey
	 * @param deviceCategoryName
	 */
	private static void testNRVLogin(String userId,String userPassword) {
		LoginController controller=new LoginController();
		/*ResponseEntity<String> response= controller.testNRVLogin(userId, userPassword, null);
		System.out.println("response..."+response.getStatusCode());
		System.out.println("response..."+response.getBody());*/
	}
	
	private static void testMultiSessionLogout() {
		MultiSessionLoginController controller=new MultiSessionLoginController();
		ResponseEntity<String> response= controller.multiSessionLogout("","\"company\": [{\"organizationName\": \"FHR\",\"db_name\": \"FHR_DSS\"},{\"organizationName\": \"koch_ag\",\"db_name\": \"koch_ag_DSS\"}]}");
		//System.out.println("response..."+response.getStatusCode());
		//System.out.println("response..."+response.getBody());
	}
	
	private static void testForgotPassword(String userId) {
		LoginController controller=new LoginController();
		ResponseEntity<String> response = controller.forgotPassword(userId);
		System.out.println("Final response: "+response);
	}
	
	private static String testMD5Hashing(String hashString) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String hashedString = null;
		hashString = hashString+"Molex_Dss";
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(hashString.getBytes("UTF8"));
		byte[] bytes = digest.digest();
		StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        hashedString = sb.toString();
		System.out.println("hashedString: "+hashedString);
		return hashedString;
	}
	
	private static void testMultiSessionLogin(String userId,String userPassword) {
		MultiSessionLoginController controller=new MultiSessionLoginController();
		ResponseEntity<String> response= controller.multiSessionLogin(userId, userPassword);
		System.out.println("response..."+response.getStatusCode());
		System.out.println("response..."+response.getBody());
	}
	
}
