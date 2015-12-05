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

import java.io.IOException;
import java.util.Properties;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;

/**
 * Not used.
 * @author dpark
 *
 */
@SuppressWarnings("rawtypes")
public class VirtualPathCacheListenerImpl extends CacheListenerAdapter<String, KeyMap> implements Declarable
{
	@Override
	public void init(Properties props)
	{
	}

	/**
	 * 
	 * @param vpd
	 */
	private synchronized void update(EntryEvent<String, KeyMap> event)
	{
		KeyMap vpd = event.getNewValue();
		String dbDir = PadoUtil.getProperty(Constants.PROP_DB_DIR);
		try {
			KeyTypeManager.registerVirtualPath(dbDir, (JsonLite)vpd, true);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	@Override
	public void afterCreate(EntryEvent<String, KeyMap> event)
	{
		update(event);
	}

	@Override
	public void afterUpdate(EntryEvent<String, KeyMap> event)
	{
		update(event);
	}

	@Override
	public void afterDestroy(EntryEvent<String, KeyMap> event)
	{
		String virtualPath = event.getKey();
		String dbDir = PadoUtil.getProperty(Constants.PROP_DB_DIR);
		KeyTypeManager.removeVirtualPath(dbDir, virtualPath);
	}
}
