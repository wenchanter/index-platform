package com.wenchanter.solr.platform.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

	private static Calendar calendar = Calendar.getInstance();

	/**
	 * 
	 * @param date
	 * @param day
	 * @return
	 */
	public static Date addDay(Date date, int day) {
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_YEAR, day);
		return calendar.getTime();
	}

	/**
	 * 由于solr时间比东八区时间少8个小时，这里补齐了。
	 * @param dateStr
	 * @param format
	 * @return
	 * @throws ParseException
	 */
	public static Date genSolrTime(String dateStr, SimpleDateFormat format) throws ParseException {
		return addHour(toDate(dateStr, format), 8);
	}

	/**
	 * 
	 * @param date
	 * @param day
	 * @return
	 */
	public static Date addHour(Date date, int hour) {
		calendar.setTime(date);
		calendar.add(Calendar.HOUR, hour);
		return calendar.getTime();
	}

	/**
	 * 把dateStr解析成date
	 * 
	 * @throws ParseException
	 */
	public static Date toDate(String dateStr, SimpleDateFormat format) throws ParseException {
		return format.parse(dateStr);
	}

	/**
	 * 把dateStr解析成long
	 * 
	 * @throws ParseException
	 */
	public static long toLong(String dateStr, SimpleDateFormat format) throws ParseException {
		return format.parse(dateStr).getTime();
	}

	public static String getNowStr() {
		SimpleDateFormat timeFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return timeFORMAT.format(new Date());
	}

}
