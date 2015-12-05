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
package com.netcrest.pado.internal.impl;

import java.util.Map;
import java.util.TreeMap;

import com.netcrest.pado.IGridCollector;

/**
 * DefaultGridCollector collects grid results in a map keyed by grid IDs.
 * 
 * @author dpark
 *
 * @param <T> result received from a single grid.
 * @param <S> Aggregated result returned to the IBiz caller.
 */
public class DefaultGridCollector<T, S extends Map<String, T>> implements IGridCollector<T, S>
{
	private Map<String, T> gridMap = new TreeMap<String, T>();
	
	@Override
	public void addResult(String gridId, T result)
	{
		gridMap.put(gridId, result);
	}

	@Override
	@SuppressWarnings("unchecked")
	public S getResult()
	{
		return (S)gridMap;
	}

	@Override
	public void endResults()
	{
	}
	
	@Override
	public void clearResults()
	{
		gridMap.clear();
	}
}
