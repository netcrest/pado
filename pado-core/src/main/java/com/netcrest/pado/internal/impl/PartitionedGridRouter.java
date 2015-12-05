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
import com.netcrest.pado.temporal.ITemporalKey;

/**
 * PartitionedGridRouter selects the grid Id based on the number of grid IDs
 * registered in Pado. It computes key.hashCode() mod <number of grids> to
 * determine the target grid ID. Note that ParitionedGridRouter does not properly
 * adjust to dynamic changes of grid Ids. Its use is limited to a fixed number
 * of grid IDs.
 * 
 * @author dpark
 * 
 */
public class PartitionedGridRouter extends AbstractGridRouter
{
	private static final long serialVersionUID = 1L;
		
	public PartitionedGridRouter()
	{
	}

	public PartitionedGridRouter(Set<String> gridIdSet)
	{
		super(gridIdSet);
	}
	
	@Override
	public Type getType()
	{
		return Type.PARTITIONED;
	}
	
//	/**
//	 * {@inheritDoc}}}
//	 */
//	@Override
//	public void setGridIdSet(Set<String> gridIdSet)
//	{
//		if (gridIdSet == null) {
//			this.gridIdSetFixed = new HashSet(3);
//		} else {
//			this.gridIdSetFixed = gridIdSet;
//		}
//		this.gridIdsFixed = gridIdSetFixed.toArray(new String[gridIdSetFixed.size()]);
//	}

	@Override
	public String getReachableGridIdForPath(IBizContextClient context, Object key)
	{
		String gridId = findGridIdForPath(context, key);
		if (gridId != null && allowedGridIdSet.contains(gridId)) {
			return gridId;
		} else {
			return null;
		}
	}
	
	@Override
	public String findGridIdForPath(IBizContextClient context, Object key)
	{
		String gids[] = allowedGridIds;
		if (gids == null || gids.length == 0) {
			return context.getGridService().getDefaultGridId();
		}
		int gridIndex;
		if (key instanceof ITemporalKey) {
			gridIndex = Math.abs(((ITemporalKey) key).getIdentityKey().hashCode() % gids.length);
		} else {
			gridIndex = Math.abs(key.hashCode() % gids.length);
		}
		return gids[gridIndex];
	}

	@Override
	public String getGridIdForNode(IBizContextClient context, String... gridIds)
	{
		return getLeastCostGridId(context, gridIds);
	}
}
