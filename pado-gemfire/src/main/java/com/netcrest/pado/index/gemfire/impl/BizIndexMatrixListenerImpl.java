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
package com.netcrest.pado.index.gemfire.impl;

import java.util.Properties;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;

@SuppressWarnings("hiding")
public class BizIndexMatrixListenerImpl<String, IndexMatrix> extends CacheListenerAdapter<String, IndexMatrix> implements
		Declarable
{
	private BizGridQueryServiceImpl  gridQueryService = null;
	
	public BizIndexMatrixListenerImpl()
	{
	}
	
	public BizIndexMatrixListenerImpl(BizGridQueryServiceImpl queryService)	
	{
		gridQueryService = queryService;
	}
	
	public void init(Properties props)
	{
	}

	public void afterDestroy(EntryEvent<String, IndexMatrix> event)
	{
		gridQueryService.indexDestroyed(event.getKey());
	}

}
