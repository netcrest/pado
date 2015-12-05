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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.internal.cache.BucketRegion;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.exception.GridNotAvailableException;
import com.netcrest.pado.gemfire.GemfireEntryImpl;
import com.netcrest.pado.util.GridUtil;

/**
 * GridMapBizImpl implements only a few methods that need to be executed
 * in the server side. Most of the IGridMapBiz methods are handled by 
 * the GridMapBizImplLocal.
 * 
 * @author dpark
 *
 * @param <K>
 * @param <V>
 */
public class GridMapBizImpl<K, V> //implements IGridMapBiz<K, V>
{
	@Resource IBizContextServer bizContext;

	@SuppressWarnings("rawtypes")
	private Region getRegion()
	{
		Object[] args = bizContext.getGridContextServer().getAdditionalArguments();
		String gridPath = (String)args[0];
		String fullPath = GridUtil.getFullPath(gridPath);
		return CacheFactory.getAnyInstance().getRegion(fullPath);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@BizMethod
	public void create(Object key, V value)
	{
		Region region = getRegion();
		region.create(key, value);
	}
	
	@SuppressWarnings("rawtypes")
	@BizMethod
	public void put(Object key, V value)
	{
		Region region = getRegion();
		region.put(key, value);
	}
	
	@SuppressWarnings("rawtypes")
	@BizMethod
	public int size()
	{
		Region region = getRegion();
		if (region == null) {
			return 0;
		}
		return region.size();
	}
	
	@SuppressWarnings("rawtypes")
	@BizMethod
	public boolean isEmpty()
	{
		Region region = getRegion();
		if (region == null) {
			return false;
		}
		return region.isEmpty();
	}

	@SuppressWarnings("rawtypes")
	@BizMethod
	public boolean containsKey(Object key)
	{
		Region region = getRegion();
		if (region == null) {
			return false;
		}
		return region.containsKey(key);
	}

	@SuppressWarnings("rawtypes")
	@BizMethod
	public boolean containsValue(Object value)
	{
		Region region = getRegion();
		if (region == null) {
			return false;
		}
		return region.containsValue(value);
	}

	@SuppressWarnings("unchecked")
	@BizMethod
	public void removeAll(Collection<? extends K> keyCollection) throws GridNotAvailableException
	{
		Region<K, V> region = getRegion();
		if (region instanceof PartitionedRegion) {
			PartitionedRegion pr = (PartitionedRegion) region;
			Set<BucketRegion> set = pr.getDataStore().getAllLocalPrimaryBucketRegions();
			for (BucketRegion bucketRegion : set) {
				final BucketRegion br = bucketRegion;
				for (K key : keyCollection) {
					br.remove(key);
				}
			}
		} else {
			for (K key : keyCollection) {
				region.remove(key);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@BizMethod
	public Map.Entry<K, V> getRandomEntry()
	{
		Region<K, V> region = getRegion();
		GemfireEntryImpl<K, V> randomEntry = null;
		
		if (region instanceof PartitionedRegion) {
			PartitionedRegion pr = (PartitionedRegion) region;
			Set<BucketRegion> set = pr.getDataStore().getAllLocalPrimaryBucketRegions();
			for (BucketRegion bucketRegion : set) {
				final BucketRegion br = bucketRegion;
				Set<Map.Entry<K, V>> brSet = br.entrySet();
				for (Map.Entry<K, V> entry : brSet) {
					randomEntry = new GemfireEntryImpl<K, V>(entry.getKey(), entry.getValue());
					break;
				}
				if (randomEntry != null) {
					break;
				}
			}
		} else {
			Set<Map.Entry<K, V>> set = region.entrySet();
			for (Map.Entry<K, V> entry : set) {
				randomEntry = new GemfireEntryImpl<K, V>(entry.getKey(), entry.getValue());
				break;
			}
		}
		return randomEntry;
	}
}
