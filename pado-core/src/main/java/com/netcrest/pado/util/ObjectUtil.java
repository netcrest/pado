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
package com.netcrest.pado.util;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.internal.util.ReflectionUtil;
import com.netcrest.pado.log.Logger;
//import com.netcrest.pado.tools.pado.PadoShell;

@SuppressWarnings("rawtypes")
public class ObjectUtil
{
	public static Object updateObject(Map<String, Method> setterMap, Object obj, String setterMethodName, String value,
			SimpleDateFormat dateFormat, boolean isCsvFormat) throws Exception
	{
		Method setterMethod = setterMap.get(setterMethodName);
		if (setterMethod == null) {
			return obj;
		}

		Class types[] = setterMethod.getParameterTypes();
		Class arg = types[0];
		if (arg == byte.class || arg == Byte.class) {
			setterMethod.invoke(obj, Byte.parseByte(value));
		} else if (arg == char.class || arg == Character.class) {
			setterMethod.invoke(obj, value.charAt(0));
		} else if (arg == short.class || arg == Short.class) {
			setterMethod.invoke(obj, Short.parseShort(value));
		} else if (arg == int.class || arg == Integer.class) {
			setterMethod.invoke(obj, Integer.parseInt(value));
		} else if (arg == long.class || arg == Long.class) {
			setterMethod.invoke(obj, Long.parseLong(value));
		} else if (arg == float.class || arg == Float.class) {
			setterMethod.invoke(obj, Float.parseFloat(value));
		} else if (arg == double.class || arg == Double.class) {
			setterMethod.invoke(obj, Double.parseDouble(value));
		} else if (arg == Date.class) {
			Date date = dateFormat.parse(value);
			setterMethod.invoke(obj, date);
		} else if (arg == String.class) {

			if (isCsvFormat) {
				// Replace double quotes with single quotes
				value = value.replaceAll("\"\"", "\"");

				// if begins with a quote then remove it
				if (value.startsWith("\"")) {
					value = value.substring(1);
				}
				// if ends with a quote then remove it
				if (value.endsWith("\"")) {
					value = value.substring(0, value.length() - 1);
				}
			}
			setterMethod.invoke(obj, value);
		} else {
			Logger.error("Error: unsupported type: " + setterMethod.getName() + "(" + arg.getName() + ")");
		}
		return obj;
	}

	public static Object updateKeyMap(KeyMap keyMap, String keyName, String value, SimpleDateFormat dateFormat,
			boolean isCsvFormat) throws Exception
	{
		KeyType keyType = keyMap.getKeyType().getKeyType(keyName);
		if (keyType == null) {
			throw new RuntimeException("KeyType " + keyName + " is undefined in "
					+ keyMap.getKeyType().getClass().getCanonicalName() + ".");
		}
		Object mapValue = null;
		Class arg = keyType.getType();
		if (value != null && value.equalsIgnoreCase("null") == false) {
			if (arg == byte.class || arg == Byte.class) {
				mapValue = Byte.parseByte(value);
			} else if (arg == char.class || arg == Character.class) {
				mapValue = value.charAt(0);
			} else if (arg == short.class || arg == Short.class) {
				mapValue = Short.parseShort(value);
			} else if (arg == int.class || arg == Integer.class) {
				mapValue = Integer.parseInt(value);
			} else if (arg == long.class || arg == Long.class) {
				mapValue = Long.parseLong(value);
			} else if (arg == float.class || arg == Float.class) {
				mapValue = Float.parseFloat(value);
			} else if (arg == double.class || arg == Double.class) {
				mapValue = Double.parseDouble(value);
			} else if (arg == Date.class) {
				Date date = dateFormat.parse(value);
				mapValue = date;
			} else if (arg == BigDecimal.class) {
				mapValue = new BigDecimal(value);
			} else if (arg == String.class) {

				if (isCsvFormat) {
					// Replace double quotes with single quotes
					value = value.replaceAll("\"\"", "\"");

					// if begins with a quote then remove it
					if (value.startsWith("\"")) {
						value = value.substring(1);
					}
					// if ends with a quote then remove it
					if (value.endsWith("\"")) {
						value = value.substring(0, value.length() - 1);
					}
				}
				mapValue = value;
			} else {
				throw new RuntimeException("Error: unsupported type: " + value + " ("
						+ value.getClass().getCanonicalName() + ")");
			}
		}

		keyMap.put(keyType, mapValue);
		return keyMap;
	}
	
