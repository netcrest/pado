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
package com.netcrest.pado.index.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.netcrest.pado.index.service.GridQuery;

public class IndexServerResultsCollector implements ResultCollector<Serializable, ClientResults>
{
	protected ClientResults results;
	
	public IndexServerResultsCollector(GridQuery request)
	{
		results = new ClientResults(request, null);
	}
	
	protected  IndexServerResultsCollector () {
		
	}

	public synchronized ClientResults getResult() throws FunctionException
	{
//		try {
//			wait(1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		return results;
	}

	public synchronized ClientResults getResult(long timeout, TimeUnit unit) throws FunctionException, InterruptedException
	{
//		try {
//			wait(1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		return results;
	}

	public void addResult(DistributedMember memberID, Serializable resultOfSingleExecution)
	{
		IGridResults<ResultItem<Object>> gridResult = (IGridResults<ResultItem<Object>>)resultOfSingleExecution;
		if (gridResult == null) {
			return;
		}
		List list = gridResult.getAggregatedSortedResults();
		if (list == null) {
			return;
		}
		List<Object> resultList = new ArrayList<Object> (list.size());
		
		if (list != null && list.size() > 0) {
			for (ResultItem result : (List<ResultItem>) list) {
				resultList.add(((ValueInfo)result.getItem()).getValue());
			}
		}
		results.setTotalSize(gridResult.getTotalServerResults());
		results.setValueResultList(resultList);
	}


	public synchronized void endResults()
	{
		results.commit();
		notifyAll();
	}

	public void clearResults()
	{
		results.clear();
	}
}
