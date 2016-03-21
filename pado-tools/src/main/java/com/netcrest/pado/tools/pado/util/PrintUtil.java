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
package com.netcrest.pado.tools.pado.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.query.Struct;
import com.gemstone.gemfire.cache.query.types.CollectionType;
import com.gemstone.gemfire.cache.query.types.ObjectType;
import com.gemstone.gemfire.cache.query.types.StructType;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.internal.util.StringUtil;
import com.netcrest.pado.temporal.TemporalData;
import com.netcrest.pado.tools.pado.PadoShell;

public class PrintUtil {
	private static boolean tableFormat = false;

	public static boolean isTableFormat() {
		return tableFormat;
	}

	public static void setTableFormat(boolean tableFormat) {
		PrintUtil.tableFormat = tableFormat;
	}

	/**
	 * Prints the region entries. It prints both keys and values formatted.
	 * 
	 * @param region
	 * @param startIndex
	 * @param startRowNum
	 * @param rowCount
	 * @param keyList
	 * @return Returns the number of rows printed.
	 * @throws Exception
	 */
	public static int printEntries(Region region, Iterator regionIterator,
			int startIndex, int startRowNum, int rowCount, List keyList)
			throws Exception {
		if (isTableFormat() == false) {
			return SimplePrintUtil.printEntries(region, regionIterator,
					startIndex, startRowNum, rowCount, keyList);
		}

		if (region == null || regionIterator == null) {
			PadoShell.println("Path is null");
			return 0;
		}

		int endIndex = startIndex + rowCount; // exclusive
		if (endIndex >= region.size()) {
			endIndex = region.size();
		}

		// Determine max column lengths
		HashSet keyNameSet = new HashSet();
		HashSet valueNameSet = new HashSet();
		ArrayList keyMaxLenList = new ArrayList();
		ArrayList valueMaxLenList = new ArrayList();
		Object key = null;
		Object value = null;
		Set nameSet = region.entrySet();
		int index = startIndex;

		ArrayList<Region.Entry> entryList = new ArrayList();
		for (Iterator itr = regionIterator; index < endIndex && itr.hasNext(); index++) {
			Region.Entry entry = (Region.Entry) itr.next();
			entryList.add(entry);
			key = entry.getKey();
			value = entry.getValue();
			computeMaxLengths(keyMaxLenList, valueMaxLenList, key, value);
			keyNameSet.add(key.getClass().getName());
			if (value != null) {
				valueNameSet.add(value.getClass().getName());
			}
		}

		if (key == null) {
			return 0;
		}

		// Print headers including row column
		int rowMax = String.valueOf(endIndex).length();
		if (rowMax < 3) {
			rowMax = 3;
		}
		printHeaders(keyMaxLenList, valueMaxLenList, key, value, rowMax);

		// Print keys and values
		int row = startRowNum;
		index = startIndex;
		for (Iterator itr = entryList.iterator(); index < endIndex
				&& itr.hasNext(); index++) {
			StringBuffer printStr = new StringBuffer();
			printStr.append(StringUtil.getRightPaddedString(row + "", rowMax,
					' '));
			printStr.append("  ");

			Region.Entry entry = (Region.Entry) itr.next();
			key = entry.getKey();
			value = entry.getValue();
			if (keyList != null) {
				keyList.add(key);
			}
			String printObjStr = printObject(keyMaxLenList, key, true);
			printStr.append(printObjStr);
			printStr.append(" | ");
			printObjStr = printObject(valueMaxLenList, value, false);
			printStr.append(printObjStr);
			PadoShell.println(printStr.toString());
			row++;
		}
		PadoShell.println("");
		PadoShell.println(" Fetch size: " + rowCount);
		PadoShell.println("   Returned: " + (row - 1) + "/" + region.size());
		for (Object keyName : keyNameSet) {
			PadoShell.println("  Key Class: " + keyName);
		}
		for (Object valueName : valueNameSet) {
			PadoShell.println("Value Class: " + valueName);

		}
		return endIndex - startIndex;
	}

	public static int printEntries(Region region, Map keyMap, List keyList)
			throws Exception {
		if (isTableFormat() == false) {
			return SimplePrintUtil.printEntries(region, keyMap, keyList);
		}

		if (region == null) {
			PadoShell.printlnError("Path is null");
			return 0;
		}

		// Determine max column lengths
		HashSet keyNameSet = new HashSet();
		HashSet valueNameSet = new HashSet();
		ArrayList keyMaxLenList = new ArrayList();
		ArrayList valueMaxLenList = new ArrayList();
		ArrayList indexList = new ArrayList(keyMap.keySet());
		Collections.sort(indexList);
		Object key = null;
		Object value = null;
		for (Iterator iterator = indexList.iterator(); iterator.hasNext();) {
			Object index = iterator.next();
			key = keyMap.get(index);
			value = region.get(key);
			computeMaxLengths(keyMaxLenList, valueMaxLenList, key, value);
			keyNameSet.add(key.getClass().getName());
			if (value != null) {
				valueNameSet.add(value.getClass().getName());
			}
		}
		if (key == null) {
			return 0;
		}

		// Print headers including row column
		int rowCount = keyMap.size();
		int rowMax = String.valueOf(rowCount).length();
		if (rowMax < 3) {
			rowMax = 3;
		}
		printHeaders(keyMaxLenList, valueMaxLenList, key, value, rowMax);

		// Print keys and values
		int row = 1;
		for (Iterator iterator = indexList.iterator(); iterator.hasNext();) {
			StringBuffer printStr = new StringBuffer();
			printStr.append(StringUtil.getRightPaddedString(row + "", rowMax,
					' '));
			printStr.append("  ");

			Object index = iterator.next();
			key = keyMap.get(index);
			value = region.get(key);
			if (keyList != null) {
				keyList.add(key);
			}
			String printObjStr = printObject(keyMaxLenList, key, true);
			printStr.append(printObjStr);
			printStr.append(" | ");
			printObjStr = printObject(valueMaxLenList, value, false);
			printStr.append(printObjStr);
			PadoShell.println(printStr.toString());
			row++;
		}
		PadoShell.println("");
		for (Object keyName : keyNameSet) {
			PadoShell.println("  Key Class: " + keyName);
		}
		for (Object valueName : valueNameSet) {
			PadoShell.println("Value Class: " + valueName);
		}
		return rowCount;
	}

	public static int printEntries(Map gridMapBiz, Set keySet, List keyList)
			throws Exception {

		if (isTableFormat() == false) {
			return SimplePrintUtil.printEntries(gridMapBiz, keySet, keyList);
		}

		if (gridMapBiz == null) {
			PadoShell.printlnError("Path is null");
			return 0;
		}

		// Determine max column lengths
		HashSet keyNameSet = new HashSet();
		HashSet valueNameSet = new HashSet();
		ArrayList keyMaxLenList = new ArrayList();
		ArrayList valueMaxLenList = new ArrayList();
		Object key = null;
		Object value = null;
		for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
			key = iterator.next();
			value = gridMapBiz.get(key);
			computeMaxLengths(keyMaxLenList, valueMaxLenList, key, value);
			keyNameSet.add(key.getClass().getName());
			if (value != null) {
				valueNameSet.add(value.getClass().getName());
			}
		}
		if (key == null) {
			return 0;
		}

