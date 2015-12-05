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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.netcrest.pado.index.result.IGridResults;
import com.netcrest.pado.index.result.ResultItem;
import com.netcrest.pado.index.result.ValueInfo;
import com.netcrest.pado.index.service.GridQuery;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class BizIndexServerResultsCollector implements ResultCollector<Serializable, BizScrollableResultSetImpl>
{
	protected BizScrollableResultSetImpl results;

	public BizIndexServerResultsCollector(GridQuery request)
	{
		results = new BizScrollableResultSetImpl(request, null);
	}

	protected BizIndexServerResultsCollector()
	{

	}

	@Override
	public synchronized BizScrollableResultSetImpl getResult() throws FunctionException
	{
		return results;
	}

	@Override
	public synchronized BizScrollableResultSetImpl getResult(long timeout, TimeUnit unit) throws FunctionException,
			InterruptedException
	{
		return results;
	}

	@Override
	public void addResult(DistributedMember memberID, Serializable resultOfSingleExecution)
	{
		IGridResults<ResultItem<Object>> gridResult = (IGridResults<ResultItem<Object>>) resultOfSingleExecution;
		if (gridResult == null) {
			return;
		}
		List list = gridResult.getAggregatedSortedResults();
		if (list == null) {
			return;
		}
		List<Object> resultList = new ArrayList<Object>(list.size());

		if (list != null && list.size() > 0) {
			for (ResultItem result : (List<ResultItem>) list) {
				resultList.add(((ValueInfo) result.getItem()).getValue());
			}
		}
		results.setTotalSize(gridResult.getTotalServerResults());
		results.setValueResultList(resultList);
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
