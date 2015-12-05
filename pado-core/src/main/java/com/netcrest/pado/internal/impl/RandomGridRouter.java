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

import java.util.Random;
import java.util.Set;

import com.netcrest.pado.AbstractGridRouter;
import com.netcrest.pado.IBizContextClient;

/**
 * RandomGridRouter randomly selects a grid ID. It returns the 
 * default grid ID configured by Pado if grid IDs are not found.
 * 
 * @author dpark
 * 
 */
public class RandomGridRouter extends AbstractGridRouter
{
	private static final long serialVersionUID = 1L;
	
	protected static final Random random = new Random();
	
	public RandomGridRouter()
	{
	}

	public RandomGridRouter(Set<String> gridIdSet)
	{
		super(gridIdSet);
	}
	
	@Override
	public Type getType()
	{
		return Type.RANDOM;
	}

	@Override
	public String getReachableGridIdForPath(IBizContextClient context, Object key)
	{
		return getGridIdForNode(context, this.gridIds);
	}
	
	@Override
	public String findGridIdForPath(IBizContextClient context, Object key)
	{
		return getReachableGridIdForPath(context, key);
	}

	@Override
	public String getGridIdForNode(IBizContextClient context, String... gridIds)
	{
		String gids[] = gridIds;
		if (gids == null || gids.length == 0) {
			return context.getGridService().getDefaultGridId();
		}
		return gids[random.nextInt(gids.length)];
	}
}
