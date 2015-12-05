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

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.netcrest.pado.index.exception.GridQueryException;
import com.netcrest.pado.index.exception.GridQueryProviderConfigurationException;
import com.netcrest.pado.index.gemfire.internal.GemfireIndexGridQuery;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.internal.IndexMatrix;
import com.netcrest.pado.index.internal.IndexMatrixManager;
import com.netcrest.pado.index.internal.IndexMatrixUtil;
import com.netcrest.pado.index.provider.IIndexMatrixProvider;
import com.netcrest.pado.index.provider.IIndexMatrixProviderListener;
import com.netcrest.pado.index.provider.IndexMatrixOQLProvider;
import com.netcrest.pado.index.provider.IndexMatrixProviderFactory;
import com.netcrest.pado.index.provider.IndexMatrixProviderListener;
import com.netcrest.pado.index.result.IGridResults;
import com.netcrest.pado.index.result.ResultItem;
import com.netcrest.pado.index.service.GridQuery;

public class IndexMatrixBuildFunction implements Function, Declarable
{
	private static final long serialVersionUID = 1L;

	public final static String Id = "IndexMatrixBuildFunction";

	private final static int FETCH_SIZE = Integer.getInteger("IndexMatrixBuildFunction.fetchSize",
			Constants.DEFAULT_INDEX_FETCH_SIZE);

	private static IndexMatrixProviderFactory indexMatrixProviderFactory = IndexMatrixProviderFactory.getInstance();

	@Override
	public boolean hasResult()
	{
		return true;
	}

	@Override
	public void execute(FunctionContext context)
	{
		GridQuery criteria = (GridQuery) context.getArguments();
		// Lock lock = getLock(criteria);
		// lock.lock();
		try {
			IndexMatrix indexMatrix = getIndexMatrix(criteria);
			if (indexMatrix == null) {
				// Use Provider
				IIndexMatrixProvider provider = indexMatrixProviderFactory.getProviderInstance(criteria
						.getProviderKey());
				if (provider == null) {
					String msg = "Provider : " + criteria.getProviderKey() + " can not be found";
					getLogger().error(msg);
					throw new GridQueryProviderConfigurationException(msg);
				}
				try {
					createListener(provider);
					IGridResults<ResultItem<Object>> gridResults = provider.executeQuery(criteria);
					context.getResultSender().lastResult(gridResults);
					provider.executeNextPageQuery(gridResults, criteria);
					gridResults = null; // for gc
//					pool.execute(new BuildRemainingIndexMatrix(gridResults, criteria, provider));

					// Use the larger fetch size
				} catch (GridQueryException ex) {
					throw ex;
				} catch (Exception ex) {
					throw new GridQueryException(ex);
				}
			} else {
				context.getResultSender().lastResult(null);
			}
		} finally {
			// lock.unlock();
		}
	}

	protected IndexMatrix getIndexMatrix(GridQuery criteria)
	{
		IndexMatrixManager manager = IndexMatrixManager.getIndexMatrixManager();
		IndexMatrix indexMatrix = manager.getIndexMatrix(criteria.getId());
		return indexMatrix;
	}

	// Extension point to plugin test Function
	protected LogWriter getLogger()
	{
		return CacheFactory.getAnyInstance().getLogger();
	}

	protected IIndexMatrixProviderListener createListener(IIndexMatrixProvider provider)
	{
		return new IndexMatrixProviderListener(provider);
	}

	// Extension point to plugin test Function
	protected Lock getLock(GridQuery criteria)
	{
		Region systemRegion = CacheFactory.getAnyInstance().getRegion(
				IndexMatrixUtil.getProperty(Constants.PROP_REGION_SYSTEM));
		Lock lock = systemRegion.getDistributedLock(criteria.getId());
		return lock;
	}

