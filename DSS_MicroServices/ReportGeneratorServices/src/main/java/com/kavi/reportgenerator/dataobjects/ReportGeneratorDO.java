package com.kavi.reportgenerator.dataobjects;

import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.mongo.db.MongoDBConnection;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


public class ReportGeneratorDO {
	
	private String fileName;
	private String query;
	private String emailId;
	private String reportId;
	private String userId;
	private Document reportConfigObject;
	private MongoDBConnection mongoSingle;
	private MongoCollection<Document> table;
	private MongoDatabase db;
	private BasicDBObject jsonDocument;
	private Document locationHierarchObject;
	private Map<String,Document> lDeviceTransactionDocument;
	private AmazonS3 s3Client; 
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	public String getReportId() {
		return reportId;
	}
	public void setReportId(String reportId) {
		this.reportId = reportId;
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public MongoDatabase getDb() {
		return db;
	}
	public void setDb(MongoDatabase db) {
		this.db = db;
	}
	public Document getLocationHierarchObject() {
		return locationHierarchObject;
	}
	public void setLocationHierarchObject(Document locationHierarchObject) {
		this.locationHierarchObject = locationHierarchObject;
	}
	public MongoDBConnection getMongoSingle() {
		return mongoSingle;
	}
	public void setMongoSingle(MongoDBConnection mongoSingle) {
		this.mongoSingle = mongoSingle;
	}
	public MongoCollection<Document> getTable() {
		return table;
	}
	public Map<String,Document> getlDeviceTransactionDocument() {
		return lDeviceTransactionDocument;
	}
	public void setlDeviceTransactionDocument(Map<String,Document> lDeviceTransactionDocument) {
		this.lDeviceTransactionDocument = lDeviceTransactionDocument;
	}
	public void setTable(MongoCollection<Document> table) {
		this.table = table;
	}
	
	
	public Document getReportConfigObject() {
		return reportConfigObject;
	}
	public void setReportConfigObject(Document reportConfigObject) {
		this.reportConfigObject = reportConfigObject;
	}
	public BasicDBObject getJsonDocument() {
		return jsonDocument;
	}
	public void setJsonDocument(BasicDBObject jsonDocument) {
		this.jsonDocument = jsonDocument;
	}
	public AmazonS3 getS3Client() {
		return s3Client;
	}
	public void setS3Client(AmazonS3 s3Client) {
		this.s3Client = s3Client;
	}

	
}
