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

public class GemfireEntryEvent<K, V> extends com.netcrest.pado.EntryEvent<K, V>
{
	private String gridId;
	private K key;
	private V value;
	private K oldKey;
	private V oldValue;
	
	public GemfireEntryEvent(String gridId, com.gemstone.gemfire.cache.EntryEvent<K, V> event)
	{
		this.gridId = gridId;
		this.key = event.getKey();
		this.value = event.getNewValue();
		this.oldKey = null;
		this.oldValue = event.getOldValue();
	}
	
	@Override
	public String getGridId()
	{
		return gridId;
	}
	
	@Override
	public K getKey()
	{
		return key;
	}

	@Override
	public V getValue()
	{
		return value;
	}

	@Override
	public K getOldKey()
	{
		return oldKey;
	}

	@Override
	public V getOldValue()
	{
		return oldValue;
	}

}
