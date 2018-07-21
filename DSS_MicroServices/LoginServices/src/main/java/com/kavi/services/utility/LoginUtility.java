package com.kavi.services.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import org.json.simple.JSONArray;

import com.kavi.common.constants.CommonConstants;
import com.kavi.common.utility.CommonUtility;
import com.kavi.common.utility.MailUtility;
import com.kavi.common.utility.SESUtility;
import com.kavi.services.dataobjects.LdapUser;
import com.kavi.services.impl.AWSEmailServiceImpl;
import com.kavi.services.intf.EmailService;

public class LoginUtility {
	
	public static boolean sendMailViaSES(String userId, String temporaryPassword, String emailId) {
		boolean status = false;
		try {
		 SESUtility sesUtil = new SESUtility();
		 Properties properties=sesUtil.getProperties();
		 properties.setProperty("mail.transport.protocol", "aws");
		 properties.setProperty("mail.aws.user", properties.getProperty("aws.sesAccessKey"));
		 properties.setProperty("mail.aws.password",  properties.getProperty("aws.sesSecretKey"));
		 EmailService emailService = new AWSEmailServiceImpl(sesUtil.createSimpleEmailService());
		 String body = MailUtility.getForgotEmailBody(userId, temporaryPassword)==null?null:MailUtility.getForgotEmailBody(userId, temporaryPassword).toString();
		 emailService.withFrom(CommonConstants.SMTP_AUTH_USER)
			.withTo(emailId)
			.withSubject("-- mSyte temporary password")
			.withBody(body)
			.sendEmail();
		 status = true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return status;
	}
	
	public static Properties initializeProperties(){
		Properties prop = new Properties();
		String file = "/environment.properties";
   		InputStream inputStream = LoginUtility.class.getResourceAsStream(file); 
   		Reader reader = new InputStreamReader(inputStream);
		try {
			prop.load(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			reader.close();
			inputStream.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return prop;
	}
	
	public static void updateLoginResponseObject(JSONArray companyArray, String sessionkey, StringBuilder sb, LdapUser ldapUser,
			JSONArray organizationArray) {
		Map<String, String> lEnvironmentMap = CommonUtility.initializeEnviromentProperties();
		sb.append("\"Success\"");
		sb.append(",");
		sb.append("\"userId\":");
		sb.append("\"" + ldapUser.getUserId() + "\"");
		sb.append(",");
		sb.append("\"userDisplayName\":");
		sb.append("\"" + ldapUser.getDisplayName() + "\"");
		sb.append(",");
		sb.append("\"sessionkey\":");
		sb.append("\"" + sessionkey + "\"");
		sb.append(",");
		sb.append("\"company\":");
		sb.append(companyArray);
		sb.append(",");
		sb.append("\"displayImage\":");
		sb.append("\"" + ldapUser.getEncodedImgString().replaceAll("\n", "") + "\"");
		sb.append(",");
		if (organizationArray.size() > 0) {
			sb.append("\"organization\":");
			sb.append(organizationArray);
			sb.append(",");
		}
		sb.append("\"encodeType\":");
		sb.append("\"base64\"");
		sb.append(",");
		sb.append("\"image_path\":");
		sb.append("\"" + lEnvironmentMap.get("image_path") + "\"");
	}

}
