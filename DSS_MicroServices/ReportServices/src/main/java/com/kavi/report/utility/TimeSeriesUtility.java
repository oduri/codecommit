package com.kavi.report.utility;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import com.mongodb.BasicDBObject;

public class TimeSeriesUtility {
	 /**
	    * 
	    * @param document
	    * @param type
	    * @return
	    */
	    public static Document getSpecificDetailsForTimeSeriesReport(BasicDBObject document,String type){
	    	Document selectedFields = new Document("_id",1);
	    	if(document.getString("deviceCategoryName").equalsIgnoreCase("Vibration")){
	    	if(document.getString("type")!=null && document.getString("sub_type")!=null){
	    		if(document.getString("type").equalsIgnoreCase("All") && document.getString("sub_type").equalsIgnoreCase("All")){
	    			selectedFields.put("rms_accel_x",1);
	    			selectedFields.put("rms_accel_y",1);
	    			selectedFields.put("rms_accel_z",1);
	    			selectedFields.put("rms_accel_x_cmnts",1);
	    			selectedFields.put("rms_accel_y_cmnts",1);
	    			selectedFields.put("rms_accel_z_cmnts",1);
	    			selectedFields.put("rms_vel_x",1);
	    			selectedFields.put("rms_vel_y",1);
	    			selectedFields.put("rms_vel_z",1);
	    			selectedFields.put("rms_vel_x_cmnts",1);
	    			selectedFields.put("rms_vel_y_cmnts",1);
	    			selectedFields.put("rms_vel_z_cmnts",1);
	    			selectedFields.put("pp_accel_x",1);
	    			selectedFields.put("pp_accel_y",1);
	    			selectedFields.put("pp_accel_z",1);
	    			selectedFields.put("pp_accel_x_cmnts",1);
	    			selectedFields.put("pp_accel_y_cmnts",1);
	    			selectedFields.put("pp_accel_z_cmnts",1);
	    			selectedFields.put("pp_vel_x",1);
	    			selectedFields.put("pp_vel_y",1);
	    			selectedFields.put("pp_vel_z",1);
	    			selectedFields.put("pp_vel_x_cmnts",1);
	    			selectedFields.put("pp_vel_y_cmnts",1);
	    			selectedFields.put("pp_vel_z_cmnts",1);
	    			/*selectedFields.put("pp_accel_alarm",1);
	    			selectedFields.put("pp_accel_alert",1);
	    			selectedFields.put("pp_vel_alarm",1);
	    			selectedFields.put("pp_vel_alert",1);
	    			selectedFields.put("rms_accel_alarm",1);
	    			selectedFields.put("rms_accel_alert",1);
	    			selectedFields.put("rms_vel_alarm",1);
	    			selectedFields.put("rms_vel_alert",1);*/
		    	}
		    	else if(document.getString("type").equalsIgnoreCase("rms") && document.getString("sub_type").equalsIgnoreCase("All")){
		    		selectedFields.put("rms_accel_x",1);
	    			selectedFields.put("rms_accel_y",1);
	    			selectedFields.put("rms_accel_z",1);
	    			selectedFields.put("rms_accel_x_cmnts",1);
	    			selectedFields.put("rms_accel_y_cmnts",1);
	    			selectedFields.put("rms_accel_z_cmnts",1);
	    			selectedFields.put("rms_vel_x",1);
	    			selectedFields.put("rms_vel_y",1);
	    			selectedFields.put("rms_vel_z",1);
	    			selectedFields.put("rms_vel_x_cmnts",1);
	    			selectedFields.put("rms_vel_y_cmnts",1);
	    			selectedFields.put("rms_vel_z_cmnts",1);

		    		
		    	}
		    	else if(document.getString("type").equalsIgnoreCase("rms") && document.getString("sub_type").equalsIgnoreCase("x_accel")){
		    		//selectedFields.put("rms.x_accel",1);
		    		selectedFields.put("rms_accel_x",1);
	    			selectedFields.put("rms_accel_x_cmnts",1);
		    		selectedFields.put("rms_vel_x",1);
	    			selectedFields.put("rms_vel_x_cmnts",1);
		   
		    	}
		    	else if(document.getString("type").equalsIgnoreCase("rms") && document.getString("sub_type").equalsIgnoreCase("y_accel")){
		    		//selectedFields.put("rms.y_accel",1);
		    		selectedFields.put("rms_accel_y",1);
		    		selectedFields.put("rms_accel_y_cmnts",1);
		    		selectedFields.put("rms_vel_y",1);
		    		selectedFields.put("rms_vel_y_cmnts",1);

		    	}
		    	else if(document.getString("type").equalsIgnoreCase("rms") && document.getString("sub_type").equalsIgnoreCase("z_accel")){
		    		selectedFields.put("rms_accel_z",1);
		    		selectedFields.put("rms_accel_z_cmnts",1);
		    		selectedFields.put("rms_vel_z",1);
		    		selectedFields.put("rms_vel_z_cmnts",1);

		    	}
		    	else if(document.getString("type").equalsIgnoreCase("pp") && document.getString("sub_type").equalsIgnoreCase("All")){
	    			selectedFields.put("pp_accel_x",1);
	    			selectedFields.put("pp_accel_y",1);
	    			selectedFields.put("pp_accel_z",1);
	    			selectedFields.put("pp_accel_x_cmnts",1);
	    			selectedFields.put("pp_accel_y_cmnts",1);
	    			selectedFields.put("pp_accel_z_cmnts",1);
	    			selectedFields.put("pp_vel_x",1);
	    			selectedFields.put("pp_vel_y",1);
	    			selectedFields.put("pp_vel_z",1);
	    			selectedFields.put("pp_vel_x_cmnts",1);
	    			selectedFields.put("pp_vel_y_cmnts",1);
	    			selectedFields.put("pp_vel_z_cmnts",1);

		    		
		    	}
		    	else if(document.getString("type").equalsIgnoreCase("pp") && document.getString("sub_type").equalsIgnoreCase("x_accel")){
		    		selectedFields.put("pp_accel_x",1);
		    		selectedFields.put("pp_accel_x_cmnts",1);
		    		selectedFields.put("pp_vel_x",1);
		    		selectedFields.put("pp_vel_x_cmnts",1);
		    		
		    	}
		    	else if(document.getString("type").equalsIgnoreCase("pp") && document.getString("sub_type").equalsIgnoreCase("y_accel")){
		    		selectedFields.put("pp_accel_y",1);
		    		selectedFields.put("pp_accel_y_cmnts",1);
		    		selectedFields.put("pp_vel_y",1);
		    		selectedFields.put("pp_vel_y_cmnts",1);
		    			    		
		    	}
		    	else if(document.getString("type").equalsIgnoreCase("pp") && document.getString("sub_type").equalsIgnoreCase("z_accel")){
		    		selectedFields.put("pp_accel_z",1);
		    		selectedFields.put("pp_accel_z_cmnts",1);
		    		selectedFields.put("pp_vel_z",1);
		    		selectedFields.put("pp_vel_z_cmnts",1);

		    	}
	    	 }
	    	selectedFields.put("rms_accel_alarm_x",1);
	    	selectedFields.put("rms_accel_alarm_y",1);
	    	selectedFields.put("rms_accel_alarm_z",1);

	    	selectedFields.put("rms_accel_alert_x",1);
	    	selectedFields.put("rms_accel_alert_y",1);
	    	selectedFields.put("rms_accel_alert_z",1);    	

	    	selectedFields.put("pp_accel_alarm_x",1);
	    	selectedFields.put("pp_accel_alarm_y",1);
	    	selectedFields.put("pp_accel_alarm_z",1);    	

	    	selectedFields.put("pp_accel_alert_x",1);
	    	selectedFields.put("pp_accel_alert_y",1);
	    	selectedFields.put("pp_accel_alert_z",1);
	    	
	    	selectedFields.put("rms_vel_alarm_x",1);
	    	selectedFields.put("rms_vel_alarm_y",1);
	    	selectedFields.put("rms_vel_alarm_z",1);

	    	selectedFields.put("rms_vel_alert_x",1);
	    	selectedFields.put("rms_vel_alert_y",1);
	    	selectedFields.put("rms_vel_alert_z",1);    	

	    	selectedFields.put("pp_vel_alarm_x",1);
	    	selectedFields.put("pp_vel_alarm_y",1);
	    	selectedFields.put("pp_vel_alarm_z",1);    	

	    	selectedFields.put("pp_vel_alert_x",1);
	    	selectedFields.put("pp_vel_alert_y",1);
	    	selectedFields.put("pp_vel_alert_z",1);
	    	selectedFields.put("temperature",1);
	      }
	    	
	    	
	    	else if(document.getString("deviceCategoryName").equalsIgnoreCase("Corrosion")){
	    		if(document.getString("type")!=null){
	    			if(document.getString("type").equals("All")){
	    				//selectedFields.put("db_parameters",1);
	    				selectedFields.put("thickness_algorithm",1);
	    				selectedFields.put("thickness",1);
	    				selectedFields.put("thickness_maxpeak",1);
	    				selectedFields.put("thickness_flank",1);
	    				selectedFields.put("thickness_zerocrossing",1);
	    				selectedFields.put("thickness_cmnts",1);
		    			selectedFields.put("temperature_corrected_thickness",1);
		    			selectedFields.put("temperature_corrected_thickness_maxpeak",1);
		    			selectedFields.put("temperature_corrected_thickness_flank",1);
		    			selectedFields.put("temperature_corrected_thickness_zerocrossing",1);
		    			selectedFields.put("temperature_corrected_thickness_cmnts",1);
		    			selectedFields.put("probe_temperature",1);
		    			selectedFields.put("probe_temperature_cmnts",1);
	    			}
	    			if(document.getString("type").equals("Thickness")){
	    				//selectedFields.put("db_parameters",1);
	    				selectedFields.put("thickness",1);
	    				selectedFields.put("thickness_maxpeak",1);
	    				selectedFields.put("thickness_flank",1);
	    				selectedFields.put("thickness_cmnts",1);
	    			}
	    			if(document.getString("type").equals("TCThickness")){
	    				selectedFields.put("temperature_corrected_thickness",1);
		    			selectedFields.put("temperature_corrected_thickness_cmnts",1);
	    			}	 
	    			if(document.getString("type").equals("Temperature_Old")){
	    				selectedFields.put("probe_temperature",1);
		    			selectedFields.put("probe_temperature_cmnts",1);
	    			}	 
		    		
	    		}
	    	}
	    	else if(document.getString("deviceCategoryName").equalsIgnoreCase("Temperature_Old")){
	    			selectedFields.put("data",1);
	    			selectedFields.put("header.temperature_date",1);
	    		}
	    	if(document.getString("document_id")!=null && document.getString("document_id").length()>0 && type.equalsIgnoreCase("get")){
		    	if(document.getString("x")!=null){
		    		selectedFields.put("x_accel_fft",1);
		    		selectedFields.put("x_accel_timebase",1);
		    		selectedFields.put("x_accel_fft_x_axis",1);
		    		selectedFields.put("x_accel_timebase_x_axis",1);
		    		selectedFields.put("x_accel_fft_peak_x_axis",1);
		    		selectedFields.put("x_vel_fft",1);
		    		selectedFields.put("x_vel_timebase",1);
		    		selectedFields.put("x_vel_fft_x_axis",1);
		    		selectedFields.put("x_vel_timebase_x_axis",1);
		    		selectedFields.put("x_vel_fft_peak_x_axis",1);
		    		
		    	}
		    	else if(document.getString("y")!=null){
		    		selectedFields.put("y_accel_fft",1);
		    		selectedFields.put("y_accel_timebase",1);
		    		selectedFields.put("y_accel_fft_x_axis",1);
		    		selectedFields.put("y_accel_timebase_x_axis",1);
		    		selectedFields.put("y_accel_fft_peak_x_axis",1);
		    		selectedFields.put("y_vel_fft",1);
		    		selectedFields.put("y_vel_timebase",1);
		    		selectedFields.put("y_vel_fft_x_axis",1);
		    		selectedFields.put("y_vel_timebase_x_axis",1);
		    		selectedFields.put("y_vel_fft_peak_x_axis",1);
		    		
		    	}
		    	else if(document.getString("z")!=null){
		    		selectedFields.put("z_accel_fft",1);
		    		selectedFields.put("z_accel_timebase",1);
		    		selectedFields.put("z_accel_fft_x_axis",1);
		    		selectedFields.put("z_accel_timebase_x_axis",1);
		    		selectedFields.put("z_accel_fft_peak_x_axis",1);
		    		selectedFields.put("z_vel_fft",1);
		    		selectedFields.put("z_vel_timebase",1);
		    		selectedFields.put("z_vel_fft_x_axis",1);
		    		selectedFields.put("z_vel_timebase_x_axis",1);
		    		selectedFields.put("z_vel_fft_peak_x_axis",1);

		    	}	    	
		    	else if(document.getString("thickness")!=null){
		    		selectedFields.put("ms_yaxis",1);
		    		selectedFields.put("ms_xaxis",1);
		    		selectedFields.put("peak_1_x",1);
		    		selectedFields.put("peak_1_y",1);
		    		selectedFields.put("peak_2_x",1);
		    		selectedFields.put("peak_2_y",1);
		    		selectedFields.put("flank_1_x",1);
		    		selectedFields.put("flank_1_y",1);
		    		selectedFields.put("flank_2_x",1);
		    		selectedFields.put("flank_2_y",1);
		    		selectedFields.put("zerocrossing_1_x",1);
		    		selectedFields.put("zerocrossing_1_y",1);
		    		selectedFields.put("zerocrossing_2_x",1);
		    		selectedFields.put("zerocrossing_2_y",1);
		    		selectedFields.put("thickness_cmnt.t_cmnts",1);
		    		selectedFields.put("gate_a_height",1);
		    		selectedFields.put("gate_a_start",1);
		    		selectedFields.put("gate_a_length",1);
		    		selectedFields.put("gate_b_height",1);
		    		selectedFields.put("gate_b_start",1);
		    		selectedFields.put("gate_b_length",1);
		    		selectedFields.put("part_material_velocity",1);
		    		selectedFields.put("thickness_algorithm",1);
		    	}
		    	
		    	
	    	}
			selectedFields.put("message_time",1);
			selectedFields.put("device_id",1);
			return selectedFields;
	    }	
	    
	    /**
	     * 
	     */
	    public static Map<String,String> loadDeviceCategoryName(){
	    	Map<String,String> hDeviceCategoryName=new HashMap<String,String>();
	   	 	hDeviceCategoryName.put("Vibration", "v_");
	   	 	hDeviceCategoryName.put("Temperature", "t_");
	   	 	hDeviceCategoryName.put("Corrosion", "c_");
	   	 	hDeviceCategoryName.put("Mcems", "c_");
	   	 	hDeviceCategoryName.put("Gas", "g_");
	   	 	return hDeviceCategoryName;
	   }
	    
	    
}
