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
package com.netcrest.pado.temporal;

import java.util.Map;
import java.util.Set;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IGridMapBizLink;

/**
 * ITemporalAdminBizLink provides administrative methods for managing temporal
 * data. It is a class loader link class that links the main class loader to an
 * IBiz class loader.
 * 
 * @author dpark
 * 
 * @param <K>
 *            Key class
 * @param <V>
 *            Value class
 */
public interface ITemporalAdminBizLink<K, V> extends IBiz
{

	/**
	 * Returns metadata for the specified target path.
	 */
	TemporalClientMetadata getMetadata(String targetPath);

	/**
	 * Returns metadata for the ITemporalBiz target path.
	 */
	TemporalClientMetadata getMetadata();

	/**
	 * Returns all temporal types in Pado.
	 */

	TemporalType[] getAllTemporalTypes();

	/**
	 * Returns the grid path.
	 */
	public String getGridPath();

	/**
	 * Sets the grid path.
	 * 
	 * @param gridPath
	 *            Grid path.
	 */
	public void setGridPath(String gridPath);

	/**
	 * Returns the temporal value of the specified temporal key.
	 * 
	 * @param temporalKey
	 *            The temporal key.
	 */
	ITemporalData<K> get(ITemporalKey<K> temporalKey);

	/**
	 * Returns all entries mapped by the specified temporal keys.
	 * 
	 * @param keys
	 *            Temporal keys that map temporal data.
	 */
	Map<ITemporalKey<K>, ITemporalData<K>> getAll(Set<ITemporalKey<K>> keys);

	/**
	 * Puts the specified temporal key and value as an entry into Pado.
	 * 
	 * @param temporalKey
	 *            The temporal key
	 * @param data
	 *            The temporal data
	 * @return The previous value associated with key. In some cases null may be
	 *         returned even if a previous value exists. The behavior depends on
	 *         the underlying data grid product.
	 */
	ITemporalData<K> put(ITemporalKey<K> temporalKey, ITemporalData<K> data);

	/**
	 * Puts all of the entries in the specified map into Pado.
	 * 
	 * @param map
	 *            The entries to put into Pado.
	 */
	void putAll(Map<ITemporalKey<K>, ITemporalData<K>> map);

	/**
	 * Returns the temporal data list of the identity key.
	 * 
	 * @param identityKey
	 *            The identity key.
	 */
	@SuppressWarnings("rawtypes")
	TemporalDataList getTemporalDataList(K identityKey);

	/**
	 * Clears the temporal list of the specified identity key. This method must
	 * be used with care as it permanently discards the temporal list from the
	 * grid. The discarded temporal list cannot be recovered.
	 * 
	 * @param identityKey
	 *            The identity key
	 */
	void clearTemporalList(K identityKey);

	/**
	 * Permanently removes the specified temporal key and its mapped data from
	 * the grid. Data is not recoverable after this call.
	 * 
	 * @param temporalKey
	 *            Temporal key
	 * @return Removed data or null if not found
	 */
	ITemporalData<K> removePermanently(ITemporalKey<K> temporalKey);

	/**
	 * Dumps the temporal list of the specified identity key in the server log
	 * file.
	 * 
	 * @param identityKey
	 */
	void dumpServer(K identityKey);

	/**
	 * Creates a bulk loader.
	 * 
	 * @param batchSize
	 *            The batch size of the bulk loader. A bulk-put is done when it
	 *            reaches the batch size.
	 */
	ITemporalBulkLoader<K, V> createBulkLoader(int batchSize);

	/**
	 * Returns the client factory object for creating temporal key and value
	 * objects.
	 */
	TemporalClientFactory<K, V> getTemporalClientFactory();

	/**
	 * Returns the {@link IGridMapBiz} instance used to directly access Pado by
	 * (key, value) pairs.
	 */
	IGridMapBizLink<ITemporalKey<K>, ITemporalData<K>> getGridMapBiz();

	/**
	 * Enables/disables the temporal data management mechanics. If Enabled
	 * (true), then it freshly builds all of the temporal lists pertaining to
	 * this temporal admin's path regardless of whether it is already enabled or
	 * the temporal lists have already been built. If disabled (false), then it
	 * clears all of the temporal lists and detaches the temporal data update
	 * listener such that it completely stops producing temporal data for this
	 * temporal admin's path.
	 * 
	 * @param enabled
	 *            true to enable, false to disable.
	 * @param spawnThread
	 *            true to enable in thread, false to block till done.
	 */
	void setEnabled(boolean enabled, boolean spawnThread);

	/**
	 * Returns true if the temporal data management mechanics is enabled, false,
	 */
	boolean isEnabled();

	/**
	 * Enables or disables temporal data for all temporal paths. Note that if
	 * enabled (true), the temporal data is rebuilt regardless whether it is
	 * already enabled.
	 * 
	 * @param enabled
	 *            true to enable, false to disable
	 * @param spawnThread
	 *            true to enable in thread, false to block till done.
	 */
	void setEnabledAll(boolean enabled, boolean spawnThread);

	/**
	 * Returns true if all of the temporal paths are enabled. It returns false
	 * if at least one path is disabled.
	 * 
	 * @return
	 */
	boolean isEnabledAll();
}
