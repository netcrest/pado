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
package com.netcrest.pado.temporal.gemfire.impl;

import com.gemstone.gemfire.cache.CacheWriter;
import com.gemstone.gemfire.cache.CacheWriterException;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.RegionEvent;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.ITemporalList;
import com.netcrest.pado.temporal.TemporalManager;
import com.netcrest.pado.temporal.gemfire.GemfireTemporalManager;

public class TemporalCacheWriter implements CacheWriter<ITemporalKey, ITemporalData>
{

	public TemporalCacheWriter()
	{
	}
	
	@Override
	public void close()
	{
	}

	@Override
	public void beforeUpdate(EntryEvent<ITemporalKey, ITemporalData> event) throws CacheWriterException
	{
		throw new CacheWriterException("Temporal entry already exists. Overwriting existing temporal data is not permitted.");
	}

	@Override
	public void beforeCreate(EntryEvent<ITemporalKey, ITemporalData> event) throws CacheWriterException
	{
		ITemporalKey key = event.getKey();
		GemfireTemporalManager tm = (GemfireTemporalManager)TemporalManager.getTemporalManager(event.getRegion().getFullPath());
		TemporalCacheListener cl = tm.getTemporalCacheListener();
		ITemporalList list = cl.getTemporalList(key.getIdentityKey());
		if (list == null) {
			return;
		}
		boolean isDeleted = list.isRemoved(key.getWrittenTime());
		if (isDeleted) {
			throw new CacheWriterException("Invalid temporal entry. Entering temporal data after the delete time is not allowed.");
		}
	}

	@Override
	public void beforeDestroy(EntryEvent<ITemporalKey, ITemporalData> event) throws CacheWriterException
	{
	}

	@Override
	public void beforeRegionDestroy(RegionEvent<ITemporalKey, ITemporalData> event) throws CacheWriterException
	{
	}

	@Override
	public void beforeRegionClear(RegionEvent<ITemporalKey, ITemporalData> event) throws CacheWriterException
	{
	}

}
