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

import java.util.Comparator;

import com.gemstone.gemfire.cache.RegionService;
import com.netcrest.pado.index.exception.GridQueryException;
import com.netcrest.pado.index.internal.IndexMatrix;
import com.netcrest.pado.index.result.IGridResults;
import com.netcrest.pado.index.result.ResultItem;
import com.netcrest.pado.index.service.GridQuery;

/**
 * The Provider hook to integrate specialized search (Lucene, OQL, etc) with
 * IndexMatrix creation and ResultSet scrolling. The various implementations of
 * this interface are discovered and registered by
 * <code>IndexMatrixProviderFactory</code> using Service API standards The
 * Services API will look for a classname in the file
 * <code>META-INF/services/com.netcrest.grid.index.IndexMatrixProvider</code> in
 * the jars in runtime classloader
 * 
 * @see IndexMatrixProviderFactory
 * 
 */
public interface IIndexMatrixProvider
{

	/**
	 * Provider Id to register this provider with
	 * <code>IndexMatrixProviderFactory</code>
	 * 
	 * @return
	 */
	String getProviderId();

	/**
	 * Sets RegionService for those apps that require multi-user authentication.
	 * This method is invoked automatically by the underlying IndexMatrix
	 * mechanism when it creates the provider. The passed-in region service may
	 * be null if RegionService is not required. The provider is required to use
	 * the region service for all GemFire specific calls that depend on
	 * client/server security.
	 * 
	 * @param regionService
	 *            GemFire region service
	 */
	void setRegionService(RegionService regionService);

	/**
	 * Called by IndexMatrixBuildFunction to registers listener to drive the
	 * completion of IndexMatrix creation
	 * 
	 * @param listner
	 */
	void registerListener(IIndexMatrixProviderListener listner);

	/**
	 * Called by IndexMatrixBuildFunction to execute the query.
	 * <p>
	 * On GridIndexServer, the query for the first batch of IGridResults is
	 * entity query, <code>query.isReturnKey()</code> = false, implementation of
	 * this method should return entities for IGridResults.
	 * <p>
	 * If IGridResults returns size == query.fetchSize(). This method is called
	 * again by IIndexMatrixProviderListener for the next batch using
	 * <code>query.isReturnKey()</code> = true, implementation class should only
	 * return Sortable keys of the entities for IGridResult when
	 * <code>query.isReturnKey()</code> = true, this continues until
	 * IGridResults returns size < query.fetchSize()
	 * </p>
	 * <p>
	 * <b>Important: Implementation of this method should call onBatchArrived on
	 * registered IIndexMatrixProviderListener before returning IGridResults</b>
	 * </p>
	 * 
	 * @param query
	 *            Criteria represents the client request
	 */
	IGridResults<ResultItem<Object>> executeQuery(GridQuery query) throws GridQueryException;

	void executeNextPageQuery(IGridResults<ResultItem<Object>> results, GridQuery query);

	/**
	 * Called by IIndexMatrixProviderListener to get the list of start indexes
	 * for the batch results, the list is used to update IndexMatrix
	 * 
	 * @param results
	 *            Results received from executing the remote call.
	 */
	int[] getStartIndexesForResults(int[] bucketIds, IGridResults<ResultItem<Object>> results);

	/**
	 * Called by IIndexMatrixProviderListener to get the list of indexes for the
	 * batch results, the list is used to update IndexMatrix
	 * 
	 * @param results
	 */
	// List<IndexInfo> getIndexesForResults (IGridResults<ResultItem<Object>>
	// results);

	/**
	 * Called by ClientResults.nextSet to retrieve entities after IndexMatrix is
	 * built
	 * 
	 * @param query
	 *            The query that is used to create the IndexMatrix
	 * @return
	 */
	IGridResults<Object> retrieveEntities(GridQuery query, IndexMatrix indexMatrix) throws GridQueryException;

	/**
	 * Returns a comparator used to sort the <code>resultClass</code>, null is
	 * an accepted value. If null is returned, and the query requires an ordered
	 * ResultSet, then the ResultSet can only include entities or keys that are
	 * <code>Comparable</code> themselves
	 * 
	 * @param resultClass
	 *            The class name of a resultClass
	 * @param sortField
	 *            The sort field of a resultClass
	 * @param ascending
	 *            true to sort in ascending order, false to sort in descending
	 *            order
	 * @param sortKey
	 *            true to sort keys. This parameter is meaningful only if the
	 *            result contains key and value.
	 * @return A Comparator for <code>resultClass</code> or null,
	 * 
	 */
	Comparator<?> getComparator(Object result, String sortField, boolean ascending, boolean sortKey);
}