	@Override
	public String getId()
	{
		return Id;
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

	class BuildRemainingIndexMatrix implements Runnable
	{
		private IGridResults<ResultItem<Object>> gridResults;
		private GridQuery criteria;
		private IIndexMatrixProvider provider;

		BuildRemainingIndexMatrix(IGridResults<ResultItem<Object>> gridResults, GridQuery criteria,
				IIndexMatrixProvider provider)
		{
			this.gridResults = gridResults;
			this.criteria = criteria;
			this.provider = provider;
		}

		public void run()
		{
			try {
				buildRemainingIndexMatrix();
			} catch (Exception ex) {
				CacheFactory
						.getAnyInstance()
						.getLogger()
						.warning(
								"Exception raised while building the remaining index matrix for "
										+ criteria.getQueryString(), ex);
			}
		}

		private void buildRemainingIndexMatrix()
		{
			IGridResults<ResultItem<Object>> gridResults = this.gridResults;
			GridQuery criteria = this.criteria;
			boolean complete = gridResults.getNextResultIndex() == Constants.END_OF_LIST ? true : false;
			while (complete == false) {
				boolean firstBatch = !criteria.isReturnKey();

				IndexMatrix indexMatrix;
				if (firstBatch && complete) {
					// No results
//					int[] bucketIds = provider.getBucketIds(gridResults);
					int[] bucketIds = gridResults.getBucketIds();
					indexMatrix = new IndexMatrix(bucketIds, gridResults.getTotalServerResults());
					indexMatrix.setPageSize(criteria.getAggregationPageSize());
					indexMatrix.begin();
					// indexMatrix.setTotalSize(gridResults.getTotalServerResults());
					// Create start indexes only if not complete. If completed,
					// then
					// the commit method generates all remaining start indexes
					// by
					// iterating thru the results.
//					if (complete == false) {
//						int[] startIndexes = provider.getStartIndexesForResults(bucketIds, gridResults);
//						indexMatrix.addStartIndexes(startIndexes);
//					}
					indexMatrix.setComplete(true);
					indexMatrix.commit(true, gridResults);
					getIndexMatrixRegion().put(criteria.getId(), indexMatrix);
				} else if (firstBatch == false) {
					indexMatrix = (IndexMatrix) getIndexMatrixRegion().get(criteria.getId());
					// indexMatrix should not be null. Just in case...
					if (indexMatrix == null) {
						int[] bucketIds = gridResults.getBucketIds();
						indexMatrix = new IndexMatrix(bucketIds, gridResults.getTotalServerResults());
						// indexMatrix.setTotalSize(gridResults.getTotalServerResults());
					}
					indexMatrix.begin();
					// Create start indexes only if not complete. If completed,
					// then
					// the commit method generates all remaining start indexes
					// by
					// iterating thru the results.
					if (complete == false) {
						int startIndexes[] = provider
								.getStartIndexesForResults(indexMatrix.getBucketIds(), gridResults);
						if (!firstBatch) {
							indexMatrix.addStartIndexes(startIndexes);
						}
					}
					indexMatrix.commit(complete, gridResults);
					getIndexMatrixRegion().put(criteria.getId(), indexMatrix);
				}

				if (!complete) {
					criteria = getNextCriteria(criteria);
					gridResults = ((IndexMatrixOQLProvider) provider).executeQuery(criteria);
					if (gridResults == null) {
						complete = true;
					} else {
						complete = gridResults.getNextResultIndex() == Constants.END_OF_LIST ? true : false;
					}
				}
			}
		}

		private GridQuery getNextCriteria(GridQuery criteria)
		{
			GridQuery newCriteria = criteria;
			if (!criteria.isReturnKey()) {
				GemfireIndexGridQuery indexGridQuery = new GemfireIndexGridQuery();
				criteria.copyTo(indexGridQuery);
				indexGridQuery.setStartIndex(0);
				indexGridQuery.setReturnKey(true);
				indexGridQuery.setForceRebuildIndex(false);
				newCriteria = indexGridQuery;
			} else {
				newCriteria.setStartIndex(gridResults.getNextResultIndex());
			}
//			newCriteria.setFetchSize(FETCH_SIZE);
			return newCriteria;
		}

		public synchronized void buildRemainingIndexMatrix(final IGridResults<ResultItem<Object>> gridResults,
				final GridQuery criteria)
		{
			boolean firstBatch = false;
			if (!criteria.isReturnKey()) {
				// first batch, indexMatrix has not built yet
				//
				firstBatch = true;
			}
			boolean complete = gridResults.getNextResultIndex() == Constants.END_OF_LIST ? true : false;

			IndexMatrix indexMatrix;
			if (firstBatch && complete) {
				// No results
				int[] bucketIds = gridResults.getBucketIds();
				indexMatrix = new IndexMatrix(bucketIds, gridResults.getTotalServerResults());
				indexMatrix.setPageSize(criteria.getAggregationPageSize());
				indexMatrix.begin();
				// indexMatrix.setTotalSize(gridResults.getTotalServerResults());
				// Create start indexes only if not complete. If completed, then
				// the commit method generates all remaining start indexes by
				// iterating thru the results.
//				if (complete == false) {
//					int[] startIndexes = provider.getStartIndexesForResults(bucketIds, gridResults);
//					indexMatrix.addStartIndexes(startIndexes);
//				}
				indexMatrix.setComplete(true);
				indexMatrix.commit(true, gridResults);
				getIndexMatrixRegion().put(criteria.getId(), indexMatrix);
				return;
			}

			if (!firstBatch) {
				indexMatrix = (IndexMatrix) getIndexMatrixRegion().get(criteria.getId());
				// indexMatrix should not be null. Just in case...
				if (indexMatrix == null) {
					int[] bucketIds = gridResults.getBucketIds();
					indexMatrix = new IndexMatrix(bucketIds, gridResults.getTotalServerResults());
					// indexMatrix.setTotalSize(gridResults.getTotalServerResults());
				}
				indexMatrix.begin();
				// Create start indexes only if not complete. If completed, then
				// the commit method generates all remaining start indexes by
				// iterating thru the results.
				if (complete == false) {
					int startIndexes[] = provider.getStartIndexesForResults(indexMatrix.getBucketIds(), gridResults);
					if (!firstBatch) {
						indexMatrix.addStartIndexes(startIndexes);
					}
				}
				indexMatrix.commit(complete, gridResults);
				getIndexMatrixRegion().put(criteria.getId(), indexMatrix);
			}

			if (!complete) {
				GridQuery newCriteria = criteria;
				if (!criteria.isReturnKey()) {
					GemfireIndexGridQuery indexGridQuery = new GemfireIndexGridQuery();
					criteria.copyTo(indexGridQuery);
					indexGridQuery.setStartIndex(0);
					indexGridQuery.setReturnKey(true);
					indexGridQuery.setForceRebuildIndex(false);
					newCriteria = indexGridQuery;
				} else {
					newCriteria.setStartIndex(gridResults.getNextResultIndex());
				}
//				newCriteria.setFetchSize(FETCH_SIZE);
				provider.executeQuery(newCriteria);
			}
		}

		private synchronized Map getIndexMatrixRegion()
		{
			return IndexMatrixManager.getIndexMatrixManager().getIndexMatrixRegion();
		}
	}
}