	public static Object updateKeyMap(KeyMap keyMap, String keyName, String value, SimpleDateFormat dateFormat, NumberFormat numFormat,
			boolean isCsvFormat, Class arg) throws Exception
	{
		Object mapValue = null;
		if (value != null && value.equalsIgnoreCase("null") == false) {
			try {
				if (arg == byte.class || arg == Byte.class) {
					if (value != null && value.length() > 0) {
						mapValue = (byte)Byte.parseByte(value);
					}
				} else if (arg == char.class || arg == Character.class) {
					if (value != null && value.length() > 0) {
						mapValue = (char)value.charAt(0);
					}
				} else if (arg == short.class || arg == Short.class) {
					if (value == null || value.length() == 0) {
						mapValue = (short)0;
					} else {
						Number number = numFormat.parse(value);
						if (value.endsWith("-")) {
							mapValue = (short)-number.shortValue();
						} else {
							mapValue = (short)number.shortValue();
						}
					}
				} else if (arg == int.class || arg == Integer.class) {
					if (value == null || value.length() == 0) {
						mapValue = (int)0;
					} else {
						Number number = numFormat.parse(value);
						if (value.endsWith("-")) {
							mapValue = (int)-number.intValue();
						} else {
							mapValue = (int)number.intValue();
						}
					}
				} else if (arg == long.class || arg == Long.class) {
					if (value == null || value.length() == 0) {
						mapValue = 0L;
					} else {
						Number number = numFormat.parse(value);
						if (value.endsWith("-")) {
							mapValue = (long)-number.longValue();
						} else {
							mapValue = (long)number.longValue();
						}
					}
				} else if (arg == float.class || arg == Float.class) {
					if (value == null || value.length() == 0) {
						mapValue = (float)0;
					} else {
						Number number = numFormat.parse(value);
						if (value.endsWith("-")) {
							mapValue = (float)-number.floatValue();
						} else {
							mapValue = (float)number.floatValue();
						}
					}
				} else if (arg == double.class || arg == Double.class) {
					if (value == null || value.length() == 0) {
						mapValue = 0d;
					} else {
						Number number = numFormat.parse(value);
						if (value.endsWith("-")) {
							mapValue = (double)-number.doubleValue();
						} else {
							mapValue = (double)number.doubleValue();
						}
					}
				} else if (arg == Date.class) {
					if (value == null || value.length() == 0) {
						mapValue = null;
					} else {
						Date date = dateFormat.parse(value);
						mapValue = date;
					}
				} else if (arg == BigDecimal.class) {
					if (arg == byte.class || arg == Byte.class) {
						mapValue = new BigDecimal(value);
					}
				} else if (arg == String.class) {
	
					if (isCsvFormat) {
						// Replace double quotes with single quotes
						value = value.replaceAll("\"\"", "\"");
	
						// if begins with a quote then remove it
						if (value.startsWith("\"")) {
							value = value.substring(1);
						}
						// if ends with a quote then remove it
						if (value.endsWith("\"")) {
							value = value.substring(0, value.length() - 1);
						}
					}
					mapValue = value;
				} else {
					throw new RuntimeException("Error: unsupported type: " + value + " ("
							+ value.getClass().getCanonicalName() + ")");
				}
			} catch (Exception ex) {
				Logger.error(ex);
				throw new PadoException("Error: invalid value - KeyMap[keyname=" + keyName + ", " + "type=" + arg.getName() + ", value=" + value + "]", ex);
			}
		}

		keyMap.put(keyName, mapValue);
		return keyMap;
	}
	
