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

import com.netcrest.pado.IBizContextClient;

/**
 * StickyGridRouter randomly selects and uses a grid ID until the grid
 * become unavailable.
 * 
 * @author dpark
 * 
 */
public class StickyGridRouter extends RandomGridRouter
{
	private static final long serialVersionUID = 1L;
	
	private String stickyGridId;
	
	public StickyGridRouter()
	{
	}

	public StickyGridRouter(Set<String> gridIdSet)
	{
		super(gridIdSet);
	}
	
	@Override
	public Type getType()
	{
		return Type.STICKY;
	}
	
	@Override
	public String getReachableGridIdForPath(IBizContextClient context, Object key)
	{
		if (stickyGridId == null || containsGridId(stickyGridId) == false) {
			stickyGridId = super.getReachableGridIdForPath(context, key);
		}
		return stickyGridId;
	}

	@Override
	public String getGridIdForNode(IBizContextClient context, String... gridIds)
	{
		if (stickyGridId == null || containsGridId(stickyGridId) == false) {
			stickyGridId = super.getGridIdForNode(context, gridIds);
		}
		return stickyGridId;
	}
	
}
