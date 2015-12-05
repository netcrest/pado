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
package com.netcrest.pado.index.gemfire.impl;

import java.util.ArrayList;
import java.util.List;

import com.netcrest.pado.IGridCollector;
import com.netcrest.pado.index.result.IGridResults;
import com.netcrest.pado.index.result.ResultItem;
import com.netcrest.pado.index.result.ValueInfo;
import com.netcrest.pado.index.service.GridQuery;

/**
 * Collects lists of collections into a single collection.
 * 
 * @author dpark
 * 
 * @param <T>
 * @param <S>
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class BizIndexMatrixCollector<T extends List<IGridResults<ResultItem<Object>>>, S> implements IGridCollector<T, S>
{
	protected BizScrollableResultSetImpl results;

	public BizIndexMatrixCollector(GridQuery request, BizGridQueryServiceImpl queryServiceImpl)
	{
		results = new BizScrollableResultSetImpl(request, queryServiceImpl);
	}

	protected BizIndexMatrixCollector()
	{
	}
	
	@Override
	public void addResult(String gridId, T result)
	{
		List<IGridResults<ResultItem<Object>>> allList = (List<IGridResults<ResultItem<Object>>>) result;
		
		for (IGridResults<ResultItem<Object>> gridResults : allList) {
			if (gridResults == null) {
				continue;
			}
			List list = gridResults.getAggregatedSortedResults();
			if (list == null) {
				return;
			}
			List<Object> resultList = new ArrayList<Object>(list.size());

			if (list != null && list.size() > 0) {
				for (ResultItem resultItem : (List<ResultItem>) list) {
					resultList.add(((ValueInfo) resultItem.getItem()).getValue());
				}
			}
			results.setTotalSize(gridResults.getTotalServerResults());
			results.setValueResultList(resultList);
		}
	}

	@Override
	public synchronized S getResult()
	{
		return (S) results;
	}

	@Override
	public synchronized void endResults()
	{
		results.commit();
		notifyAll();
	}

	@Override
	public void clearResults()
	{
		results.clear();
	}
}