	public static String toJAVA(String unicode) {
	    String output = "";

	    char[] charArray = unicode.toCharArray();

	    for (int i = 0; i < charArray.length; ++i) {
	      char a = charArray[i];
	      if ((int) a > 255) {
	        output += "\\u" + Integer.toHexString((int) a);
	      } else {
	        output += a;
	      }
	    }

	    return output;
	  }

//	public static Object getPrimitive(PadoShell padoShell, String value, boolean quoted) throws ParseException
//	{
//		if (quoted) {
//			// string
//			return value;
//		}
//
//		value = value.trim().toLowerCase();
//		if (value.length() == 0) {
//			return null;
//		}
//		char firstChar = value.charAt(0);
//		if (!(firstChar == '.' || firstChar >= '0' && firstChar <= '9')) {
//			// it's not number
//			return null;
//		}
//
//		Object obj = null;
//		if (value.endsWith("b")) {
//			// byte
//			obj = new Byte(value.substring(0, value.length() - 1));
//		} else if (value.endsWith("c")) {
//			// char
//			obj = value.charAt(0);
//		} else if (value.endsWith("s")) {
//			// short
//			obj = new Short(value.substring(0, value.length() - 1));
//		} else if (value.endsWith("i")) {
//			// int
//			obj = new Integer(value.substring(0, value.length() - 1));
//		} else if (value.endsWith("l")) {
//			// long
//			obj = new Long(value.substring(0, value.length() - 1));
//		} else if (value.endsWith("f")) {
//			// float
//			obj = new Float(value.substring(0, value.length() - 1));
//		} else if (value.endsWith("d")) {
//			// double
//			obj = new Double(value.substring(0, value.length() - 1));
//		} else if (value.startsWith("to_date")) {
//			obj = padoShell.getDate(value);
//		} else {
//			if (value.indexOf(".") != -1 || value.indexOf("e") != -1) {
//				// use double by default
//				obj = new Double(value);
//			} else {
//				// use int by default
//				obj = new Integer(value);
//			}
//		}
//
//		return obj;
//	}
//
//	public static Object updateObject(PadoShell padoShell, Map<String, Method> setterMap, Object obj, String tableColumnName,
//			Object value) throws Exception
//	{
//		Method setterMethod = setterMap.get("set" + tableColumnName);
//		if (setterMethod == null) {
//			// TODO: need to search the map. column names can be case
//			// insensitive
//			return obj;
//		}
//		setterMethod.invoke(obj, value);
//
//		return obj;
//	}
//
//	public static Object updateObject(Object obj, Map<String, String> fieldsMap, PadoShell padoShell) throws Exception
//	{
//
//		Map<String, Method> setterMap = ReflectionUtil.getAllSettersMap(obj.getClass());
//
//		String setterMethodName = null;
//		String value = null;
//		for (String field : fieldsMap.keySet()) {
//			setterMethodName = "set"
//					+ field.trim().replaceFirst(field.substring(0, 1), field.substring(0, 1).toUpperCase());
//			Method setterMethod = setterMap.get(setterMethodName);
//			if (setterMethod == null) {
//				Logger.error("Error: Attribute \"+field+ \" undefined in " + obj.getClass().getName());
//				return null;
//			}
//			value = fieldsMap.get(field);
//			Class types[] = setterMethod.getParameterTypes();
//			Class arg = types[0];
//			if (arg == byte.class || arg == Byte.class) {
//				setterMethod.invoke(obj, Byte.parseByte(value));
//			} else if (arg == char.class || arg == Character.class) {
//				setterMethod.invoke(obj, value.charAt(0));
//			} else if (arg == short.class || arg == Short.class) {
//				setterMethod.invoke(obj, Short.parseShort(value));
//			} else if (arg == int.class || arg == Integer.class) {
//				setterMethod.invoke(obj, Integer.parseInt(value));
//			} else if (arg == long.class || arg == Long.class) {
//				setterMethod.invoke(obj, Long.parseLong(value));
//			} else if (arg == float.class || arg == Float.class) {
//				setterMethod.invoke(obj, Float.parseFloat(value));
//			} else if (arg == double.class || arg == Double.class) {
//				setterMethod.invoke(obj, Double.parseDouble(value));
//			} else if (arg == Date.class) {
//				Date date = padoShell.getDate(value);
//				if (date == null) {
//					padoShell.println("   Unable to parse date.");
//				} else {
//					setterMethod.invoke(obj, date);
//				}
//			} else if (arg == String.class) {
//				setterMethod.invoke(obj, value);
//			} else {
//				padoShell.println("   Unsupported type: " + setterMethod.getName() + "(" + arg.getName() + ")");
//			}
//		}
//		return obj;
//	}
//
//	public static Object generateObject(String className, Map<String, String> fieldsMap, PadoShell padoShell) throws Exception
//	{
//
//		Object obj = null;
//		obj = Class.forName(className).newInstance();
//		return (updateObject(obj, fieldsMap, padoShell));
//	}

	public static Object getPrintableObject(Object object)
	{
		if (object == null) {
			return "null";
		} else if (object instanceof String || object.getClass().isPrimitive() || object.getClass() == Boolean.class
				|| object.getClass() == Byte.class || object.getClass() == Character.class
				|| object.getClass() == Short.class || object.getClass() == Integer.class
				|| object.getClass() == Long.class || object.getClass() == Float.class
				|| object.getClass() == Double.class) {
			return object.toString();

		} else if (object instanceof Date) {

			return object.toString();

		} else {
			return ReflectionUtil.toStringGettersAnd(object);
		}
	}

}
