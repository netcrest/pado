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
import com.netcrest.pado.info.AppInfo;
import com.netcrest.pado.internal.impl.PadoClientManager;
import com.netcrest.pado.log.Logger;

public class AppInfoCacheListenerImpl extends CacheListenerAdapter<String, AppInfo> implements Declarable
{

	@Override
	public void init(Properties props)
	{
	}

	private void update(EntryEvent<String, AppInfo> event)
	{
		String appId = event.getKey();
		PadoClientManager.getPadoClientManager().refresh(appId);
		Logger.info("AppInfo received and applied to PadoClientManager: [appId=" + appId + "].");
	}

	public void afterCreate(EntryEvent<String, AppInfo> event)
	{
		update(event);
	}

	public void afterUpdate(EntryEvent<String, AppInfo> event)
	{
		update(event);
	}

	public void afterDestroy(EntryEvent<String, AppInfo> event)
	{
		String appId = event.getKey();
		PadoClientManager.getPadoClientManager().removeApp(appId);
	}
}
