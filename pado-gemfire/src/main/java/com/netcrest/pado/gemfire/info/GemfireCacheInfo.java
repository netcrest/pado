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
package com.netcrest.pado.gemfire.info;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.server.CacheServer;
import com.gemstone.gemfire.cache.util.GatewayHub;
import com.netcrest.pado.info.CacheInfo;
import com.netcrest.pado.info.PathInfo;
import com.netcrest.pado.internal.factory.InfoFactory;

public class GemfireCacheInfo extends CacheInfo implements DataSerializable
{
	private static final long serialVersionUID = 1L;

	private int searchTimeout;
	private int lockTimeout;
	private int lockLease;
	private int messageSyncInterval;

	public GemfireCacheInfo()
	{
	}

	public GemfireCacheInfo(String gridId, Cache cache)
	{
		super(gridId);
		
		Set<Region<?, ?>> regionSet = cache.rootRegions();
		for (Region<?, ?> region : regionSet) {
			if (region.isDestroyed()) {
				continue;
			}
			GemfireRegionInfo regionInfo = createRegionInfo(region, null);
			pathInfoList.add(regionInfo);
		}
		Collections.sort(pathInfoList);

		List<CacheServer> cacheServerList = cache.getCacheServers();
		for (CacheServer cacheServer : cacheServerList) {
			GemfireCacheServerInfo cacheServerInfo = (GemfireCacheServerInfo)InfoFactory.getInfoFactory().createCacheServerInfo();
			cacheServerInfo.setPort(cacheServer.getPort());
			cacheServerInfo.setGroups(cacheServer.getGroups());
			cacheServerInfo.setBindAddress(cacheServer.getBindAddress());
			cacheServerInfo.setHostnameForClients(cacheServer.getHostnameForClients());
			cacheServerInfo.setMaxThreads(cacheServer.getMaxThreads());
			cacheServerInfoList.add(cacheServerInfo);
		}

		List<GatewayHub> gatewayHubList = cache.getGatewayHubs();
		if (gatewayHubList != null) {
			for (GatewayHub gatewayHub : gatewayHubList) {
				gatewayHubInfoList.add(new GemfireGatewayHubInfo(gatewayHub));
			}
		}
		Collections.sort(gatewayHubInfoList);

		name = cache.getName();
		id = cache.getDistributedSystem().getDistributedMember().getId();
		host = cache.getDistributedSystem().getDistributedMember().getHost();
		processId = cache.getDistributedSystem().getDistributedMember().getProcessId();
		searchTimeout = cache.getSearchTimeout();
		lockTimeout = cache.getLockTimeout();
		lockLease = cache.getLockLease();
		messageSyncInterval = cache.getMessageSyncInterval();
	}

	private GemfireRegionInfo createRegionInfo(Region region, GemfireRegionInfo parentInfo)
	{
		GemfireRegionInfo regionInfo = createRegionInfo(region);
		if (parentInfo != null) {
			parentInfo.setParent(parentInfo);
			parentInfo.getChildList().add(regionInfo);
		}
		Set<Region<?, ?>> regionSet = region.subregions(false);
		for (Region<?, ?> region2 : regionSet) {
			createRegionInfo(region2, regionInfo);
		}
		Collections.sort(regionInfo.getChildList());
		return regionInfo;
	}

	private GemfireRegionInfo createRegionInfo(Region region)
	{
		GemfireRegionInfo info = new GemfireRegionInfo(region);
		return info;
	}

	public int getSearchTimeout()
	{
		return searchTimeout;
	}

	public int getLockTimeout()
	{
		return lockTimeout;
	}

	public int getLockLease()
	{
		return lockLease;
	}

	public int getMessageSyncInterval()
	{
		return messageSyncInterval;
	}

	/**
	 * Resolves RegionInfo parents for the specified RegionInfo object and all
	 * of its children.
	 */
	private void resolveParents(PathInfo regionInfo)
	{
		List<PathInfo> regionInfoList = regionInfo.getChildList();
		for (PathInfo pathInfo : regionInfoList) {
			GemfireRegionInfo regionInfo2 = (GemfireRegionInfo)pathInfo;
			regionInfo2.setParent(regionInfo);
			resolveParents(regionInfo2);
		}
	}

	/**
	 * Resolves RegionInfo parents for all of the regions in CacheInfo. This is
	 * invoked right after deserialization
	 */
	private void resolveParents()
	{
		for (PathInfo regionInfo : pathInfoList) {
			regionInfo.setParent(null);
			resolveParents(regionInfo);
		}
	}
	
	protected void getAppChildPathInfo(PathInfo pathInfo, List<PathInfo> list)
	{
		List<PathInfo> childList = pathInfo.getChildList();
		for (PathInfo child : childList) {
			GemfireRegionInfo childRegionInfo = (GemfireRegionInfo)child;
			if (childRegionInfo.isHidden(false) || childRegionInfo.isScopeLocalRegion(false)) {
				continue;
			}
			list.add(childRegionInfo);
			getChildPathInfo(childRegionInfo, list);
		}
	}
	
	/**
	 * Reads the state of this object from the given <code>DataInput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void fromData(DataInput input) throws IOException, ClassNotFoundException
	{
		gridId = DataSerializer.readString(input);
		name = DataSerializer.readString(input);
		id = DataSerializer.readString(input);
		host = DataSerializer.readString(input);
		processId = DataSerializer.readPrimitiveInt(input);
		searchTimeout = DataSerializer.readPrimitiveInt(input);
		lockTimeout = DataSerializer.readPrimitiveInt(input);
		lockLease = DataSerializer.readPrimitiveInt(input);
		messageSyncInterval = DataSerializer.readPrimitiveInt(input);
		pathInfoList = DataSerializer.readObject(input);
		cacheServerInfoList = DataSerializer.readObject(input);
		gatewayHubInfoList = DataSerializer.readObject(input);
	}

	/**
	 * Writes the state of this object to the given <code>DataOutput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void toData(DataOutput output) throws IOException
	{
		DataSerializer.writeString(gridId, output);
		DataSerializer.writeString(name, output);
		DataSerializer.writeString(id, output);
		DataSerializer.writeString(host, output);
		DataSerializer.writePrimitiveInt(processId, output);
		DataSerializer.writePrimitiveInt(searchTimeout, output);
		DataSerializer.writePrimitiveInt(lockTimeout, output);
		DataSerializer.writePrimitiveInt(lockLease, output);
		DataSerializer.writePrimitiveInt(messageSyncInterval, output);
		DataSerializer.writeObject(pathInfoList, output);
		DataSerializer.writeObject(cacheServerInfoList, output);
		DataSerializer.writeObject(gatewayHubInfoList, output);
	}
}