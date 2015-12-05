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
 * LocationBasedGridRouter selects the grid Id that is in the specified
 * geographical location and has the least cost in terms of
 * latency. The location must be specified via {@link IBizContextClient#getGridContextClient()}.
 * The cost is computed by GridRoutingTable. If the grid Id cannot be
 * determined by GridRoutingTable then it selects the default grid Id provided
 * by GridService.
 * 
 * @author dpark
 * 
 */
public class LocationBasedGridRouter extends AbstractGridRouter
{
	private static final long serialVersionUID = 1L;
	
	public LocationBasedGridRouter()
	{
	}

	public LocationBasedGridRouter(Set<String> gridIdSet)
	{
		super(gridIdSet);
	}

	@Override
	public Type getType()
	{
		return Type.LOCATION;
	}
	
	@Override
	public String getReachableGridIdForPath(IBizContextClient context, Object key)
	{
		return getLeastLocationCostGridId(context, getGridIds());
	}
	
	@Override
	public String getGridIdForNode(IBizContextClient context, String... gridIds)
	{
		return getLeastLocationCostGridId(context, gridIds);
	}
	
	@Override
	public String findGridIdForPath(IBizContextClient context, Object key)
	{
		return getReachableGridIdForPath(context, key);
	}

	private String getLeastLocationCostGridId(IBizContextClient context, String...gridIds)
	{
		String location = context.getGridContextClient().getGridLocation();
		if (location == null) {
			return getLeastCostGridId(context, gridIds);
		} else {
			GridRoutingTable routingTable = GridRoutingTable.getGridRoutingTable(context.getGridService().getAppId());
			GridRoutingTable.Grid grid = routingTable.getLeastCostLocation(location, gridIds);
			if (grid == null) {
				return context.getGridService().getDefaultGridId();
			} else {
				return grid.getGridId();
			}
		}
	}
}
