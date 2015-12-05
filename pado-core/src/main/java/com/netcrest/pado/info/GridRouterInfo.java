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
package com.netcrest.pado.info;

import java.util.HashSet;
import java.util.Set;

import com.netcrest.pado.IGridRouter;
import com.netcrest.pado.internal.impl.DefaultGridRouter;

/**
 * GridRouterInfo provides router information of its grid path.
 * 
 * @author dpark
 * 
 */
public abstract class GridRouterInfo
{
	/**
	 * Grid path
	 */
	protected String gridPath;

	/**
	 * Grid router. If undefined then the default router is assigned.
	 */
	protected IGridRouter gridRouter = new DefaultGridRouter();

	/**
	 * Disabled set of grid IDs. Disabled grids are not accessible until they
	 * are enabled. A grid may be disable itself from the Pado cloud to block
	 * clients and grids from accessing its data and services. A disabled grid
	 * can still access other grids, however. It can also enable itself at any
	 * time to re-join the Pado cloud to provide data and services.
	 */
	protected Set<String> disabledGridIdSet = new HashSet<String>(2);

	/**
	 * Constructs an empty GridRouterInfo object.
	 */
	public GridRouterInfo()
	{
	}

	/**
	 * Constructs a GridRouterInfo object for the specified grid path.
	 * 
	 * @param gridPath
	 *            Grid path
	 */
	public GridRouterInfo(String gridPath)
	{
		this.gridPath = gridPath;
	}

	/**
	 * Adds the specified grid ID. Grid path may span one or more grids.
	 * 
	 * @param gridId
	 *            Grid ID
	 */
	public void addGridId(String gridId)
	{
		gridRouter.addGridId(gridId);
	}

	/**
	 * Removes the specified grid ID.
	 * 
	 * @param gridId
	 *            Grid ID
	 */
	public void removeGridId(String gridId)
	{
		gridRouter.removeGridId(gridId);
	}

	/**
	 * Returns the set of grid IDs that this router in determining the target
	 * grid(s) when making IBiz calls.
	 */
	public Set<String> getGridIdSet()
	{
		return gridRouter.getGridIdSet();
	}

	public void setGridEnabled(String gridId, boolean enabled)
	{
		if (getGridIdSet().contains(gridId)) {
			getGridIdSet().remove(gridId);
			disabledGridIdSet.add(gridId);
		}
	}

	/**
	 * Returns true if the specified grid ID is enabled in this router.
	 * 
	 * @param gridId
	 *            Grid ID
	 */
	public boolean isGridEnabled(String gridId)
	{
		return getGridIdSet().contains(gridId);
	}

	/**
	 * Returns the grid path.
	 */
	public String getGridPath()
	{
		return gridPath;
	}

	/**
	 * Sets the grid path.
	 * 
	 * @param gridPath
	 *            Grid path
	 */
	public void setGridPath(String gridPath)
	{
		this.gridPath = gridPath;
	}

	/**
	 * Returns the grid router.
	 */
	public IGridRouter getGridRouter()
	{
		return gridRouter;
	}

	/**
	 * Sets the grid router.
	 * 
	 * @param gridRouter
	 *            Grid router
	 */
	public void setGridRouter(IGridRouter gridRouter)
	{
		this.gridRouter = gridRouter;
	}
}
