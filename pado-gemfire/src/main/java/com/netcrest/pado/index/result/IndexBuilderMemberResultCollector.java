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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.provider.IIndexMatrixProvider;
import com.netcrest.pado.index.service.GridQuery;

/**
 * The MemberResultCollector used for IndexMatrix building
 */
public class IndexBuilderMemberResultCollector extends
		BaseMemberResultCollector {
	
	private ResultStaging resultStaging = null;
	
	/**
	 * Stage the sorted results after every run 
	 */
	public class ResultStaging {
		private ConcurrentHashMap<Integer, List> memberResultItemList = null;
		private Collection<IMemberResults> memResults = null;
		
		void setSortedResults (ConcurrentHashMap<Integer, List> results) {
				this.memberResultItemList = results;
		}
		void setMemberResults (Collection<IMemberResults> memResults) {
			this.memResults = memResults;
		}		
		 ConcurrentHashMap<Integer, List>  getSortedResults () {
			return this.memberResultItemList;
		}
		public Set<?> getNextSetFilters() {
			Set<Integer> nextBatchFilters = new HashSet<Integer> ();
			Collection<IMemberResults> memResults = memberResults.values();
			for (IMemberResults memResult : memResults) {
				if (memResult.getNextBatchIndexOnServer() != Constants.END_OF_LIST)	{
					nextBatchFilters.add(memResult.getBucketId());
				}
			}
			return nextBatchFilters;
		}			
	}

	public IndexBuilderMemberResultCollector(GridQuery query, IIndexMatrixProvider provider) {
		super(query, provider);
	}
	
	public IndexBuilderMemberResultCollector(GridQuery query, IIndexMatrixProvider provider, IndexBuilderMemberResultCollector lastRun) {
		super(query, provider);
		if (lastRun.getResultStaging () != null)
			setResultStaging(lastRun.getResultStaging());
	}	
	
	@Override
	public void clearResults() {
//		sortedResults.clear();
		super.clearResults();
		if (this.resultStaging != null) {
			memberResultItemList = this.resultStaging.getSortedResults();
		}		
	}	
	
	public void setResultStaging (ResultStaging staging) {
		this.resultStaging = staging;
		memberResultItemList = this.resultStaging.getSortedResults();
	}

	public ResultStaging getResultStaging () {
		return this.resultStaging;
	}
	
	public Set getFilter () {
		if (this.resultStaging != null)
			return resultStaging.getNextSetFilters();
		return null;
	}

	@Override
	public synchronized void endResults() {
		super.endResults();
		resultStaging = new ResultStaging();
		resultStaging.setSortedResults(memberResultItemList);
		resultStaging.setMemberResults(memberResults.values());
	}	


	@Override
	protected Object transformResultItem(Object obj, IMemberResults result, int seq) {
		ResultItem<Object> resultItem = new ResultItem<Object>();
		resultItem.setBucketId(result.getBucketId());
		resultItem.setItem(obj);
		resultItem.setResultIndex(result.getCurrentBatchIndexOnServer() + seq);
		return resultItem;
	}

	

//	@Override
//	protected void transformResultBatch(List batchResults) {
//		int start = query.getStartIndex();
//		int i = 0;
//		List<ResultItem<DataSerializable>> batched = (List<ResultItem<DataSerializable>>) batchResults;
//		for (ResultItem<DataSerializable> resultItem : batched) {			
//			//set the next batch start
//			setNextBatchStartIndexOnServer (resultItem, memberResults.get(resultItem.getBucketId()));
//		}
//	}
//
//	private void setNextBatchStartIndexOnServer(
//			ResultItem<DataSerializable> resultItem,
//			IMemberResults iMemberResults) {
//		//if return entity, then set the IMemberResults to point to the last result + 1 returned for server
//		//since the resultItem is sorted, after the iteration, the 
//		if (!query.isReturnKey()) {
//			if (iMemberResults.getTotalSizeOnServer() > 0) {
//				iMemberResults.setNextBatchIndexOnServer(iMemberResults.getNextBatchIndexOnServer() );
//			}
//		}
//	}
	
	
}
