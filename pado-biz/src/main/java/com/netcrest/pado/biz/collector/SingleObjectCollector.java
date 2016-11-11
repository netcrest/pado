/*
 * Copyright (c) 2013-2016 Netcrest Technologies, LLC. All rights reserved.
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

import java.util.List;

import com.netcrest.pado.IGridCollector;

/**
 * Returns the first object received from collections. All others are discarded.
 * 
 * @author dpark
 *
 * @param <T>
 * @param <S>
 */
@SuppressWarnings({ "unchecked"})
public class SingleObjectCollector<T extends List<Object>, S> implements IGridCollector<T, S>
{

	private Object object;

	@Override
	public void addResult(String gridId, T result)
	{
		if (object != null) {
			return;
		}
		List<Object> list = (List<Object>) result;
		if (list != null && list.size() > 0) {
			object = list.get(0);
		}
	}

	@Override
	public S getResult()
	{
		return (S) object;
	}

	@Override
	public void endResults()
	{
	}

	@Override
	public void clearResults()
	{
		object = null;
	}

}
