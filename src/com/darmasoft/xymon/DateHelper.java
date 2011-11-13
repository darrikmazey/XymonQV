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
	
	public static String formatted_elapsed_time(long since) {
		long second = System.currentTimeMillis();
		return(formatted_elapsed_time(since, second));
	}
	
	public static String formatted_elapsed_time(long first, long second) {
		String elapsed_formatted;
		
		long el = second - first;
		int days = 0;
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		
		while (el > (24 * 60 * 60 * 1000)) {
			days += 1;
			el -= (24 * 60 * 60 * 1000);
		}
		
		while (el > (60 * 60 * 1000)) {
			hours += 1;
			el -= (60 * 60 * 1000);
		}
		
		while (el > (60 * 1000)) {
			minutes += 1;
			el -= (60 * 1000);
		}

		seconds = (int) el / 1000;
		
		if (days > 1) {
			elapsed_formatted = String.format("%d days, %02d:%02d:%02d", days, hours, minutes, seconds);
		} else if (days == 1) {
			elapsed_formatted = String.format("%d day, %02d:%02d:%02d", days, hours, minutes, seconds);
		} else {
			elapsed_formatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		}
		
		return(elapsed_formatted);
	}
}
