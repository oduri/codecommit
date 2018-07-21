package com.kavi.report.utility;
import java.util.Comparator;

import com.kavi.report.dataobjects.PointsDO;


public class SortingUtility implements Comparator<PointsDO> {
	
	/**
	 * 
	 */
	@Override
	public int compare(PointsDO o1, PointsDO o2) {
		 if (o1.getX() < o2.getX()) return -1;
	     if (o1.getX() > o2.getX()) return 1;
	     return 0;
	}
    
}
