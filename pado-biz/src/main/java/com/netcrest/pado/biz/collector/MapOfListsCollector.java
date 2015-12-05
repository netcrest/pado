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
package com.netcrest.pado.biz.collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.netcrest.pado.IGridCollector;

/**
 * Collects lists of maps that contain &lt;Object, List&gt; entries into a 
 * single map of &lt;Object, List&gt; entries.
 * @author dpark
 *
 * @param <T>
 * @param <S>
 */
public class MapOfListsCollector<T extends List<Map>, S> implements IGridCollector<T, S>
{

	private Map<Object, List> resultMap = new HashMap();
	
	@Override
	public void addResult(String gridId, T result)
	{
		List<Map> list = (List<Map>)result;
		for (Map map : list) {
			Set<Map.Entry<Object, List>> set = map.entrySet();
			for (Map.Entry<Object, List> entry : set) {
				Object key = entry.getKey();
				List value = entry.getValue();
				List list2 = resultMap.get(key);
				if (list2 == null) {
					list2 = new ArrayList(value);
					resultMap.put(key, list2);
				} else {
					list2.addAll(value);
				}
			}
		}
	}
	
	@Override
	public S getResult()
	{
		return (S)resultMap;
	}

	@Override
	public void endResults()
	{
	}

	@Override
	public void clearResults()
	{
		resultMap.clear();
	}

}
