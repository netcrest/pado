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

import java.util.List;

import javax.annotation.Resource;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IPado;
import com.netcrest.pado.biz.server.IGridBiz;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.ServerInfo;

public class GridBizImplLocal implements IGridBiz, IBizLocal
{
	@Resource
	private IGridBiz biz;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IBiz biz, IPado pado, Object... args)
	{
		this.biz = (IGridBiz)biz;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBizContextClient getBizContext()
	{
		return biz.getBizContext();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GridInfo getGridInfo()
	{
		return biz.getGridInfo();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void publishGridInfo(GridInfo gridInfo)
	{
		biz.publishGridInfo(gridInfo);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ServerInfo> getServerInfoList(String regionPath)
	{
		return biz.getServerInfoList(regionPath);
	}

}
