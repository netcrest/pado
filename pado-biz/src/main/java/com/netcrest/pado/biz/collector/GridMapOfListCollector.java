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

import com.netcrest.pado.IGridCollector;

/**
 * Returns a map of &lt;gridId, result&gt; pairs. 
 * @author dpark
 *
 * @param <T>
 * @param <S>
 */
public class GridMapOfListCollector<T extends List<Map>, S> implements IGridCollector<T, S>
{

	private Map<String, List> resultMap = new HashMap(10);
	
	@Override
	public void addResult(String gridId, T result)
	{
		List<Map> list = (List<Map>)result;
		List resultList = resultMap.get(gridId);
		if (resultList == null) {
			resultList = new ArrayList();
			resultMap.put(gridId, resultList);
		}
		for (Map<String, List> map : list) {
			for (List object : map.values()) {
				resultList.addAll(object);
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
