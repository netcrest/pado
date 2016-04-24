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
package com.netcrest.pado.internal.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * SchemaUtil provides utility methods for generating schema files for the
 * import_csv command.
 * 
 * @author dpark
 *
 */
public class SchemaUtil
{
	public static String readFile(InputStream is) throws IOException, URISyntaxException
	{
		StringBuffer buffer = new StringBuffer(2000);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
				buffer.append("\n");
			}
		} finally {
			reader.close();
		}
		return buffer.toString();
	}

	public static SchemaProp determineSchemaProp(File csvFile, int headerRow) throws IOException
	{
		InputStream inputStream = new FileInputStream(csvFile);
		Reader reader = new InputStreamReader(inputStream, Charset.forName("US-ASCII"));
		String line;
		int lineNum = 0;
		int headerLineNum = 0;
		int headerNumColumns = 0;
		String delimiter = ",";
		String headerLine = "";

		if (headerRow > 0) {
			while ((line = readLine(reader)) != null && lineNum < headerRow) {
				lineNum++;
				if (lineNum == headerRow) {
					break;
				}
			}
			if (lineNum > headerRow) {
				throw new IOException(
						"Secified headerRow is greater than the total number of lines in the data file. startRow="
								+ headerRow);
			}

			String tabTokens[] = getTokens(line, (char) 29); // tab
			String commaTokens[] = getTokens(line, ',');
			int numColumns;
			if (tabTokens.length > commaTokens.length) {
				numColumns = tabTokens.length;
				delimiter = "\\t";
			} else {
				numColumns = commaTokens.length;
				delimiter = ",";
			}
			headerLineNum = lineNum;
			headerNumColumns = numColumns;
			headerLine = line;
		} else {
			while ((line = readLine(reader)) != null && lineNum < 10) {
				lineNum++;
				String tabTokens[] = getTokens(line, '\t'); // tab
				String commaTokens[] = getTokens(line, ',');
				int numColumns;
				if (tabTokens.length > commaTokens.length) {
					numColumns = tabTokens.length;
					delimiter = "\\t";
				} else {
					numColumns = commaTokens.length;
					delimiter = ",";
				}
				if (numColumns > headerNumColumns) {
					headerLineNum = lineNum;
					headerNumColumns = numColumns;
					headerLine = line;
				}
			}
		}
		reader.close();

		SchemaProp sp = new SchemaProp();
		sp.headerLineNum = headerLineNum;
		sp.startRow = headerLineNum + 1;
		sp.delimiter = delimiter;
		sp.fieldNames = "";

		// Parse the line to get all columns
		String fieldNames[] = headerLine.split(delimiter);
		String camelCaseNames[] = new String[fieldNames.length];
		for (int i = 0; i < fieldNames.length; i++) {
			camelCaseNames[i] = toCamelCase(fieldNames[i]);
		}
		ArrayList<String> fieldNameList = new ArrayList<String>(camelCaseNames.length);
		for (String fieldName : camelCaseNames) {
			if (fieldNameList.contains(fieldName) == false) {
				fieldNameList.add(fieldName);
			} else {
				int j = 2;
				String newFieldName = fieldName + j;
				while (fieldNameList.contains(newFieldName)) {
					j++;
					newFieldName = fieldName + j;
				}
				fieldNameList.add(newFieldName);
			}
		}
		StringBuffer buffer = new StringBuffer(fieldNameList.size() * 20);
		for (String fieldName : fieldNameList) {
			buffer.append(fieldName);
			buffer.append("\n");
		}
		sp.fieldNames = buffer.toString();
		return sp;
	}

	/**
	 * Returns an array of tokens extracted from the specified line parameter.
	 * 
	 * @param line
	 *            The string to be tokenized.
	 * @param delimiter
	 *            Token separator.
	 */
	public static String[] getTokens(String line, char delimiter)
	{
		if (line.length() == 0) {
			return null;
		}
		
		// HBAN,23.82,300,23.79,800,"Thu, ""test"", 'hello' Jun 08 09:41:19 EDT
		// 2006",99895,1094931009,82,99895,8,HBAN
		ArrayList<String> list = new ArrayList<String>();
		boolean openQuote = false;
		String value = "";
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);

			if (c == delimiter) {
				if (openQuote == false) {
					value = value.trim();
					if (value.startsWith("\"") && (value.endsWith("\"") || value.indexOf(" ") != -1)) {
						value = value.substring(1);
						if (value.endsWith("\"")) {
							value = value.substring(0, value.length() - 1);
						}
					}

					list.add(value);
					value = "";
					continue;
				}
			} else if (c == '"' && delimiter == ',') {
				openQuote = !openQuote;
			}
			value += c;
		}
		if (value.startsWith("\"") && value.endsWith("\"")) {
			value = value.substring(1);
			value = value.substring(0, value.length() - 1);
		}
		list.add(value);
		return (String[]) list.toArray(new String[0]);
	}

	private static String toCamelCase(String str)
	{
		StringBuffer buffer = new StringBuffer(str.length());
		StringBuffer word = new StringBuffer(str.length());
		char[] charArray = str.toCharArray();
		boolean nextCharUpper = false;
		for (char c : charArray) {
			switch (c) {
			case ' ':
			case '-':
			case '_':
				nextCharUpper = true;
				break;
			default:
				if ('0' <= c && c <= '9' || 'A' <= c && c <= 'Z' || 'a' <= c && c <= 'z') {
					if (nextCharUpper) {
						// Convert the first char to uppercase and rest
						// lowercase
						if (word.length() > 0) {
							char firstChar = word.charAt(0);
							if ('a' <= firstChar && firstChar <= 'z') {
								firstChar = (char) ('A' + firstChar - 'a');
							}
							String restWord = word.substring(1).toLowerCase();
							buffer.append(firstChar);
							buffer.append(restWord);
							word.delete(0, word.length());
							word.append(c);
						}
						nextCharUpper = false;
					} else {
						word.append(c);
					}
				}
			}
		}
		if (word.length() > 0) {
			char firstChar = word.charAt(0);
			if ('a' <= firstChar && firstChar <= 'z') {
				firstChar = (char) ('A' + firstChar - 'a');
			}
			buffer.append(firstChar);
			if (word.length() >= 1) {
				String wordToAdd = word.substring(1);
				buffer.append(wordToAdd);
			}
		}

		// If string begins with a number then prefix it with '_'
		String prefix = "";
		if (buffer.length() > 0) {
			char c = buffer.charAt(0);
			if ('0' <= c && c <= '9') {
				prefix = "_";
			}
		}
		return prefix + buffer.toString();
	}

	private static String readLine(Reader reader) throws IOException
	{
		StringBuffer buffer = new StringBuffer(100);
		boolean endOfLine = false;
		boolean oddQuote = false;
		int c;
		buffer.delete(0, buffer.length());
		while ((c = reader.read()) != -1) {
			// TODO: The following supports enclosed quotes for ','. All
			// other delimiters treat the double quote as a regular character.
			// We may need to support delimiters other than ',' for enclosed
			// quotes.
			// if (c == delimiter && c != ',') {
			// oddQuote = false;
			// buffer.append((char) c);
			// } else {
			switch (c) {
			case '"':
				oddQuote = !oddQuote;
				buffer.append((char) c);
				break;
			case '\r':
				break;
			case '\n':
				if (oddQuote) {
					buffer.append((char) c);
				} else {
					endOfLine = true;
				}
				break;
			default:
				buffer.append((char) c);
				break;
			}
			if (endOfLine) {
				break;
			}
		}
		// }
		if (c == -1) {
			return null;
		}
		return buffer.toString();
	}

	public static class SchemaProp
	{
		public int headerLineNum;
		public int startRow;
		public String delimiter;
		public String fieldNames;
	}
}
