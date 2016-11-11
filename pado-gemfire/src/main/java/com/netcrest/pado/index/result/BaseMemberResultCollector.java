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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.internal.concurrent.ConcurrentHashSet;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.internal.GridResults;
import com.netcrest.pado.index.provider.IIndexMatrixProvider;
import com.netcrest.pado.index.provider.ITextSearchProvider;
import com.netcrest.pado.index.provider.TextSearchProviderFactory;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.pql.PqlParser;
import com.netcrest.pado.pql.CompiledUnit.QueryLanguage;

/**
 * Base impl class for MemberberResultCollector that does mergeSort.
 */
public class BaseMemberResultCollector implements IMemberResultCollector
{

	protected GridQuery query = null;

	protected IIndexMatrixProvider provider = null;

	protected volatile Comparator<?> comparator = null;

	protected GridResults gridResults = null;

	protected List mergedResults = null;

	// Map<bucketId, IMemberResults>
	protected ConcurrentHashMap<Integer, IMemberResults> memberResults = new ConcurrentHashMap<Integer, IMemberResults>();
	// Map<bucketId, List<ResultItem>>
	protected ConcurrentHashMap<Integer, List> memberResultItemList = new ConcurrentHashMap<Integer, List>();

	private ConcurrentHashSet<Integer> addResultInvokedSet = new ConcurrentHashSet<Integer>();
	
	public BaseMemberResultCollector(GridQuery query, IIndexMatrixProvider provider)
	{
		this.query = query;
		this.provider = provider;
	}

	@Override
	public void addResult(DistributedMember distMember, IMemberResults result)
	{
		if (addResultInvokedSet.contains(result.getBucketId())) {
			return;
		}
		
		if (result.getResults() != null && result.getResults().size() > 0) {
			memberResults.put(result.getBucketId(), result);

			List memberResultList = memberResultItemList.get(result.getBucketId());
			if (memberResultList == null) {
				memberResultList = new ArrayList(result.getTotalSizeOnServer());
				memberResultItemList.put(result.getBucketId(), memberResultList);
			}
			int i = 0;
			for (Object obj : result.getResults()) {
				memberResultList.add(transformResultItem(obj, result, i));
				i++;
			}
			
			addResultInvokedSet.add(result.getBucketId());
		}
	}

	protected Object transformResultItem(Object obj, IMemberResults result, int seq)
	{
		return obj;
	}

	protected void transformResultBatch(List batchResults)
	{
	}

	@Override
	public void clearResults()
	{
		// sortedResults.clear();
//		memberResultItemList.clear();
//		memberResults.clear();

	}

	boolean endResultsCompleted = false;

	@Override
	public void endResults()
	{
		if (endResultsCompleted) {
			return;
		}
		gridResults = new GridResults();
		
		// Set bucket Ids. Note that the very first memberResultItemList contains
		// all bucket Ids regardless whether there are data in the corresponding
		// servers. The subsequent memberResultItemList may not contain all 
		// bucket Ids depending on the remaining data fetch implementation.
		ArrayList<Integer> list = new ArrayList<Integer>(memberResultItemList.keySet());
		Collections.sort(list);
		int bucketIds[] = new int[list.size()];
		for (int i = 0; i < bucketIds.length; i++) {
			bucketIds[i] = list.get(i);
		}
		gridResults.setBucketIds(bucketIds);
		
		// get the total and set the next
		int total = 0;

		Collection<IMemberResults> memResults = memberResults.values();
		boolean noNext = true;
		for (IMemberResults memResult : memResults) {
			total = total + memResult.getTotalSizeOnServer();
			if (memResult.getNextBatchIndexOnServer() != Constants.END_OF_LIST) {
				noNext = false;
			}
		}
		gridResults.setTotalServerResults(total);
		if (noNext) {
			mergeResults(FULL_MERGE);
			gridResults.setNextResultIndex(Constants.END_OF_LIST);
		} else {
//			mergeResults(query.getFetchSize());
//			gridResults.setNextResultIndex(query.getStartIndex() + query.getFetchSize());
			
			int mergeResultSize = query.getAggregationPageSize() * (query.getEndPageIndex() - query.getStartPageIndex() + 1);
			mergeResults(mergeResultSize);
			gridResults.setNextResultIndex(query.getStartIndex() + mergeResultSize);
			
//			mergeResults(AbstractEntitySearchFunction.pageSize);
//			gridResults.setNextResultIndex(query.getStartIndex() + AbstractEntitySearchFunction.pageSize);
		}
		transformResultBatch(mergedResults);
		gridResults.setAggregatedSortedResults(mergedResults);
		endResultsCompleted = true;
	}

