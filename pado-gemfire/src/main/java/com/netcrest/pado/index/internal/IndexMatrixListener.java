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
package com.netcrest.pado.index.internal;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import com.netcrest.pado.index.service.impl.GridQueryServiceImpl;
import com.netcrest.pado.internal.util.PadoUtil;

@SuppressWarnings("hiding")
public class IndexMatrixListener<String, IndexMatrix> extends CacheListenerAdapter<String, IndexMatrix> implements
		Declarable
{
	private GridQueryServiceImpl gridQueryService = null;
	private ExecutorService es;

	public IndexMatrixListener(GridQueryServiceImpl queryService)
	{
		java.lang.String val = PadoUtil.getProperty("indexMatrix.listener.threadCount", "4");
		int threadCount;
		try {
			threadCount = Integer.parseInt(val);
		} catch (NumberFormatException ex) {
			threadCount = 4;
		} 
		es = Executors.newFixedThreadPool(threadCount);
		gridQueryService = queryService;
	}

	public void init(Properties props)
	{
	}

	public void afterDestroy(final EntryEvent<String, IndexMatrix> event)
	{
		// Must dispatch in a separate thread to avoid deadlock
		// when unregistering interest.
		es.submit(new Runnable() {
			@Override
			public void run()
			{
				gridQueryService.indexDestroyed(event.getKey());
			}
		});

	}

}
