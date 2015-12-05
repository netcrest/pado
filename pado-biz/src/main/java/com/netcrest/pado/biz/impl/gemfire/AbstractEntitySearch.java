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
package com.netcrest.pado.biz.impl.gemfire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.internal.cache.LocalDataSet;
import com.netcrest.pado.index.exception.GridQueryException;
import com.netcrest.pado.index.gemfire.function.IEntitySearchFunction;
import com.netcrest.pado.index.helper.ComparatorFactory;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.internal.IndexMatrixUtil;
import com.netcrest.pado.index.internal.SearchResults;
import com.netcrest.pado.index.provider.IIndexMatrixProvider;
import com.netcrest.pado.index.provider.IndexMatrixProviderFactory;
import com.netcrest.pado.index.result.IMemberResults;
import com.netcrest.pado.index.result.MemberResults;
import com.netcrest.pado.index.result.ValueInfo;
import com.netcrest.pado.index.service.GridQuery;

public abstract class AbstractEntitySearch implements IEntitySearchFunction
{
	public IMemberResults execute(GridQuery criteria, final FunctionContext context) 
	{
		int bucketId = -1;
		IMemberResults memResults = null;

		// Get the cachedResults, if exists, return right back,
		// otherwise, do the query
		List resultList = getCachedResultSet(criteria);

		if (resultList != null) {
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
			memResults = makeResults(criteria, resultList);
		} else {
			resultList = queryLocal(criteria, context);
			if (resultList == null) {
				resultList = new ArrayList ();
			}
			bucketId = getBucketId(context);
			sort(criteria, resultList);
			cacheResultSet(criteria, resultList);
			memResults = makeResults(criteria, resultList);
		}
		memResults.setBucketId(bucketId);

		return memResults;
	}
	
	/**
	 * Auxiliary method for sorting the entities in the list using specified
	 * field <code>com.netcrest.grid.index.helper.ComparatorFactory</code>
	 * 
	 * @param criteria
	 * @param list
	 * @throws GridQueryException
	 */
	protected void sortWithComparator(GridQuery criteria, List list)
			throws GridQueryException {
		if (list != null && list.size() > 0 && criteria.getSortField() != null) {
			Comparator comparator = getComparator(criteria, list.get(0));
			if (comparator != null) {
				if (getLogger().fineEnabled()) {
					getLogger().fine("Sort with comparator");
				}
				Collections.sort(list, comparator);

				return;
			}
		}
		if (getLogger().infoEnabled()) {
			getLogger().info("Sort with no comparator");
		}
		Collections.sort(list);

	}

	protected Comparator getComparator(GridQuery criteria, Object entity)
			throws GridQueryException {
		IIndexMatrixProvider provider = IndexMatrixProviderFactory
				.getInstance().getProviderInstance(criteria.getProviderKey());
		return provider.getComparator(entity, criteria.getSortField(),
				criteria.isAscending(), criteria.isSortKey());

	}

	/**
	 * Transforms the sorted results to result objects, the list passed in
	 * contains sorted entities. If the criteria asks for key, the sortable keys
	 * should be retrieved, if it askes for entities, the entities should be
	 * returne therein. or keys depending on
	 * <code> criteria.isReturnKey () </code>
	 * 
	 * @param interimResult
	 * @return
	 */
	protected IMemberResults makeResults(GridQuery criteria, List resultsList) {
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
		int toIndex = fromIndex + criteria.getFetchSize() - 1;
		if (toIndex > resultsList.size()) {
			toIndex = resultsList.size() - 1;
		}

		// Set the total size of the results
		memberResults.setTotalSizeOnServer(resultsList.size());
		memberResults.setCurrentBatchIndexOnServer(fromIndex);

		int arraySize = toIndex - fromIndex + 1;
		if (arraySize > 0) {
			try {
				ArrayList<ValueInfo> list = new ArrayList<ValueInfo>(toIndex
						- fromIndex + 1);
				for (int i = fromIndex; i <= toIndex; i++) {
					Object v = resultsList.get(i);
					if (criteria.isReturnKey()) {
						list.add(new ValueInfo(makeKey(v, criteria), i));
					} else {
						list.add(new ValueInfo(transformEntity(v), i));
					}
				}
				memberResults.setResults(list);
				if (toIndex == resultsList.size() - 1) {
					memberResults
							.setNextBatchIndexOnServer(Constants.END_OF_LIST);
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
	public Object transformEntity(Object fromData) {
		return fromData;
	}

	/**
	 * Get sortable key using
	 * 
	 * @param object
	 * @param i
	 * @return
	 */
	protected Object makeKey(Object entity, GridQuery criteria) {
		if (criteria.getSortField() != null) {
			ComparatorFactory comparatorFactory = getComparatorFactory ();
			if (comparatorFactory != null) {
				Object field = comparatorFactory.getField(entity.getClass(),
						criteria.getSortField(), entity);
				if (field == null) {
					return entity;
				}
			}
		}
		return entity;
	}
	

	public void sort(final GridQuery criteria, final List list)
			throws GridQueryException {
//		if (criteria.isOrdered()) {
//			sortWithComparator(criteria, list);
//		}
	}	
	
	/**
	 * Get a comparatorFactory configured at cache startup
	 * @return
	 */
	protected ComparatorFactory getComparatorFactory () {
		return IndexMatrixProviderFactory.getInstance().getComparatorFactory();
	}

	/**
	 * Get a comparatorFactory configured at cache startup
	 * 
	 * @return
	 */

	@Override
	public List getCachedResultSet(GridQuery criteria) {
		Region resultsRegion = CacheFactory.getAnyInstance().getRegion(
				IndexMatrixUtil.getProperty(Constants.PROP_REGION_RESULTS));
		SearchResults searchResults = null;
		if (criteria.isForceRebuildIndex()) {
			resultsRegion.remove(criteria.getId());
		} else {
			searchResults = (SearchResults) resultsRegion
				.get(criteria.getId());
		}
		if (searchResults != null) {
			return searchResults.getResults();
		}
		return null;
	}

	@Override
	public void cacheResultSet(GridQuery criteria, List resultsList) {
		Region resultsRegion = CacheFactory.getAnyInstance().getRegion(
				IndexMatrixUtil.getProperty(Constants.PROP_REGION_RESULTS));
		SearchResults searchResults = new SearchResults();
		searchResults.setResults(resultsList);
		resultsRegion.put(criteria.getId(), searchResults);

	}

	protected LogWriter getLogger() {
		return CacheFactory.getAnyInstance().getLogger();
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
