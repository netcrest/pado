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
package com.netcrest.pado.gemfire.biz.biz.impl.gemfire;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionService;
import com.gemstone.gemfire.cache.client.Pool;
import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IPado;
import com.netcrest.pado.gemfire.GemfireGridService;
import com.netcrest.pado.gemfire.biz.biz.IGemfireBiz;


public class GemfireBizImplLocal implements IGemfireBiz, IBizLocal
{

	@Resource IGemfireBiz biz;
	
	@Override
	public void init(IBiz biz, IPado pado, Object... args)
	{
	}

	@Override
	public IBizContextClient getBizContext()
	{
		return biz.getBizContext();
	}

	@Override
	public String getAppId()
	{
		return biz.getBizContext().getGridService().getAppId();
	}
	
	@Override
	public Cache getCache()
	{
		return CacheFactory.getAnyInstance();
	}

	@Override
	public List<Pool> getPoolList(String gridId)
	{
		return ((GemfireGridService)biz.getBizContext().getGridService()).getPoolList(gridId);
	}

	@Override
	public Pool getPool(String gridId)
	{
		return ((GemfireGridService)biz.getBizContext().getGridService()).getPool(gridId);
	}

	@Override
	public Pool getSharedPool(String gridId)
	{
		return ((GemfireGridService)biz.getBizContext().getGridService()).getSharedPool(gridId);
	}

	@Override
	public RegionService getRegionService(String gridId)
	{
		return ((GemfireGridService)biz.getBizContext().getGridService()).getRegionService(gridId);
	}

	@Override
	public String getRootPath(String gridId)
	{
		return biz.getBizContext().getGridService().getRootPath(gridId);
	}

	@Override
	public Pool getPadoPool()
	{
		return ((GemfireGridService)biz.getBizContext().getGridService()).getSharedPool();
	}

	@Override
	public Region getRegion(String gridId, String gridPath)
	{
		return ((GemfireGridService)biz.getBizContext().getGridService()).getRegion(gridId, gridPath);
	}

	@Override
	public String[] getGridIds(String gridPath)
	{
		return biz.getBizContext().getGridService().getGridIds(gridPath);
	}

	@Override
	public Map<String, Region> getGridRegionMap(String gridPath)
	{
		return ((GemfireGridService)biz.getBizContext().getGridService()).getGridRegionMap(gridPath);
	}
}