	@Override
	public IGridResults getResult() throws FunctionException
	{
		try {
			// introduced as a workaround to a GemFire bug in ResultCollector.getResult()
			// which does not block till all results are received.
			while (endResultsCompleted == false) {
				Thread.sleep(10);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return gridResults;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public IGridResults getResult(long arg0, TimeUnit arg1) throws FunctionException, InterruptedException
	{
		try {
			// introduced as a workaround to a GemFire bug in ResultCollector.getResult()
			// which does not block till all results are received.
			while (endResultsCompleted == false) {
				Thread.sleep(10);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return gridResults;
	}

	@SuppressWarnings("rawtypes")
	protected Comparator getComparator(Object value)
	{
		if (query.isOrdered() && this.comparator == null) {
			synchronized (this) {
				if (this.comparator == null) {
					Class compClass = value.getClass();
					if (ResultItem.class.isAssignableFrom(value.getClass())) {
						compClass = ((ResultItem) value).getItem().getClass();
					}
					if (ValueInfo.class.isAssignableFrom(compClass)) {
						compClass = ((ValueInfo) ((ResultItem) value).getItem()).getValue().getClass();
					}
					comparator = provider
							.getComparator(value, query.getSortField(), query.isAscending(), query.isSortKey());
				}
			}
		}
		return this.comparator;
	}

	/**
	 * Merges results up to the specified size.
	 * @param Size of the merged results
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void mergeResults(int size)
	{
		// cursors: <bucketID, nextIndex>
		Map<Integer, Integer> cursors = new HashMap<Integer, Integer>(memberResults.size());
		for (Map.Entry<Integer, List> mapEntry : memberResultItemList.entrySet()) {
			cursors.put(mapEntry.getKey(), Integer.valueOf(0));
		}
		int returnSize = size;

		int total = 0;
		for (Map.Entry<Integer, List> mapEntry : memberResultItemList.entrySet()) {
			total = total + mapEntry.getValue().size();
		}
		if ((returnSize != FULL_MERGE && returnSize > total) || returnSize == FULL_MERGE)
			returnSize = total;

		List sortedResults = new ArrayList(returnSize);

		// Determine if it's comparable
		boolean isComparable = false;
		for (Map.Entry<Integer, List> mapEntry : memberResultItemList.entrySet()) {
			if (!mapEntry.getValue().isEmpty()) {
				for (Object o : mapEntry.getValue()) {
					isComparable = (o instanceof Comparable);
					break;
				}
			}
			if (isComparable) {
				break;
			}
		}
		
		//if it is TopN 
		//For topN, second sort on score is required
		if (PqlParser.isTopN(query)) {
			
			List<IndexableResult<Integer, Object>> toBeSortedResults = new ArrayList<IndexableResult<Integer,Object>>();
			ITextSearchProvider textProvider = TextSearchProviderFactory.getInstance().getProvider(QueryLanguage.LUCENE, query);
			if (textProvider == null) {
				throw new RuntimeException ("TopN query needs a textProvider");
			}
			for (Map.Entry<Integer, List> mapEntry : memberResultItemList.entrySet()) {
				if (!mapEntry.getValue().isEmpty()) {
					for (Object o : mapEntry.getValue()) {
						//For topN, second sort on score is required
						IndexableResult<Integer, Object> indexableResult = new IndexableResult<Integer, Object>(mapEntry.getKey(), o);
						toBeSortedResults.add(indexableResult);
					}
				}
			}
			List<IndexableResult<Integer, Object>> afterSorted = (List<IndexableResult<Integer, Object>>) textProvider.combineAndSort(toBeSortedResults, query, false);
			int i = 0;
			for (IndexableResult<Integer, Object> indexable : afterSorted) {	
				
				sortedResults.add(indexable.getValue());
				int cursor = cursors.get(indexable.getKey());
				cursors.put(indexable.getKey(), ++cursor);
				i++;
				if (i >= returnSize)
					break;
			}
		} else if (!query.isOrdered() || query.getSortField() == null || query.getSortField().length() == 0 || isComparable == false) { //no orderBy
			int i = 0;
			for (Map.Entry<Integer, List> mapEntry : memberResultItemList.entrySet()) {
				if (!mapEntry.getValue().isEmpty()) {
					for (Object o : mapEntry.getValue()) {
						//For topN, second sort on score is required
						sortedResults.add(o);
						int cursor = cursors.get(mapEntry.getKey());
						cursors.put(mapEntry.getKey(), ++cursor);
						i++;
						if (i >= returnSize)
							break;
					}
				}

				if (i >= returnSize)
					break;
			}
		} else {
			
			// Debug only
//			for (Map.Entry<Integer, List> mapEntry : memberResultItemList.entrySet()) {
//				List<ResultItem> list = mapEntry.getValue();
//				System.out.println(mapEntry.getKey());
//				System.out.println("-------------------------------------");
//				long prevStartValidTime = -1;
//				int i = 0;
//				for (ResultItem ri : list) {
//					ValueInfo vi = (ValueInfo)ri.getItem();
//					TemporalEntry te = (TemporalEntry)vi.getValue();
//					long startValidTime = te.getTemporalKey().getStartValidTime();
//					if (prevStartValidTime != -1 && prevStartValidTime > startValidTime) {
//						System.out.println(i + ". Error (before sort) Previous: " + prevStartValidTime + ", Current: " + startValidTime);
//					}
//					i++;
//					prevStartValidTime = startValidTime;
//				}
//				System.out.println();
//				System.out.flush();
//			}
//			System.out.println();
			
			long prevStartValidTime = -1;
			for (int i = 0; i < returnSize; i++) {
				Object lowestValue = null;
				int lowestValueBucketId = -1;
				// <bucketId, List>
				for (Map.Entry<Integer, List> mapEntry : memberResultItemList.entrySet()) {
					int cursor = cursors.get(mapEntry.getKey());
					if (mapEntry.getValue().isEmpty() || mapEntry.getValue().size() <= cursor) {
						continue;
					}
					Object element = mapEntry.getValue().get(cursor);
					
					// Internal comparators handles the sort order such that
					// the following compare() call returns > if lowestValue 
					// < element and sort order is descending.
					if (lowestValue == null || compare(lowestValue, element) > 0) {
						lowestValue = element;
						lowestValueBucketId = mapEntry.getKey();
					}
				}
				if (lowestValue == null) {
					break;
				} else {
					int cursor = cursors.get(lowestValueBucketId);
					cursors.put(lowestValueBucketId, ++cursor);
				}
				
				// Debug only
//				ResultItem ri = (ResultItem)lowestValue;
//				ValueInfo vi = (ValueInfo)ri.getItem();
//				TemporalEntry te = (TemporalEntry)vi.getValue();
//				long startValidTime = te.getTemporalKey().getStartValidTime();
//				if (prevStartValidTime != -1 && prevStartValidTime > startValidTime) {
//					System.out.println(i + ". Error (while sorting) Previous: " + prevStartValidTime + ", Current: " + startValidTime);
//				}
//				prevStartValidTime = startValidTime;
				
				sortedResults.add(lowestValue);
			}
		}
		mergedResults = sortedResults;
		
		// Debug only
//		long prevStartValidTime = -1;
//		for (int i = 0; i < mergedResults.size(); i++) {
//			ResultItem item = (ResultItem)mergedResults.get(i);
//			ValueInfo vi = (ValueInfo)item.getItem();
//			TemporalEntry te = (TemporalEntry)vi.getValue();
//			long startValidTime = te.getTemporalKey().getStartValidTime();
//			if (prevStartValidTime != -1 && prevStartValidTime > startValidTime) {
//				System.out.println(i + ". Error! Previous: " + prevStartValidTime + ", Current: " + startValidTime);
//			}
//			prevStartValidTime = startValidTime;
//		}

		// <bucketId, List>
		for (Map.Entry<Integer, List> mapEntry : memberResultItemList.entrySet()) {
			int cursor = cursors.get(mapEntry.getKey());
			mapEntry.getValue().subList(0, cursor).clear();
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private int compare(Object lowest, Object element)
	{
		Comparator comparator = getComparator(lowest);
		if (comparator == null && query.isOrdered() && lowest instanceof Comparable && element instanceof Comparable) {
			return ((Comparable) lowest).compareTo((Comparable) element);
		} else if (comparator == null) {
			return 1;
		} else {
			return comparator.compare(lowest, element);
		}
	}
}
