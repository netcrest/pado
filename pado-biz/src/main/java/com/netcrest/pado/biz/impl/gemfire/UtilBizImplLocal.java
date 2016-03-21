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
package com.netcrest.pado.biz.impl.gemfire;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IPado;
import com.netcrest.pado.biz.IUtilBiz;
import com.netcrest.pado.biz.data.ServerLoad;
import com.netcrest.pado.biz.file.CompositeKeyInfo;
import com.netcrest.pado.info.CacheDumpInfo;
import com.netcrest.pado.info.WhichInfo;

public class UtilBizImplLocal implements IUtilBiz, IBizLocal
{
	@Resource
	IUtilBiz biz;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IBiz biz, IPado pado, Object... args)
	{
		this.biz = (IUtilBiz) biz;
		if (args != null && args.length > 0) {
			biz.getBizContext().getGridContextClient().setGridPath(args[0].toString());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBizContextClient getBizContext()
	{
		return biz.getBizContext();
	}

	@Override
	public byte[] ping(byte[] payload)
	{
		biz.getBizContext().getGridContextClient().reset();
		return biz.ping(payload);
	}

	@Override
	public String echo(String message)
	{
		biz.getBizContext().getGridContextClient().reset();
		return biz.echo(message);
	}

	@Override
	public ServerLoad getServerLoad()
	{
		biz.getBizContext().getGridContextClient().reset();
		return biz.getServerLoad();
	}

	@Override
	public Date getServerTime()
	{
		biz.getBizContext().getGridContextClient().reset();
		return biz.getServerTime();
	}
	
	@Override
	public List<String> dumpAll()
	{	
		biz.getBizContext().getGridContextClient().reset();
		Date date = biz.getServerTime();
		biz.getBizContext().getGridContextClient().setAdditionalArguments(date);
		return biz.dumpAll();
	}
	
	@Override
	public List<String> dumpServers(String...gridPaths)
	{
		biz.getBizContext().getGridContextClient().reset();
		Date date = biz.getServerTime();
		biz.getBizContext().getGridContextClient().setAdditionalArguments(date);
		return biz.dumpServers(gridPaths);
	}
	
	@Override
	public List<WhichInfo> which(String gridPath, Object key)
	{
		biz.getBizContext().getGridContextClient().reset();
		return biz.which(gridPath, key);
	}

	@Override
	public WhichInfo whichRoutingKey(String gridPath, Object routingKey)
	{
		biz.getBizContext().getGridContextClient().reset();
		biz.getBizContext().getGridContextClient().setGridPath(gridPath);
		biz.getBizContext().getGridContextClient().setRoutingKeys(Collections.singleton(routingKey));
		return biz.whichRoutingKey(gridPath, routingKey);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List executeRoutingQuery(String queryString, Object routingKey)
	{
		biz.getBizContext().getGridContextClient().reset();
		biz.getBizContext().getGridContextClient().setRoutingKeys(Collections.singleton(routingKey));
		return biz.executeRoutingQuery(queryString, routingKey);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List executeQuery(String queryString)
	{
		biz.getBizContext().getGridContextClient().reset();
		return biz.executeQuery(queryString);
	}

	@Override
	public List<String>importAll()
	{
		biz.getBizContext().getGridContextClient().reset();
		return biz.importAll();
	}

	@Override
	public List<String> importServers(boolean isAll, String... gridPaths)
	{
		biz.getBizContext().getGridContextClient().reset();
		return biz.importServers(isAll, gridPaths);
	}

	@Override
	public List<String> importAll(Date asOfDate)
	{
		biz.getBizContext().getGridContextClient().reset();
		return biz.importAll(asOfDate);
	}

	@Override
	public List<String> importServers(Date asOfDate, boolean isAll, String... gridPaths)
	{
		biz.getBizContext().getGridContextClient().reset();
		return biz.importServers(asOfDate, isAll, gridPaths);
	}

	@Override
	public List<CacheDumpInfo> getCacheDumpInfoList(boolean isAll)
	{
		biz.getBizContext().getGridContextClient().reset();
		return biz.getCacheDumpInfoList(isAll);
	}

	@Override
	public CacheDumpInfo getCacheDumpInfo(boolean isAll)
	{
		biz.getBizContext().getGridContextClient().reset();
		return biz.getCacheDumpInfo(isAll);
	}

	@Override
	public void setCompositeKeyInfo(String gridPath, CompositeKeyInfo compositeKeyInfo)
	{
		biz.getBizContext().getGridContextClient().reset();
		biz.setCompositeKeyInfo(gridPath, compositeKeyInfo);
	}

	@Override
	public CompositeKeyInfo getCompositeKeyInfo(String gridPath)
	{
		biz.getBizContext().getGridContextClient().reset();
		return biz.getCompositeKeyInfo(gridPath);
	}
}
