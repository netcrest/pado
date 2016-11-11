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

import java.util.Set;

import com.netcrest.pado.IGridCollector;
import com.netcrest.pado.IGridContextClient;
import com.netcrest.pado.annotation.RouterType;

@SuppressWarnings("rawtypes")
public class GridContextClientImpl implements IGridContextClient
{
	private Set<?> routingKeys;
	private String targetPath;
	private String[] gridIds;
	private IGridCollector gridCollector;
	private boolean padoAsTarget;
	private RouterType routerType;
	private String location;
	private Object[] additionalArgs;
	private transient Object[] transientData;
	private Object productData;
	
	public GridContextClientImpl()
	{}
	
	@Override
	public Set<?> getRoutingKeys()
	{
		return routingKeys;
	}

	@Override
	public void setRoutingKeys(Set<?> routingKeys)
	{
		this.routingKeys = routingKeys;
	}
	
	@Override
	public void setGridPath(String targetPath)
	{
		this.targetPath = targetPath;
	}

	@Override
	public String getGridPath()
	{
		return targetPath;
	}
	
	@Override
	public void setGridIds(String... gridIds)
	{
		this.gridIds = gridIds;
	}

	@Override
	public String[] getGridIds()
	{
		return gridIds;
	}

	@Override
	public void setGridCollector(IGridCollector gridCollector)
	{
		this.gridCollector = gridCollector;
	}

	@Override
	public IGridCollector getGridCollector()
	{
		return gridCollector;
	}

	@Override
	public boolean isPadoAsTarget()
	{
		return padoAsTarget;
	}

	@Override
	public void setPadoAsTarget(boolean padoAsTarget)
	{
		this.padoAsTarget = padoAsTarget;
	}
	
	@Override
	public void setRouterType(RouterType routerType)
	{
		this.routerType = routerType;
	}

	@Override
	public RouterType getRouterType()
	{
		return routerType;
	}
	
	@Override
	public void setGridLocation(String location)
	{
		this.location = location;
	}

	@Override
	public String getGridLocation()
	{
		return location;
	}
	
	@Override
	public void setAdditionalArguments(Object... args)
	{
		this.additionalArgs = args;
	}

	@Override
	public Object[] getAttionalArguments()
	{
		return this.additionalArgs;
	}
	
	@Override
	public void setTransientData(Object... transientData)
	{
		this.transientData = transientData;
	}

	@Override
	public Object[] getTransientData()
	{
		return this.transientData;
	}
	
	@Override
	public void reset()
	{
		setGridIds();
		setAdditionalArguments();
		setGridCollector(null);
		setGridLocation(null);
		setProductSpecificData(null);
		setRouterType(null);
		setRoutingKeys(null);
		setPadoAsTarget(false);
		setTransientData();
	}

	@Override
	public void setProductSpecificData(Object productData)
	{
		this.productData = productData;
	}

	@Override
	public Object getProductSpecificData()
	{
		return productData;
	}

}
