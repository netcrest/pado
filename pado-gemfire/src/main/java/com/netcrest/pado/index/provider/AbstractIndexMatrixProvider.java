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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.netcrest.pado.gemfire.GemfireGridService;
import com.netcrest.pado.gemfire.GemfirePadoServerManager;
import com.netcrest.pado.gemfire.info.GemfireGridInfo;
import com.netcrest.pado.gemfire.info.GemfireRegionInfo;
import com.netcrest.pado.gemfire.util.GemfireGridUtil;
import com.netcrest.pado.index.exception.GridQueryException;
import com.netcrest.pado.index.gemfire.internal.GemfireIndexGridQuery;
import com.netcrest.pado.index.helper.BaseComparatorFactory;
import com.netcrest.pado.index.helper.FunctionExecutor.Realm;
import com.netcrest.pado.index.helper.IndexMatrixOperationUtility;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.internal.IndexMatrix;
import com.netcrest.pado.index.internal.IndexMatrixFilter;
import com.netcrest.pado.index.internal.IndexMatrixManager;
import com.netcrest.pado.index.result.BaseMemberResultCollector;
import com.netcrest.pado.index.result.IGridResults;
import com.netcrest.pado.index.result.IndexBuilderMemberResultCollector;
import com.netcrest.pado.index.result.ResultItem;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.info.BucketInfo;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.PathInfo;
import com.netcrest.pado.internal.impl.GridService;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.server.VirtualPathEngine;
import com.netcrest.pado.util.GridUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class AbstractIndexMatrixProvider implements IIndexMatrixProvider
{

	private final static int FETCH_SIZE = Integer.getInteger("IndexMatrixBuildFunction.fetchSize", 500);

	protected RegionService regionService;
	protected IndexBuilderMemberResultCollector lastRun = null;
	protected IndexBuilderMemberResultCollector entityRun = null;

	private List<IIndexMatrixProviderListener> listeners = new ArrayList<IIndexMatrixProviderListener>();
	private final static ExecutorService pool = Executors.newCachedThreadPool(new ThreadFactory() {
		public Thread newThread(Runnable r)
		{
			Thread t = new Thread(r, "Pado-IndexMatrixProviderCached");
			t.setDaemon(true);
			return t;
		}
	});
	protected volatile Properties providerProps = null;

	@Override
	public void registerListener(final IIndexMatrixProviderListener listner)
	{
		listeners.add(listner);
	}

	public void setRegionService(RegionService regionService)
	{
		this.regionService = regionService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int[] getStartIndexesForResults(int[] bucketIds, IGridResults<ResultItem<Object>> results)
	{
		if (results == null || bucketIds == null) {
			return null;
		}
		int[] startIndexes = new int[bucketIds.length];
		for (int i = 0; i < startIndexes.length; i++) {
			startIndexes[i] = -1;
		}
		List<ResultItem<Object>> resultItems = results.getAggregatedSortedResults();
		for (int i = 0; i < bucketIds.length; i++) {
			for (ResultItem<Object> resultItem : resultItems) {
				if (resultItem.getBucketId() == bucketIds[i]) {
					startIndexes[i] = resultItem.getResultIndex();
					break;
				}
			}
		}

		return startIndexes;
	}

	public List<IndexInfo> getIndexesForResults_org(IGridResults<ResultItem<Object>> results)
	{
		if (results == null) {
			return Collections.EMPTY_LIST;
		}
		List<ResultItem<Object>> resultItems = results.getAggregatedSortedResults();
		if (resultItems == null || resultItems.size() == 0) {
			return Collections.EMPTY_LIST;
		}
		List<IndexInfo> indexes = new ArrayList<IndexInfo>(resultItems.size());
		for (ResultItem<Object> resultItem : resultItems) {
			IndexInfo info = new IndexInfo();
			info.setBucketId(resultItem.getBucketId());
			info.setIndex(resultItem.getResultIndex());
			indexes.add(info);
		}
		return indexes;
	}

	@Override
	public IGridResults<Object> retrieveEntities(GridQuery query, IndexMatrix indexMatrix) throws GridQueryException
	{
		HashMap<Integer, IndexMatrixFilter> map = new HashMap<Integer, IndexMatrixFilter>();
		if (indexMatrix != null) {
			int bucketIds[] = indexMatrix.getBucketIds();
			int[] startIndexes = indexMatrix.getStartIndexes(query.getStartPageIndex());
			int[] nextStartIndexes = indexMatrix.getStartIndexes(query.getEndPageIndex() + 1);
			if (startIndexes == null) {
				// must wait till populated
			}
			if (nextStartIndexes == null) {
				// if reached the end of results then set to the limit
				nextStartIndexes = new int[startIndexes.length];
				for (int i = 0; i < nextStartIndexes.length; i++) {
					// over-commit the end index using the page size so that
					// we can get all remaining results
					if (startIndexes[i] != -1) {
						nextStartIndexes[i] = startIndexes[i]
								+ (query.getEndPageIndex() - query.getStartPageIndex() + 1) * indexMatrix.getPageSize();
					}
				}
			}
			for (int i = 0; i < bucketIds.length; i++) {
				int bucketId = bucketIds[i];
				IndexMatrixFilter filter = map.get(bucketId);
				if (filter == null) {
					int endIndex = nextStartIndexes[i] - 1;
					if (endIndex < 0) {
						endIndex = Integer.MAX_VALUE;
					}
					filter = new IndexMatrixFilter(query.getId(), bucketId, startIndexes[i], endIndex);
					map.put(bucketId, filter);
				}
			}
		}
		HashSet<IndexMatrixFilter> filterSet = new HashSet<IndexMatrixFilter>(map.values());
		BaseMemberResultCollector entityResultCollector = new BaseMemberResultCollector(query, this);

		ResultCollector rc = executeFunctionRetrieveEntities(query, filterSet, query, entityResultCollector);

		// return (IGridResults<Object>) entityResultCollector.getResult();
		return (IGridResults<Object>) rc.getResult();
	}

	@Override
	public Comparator<?> getComparator(Object result, String sortField, boolean ascending, boolean sortKey)
	{
		BaseComparatorFactory factory = IndexMatrixProviderFactory.getInstance().getComparatorFactory();
		if (factory != null) {
			return factory.getComparator(result, sortField, ascending, sortKey);
		}
		return null;
	}

	protected IGridResults executeQueryEntityRemote(GridQuery query) throws GridQueryException
	{
		IndexBuilderMemberResultCollector indexMemberResultCollector = new IndexBuilderMemberResultCollector(query,
				this);
		Set filterSet = null;
		Integer routingBucketId = (Integer) query.getParam("RoutingBucketId");
		if (routingBucketId != null) {
			filterSet = Collections.singleton(routingBucketId);
		}
		ResultCollector rc = executeFunctionQueryEntityRemote(query, filterSet, query, indexMemberResultCollector);
		entityRun = indexMemberResultCollector;
		// return indexMemberResultCollector.getResult();
		return (IGridResults) rc.getResult();
	}

	protected IGridResults executeQueryKeyRemote(GridQuery query) throws GridQueryException
	{
		IndexBuilderMemberResultCollector indexMemberResultCollector = null;
		Set filter = null;
		if (lastRun == null) {
			indexMemberResultCollector = new IndexBuilderMemberResultCollector(query, this, entityRun);
			filter = entityRun.getFilter();
		} else {
			indexMemberResultCollector = new IndexBuilderMemberResultCollector(query, this, lastRun);
			filter = indexMemberResultCollector.getFilter();
		}

		ResultCollector rc = executeFunctionQueryKeyRemote(query, filter, query, indexMemberResultCollector);
		lastRun = indexMemberResultCollector;
		IGridResults results = (IGridResults) rc.getResult();
		return results;
	}

	@Override
	public IGridResults<ResultItem<Object>> executeQuery(final GridQuery criteria) throws GridQueryException
	{
		IGridResults results = null;
		if (criteria.isReturnKey()) {
			results = executeQueryKeyRemote(criteria);
		} else {
			results = executeQueryEntityRemote(criteria);
		}
		return results;
	}

	@Override
	public void executeNextPageQuery(final IGridResults<ResultItem<Object>> results, final GridQuery criteria)
	{
		if (listeners.size() > 0) {
			for (final IIndexMatrixProviderListener listner : listeners) {
				pool.execute(new Runnable() {
					@Override
					public void run()
					{
						listner.onBatchArrived(results, criteria);
					}
				});
			}
		}
	}

	public void buildRemainingIndexMatrix(final IGridResults<ResultItem<Object>> gridResults, final GridQuery criteria)
	{
		boolean firstBatch = false;
		if (!criteria.isReturnKey()) {
			// first batch, indexMatrix has not built yet
			//
			firstBatch = true;
		}
		System.out.println("IndexMatrixProvider.onBatchArrived(): gridResults.getNextResultIndex()1="
				+ gridResults.getNextResultIndex() + ", hashCode=" + gridResults.hashCode());
		boolean complete = gridResults.getNextResultIndex() == Constants.END_OF_LIST ? true : false;
		System.out.println("IndexMatrixProvider.onBatchArrived(): gridResults.getNextResultIndex()2="
				+ gridResults.getNextResultIndex() + ", hashCode=" + gridResults.hashCode());

		IndexMatrix indexMatrix;
		if (firstBatch && complete) {
			// No results
			// int bucketIds[] = this.getBucketIds(gridResults);
			int[] bucketIds = gridResults.getBucketIds();
			indexMatrix = new IndexMatrix(bucketIds, gridResults.getTotalServerResults());
			indexMatrix.setPageSize(criteria.getAggregationPageSize());
			indexMatrix.begin();
			// indexMatrix.setTotalSize(gridResults.getTotalServerResults());

			// Create start indexes only if not complete. If completed, then
			// the commit method generates all remaining start indexes by
			// iterating thru the results.
			// if (complete == false) {
			// int startIndexes[] = this.getStartIndexesForResults(bucketIds,
			// gridResults);
			// indexMatrix.addStartIndexes(startIndexes);
			// }
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
				int startIndexes[] = this.getStartIndexesForResults(indexMatrix.getBucketIds(), gridResults);
				if (!firstBatch) {
					indexMatrix.addStartIndexes(startIndexes);
				}
			}
			indexMatrix.commit(complete, gridResults);
			System.out.println("IndexMatrixProviderListener.onBatchArrived(): indexMatrix.indexList.size()="
					+ indexMatrix.getIndexList().size() + ", complete=" + complete);
			getIndexMatrixRegion().put(criteria.getId(), indexMatrix);
		}

		if (!complete) {
			GridQuery newQuery = criteria;
			if (!criteria.isReturnKey()) {
				GemfireIndexGridQuery indexGridQuery = new GemfireIndexGridQuery();
				criteria.copyTo(indexGridQuery);
				indexGridQuery.setStartIndex(0);
				indexGridQuery.setReturnKey(true);
				indexGridQuery.setForceRebuildIndex(false);
				newQuery = indexGridQuery;
			} else {
				newQuery.setStartIndex(gridResults.getNextResultIndex());
			}
			newQuery.setFetchSize(FETCH_SIZE);
			this.executeQuery(newQuery);
		}
	}

	private synchronized Map getIndexMatrixRegion()
	{
		return IndexMatrixManager.getIndexMatrixManager().getIndexMatrixRegion();
	}

	protected Properties loadProps()
	{
		String name = "META-INF/services/" + getClass().getName() + ".properties";
		ClassLoader cl = getClass().getClassLoader();
		InputStream iss = null;
		Properties props = new Properties();

		try {
			URL propUrl = cl.getResource(name);
			if (propUrl != null) {
				iss = new BufferedInputStream(propUrl.openStream());
				props.load(iss);
				return props;
			}
			return null;
		} catch (Exception ex) {
			return null;
		} finally {
			if (iss != null) {
				try {
					iss.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected Region getRegion(GridQuery query)
	{
		return IndexMatrixOperationUtility.getRegionFromName(query.getFullPath(), regionService);
	}

	/**
	 * For client
	 * 
	 * @param gridService
	 * @param gridId
	 * @param fullPath
	 * @return
	 */
	protected Region getRegionClient(GridService gridService, String gridId, String fullPath)
	{
		RegionService regionService = ((GemfireGridService) gridService).getRegionService(gridId);
		if (regionService != null) {
			return regionService.getRegion(fullPath);
		} else {
			return CacheFactory.getAnyInstance().getRegion(fullPath);
		}
	}

	protected Region getServerRegion(GridService gridService, String gridId)
	{
		return getRegionClient(gridService, gridId, gridService.getRootPath(gridId) + "/__pado/server");
	}

	/**
	 * For client
	 * 
	 * @param gridService
	 * @param query
	 * @return
	 */
	protected Region getRegionClient(GridQuery query)
	{
		if (query.getGridService() == null) {
			return null;
		}
		if (query.getGridIds() != null && query.getGridIds().length == 1) {
			return getServerRegion(query.getGridService(), query.getGridIds()[0]);
		}
		return null;
	}

	private Random random = new Random();

	/**
	 * Not used. Primary BucketInfo list is not available. It is dynamically
	 * built from the client side.
	 * 
	 * @param gridQuery
	 * @return
	 */
	@SuppressWarnings("unused")
	private Set getInitialFilterSet(GridQuery gridQuery)
	{
		GemfireGridInfo gridInfo = (GemfireGridInfo) GemfirePadoServerManager.getPadoServerManager()
				.getGridInfoForFullPath(gridQuery.getFullPath());
		if (gridInfo != null) {
			PathInfo regionInfo = gridInfo.getCacheInfo().getPathInfo(gridQuery.getFullPath());
			if (((GemfireRegionInfo) regionInfo).isDataPolicyPartitionedRegion(false)) {
				return null;
			} else {
				BucketInfo bucketInfo = ((GemfireRegionInfo) gridInfo.getGridRegionInfo()).getPrimaryBucketInfoList()
						.get(random.nextInt(
								((GemfireRegionInfo) gridInfo.getGridRegionInfo()).getPrimaryBucketInfoList().size()));
				return Collections.singleton(bucketInfo.getBucketId());
			}
		}
		return null;
	}

	protected Realm getRealmForGrid(GridInfo gridInfo, GridQuery query)
	{
		Realm realm;
		if (gridInfo != null) {

			PathInfo pathInfo = gridInfo.getCacheInfo().getPathInfo(query.getFullPath());
			if (pathInfo == null) {
				String gridPath = GridUtil.getChildPath(query.getFullPath());
				if (VirtualPathEngine.getVirtualPathEngine().isEntityVirtualPath(gridPath)) {
					realm = Realm.ALL_MEMBERS;
//					DistributedMember member = GemfireGridUtil.getRandomDistributedMember();
//					query.setParameter("memberId", member.getId());
				} else if (VirtualPathEngine.getVirtualPathEngine().isVirtualPath(gridPath)) {
					realm = Realm.MEMBER;
//					DistributedMember member = GemfireGridUtil.getRandomDistributedMember();
//					query.setParameter("memberId", member.getId());
				} else {
					if (gridInfo.getGridId().equals(PadoServerManager.getPadoServerManager().getGridId())) {
						realm = Realm.MEMBER;
					} else {
						realm = Realm.SERVER;
					}
				}
			} else {
				GemfireRegionInfo regionInfo = (GemfireRegionInfo) pathInfo;
				if (PadoServerManager.getPadoServerManager().isPadoPath(query.getFullPath()) == false) {
					// if non-pado region
					if (gridInfo.getGridId().equals(PadoServerManager.getPadoServerManager().getGridId())) {
						// if query is for this grid
						if (regionInfo.isDataPolicyPartitionedRegion(false)) {
							realm = Realm.REGION;
						} else if (regionInfo.isScopeLocalRegion(false)) {
							realm = Realm.ALL_MEMBERS;
						} else {
							realm = Realm.MEMBER;
						}
					} else {
						// query is for remote grid
						if (regionInfo.isDataPolicyPartitionedRegion(false) || regionInfo.isScopeLocalRegion(false)) {
							realm = Realm.ALL_SERVERS;
						} else {
							realm = Realm.SERVER;
						}
					}

				} else {

					// if pado region
					if (regionInfo != null && regionInfo.isDataPolicyPartitionedRegion(false)) {
						realm = Realm.REGION;
					} else {
						if (gridInfo.getGridId().equals(PadoServerManager.getPadoServerManager().getGridId())) {
							realm = Realm.MEMBER;
						} else if (regionInfo.isScopeLocalRegion(false)) {
							realm = Realm.ALL_SERVERS;
						} else {
							realm = Realm.SERVER;
						}
					}
				}
			}
		} else {
			realm = Realm.UNDEFINED;
		}

		return realm;
	}

	protected Realm getRealmForGrid(GridQuery gridQuery)
	{
		Realm realm = Realm.UNDEFINED;
		if (gridQuery != null) {
			GemfireGridInfo gridInfo = (GemfireGridInfo) GemfirePadoServerManager.getPadoServerManager()
					.getGridInfoForFullPath(gridQuery.getFullPath());
			if (gridInfo == null) {

				// if non-pado region
				String gridIds[] = gridQuery.getGridIds();
				if (gridIds == null || gridIds.length == 0) {
					return realm;
				}
				String gridId = gridIds[0];
				gridInfo = (GemfireGridInfo) GemfirePadoServerManager.getPadoServerManager()
						.getGridInfoForGridId(gridId);
				if (gridInfo == null) {
					return realm;
				}
				PathInfo pathInfo = gridInfo.getCacheInfo().getPathInfo(gridQuery.getFullPath());
				GemfireRegionInfo regionInfo = (GemfireRegionInfo) pathInfo;
				if (gridInfo.getGridId().equals(PadoServerManager.getPadoServerManager().getGridId())) {
					// if query is for this grid
					if (regionInfo.isDataPolicyPartitionedRegion(false)) {
						realm = Realm.REGION;
					} else {
						realm = Realm.LOCAL;
					}
				} else {
					// query is for remote grid
					if (regionInfo.isDataPolicyPartitionedRegion(false)) {
						realm = Realm.ALL_SERVERS;
					} else {
						realm = Realm.SERVER;
					}
				}

			} else {

				// if pado region
				PathInfo pathInfo = gridInfo.getCacheInfo().getPathInfo(gridQuery.getFullPath());
				GemfireRegionInfo regionInfo = (GemfireRegionInfo) pathInfo;
				if (regionInfo != null) {
					if (regionInfo.isDataPolicyPartitionedRegion(false)) {
						realm = Realm.REGION;
					} else if (gridInfo.getGridId().equals(PadoServerManager.getPadoServerManager().getGridId())) {
						realm = Realm.LOCAL;
					} else {
						realm = Realm.SERVER;
					}
				}
			}
		}
		return realm;
	}

	protected Realm getRealmForClient(Region region)
	{
		Realm realm = Realm.REGION;
		return realm;
	}

	// Extension point for plugin local test provider
	protected abstract ResultCollector executeFunctionRetrieveEntities(GridQuery query, Set filterSet,
			Serializable argument, ResultCollector resultCollector);

	// Extension point for plugin local test provider
	protected abstract ResultCollector executeFunctionQueryKeyRemote(GridQuery query, Set filterSet,
			Serializable argument, ResultCollector resultCollector);

	// Extension point for plugin local test provider
	protected abstract ResultCollector executeFunctionQueryEntityRemote(GridQuery query, Set filterSet,
			Serializable argument, ResultCollector resultCollector);

}
