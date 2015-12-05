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
package com.netcrest.pado.biz.mon;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.netcrest.pado.IGridCollector;
import com.netcrest.pado.info.KeyTypeInfo;

/**
 * Collects grid results of the form Map&lt;Object, SortedSet&lt;KeyTypeInfo&gt;&gt; into a single SortedMap.
 * All of the objects in Set are merged into a single Set per Object key.
 * @author dpark
 *
 * @param <T> Individual grid result type
 * @param <S> Return type
 */
@SuppressWarnings("rawtypes")
public class SortedMapOfSortedSetsKeyTypeInfoCollector<T extends List<Map<Object, SortedSet>>, S> implements IGridCollector<T, S>
{

	private SortedMap<Object, SortedSet> resultMap = new TreeMap<Object, SortedSet>();
	
	@SuppressWarnings("unchecked")
	@Override
	public void addResult(String gridId, T result)
	{
		List<Map<Object, SortedSet>> list = (List<Map<Object, SortedSet>>)result;
		for (Map<Object, SortedSet> map : list) {
			for (Map.Entry<?, SortedSet> entry : map.entrySet()) {
				Object key = entry.getKey();
				Set<KeyTypeInfo> set = entry.getValue();
				SortedSet<KeyTypeInfo> resultSet = resultMap.get(key);
				if (resultSet == null) {
					resultSet = new TreeSet(set);
					resultMap.put(key, resultSet);
				} else {
					for (KeyTypeInfo keyTypeInfo : set) {
						for (KeyTypeInfo keyTypeInfo2 : resultSet) {
							if (keyTypeInfo.getKeyTypeClassName().equals(keyTypeInfo2.getKeyTypeClassName())) {
								keyTypeInfo2.setInstanceCount(keyTypeInfo.getInstanceCount() + keyTypeInfo2.getInstanceCount());
								break;
							}
						}
					}
					resultSet.addAll(set);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
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
