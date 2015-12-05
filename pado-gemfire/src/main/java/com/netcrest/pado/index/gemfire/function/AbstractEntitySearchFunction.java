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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.gemfire.GemfirePadoServerManager;
import com.netcrest.pado.index.exception.GridQueryException;
import com.netcrest.pado.index.helper.ComparatorFactory;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.internal.IndexMatrix;
import com.netcrest.pado.index.internal.IndexMatrixUtil;
import com.netcrest.pado.index.internal.SearchResults;
import com.netcrest.pado.index.provider.IIndexMatrixProvider;
import com.netcrest.pado.index.provider.ITextSearchProvider;
import com.netcrest.pado.index.provider.IndexMatrixProviderFactory;
import com.netcrest.pado.index.provider.TextSearchProviderFactory;
import com.netcrest.pado.index.result.IMemberResults;
import com.netcrest.pado.index.result.MemberResults;
import com.netcrest.pado.index.result.ValueInfo;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.pql.CompiledUnit;
import com.netcrest.pado.pql.PqlParser;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class AbstractEntitySearchFunction implements IEntitySearchFunction, Function, Declarable
{
	private static final long serialVersionUID = 1L;

	// public static int pageSize = 10000;

	@Override
	public boolean hasResult()
	{
		return true;
	}

	@Override
	public void execute(final FunctionContext context)
	{
		GridQuery criteria = (GridQuery) context.getArguments();
		int bucketId = -1;
		IMemberResults memResults = null;

		// Get the cachedResults, if exists, return right back;
		// otherwise, do the query
		List resultList = getCachedResultSet(criteria);

		if (resultList != null) {
			if (context instanceof RegionFunctionContext) {
				RegionFunctionContext rfc = (RegionFunctionContext) context;
				Set<Integer> filterSet = (Set<Integer>) rfc.getFilter();
				if (filterSet != null) {
					for (Integer intFilter : filterSet) {
						bucketId = intFilter;
						break;
					}
				} else {
					bucketId = getBucketId(context);
				}
			} else {
				bucketId = getBucketId(context);
			}
			memResults = makeResults(criteria, resultList);
		} else {
			resultList = queryLocal(criteria, context);
			if (resultList == null) {
				resultList = new ArrayList();
			}
			bucketId = getBucketId(context);
			sort(criteria, resultList);
			
//			// DEBUG
//			SimpleDateFormat dataFormat = new SimpleDateFormat("yyyyMMdd");
//			long prevWrittenTime = -1;
//			Object prevItem = null;
//			for (int i = 0; i < resultList.size(); i++) {
//				TemporalEntry entry = (TemporalEntry)resultList.get(i);
//				Object item = entry.getTemporalData().getValue();
//				long writtenTime = entry.getTemporalKey().getWrittenTime();
//				if (prevItem != null && writtenTime < prevWrittenTime) {
//					System.out.println(i + ". Prev Item: " + dataFormat.format(new Date(writtenTime)) + ", " + prevItem);
//					System.out.println(i + ". Item: " + dataFormat.format(new Date(writtenTime)) + ", " + item);
//				}
//				prevWrittenTime = writtenTime;
//				prevItem = item;
//			}
//			
			cacheResultSet(criteria, resultList);
			memResults = makeResults(criteria, resultList);
		}
		memResults.setBucketId(bucketId);

		context.getResultSender().lastResult(memResults);
	}

	/**
	 * Auxiliary method for sorting the entities in the list using specified
	 * field <code>com.netcrest.grid.index.helper.ComparatorFactory</code>
	 * 
	 * @param criteria
	 * @param list
	 * @throws GridQueryException
	 */
	protected void sortWithComparator(GridQuery criteria, List list) throws GridQueryException
	{
		if (list != null && list.size() > 0 && criteria.getSortField() != null) {
			Comparator comparator = getComparator(criteria, list.get(0));
			if (comparator != null) {
				if (getLogger().fineEnabled()) {
					getLogger().fine("Sort with comparator");
				}
				Collections.sort(list, comparator);
				return;
			} else if (list.get(0) instanceof Comparable) {
				Collections.sort(list);
			}
		}
	}
	
	protected void sortWithScore(GridQuery criteria, List list) {
		// TODO Auto-generated method stub
		ITextSearchProvider provider = TextSearchProviderFactory.getInstance().getProvider(CompiledUnit.QueryLanguage.LUCENE, criteria);		
		provider.combineAndSort(list, criteria, true);
	}	

	protected Comparator getComparator(GridQuery criteria, Object entity) throws GridQueryException
	{
		IIndexMatrixProvider provider = IndexMatrixProviderFactory.getInstance().getProviderInstance(
				criteria.getProviderKey());
		return provider.getComparator(entity, criteria.getSortField(), criteria.isAscending(), criteria.isSortKey());

	}

	/**
	 * Transforms the sorted results to result objects, the list passed in
	 * contains sorted entities. If the criteria asks for key, the sortable keys
	 * should be retrieved, if it asks for entities, the entities should be
	 * returned therein or keys depending on
	 * <code> criteria.isReturnKey () </code>
	 * 
	 * @param interimResult
	 * @return
	 */
	protected IMemberResults makeResults(GridQuery criteria, List resultsList)
	{
		MemberResults memberResults = new MemberResults();

		// Determine fromIndex and toIndex
		if (resultsList == null || resultsList.size() == 0) {
			memberResults.setTotalSizeOnServer(0);
			memberResults.setCurrentBatchIndexOnServer(Constants.END_OF_LIST);
			memberResults.setNextBatchIndexOnServer(Constants.END_OF_LIST);
			memberResults.setResults(new ArrayList<ValueInfo>());
			return memberResults;
		}
		int fromIndex = criteria.getStartIndex();
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		// int toIndex = fromIndex + criteria.getFetchSize() - 1;
		// int toIndex = fromIndex + pageSize - 1;
		int toIndex = fromIndex + criteria.getAggregationPageSize() - 1;
		if (toIndex > resultsList.size()) {
			toIndex = resultsList.size() - 1;
		}

		// Set the total size of the results
		// if it is TopN, set totalSize to be the lower of the result and topN
		int totalReturnSize = resultsList.size();
		if (PqlParser.isTopN(criteria)) {
			totalReturnSize = (resultsList.size() <  criteria.getFetchSize() ? resultsList.size() : criteria.getFetchSize());
			memberResults.setTotalSizeOnServer(totalReturnSize);
		} else {
			memberResults.setTotalSizeOnServer(resultsList.size());
		}
		memberResults.setCurrentBatchIndexOnServer(fromIndex);

		int arraySize = toIndex - fromIndex + 1;
		if (arraySize > 0) {
			try {
				ArrayList<ValueInfo> list = new ArrayList<ValueInfo>(toIndex - fromIndex + 1);
				for (int i = fromIndex; i <= toIndex; i++) {
					Object v = resultsList.get(i);
					if (criteria.isReturnKey()) {
						list.add(new ValueInfo(makeKey(v, criteria), i));
					} else {
						list.add(new ValueInfo(transformEntity(v), i));
					}
				}
				memberResults.setResults(list);
				if (toIndex == totalReturnSize - 1) {
					memberResults.setNextBatchIndexOnServer(Constants.END_OF_LIST);
				} else {
					memberResults.setNextBatchIndexOnServer(toIndex + 1);
				}
			} catch (IndexOutOfBoundsException ex) {
				// ignore
			}
		} else {
			memberResults.setNextBatchIndexOnServer(Constants.END_OF_LIST);
		}
		return memberResults;
	}

	@Override
	public Object transformEntity(Object fromData)
	{
		return fromData;
	}

	/**
	 * Get sortable key using
	 * 
	 * @param object
	 * @param i
	 * @return
	 */
	protected Object makeKey(Object entity, GridQuery criteria)
	{
//		if (criteria.getSortField() != null) {
//			ComparatorFactory comparatorFactory = getComparatorFactory();
//			if (comparatorFactory != null) {
//				Object field = comparatorFactory.getField(entity.getClass(), criteria.getSortField(), entity);
//				if (field == null) {
//					return entity;
//				}
//			}
//		}
		return entity;
	}

	public void sort(final GridQuery criteria, final List list) throws GridQueryException
	{
		if (criteria.isOrdered()) {
			sortWithComparator(criteria, list);
		} else if (PqlParser.isTopN(criteria)) {
			sortWithScore (criteria, list);
		}
	}

	/**
	 * Get a comparatorFactory configured at cache startup
	 * 
	 * @return
	 */
	protected ComparatorFactory getComparatorFactory()
	{
		return IndexMatrixProviderFactory.getInstance().getComparatorFactory();
	}

	/**
	 * Get a comparatorFactory configured at cache startup
	 * 
	 * @return
	 */

	@Override
	public synchronized List getCachedResultSet(GridQuery criteria)
	{
		Region resultsRegion = CacheFactory.getAnyInstance().getRegion(
				IndexMatrixUtil.getProperty(Constants.PROP_REGION_RESULTS));
		SearchResults searchResults = null;
		if (criteria.isForceRebuildIndex()) {
			Region<String, IndexMatrix> indexRegion = CacheFactory.getAnyInstance().getRegion(IndexMatrixUtil.getProperty(Constants.PROP_REGION_INDEX));
			IndexMatrix indexMatrix = indexRegion.get(criteria.getId());
			if (indexMatrix != null && indexMatrix.isInProgress()) {
				searchResults = (SearchResults) resultsRegion.get(criteria.getId());
			} else {
				resultsRegion.remove(criteria.getId());
			}
		} else {
			searchResults = (SearchResults) resultsRegion.get(criteria.getId());
		}
		if (searchResults != null) {
			return searchResults.getResults();
		}
		return null;
	}

	@Override
	public void cacheResultSet(GridQuery criteria, List resultsList)
	{
		Region resultsRegion = CacheFactory.getAnyInstance().getRegion(
				IndexMatrixUtil.getProperty(Constants.PROP_REGION_RESULTS));
		SearchResults searchResults = new SearchResults();
		searchResults.setResults(resultsList);
		resultsRegion.put(criteria.getId(), searchResults);

	}

	protected LogWriter getLogger()
	{
		return CacheFactory.getAnyInstance().getLogger();
	}

	protected int getBucketId(FunctionContext context)
	{
		// if (context instanceof RegionFunctionContext) {
		// RegionFunctionContext rfc = (RegionFunctionContext) context;
		// LocalDataSet localDS = (LocalDataSet)
		// PartitionRegionHelper.getLocalPrimaryData(rfc.getDataSet());
		// for (int bucketId : localDS.getBucketSet()) {
		// return bucketId;
		// }
		// } else {
		// Use the bucket Id of an internal partitioned region, i.e.,
		// "/<grid>/__pado/server"
		// It is expected that subsequent index queries are performed
		// against this region
		// using onRegion.
		Region serverRegion = GemfirePadoServerManager.getPadoServerManager().getServerRegion();
		LocalDataSet localDS = (LocalDataSet) PartitionRegionHelper.getLocalPrimaryData(serverRegion);
		for (int bucketId : localDS.getBucketSet()) {
			return bucketId;
		}
		// }
		return -1;
	}

	@Override
	public boolean optimizeForWrite()
	{
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

}
