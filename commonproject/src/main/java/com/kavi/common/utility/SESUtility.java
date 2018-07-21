package com.kavi.common.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;

public class SESUtility {
	
	private static Properties properties = new Properties(); 
	
	public SESUtility() {
		 String file = "/environment.properties";
		 InputStream inputStream=null;
		 Reader reader=null;
		 try {
			 	inputStream = getClass().getResourceAsStream(file); 
			 	reader = new InputStreamReader(inputStream);
			 	properties.load(reader);
		 }catch(Exception e) {
			 e.printStackTrace();
		 }
		 try {
				reader.close();
				inputStream.close();
			}catch(Exception e) {
				e.printStackTrace();
		 }
	}
	public AWSCredentials createAWSCredentials() throws IOException {
		AWSCredentials credentials = new BasicAWSCredentials(properties.getProperty("aws.sesAccessKey"),properties.getProperty("aws.sesSecretKey"));
		return credentials;
	}

	public AmazonSimpleEmailService createSimpleEmailService() throws IOException {
		AWSCredentialsProvider defaultChainProvider = new AWSStaticCredentialsProvider(createAWSCredentials()); 
		AmazonSimpleEmailService client =  AmazonSimpleEmailServiceClientBuilder.standard()
	              .withRegion(Regions.US_EAST_1).withCredentials(defaultChainProvider).build();
		return client;
	}
	
	public Properties getProperties() throws IOException {
		return properties;
	}

}
