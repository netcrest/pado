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
package com.netcrest.pado.biz.server.impl.gemfire;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.biz.server.IGridBiz;
import com.netcrest.pado.info.CacheInfo;
import com.netcrest.pado.info.CacheServerInfo;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.ServerInfo;
import com.netcrest.pado.internal.factory.InfoFactory;
import com.netcrest.pado.server.PadoServerManager;

public class GridBizImpl implements IGridBiz
{
	@Resource
	private IBizContextServer bizContext;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBizContextClient getBizContext()
	{
		// IBizContextClient not available within grid
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@Override
	public GridInfo getGridInfo()
	{
		// Return a new GridInfo instead of the cached one.
		return PadoServerManager.getPadoServerManager().createGridInfo();
	}

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@Override
	public void publishGridInfo(GridInfo gridInfo)
	{
		if (gridInfo == null) {
			return;
		}
		PadoServerManager.getPadoServerManager().updateAppInfo(gridInfo);
	}

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@Override
	public List<ServerInfo> getServerInfoList(String fullPath)
	{
		GridInfo gridInfo = PadoServerManager.getPadoServerManager().getGridInfo();
		CacheInfo cacheInfo = gridInfo.getCacheInfo();
		List<CacheServerInfo> cacheServerInfoList = cacheInfo.getCacheServerInfoList();
		List<ServerInfo> serverInfoList = new ArrayList(cacheServerInfoList.size() + 1);
		for (CacheServerInfo cacheServerInfo : cacheServerInfoList) {
			serverInfoList.add(InfoFactory.getInfoFactory().createServerInfo(gridInfo, cacheInfo, cacheServerInfo, fullPath));
		}
		return serverInfoList;
	}
}
