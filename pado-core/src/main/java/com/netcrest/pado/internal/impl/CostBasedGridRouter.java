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
package com.netcrest.pado.internal.impl;

import java.util.Set;

import com.netcrest.pado.AbstractGridRouter;
import com.netcrest.pado.IBizContextClient;

/**
 * CostBasedGridRouter selects the grid ID that has the least cost in terms of
 * latency. The cost is computed by GridRoutingTable. If the grid Id cannot be
 * determined by GridRoutingTable then it selects the default grid Id provided
 * by GridService.
 * 
 * @author dpark
 * 
 */
public class CostBasedGridRouter extends AbstractGridRouter
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an empty cost-based router
	 */
	public CostBasedGridRouter()
	{
	}

	/**
	 * Constructs a cost-base router that determines the target grid from the
	 * specified grid ID set.
	 * 
	 * @param gridIdSet
	 *            Grid ID set
	 */
	public CostBasedGridRouter(Set<String> gridIdSet)
	{
		super(gridIdSet);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Type getType()
	{
		return Type.COST;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getReachableGridIdForPath(IBizContextClient context, Object key)
	{
		return getLeastCostGridId(context, getGridIds());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String findGridIdForPath(IBizContextClient context, Object key)
	{
		return getReachableGridIdForPath(context, key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getGridIdForNode(IBizContextClient context, String... gridIds)
	{
		return getLeastCostGridId(context, gridIds);
	}
}
