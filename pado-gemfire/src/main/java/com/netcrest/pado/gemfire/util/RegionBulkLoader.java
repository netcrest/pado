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
package com.netcrest.pado.gemfire.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.netcrest.pado.exception.PathUndefinedException;
import com.netcrest.pado.util.IBulkLoader;
import com.netcrest.pado.util.IBulkLoaderListener;

public class RegionBulkLoader<K, V> implements IBulkLoader<K, V>
{
	protected Region<K, V> region;
	protected HashMap<K, V> map;
	protected int batchSize = 1000;
	protected Set<IBulkLoaderListener> bulkLoaderListenerSet = new HashSet<IBulkLoaderListener>(3);

	public RegionBulkLoader()
	{
		map = new HashMap<K, V>(batchSize, 1f);
	}

	public RegionBulkLoader(String gridPath)
	{
		this(gridPath, 1000);
	}

	public RegionBulkLoader(String gridPath, int batchSize)
	{
		setPath(gridPath);
		this.batchSize = batchSize;
		map = new HashMap<K, V>(batchSize, 1f);
	}

	public RegionBulkLoader(Region<K, V> region)
	{
		this(region, 1000);
	}

	public RegionBulkLoader(Region<K, V> region, int batchSize)
	{
		this.region = region;
		this.batchSize = batchSize;
		map = new HashMap<K, V>(batchSize, 1f);
	}

	public Region<K, V> getRegion()
	{
		return region;
	}

	/**
	 * Returns the object batch size. IBulkLoader loads data objects into a
	 * GemFire region a batch at a time by invoking {@link Region#putAll}. This
	 * batch size determines the number of data objects per
	 * {@link Region#putAll} call. The default size is 1000.
	 */
	@Override
	public int getBatchSize()
	{
		return batchSize;
	}

	/**
	 * Sets the batch size. The default size is 1000.
	 * 
	 * @param batchSize
	 *            Batch size
	 */
	@Override
	public void setBatchSize(int batchSize)
	{
		this.batchSize = batchSize;
	}

	@Override
	public void setPath(String gridPath) throws PathUndefinedException
	{
		flush();
		region = null;
		Cache cache = CacheFactory.getAnyInstance();
		String fullPath = GemfireGridUtil.getFullPath(gridPath);
		region = cache.getRegion(fullPath);
	}

	@Override
	public String getPath()
	{
		if (region == null) {
			return null;
		}
		return GemfireGridUtil.getChildPath(region.getFullPath());
	}

	@Override
	public void put(K key, V value) throws PathUndefinedException
	{
		if (region == null) {
			throw new PathUndefinedException();
		}
		map.put(key, value);
		if (map.size() % batchSize == 0) {
			flush();
		}
	}

	@Override
	public void flush()
	{
		int count = map.size();
		if (count > 0 && region != null) {
			region.putAll(map);
			map.clear();
			synchronized (bulkLoaderListenerSet) {
				Iterator<IBulkLoaderListener> iterator = bulkLoaderListenerSet.iterator();
				while (iterator.hasNext()) {
					IBulkLoaderListener listener = iterator.next();
					listener.flushed(count);
				}
			}
		}
	}

	@Override
	public void remove(K key) throws PathUndefinedException
	{
		if (region == null) {
			throw new PathUndefinedException();
		}
		region.remove(key);
		map.remove(key);
	}

	@Override
	public void addBulkLoaderListener(IBulkLoaderListener listener)
	{
		synchronized (bulkLoaderListenerSet) {
			bulkLoaderListenerSet.add(listener);
		}
	}

	@Override
	public void removeBulkLoaderListener(IBulkLoaderListener listener)
	{
		synchronized (bulkLoaderListenerSet) {
			bulkLoaderListenerSet.remove(listener);
		}
	}
}
