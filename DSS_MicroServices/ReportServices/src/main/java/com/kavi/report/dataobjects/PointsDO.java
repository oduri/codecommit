package com.kavi.report.dataobjects;

import java.util.Comparator;

public class PointsDO {
	
	private double x;
	private double y;
	
	public PointsDO(double x,double y){
		this.x=x;
		this.y=y;
	}
	public PointsDO() {
		
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	
	public int compareTo(PointsDO o) {
		int result = Double.compare(o.getX(), this.getX());
        if ( result == 0 ) {
          // both X are equal -> compare Y too
          result = Double.compare(o.getY(), this.getY());
        } 
		return result;
	}
	
	
	public static Comparator<PointsDO> PointsDOComparator = new Comparator<PointsDO>() {
		@Override
		public int compare(PointsDO o1, PointsDO o2) {
			return o2.compareTo(o1);
		}
	};
	
}
