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
package com.netcrest.pado.link;

import java.util.List;
import java.util.Set;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.index.result.IMemberResults;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;

/**
 * ILuceneBizLink links the main class loader to {@link ILuceneBiz}.
 * 
 * <p>
 * <b>Arguments: None</b>
 * <p>
 * 
 * @author dpark
 * 
 */
@SuppressWarnings({ "rawtypes" })
public interface ILuceneBizLink extends IBiz
{
	/**
	 * Builds all Lucene indexes if Lucene is enabled for all grids including
	 * the parent and child grids.
	 */
	void buildAllIndexes();

	/**
	 * Builds Lucene indexes if Lucene is enabled for the specified grid paths
	 * for all grids including the parent and child grids.
	 * 
	 * @param gridPaths
	 *            Grid paths. If null or empty then build indexes for all grid
	 *            paths, i.e., analogous to invoking {@link #buildAllIndexes()}.
	 */
	void buildAllGridIndexes(String... gridPaths);

	/**
	 * Builds all Lucene indexes if Lucene is enabled for the specified grid.
	 * 
	 * @param gridId
	 *            Grid ID.
	 */
	void buildAllPathIndexes(String gridId);

	/**
	 * Builds Lucene indexes if Lucene is enabled for the specified grid ID and
	 * paths.
	 * 
	 * @param gridId
	 *            Grid ID.
	 * @param gridPaths
	 *            Grid paths. If null or empty then build indexes for all grid
	 *            paths, i.e., analogous to buildAllIndexes(gridId).
	 */
	void buildIndexes(String gridId, String... gridPaths);

	/**
	 * Builds Temporal indexes first and then Lucene indexes for the specified
	 * grid ID and paths.
	 * 
	 * @param gridId
	 *            Grid ID.
	 * @param gridPaths
	 *            Grid paths. If null or empty then build indexes for all grid
	 *            paths, i.e., analogous to buildAllIndexes(gridId).
	 */
	void buildTemporalIndexes(String gridId, String... gridPaths);

	/**
	 * Executes the specified Lucene query criteria.
	 * 
	 * @param criteria
	 *            Query criteria
	 * @return Return member results
	 */
	IMemberResults query(GridQuery criteria);

	/**
	 * Returns the Lucene search results of the specified criteria from all
	 * grids.
	 * 
	 * @param criteria
	 *            Grid query criteria
	 * @deprecated Use {@linkplain ITemporalLink} instead.
	 */
	List<TemporalEntry> searchTemporal(GridQuery criteria);

	/**
	 * Searches temporal identity keys by executing the specified Lucene query
	 * from all grids.
	 * 
	 * @param gridPath
	 *            Grid path
	 * @param queryString
	 *            Lucene query string
	 * @deprecated Use {@linkplain ITemporalLink} instead.
	 */
	Set getTemporalIdentityKeySet(String gridPath, String queryString);

	/**
	 * Searches temporal keys by executing the specified Lucene query from all
	 * grids.
	 * 
	 * @param gridPath
	 *            Grid path
	 * @param queryString
	 *            Lucene query string
	 * @deprecated Use {@linkplain ITemporalLink} instead.
	 */
	Set<ITemporalKey> getTemporalKeySet(String gridPath, String queryString);

	/**
	 * Enables/disables the Lucene indexing mechanics for the specified grid
	 * path. If disabled then the servers stop building Lucene indexes for the
	 * temporal grid path. If enabled and it was previously disabled, then
	 * Lucene indexes are rebuilt for all of the contents in the grid path.
	 * 
	 * @param gridPath
	 *            Grid path
	 * @param enabled
	 *            true to enable, false to disable Lucene indexing
	 */
	void setLuceneEnabled(String gridPath, boolean enabled);

	/**
	 * Returns true if Lucene indexing is enabled for the specified temporal
	 * grid path.
	 */
	boolean isLuceneEnabled(String gridPath);

	/**
	 * Enables/disables the dynamic indexing mechanics for all of the temporal
	 * grid paths. If disabled then the servers stop building Lucene indexes for
	 * all grid paths. If enabled, then Lucene indexes are rebuilt for all
	 * temporal grid paths that were previously disabled.
	 * 
	 * @param enabled
	 *            true to enable, false to disable Lucene indexing
	 */
	void setLuceneEnabledAll(boolean enabled);

	/**
	 * Returns true if Lucene indexing is enabled for all grid temporal paths.
	 */
	boolean isLuceneEnabledAll();
}
