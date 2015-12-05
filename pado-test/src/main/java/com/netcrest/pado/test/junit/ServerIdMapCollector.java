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
package com.netcrest.pado.test.junit;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.netcrest.pado.IGridCollector;

public class ServerIdMapCollector implements IGridCollector<List<String>, Map<String, List<String>>>
{
	Map<String, List<String>> serverIdMap = new TreeMap<String, List<String>>();
	
	@Override
	public void addResult(String gridId, List<String> result)
	{
		serverIdMap.put(gridId, result);
	}

	@Override
	public Map<String, List<String>> getResult()
	{
		return serverIdMap;
	}
	
	@Override
	public void endResults()
	{
	}
	
	@Override
	public void clearResults()
	{
		serverIdMap.clear();
	}
}
