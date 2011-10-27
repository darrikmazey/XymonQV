package com.darmasoft.xymon;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {

	private static final String SQL_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static final String DISPLAY_FORMAT = "dd/MM/YYYY HH:mm:ss";
	
	public static String dateToSqlString(Date d) {
		SimpleDateFormat formatter;
		formatter = new SimpleDateFormat(SQL_FORMAT);
		return(formatter.format(d));
	}
	
	public static Date sqlStringToDate(String s) throws ParseException {
		SimpleDateFormat formatter;
		formatter = new SimpleDateFormat(SQL_FORMAT);
		return(formatter.parse(s));
	}
	
	public static String dateToDisplayString(Date d) {
		SimpleDateFormat formatter;
		formatter = new SimpleDateFormat(DISPLAY_FORMAT);
		return(formatter.format(d));
	}
	
	public static String sqlStringToDisplayString(String s) throws ParseException {
		return(dateToDisplayString(sqlStringToDate(s)));
	}
}
