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
package com.netcrest.pado.gemfire;

import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;

/**
 * GridRouterCacheListenerImpl is for listening GridRouterInfo updates. It
 * is currently not used.
 * @author dpark
 *
 * @param <String> Grid path
 * @param <GridRouterInfo> GridRouterInfo of the grid path
 */
public class GridRouterCacheListenerImpl<String, GridRouterInfo> extends CacheListenerAdapter<String, GridRouterInfo>
{
	private void update(EntryEvent<String, GridRouterInfo> event)
	{
		GridRouterInfo gridRouterInfo = event.getNewValue();
		
	}

	public void afterCreate(EntryEvent<String, GridRouterInfo> event)
	{
		update(event);
	}

	public void afterUpdate(EntryEvent<String, GridRouterInfo> event)
	{
		update(event);
	}
}
