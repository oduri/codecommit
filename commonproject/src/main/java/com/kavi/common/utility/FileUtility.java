package com.kavi.common.utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.amazonaws.auth.BasicAWSCredentials;
import com.kavi.common.constants.CommonConstants;


public class FileUtility {
	 
	private final static Logger logger = Logger.getLogger(FileUtility.class);
	
	/**
	 * 
	 * @param uploadedInputStream
	 * @param uploadedFileLocation
	 */
	public static void writeToFile(InputStream uploadedInputStream,
		String uploadedFileLocation) {
		OutputStream out=null;
		try {
			out = new FileOutputStream(new File(
					uploadedFileLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			uploadedInputStream.close();
			deleteFile(uploadedFileLocation);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
   }
	
	/**
	 * 
	 * @param inputStream
	 * @param fileName
	 */
	public static void convertBinaryToHex(InputStream inputStream,String fileName,String s3BucketName){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		byte[] content = new byte[ 1024 ];  
		int bytesRead = -1;  
	    try {
			while( ( bytesRead = inputStream.read( content ) ) != -1 ) {  
			     baos.write( content, 0, bytesRead );  
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	    createHexFile(fileName,baos,s3BucketName);
	    
	}
	
	/**
	 * 
	 * @param fileName
	 * @param message
	 * @param s3BucketName
	 */
	private static void createHexFile(String fileName,ByteArrayOutputStream message,String s3BucketName) {
		  byte[] bArray=message.toByteArray();
	      String hexString=new String();
	      for(byte byt:bArray){
	    	  hexString+=toHexString(byt);
	      }
	     BasicAWSCredentials credentials = new BasicAWSCredentials(CommonConstants.S3_ACCESS_KEY,CommonConstants.S3_SECRET_KEY);
	     S3Utility.writeFileToS3(fileName, credentials, hexString,s3BucketName);
	}
	
	/**
	 * 
	 * @param b
	 * @return
	 */
	public static String toHexString(byte b) {
	    return String.format("%02X", b);
    }
	
   /**
    * 	
    * @param fileLocation
    */
   private static void deleteFile(String fileLocation){
	   File f=new File(fileLocation);
	   f.deleteOnExit();
   }
   
}


	
	
