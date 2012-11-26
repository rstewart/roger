package com.shopwiki.messaging.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @owner rstewart
 */
public class TimeUtil {

	private static final TimeZone NY_TIMEZONE = TimeZone.getTimeZone("America/New_York");

	private static final ThreadLocal<DateFormat> _dateFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			format.setTimeZone(NY_TIMEZONE);
			return format;
		};
	};

	public static String format(Date date) {
		if (date == null)
			return null;
		return _dateFormat.get().format(date);
	}

	public static String now() {
		return format(new Date());
	}
}
