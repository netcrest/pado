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

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.gemstone.gemfire.cache.query.Struct;
import com.netcrest.pado.biz.file.SchemaInfo;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalData;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class OutputUtil
{
	public static final int TYPE_KEYS = 0;
	public static final int TYPE_VALUES = 1;
	public static final int TYPE_KEYS_VALUES = 2;

	private static ResultMetaData getMetaData(Object key, Object value)
	{
		// Get all getters
		Method keyGetters[] = null;
		Method valueGetters[] = null;
		ITemporalKey temporalKey = null;
		ITemporalData temporalData = null;
		KeyType keyType = null;
		List keyList = null;

		if (key != null) {
			if (key instanceof ITemporalKey) {
				temporalKey = (ITemporalKey) key;
				Object identityKey = temporalKey.getIdentityKey();
				keyGetters = ReflectionUtil.getAllGetters(identityKey.getClass());
			} else {
				keyGetters = ReflectionUtil.getAllGetters(key.getClass());
			}
		}
		if (value == null) {
			valueGetters = new Method[0];
		} else if (value instanceof ITemporalData) {
			temporalData = (ITemporalData) value;
			if (temporalData instanceof TemporalData) {
				TemporalData td = (TemporalData) temporalData;
				value = td.getValue();
				if (value instanceof KeyMap) {
					KeyMap keyMap = (KeyMap) value;
					keyType = KeyTypeManager.getLatestKeyTypeVersion(keyMap.getKeyType());
					Set keySet;
					if (keyType == null) {
						Map valueMap = (Map) value;
						keySet = valueMap.keySet();
					} else {
						keySet = keyType.getNameSet();
					}
					keyList = new ArrayList(keySet);
					Collections.sort(keyList);
				}
			} else if (temporalData instanceof KeyMap) {
				KeyMap keyMap = (KeyMap) temporalData;
				keyType = KeyTypeManager.getLatestKeyTypeVersion(keyMap.getKeyType());
				Set keySet;
				if (keyType == null) {
					Map valueMap = (Map) value;
					keySet = valueMap.keySet();
				} else {
					keySet = keyType.getNameSet();
				}
				keyList = new ArrayList(keySet);
				Collections.sort(keyList);
			} else {
				valueGetters = ReflectionUtil.getAllGetters(value.getClass());
			}
		} else if (value instanceof Map) {
			Set keySet;
			if (value instanceof KeyMap) {
				KeyMap keyMap = (KeyMap) value;
				keyType = KeyTypeManager.getLatestKeyTypeVersion(keyMap.getKeyType());
				if (keyType != null) {
					keySet = keyType.getNameSet();
				} else {
					keySet = keyMap.keySet();
				}
			} else {
				Map valueMap = (Map) value;
				keySet = valueMap.keySet();
			}
			keyList = new ArrayList(keySet);
			Collections.sort(keyList);
		} else {
			valueGetters = ReflectionUtil.getAllGetters(value.getClass());
		}

		ResultMetaData rmd = new ResultMetaData();

		rmd.keyGetters = keyGetters;
		rmd.keyList = keyList;
		rmd.keyType = keyType;
		rmd.temporalData = temporalData;
		rmd.temporalKey = temporalKey;
		rmd.valueGetters = valueGetters;

		return rmd;
	}

	private static class ResultMetaData
	{
		Method keyGetters[] = null;
		Method valueGetters[] = null;
		ITemporalKey temporalKey = null;
		ITemporalData temporalData = null;
		KeyType keyType = null;
		List keyList = null;
	}

	/**
	 * IMPORTANT: Currently supports only Map objects in the specified map.
	 * @param writer
	 * @param map
	 * @param fieldTerminator
	 * @param printType
	 * @param dateFormat
	 * @param isPrintColumnHeader
	 */
	public static void printEntries(PrintWriter writer, Map map, String fieldTerminator, int printType,
			SimpleDateFormat dateFormat, boolean isPrintColumnHeader)
	{
		if (map == null) {
			return;
		}

		// Get all getters
		Set<Map.Entry> entrySet = map.entrySet();
		Object key = null;
		Object value = null;
		ResultMetaData rmd = null;
		for (Entry entry : entrySet) {
			key = entry.getKey();
			value = entry.getValue();
			rmd = getMetaData(key, value);
			break;
		}

		// Empty map
		if (rmd == null) {
			return;
		}

		switch (printType) {
		case TYPE_KEYS:
			// Print keys
			if (isPrintColumnHeader) {
				printCsvHeader(writer, key, rmd.keyGetters, rmd.keyList, fieldTerminator, "Key");
//				writer.print(fieldTerminator);
				writer.println();
			}
			if (rmd.temporalKey == null) {
				for (Entry entry : entrySet) {
					key = entry.getKey();
					printObject(writer, rmd.keyGetters, key, fieldTerminator, dateFormat);
					writer.println();
				}
			} else {
				for (Entry entry : entrySet) {
					rmd.temporalKey = (ITemporalKey) entry.getKey();
					printTemporalKey(writer, rmd.keyGetters, rmd.temporalKey, fieldTerminator, dateFormat);
					writer.println();
				}
			}

			break;
		case TYPE_VALUES:
			// Print values
			if (isPrintColumnHeader) {
				printCsvHeader(writer, value, rmd.valueGetters, rmd.keyList, fieldTerminator, "Value");
				writer.println();
			}
			if (value instanceof ITemporalData) {
				for (Entry entry : entrySet) {
					rmd.temporalData = (ITemporalData) entry.getValue();
					printTemporalData(writer, rmd.keyType, rmd.keyList, rmd.valueGetters, rmd.temporalData,
							fieldTerminator, dateFormat);
					writer.println();
				}
			} else if (rmd.keyList != null) {
				for (Entry entry : entrySet) {
					value = entry.getValue();
					// TODO: Support objects other than Map.
					printMap(writer, rmd.keyList, (Map) value, fieldTerminator, dateFormat);
					writer.println();
				}
			} else if (rmd.temporalKey == null) {
				for (Entry entry : entrySet) {
					value = entry.getValue();
					printObject(writer, rmd.valueGetters, value, fieldTerminator, dateFormat);
					writer.println();
				}
			}
			break;
		case TYPE_KEYS_VALUES:
		default:
			// Print keys and values
			// key (identity key) header
			if (isPrintColumnHeader) {
				printCsvHeader(writer, key, rmd.keyGetters, rmd.keyList, fieldTerminator, "Key");
				writer.print(fieldTerminator);
				printCsvHeader(writer, value, rmd.valueGetters, rmd.keyList, fieldTerminator, "Value");
				writer.println();
			}
			if (rmd.keyList != null && rmd.temporalKey == null) {
				for (Entry entry : entrySet) {
					key = entry.getKey();
					value = entry.getValue();
					printObject(writer, rmd.keyGetters, key, fieldTerminator, dateFormat);
					writer.print(fieldTerminator);
					printMap(writer, rmd.keyList, (Map) value, fieldTerminator, dateFormat);
					writer.println();
				}
			} else if (rmd.temporalKey == null) {
				for (Entry entry : entrySet) {
					key = entry.getKey();
					value = entry.getValue();
					printObject(writer, rmd.keyGetters, key, fieldTerminator, dateFormat);
					writer.print(fieldTerminator);
					printObject(writer, rmd.valueGetters, value, fieldTerminator, dateFormat);
					writer.println();
				}
			} else {
				for (Entry entry : entrySet) {
					rmd.temporalKey = (ITemporalKey) entry.getKey();
					rmd.temporalData = (ITemporalData) entry.getValue();
					printTemporalKey(writer, rmd.keyGetters, rmd.temporalKey, fieldTerminator, dateFormat);
					writer.print(fieldTerminator);
					printTemporalData(writer, rmd.keyType, rmd.keyList, rmd.valueGetters, rmd.temporalData,
							fieldTerminator, dateFormat);
					writer.println();
				}
			}
			break;
		}
	}

	/**
	 * Prints the contents of the specified list to the specified writer.
	 * IMPORTANT: Currently supports only Map objects.
	 * 
	 * @param writer
	 * @param list
	 * @param fieldTerminator
	 * @param printType
	 *            If KEYS_VALUES then the specified list must contain keys and
	 *            values in GemFire Struct form. Otherwise, the list must contain
	 *            non-Struct types.
	 * @param dateFormat
	 * @param isPrintColumnHeader
	 */
	public static void printList(PrintWriter writer, List list, String fieldTerminator, int printType,
			SimpleDateFormat dateFormat, boolean isPrintColumnHeader)
	{
		if (list == null || list.size() == 0) {
			return;
		}
		ResultMetaData rmd = null;

		boolean isStructType = list.size() > 0 && list.get(0) instanceof Struct;

		switch (printType) {
		case TYPE_KEYS:
			// Print keys
			if (isStructType == false) {
				Object key = list.get(0);
				rmd = getMetaData(key, null);
				if (isPrintColumnHeader) {
					printCsvHeader(writer, key, rmd.keyGetters, rmd.keyList, fieldTerminator, "Key");
//					writer.print(fieldTerminator);
					writer.println();
				}
				if (rmd.temporalKey == null) {
					for (Object key2 : list) {
						printObject(writer, rmd.keyGetters, key2, fieldTerminator, dateFormat);
						writer.println();
					}
				} else {
					for (Object key2 : list) {
						ITemporalKey temporalKey = (ITemporalKey) key2;
						printTemporalKey(writer, rmd.keyGetters, temporalKey, fieldTerminator, dateFormat);
						writer.println();
					}
				}
			}

			break;
		case TYPE_VALUES:
			// Print values
			if (isStructType == false) {
				Object value = list.get(0);
				rmd = getMetaData(null, value);
				if (isPrintColumnHeader) {
					printCsvHeader(writer, value, rmd.valueGetters, rmd.keyList, fieldTerminator, "Value");
					writer.println();
				}
				if (value instanceof ITemporalData) {
					for (Object value2 : list) {
						ITemporalData temporalData = (ITemporalData) value2;
						printTemporalData(writer, rmd.keyType, rmd.keyList, rmd.valueGetters, temporalData,
								fieldTerminator, dateFormat);
						writer.println();
					}
				} else if (rmd.keyList != null) {
					for (Object value2 : list) {
						// TODO: Support other than Map objects
						printMap(writer, rmd.keyList, (Map) value2, fieldTerminator, dateFormat);
						writer.println();
					}
				} else if (rmd.temporalKey == null) {
					for (Object value2 : list) {
						printObject(writer, rmd.valueGetters, value2, fieldTerminator, dateFormat);
						writer.println();
					}
				}
			}
			break;
		case TYPE_KEYS_VALUES:
			if (isStructType) {
				Struct struct = (Struct) list.get(0);
				Object key = struct.getFieldValues()[0];
				Object value = struct.getFieldValues()[1];
				rmd = getMetaData(key, value);

				// Print keys and values
				// key (identity key) header
				if (isPrintColumnHeader) {
					printCsvHeader(writer, key, rmd.keyGetters, rmd.keyList, fieldTerminator, "Key");
					writer.print(fieldTerminator);
					printCsvHeader(writer, value, rmd.valueGetters, rmd.keyList, fieldTerminator, "Value");
					writer.println();
				}
				if (rmd.keyList != null && rmd.temporalKey == null) {
					for (Object object : list) {
						struct = (Struct) object;
						key = struct.getFieldValues()[0];
						value = struct.getFieldValues()[1];
						printObject(writer, rmd.keyGetters, key, fieldTerminator, dateFormat);
						writer.print(fieldTerminator);
						printMap(writer, rmd.keyList, (Map) value, fieldTerminator, dateFormat);
						writer.println();
					}
				} else if (rmd.temporalKey == null) {
					for (Object object : list) {
						struct = (Struct) object;
						key = struct.getFieldValues()[0];
						value = struct.getFieldValues()[1];
						printObject(writer, rmd.keyGetters, key, fieldTerminator, dateFormat);
						writer.print(fieldTerminator);
						printObject(writer, rmd.valueGetters, value, fieldTerminator, dateFormat);
						writer.println();
					}
				} else {
					for (Object object : list) {
						struct = (Struct) object;
						ITemporalKey temporalKey = (ITemporalKey) struct.getFieldValues()[0];
						ITemporalData temporalData = (ITemporalData) struct.getFieldValues()[1];
						printTemporalKey(writer, rmd.keyGetters, temporalKey, fieldTerminator, dateFormat);
						writer.print(fieldTerminator);
						printTemporalData(writer, rmd.keyType, rmd.keyList, rmd.valueGetters, temporalData,
								fieldTerminator, dateFormat);
						writer.println();
					}
				}
			}

			break;
		}
	}

	public static void printScrollableResultSet(PrintWriter writer, IScrollableResultSet srs, String fieldTerminator,
			int printType, SimpleDateFormat dateFormat)
	{
		printList(writer, srs.toList(), fieldTerminator, printType, dateFormat, true);
		if (srs.nextSet()) {
			do {
				printList(writer, srs.toList(), fieldTerminator, printType, dateFormat, false);
			} while (srs.nextSet());
		}
	}

	public static void printSchema(PrintWriter writer, String gridPath, Object key, Object value, List keyList,
			int printType, String fieldTerminator, SimpleDateFormat dateFormat, boolean isCsvFileHeader,
			boolean printHeader)
	{
		Object keyObject = null;
		Method[] keyGetters = null;
		Method[] valueGetters = null;
		ITemporalKey temporalKey = null;
		ITemporalData temporalData = null;
		if (key != null) {
			if (key instanceof ITemporalKey) {
				temporalKey = (ITemporalKey) key;
				keyObject = temporalKey.getIdentityKey();
			} else {
				keyObject = key;
			}

			if (printHeader) {
				writer.println(SchemaInfo.PROP_GRID_PATH + "=" + gridPath);
				if (printType == TYPE_KEYS_VALUES) {
					writer.println(SchemaInfo.PROP_IS_KEY_COLUMNS + "=true");
				}
				writer.println(SchemaInfo.PROP_KEY_CLASS + "=" + keyObject.getClass().getName());
				if (value instanceof ITemporalData) {
					temporalData = (ITemporalData) value;
					TemporalData td = (TemporalData) temporalData;
					value = td.getValue();
				}
				writer.println(SchemaInfo.PROP_VALUE_CLASS + "=" + value.getClass().getName());
				KeyType keyType = null;
				if (value instanceof KeyMap) {
					KeyMap keyMap = (KeyMap) value;
					keyType = KeyTypeManager.getLatestKeyTypeVersion(keyMap.getKeyType());
				}
				if (keyType != null) {
					writer.println(SchemaInfo.PROP_KEY_TYPE_CLASS + "="
							+ KeyTypeManager.getLatestKeyTypeVersion(keyType).getClass().getName());
				}
				writer.println(SchemaInfo.PROP_DELIMITER + "=" + fieldTerminator);
				writer.println(SchemaInfo.PROP_FILE_LOADER_CLASS + "=com.netcrest.pado.biz.file.CsvFileLoader");
				writer.println(SchemaInfo.PROP_BATCH_SIZE + "=5000");
				writer.println(SchemaInfo.PROP_DATE_FORMAT + "=" + dateFormat.toPattern());
				writer.println(SchemaInfo.PROP_IS_CASE_SENSITIVE + "=true");
				int startRow = 1;
				if (isCsvFileHeader) {
					startRow = 2;
				}
				writer.println(SchemaInfo.PROP_START_ROW + "=" + startRow);
				if (temporalKey != null) {
					writer.println(SchemaInfo.PROP_IS_TEMPORAL + "=true");
				} else {
					writer.println(SchemaInfo.PROP_IS_TEMPORAL + "=false");
				}
			}
			keyGetters = ReflectionUtil.getAllGetters(keyObject.getClass());
		}

		if (value != null) {
			valueGetters = ReflectionUtil.getAllGetters(value.getClass());
		}

		if (printHeader) {
			writer.println();
		}
		writer.println("#");
		writer.println("# Schema Definition");
		writer.println("#");
		printSchema(writer, key, keyGetters, "Key", "Primary");
		printSchema(writer, value, valueGetters, "Value", null);
	}

	private static void printSchema(PrintWriter writer, Object object, Method methods[], String primitiveFieldName,
			String categoryName)
	{
		if (object == null || object instanceof String || object.getClass().isPrimitive()
				|| object.getClass() == Boolean.class || object.getClass() == Byte.class
				|| object.getClass() == Character.class || object.getClass() == Short.class
				|| object.getClass() == Integer.class || object.getClass() == Long.class
				|| object.getClass() == Float.class || object.getClass() == Double.class || object instanceof Date) {
			writer.print(primitiveFieldName + ", " + object.getClass().getName());
			if (categoryName != null) {
				writer.println(", " + categoryName);
			} else {
				writer.println();
			}
		} else if (object instanceof ITemporalKey) {
			ITemporalKey tk = (ITemporalKey) object;
			// identity key separated by the column separator
			printSchema(writer, tk.getIdentityKey(), methods, primitiveFieldName, categoryName);
			writer.println(SchemaInfo.PROP_TEMPORAL_START_VALID_TIME + ", Date, Temporal");
			writer.println(SchemaInfo.PROP_TEMPORAL_END_VALID_TIME + ", Date, Temporal");
			writer.println(SchemaInfo.PROP_TEMPORAL_WRITTEN_TIME + ", Date, Temporal");
			writer.println(SchemaInfo.PROP_USER_NAME + ", String, Temporal");
		} else if (object instanceof Map) {
			boolean doMap = false;
			if (object instanceof KeyMap) {
				KeyMap keyMap = (KeyMap) object;
				KeyType keyType = KeyTypeManager.getLatestKeyTypeVersion(keyMap.getKeyType());
				if (keyType != null) {
					Set<String> keySet = keyType.getNameSet();
					ArrayList<String> keyList = new ArrayList<String>(keySet);
					Collections.sort(keyList);
					for (String keyName : keyList) {
						KeyType kt = keyType.getKeyType(keyName);
						writer.println(kt.getName() + ", " + kt.getType().getName());
					}
				} else {
					doMap = true;
				}
			}

			if (doMap) {
				Map mapObj = (Map) object;
				Set keySet = mapObj.keySet();
				ArrayList keyList = new ArrayList(keySet);
				Collections.sort(keyList);
				for (Object key : keyList) {
					Object val = mapObj.get(key);
					writer.print(key.toString() + ", ");
					if (val == null) {
						writer.println("Object");
					} else {
						writer.println(val.getClass().getName());
					}
				}
			}
		} else {
			for (int i = 0; i < methods.length; i++) {
				writer.println(methods[i].getName().substring(2) + ", " + methods[i].getReturnType().getName());
			}
		}
	}

	private static void printCsvHeader(PrintWriter writer, Object object, Method methods[], List<String> keyList,
			String fieldTerminator, String primitiveFieldName)
	{
		if (object == null || object instanceof String || object.getClass().isPrimitive()
				|| object.getClass() == Boolean.class || object.getClass() == Byte.class
				|| object.getClass() == Character.class || object.getClass() == Short.class
				|| object.getClass() == Integer.class || object.getClass() == Long.class
				|| object.getClass() == Float.class || object.getClass() == Double.class || object instanceof Date) {
			writer.print(primitiveFieldName);
		} else if (object instanceof ITemporalKey) {
			ITemporalKey tk = (ITemporalKey) object;
			// identity key separated by the column separator
			printCsvHeader(writer, tk.getIdentityKey(), methods, keyList, fieldTerminator, primitiveFieldName);
			writer.print(fieldTerminator);
			writer.write(SchemaInfo.PROP_TEMPORAL_START_VALID_TIME);
			writer.print(fieldTerminator);
			writer.write(SchemaInfo.PROP_TEMPORAL_END_VALID_TIME);
			writer.print(fieldTerminator);
			writer.write(SchemaInfo.PROP_TEMPORAL_WRITTEN_TIME);
			writer.print(fieldTerminator);
			writer.print(SchemaInfo.PROP_USER_NAME);
		} else if (keyList != null) {
			for (int i = 0; i < keyList.size(); i++) {
				if (i > 0) {
					writer.print(fieldTerminator);
				}
				writer.print(keyList.get(i));
			}
		} else if (object instanceof KeyMap) {

			KeyMap keyMap = (KeyMap) object;
			KeyType keyType = KeyTypeManager.getLatestKeyTypeVersion(keyMap.getKeyType());
			Set<String> keySet;
			if (keyType != null) {
				keySet = keyType.getNameSet();
			} else {
				keySet = keyMap.keySet();
			}
			keyList = new ArrayList<String>(keySet);
			Collections.sort(keyList);
			for (int i = 0; i < keyList.size(); i++) {
				String keyName = keyList.get(i);
				writer.print(keyName);
				if (i < keyList.size() - 1) {
					writer.print(fieldTerminator);
				}
			}
		} else {
			for (int i = 0; i < methods.length; i++) {
				String name = methods[i].getName().substring(3);
				writer.print(name);
				if (i < methods.length - 1) {
					writer.print(fieldTerminator);
				}
			}
		}
	}

	public static void printObject(PrintWriter writer, Method methods[], Object object, String fieldTerminator,
			String rowTerminator, SimpleDateFormat dateFormat)
	{
		printObject(writer, methods, object, fieldTerminator, dateFormat);
		writer.print(rowTerminator);
	}

	private static void printObject(PrintWriter writer, Method methods[], Object object, String fieldTerminator,
			SimpleDateFormat dateFormat)
	{
		if (object == null) {
			writer.print("null");
		} else if (object instanceof String) {
			String value = object.toString();

			// For each quote, add matching quote
			value = value.replaceAll("\"", "\"\"");

			// If contains a quote then enclose it with quotes
			if (value.indexOf("\"") != -1) {
				value = "\"" + value;
				value = value + "\"";
			} else {

				// If begins with a " then prepend a ".
				if (value.startsWith("\"")) {
					value = "\"" + value;
				}

				// If ends with a " then end it with a ".
				if (value.endsWith("\"")) {
					value = value + "\"";
				}
			}

			// If the string contains a field terminator or a line break then
			// enclose it in quotes.
			if (value.indexOf(fieldTerminator) >= 0 || value.indexOf("\n") >= 0) {
				writer.print("\"");
				writer.print(value);
				writer.print("\"");
			} else {
				writer.print(value);
			}

		} else if (object.getClass().isPrimitive() || object.getClass() == Boolean.class
				|| object.getClass() == Byte.class || object.getClass() == Character.class
				|| object.getClass() == Short.class || object.getClass() == Integer.class
				|| object.getClass() == Long.class || object.getClass() == Float.class
				|| object.getClass() == Double.class) {
			writer.print(object.toString());

		} else if (object instanceof Date) {

			writer.print(dateFormat.format((Date) object));

		} else if (methods != null) {
			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];
				try {
					Object value = method.invoke(object);
					value = getPrintableValue(value);
					printObject(writer, null, value, fieldTerminator, dateFormat);
					if (i < methods.length - 1) {
						writer.print(fieldTerminator);
					}
				} catch (Exception ex) {
				}
			}
		} else {
			String v = object.toString();
			if (v.indexOf(fieldTerminator) >= 0) {
				writer.print("\"");
				writer.print(v);
				writer.print("\"");
			} else {
				writer.print(v);
			}
		}
	}

	private static void printMap(PrintWriter writer, List keyList, Map map, String fieldTerminator,
			SimpleDateFormat dateFormat)
	{
		if (map == null) {
			writer.print("null");
		}

		int size = keyList.size();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				writer.print(fieldTerminator);
			}
			Object value = map.get(keyList.get(i));
			value = getPrintableValue(value);
			printObject(writer, null, value, fieldTerminator, dateFormat);
		}
	}

	/**
	 * 
	 * @param writer
	 * @param keyType
	 *            KeyType to retrieve values from the specified KeyMap object.
	 *            This key type is used represents the version of KeyType class
	 *            to use for all value retrieval from KeyMap.
	 * @param keyMap
	 * @param fieldTerminator
	 * @param dateFormat
	 */
	private static void printKeyMap(PrintWriter writer, KeyType keyType, KeyMap keyMap, List keyList,
			String fieldTerminator, SimpleDateFormat dateFormat)
	{
		if (keyList != null) {
			if (keyList.size() > 0) {
				printObject(writer, null, keyMap.get(keyList.get(0)), fieldTerminator, dateFormat);
			}
			for (int i = 1; i < keyList.size(); i++) {
				writer.print(fieldTerminator);
				printObject(writer, null, keyMap.get(keyList.get(i)), fieldTerminator, dateFormat);
			}
		} else if (keyType != null) {
			KeyType[] keyTypes = keyType.getValues();
			if (keyTypes.length > 0) {
				printObject(writer, null, keyMap.get(keyTypes[0]), fieldTerminator, dateFormat);
			}
			for (int i = 1; i < keyTypes.length; i++) {
				writer.print(fieldTerminator);
				printObject(writer, null, keyMap.get(keyTypes[i]), fieldTerminator, dateFormat);
			}
		}
	}

	private static void printTemporalKey(PrintWriter writer, Method methods[], ITemporalKey key,
			String fieldTerminator, SimpleDateFormat dateFormat)
	{
		printObject(writer, methods, key.getIdentityKey(), fieldTerminator, dateFormat);
		writer.print(fieldTerminator);
		writer.print(dateFormat.format(new Date(key.getStartValidTime())));
		writer.print(fieldTerminator);
		writer.print(dateFormat.format(new Date(key.getEndValidTime())));
		writer.print(fieldTerminator);
		writer.print(dateFormat.format(new Date(key.getWrittenTime())));
		writer.print(fieldTerminator);
		writer.print(key.getUsername());
	}

	private static void printTemporalData(PrintWriter writer, KeyType keyType, List keyList, Method methods[],
			ITemporalData data, String fieldTerminator, SimpleDateFormat dateFormat)
	{
		Object value;
		if (data instanceof TemporalData) {
			value = ((TemporalData) data).getValue();
		} else {
			value = data;
		}
		if (value instanceof KeyMap) {
			KeyMap keyMap = (KeyMap) value;
			printKeyMap(writer, keyType, keyMap, keyList, fieldTerminator, dateFormat);
		} else {
			printObject(writer, methods, data, fieldTerminator, dateFormat);
		}
	}

	private static Object getPrintableValue(Object value)
	{
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

}
