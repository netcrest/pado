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
package com.netcrest.pado.biz.gemfire.proxy.functions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.IDataContext;
import com.netcrest.pado.IGridContextServer;
import com.netcrest.pado.IUserContext;
import com.netcrest.pado.IUserPrincipal;

public class BizContextServerSingletonImpl implements IBizContextServer
{
	private static final long serialVersionUID = 1L;
	
	private final Map<Long, Context> clientContextMap = new ConcurrentHashMap<Long, Context>(10);

	public BizContextServerSingletonImpl()
	{
	}
	
	public void addBizContextClient(BizContextClientImpl bizContextClient, FunctionContext fc, Object[] additionalArgs, Object[] transientData)
	{
		clientContextMap.put(Thread.currentThread().getId(), new Context(bizContextClient, fc, additionalArgs, transientData));
	}
	
	public void removeBizContextClient()
	{
		clientContextMap.remove(Thread.currentThread().getId());
	}
	
	private BizContextClientImpl getBizContextClient()
	{
		Context context = clientContextMap.get(Thread.currentThread().getId());
		if (context == null) {
			return null;
		}
		return context.client;
	}
	
	@Override
	public IUserContext getUserContext()
	{
		BizContextClientImpl client = getBizContextClient();
		if (client == null) {
			return null;
		}
		return client.getUserContext();
	}

	@Override
	public IDataContext getDataContext()
	{
		BizContextClientImpl client = getBizContextClient();
		if (client == null) {
			return null;
		}
		return client.getDataContext();
	}

	@Override
	public IGridContextServer getGridContextServer()
	{
		Context context = clientContextMap.get(Thread.currentThread().getId());
		if (context == null) {
			return null;
		}
		return context.gridContext;
	}

	@Override
	public IUserPrincipal getUserPrincipal()
	{
		IUserPrincipal principal = getUserContext().getUserInfo().getUserPrincipal();
		return principal;
	}
	
	
	class Context
	{
		BizContextClientImpl client;
		GemfireGridContextServerImpl gridContext;
		
		Context(BizContextClientImpl client, FunctionContext fc, Object[] additionalArgs, Object[] transientData)
		{
			this.client = client;
			this.gridContext = new GemfireGridContextServerImpl(fc, additionalArgs, transientData);
		}
	}
}