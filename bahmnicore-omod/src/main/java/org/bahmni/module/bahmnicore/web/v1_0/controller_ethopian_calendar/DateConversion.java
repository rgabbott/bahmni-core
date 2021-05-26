package org.bahmni.module.bahmnicore.web.v1_0.controller_ethopian_calendar;

import java.util.Date;

public class DateConversion {

	private String date; 
	private String month;
	private String year;
	private Date gmtDate;
	private String eatDate;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public Date getGmtDate() {
		return gmtDate;
	}

	public void setGmtDate(Date gmtDate) {
		this.gmtDate = gmtDate;
	}

	public String getEatDate() {
		return eatDate;
	}

	public void setEatDate(String eatDate) {
		this.eatDate = eatDate;
	}

	@Override
	public String toString() {
		return "DateConversion [date=" + date + ", month=" + month + ", year=" + year + ", gmtDate=" + gmtDate
				+ ", eatDate=" + eatDate + "]";
	}

	

}