		// Print headers including row column
		int rowCount = keySet.size();
		int rowMax = String.valueOf(rowCount).length();
		if (rowMax < 3) {
			rowMax = 3;
		}
		printHeaders(keyMaxLenList, valueMaxLenList, key, value, rowMax);

		// Print keys and values
		int row = 1;
		for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
			StringBuffer printStr = new StringBuffer();
			printStr.append(StringUtil.getRightPaddedString(row + "", rowMax,
					' '));
			printStr.append("  ");

			key = iterator.next();
			value = gridMapBiz.get(key);
			if (keyList != null) {
				keyList.add(key);
			}
			String printObjStr = printObject(keyMaxLenList, key, true);
			printStr.append(printObjStr);
			printStr.append(" | ");
			printObjStr = printObject(valueMaxLenList, value, false);
			printStr.append(printObjStr);
			PadoShell.println(printStr.toString());
			row++;
		}
		for (Object keyName : keyNameSet) {
			PadoShell.println("  Key Class: " + keyName);
		}
		for (Object valueName : valueNameSet) {
			PadoShell.println("Value Class: " + valueName);
		}
		return rowCount;
	}

	public static int printEntries(Map map, int startIndex, int startRowNum,
			int rowCount, int actualSize, List keyList) throws Exception {
		if (isTableFormat() == false) {
			return SimplePrintUtil.printEntries(map, startIndex, startRowNum,
					rowCount, actualSize, keyList);
		}

		if (map == null) {
			PadoShell.printlnError("Map is null");
			return 0;
		}

		// Determine max column lengths
		HashSet keyNameSet = new HashSet();
		HashSet valueNameSet = new HashSet();
		ArrayList keyMaxLenList = new ArrayList();
		ArrayList valueMaxLenList = new ArrayList();
		Object key = null;
		Object value = null;
		Set entrySet = map.entrySet();
		int count = 0;
		for (Iterator itr = entrySet.iterator(); count < rowCount
				&& itr.hasNext(); count++) {
			Map.Entry entry = (Map.Entry) itr.next();
			key = entry.getKey();
			value = entry.getValue();
			computeMaxLengths(keyMaxLenList, valueMaxLenList, key, value);
			keyNameSet.add(key.getClass().getName());
			if (value != null) {
				valueNameSet.add(value.getClass().getName());
			}
		}

		if (key == null) {
			return 0;
		}

		// Print headers including row column
		int rowMax = String.valueOf(startRowNum + rowCount - 1).length();
		if (rowMax < 3) {
			rowMax = 3;
		}
		printHeaders(keyMaxLenList, valueMaxLenList, key, value, rowMax);

		// Print keys and values
		// int row = 1;
		count = 0;
		int row = startRowNum;
		int lastRow = startRowNum + rowCount - 1;
		for (Iterator itr = entrySet.iterator(); count < rowCount
				&& itr.hasNext(); count++) {
			StringBuffer printStr = new StringBuffer();
			printStr.append(StringUtil.getRightPaddedString(row + "", rowMax,
					' '));
			printStr.append("  ");

			Map.Entry entry = (Map.Entry) itr.next();
			key = entry.getKey();
			value = entry.getValue();
			if (keyList != null) {
				keyList.add(key);
			}
			String printObjStr = printObject(keyMaxLenList, key, true);
			printStr.append(printObjStr);
			printStr.append(" | ");
			printObjStr = printObject(valueMaxLenList, value, false);
			printStr.append(printObjStr);
			PadoShell.println(printStr.toString());
			row++;
		}
		PadoShell.println("");
		PadoShell.println(" Fetch size: " + rowCount);
		PadoShell.println("   Returned: " + (row - 1) + "/" + actualSize);
		for (Object keyName : keyNameSet) {
			PadoShell.println("  Key Class: " + keyName);
		}
		for (Object valueName : valueNameSet) {
			PadoShell.println("Value Class: " + valueName);
		}
		return count;
	}

	public static int printSet(Set set, int rowCount, List keyList)
			throws Exception {
		return printSet(set, rowCount, keyList, true);
	}

	public static int printSet(Set set, int rowCount, List keyList,
			boolean showSummary) throws Exception {
		return printSet(set, rowCount, keyList, "Key", showSummary);
	}

	public static int printSet(Set set, int rowCount, List keyList,
			String keyColumnName, boolean showSummary) throws Exception {
		if (isTableFormat() == false) {
			return SimplePrintUtil.printSet(set, rowCount, keyList,
					keyColumnName, showSummary);
		}

		if (set == null) {
			return 0;
		}

		// Determine max column lengths
		HashSet keyNameSet = new HashSet();
		ArrayList keyMaxLenList = new ArrayList();
		Object key = null;
		Set nameSet = set;
		int count = 0;
		int keyMin = keyColumnName.length();
		for (Iterator itr = nameSet.iterator(); count < rowCount
				&& itr.hasNext(); count++) {
			key = itr.next();
			computeMaxLengths(keyMaxLenList, key, true, keyMin, 0);
			keyNameSet.add(key.getClass().getName());
		}

		if (key == null) {
			return 0;
		}

		// Print headers including row column
		int rowMax = String.valueOf(rowCount).length();
		if (rowMax < 3) {
			rowMax = 3;
		}
		printHeaders(keyMaxLenList, null, key, null, rowMax, keyColumnName,
				null, false);

		// Print keys and values
		int row = 1;
		count = 0;
		for (Iterator itr = nameSet.iterator(); count < rowCount
				&& itr.hasNext(); count++) {
			StringBuffer printStr = new StringBuffer();
			printStr.append(StringUtil.getRightPaddedString(row + "", rowMax,
					' '));
			printStr.append("  ");

			key = itr.next();
			if (keyList != null) {
				keyList.add(key);
			}
			String printObjStr = printObject(keyMaxLenList, key, true);
			printStr.append(printObjStr);
			PadoShell.println(printStr.toString());
			row++;
		}
		if (showSummary) {
			PadoShell.println("");
			PadoShell.println("Displayed (fetched): " + (row - 1));
			PadoShell.println("        Actual Size: " + set.size());
			for (Object keyName : keyNameSet) {
				PadoShell.println("          " + keyColumnName + " Class: "
						+ keyName);
			}
		}
		return row - 1;
	}

	public static int printEntries(Map map, int rowCount, List keyList)
			throws Exception {
		return printEntries(map, rowCount, keyList, true, true);
	}

	public static int printEntries(Map map, int rowCount, List keyList,
			boolean showSummary, boolean showValues) throws Exception {
		return printEntries(map, rowCount, keyList, "Key", "Value",
				showSummary, showValues);
	}

	public static int printEntries(Map map, int rowCount, List keyList,
			String keyColumnName, String valueColumnName, boolean showSummary,
			boolean showValues) throws Exception {
		return printEntries(map, rowCount, keyList, "Key", "Value",
				showSummary, showValues, true, false, false);
	}

	public static int printEntries(Map map, int rowCount, List keyList,
			String keyColumnName, String valueColumnName, boolean showSummary,
			boolean showValues, boolean columnSeperator, boolean sortByKey,
			boolean sortByValue) throws Exception {
		if (isTableFormat() == false) {
			return SimplePrintUtil.printEntries(map, rowCount, keyList,
					keyColumnName, valueColumnName, showSummary, showValues);
		}

		if (map == null) {
			PadoShell.printlnError("Path is null");
			return 0;
		}

		// Determine max column lengths
		HashSet keyNameSet = new HashSet();
		HashSet valueNameSet = new HashSet();
		ArrayList keyMaxLenList = new ArrayList();
		ArrayList valueMaxLenList = new ArrayList();
		Object key = null;
		Object value = null;
		ArrayList indexList = null;
		if (sortByValue && !sortByKey && map instanceof HashMap) {
			map = sortHashMapByValues((HashMap) map);
		}
		indexList = new ArrayList(map.keySet());
		if (sortByKey) {
			Collections.sort(indexList);
		}
		int count = 0;
		int keyMin = keyColumnName.length();
		int valueMin = valueColumnName.length();
		for (Iterator itr = indexList.iterator(); count < rowCount
				&& itr.hasNext(); count++) {
			key = itr.next();
			value = map.get(key);
			computeMaxLengths(keyMaxLenList, valueMaxLenList, key, value,
					keyMin, valueMin);
			keyNameSet.add(key.getClass().getName());
			if (value != null) {
				valueNameSet.add(value.getClass().getName());
			}
		}

		if (key == null) {
			return 0;
		}

		// Print headers including row column
		int rowMax = String.valueOf(rowCount).length();
		if (rowMax < 3) {
			rowMax = 3;
		}
		printHeaders(keyMaxLenList, valueMaxLenList, key, value, rowMax,
				keyColumnName, valueColumnName, showValues, columnSeperator);

		// Print keys and values
		int row = 1;
		count = 0;
		for (Iterator itr = indexList.iterator(); count < rowCount
				&& itr.hasNext(); count++) {
			StringBuffer printStr = new StringBuffer();
			printStr.append(StringUtil.getRightPaddedString(row + "", rowMax,
					' '));
			printStr.append("  ");

			key = itr.next();
			value = map.get(key);
			if (keyList != null) {
				keyList.add(key);
			}
			String printObjStr = printObject(keyMaxLenList, key, true);
			printStr.append(printObjStr);
			if (showValues) {
				if (columnSeperator) {
					printStr.append(" | ");
				} else {
					printStr.append("   ");
				}
				printObjStr = printObject(valueMaxLenList, value, false);
				printStr.append(printObjStr);
			}
			PadoShell.println(printStr.toString());
			row++;
		}
		if (showSummary) {
			PadoShell.println("");
			PadoShell.println("Displayed (fetched): " + (row - 1));
			PadoShell.println("        Actual Size: " + map.size());
			for (Object keyName : keyNameSet) {
				PadoShell.println("          " + keyColumnName + " Class: "
						+ keyName);
			}
			for (Object valueName : valueNameSet) {
				PadoShell.println("        " + valueColumnName + " Class: "
						+ valueName);

			}
		}
		return row - 1;
	}

	private static void computeMaxLengths(List keyList, List valueList,
			Object key, Object value) {
		computeMaxLengths(keyList, valueList, key, value, 3, 5); // "Key",
																	// "Value"
	}

	private static void computeMaxLengths(List keyList, List valueList,
			Object key, Object value, int keyMin, int valueMin) {
		computeMaxLengths(keyList, key, true, keyMin, valueMin);
		computeMaxLengths(valueList, value, false, keyMin, valueMin);
	}

	private static String printTopHeaders(List list, Object obj,
			boolean printLastColumnSpaces, String primitiveHeader) {
		return printTopHeaders(list, obj, printLastColumnSpaces, primitiveHeader, 0);
	}

	private static String printTopHeaders(List list, Object obj,
			boolean printLastColumnSpaces, String primitiveHeader,
			int printColumnIndex) {
		StringBuffer printStr = new StringBuffer();
		Object object = obj;
		if (object == null) {
			object = "null";
		}
		if (object instanceof String || object.getClass().isPrimitive()
				|| object.getClass() == Boolean.class
				|| object.getClass() == Byte.class
				|| object.getClass() == Character.class
				|| object.getClass() == Short.class
				|| object.getClass() == Integer.class
				|| object.getClass() == Long.class
				|| object.getClass() == Float.class
				|| object.getClass() == Double.class
				|| object.getClass().isArray() || object instanceof Date) {

			int maxLen = (Integer) list.get(printColumnIndex);
			if (maxLen < primitiveHeader.length()) {
				maxLen = primitiveHeader.length();
			}
			if (printLastColumnSpaces) {
				printStr.append(StringUtil.getRightPaddedString(
						primitiveHeader, maxLen, ' '));
			} else {
				printStr.append(primitiveHeader);
			}

		} else if (object instanceof Map) {

			// Map
			Map map = (Map) object;
			Set set = map.keySet();
			if (set != null) {
				TreeSet keySet = new TreeSet(set);
				int listIndex = printColumnIndex;
				for (Object header : keySet) {
					int maxLen = (Integer) list.get(listIndex);
					if (listIndex == list.size() - 1) {
						if (printLastColumnSpaces) {
							printStr.append(StringUtil.getRightPaddedString(
									header.toString(), maxLen, ' '));
						} else {
							printStr.append(header);
						}
					} else {
						printStr.append(StringUtil.getRightPaddedString(
								header.toString(), maxLen, ' '));
						printStr.append("  ");
					}

					listIndex++;
				}
			}

		} else {

			// print object
			Class cls = object.getClass();
			Method methods[] = cls.getMethods();
			Method method;
			Class retType;
			String name;
			Object value;
			int listIndex = printColumnIndex;
			for (int i = 0; i < methods.length; i++) {
				method = methods[i];
				name = method.getName();
				if (name.length() <= 3 || name.startsWith("get") == false
						|| name.equals("getClass")) {
					continue;
				}
				retType = method.getReturnType();
				if (retType == Void.TYPE) {
					continue;
				}
				try {
					value = method.invoke(object, null);
					value = getPrintableValue(value);
					int maxLen = (Integer) list.get(listIndex);
					String header = name.substring(3);
					if (listIndex == list.size() - 1) {
						if (printLastColumnSpaces) {
							printStr.append(StringUtil.getRightPaddedString(
									header, maxLen, ' '));
						} else {
							printStr.append(header);
						}
					} else {
						printStr.append(StringUtil.getRightPaddedString(
								header, maxLen, ' '));
						printStr.append("  ");
					}

					listIndex++;
				} catch (Exception ex) {
				}
			}
		}
		return printStr.toString();
	}

	private static String printBottomHeaders(List list, Object obj,
			boolean printLastColumnSpaces, String primitiveHeader) {
		return printBottomHeaders(list, obj, printLastColumnSpaces, primitiveHeader, 0);
	}

	private static String printBottomHeaders(List list, Object obj,
			boolean printLastColumnSpaces, String primitiveHeader,
			int printColumnIndex) {
		StringBuffer printStr = new StringBuffer();
		Object object = obj;
		if (object == null) {
			object = "null";
		}

		if (object instanceof String || object.getClass().isPrimitive()
				|| object.getClass() == Boolean.class
				|| object.getClass() == Byte.class
				|| object.getClass() == Character.class
				|| object.getClass() == Short.class
				|| object.getClass() == Integer.class
				|| object.getClass() == Long.class
				|| object.getClass() == Float.class
				|| object.getClass() == Double.class
				|| object.getClass().isArray() || object instanceof Date) {

			int maxLen = (Integer) list.get(printColumnIndex);
			if (maxLen < primitiveHeader.length()) {
				maxLen = primitiveHeader.length();
			}
			if (printLastColumnSpaces) {
				printStr.append(StringUtil.getRightPaddedString(
						StringUtil.getRightPaddedString("",
								primitiveHeader.length(), '-'), maxLen, ' '));
			} else {
				printStr.append(StringUtil.getRightPaddedString("",
						primitiveHeader.length(), '-'));
			}

		} else if (object instanceof Map) {

			// Map
			Map map = (Map) object;
			Set<String> set = map.keySet();
			if (set != null) {
				TreeSet keySet = new TreeSet(set);
				int listIndex = printColumnIndex;
				for (Object header : keySet) {
					Object value = map.get(header);
					value = getPrintableValue(value);
					int maxLen = (Integer) list.get(listIndex);

					if (listIndex == list.size() - 1) {
						if (printLastColumnSpaces) {
							printStr.append(StringUtil.getRightPaddedString(
									StringUtil.getRightPaddedString("", header
											.toString().length(), '-'), maxLen,
									' '));
						} else {
							printStr.append(StringUtil.getRightPaddedString(
									"", header.toString().length(), '-'));
						}
					} else {
						printStr.append(StringUtil.getRightPaddedString(
										StringUtil
												.getRightPaddedString("",
														header.toString()
																.length(), '-'),
										maxLen, ' '));
						printStr.append("  ");
					}
					listIndex++;
				}
			}

		} else {

			Class cls = object.getClass();
			Method methods[] = cls.getMethods();
			Method method;
			Class retType;
			String name;
			Object value;
			int listIndex = printColumnIndex;
			for (int i = 0; i < methods.length; i++) {
				method = methods[i];
				name = method.getName();
				if (name.length() <= 3 || name.startsWith("get") == false
						|| name.equals("getClass")) {
					continue;
				}
				retType = method.getReturnType();
				if (retType == Void.TYPE) {
					continue;
				}
				try {
					value = method.invoke(object, null);
					value = getPrintableValue(value);
					int maxLen = (Integer) list.get(listIndex);
					String header = name.substring(3);

					if (listIndex == list.size() - 1) {
						if (printLastColumnSpaces) {
							printStr.append(StringUtil.getRightPaddedString(
											StringUtil.getRightPaddedString("",
													header.length(), '-'),
											maxLen, ' '));
						} else {
							printStr.append(StringUtil.getRightPaddedString(
									"", header.length(), '-'));
						}
					} else {
						printStr.append(StringUtil.getRightPaddedString(
								StringUtil.getRightPaddedString("",
										header.length(), '-'), maxLen, ' '));
						printStr.append("  ");
					}
					listIndex++;
				} catch (Exception ex) {
				}
			}
		}
		return printStr.toString();
	}

	private static void printHeaders(List keyList, List valueList, Object key,
			Object value, int rowMaxLen) throws Exception {
		printHeaders(keyList, valueList, key, value, rowMaxLen, "Key", "Value",
				true);
	}

	private static void printHeaders(List keyList, List valueList, Object key,
			Object value, int rowMaxLen, String keyColumnName,
			String valueColumnName, boolean showValues) throws Exception {
		boolean columnSeperator = true;
		printHeaders(keyList, valueList, key, value, rowMaxLen, keyColumnName,
				valueColumnName, showValues, columnSeperator);
	}

	private static void printHeaders(List keyList, List valueList, Object key,
			Object value, int rowMaxLen, String keyColumnName,
			String valueColumnName, boolean showValues, boolean columnSeperator)
			throws Exception {
		StringBuffer printStr = new StringBuffer();
		printStr.append(StringUtil.getRightPaddedString("Row", rowMaxLen, ' '));
		printStr.append("  ");
		String topHeaderStr = printTopHeaders(keyList, key, true, keyColumnName);
		printStr.append(topHeaderStr);
		if (showValues) {
			if (columnSeperator) {
				printStr.append(" | ");
			} else {
				printStr.append("   ");
			}
			topHeaderStr = printTopHeaders(valueList, value, false, valueColumnName);
			printStr.append(topHeaderStr);
		}
		PadoShell.println(printStr.toString());

		if (rowMaxLen < 3) {
			rowMaxLen = 3;
		}
		printStr = new StringBuffer();
		printStr.append(StringUtil.getRightPaddedString("", rowMaxLen, '-'));
		printStr.append("  ");
		String bottomHeaderStr = printBottomHeaders(keyList, key, true, keyColumnName);
		printStr.append(bottomHeaderStr);
		if (showValues) {
			if (columnSeperator) {
				printStr.append(" | ");
			} else {
				printStr.append("   ");
			}
			bottomHeaderStr = printBottomHeaders(valueList, value, false, valueColumnName);
			printStr.append(bottomHeaderStr);
		}
		PadoShell.println(printStr.toString());
	}

	private static void printHeaders(List<String> keyList, List rowMaxLenList) {
		int keyIndex = 0;
		StringBuffer printStr = new StringBuffer();
		for (String headerName : keyList) {
			String topHeaderStr = printTopHeaders(rowMaxLenList, null, true, headerName, keyIndex++);
			printStr.append(topHeaderStr);
			printStr.append("   ");
		}
		PadoShell.println(printStr.toString());
		printStr = new StringBuffer();
		keyIndex = 0;
		for (String headerName : keyList) {
			String bottomHeaderStr = printBottomHeaders(rowMaxLenList, null, true, headerName,
					keyIndex++);
			printStr.append(bottomHeaderStr);
			printStr.append("   ");
		}
		PadoShell.println(printStr.toString());
	}
	
	private static void printHeaders(List<String> keyList, List rowMaxLenList,int resultIndex) {
		StringBuffer printStr = new StringBuffer();
		int keyIndex = 0;
		for(int i=0;i<keyList.size();i++)
		{
			String topHeaderStr = printTopHeaders(rowMaxLenList, null, true, keyList.get(i), i);
			printStr.append(topHeaderStr);
			if(i==resultIndex) {
				printStr.append(" | ");
			} else {
				printStr.append("   ");
			}
		}
		PadoShell.println(printStr.toString());
		printStr = new StringBuffer();
		keyIndex = 0;
		
		for(int i=0;i<keyList.size();i++)
		{
			String bottomHeaderStr = printBottomHeaders(rowMaxLenList, null, true, keyList.get(i), i);
			printStr.append(bottomHeaderStr);
			if(i==(resultIndex))
				printStr.append(" | ");
			else
				printStr.append("   ");
		}
		PadoShell.println(printStr.toString());
	}
	
	/**
	 * Prints the SelectResults contents up to the specified rowCount.
	 * 
	 * @param srs
	 * @param startRowNum
	 * @param rowCount
	 * @return The number of rows printed
	 */
	public static int printScrollableResultSet(IScrollableResultSet srs, int startRow, boolean isShowHeader) {

		if (isTableFormat() == false) {
			return SimplePrintUtil.printScrollableResultSet(srs, startRow);
		}

		if (srs == null || srs.toList().size() == 0) {
			return 0;
		}

		HashSet elementNameSet = new HashSet();
		ArrayList maxLenList = new ArrayList();
		
		StructType structType = null;
		Struct struct = null;
		List srList = srs.toList();
		boolean isStructType = srList.size() > 0 && srList.get(0) instanceof Struct;
		int row = startRow;
		for (Object element : srList) {
			if (isStructType) {
				struct = (Struct) element;
				computeMaxLengths(maxLenList, struct);
				isStructType = true;
			} else {
				computeMaxLengths(maxLenList, element, false);
				if (element != null) {
					elementNameSet.add(element.getClass().getName());
					if  (element instanceof KeyMap) {
						if (((KeyMap)element).getKeyTypeName() != null) {
							elementNameSet.add(((KeyMap)element).getKeyTypeName());
						}
					}
				}
			}
			row++;
		}

		int endRow = startRow + srList.size() - 1;
		int rowMax = String.valueOf(endRow).length();
		if (rowMax < 3) {
			rowMax = 3;
		}
		if (isShowHeader) {
			if (isStructType) {
				printHeaders(maxLenList, struct, rowMax);
			} else {
				printHeaders(maxLenList, srList.get(0), rowMax);
			}
		}

		row = startRow;
		for (Object element : srList) {
			StringBuffer printStr = new StringBuffer();
			if (isStructType) {
				struct = (Struct) element;
				printStr.append(StringUtil.getRightPaddedString(row + "",
						rowMax, ' '));
				printStr.append("  ");
				String structStr = printStruct(maxLenList, struct);
				printStr.append(structStr);
			} else {
				printStr.append(StringUtil.getRightPaddedString(row + "",
						rowMax, ' '));
				printStr.append("  ");
				String printObjStr = printObject(maxLenList, element, false);
				printStr.append(printObjStr);
			}
			PadoShell.println(printStr.toString());
			row++;
		}
		PadoShell.println("");
		for (Object elementClassName : elementNameSet) {
			PadoShell.println("Class: " + elementClassName);
		}
		return srList.size();
	}

	/**
	 * Prints the SelectResults contents up to the specified rowCount.
	 * 
	 * @param sr
	 * @param startRowNum
	 * @param rowCount
	 * @return The number of rows printed
	 */
	public static int printSelectResults(SelectResults sr, int startIndex,
			int startRowNum, int rowCount) {
		if (isTableFormat() == false) {
			return SimplePrintUtil.printSelectResults(sr, startIndex,
					startRowNum, rowCount);
		}

		if (sr == null) {
			PadoShell.printlnError("SelectResults is null");
			return 0;
		}

		int endIndex = startIndex + rowCount; // exclusive
		if (endIndex >= sr.size()) {
			endIndex = sr.size();
		}

		CollectionType type = sr.getCollectionType();
		ObjectType elementType = type.getElementType();
		int row = 1;
		if (rowCount == -1) {
			rowCount = sr.size();
		}

		HashSet elementNameSet = new HashSet();
		ArrayList maxLenList = new ArrayList();
		Object element = null;
		boolean isStructType = false;
		StructType structType = null;
		Struct struct = null;
		List srList = sr.asList();

		for (int i = startIndex; i < endIndex; i++) {
			element = srList.get(i);
			if (elementType.isStructType()) {
				structType = (StructType) elementType;
				struct = (Struct) element;
				computeMaxLengths(maxLenList, struct);
				isStructType = true;
			} else {
				computeMaxLengths(maxLenList, element, false);
				if (element != null) {
					elementNameSet.add(element.getClass().getName());
					if  (element instanceof KeyMap) {
						elementNameSet.add(((KeyMap)element).getKeyTypeName());
					}
				}
			}
			row++;
		}

		if (element == null && struct == null) {
			return 0;
		}

		int rowMax = String.valueOf(startRowNum + rowCount - 1).length();
		if (rowMax < 3) {
			rowMax = 3;
		}
		if (isStructType) {
			printHeaders(maxLenList, struct, rowMax);
		} else {
			printHeaders(maxLenList, element, rowMax);
		}

		row = startRowNum;
		for (int i = startIndex; i < endIndex; i++) {
			element = srList.get(i);
			StringBuffer printStr = new StringBuffer();
			if (elementType.isStructType()) {
				structType = (StructType) elementType;
				struct = (Struct) element;
				printStr.append(StringUtil.getRightPaddedString(row + "",
						rowMax, ' '));
				printStr.append("  ");
				String structStr = printStruct(maxLenList, struct);
				printStr.append(structStr);
			} else {
				printStr.append(StringUtil.getRightPaddedString(row + "",
						rowMax, ' '));
				printStr.append("  ");
				String printObjStr = printObject(maxLenList, element, false);
				printStr.append(printObjStr);
			}
			PadoShell.println(printStr.toString());
			row++;
		}
		PadoShell.println("");
		for (Object elementClassName : elementNameSet) {
			PadoShell.println("Class: " + elementClassName);
		}
		return endIndex - startIndex;
	}

	private static int printSelectResults_iterator(SelectResults sr,
			int startRowNum, int rowCount) {
		if (sr == null) {
			PadoShell.println("SelectResults is null");
			return 0;
		}

		CollectionType type = sr.getCollectionType();
		ObjectType elementType = type.getElementType();
		int row = 1;
		if (rowCount == -1) {
			rowCount = sr.size();
		}

		HashSet elementNameSet = new HashSet();
		ArrayList maxLenList = new ArrayList();
		Object element = null;
		boolean isStructType = false;
		StructType structType = null;
		Struct struct = null;
		for (Iterator iter = sr.iterator(); iter.hasNext() && row <= rowCount;) {
			element = iter.next();
			if (elementType.isStructType()) {
				structType = (StructType) elementType;
				struct = (Struct) element;
				computeMaxLengths(maxLenList, struct);
				isStructType = true;
			} else {
				computeMaxLengths(maxLenList, element, false);
				elementNameSet.add(element.getClass().getName());
				if (element instanceof KeyMap) {
					elementNameSet.add(((KeyMap)element).getKeyTypeName());
				}
			}
			row++;
		}

		if (element == null && struct == null) {
			return 0;
		}

		int rowMax = String.valueOf(startRowNum + rowCount - 1).length();
		if (rowMax < 3) {
			rowMax = 3;
		}
		if (isStructType) {
			printHeaders(maxLenList, struct, rowMax);
		} else {
			printHeaders(maxLenList, element, rowMax);
		}

		row = startRowNum;
		int lastRow = startRowNum + rowCount - 1;
		for (Iterator iter = sr.iterator(); iter.hasNext() && row <= lastRow;) {
			element = iter.next();
			StringBuffer printStr = new StringBuffer();
			if (elementType.isStructType()) {
				structType = (StructType) elementType;
				struct = (Struct) element;
				printStr.append(StringUtil.getRightPaddedString(row + "",
						rowMax, ' '));
				printStr.append("  ");
				String structStr = printStruct(maxLenList, struct);
				printStr.append(structStr);
			} else {
				printStr.append(StringUtil.getRightPaddedString(row + "",
						rowMax, ' '));
				printStr.append("  ");
				String printObjStr = printObject(maxLenList, element, false);
				printStr.append(printObjStr);
			}
			PadoShell.println(printStr.toString());
			row++;
		}
		PadoShell.println("");
		for (Object elementClassName : elementNameSet) {
			PadoShell.println("Class: " + elementClassName);
		}
		return row - 1;
	}

	private static void computeMaxLengths(List list,
			Struct struct) {
		StructType structType = struct.getStructType();
		ObjectType[] fieldTypes = structType.getFieldTypes();
		String[] fieldNames = structType.getFieldNames();
		Object[] fieldValues = struct.getFieldValues();

		int listIndex = 0;
		for (int i = 0; i < fieldTypes.length; i++) {
			ObjectType fieldType = fieldTypes[i];
			String fieldName = fieldNames[i];
			Object fieldValue = fieldValues[i];

			Integer len;
			if (listIndex >= list.size()) {
				len = fieldName.length();
				list.add(len);
			} else {
				len = (Integer) list.get(listIndex);
			}
			if (fieldValue == null) {
				if (len.intValue() < 4) {
					len = 4;
				}
			} else {
				int valueLen = fieldValue.toString().length();
				if (len.intValue() < valueLen) {
					len = valueLen;
				}
			}
			list.set(listIndex, len);
			listIndex++;
		}
	}

	private static void computeMaxLengths(List list, Object obj, boolean isKey) {
		computeMaxLengths(list, obj, isKey, 3, 5);
	}

	private static void computeMaxLengths(List list, Object obj, boolean isKey,
			int keyMin, int valueMin) {
		int indexToCompare = 0;
		computeMaxLengths(list, obj, isKey, keyMin, valueMin, indexToCompare);
	}

	private static void computeMaxLengths(List list, Object obj, boolean isKey,
			int keyMin, int valueMin, int indexToCompare) {
		Object object = obj;
		if (obj == null) {
			object = "null";
		}
		if (object instanceof String || object.getClass().isPrimitive()
				|| object.getClass() == Boolean.class
				|| object.getClass() == Byte.class
				|| object.getClass() == Character.class
				|| object.getClass() == Short.class
				|| object.getClass() == Integer.class
				|| object.getClass() == Long.class
				|| object.getClass() == Float.class
				|| object.getClass() == Double.class
				|| object.getClass().isArray() || object instanceof Date) {
			if (list.size() > indexToCompare) {
				int len = (Integer) list.get(indexToCompare);
				if (len < object.toString().length()) {
					list.set(indexToCompare, object.toString().length());
				}
			} else {
				if (isKey) {
					if (object.toString().length() < keyMin) { // Key
						list.add(indexToCompare, keyMin);
					} else {
						list.add(indexToCompare, object.toString().length());
					}
				} else {
					if (object.toString().length() < valueMin) { // Value
						list.add(indexToCompare, valueMin);
					} else {
						list.add(indexToCompare, object.toString().length());
					}
				}
			}

		} else if (object instanceof Map) {

			// Map
			Map map = (Map) object;
			Set set = map.keySet();
			if (set != null) {
				TreeSet keySet = new TreeSet(set);
				int listIndex = indexToCompare;
				for (Object name : keySet) {
					Object value = map.get(name);
					value = getPrintableValue(value);
					Integer len;
					if (listIndex >= list.size()) {
						len = name.toString().length();
						list.add(len);
					} else {
						len = (Integer) list.get(listIndex);
					}
					if (value == null) {
						if (len.intValue() < 4) {
							len = 4;
						}
					} else {
						int valueLen = value.toString().length();
						if (len.intValue() < valueLen) {
							len = valueLen;
						}
					}
					list.set(listIndex, len);
					listIndex++;
				}
			}

		} else {

			Class cls = object.getClass();
			Method methods[] = cls.getMethods();
			Method method;
			Class retType;
			String name;
			Object value;
			int listIndex = indexToCompare;
			for (int i = 0; i < methods.length; i++) {
				method = methods[i];
				name = method.getName();
				if (name.length() <= 3 || name.startsWith("get") == false
						|| name.equals("getClass")) {
					continue;
				}
				retType = method.getReturnType();
				if (retType == Void.TYPE) {
					continue;
				}
				try {
					value = method.invoke(object, null);
					value = getPrintableValue(value);
					Integer len;
					if (listIndex >= list.size()) {
						len = name.length() - 3;
						list.add(len);
					} else {
						len = (Integer) list.get(listIndex);
					}
					if (value == null) {
						if (len.intValue() < 4) {
							len = 4;
						}
					} else {
						int valueLen = value.toString().length();
						if (len.intValue() < valueLen) {
							len = valueLen;
						}
					}
					list.set(listIndex, len);
					listIndex++;
				} catch (Exception ex) {
				}
			}
		}
	}

	private static void printHeaders(List list,
			Struct struct, int rowMaxLen) {
		StringBuffer printStr = new StringBuffer();
		printStr.append(StringUtil.getRightPaddedString("Row", rowMaxLen, ' '));
		printStr.append("  ");

		StructType structType = struct.getStructType();
		ObjectType[] fieldTypes = structType.getFieldTypes();
		String[] fieldNames = structType.getFieldNames();
		Object[] fieldValues = struct.getFieldValues();

		int listIndex = 0;
		for (int i = 0; i < fieldTypes.length; i++) {
			ObjectType fieldType = fieldTypes[i];
			String fieldName = fieldNames[i];
			Object fieldValue = fieldValues[i];
			fieldValue = getPrintableValue(fieldValue);
			int maxLen = (Integer) list.get(listIndex);
			String header = fieldName;
			printStr.append(StringUtil.getRightPaddedString(header, maxLen,
					' '));
			printStr.append("  ");
			listIndex++;
		}
		PadoShell.println(printStr.toString());
		printStr = new StringBuffer();
		printStr.append(StringUtil.getRightPaddedString("", rowMaxLen, '-'));
		printStr.append("  ");
		listIndex = 0;
		for (int i = 0; i < fieldTypes.length; i++) {
			ObjectType fieldType = fieldTypes[i];
			String fieldName = fieldNames[i];
			Object fieldValue = fieldValues[i];
			fieldValue = getPrintableValue(fieldValue);
			int maxLen = (Integer) list.get(listIndex);
			String header = fieldName;
			printStr.append(StringUtil.getRightPaddedString(
					StringUtil.getRightPaddedString("", header.length(), '-'),
					maxLen, ' '));
			printStr.append("  ");
			listIndex++;
		}
		PadoShell.println(printStr.toString());
	}

	private static void printHeaders(List list, Object object, int rowMaxLen) {
		StringBuffer printStr = new StringBuffer();
		printStr.append(StringUtil.getRightPaddedString("Row", rowMaxLen, ' '));
		printStr.append("  ");
		String topHeaderStr = printTopHeaders(list, object, false, "Value");
		printStr.append(topHeaderStr);
		PadoShell.println(printStr.toString());
		printStr = new StringBuffer();

		printStr.append(StringUtil.getRightPaddedString("", rowMaxLen, '-'));
		printStr.append("  ");
		String bottomHeaderStr = printBottomHeaders(list, object, false, "Value");
		printStr.append(bottomHeaderStr);
		PadoShell.println(printStr.toString());
	}

	private static String printStruct(List list,
			Struct struct) {
		StringBuffer printStr = new StringBuffer();
		StructType structType = struct.getStructType();
		ObjectType[] fieldTypes = structType.getFieldTypes();
		String[] fieldNames = structType.getFieldNames();
		Object[] fieldValues = struct.getFieldValues();

		int listIndex = 0;
		for (int i = 0; i < fieldTypes.length; i++) {
			ObjectType fieldType = fieldTypes[i];
			String fieldName = fieldNames[i];
			Object fieldValue = fieldValues[i];
			int maxLen = (Integer) list.get(listIndex);
			if (fieldValue instanceof TemporalData) {
				((TemporalData)fieldValue).__getTemporalValue().deserializeAll();
			}
			if (listIndex == list.size() - 1) {
				if (fieldValue == null) {
					printStr.append("null");
				} else {
					printStr.append(fieldValue.toString());
				}
			} else {
				if (fieldValue == null) {
					printStr.append(StringUtil.getRightPaddedString("null",
							maxLen, ' '));
				} else {
					printStr.append(StringUtil.getRightPaddedString(
							fieldValue.toString(), maxLen, ' '));
				}
				printStr.append("  ");
			}
			listIndex++;
		}
		return printStr.toString();
	}

	private static String printObject(List list, Object obj,
			boolean printLastColumnSpaces) {
		return printObject(list, obj, printLastColumnSpaces, 0);
	}

	private static String printObject(List list, Object obj,
			boolean printLastColumnSpaces, int printColumnIndex) {
		StringBuffer printStr = new StringBuffer();
		boolean enableLeftPadding = false;
		Object object = obj;
		if (object == null) {
			object = "null";
		}

		if (object.getClass() == Short.class
				|| object.getClass() == Integer.class
				|| object.getClass() == Long.class
				|| object.getClass() == Float.class
				|| object.getClass() == Double.class) {
			printLastColumnSpaces = true;
			enableLeftPadding = true;
		}

		if (object instanceof String || object.getClass().isPrimitive()
				|| object.getClass() == Boolean.class
				|| object.getClass() == Byte.class
				|| object.getClass() == Character.class
				|| object.getClass() == Short.class
				|| object.getClass() == Integer.class
				|| object.getClass() == Long.class
				|| object.getClass() == Float.class
				|| object.getClass() == Double.class
				|| object.getClass().isArray() || object instanceof Date) {

			object = getPrintableValue(object);
			if (list.size() > 0) {
				int maxLen = (Integer) list.get(printColumnIndex);
				if (printLastColumnSpaces) {
					if (enableLeftPadding) {
						printStr.append(StringUtil.getLeftPaddedString(
								object.toString(), maxLen, ' '));
					} else {
						printStr.append(StringUtil.getRightPaddedString(
								object.toString(), maxLen, ' '));
					}
				} else {
					printStr.append(object.toString());
				}
			} else {
				printStr.append(object.toString());
			}

		} else if (object instanceof Map) {

			Map map = (Map) object;
			Set set = map.keySet();
			if (set != null) {
				TreeSet keySet = new TreeSet(set);
				int listIndex = printColumnIndex;
				for (Object key : keySet) {
					Object value = map.get(key);
					value = getPrintableValue(value);

					int maxLen = (Integer) list.get(listIndex);
					if (listIndex == list.size() - 1) {
						if (value == null) {
							if (printLastColumnSpaces) {
								printStr.append(StringUtil
										.getRightPaddedString("null", maxLen,
												' '));
							} else {
								printStr.append("null");
							}
						} else {
							if (printLastColumnSpaces) {
								printStr.append(StringUtil
										.getRightPaddedString(value.toString(),
												maxLen, ' '));
							} else {
								printStr.append(value.toString());
							}
						}
					} 
					else {
						if (value == null) {
							printStr.append(StringUtil.getRightPaddedString(
									"null", maxLen, ' '));
						} else {
							printStr.append(StringUtil.getRightPaddedString(
									value.toString(), maxLen, ' '));
						}
						printStr.append("  ");
					}
					
					listIndex++;
				}
			}

		} else {
			Class cls = object.getClass();
			Method methods[] = cls.getMethods();
			Method method;
			Class retType;
			String name;
			Object value;
			ArrayList<String> methodList = new ArrayList();
			HashMap<String, Method> methodMap = new HashMap();
			int listIndex = printColumnIndex;
			for (int i = 0; i < methods.length; i++) {
				method = methods[i];
				name = method.getName();
				if (name.length() <= 3 || name.startsWith("get") == false
						|| name.equals("getClass")) {
					continue;
				}
				retType = method.getReturnType();
				if (retType == Void.TYPE) {
					continue;
				}
				try {
					value = method.invoke(object, null);
					value = getPrintableValue(value);

					int maxLen = (Integer) list.get(listIndex);
					if (listIndex == list.size() - 1) {
						if (value == null) {
							if (printLastColumnSpaces) {
								printStr.append(StringUtil
										.getRightPaddedString("null", maxLen,
												' '));
							} else {
								printStr.append("null");
							}
						} else {
							if (printLastColumnSpaces) {
								printStr.append(StringUtil
										.getRightPaddedString(value.toString(),
												maxLen, ' '));
							} else {
								printStr.append(value.toString());
							}
						}

					} else {
						if (value == null) {
							printStr.append(StringUtil.getRightPaddedString(
									"null", maxLen, ' '));
						} else {
							printStr.append(StringUtil.getRightPaddedString(
									value.toString(), maxLen, ' '));
						}
						printStr.append("  ");
					}

					listIndex++;
				} catch (Exception ex) {
				}
			}
		}
		return printStr.toString();
	}

	public static void printList(List<List<Object>> resultList,
			List<String> headerList,int resultIndex) {
	
		if (isTableFormat() == false) {
			SimplePrintUtil.printList(resultList, headerList);
		} else {
			ArrayList maxLenList = new ArrayList();
						
			// Calculate the max length for each field to print.
			for (List<Object> resultArr : resultList) {
				for (int i = 0; i < resultArr.size(); i++) {
					Object object = resultArr.get(i);
					if (object != null)
						computeMaxLengths(maxLenList, object.toString(), true,
								headerList.get(i).length(), 0, i);
					else
						computeMaxLengths(maxLenList, object, true, headerList
								.get(i).length(), 0, i);
				}
			}
			// Print Header
			printHeaders(headerList, maxLenList,resultIndex);
			// Print Values
			for (List<Object> resultArr : resultList) {
				StringBuffer printStr = new StringBuffer();
				for (int i = 0; i < resultArr.size(); i++) {
					Object object = resultArr.get(i);
					String ObjStr = "";
					if (object != null) {
						ObjStr = printObject(maxLenList, object.toString(), true, i);
					} else {
						ObjStr = printObject(maxLenList, object, true, i);
					}
					printStr.append(ObjStr);
					
					if(i==(resultIndex)) {
						printStr.append(" | ");
					} else {
						printStr.append("   ");
					}
				}
				PadoShell.println(printStr.toString());
			}
		}
	}
	
	public static void printList(List resultList) {
		printList(resultList, null);
	}

	public static void printList(List resultList, List<String>headerList) {
		ArrayList maxLenList = new ArrayList();
		Object nonNullObject = null;
		for (int i = 0; i < resultList.size(); i++) {
			Object object = resultList.get(i);
			if (object != null) {
				nonNullObject = object;
			}
			computeMaxLengths(maxLenList, object, true); // TODO: true?
		}
		if (nonNullObject == null) {
			return;
		}

		int rowMax = String.valueOf(resultList.size()).length();
		if (rowMax < 3) {
			rowMax = 3;
		}
		if (headerList == null) {
			printHeaders(maxLenList, nonNullObject, rowMax);
		} else {
			
		}
		for (int i = 0; i < resultList.size(); i++) {
			StringBuffer printStr = new StringBuffer();
			Object object = resultList.get(i);
			printStr.append(StringUtil.getRightPaddedString((i + 1) + "",
					rowMax, ' '));
			printStr.append("  ");
			String printObjStr = printObject(maxLenList, object, false);
			printStr.append(printObjStr);
			PadoShell.println(printStr.toString());
		}
	}

	public static int printList(List list, int startIndex, int startRowNum,
			int rowCount, int actualSize, List keyList) throws Exception {
		if (list == null || list.size() == 0) {
			return 0;
		}

		if (isTableFormat() == false) {
			return SimplePrintUtil.printList(list, startIndex, startRowNum,
					rowCount, actualSize);
		}

		// Determine max column lengths
		HashSet objectNameSet = new HashSet();
		ArrayList maxLenList = new ArrayList();
		int count = 0;
		Object object = null;
		for (Iterator itr = list.iterator(); count < rowCount && itr.hasNext(); count++) {
			object = itr.next();
			computeMaxLengths(maxLenList, object, true);
			objectNameSet.add(object.getClass().getName());
		}

		if (object == null) {
			return 0;
		}

		// Print headers including row column
		int rowMax = String.valueOf(startRowNum + rowCount - 1).length();
		if (rowMax < 3) {
			rowMax = 3;
		}
		printHeaders(maxLenList, object, rowMax);

		// Print keys and values
		// int row = 1;
		count = 0;
		int row = startRowNum;
		int lastRow = startRowNum + rowCount - 1;
		for (Iterator itr = list.iterator(); count < rowCount && itr.hasNext(); count++) {
			StringBuffer printStr = new StringBuffer();
			printStr.append(StringUtil.getRightPaddedString(row + "", rowMax,
					' '));
			printStr.append("  ");

			object = itr.next();
			if (keyList != null) {
				keyList.add(object);
			}
			String printObjStr = printObject(maxLenList, object, true);
			printStr.append(printObjStr);
			PadoShell.println(printStr.toString());
			row++;
		}
		PadoShell.println("");
		PadoShell.println(" Fetch size: " + rowCount);
		PadoShell.println("   Returned: " + (row - 1) + "/" + actualSize);
		for (Object keyName : objectNameSet) {
			PadoShell.println("      Class: " + keyName);
		}
		return count;
	}

	private static Object getPrintableValue(Object value) {
		if (value instanceof Byte) {
			value = ((Byte) value).toString();
		} else if (value instanceof byte[]) {
			value = "[B " + ((byte[]) value).length;
		} else if (value instanceof boolean[]) {
			value = "[Z " + ((boolean[]) value).length;
		} else if (value instanceof short[]) {
			value = "[S " + ((short[]) value).length;
		} else if (value instanceof int[]) {
			value = "[I " + ((int[]) value).length;
		} else if (value instanceof long[]) {
			value = "[J " + ((long[]) value).length;
		} else if (value instanceof float[]) {
			value = "[F " + ((float[]) value).length;
		} else if (value instanceof double[]) {
			value = "[D " + ((double[]) value).length;
		}
		return value;
	}

	private static LinkedHashMap sortHashMapByValues(HashMap passedMap) {
		List mapKeys = new ArrayList(passedMap.keySet());
		List mapValues = new ArrayList(passedMap.values());
		Collections.sort(mapValues, Collections.reverseOrder());
		Collections.sort(mapKeys);

		LinkedHashMap sortedMap = new LinkedHashMap();

		Iterator valueIt = mapValues.iterator();

		while (valueIt.hasNext()) {
			Object val = valueIt.next();
			Iterator keyIt = mapKeys.iterator();
			while (keyIt.hasNext()) {
				Object key = keyIt.next();
				if ((passedMap.get(key) == null && val == null)
						|| (val != null && val.equals(passedMap.get(key)))) {
					passedMap.remove(key);
					mapKeys.remove(key);
					sortedMap.put(key, val);
					break;
				}
			}
		}
		return sortedMap;
	}

}
