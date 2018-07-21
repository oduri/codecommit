package com.kavi.common.utility;

import java.io.PrintWriter;
import java.io.StringWriter;


public class MessageUtility {
	/**
	 * 
	 * @param status
	 * @param statusCode
	 * @param statusMessage
	 * @return
	 */
	public static String updateMessage(StringBuilder status,long statusCode,String statusMessage) {
		status.append(",");
		status.append("\"statusCode\":");
		status.append(statusCode);
		status.append(",");
		status.append("\"statusMessage\":");
		status.append("\""+statusMessage+"\"");
		status.append("}");
		return status.toString();
	}
	/**
	 * 
	 * @param status
	 * @param statusCode
	 * @param statusMessage
	 * @param error
	 * @return
	 */
	public static String updateMessageWithErrors(StringBuilder status,long statusCode,String statusMessage,Exception error) {
		status.append(",");
		status.append("\"statusCode\":");
		status.append(statusCode);
		status.append(",");
		status.append("\"statusMessage\":");
		status.append("\""+statusMessage+"\"");
		status.append(",");
	    status.append("\"statusErrorMessage\":");
		status.append("\""+getStackTrace(error)+"\"");
		status.append("}");
		return status.toString();
	}
	/**
	 * 
	 * @param throwable
	 * @return
	 */
	private static String getStackTrace(final Throwable throwable) {
	    final StringWriter sw = new StringWriter();
	     final PrintWriter pw = new PrintWriter(sw, true);
	     throwable.printStackTrace(pw);
	     String updString=sw.getBuffer().toString().replaceAll(System.getProperty("line.separator"), " ");
	     updString=updString.replace(" 	", "");
	     return (updString);
	}

	/**
	 * 
	 * @param status
	 * @param statusCode
	 * @param statusMessage
	 * @param arrayName
	 * @param jsonArray
	 * @return
	 */
	public static String updateArrayMessage(StringBuilder status,long statusCode,String statusMessage,String arrayName,String jsonArray) {
		status.append(",");
		status.append("\"statusCode\":");
		status.append(statusCode);
		status.append(",");
		status.append("\"statusMessage\":");
		status.append("\""+statusMessage+"\"");
		status.append(",");
		status.append("\""+arrayName+"\":");
		status.append(jsonArray);
		status.append("}");
		return status.toString();
	}
	
	
	/**
	 * 
	 * @param status
	 * @param statusCode
	 * @param statusMessage
	 * @param exception
	 * @return
	 */
	public static String updateErrorMessage(StringBuffer status,long statusCode,String statusMessage,Exception exception) {
		
		StringBuffer exceptionBuffer = new StringBuffer();
		StringBuffer exceptionMessage = new StringBuffer();
		StackTraceElement[] listElements=exception.getStackTrace();
		if(listElements!=null && listElements.length>0){
			for(StackTraceElement elements:listElements){
				if("DBPort.java".equalsIgnoreCase(elements.getFileName())){
					   exceptionBuffer=new StringBuffer();
					   exceptionBuffer.append("{");
					   exceptionBuffer.append("\"status\":");
					   exceptionBuffer.append("\"MetaData Connection\"");
					   exceptionBuffer.append(",");
					   exceptionBuffer.append("\"statusCode\":");
					   exceptionBuffer.append(2001);
					   exceptionBuffer.append(",");
					   exceptionBuffer.append("\"statusMessage\":");
					   exceptionBuffer.append("\"MetaData Connection Failure\"");
					   exceptionBuffer.append("}");
					   break;
				}else{
					
					   exceptionBuffer=new StringBuffer();
					   exceptionBuffer.append("{");
					   exceptionBuffer.append("\"status\":");
					   exceptionBuffer.append("\"Java Exception\"");
					   exceptionBuffer.append(",");
					   exceptionBuffer.append("\"statusCode\":");
					   exceptionBuffer.append(statusCode);
					   exceptionMessage=new StringBuffer();
					   exceptionMessage.append(elements.getFileName());
					   exceptionMessage.append(",");
					   exceptionMessage.append(elements.getMethodName());
					   exceptionMessage.append(",");
					   exceptionMessage.append(elements.getLineNumber());
					   exceptionMessage.append(",");
					   exceptionMessage.append(elements.getClassName());
					   exceptionMessage.append(",");
					   exceptionBuffer.append("\"statusMessage\":");
					   exceptionBuffer.append("\""+exceptionMessage+"\"");
					   exceptionBuffer.append("}");
					   break;
				}
			}
		}
		
		return exceptionBuffer.toString();
	}
	
}
