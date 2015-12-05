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

import java.util.List;

import com.netcrest.pado.IGridCollector;

/**
 * Applies logical OR to all results. It returns true if any of the results is
 * true. It returns false if all of the results is false.
 * 
 * @author dpark
 * 
 * @param <T>
 */
public class BooleanOrCollector<T extends List<Boolean>, S> implements IGridCollector<T, Boolean>
{
	private boolean collectionResult = false;

	@Override
	public void addResult(String gridId, T result)
	{
		if (collectionResult) {
			return;
		}
		List<Boolean> list = (List) result;
		for (Boolean val : list) {
			if (val.booleanValue()) {
				collectionResult = val;
				break;
			}
		}
	}

	@Override
	public Boolean getResult()
	{
		return collectionResult;
	}

	@Override
	public void endResults()
	{
	}

	@Override
	public void clearResults()
	{
		collectionResult = false;
	}
}
