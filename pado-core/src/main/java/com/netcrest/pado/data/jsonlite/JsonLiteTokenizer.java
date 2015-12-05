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
package com.netcrest.pado.data.jsonlite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.data.jsonlite.JsonLiteSchemaManager.KeyInfo;
import com.netcrest.pado.data.jsonlite.internal.JsonLiteHelper;
import com.netcrest.pado.data.jsonlite.internal.JsonLiteSerializer;

/*
 Copyright (c) 2002 JSON.org

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 The Software shall be used for Good, not Evil.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

/**
 * A JSONTokenizer takes a source string and extracts characters and tokens from
 * it. It is used by the JSONObject and JSONArray constructors to parse JSON
 * source strings.
 * <p>
 * This is an extended version of JSONTokenizer provided by JSON.org. It
 * includes support for additional data types specific to JsonLite. It is for
 * internal use only.
 * 
 * @author JSON.org
 * @version 2012-02-16
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class JsonLiteTokenizer
{
	public transient Object nonJsonLiteValue;

	private long character;
	private boolean eof;
	private long index;
	private long line;
	private char previous;
	private Reader reader;
	private boolean usePrevious;

	private KeyType keyType;
	private Map<String, JsonLiteSchemaManager.KeyInfo> typeMap;
	private Class beanClass;
	private String genericTypeString;
	private boolean isArray;

	/**
	 * Construct a JSONTokener from a Reader.
	 * 
	 * @param reader
	 *            A reader.
	 * @param schemaTypeMap
	 *            Map of (string key, KeyType) pairs for deserializing the
	 *            specified JSON string.
	 */
	public JsonLiteTokenizer(Reader reader, Map<String, JsonLiteSchemaManager.KeyInfo> schemaTypeMap)
	{
		this.reader = reader.markSupported() ? reader : new BufferedReader(reader);
		this.eof = false;
		this.usePrevious = false;
		this.previous = 0;
		this.index = 0;
		this.character = 1;
		this.line = 1;
		this.typeMap = schemaTypeMap;
		if (typeMap != null) {
			KeyInfo keyInfo = typeMap.get("__root");
			if (keyInfo != null) {
				keyType = keyInfo.keyType;
			}
		}
	}

	/**
	 * Construct a JSONTokener from an InputStream.
	 * 
	 * @param schemaTypeMap
	 *            Map of (string key, KeyType) pairs for deserializing the
	 *            specified JSON string.
	 */
	public JsonLiteTokenizer(InputStream inputStream, Map<String, JsonLiteSchemaManager.KeyInfo> schemaTypeMap)
			throws JsonLiteException
	{
		this(new InputStreamReader(inputStream), schemaTypeMap);
	}

	/**
	 * Construct a JSONTokenizer from a string.
	 * 
	 * @param s
	 *            A source string.
	 * @param schemaTypeMap
	 *            Map of (string key, KeyType) pairs for deserializing the
	 *            specified JSON string.
	 */
	public JsonLiteTokenizer(String s, Map<String, JsonLiteSchemaManager.KeyInfo> schemaTypeMap)
	{
		this(new StringReader(s), schemaTypeMap);
		resetClassType("__root");
	}

	public JsonLiteTokenizer(String s, KeyType keyType)
	{
		this(new StringReader(s), null);
		this.keyType = keyType;
	}

	public JsonLiteTokenizer(String s, Class<?> beanClass)
	{
		this(new StringReader(s), null);
		this.beanClass = beanClass;
	}

	private void resetClassType(String key)
	{
		if (typeMap != null) {
			KeyInfo keyInfo = typeMap.get(key);
			if (keyInfo == null) {
				keyType = null;
			} else {
				keyType = keyInfo.keyType;
			}
			if (keyType == null) {
				beanClass = keyInfo.returnType;
			}
		}
	}

	/**
	 * Back up one character. This provides a sort of lookahead capability, so
	 * that you can test for a digit or letter before attempting to parse the
	 * next number or identifier.
	 */
	public void back() throws JsonLiteException
	{
		if (this.usePrevious || this.index <= 0) {
			throw new JsonLiteException("Stepping back two steps is not supported");
		}
		this.index -= 1;
		this.character -= 1;
		this.usePrevious = true;
		this.eof = false;
	}

	/**
	 * Get the hex value of a character (base16).
	 * 
	 * @param c
	 *            A character between '0' and '9' or between 'A' and 'F' or
	 *            between 'a' and 'f'.
	 * @return An int between 0 and 15, or -1 if c was not a hex digit.
	 */
	public static int dehexchar(char c)
	{
		if (c >= '0' && c <= '9') {
			return c - '0';
		}
		if (c >= 'A' && c <= 'F') {
			return c - ('A' - 10);
		}
		if (c >= 'a' && c <= 'f') {
			return c - ('a' - 10);
		}
		return -1;
	}

	public boolean end()
	{
		return this.eof && !this.usePrevious;
	}

	/**
	 * Determine if the source string still contains characters that next() can
	 * consume.
	 * 
	 * @return true if not yet at the end of the source.
	 */
	public boolean more() throws JsonLiteException
	{
		this.next();
		if (this.end()) {
			return false;
		}
		this.back();
		return true;
	}

	/**
	 * Get the next character in the source string.
	 * 
	 * @return The next character, or 0 if past the end of the source string.
	 */
	public char next() throws JsonLiteException
	{
		int c;
		if (this.usePrevious) {
			this.usePrevious = false;
			c = this.previous;
		} else {
			try {
				c = this.reader.read();
			} catch (IOException exception) {
				throw new JsonLiteException(exception);
			}

			if (c <= 0) { // End of stream
				this.eof = true;
				c = 0;
			}
		}
		this.index += 1;
		if (this.previous == '\r') {
			this.line += 1;
			this.character = c == '\n' ? 0 : 1;
		} else if (c == '\n') {
			this.line += 1;
			this.character = 0;
		} else {
			this.character += 1;
		}
		this.previous = (char) c;
		return this.previous;
	}

	/**
	 * Consume the next character, and check that it matches a specified
	 * character.
	 * 
	 * @param c
	 *            The character to match.
	 * @return The character.
	 * @throws JsonLiteException
	 *             if the character does not match.
	 */
	public char next(char c) throws JsonLiteException
	{
		char n = this.next();
		if (n != c) {
			throw this.syntaxError("Expected '" + c + "' and instead saw '" + n + "'");
		}
		return n;
	}

	/**
	 * Get the next n characters.
	 * 
	 * @param n
	 *            The number of characters to take.
	 * @return A string of n characters.
	 * @throws JsonLiteException
	 *             Substring bounds error if there are not n characters
	 *             remaining in the source string.
	 */
	public String next(int n) throws JsonLiteException
	{
		if (n == 0) {
			return "";
		}

		char[] chars = new char[n];
		int pos = 0;

		while (pos < n) {
			chars[pos] = this.next();
			if (this.end()) {
				throw this.syntaxError("Substring bounds error");
			}
			pos += 1;
		}
		return new String(chars);
	}

	/**
	 * Get the next char in the string, skipping whitespace.
	 * 
	 * @throws JsonLiteException
	 * @return A character, or 0 if there are no more characters.
	 */
	public char nextClean() throws JsonLiteException
	{
		for (;;) {
			char c = this.next();
			if (c == 0 || c > ' ') {
				return c;
			}
		}
	}

	/**
	 * Return the characters up to the next close quote character. Backslash
	 * processing is done. The formal JSON format does not allow strings in
	 * single quotes, but an implementation is allowed to accept them.
	 * 
	 * @param quote
	 *            The quoting character, either <code>"</code>
	 *            &nbsp;<small>(double quote)</small> or <code>'</code>
	 *            &nbsp;<small>(single quote)</small>.
	 * @return A String.
	 * @throws JsonLiteException
	 *             Unterminated string.
	 */
	public String nextString(char quote) throws JsonLiteException
	{
		char c;
		StringBuffer sb = new StringBuffer();
		for (;;) {
			c = this.next();
			switch (c) {
			case 0:
			case '\n':
			case '\r':
				throw this.syntaxError("Unterminated string");
			case '\\':
				c = this.next();
				switch (c) {
				case 'b':
					sb.append('\b');
					break;
				case 't':
					sb.append('\t');
					break;
				case 'n':
					sb.append('\n');
					break;
				case 'f':
					sb.append('\f');
					break;
				case 'r':
					sb.append('\r');
					break;
				case 'u':
					sb.append((char) Integer.parseInt(this.next(4), 16));
					break;
				case '"':
				case '\'':
				case '\\':
				case '/':
					sb.append(c);
					break;
				default:
					throw this.syntaxError("Illegal escape.");
				}
				break;
			default:
				if (c == quote) {
					return sb.toString();
				}
				sb.append(c);
			}
		}
	}

	/**
	 * Get the text up but not including the specified character or the end of
	 * line, whichever comes first.
	 * 
	 * @param delimiter
	 *            A delimiter character.
	 * @return A string.
	 */
	public String nextTo(char delimiter) throws JsonLiteException
	{
		StringBuffer sb = new StringBuffer();
		for (;;) {
			char c = this.next();
			if (c == delimiter || c == 0 || c == '\n' || c == '\r') {
				if (c != 0) {
					this.back();
				}
				return sb.toString().trim();
			}
			sb.append(c);
		}
	}

	/**
	 * Get the text up but not including one of the specified delimiter
	 * characters or the end of line, whichever comes first.
	 * 
	 * @param delimiters
	 *            A set of delimiter characters.
	 * @return A string, trimmed.
	 */
	public String nextTo(String delimiters) throws JsonLiteException
	{
		char c;
		StringBuffer sb = new StringBuffer();
		for (;;) {
			c = this.next();
			if (delimiters.indexOf(c) >= 0 || c == 0 || c == '\n' || c == '\r') {
				if (c != 0) {
					this.back();
				}
				return sb.toString().trim();
			}
			sb.append(c);
		}
	}

	/**
	 * Get the next value. The value can be a Boolean, Double, Integer,
	 * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
	 * 
	 * @param key
	 *            Key
	 * @param ts
	 *            Type string, i.e., Z, T, [L, Lfoo.Foo, etc.
	 * @throws JsonLiteException
	 *             If syntax error.
	 * 
	 * @return An object.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object nextValue(String key, String ts) throws JsonLiteException
	{
		char c = this.nextClean();
		String string = null;

		switch (c) {
		case '"':
		case '\'':
			string = this.nextString(c);
			if (ts == null || ts.equals("T")) {
				return string;
			}
			break;
		case '{':
			this.back();
			if (key != null) {
				if (key.equals("__h")) {
					return getHeaderValue();
				} else {
					KeyInfo keyInfo = null;
					if (typeMap != null) {
						keyInfo = typeMap.get(key);
					}
					if (keyInfo != null && keyInfo.returnType != JsonLite.class) {
						if (keyInfo.returnType != null) {
							if (keyInfo.returnType.isArray()) {
								ArrayList arrayList = (ArrayList) getCollection(new ArrayList(), keyInfo);
								return arrayList.toArray(new Object[arrayList.size()]);
							} else if (Collection.class.isAssignableFrom(keyInfo.returnType)) {
								try {
									Collection collection = getCollection(
											(Collection) keyInfo.returnType.newInstance(), keyInfo);
									return collection;
								} catch (Exception ex) {
									throw new JsonLiteException(ex);
								}
							} else if (Map.class.isAssignableFrom(keyInfo.returnType)) {
								try {
									Map map = getMap((Map) keyInfo.returnType.newInstance(), keyInfo);
									return map;
								} catch (Exception ex) {
									throw new JsonLiteException(ex);
								}
								
							} else if (keyInfo.keyType != null) {
								Map<String, KeyInfo> schemaTypeMap = JsonLiteSchemaManager
										.getSchemaMap(keyInfo.keyType);
								JsonLiteTokenizer jlk = new JsonLiteTokenizer(reader, schemaTypeMap);
								jlk.usePrevious = true;
								jlk.previous = c;
								JsonLite jl = new JsonLite(jlk, null);
								// advance to compensate for back() done at the
								// beginning
								nextClean();
								if (IJsonLiteWrapper.class.isAssignableFrom(keyInfo.returnType)) {
									try {
										IJsonLiteWrapper wrapper = (IJsonLiteWrapper)keyInfo.returnType.newInstance();
										wrapper.fromJsonLite(jl);
										return wrapper;
									} catch (Exception e) {
										throw new JsonLiteException(e);
									}
								} else {
									return jl;
								}
							} else {
								// bean
								Object bean = getBean(keyInfo.returnType);
								return bean;
							}
						}
					} else {
						resetClassType(key);
						nonJsonLiteValue = null;
						JsonLite jl = new JsonLite(this, keyType);
						if (nonJsonLiteValue != null) {
							return nonJsonLiteValue;
						} else {
							return jl;
						}
					}
				}
			} else {
				nonJsonLiteValue = null;
				JsonLite jl = new JsonLite(this, null);
				if (nonJsonLiteValue != null) {
					return nonJsonLiteValue;
				} else {
					return jl;
				}
			}
		case '[':
			this.back();
			KeyInfo keyInfo = null;
			if (typeMap != null) {
				keyInfo = typeMap.get(key);
			}
			if (keyInfo != null) {
				if (keyInfo.returnType != null) {
					if (keyInfo.returnType.isArray()) {
						ArrayList arrayList = (ArrayList) getCollection(new ArrayList(), keyInfo);
						return arrayList.toArray(new Object[arrayList.size()]);
					} else if (Collection.class.isAssignableFrom(keyInfo.returnType)) {
						try {
							Collection collection = getCollection((Collection) keyInfo.returnType.newInstance(),
									keyInfo);
							return collection;
						} catch (Exception ex) {
							throw new JsonLiteException(ex);
						}
					} else if (keyInfo.keyType != null) {
						Map<String, KeyInfo> schemaTypeMap = JsonLiteSchemaManager.getSchemaMap(keyInfo.keyType);
						JsonLiteTokenizer jlk = new JsonLiteTokenizer(reader, schemaTypeMap);
						jlk.usePrevious = true;
						jlk.previous = c;
						JsonLite jl = new JsonLite(jlk, null);
						// advance to compensate for back() done at the
						// beginning
						nextClean();
						return jl;
					} else {

					}
				}
			} else {
				ArrayList arrayList = (ArrayList) getCollection(new ArrayList(), null);
				return arrayList.toArray(new Object[arrayList.size()]);
			}
		}

		/*
		 * Handle unquoted text. This could be the values true, false, or null,
		 * or it can be a number. An implementation (such as this one) is
		 * allowed to also accept non-standard forms.
		 * 
		 * Accumulate characters until we reach the end of the text or a
		 * formatting character.
		 */
		if (string == null) {
			StringBuffer sb = new StringBuffer();
			while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
				sb.append(c);
				c = this.next();
			}
			this.back();

			string = sb.toString().trim();
			if ("".equals(string)) {
				throw this.syntaxError("Missing value");
			}
		}
		try {
			return stringToValue(string, ts);
		} catch (ParseException ex) {
			throw new JsonLiteException(ex);
		}
	}

	/**
	 * Try to convert a string into a number, boolean, or null. If the string
	 * can't be converted, return the string.
	 * 
	 * @param string
	 *            A String.
	 * @return A simple JSON value.
	 * @throws ParseException
	 */
	public static Object stringToValue(String string, String ts) throws ParseException
	{
		if (ts == null) {
			Double d;
			if (string.equals("")) {
				return string;
			}
			if (string.equalsIgnoreCase("true")) {
				return Boolean.TRUE;
			}
			if (string.equalsIgnoreCase("false")) {
				return Boolean.FALSE;
			}
			if (string.equalsIgnoreCase("null")) {
				return null;
			}

			/*
			 * If it might be a number, try converting it. If a number cannot be
			 * produced, then the value will just be a string.
			 */

			char b = string.charAt(0);
			if ((b >= '0' && b <= '9') || b == '-') {
				try {
					if (string.indexOf('.') > -1 || string.indexOf('e') > -1 || string.indexOf('E') > -1) {
						d = Double.valueOf(string);
						if (!d.isInfinite() && !d.isNaN()) {
							return d;
						}
					} else {
						Long myLong = new Long(string);
						if (string.equals(myLong.toString())) {
							if (myLong.longValue() == myLong.intValue()) {
								return new Integer(myLong.intValue());
							} else {
								return myLong;
							}
						}
					}
				} catch (Exception ignore) {
				}
			}
			return string;
		} else {

			if (string.equals("null")) {
				return null;
			}
			if (ts.equalsIgnoreCase("z")) {
				if (string.equalsIgnoreCase("true")) {
					return Boolean.TRUE;
				} else {
					return Boolean.FALSE;
				}
			} else if (ts.equalsIgnoreCase("b")) {
				return Byte.valueOf(string);
			} else if (ts.equalsIgnoreCase("c")) {
				return string.charAt(0);
			} else if (ts.equalsIgnoreCase("d")) {
				return Double.valueOf(string);
			} else if (ts.equalsIgnoreCase("f")) {
				return Float.valueOf(string);
			} else if (ts.equalsIgnoreCase("i")) {
				return Integer.valueOf(string);
			} else if (ts.equalsIgnoreCase("j")) {
				return Long.valueOf(string);
			} else if (ts.equalsIgnoreCase("s")) {
				return Short.valueOf(string);
			} else if (ts.equalsIgnoreCase("t")) {
				return string;
			} else if (ts.equalsIgnoreCase("Date")) {
				return JsonLiteHelper.iso8601DateFormat.parse(string);
			} else if (ts.equalsIgnoreCase("BD")) {
				return new BigDecimal(string);
			} else if (ts.equalsIgnoreCase("BI")) {
				return new BigInteger(string);
			} else {
				return string;
			}
		}
	}

	private Class getClass(String jlClassName) throws ClassNotFoundException
	{
		int index = jlClassName.indexOf('<');
		if (index == -1) {
			return Class.forName(jlClassName);
		} else {
			int index2 = jlClassName.indexOf('>');
			if (index2 == -1) {
				throw new JsonLiteException("Generic type incomplete. '>' missing");
			}
			String className = jlClassName.substring(0, index);
			genericTypeString = jlClassName.substring(index + 1, index2);
			Class clazz = Class.forName(className);
			return clazz;
		}
	}
	
	static String getTypeName(Class type)
	{
		if (type == boolean.class) {
			return "z";
		} else if (type == Boolean.class) {
			return "Z";
		} else if (type == byte.class) {
			return "b";
		} else if (type == Byte.class) {
			return "B";
		} else if (type == char.class) {
			return "c";
		} else if (type == Character.class) {
			return "C";
		} else if (type == double.class) {
			return "d";
		} else if (type == Double.class) {
			return "D";
		} else if (type == float.class) {
			return "f";
		} else if (type == Float.class) {
			return "F";
		} else if (type == int.class) {
			return "i";
		} else if (type == Integer.class) {
			return "I";
		} else if (type == long.class) {
			return "j";
		} else if (type == Long.class) {
			return "J";
		} else if (type == short.class) {
			return "s";
		} else if (type == Short.class) {
			return "S";
		} else if (type == String.class) {
			return "T";
		} else if (type == Date.class) {
			return "Date";
		} else if (type == BigDecimal.class) {
			return "BD";
		} else if (type == BigInteger.class) {
			return "BI";
		} else if (type == ArrayList.class) {
			return "List";
		} else if (type == HashMap.class) {
			return "Map";
		} else if (type == Object.class) {
			return "L";
		}
		return type.getCanonicalName();
	}

	private Class getType(String ts) throws ClassNotFoundException
	{
		genericTypeString = null;
		isArray = false;
		if (ts.startsWith("L")) {
			if (ts.startsWith("List")) {
				return ArrayList.class;
			}
			String className = ts.substring(1);
			return getClass(className);
		} else if (ts.startsWith("[")) {
			String t = ts.substring(1);
			Class clazz;
			if (t.equals("L")) {
				clazz = Object.class;
			} else {
				clazz = getType(t);
				if (clazz == null) {
					clazz = Class.forName(t);
				}
			}
			genericTypeString = t;
			isArray = true;
			return clazz;
		} else if (ts.equals("z")) {
			return boolean.class;
		} else if (ts.equals("Z")) {
			return Boolean.class;
		} else if (ts.equals("b")) {
			return byte.class;
		} else if (ts.equals("B")) {
			return Byte.class;
		} else if (ts.equals("c")) {
			return char.class;
		} else if (ts.equals("C")) {
			return Character.class;
		} else if (ts.equals("d")) {
			return double.class;
		} else if (ts.equals("D")) {
			return Double.class;
		} else if (ts.equals("f")) {
			return float.class;
		} else if (ts.equals("F")) {
			return Float.class;
		} else if (ts.equals("i")) {
			return int.class;
		} else if (ts.equals("I")) {
			return Integer.class;
		} else if (ts.equals("j")) {
			return long.class;
		} else if (ts.equals("J")) {
			return Long.class;
		} else if (ts.equals("s")) {
			return short.class;
		} else if (ts.equals("S")) {
			return Short.class;
		} else if (ts.equalsIgnoreCase("t")) {
			return String.class;
		} else if (ts.equalsIgnoreCase("Date")) {
			return Date.class;
		} else if (ts.equalsIgnoreCase("BD")) {
			return BigDecimal.class;
		} else if (ts.equalsIgnoreCase("BI")) {
			return BigInteger.class;
		} else if (ts.equalsIgnoreCase("Map")) {
			return HashMap.class;
		} else {
			return null;
		}
	}

	/**
	 * Returns the next raw value as String. This method should be invoked to
	 * get the next metadata value in the array string.
	 * 
	 * @throws JsonLiteException
	 */
	private Object nextStringValue() throws JsonLiteException
	{
		char c = this.nextClean();
		String string;

		switch (c) {
		case '"':
		case '\'':
			return this.nextString(c);
		}

		/*
		 * Handle unquoted text. This could be the values true, false, or null,
		 * or it can be a number. An implementation (such as this one) is
		 * allowed to also accept non-standard forms.
		 * 
		 * Accumulate characters until we reach the end of the text or a
		 * formatting character.
		 */

		StringBuffer sb = new StringBuffer();
		while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
			sb.append(c);
			c = this.next();
		}
		this.back();

		string = sb.toString().trim();
		if ("".equals(string)) {
			throw this.syntaxError("Missing value");
		}
		return string;
	}

	public KeyType getKeyType()
	{
		return keyType;
	}

	public Class<?> getBeanClass()
	{
		return beanClass;
	}

	public Object getHeaderValue()
	{
		char c;
		String key;
		// Reset key type & bean class. Note that only one of them can be set.
		keyType = null;
		beanClass = null;

		if (nextClean() != '{') {
			throw syntaxError("A JsonLite header value text must begin with '{'");
		}
		String type = null;
		Class clazz = null;
		ArrayList<String> typeList = null;
		// valueList is used only if the generic type is specified in order
		// to determine the size of the array
		ArrayList valueList = null;
		Object value = null;
		for (;;) {
			c = nextClean();
			switch (c) {
			case 0:
				throw syntaxError("A JsonLite header value text must end with '}'");
			case '}':
				return value;
			default:
				back();
				key = nextValue(null, null).toString();
			}

			// The key is followed by ':'.

			c = nextClean();
			if (c != ':') {
				throw syntaxError("Expected a ':' after a key");
			}

			if (key.equalsIgnoreCase("c")) {
				// class name
				Object t = nextValue(null, null);
				if (t != null) {
					type = t.toString();
					try {
						clazz = getType(type);
					} catch (Exception e) {
						throw new JsonLiteException(e);
					}
					if (clazz != null) {
						if (clazz == Object.class) {
							if (typeList != null) {
								value = Array.newInstance(clazz, typeList.size());
							}
						} else if (KeyType.class.isAssignableFrom(clazz)) {
							// KeyType
							Object[] enums = clazz.getEnumConstants();
							if (enums != null && enums.length > 0) {
								keyType = (KeyType) enums[0];
							} else {
								throw new JsonLiteException("Invalid KeyType enum class: " + clazz.getCanonicalName());
							}
						} else if (Collection.class.isAssignableFrom(clazz)) {
							try {
								value = clazz.newInstance();
							} catch (Exception e) {
								throw new JsonLiteException(e);
							}
						} else if (Map.class.isAssignableFrom(clazz)) {
							try {
								value = clazz.newInstance();
							} catch (Exception e) {
								throw new JsonLiteException(e);
							}
						} else if (isArray) {
							if (typeList != null) {
								value = Array.newInstance(clazz, typeList.size());
							}
						} else {
							// regular class
							beanClass = clazz;
							try {
								value = clazz.newInstance();
							} catch (Exception e) {
								throw new JsonLiteException(e);
							}
						}
					} else {
						// UUID for KeyType
						if (type.equalsIgnoreCase("u")) {
							String uuidStr = (String) nextStringValue();
							String split[] = uuidStr.split(":");
							if (split.length > 1) {
								try {
									long uuidMostSigBits = Long.parseLong(split[0]);
									long uuidLeastSigBits = Long.parseLong(split[1]);
									keyType = KeyTypeManager.getKeyType(uuidMostSigBits, uuidLeastSigBits);
								} catch (Exception ex) {
									throw new JsonLiteException(
											"Invalid KeyType UUID. Must be 2 long values separated by ':': " + uuidStr,
											ex);
								}
							} else {
								throw new JsonLiteException(
										"Invalid KeyType UUID. Must be 2 long values separated by ':': " + uuidStr);
							}
						}
					}
				}
			} else if (key.equalsIgnoreCase("t")) {
				typeList = getStringArrayList();

			} else if (key.equalsIgnoreCase("d")) {
				// if (typeList != null || genericType != null) {
				c = nextClean();
				if (c != '[') {
					throw syntaxError("Expected a '[' after \"d\":");
				}
				int i = 0;
				for (;;) {
					c = nextClean();
					switch (c) {
					case ']':
						break;
					case ',':
						String ts;
						if (typeList == null) {
							ts = genericTypeString;
						} else {
							ts = typeList.get(i++);
						}
						Object val = nextValue(null, ts);
						if (val instanceof JsonLite) {
							JsonLite jl = (JsonLite)val;
							val = JsonLiteSerializer.checkDomainObject(jl);							
						}
						if (value == null) {
							if (valueList == null) {
								valueList = new ArrayList();
							}
							valueList.add(val);
						} else if (value.getClass().isArray()) {
							Array.set(value, i++, value);
						} else {
							((Collection) value).add(val);
						}
						break;
					default:
						back();
						if (typeList == null) {
							ts = genericTypeString;
						} else {
							ts = typeList.get(i++);
						}
						val = nextValue(null, ts);
						if (val instanceof JsonLite) {
							JsonLite jl = (JsonLite)val;
							Class dataClass = jl.getKeyType().getDomainClass();
							if (dataClass != null) {
								try {
									IJsonLiteWrapper wrapper = (IJsonLiteWrapper)dataClass.newInstance();
									wrapper.fromJsonLite(jl);
									val = wrapper;
								} catch (Exception ex) {
									throw new JsonLiteException (ex);
								}
							}
							
						}
						if (value == null) {
							if (valueList == null) {
								if (clazz == byte.class) {
									valueList = new ArrayList<Byte>();
								} else {
									valueList = new ArrayList();
								}
							}
							valueList.add(val);
						} else if (value.getClass().isArray()) {
							Array.set(value, i++, value);
						} else {
							((Collection) value).add(val);
						}
						break;
					}
					if (c == ']') {
						break;
					}
				}
				// }
			}

			// Pairs are separated by ','.
			switch (c = nextClean()) {
			case ';':
			case ',':
				if (nextClean() == '}') {
					return value;
				}
				back();
				break;
			case '}':
				if (value == null && valueList != null) {
					value = Array.newInstance(clazz, valueList.size());
					for (int i = 0; i < valueList.size(); i++) {
						Object obj = valueList.get(i);
						try {
							if (clazz == byte.class && obj instanceof Byte == false) {
								Array.set(value, i, ((Integer) obj).byteValue());
							} else if (clazz == char.class && obj instanceof Character == false) {
								Array.set(value, i, ((String) obj).charAt(0));
							} else if (clazz == float.class && obj instanceof Float == false) {
								Array.set(value, i, ((Double) obj).floatValue());
							} else if (clazz == short.class && obj instanceof Short == false) {
								Array.set(value, i, ((Integer) obj).shortValue());
							} else {
								Array.set(value, i, obj);
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					// value = valueList.toArray(Array.newInstance(clazz,
					// valueList.size()));
				}
				return value;
			default:
				throw syntaxError("Expected a ',' or '}'");
			}
		}
	}

	private Object getBean(Class<?> beanClass)
	{
		try {
			Object bean = beanClass.newInstance();
			// get all setters
			Method methods[] = bean.getClass().getMethods();
			Map<String, Method> methodMap = new HashMap(methods.length, 1f);
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().startsWith("set") && methods[i].getParameterTypes().length == 1) {
					methodMap.put(methods[i].getName(), methods[i]);
				}
			}
			char c;
			String key;
			for (;;) {
				c = nextClean();
				switch (c) {
				case 0:
					throw syntaxError("A JsonLite header value text must end with '}'");
				case '{':
				case ',':
					continue;
				case '}':
					return bean;
				default:
					back();
					key = nextValue(null, null).toString();
				}

				// The key is followed by ':'.
				c = nextClean();
				if (c != ':') {
					throw syntaxError("Expected a ':' after a key");
				}
				Object value = nextValue(key, null);
				Method method = methodMap.get("set" + key);
				if (method == null) {
					throw new JsonLiteException("Undefined setter method: " + bean.getClass().getCanonicalName()
							+ ".set" + key);
				}
				try {
					method.invoke(bean, value);
				} catch (Exception ex) {
					throw new JsonLiteException("Invalid key for the setter method: "
							+ bean.getClass().getCanonicalName() + ".set" + key, ex);
				}
			}
		} catch (Exception ex) {
			throw new JsonLiteException("Error parsing bean " + beanClass.getCanonicalName(), ex);
		}
	}

	private Collection getCollection(Collection collection, KeyInfo keyInfo)
	{
		try {
			if (nextClean() != '[') {
				throw syntaxError("A JSON array text must start with '['");
			}

			Map<String, KeyInfo> schemaTypeMap = null;
			if (keyInfo != null) {
				schemaTypeMap = JsonLiteSchemaManager.getSchemaMap(keyInfo.componentType);
			}

			if (nextClean() != ']') {
				back();
				for (;;) {
					char c = nextClean();
					if (c == ',') {
						back();
						collection.add(null);
					} else if (schemaTypeMap != null) {
						JsonLiteTokenizer jlk = new JsonLiteTokenizer(reader, schemaTypeMap);
						jlk.usePrevious = true;
						jlk.previous = c;
						JsonLite jl = new JsonLite(jlk, null);
						if (IJsonLiteWrapper.class.isAssignableFrom(keyInfo.componentType)) {
							try {
								IJsonLiteWrapper wrapper = (IJsonLiteWrapper)keyInfo.componentType.newInstance();
								wrapper.fromJsonLite(jl);
								collection.add(wrapper);
							} catch (Exception e) {
								throw new JsonLiteException(e);
							}
						} else {
							collection.add(jl);
						}
					} else {
						back();
						collection.add(nextValue(null, null));
					}
					switch (nextClean()) {
					case ',':
						if (nextClean() == ']') {
							return collection;
						}
						back();
						break;
					case ']':
						return collection;
					default:
						throw syntaxError("Expected a ',' or ']'");
					}
				}
			}
			return collection;
		} catch (Exception ex) {
			throw new JsonLiteException("Error parsing collection " + keyInfo.toString(), ex);
		}
	}

	public Object[] getArray()
	{
		Collection col = getCollection(new ArrayList(), null);
		if (col == null) {
			return null;
		}
		return col.toArray();
	}
	
	private ArrayList getArrayList()
	{
		if (nextClean() != '[') {
			throw syntaxError("A JSON array text must start with '['");
		}
		ArrayList arrayList = new ArrayList();
		if (nextClean() != ']') {
			back();
			for (;;) {
				char c = nextClean();
				if (c == ',') {
					back();
					arrayList.add(null);
				} else {
					back();
					arrayList.add(nextValue(null, null));
				}
				switch (nextClean()) {
				case ',':
					if (nextClean() == ']') {
						return arrayList;
					}
					back();
					break;
				case ']':
					return arrayList;
				default:
					throw syntaxError("Expected a ',' or ']'");
				}
			}
		}
		return arrayList;
	}

	/**
	 * Returns an array list of raw values, i.e., String without quotes. This
	 * method should be invoked to get meta data contents.
	 */
	private ArrayList<String> getStringArrayList()
	{
		if (nextClean() != '[') {
			throw syntaxError("A JSON array text must start with '['");
		}
		ArrayList arrayList = new ArrayList();
		if (nextClean() != ']') {
			back();
			for (;;) {
				if (nextClean() == ',') {
					back();
					arrayList.add(null);
				} else {
					back();
					arrayList.add(nextStringValue());
				}
				switch (nextClean()) {
				case ',':
					if (nextClean() == ']') {
						return arrayList;
					}
					back();
					break;
				case ']':
					return arrayList;
				default:
					throw syntaxError("Expected a ',' or ']'");
				}
			}
		}
		return arrayList;
	}

	private Map getMap(Map map, KeyInfo keyInfo)
	{
		char c;
		String prevKey;
		String key = null;
		Class beanClass = null;
		Object bean = null;
		Map<String, Method> methodMap = null;
		c = nextClean();
		KeyType keyType;

		if (c != '{') {
			throw syntaxError("A JsonLite text must begin with '{'");
		}
		for (;;) {
			c = nextClean();
			switch (c) {
			case 0:
				throw syntaxError("A JsonLite text must end with '}'");
			case '}':
				return map;
			default:
				back();
				prevKey = key;
				key = nextValue(null, null).toString();
			}

			// The key is followed by ':'.
			c = nextClean();
			if (c != ':') {
				throw syntaxError("Expected a ':' after a key");
			}
			Object value = nextValue(key, null);

//			if (keyType != null) {
//				KeyType kt = keyType.getKeyType(key);
//				if (kt == null) {
//					throw new JsonLiteException("Invalid key: " + key + ". It does not exist in "
//							+ keyType.getClass().getCanonicalName());
//				}
//				Class ktType = kt.getType();
//				value = nextValue(key, getTypeName(ktType));
//			} else {
//				keyType = getKeyType();
//				if (keyType != null) {
//					KeyType kt = keyType.getKeyType(key);
//					if (kt == null) {
//						throw new JsonLiteException("Invalid key: " + key + ". It does not exist in "
//								+ keyType.getClass().getCanonicalName());
//					}
//					Class ktType = kt.getType();
//					value = nextValue(key, getTypeName(ktType));
//				} else {
//					value = nextValue(key, null);
//				}
//			}
	
			map.put(key, value);

			// Pairs are separated by ','.
			c = nextClean();
			switch (c) {
			case ';':
			case ',':
				if (nextClean() == '}') {
					return map;
				}
				back();
				break;
			case '}':
				return map;
			default:
				throw syntaxError("Expected a ',' or '}'");
			}
		}
	}

	/**
	 * Skip characters until the next character is the requested character. If
	 * the requested character is not found, no characters are skipped.
	 * 
	 * @param to
	 *            A character to skip to.
	 * @return The requested character, or zero if the requested character is
	 *         not found.
	 */
	public char skipTo(char to) throws JsonLiteException
	{
		char c;
		try {
			long startIndex = this.index;
			long startCharacter = this.character;
			long startLine = this.line;
			this.reader.mark(1000000);
			do {
				c = this.next();
				if (c == 0) {
					this.reader.reset();
					this.index = startIndex;
					this.character = startCharacter;
					this.line = startLine;
					return c;
				}
			} while (c != to);
		} catch (IOException exc) {
			throw new JsonLiteException(exc);
		}

		this.back();
		return c;
	}

	/**
	 * Make a JsonLiteException to signal a syntax error.
	 * 
	 * @param message
	 *            The error message.
	 * @return A JsonLiteException object, suitable for throwing
	 */
	public JsonLiteException syntaxError(String message)
	{
		return new JsonLiteException(message + this.toString());
	}

	/**
	 * Make a printable string of this JSONTokener.
	 * 
	 * @return " at {index} [character {character} line {line}]"
	 */
	public String toString()
	{
		return " at " + this.index + " [character " + this.character + " line " + this.line + "]";
	}
}
