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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.netcrest.pado.data.jsonlite.JsonLite;

@SuppressWarnings({ "rawtypes" })
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

	/**
	 * Returns a composite identity key for the specified arguments. An identity
	 * composite key is comprised of one or more string values separated by '.'.
	 * 
	 * @param args
	 *            Key arguments
	 */
	public static String getIdentityKey(String... args)
	{
		if (args == null) {
			return null;
		}
		String retVal = "";
		int i = 0;
		for (String arg : args) {
			if (i > 0) {
				retVal += ".";
			}
			retVal += arg;
			i++;
		}
		return retVal;
	}

	/**
	 * Finds the first JsonLite object that has the specified key in the
	 * specified list.
	 * 
	 * @param list
	 *            JsonLite object list
	 * @param key
	 *            Key to search
	 * @param value
	 *            Value to search
	 * @return null if not found;
	 */
	public static JsonLite findValue(List<JsonLite> list, Object key, JsonLite value)
	{
		JsonLite foundJl = null;
		for (JsonLite jl : list) {
			Object obj = jl.get(key);
			if (obj != null && obj.equals(value)) {
				foundJl = jl;
				break;
			}
		}
		return foundJl;
	}

	/**
	 * Returns a list of all found JsonLite objects that have the specified key
	 * in the specified list.
	 * 
	 * @param list
	 *            JsonLite object list
	 * @param key
	 *            Field name to search
	 * @param value
	 *            Field value to search
	 * @return null if not found;
	 */
	public static List<JsonLite> findValueList(List<JsonLite> list, String key, JsonLite value)
	{
		List<JsonLite> foundList = null;
		for (JsonLite jl : list) {
			Object obj = jl.get(key);
			if (obj != null && obj.equals(value)) {
				if (foundList == null) {
					foundList = new ArrayList<JsonLite>(5);
				}
				foundList.add(jl);
			}
		}
		return foundList;
	}

	/**
	 * Returns a list of all found JsonLite objects that have the specified keys
	 * in the specified list.
	 * 
	 * @param list
	 *            JsonLite object list
	 * @param key1
	 *            First key to search
	 * @param value1
	 *            First value to search
	 * @param key2
	 *            Second key to search
	 * @param value2
	 *            Second value to search
	 * @return null if not found;
	 */
	public static List<JsonLite> findValueList(List<JsonLite> list, Object key1, JsonLite value1, Object key2,
			JsonLite value2)
	{
		List<JsonLite> foundList = null;
		for (JsonLite jl : list) {
			Object obj1 = jl.get(key1);
			if (obj1 != null && obj1.equals(value1)) {
				Object obj2 = jl.get(key2);
				if (obj2 != null && obj2.equals(value2)) {
					if (foundList == null) {
						foundList = new ArrayList<JsonLite>(5);
					}
					foundList.add(jl);
				}
			}
		}
		return foundList;
	}

	/**
	 * Returns a list of all found JsonLite objects that have the specified key
	 * in the specified list.
	 * 
	 * @param list
	 *            Temporal entry list
	 * @param key
	 *            Key to search
	 * @param value
	 *            Value to search
	 * @return null if not found;
	 */
	public static List<JsonLite> findValueTemporalList(List<TemporalEntry<ITemporalKey, ITemporalData>> list,
			Object key, JsonLite value)
	{
		List<JsonLite> foundList = null;
		for (TemporalEntry<ITemporalKey, ITemporalData> te : list) {
			JsonLite jl = (JsonLite) te.getTemporalData().getValue();
			Object obj = jl.get(key);
			if (obj != null && obj.equals(value)) {
				if (foundList == null) {
					foundList = new ArrayList<JsonLite>(5);
				}
				foundList.add(jl);
			}
		}
		return foundList;
	}

	/**
	 * Returns a list of all found JsonLite objects that have the specified keys
	 * in the specified list.
	 * 
	 * @param list
	 *            Temporal entry list
	 * @param key1
	 *            First key to search
	 * @param value1
	 *            First value to search
	 * @param key2
	 *            Second key to search
	 * @param value2
	 *            Second value to search
	 * @return null if not found;
	 */
	public static List<JsonLite> findValueTemporalList(List<TemporalEntry<ITemporalKey, ITemporalData>> list,
			Object key1, Object value1, Object key2, JsonLite value2)
	{
		List<JsonLite> foundList = null;
		for (TemporalEntry<ITemporalKey, ITemporalData> te : list) {
			JsonLite jl = (JsonLite) te.getTemporalData().getValue();
			Object obj1 = jl.get(key1);
			if (obj1 != null && obj1.equals(value1)) {
				Object obj2 = jl.get(key2);
				if (obj2 != null && obj2.equals(value2)) {
					if (foundList == null) {
						foundList = new ArrayList<JsonLite>(5);
					}
					foundList.add(jl);
				}
			}
		}
		return foundList;
	}

	/**
	 * Returns a list of all found JsonLite objects that have the specified key
	 * in the specified list. The specified map contains all entries starting
	 * from the beginning of time. This method finds entries with writtenTime >=
	 * fromWrttenTime.
	 * 
	 * @param map
	 *            Temporal entry map
	 * @param key
	 *            Key to search
	 * @param value
	 *            Value to search
	 * @param fromWrittenTime
	 *            Start written time
	 * 
	 * @return null if not found
	 */
	public static List<JsonLite> findValueTemporalMap(Map<ITemporalKey, ITemporalData> map, Object key, JsonLite value,
			long fromWrittenTime)
	{
		List<JsonLite> foundList = null;
		Set<Map.Entry<ITemporalKey, ITemporalData>> set = map.entrySet();
		for (Map.Entry<ITemporalKey, ITemporalData> entry : set) {
			ITemporalKey tk = entry.getKey();
			if (fromWrittenTime > tk.getWrittenTime()) {
				continue;
			}
			JsonLite jl = (JsonLite) entry.getValue().getValue();
			Object obj = jl.get(key);
			if (obj != null && obj.equals(value)) {
				if (foundList == null) {
					foundList = new ArrayList<JsonLite>(5);
				}
				foundList.add(jl);
			}
		}
		return foundList;
	}

	/**
	 * Returns a list of JsonLite objects that have writtenTime >=
	 * fromWrittenTime.
	 * 
	 * @param map
	 *            Temporal entry map
	 * @param fromWrittenTime
	 *            Start written time
	 */
	public static List<JsonLite> findValueTemporalMap(Map<ITemporalKey, ITemporalData> map, long fromWrittenTime)
	{
		List<JsonLite> foundList = new ArrayList<JsonLite>();
		Set<Map.Entry<ITemporalKey, ITemporalData>> set = map.entrySet();
		for (Map.Entry<ITemporalKey, ITemporalData> entry : set) {
			ITemporalKey tk = entry.getKey();
			if (fromWrittenTime > tk.getWrittenTime()) {
				continue;
			}
			JsonLite jl = (JsonLite) entry.getValue().getValue();
			foundList.add(jl);
		}
		return foundList;
	}

	/**
	 * Returns a list of all found JsonLite objects that have the specified keys
	 * in the specified list.
	 * 
	 * @param map
	 *            Temporal entry map
	 * @param key1
	 *            First key to search
	 * @param fieldValue1
	 *            First value to search
	 * @param key2
	 *            Second key to search
	 * @param fieldValue2
	 *            Second value to search
	 * @return null if not found;
	 */
	public static List<JsonLite> findValueTemporalMap(Map<ITemporalKey, ITemporalData> map, Object key1,
			JsonLite fieldValue1, Object key2, JsonLite fieldValue2)
	{
		List<JsonLite> foundList = null;
		Set<Map.Entry<ITemporalKey, ITemporalData>> set = map.entrySet();
		for (Map.Entry<ITemporalKey, ITemporalData> entry : set) {
			JsonLite jl = (JsonLite) entry.getValue().getValue();
			Object obj1 = jl.get(key1);
			if (obj1 != null && obj1.equals(fieldValue1)) {
				Object obj2 = jl.get(key2);
				if (obj2 != null && obj2.equals(fieldValue2)) {
					if (foundList == null) {
						foundList = new ArrayList<JsonLite>(5);
					}
					foundList.add(jl);
				}
			}
		}
		return foundList;
	}

}
