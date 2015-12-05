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
package com.netcrest.pado.temporal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TemporalUtil
{
	public static final TimeZone TIME_ZONE;
	public static final SimpleDateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	public static final long MIN_TIME;
	public static final long MAX_TIME;
	public static final Date MIN_DATE;
	public static final Date MAX_DATE;
	
	static {
		String tzStr = System.getProperty("pado.timezone");
		TIME_ZONE = tzStr == null ? TimeZone.getDefault() : TimeZone.getTimeZone(tzStr);
	}
	
	private static final ThreadLocal<Calendar> TL_CAL = new ThreadLocal<Calendar>() {
		@Override
		protected Calendar initialValue()
		{
			return Calendar.getInstance(TIME_ZONE, Locale.ROOT);
		}
	};

	static {
		iso8601DateFormat.setTimeZone(TIME_ZONE);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		dateFormat.setTimeZone(TIME_ZONE);
		long min = 0;
		long max = Long.MAX_VALUE;
		try {
			min = round(dateFormat.parse("19700101").getTime(), Resolution.DAY);
			max = round(dateFormat.parse("20991231").getTime(), Resolution.DAY);
		} catch (ParseException e) {
			// ignore
		} finally {
			MIN_TIME = min;
			MAX_TIME = max;
			MIN_DATE = new Date(MIN_TIME);
			MAX_DATE = new Date(MAX_TIME);
		}
	}

	/**
	 * Limit a date's resolution. For example, the date
	 * <code>2004-09-21 13:50:11</code> will be changed to
	 * <code>2004-09-01 00:00:00</code> when using <code>Resolution.MONTH</code>
	 * .
	 * 
	 * @param resolution
	 *            The desired resolution of the date to be returned
	 * @return the date with all values more precise than
	 *         <code>resolution</code> set to 0 or 1
	 */
	public static Date round(Date date, Resolution resolution)
	{
		return new Date(round(date.getTime(), resolution));
	}

	@SuppressWarnings("fallthrough")
	public static long round(long time, Resolution resolution)
	{
		final Calendar calInstance = TL_CAL.get();
		calInstance.setTimeInMillis(time);

		switch (resolution) {
		// NOTE: switch statement fall-through is deliberate
		case YEAR:
			calInstance.set(Calendar.MONTH, 0);
		case MONTH:
			calInstance.set(Calendar.DAY_OF_MONTH, 1);
		case DAY:
			calInstance.set(Calendar.HOUR_OF_DAY, 0);
		case HOUR:
			calInstance.set(Calendar.MINUTE, 0);
		case MINUTE:
			calInstance.set(Calendar.SECOND, 0);
		case SECOND:
			calInstance.set(Calendar.MILLISECOND, 0);
		case MILLISECOND:
			// don't cut off anything
			break;
		default:
			throw new IllegalArgumentException("unknown resolution " + resolution);
		}
		return calInstance.getTimeInMillis();
	}

	public static enum Resolution
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
		final SimpleDateFormat format;// should be cloned before use, since it's
										// not thread-safe

		Resolution(int formatLen)
		{
			this.formatLen = formatLen;
			// formatLen 10's place: 11111111
			// formatLen 1's place: 12345678901234567
			this.format = new SimpleDateFormat("yyyyMMddHHmmssSSS".substring(0, formatLen), Locale.ROOT);
			this.format.setTimeZone(TIME_ZONE);
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
}
