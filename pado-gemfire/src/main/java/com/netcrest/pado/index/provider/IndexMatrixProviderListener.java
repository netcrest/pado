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
package com.netcrest.pado.index.provider;

import java.util.Map;

import com.netcrest.pado.index.gemfire.internal.GemfireIndexGridQuery;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.internal.IndexMatrix;
import com.netcrest.pado.index.internal.IndexMatrixManager;
import com.netcrest.pado.index.result.IGridResults;
import com.netcrest.pado.index.result.ResultItem;
import com.netcrest.pado.index.service.GridQuery;

public class IndexMatrixProviderListener implements IIndexMatrixProviderListener
{

	private IndexMatrix indexMatrix = null;

	private IIndexMatrixProvider provider = null;

	private final static int FETCH_SIZE = Integer.getInteger("IndexMatrixBuildFunction.fetchSize", 500);

	public IndexMatrixProviderListener(IIndexMatrixProvider provider)
	{
		this.provider = provider;
		provider.registerListener(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void onBatchArrived(final IGridResults<ResultItem<Object>> gridResults, final GridQuery criteria)
	{
		boolean firstBatch = false;
		if (!criteria.isReturnKey()) {
			// first batch, indexMatrix has not built yet
			//
			firstBatch = true;
		}
		boolean complete = gridResults.getNextResultIndex() == Constants.END_OF_LIST ? true : false;

		if (firstBatch && complete) {
			// No results
//			int[] bucketIds = provider.getBucketIds(gridResults);
			int[] bucketIds = gridResults.getBucketIds();
			indexMatrix = new IndexMatrix(bucketIds, gridResults.getTotalServerResults());
			indexMatrix.setPageSize(criteria.getAggregationPageSize());
			indexMatrix.begin();
			// indexMatrix.setTotalSize(gridResults.getTotalServerResults());
			// Create start indexes only if not complete. If completed, then
			// the commit method generates all remaining start indexes by
			// iterating thru the results.
//			if (complete == false) {
//				int[] startIndexes = provider.getStartIndexesForResults(bucketIds, gridResults);
//				indexMatrix.addStartIndexes(startIndexes);
//			}
			indexMatrix.setComplete(true);
			indexMatrix.commit(true, gridResults);
			getIndexMatrixRegion().put(criteria.getId(), indexMatrix);
			return;
		}

		// if (!firstBatch) {
		indexMatrix = (IndexMatrix) getIndexMatrixRegion().get(criteria.getId());
		// indexMatrix should not be null. Just in case...
		if (indexMatrix == null) {
//			int bucketIds[] = provider.getBucketIds(gridResults);
			int bucketIds[] = gridResults.getBucketIds();
			indexMatrix = new IndexMatrix(bucketIds, gridResults.getTotalServerResults());
			indexMatrix.setPageSize(criteria.getAggregationPageSize());
		}
		indexMatrix.begin();

		// Create start indexes only if not complete. If completed, then
		// the commit method generates all remaining start indexes by
		// iterating thru the results.
		if (complete == false) {
			int startIndexes[] = provider.getStartIndexesForResults(indexMatrix.getBucketIds(), gridResults);
			indexMatrix.addStartIndexes(startIndexes);
		}
		indexMatrix.commit(complete, gridResults);
		getIndexMatrixRegion().put(criteria.getId(), indexMatrix);
		// }

		if (!complete) {
			GridQuery newQuery = criteria;
			if (!criteria.isReturnKey()) {
				GemfireIndexGridQuery indexGridQuery = new GemfireIndexGridQuery();
				criteria.copyTo(indexGridQuery);
				indexGridQuery.setStartIndex(gridResults.getNextResultIndex());
				indexGridQuery.setReturnKey(true);
				indexGridQuery.setForceRebuildIndex(false);
				newQuery = indexGridQuery;
			} else {
				newQuery.setStartIndex(gridResults.getNextResultIndex());
			}
			newQuery.setFetchSize(FETCH_SIZE);
			IGridResults<ResultItem<Object>> newResults = provider.executeQuery(newQuery);
			provider.executeNextPageQuery(newResults, newQuery);
		}
	}

	@SuppressWarnings("rawtypes")
	protected synchronized Map getIndexMatrixRegion()
	{
		return IndexMatrixManager.getIndexMatrixManager().getIndexMatrixRegion();
	}

}
