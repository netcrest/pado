/*
 * Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netcrest.pado.index.provider.lucene;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.lucene.queryparser.flexible.standard.config.NumberDateFormat;

public class DateTool
{
	private static final TimeZone TIME_ZONE;
	static {
		String tzStr = System.getProperty("pado.timezone");
		TIME_ZONE = tzStr == null ? TimeZone.getDefault() : TimeZone.getTimeZone(tzStr);
	}
			
	public enum Resolution
	{
		/** Limit a date's resolution to year granularity. */
		YEAR(4),
		/** Limit a date's resolution to month granularity. */
		MONTH(6),
		/** Limit a date's resolution to day granularity. */
		DAY(8),
		/** Limit a date's resolution to hour granularity. */
		HOUR(10),
		/** Limit a date's resolution to minute granularity. */
		MINUTE(12),
		/** Limit a date's resolution to second granularity. */
		SECOND(14),
		/** Limit a date's resolution to millisecond granularity. */
		MILLISECOND(17);

		final int formatLen;
		public final SimpleDateFormat format;
		final NumberDateFormat numericFormat;

		Resolution(int formatLen)
		{
			this.formatLen = formatLen;
			// formatLen 10's place: 11111111
			// formatLen 1's place: 12345678901234567
			this.format = new SimpleDateFormat("yyyyMMddHHmmssSSS".substring(0, formatLen), Locale.ROOT);
			this.format.setTimeZone(TIME_ZONE);
			this.numericFormat = new NumberDateFormat(this.format);
		}

		/**
		 * this method returns the name of the resolution in lowercase (for
		 * backwards compatibility)
		 */
		@Override
		public String toString()
		{
			return super.toString().toLowerCase(Locale.ROOT);
		}
	}

	public static String timeToString(long time, Resolution res)
	{
		return res.format.format(new Date(time));
	}
	
	public static String dateToString(Date date, Resolution res)
	{
		return res.format.format(date);
	}
	
	public static long stringToTime(String dateStr, Resolution res) throws ParseException
	{
		return res.format.parse(dateStr).getTime();
	}
	
	public static Date stringToDate(String dateStr, Resolution res) throws ParseException
	{
		return res.format.parse(dateStr);
	}
	
	public static long stringToTime(String dateStr) throws ParseException
	{
		return stringToDate(dateStr).getTime();
	}
	
	public static Date stringToDate(String dateStr) throws ParseException
	{
		int len = dateStr.length();
		switch (len) {
		case 4:
			return stringToDate(dateStr, Resolution.YEAR);
		case 6:
			return stringToDate(dateStr, Resolution.MONTH);
		case 8:
			return stringToDate(dateStr, Resolution.DAY);
		case 10:
			return stringToDate(dateStr, Resolution.HOUR);
		case 12:
			return stringToDate(dateStr, Resolution.MINUTE);
		case 14:
			return stringToDate(dateStr, Resolution.SECOND);
		case 17:
		default:
			return stringToDate(dateStr, Resolution.MILLISECOND);
		}
	}
	
}
