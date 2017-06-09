/*
 * Copyright (c) 2013-2017 Netcrest Technologies, LLC. All rights reserved.
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
import com.netcrest.pado.data.jsonlite.JsonLite;

/**
 * Collects the list from each grid a &lt;gridId, result&gt; entry to JsonLite.
 * 
 * @author dpark
 *
 * @param <T>
 * @param <S>
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class JsonLiteGridCollector<T extends List<JsonLite>, S> implements IGridCollector<T, S>
{
	private JsonLite resultJl = new JsonLite();

	@Override
	public void addResult(String gridId, T result)
	{
		if (result == null) {
			return;
		}
		resultJl.put(gridId, result);
	}

	@Override
	public S getResult()
	{
		return (S) resultJl;
	}

	@Override
	public void endResults()
	{
	}

	@Override
	public void clearResults()
	{
		resultJl.clear();
	}

}
