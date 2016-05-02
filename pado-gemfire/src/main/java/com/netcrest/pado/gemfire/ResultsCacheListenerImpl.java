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

import java.util.Properties;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import com.netcrest.pado.index.internal.db.LocalResultsDb;
import com.netcrest.pado.info.GridInfo;

/**
 * 
 * @author dpark
 *
 */
public class ResultsCacheListenerImpl extends CacheListenerAdapter<String, GridInfo> implements Declarable
{
	public ResultsCacheListenerImpl()
	{
	}

	@Override
	public void init(Properties props)
	{
	}

	/**
	 * This method is invoked upon expiration. It removes the corresponding
	 * result file if exists.
	 */
	@Override
	public void afterDestroy(EntryEvent<String, GridInfo> event)
	{
		String resultId = (String)event.getKey();
		LocalResultsDb.getLocalResultsDb().removeFile(resultId);
	}
}
