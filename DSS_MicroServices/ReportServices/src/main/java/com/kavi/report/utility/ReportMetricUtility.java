package com.kavi.report.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.bson.Document;

import com.mongodb.BasicDBObject;

public class ReportMetricUtility {
	
	/**
	 * 
	 * @return
	 */
	public static List<Document> frameQueryForUtilization(BasicDBObject json) {
		
		Document pushDocument = new Document();
		pushDocument.put("asset_name", "$asset_name");
		List<Document> metricListQuery=new ArrayList<Document>();
		Document projectFields = new Document();	
		projectFields.put("site_id", 1);
		projectFields.put("asset_id", 1);
		projectFields.put("utilPercentageSum", 1);
		projectFields.put("totalCount", 1);
		projectFields.put("site_id", "$_id.site_id");
		projectFields.put("asset_id", "$_id.asset_id");
		Document groupByFields=new Document();
		groupByFields.put("site_id", "$site_id");
		groupByFields.put("asset_id", "$asset_id");
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
		Date fromDate = formatDate(simpleDateFormat, json, "from_date");
		Calendar cal = Calendar.getInstance();
		cal.setTime(fromDate);
		int year = cal.get(Calendar.YEAR);
		int monthNumber = cal.get(Calendar.MONTH);
		int dateNumber = cal.get(Calendar.DAY_OF_MONTH);
		monthNumber += 1;
		Date toDate = formatDate(simpleDateFormat, json, "to_date");
		cal = Calendar.getInstance();
		cal.setTime(toDate);

		int toYear = cal.get(Calendar.YEAR);
		int toMonthNumber = cal.get(Calendar.MONTH);
		int toDateNumber = cal.get(Calendar.DAY_OF_MONTH);
		toMonthNumber += 1;
		
		Document searchQuery=new Document();
		searchQuery.put("site_id", json.getString("site_id"));
		if(json.get("device_category")!=null) {
			BasicDBObject inQuery = new BasicDBObject();
			inQuery.put("$in", json.get("device_category"));
			searchQuery.put("device_category",inQuery);
		}
		if(json.getString("asset_id")!=null) {
			BasicDBObject inQuery = new BasicDBObject();
			inQuery.put("$in", json.get("asset_id"));
			searchQuery.put("asset_id",inQuery);
		}
		searchQuery.put("summary_date",
				new BasicDBObject("$gte",
						new DateTime(year, monthNumber, dateNumber, 0, 0, DateTimeZone.UTC)
								.toDate()).append("$lte",
										new DateTime(toYear, toMonthNumber, toDateNumber, 23, 59,
												DateTimeZone.UTC).toDate()));
		
		if(json.getString("period").equalsIgnoreCase("overallnew")) {
			
			Document utilPercentageGroup = new Document(
				    "$group", new Document("_id", groupByFields).append("asset", new Document("$addToSet", pushDocument)).append(
				     "utilPercentageSum", new Document( "$sum", "$util_perc" )).append("totalCount", new Document( "$sum", 1 ))
				);
			projectFields.put("asset", "$asset");
			projectFields.put("_id", 0);
			Document matchQuery=new Document();
			matchQuery.put("$match", searchQuery);
			metricListQuery.add(matchQuery);
			metricListQuery.add(utilPercentageGroup);
			Document projectQuery = new Document();	
			projectQuery.put("$project",projectFields);
			metricListQuery.add(projectQuery);
		}
		else if(json.getString("period").equalsIgnoreCase("overall")) {
			
			Document utilPercentageGroup = new Document(
				    "$group", new Document("_id", groupByFields).append(
				     "utilPercentageSum", new Document( "$sum", "$util_perc" )).append("totalCount", new Document( "$sum", 1 ))
				);
			projectFields.put("_id", 0);
			Document matchQuery=new Document();
			matchQuery.put("$match", searchQuery);
			metricListQuery.add(matchQuery);
			metricListQuery.add(utilPercentageGroup);
			Document projectQuery = new Document();	
			projectQuery.put("$project",projectFields);
			metricListQuery.add(projectQuery);
		}
		else if(json.getString("period").equalsIgnoreCase("month")) {
			
			Document yearfields=new Document();
			yearfields.put("$year", "$summary_date");
			Document monthfields=new Document();
			monthfields.put("$month", "$summary_date");
			groupByFields.put("month",monthfields);
			groupByFields.put("year",yearfields);
			Document utilPercentageGroup = new Document(
				    "$group", new Document("_id", groupByFields).append(
				     "utilPercentageSum", new Document( "$sum", "$util_perc" )).append("totalCount", new Document( "$sum", 1 ))
				);
			projectFields.put("year","$_id.year");
			projectFields.put("month", "$_id.month");
			projectFields.put("_id", 0);
			Document matchQuery=new Document();
			matchQuery.put("$match", searchQuery);
			metricListQuery.add(matchQuery);
			metricListQuery.add(utilPercentageGroup);
			Document projectQuery = new Document();	
			projectQuery.put("$project",projectFields);
			metricListQuery.add(projectQuery);
		}
		else if(json.getString("period").equalsIgnoreCase("day")) {
			Document yearfields=new Document();
			yearfields.put("$year", "$summary_date");
			Document monthfields=new Document();
			monthfields.put("$month", "$summary_date");
			Document dayfields=new Document();
			dayfields.put("$dayOfMonth", "$summary_date");
			groupByFields.put("month",monthfields);
			groupByFields.put("year",yearfields);
			groupByFields.put("day",dayfields);
			
			Document utilPercentageGroup = new Document(
				    "$group", new Document("_id", groupByFields).append(
				     "utilPercentageSum", new Document( "$sum", "$util_perc" )).append("totalCount", new Document( "$sum", 1 ))
				);
			
			projectFields.put("year","$_id.year");
			projectFields.put("month", "$_id.month");
			projectFields.put("day", "$_id.day");
			projectFields.put("_id", 0);
			Document matchQuery=new Document();
			matchQuery.put("$match", searchQuery);
			metricListQuery.add(matchQuery);
			metricListQuery.add(utilPercentageGroup);
			Document projectQuery = new Document();	
			projectQuery.put("$project",projectFields);
			metricListQuery.add(projectQuery);
			
		}
		return metricListQuery;
	}
	
	/**
	 * 
	 * @param simpleDateFormat
	 * @return
	 */
	private static Date formatDate(SimpleDateFormat simpleDateFormat,BasicDBObject json,String jsonkey) {
		Date date=new Date();
		try {
				date = simpleDateFormat.parse(json.getString(jsonkey));
			return date;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
}
