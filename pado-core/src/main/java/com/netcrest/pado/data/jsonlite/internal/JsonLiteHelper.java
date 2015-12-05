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
package com.netcrest.pado.data.jsonlite.internal;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.data.jsonlite.JsonLiteException;

/**
 * JsonLiteHelper provides helper methods for JsonLite implementation.
 * @author dpark
 *
 */
public class JsonLiteHelper
{
	public static final SimpleDateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	static {
		iso8601DateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public static Date parseDate(String dateString) throws ParseException
	{
		return iso8601DateFormat.parse(dateString);
	}
	
	/**
	 * Produce a string from a Number.
	 * 
	 * @param number
	 *            A Number
	 * @return A String.
	 * @throws JsonLiteException
	 *             If n is a non-finite number.
	 */
	public static String numberToString(Number number) throws JsonLiteException
	{
		if (number == null) {
			throw new JsonLiteException("Null pointer");
		}
		testValidity(number);

		// Shave off trailing zeros and decimal point, if possible.

		String string = number.toString();
		if (string.indexOf('.') > 0 && string.indexOf('e') < 0 && string.indexOf('E') < 0) {
			while (string.endsWith("0")) {
				string = string.substring(0, string.length() - 1);
			}
			if (string.endsWith(".")) {
				string = string.substring(0, string.length() - 1);
			}
		}
		return string;
	}
	
	/**
	 * Throw an exception if the object is a NaN or infinite number.
	 * 
	 * @param o
	 *            The object to test.
	 * @throws JsonLiteException
	 *             If o is a non-finite number.
	 */
	public static void testValidity(Object o) throws JsonLiteException
	{
		if (o != null) {
			if (o instanceof Double) {
				if (((Double) o).isInfinite() || ((Double) o).isNaN()) {
					throw new JsonLiteException("JSON does not allow non-finite numbers.");
				}
			} else if (o instanceof Float) {
				if (((Float) o).isInfinite() || ((Float) o).isNaN()) {
					throw new JsonLiteException("JSON does not allow non-finite numbers.");
				}
			}
		}
	}
	
	/**
	 * Produce a string in double quotes with backslash sequences in all the
	 * right places. A backslash will be inserted within &lt;/, producing
	 * &lt;\/, allowing JSON text to be delivered in HTML. In JSON text, a
	 * string cannot contain a control character or an unescaped quote or
	 * backslash.
	 * 
	 * @param string
	 *            A String
	 * @return A String correctly formatted for insertion in a JSON text.
	 */
	public static String quote(String string)
	{
		StringWriter sw = new StringWriter();
		synchronized (sw.getBuffer()) {
			try {
				return quote(string, sw).toString();
			} catch (IOException ignored) {
				// will never happen - we are writing to a string writer
				return "";
			}
		}
	}

	public static Writer quote(String string, Writer w) throws IOException
	{
		if (string == null || string.length() == 0) {
			w.write("\"\"");
			return w;
		}

		char b;
		char c = 0;
		String hhhh;
		int i;
		int len = string.length();

		w.write('"');
		for (i = 0; i < len; i += 1) {
			b = c;
			c = string.charAt(i);
			switch (c) {
			case '\\':
			case '"':
				w.write('\\');
				w.write(c);
				break;
			case '/':
				if (b == '<') {
					w.write('\\');
				}
				w.write(c);
				break;
			case '\b':
				w.write("\\b");
				break;
			case '\t':
				w.write("\\t");
				break;
			case '\n':
				w.write("\\n");
				break;
			case '\f':
				w.write("\\f");
				break;
			case '\r':
				w.write("\\r");
				break;
			default:
				if (c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
					w.write("\\u");
					hhhh = Integer.toHexString(c);
					w.write("0000", 0, 4 - hhhh.length());
					w.write(hhhh);
				} else {
					w.write(c);
				}
			}
		}
		w.write('"');
		return w;
	}
	
	public static void put(JsonLite jl, KeyType key, IJsonLiteWrapper value)
	{
		jl.put(key, value.toJsonLite());
	}
	
	public static void put(JsonLite jl, KeyType key, List list)
	{	
		try {
			List jlList = list.getClass().newInstance();
			for (Object object : list) {
				if (object instanceof IJsonLiteWrapper) {
					jlList.add(((IJsonLiteWrapper) object).toJsonLite());
				} else {
					jlList.add(object);
				}
			}
			jl.put(key,  jlList);
		} catch (Exception e) {
			throw new JsonLiteException(e);
		} 
	}
}
