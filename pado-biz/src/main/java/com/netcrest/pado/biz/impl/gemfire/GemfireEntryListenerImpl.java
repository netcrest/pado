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

import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import com.netcrest.pado.IEntryListener;

public class GemfireEntryListenerImpl<K, V> extends CacheListenerAdapter<K, V>
{
	private String gridId;
	private IEntryListener<K, V> entryListener;
	
	public GemfireEntryListenerImpl(String gridId, IEntryListener<K, V> entryListener)
	{
		this.gridId = gridId;
		this.entryListener = entryListener;
	}
	
	@Override
	public void afterCreate(com.gemstone.gemfire.cache.EntryEvent<K, V> event)
	{
		entryListener.onCreate(new GemfireEntryEvent<K, V>(gridId, event));
	}

	@Override
	public void afterUpdate(com.gemstone.gemfire.cache.EntryEvent<K, V> event)
	{
		entryListener.onUpdate(new GemfireEntryEvent<K, V>(gridId, event));
	}

	@Override
	public void afterInvalidate(com.gemstone.gemfire.cache.EntryEvent<K, V> event)
	{
		entryListener.onInvalidate(new GemfireEntryEvent<K, V>(gridId, event));
	}

	@Override
	public void afterDestroy(com.gemstone.gemfire.cache.EntryEvent<K, V> event)
	{
		entryListener.onRemove(new GemfireEntryEvent<K, V>(gridId, event));
	}
}
