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
package com.netcrest.pado.gemfire.factory;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.gemstone.gemfire.cache.CacheFactory;
import com.netcrest.pado.gemfire.info.GemfireBizInfo;
import com.netcrest.pado.gemfire.info.GemfireBucketInfo;
import com.netcrest.pado.gemfire.info.GemfireCacheDumpInfo;
import com.netcrest.pado.gemfire.info.GemfireCacheInfo;
import com.netcrest.pado.gemfire.info.GemfireCacheServerInfo;
import com.netcrest.pado.gemfire.info.GemfireConfigInfo;
import com.netcrest.pado.gemfire.info.GemfireDumpInfo;
import com.netcrest.pado.gemfire.info.GemfireGridInfo;
import com.netcrest.pado.gemfire.info.GemfireGridPathInfo;
import com.netcrest.pado.gemfire.info.GemfireGridRouterInfo;
import com.netcrest.pado.gemfire.info.GemfireGridStatusInfo;
import com.netcrest.pado.gemfire.info.GemfireLoginInfo;
import com.netcrest.pado.gemfire.info.GemfirePadoInfo;
import com.netcrest.pado.gemfire.info.GemfireServerInfo;
import com.netcrest.pado.info.BizInfo;
import com.netcrest.pado.info.BucketInfo;
import com.netcrest.pado.info.CacheDumpInfo;
import com.netcrest.pado.info.CacheInfo;
import com.netcrest.pado.info.CacheServerInfo;
import com.netcrest.pado.info.ConfigInfo;
import com.netcrest.pado.info.DumpInfo;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.GridPathInfo;
import com.netcrest.pado.info.GridRouterInfo;
import com.netcrest.pado.info.LoginInfo;
import com.netcrest.pado.info.PadoInfo;
import com.netcrest.pado.info.ServerInfo;
import com.netcrest.pado.info.message.GridStatusInfo;
import com.netcrest.pado.internal.factory.InfoFactory;
import com.netcrest.pado.server.PadoServerManager;

public class GemfireInfoFactory extends InfoFactory
{
	private static GemfireInfoFactory infoFactory = new GemfireInfoFactory();

	public static InfoFactory getInfoFactory()
	{
		return infoFactory;
	}

	public LoginInfo createLoginInfo(String appId, String domain, String username, Object token, Set<BizInfo> bizSet)
	{
		return new GemfireLoginInfo(appId, domain, username, token, bizSet);
	}

	public ConfigInfo createConfigInfo()
	{
		return new GemfireConfigInfo();
	}

	public PadoInfo createPadoInfo(String appId)
	{
		return new GemfirePadoInfo(appId);
	}

	public BizInfo createBizInfo(String bizInterfaceName)
	{
		return new GemfireBizInfo(bizInterfaceName);
	}

	public BucketInfo createBucketInfo(int bucketId, boolean isPrimary, int size, long totalBytes)
	{
		return new GemfireBucketInfo(bucketId, isPrimary, size, totalBytes);
	}

	public CacheServerInfo createCacheServerInfo()
	{
		return new GemfireCacheServerInfo();
	}

	public GridRouterInfo createGridRouterInfo(String gridPath)
	{
		return new GemfireGridRouterInfo(gridPath);
	}

	public ServerInfo createServerInfo(GridInfo gridInfo, CacheInfo cacheInfo, CacheServerInfo cacheServerInfo,
			String fullPath)
	{
		return new GemfireServerInfo((GemfireGridInfo) gridInfo, (GemfireCacheInfo) cacheInfo,
				(GemfireCacheServerInfo) cacheServerInfo, fullPath);
	}
	
	public CacheDumpInfo createCacheDumpInfo(List<DumpInfo> dumpInfoList)
	{
		PadoServerManager sm = PadoServerManager.getPadoServerManager();
		return new GemfireCacheDumpInfo(sm.getGridId(), CacheFactory.getAnyInstance(), dumpInfoList);
	}
	
	public DumpInfo createDumpInfo(String parentFullPath, File file, Date date, boolean recursive)
	{
		return new GemfireDumpInfo(parentFullPath, file, date, recursive);
	}

	public GridStatusInfo createGridStatusInfo(GridStatusInfo.Status status, String gridId, String masterId,
			String serverId, String message)
	{
		return new GemfireGridStatusInfo(status, gridId, masterId, serverId, message);
	}

	public GridPathInfo createGridPathInfo(String gridPath)
	{
		return new GemfireGridPathInfo(gridPath);
	}
}