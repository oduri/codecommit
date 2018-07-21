package com.kavi.reportgenerator.dataobjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

public class ReportGeneratorTableDO {
	
	private String siteId;
	
	private String siteName;
	
	private String companyName;
	
	private String unitName;
	
	private Map<Integer,String> lStatusMap=new HashMap<Integer,String>();
	
	private List<Document> assetList=new ArrayList<Document>();
	
	public String getSiteId() {
		return siteId;
	}
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
	public String getUnitName() {
		return unitName;
	}
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
	
	
	
	public Map<Integer,String> getlStatusMap() {
		return lStatusMap;
	}
	public void setlStatusMap(Map<Integer,String> lStatusMap) {
		this.lStatusMap = lStatusMap;
	}
	public List<Document> getAssetList() {
		return assetList;
	}
	public void setAssetList(List<Document> assetList) {
		this.assetList = assetList;
	}
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
		
}
