package com.kavi.common.utility;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;


public class S3Utility {
	 
	private final static Logger logger = Logger.getLogger(S3Utility.class);
	
	private static AmazonS3 s3Client=null;
	/**
	 * 
	 * @param fileName
	 * @param credentials
	 * @param hexdata
	 * @param s3BucketName
	 */
	public static void writeFileToS3(String fileName,BasicAWSCredentials credentials,String hexdata,String s3BucketName){
		 AWSCredentialsProvider defaultChainProvider = new AWSStaticCredentialsProvider(credentials); 
		 s3Client =  AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).withCredentials(defaultChainProvider).build();
	     try {
	    	 	 writeContentToS3(s3BucketName,fileName,hexdata,s3Client);
	    	 	
	       } catch (AmazonServiceException ase) {
	         logger.error("Caught an AmazonServiceException, which" +
	                 " means your request made it " +
	                 "to Amazon S3, but was rejected with an error response" +
	                 " for some reason.");
	         logger.error("Error Message:    " + ase.getMessage());
	         logger.error("HTTP Status Code: " + ase.getStatusCode());
	         logger.error("AWS Error Code:   " + ase.getErrorCode());
	         logger.error("Error Type:       " + ase.getErrorType());
	         logger.error("Request ID:       " + ase.getRequestId());
	      } catch (AmazonClientException ace) {
	         logger.error("Caught an AmazonClientException, which means"+
	                 " the client encountered " +
	                 "an internal error while trying to " +
	                 "communicate with S3, " +
	                 "such as not being able to access the network.");
	         logger.error("Error Message: " + ace.getMessage());
	     } 
	     s3Client=null;
	 }
	/**
	 *  
	 * @param bucket
	 * @param key
	 * @param stringToWrite
	 * @param s3Client
	 * @return
	 */
	private static PutObjectResult writeContentToS3(String bucket, String key, String stringToWrite, AmazonS3 s3Client) {
	    ObjectMetadata meta = new ObjectMetadata();
	    meta.setContentMD5(new String(com.amazonaws.util.Base64.encode(DigestUtils.md5(stringToWrite))));
	    meta.setContentLength(stringToWrite.length());
	    InputStream stream = new ByteArrayInputStream(stringToWrite.getBytes(StandardCharsets.UTF_8));
	    return s3Client.putObject(bucket, key, stream, meta);
	}
	
	/**
	 * 
	 * @param fileName
	 * @param credentials
	 * @param hexdata
	 * @param s3BucketName
	 */
	public static boolean writeFileToS3(BasicAWSCredentials credentials,String bucketName,String key,String uploadFileName,String uploadFilePath){
		boolean flag=false;
		 AWSCredentialsProvider defaultChainProvider = new AWSStaticCredentialsProvider(credentials); 
		 s3Client =  AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).withCredentials(defaultChainProvider).build();
	     try {
	    	 	File file = new File(uploadFilePath);
	    	 	s3Client.putObject(new PutObjectRequest(bucketName+"/"+key, uploadFileName, file));
	            flag=true;
	            //file.delete();
	       } catch (AmazonServiceException ase) {
	         logger.error("Caught an AmazonServiceException, which" +
	                 " means your request made it " +
	                 "to Amazon S3, but was rejected with an error response" +
	                 " for some reason.");
	         logger.error("Error Message:    " + ase.getMessage());
	         logger.error("HTTP Status Code: " + ase.getStatusCode());
	         logger.error("AWS Error Code:   " + ase.getErrorCode());
	         logger.error("Error Type:       " + ase.getErrorType());
	         logger.error("Request ID:       " + ase.getRequestId());
	         flag=false;
	      } catch (AmazonClientException ace) {
	         logger.error("Caught an AmazonClientException, which means"+
	                 " the client encountered " +
	                 "an internal error while trying to " +
	                 "communicate with S3, " +
	                 "such as not being able to access the network.");
	         logger.error("Error Message: " + ace.getMessage());
	         flag=false;
	     } 
	     return flag;
	 }
	
	/**
	 * 
	 * @param s3Client
	 * @param bucketName
	 * @param key
	 * @param hour
	 * @return
	 */
	public static String generatePreSignedURL(String bucketName,String key,int hour){
		   try{
			      
			    java.util.Date expiration = new java.util.Date();
				long milliSeconds = expiration.getTime();
				milliSeconds += 1000 * 60 * 60*hour; 
				expiration.setTime(milliSeconds);
				GeneratePresignedUrlRequest generatePresignedUrlRequest = 
					    new GeneratePresignedUrlRequest(bucketName, key);
				generatePresignedUrlRequest.setMethod(HttpMethod.GET); 
				generatePresignedUrlRequest.setExpiration(expiration);
				URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
				return url.toString();
		   }catch(Exception e){
			   e.printStackTrace();
		   }
		 return "N/A";
	 }
	
	/**
	  * 
	  * @param s3
	  * @param bucketName
	  * @param key
	  * @param webpage
	  * @return
	  */
	 public static boolean saveContentToS3(BasicAWSCredentials credentials,String bucketName, String key, String content)
	    {
		 	//s3Client = new AmazonS3Client(credentials);
		 AWSCredentialsProvider defaultChainProvider = new AWSStaticCredentialsProvider(credentials); 
		 s3Client =  AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).withCredentials(defaultChainProvider).build();
	        try
	        {
	            byte[] contentAsBytes = content.getBytes("UTF-8");
	            ByteArrayInputStream    contentsAsStream      = new ByteArrayInputStream(contentAsBytes);
	            ObjectMetadata          md = new ObjectMetadata();
	            md.setContentLength(contentAsBytes.length);
	            s3Client.putObject(new PutObjectRequest(bucketName, key, contentsAsStream, md));
	            return true;
	        }
	        catch(AmazonServiceException e)
	        {
	        	e.printStackTrace();
	            return false;
	        }
	        catch(Exception ex)
	        {
	            return false;
	        }
	    }
	 
	 
	 public static AmazonS3  establishConnectionToS3(BasicAWSCredentials credentials) {
		 AWSCredentialsProvider defaultChainProvider = new AWSStaticCredentialsProvider(credentials); 
		 s3Client =  AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).withCredentials(defaultChainProvider).build();
		 return s3Client;
	 }
	
	
}


	
	
