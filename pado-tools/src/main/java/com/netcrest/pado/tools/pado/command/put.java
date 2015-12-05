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
package com.netcrest.pado.tools.pado.command;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.util.GridUtil;
import com.netcrest.pado.internal.util.ReflectionUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.tools.pado.BufferInfo;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.SharedCache;
import com.netcrest.pado.tools.pado.util.ObjectUtil;
import com.netcrest.pado.tools.pado.util.PrintUtil;

public class put implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
		options.addOption("k", false, "");
		options.addOption("v", false, "");
		options.addOption("kv", false, "");
		options.addOption("vk", false, "");
		options.addOption("buffer", true, "");
	}

	@Override
	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
		padoShell.addBufferCommand(this.getClass().getSimpleName());
	}

	@Override
	public void help()
	{
		PadoShell.println("put [-path <path>] | [-buffer <name> [-kv]] (<key1>,<value1>)(<key2>,<value2>)... | [-?]");
		PadoShell.println("put (<key1>,<value1>)(<key2>,<value2>)...");
		PadoShell.println("put -buffer <name> -k (<key num1>,<value1>)(<key num2>,<value2>)...");
		PadoShell.println("put -buffer <name> -v (<key1>,<value num1>)(<key2>,<value num2>)...");
		PadoShell.println("put -buffer <name> -kv (<key num1>,<value num1>)(<key num2>,<value num2>)...");
		PadoShell.println("   Put entries. Commands that use buffered results are:");
		PadoShell.println("   " + padoShell.getBufferCommandSet());
		PadoShell.println();
		PadoShell.println("      <key> and <value> support primitive, String, and java.util.Date");
		PadoShell.println("      types. These types must be specifed with special tags as follows:");
		PadoShell.println("         <decimal>b|B - Byte      (e.g., 1b)");
		PadoShell.println("         <decimal>c|C - Character (e.g., 1c)");
		PadoShell.println("         <decimal>s|S - Short     (e.g., 12s)");
		PadoShell.println("         <decimal>i|I - Integer   (e.g., 15 or 15i)");
		PadoShell.println("         <decimal>l|L - Long      (e.g., 20l");
		PadoShell.println("         <decimal>f|F - Float     (e.g., 15.5 or 15.5f)");
		PadoShell.println("         <decimal>d|D - Double    (e.g., 20.0d)");
		PadoShell.println("         '<string with \\ delimiter>' (e.g., '\\'Wow!\\'!' Hello, world')");
		PadoShell.println("         to_date('<date string>', '<simple date format>'");
		PadoShell.println("                       (e.g., to_date('04/10/2009', 'MM/dd/yyyy')");
		PadoShell.println();
		PadoShell.println("     If a suffix letter is not specifed then it is read as Integer");
		PadoShell.println("     unless the decimal point or the letter 'e' or 'E' is specified,");
		PadoShell.println("     in which case, it is read as Double. Note that if the <key> or");
		PadoShell.println("     <value> class is used then a suffix letter is *not* required.");
		PadoShell.println();
		PadoShell.println("     <key> The key class defined by the 'key' command is used");
		PadoShell.println("           to construct the key object.");
		PadoShell.println("     <value> The value class defined by the 'value' command is used");
		PadoShell.println("           to construct the value object.");
		PadoShell.println("     The <key> and <value> objects are created using the following");
		PadoShell.println("     format:");
		PadoShell.println("         <property name1>=<property value1> and ");
		PadoShell.println("         <property name2>=<property value1> and ...");
		PadoShell.println();
		PadoShell.println("     -k Put enumerated keys. If this option is not specified, then");
		PadoShell.println("        <key> is expected.");
		PadoShell.println("     -v Put enumerated values. If this option is not specified, then");
		PadoShell.println("        <value> is expected.");
		PadoShell.println();
		PadoShell.println("     Examples:");
		PadoShell.println("        put (15L, to_date('04/10/2009', 'MM/dd/yyyy')");
		PadoShell.println("        put ('NETCREST', Price=100.50 and Date=to_date('03/03/2015',\\");
		PadoShell.println("             'MM/dd/yyyy')");
		PadoShell.println("        put -kv (1, 5)  - puts the enum key 1 with the enum 5 value");
	}

	@Override 
	public String getShortDescription()
	{
		return "Put one or more entries into path. Put buffer contents into a path.";
	}
	
	@Override
	public boolean isLoginRequired()
	{
		return true;
	}

	@Override
	public Options getOptions()
	{
		return options;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void run(CommandLine commandLine, String command) throws Exception
	{
		String path = commandLine.getOptionValue("path");
		String bufferName = commandLine.getOptionValue("buffer");
		List<String> argList = commandLine.getArgList();

		if (path != null && bufferName != null) {
			PadoShell.printlnError(this, "Specifying both path and buffer not allowed. Only one option allowed.");
			return;
		}
		if (argList.size() < 2) {
			PadoShell.println(this, "Must specify key/value pair(s).");
			return;
		}
		if (path == null && bufferName == null) {
			path = padoShell.getCurrentPath();
		}

		boolean keyEnumerated = commandLine.hasOption('k');
		boolean valueEnumerated = commandLine.hasOption('v');
		Option options[] = commandLine.getOptions();
		for (Option option : options) {
			if (keyEnumerated == false) {
				keyEnumerated = option.getOpt().contains("k");
			}
			if (valueEnumerated == false) {
				valueEnumerated = option.getOpt().contains("v");
			}
		}

		int keyIndex = argList.indexOf(argList.get(1));
		String fullPath;
		String gridId;
		String gridPath;
		BufferInfo bufferInfo = null;
		if (path != null) {
			fullPath = padoShell.getFullPath(path);
			gridPath = GridUtil.getChildPath(fullPath);
			gridId = SharedCache.getSharedCache().getGridId(fullPath);
		} else {
			bufferInfo = SharedCache.getSharedCache().getBufferInfo(bufferName);
			if (bufferInfo == null) {
				PadoShell.printlnError(this, bufferName + ": Buffer undefined.");
				return;
			}
			gridId = bufferInfo.getGridId();
			gridPath = bufferInfo.getGridPath();
			if (gridId == null || gridPath == null) {
				PadoShell.printlnError(this, bufferName + ": Invalid buffer. This buffer does not contain keys.");
				return;
			}
			fullPath = SharedCache.getSharedCache().getPado().getCatalog().getGridService()
					.getFullPath(gridId, gridPath);
		}
		Map map = getEntryMap(fullPath, bufferInfo, argList, keyEnumerated, valueEnumerated, keyIndex);
		IGridMapBiz gridMapBiz = SharedCache.getSharedCache().getPado().getCatalog()
				.newInstance(IGridMapBiz.class, gridPath);
		gridMapBiz.getBizContext().getGridContextClient().setGridIds(gridId);
		gridMapBiz.putAll(map);

		// ArrayList keyList = new ArrayList();
		if (padoShell.isShowResults()) {
			PrintUtil.printEntries(gridMapBiz, map.keySet(), null);
		}
	}

	/**
	 * Returns the index of the enclosed parenthesis, i.e., ')'.
	 * 
	 * @param buffer
	 * @param startIndex
	 * @return
	 */
	private int getEnclosingParenthesis(StringBuffer buffer, int startIndex)
	{
		int enclosedIndex = -1;
		int parenCount = 0;
		boolean inQuote = false;
		// to_date('04/09/2009', 'MM/dd/yyyy')
		for (int i = startIndex; i < buffer.length(); i++) {
			char c = buffer.charAt(i);
			if (c == '(') {
				if (inQuote == false) {
					parenCount++;
				}
			} else if (c == ')') {
				if (inQuote == false) {
					parenCount--;
				}
				if (parenCount == 0) {
					enclosedIndex = i;
					break;
				}
			} else if (c == '\'') {
				inQuote = !inQuote;
			}
		}
		return enclosedIndex;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map getEntryMap(String fullPath, BufferInfo bufferInfo, List<String> list, boolean keyEnumerated, boolean valueEnumerated,
			int startIndex) throws Exception
	{
		String pairs = "";
		for (int i = startIndex; i < list.size(); i++) {
			pairs += list.get(i) + " ";
		}

		Map<String, Method> keySetterMap = ReflectionUtil.getAllSettersMap(padoShell.getKeyClass());
		Map<String, Method> valueSetterMap = ReflectionUtil.getAllSettersMap(padoShell.getValueClass());

		String gridPath = GridUtil.getChildPath(fullPath);
		String gridId = SharedCache.getSharedCache().getGridId(fullPath);
		IGridMapBiz gridMapBiz = SharedCache.getSharedCache().getPado().getCatalog()
				.newInstance(IGridMapBiz.class, gridPath);
		gridMapBiz.getBizContext().getGridContextClient().setGridIds(gridId);

		// (x='1,2,3' and y='2',a='hello, world' and b='test')
		HashMap map = new HashMap();
		StringBuffer buffer = new StringBuffer(pairs);
		boolean keySearch = false;
		boolean fieldSearch = false;
		boolean openQuote = false;
		boolean delimiter = false;
		boolean quoted = false;
		String fieldString = "";
		String valueString = "";
		String and = "";

		Object key = null;
		Object value = null;
		for (int i = 0; i < buffer.length(); i++) {
			char c = buffer.charAt(i);
			if (c == '(') {
				if (openQuote == false) {

					String function = null;
					String functionCall = null;
					String functionString = null;
					if (valueString.length() > 0) {
						functionString = valueString;
					} else if (fieldString.length() > 0) {
						functionString = fieldString;
					}
					if (functionString != null) {

						// it's a function

						// get enclosed parenthesis
						int enclosedIndex = getEnclosingParenthesis(buffer, i);

						function = functionString.toLowerCase();
						if (enclosedIndex == -1) {
							throw new ParseException("Malformed function call: " + function, i);
						}

						functionCall = function + buffer.substring(i, enclosedIndex + 1);
						Logger.fine("functionCall = |" + functionCall + "|");
						i = enclosedIndex;
					}
					if (functionCall != null) {
						if (valueString.length() > 0) {
							valueString = functionCall;
						} else if (fieldString.length() > 0) {
							fieldString = functionCall;
						}

					} else {
						key = null;
						value = null;
						keySearch = true;
						fieldSearch = true;
						fieldString = "";
						valueString = "";
					}

					quoted = false;

					continue;
				}

			} else if (c == '=') {
				if (keySearch && key == null && keyEnumerated == false) {
					key = padoShell.getKeyClass().newInstance();
				}
				if (keySearch == false && value == null && valueEnumerated == false) {
					if (padoShell.getValueClass() == null) {
						throw new ClassNotFoundException(
								"Undefined value class. Use the 'value' command to set the class name");
					}
					value = padoShell.getValueClass().newInstance();
				}
				fieldSearch = false;
				continue;
			} else if (c == ')') {
				if (openQuote == false) {
					Logger.fine("v: field = " + fieldString);
					Logger.fine("v: value = " + valueString);
					Logger.fine("");

					if (valueEnumerated) {
						Object k = bufferInfo.getKey(Integer.parseInt(fieldString) - 1);
						if (k == null) {
							PadoShell.printlnError("Error: value not found in the cache for the key number "
									+ fieldString);
							PadoShell.println("       run 'key -l' to view the enumerated keys.");
							map.clear();
							break;
						}
						value = gridMapBiz.get(k);
						if (key == null) {
							PadoShell.printlnError("Error: value not in the cache - " + fieldString);
							map.clear();
							break;
						}
						Logger.fine("k = " + k);
						Logger.fine("key = " + key);
						Logger.fine("value = " + value);
					} else {
						if (valueString.length() == 0) {
							// primitive
							value = ObjectUtil.getPrimitive(padoShell, fieldString, quoted);
						} else {
							updateObject(valueSetterMap, value, fieldString, valueString);
						}
					}

					map.put(key, value);

					fieldSearch = true;
					quoted = false;
					fieldString = "";
					valueString = "";
					key = null;
					and = "";
					continue;
				}
			} else if (c == '\\') {
				// ignore and treat the next character as a character
				delimiter = true;
				continue;
			} else if (c == '\'') {
				if (delimiter) {
					delimiter = false;
				} else {
					if (openQuote) {
						quoted = true;
					}
					openQuote = !openQuote;
					continue;
				}
			} else if (c == ' ') {
				if (openQuote == false) {
					boolean andExpected = false;
					if (keySearch) {
						Logger.fine("k: field = " + fieldString);
						Logger.fine("k: value = " + valueString);
						Logger.fine("");

						if (fieldString.length() > 0) {
							updateObject(keySetterMap, key, fieldString, valueString);
							andExpected = true;
						}
					} else {
						Logger.fine("v: field = " + fieldString);
						Logger.fine("v: value = " + valueString);
						Logger.fine("");

						if (fieldString.length() > 0) {
							updateObject(valueSetterMap, value, fieldString, valueString);
							andExpected = true;
						}
					}

					if (andExpected) {
						and = "";
						int index = -1;
						for (int j = i; j < buffer.length(); j++) {
							and += buffer.charAt(j);
							and = and.trim().toLowerCase();
							if (and.equals("and")) {
								index = j;
								break;
							} else if (and.length() > 3) {
								break;
							}
						}
						if (index != -1) {
							i = index;
						}
					}

					fieldSearch = true;
					fieldString = "";
					valueString = "";
					and = "";
					quoted = false;
					continue;
				}
			}

			if (c == ',') {

				// if ',' is not enclosed in quotes...
				if (openQuote == false) {

					fieldString = fieldString.trim();
					valueString = valueString.trim();

					// end of key
					Logger.fine("k: field = " + fieldString);
					Logger.fine("k: value = " + valueString);
					Logger.fine("");

					if (keySearch) {
						if (keyEnumerated) {
							key = bufferInfo.getKey(Integer.parseInt(fieldString) - 1);
							if (key == null) {
								PadoShell.printlnError("Error: value not found in the cache for the key number "
										+ fieldString);
								PadoShell.println("       run 'key -l' to view the enumerated keys.");
								map.clear();
								break;
							}
						} else {
							if (valueString.length() == 0) {
								key = ObjectUtil.getPrimitive(padoShell, fieldString, quoted);
							} else {
								updateObject(keySetterMap, key, fieldString, valueString);
							}
						}
					} else {

						if (valueEnumerated) {
							Object k = bufferInfo.getKey(Integer.parseInt(fieldString) - 1);
							value = gridMapBiz.get(k);
							if (value == null) {
								PadoShell.printlnError("Error: undefined value num " + fieldString);
								map.clear();
								break;
							}
						} else {
							if (valueString.length() == 0) {
								value = ObjectUtil.getPrimitive(padoShell, fieldString, quoted);
							} else {

								updateObject(valueSetterMap, value, fieldString, valueString);
							}
						}

					}

					fieldSearch = true;
					keySearch = false;
					quoted = false;
					fieldString = "";
					valueString = "";
					and = "";
					continue;
				}
			}

			if (fieldSearch) {
				fieldString += c;
			} else if (quoted == false) {
				valueString += c;
			}
		}

		return map;
	}

	@SuppressWarnings("unused")
	private Object getFunctionValue(String functionCall) throws ParseException
	{
		if (functionCall.startsWith("to_date")) {
			return padoShell.getDate(functionCall);
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private void updateObject(Map<String, Method> setterMap, Object obj, String field, String value) throws Exception
	{
		String setterMethodName = "set" + field.trim();
		Method setterMethod = setterMap.get(setterMethodName);
		if (setterMethod == null) {
			return;
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
			Date date = padoShell.getDate(value);
			if (date == null) {
				PadoShell.println("   Unable to parse date.");
			} else {
				setterMethod.invoke(obj, date);
			}
		} else if (arg == String.class) {
			setterMethod.invoke(obj, value);
		} else {
			PadoShell.println("   Unsupported type: " + setterMethod.getName() + "(" + arg.getName() + ")");
			return;
		}
	}

	@SuppressWarnings({ "unused", "rawtypes" })
	public static void main(String[] args) throws Exception
	{
		String command = "put (x=123 and y='2' and z=123, a='hello, world' and b=12)(x='abc' and y='de' and z=456, a='test1' and b=99')";
		ArrayList list = new ArrayList();
		PadoShell padoShell = new PadoShell(null);
		put p = new put();
		p.initialize(padoShell);
//		p.getEntryMap(list, false, false, 1);
	}
}
