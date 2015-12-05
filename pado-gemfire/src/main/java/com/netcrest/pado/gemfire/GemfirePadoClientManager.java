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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.gemstone.gemfire.cache.Region;
import com.netcrest.pado.info.AppInfo;
import com.netcrest.pado.internal.impl.PadoClientManager;

public class GemfirePadoClientManager extends PadoClientManager
{	
	/**
	 * <appId, Region<appId, AppInfo>>
	 */
	private Map<String, Region<String, AppInfo>> appRegionMap = new HashMap<String, Region<String, AppInfo>>(4);

	public GemfirePadoClientManager()
	{}

	public static GemfirePadoClientManager getPadoClientManager()
	{
		return (GemfirePadoClientManager)clientManager;
	}
	
	@Override
	public AppInfo getAppInfo(String appId)
	{
		Region<String, AppInfo> appRegion = appRegionMap.get(appId);
		if (appRegion != null && appRegion.isDestroyed() == false) {
			return appRegion.get(appId);
		// if this VM is a grid and a parent then use its own app region.
	    // this is needed in order to support biz calls from a grid, i.e.,
	    // Lucene calls.
		} else if (GemfirePadoServerManager.getPadoServerManager().isServer()) {
			return GemfirePadoServerManager.getPadoServerManager().getAppRegion().get(appId);
		} else {
			return null;
		}
	}
	
	@Override
	public AppInfo getAppInfo(String appId, boolean fromRemote)
	{
		Region<String, AppInfo> appRegion = appRegionMap.get(appId);
		if (appRegion != null) {
			if (fromRemote) {
				appRegion.invalidate(appId);
			}
			return appRegion.get(appId);
		} else {
			return null;
		}
	}
	
	public void addAppRegion(String appId, Region<String, AppInfo> appRegion)
	{
		if (appRegionMap.containsKey(appId) == false) {
			appRegionMap.put(appId, appRegion);
		}
	}
	
	//----------------------

	
	/**
	 * Returns all live grid IDs that are assigned to all apps that this
	 * VM has registered via Pado logins. It always returns a non-null
	 * set.
	 */
	@Override
	public Set<String> getLiveGridIdSet()
	{
		Set<Map.Entry<String, Region<String, AppInfo>>> set = appRegionMap.entrySet();
		HashSet<String> gridSet = new HashSet(10);
		for (Map.Entry<String, Region<String, AppInfo>> entry : set) {
			String appId = entry.getKey();
			Region<String, AppInfo> appRegion = entry.getValue();
			AppInfo appInfo = appRegion.get(appId);
			gridSet.addAll(appInfo.getGridIdSet());
		}
		return gridSet;
	}
	
	public void clear()
	{
		super.clear();
		appRegionMap.clear();
	}
}