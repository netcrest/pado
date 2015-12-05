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
package com.netcrest.pado.tools.pado;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.gemstone.gemfire.cache.query.Struct;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.tools.pado.command.less;

public class BufferInfo
{
	private String name;
	private ICommand command;
	private String commandString;
	private IScrollableResultSet<?> srs;
	private String gridId;
	private String gridPath;
	private Set<Integer> removedRowNumberSet = new TreeSet<Integer>();

	public BufferInfo(String name, String commandString, ICommand command, IScrollableResultSet<?> srs)
	{
		this(name, commandString, command, srs, null, null);
	}
	
	public BufferInfo(String name, String commandString, ICommand command, IScrollableResultSet<?> srs, String gridId, String gridPath)
	{
		this.name = name;
		this.commandString = commandString;
		this.command = command;
		this.srs = srs;
		this.gridId = gridId;
		this.gridPath = gridPath;
	}
	
	public String getName()
	{
		return name;
	}

	public String getGridId()
	{
		return gridId;
	}

	public String getGridPath()
	{
		return gridPath;
	}
	
	public String getCommandString()
	{
		return commandString;
	}

	public ICommand getCommand()
	{
		return command;
	}

	public IScrollableResultSet<?> getScrollableResultSet()
	{
		return srs;
	}

	public Object getKey(int index)
	{
		Object key = null;
		int startIndex = srs.getStartIndex();
		if (srs != null) {
			srs.goToSet(index);
			List list = srs.toList();
			if (list != null && list.size() > 0) {
				Object obj = list.get(0);
				if (command instanceof less) {
					if (obj instanceof Struct) {
						Struct struct = (Struct) obj;
						Object[] fields = struct.getFieldValues();
						if (fields.length > 0) {
							key = fields[0];
						}
					} else {
						key = obj;
					}
				}
			}
		}
		srs.goToSet(startIndex);
		return key;
	}

	public Map<Integer, Object> getKeyMap(List<?> bufferNumList, int bufferNumListStartIndex)
	{
		HashMap<Integer, Object> keyMap = new HashMap<Integer, Object>();
		if (srs != null) {
			for (int i = bufferNumListStartIndex; i < bufferNumList.size(); i++) {
				String val = (String) bufferNumList.get(i);
				String split[] = val.split("-");
				if (split.length == 2) {
					int startI = Integer.parseInt(split[0]);
					int endIndex = Integer.parseInt(split[1]);

					for (int j = startI; j <= endIndex; j++) {
						Object key = getKey(j - 1);
						if (key != null) {
							keyMap.put(j, key);
						}
					}
				} else {
					int index = Integer.parseInt(split[0]);
					Object key = getKey(index - 1);
					if (key != null) {
						keyMap.put(index, key);
					}
				}
			}
		}
		return keyMap;
	}

	public void addRemovedRowNumber(int rowNumber)
	{
		removedRowNumberSet.add(rowNumber);
	}
	
	/**
	 * Returns the sorted set of all removed row numbers.
	 */
	public Set<Integer> getRemovedRowNumberSet()
	{
		return removedRowNumberSet;
	}
}
