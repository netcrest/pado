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

import java.util.Collection;
import java.util.List;

import com.netcrest.pado.IGridCollector;

/**
 * Collects lists of collections into a single collection.
 * 
 * @author dpark
 * 
 * @param <T>
 * @param <S>
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CollectionCollector<T extends List<Collection>, S> implements IGridCollector<T, S>
{
	private Collection resultCollection;

	@Override
	public void addResult(String gridId, T result)
	{
		List<Collection> list = (List<Collection>) result;
		if (resultCollection == null) {
			for (Collection col : list) {
				if (col != null) {
					if (resultCollection == null) {
						resultCollection = col;
						continue;
					} else {
						resultCollection.addAll(col);
					}
				}
			}
		} else {
			for (Collection col : list) {
				if (col != null) {
					resultCollection.addAll(col);
				}
			}
		}
	}

	@Override
	public S getResult()
	{
		return (S) resultCollection;
	}

	@Override
	public void endResults()
	{
	}

	@Override
	public void clearResults()
	{
		if (resultCollection != null) {
			resultCollection.clear();
		}
	}
}
