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
package com.netcrest.pado.index.gemfire.function;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.internal.cache.LocalDataSet;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.internal.IndexMatrixFilter;
import com.netcrest.pado.index.internal.IndexMatrixUtil;
import com.netcrest.pado.index.internal.SearchResults;
import com.netcrest.pado.index.result.MemberResults;

/**
 * This Function provides retrieval of entities from ResultSet cache on each Data Server
 * 
 */
public class EntitySearchCacheRetrieveBaseFunction implements Function, Declarable
{
	private static final long serialVersionUID = 1L;
	
	public final static String Id = EntitySearchCacheRetrieveBaseFunction.class.getSimpleName();

	@Override
	public boolean hasResult()
	{
		return true;
	}
	
	//Extension point to plugin test Function
	protected LogWriter getLogger () {
		return CacheFactory.getAnyInstance().getLogger();
	}	

	@Override
	public void execute(FunctionContext context)
	{
		Set<IndexMatrixFilter> filterSet = null;
		if (context instanceof RegionFunctionContext) {
			RegionFunctionContext rfc = (RegionFunctionContext) context;
			filterSet = (Set<IndexMatrixFilter>) rfc.getFilter();			
		} else  {
			Method method;
			try {
				method = context.getClass().getMethod("getFilter", null);
				filterSet = (Set<IndexMatrixFilter>) method.invoke(context, null);
			} catch (Exception e) {
				getLogger ().warning(e.getMessage() + " The call is not configured for the Region ");
			} 
			
		}

		IndexMatrixFilter filter = null;
		for (IndexMatrixFilter indexMatrixFilter : filterSet) {
			filter = indexMatrixFilter;
			break;
		}
		search(context, filter);
	} 
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void search(FunctionContext rfc, IndexMatrixFilter filter)
	{
		MemberResults results = new MemberResults();		
		results.setBucketId(getBucketId (rfc));
		if (filter == null) {
			rfc.getResultSender().lastResult(results);
			return;
		}
	
		SearchResults searchResults = getSearchResults(filter);		
		if (searchResults == null) {
			rfc.getResultSender().lastResult(results);
			return;
		}
		results.setCurrentBatchIndexOnServer(filter.getStartIndex());
		results.setTotalSizeOnServer(searchResults.getResults().size());
		if (filter.getEndIndex() >= searchResults.getResults().size() - 1) {
			results.setNextBatchIndexOnServer(Constants.END_OF_LIST);
		} else {
			results.setNextBatchIndexOnServer(filter.getEndIndex() + 1);
		}
		List resultList = searchResults.getResults();
		int startIndex = filter.getStartIndex();
		int endIndex = filter.getEndIndex();
		List list;
		if (startIndex < 0 || startIndex >= resultList.size()) {
			list = new ArrayList(0);
		} else {
			if (endIndex >= resultList.size()) {
				endIndex = resultList.size() - 1;
			}
			list = new ArrayList(endIndex - startIndex + 1);
			for (int i = startIndex; i <= endIndex; i++) {
				list.add(transformEntity(resultList.get(i)));
			}
		}
		results.setResults(list);

		rfc.getResultSender().lastResult(results);
	}
	
	//Extension point for local Test provider
	protected SearchResults getSearchResults (IndexMatrixFilter filter) {
		Region<Object, SearchResults> resultsRegion = CacheFactory.getAnyInstance().getRegion(IndexMatrixUtil.getProperty(Constants.PROP_REGION_RESULTS));
		SearchResults searchResults = resultsRegion.get(filter.getId());
		return searchResults;
	}

	@Override
	public String getId()
	{
		return Id;
	}
	
	protected Object transformEntity(Object fromData) {
		return fromData;
	}	

	@Override
	public boolean optimizeForWrite() {
		return true;
	}

	@Override
	public boolean isHA()
	{
		return false;
	}

	@Override
	public void init(Properties props)
	{
	}
	
	protected int getBucketId(FunctionContext context) {
		RegionFunctionContext rfc = (RegionFunctionContext) context;
		LocalDataSet localDS = (LocalDataSet) PartitionRegionHelper
				.getLocalPrimaryData(rfc.getDataSet());
		for (int bucketId : localDS.getBucketSet()) {
			return bucketId;
		}
		return -1;
	}

}
