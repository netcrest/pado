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

import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import com.netcrest.pado.IGridRouter;
import com.netcrest.pado.IGridService;
import com.netcrest.pado.annotation.RouterType;
import com.netcrest.pado.info.AppInfo;
import com.netcrest.pado.info.GridRouterInfo;

/**
 * GridService implements IGridService which provides grid metadata to the
 * underlying client proxy infrastructure for determining the target grid and
 * server and gathering context information before executing the target remote
 * method.
 * 
 * @author dpark
 * 
 */
public abstract class GridService implements IGridService
{
	/**
	 * The grid ID of this VM. This is null if the VM is a pure client.
	 */
	protected String gridId;
	protected boolean isGridParent;

	protected String appId;
	protected String padoGridId;
	protected String defaultGridId;
	protected Properties credentials;
	protected Object token;
	protected String username;

	protected IGridRouter defaultGridRouter;
	protected CostBasedGridRouter costBasedGridRouter;
	protected LocationBasedGridRouter locationBasedGridRouter;

	// <gridId>
	protected TreeSet<String> allowedGridIdSet;

	/**
	 * This constructor is invoked by servers.
	 * 
	 * @param gridId
	 *            Grid ID
	 * @param appId
	 *            App ID
	 * @param isGridParent
	 *            true if this JVM is a grid parent
	 */
	public GridService(String gridId, String appId, boolean isGridParent)
	{
		this(appId);
		this.gridId = gridId;
		this.isGridParent = isGridParent;
	}

	/**
	 * This constructor is invoked by clients.
	 * 
	 * @param appId
	 *            App ID
	 */
	public GridService(String appId)
	{
		this.appId = appId;
	}

	public abstract void refresh();

	/**
	 * Returns the grid ID of this VM. It returns null if this VM is a pure
	 * client.
	 */
	@Override
	public String getGridId()
	{
		return gridId;
	}

	/**
	 * Returns true if this VM is a pure client. It returns false if this VM is
	 * a grid.
	 */
	public boolean isPureClient()
	{
		return gridId == null;
	}

	/**
	 * Returns true if the specified grid ID is same as the grid ID returned by
	 * {@link #getGridId()}.
	 * 
	 * @param gridId
	 */
	public boolean isGridLocal(String gridId)
	{
		return this.gridId == null ? false : this.gridId.equals(gridId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAppId()
	{
		return appId;
	}

	/**
	 * Returns the root path of the specified grid ID.
	 * @param gridId Grid ID
	 */
	public abstract String getRootPath(String gridId);
	
	/**
	 * Returns the full path of the the specified grid ID.
	 * @param gridId Grid ID
	 * @param gridPath Grid path which is always relative to the root path.
	 */
	public abstract String getFullPath(String gridId, String gridPath);

	/**
	 * Returns a set of allowed grid IDs including live and dead. Note that the
	 * parent (pado) is also included in the returned set. It always returns a
	 * non-null set. To get all live grid IDs, invoke {@link #getGridIdSet()}.
	 */
	public Set<String> getAllowedGridIdSet()
	{
		return allowedGridIdSet;
	}
	
	/**
	 * Returns the Pado grid ID, i.e., the grid that this client is logged on to.
	 */
	public String getPadoGridId()
	{
		return padoGridId;
	}

	/**
	 * Returns the default grid ID for this app, which can be any of the grids
	 * returned by {@link #getGridIds()}. The default grid is the one that can
	 * satisfy most of IBiz data requirements, if not all. If the default grid
	 * does not completely satisfy IBiz, then depending on the pado
	 * configuration, other grids are implicitly or explicitly executed.
	 */
	public String getDefaultGridId()
	{
		return defaultGridId;
	}

	@Override
	public Object getToken()
	{
		return token;
	}

	@Override
	public String getUsername()
	{
		return username;
	}

	/**
	 * Returns a set of all live grid IDs. It also includes the parent (pado)
	 * ID. It always returns a non-null set.
	 */
	public abstract Set<String> getGridIdSet();

	/**
	 * Returns all live grid IDs. It also includes the parent (pado) IDs. It
	 * always returns a non-null array.
	 */
	public abstract String[] getGridIds();

	/**
	 * Returns IDs of grids that contain the specified grid path.
	 * 
	 * @param gridPath
	 *            Grid path
	 */
	public abstract String[] getGridIds(String gridPath);

	public abstract void remove(String gridId);

	/**
	 * Returns the grid ID of the specified full path
	 * 
	 * @param fullPath
	 *            Full path
	 */
	public String getGridId(String fullPath)
	{
		AppInfo appInfo = PadoClientManager.getPadoClientManager().getAppInfo(appId);
		if (appInfo != null) {
			return appInfo.getGridIdFromFullPath(fullPath);
		}
		return null;
	}

	/**
	 * Returns all grid relative paths in a sorted set.
	 */
	public abstract Set<String> getGridPathSet();

	/**
	 * Returns the grid router that determines the target grids to forward IBiz
	 * calls.
	 * 
	 * @param gridPath
	 *            Grid path
	 */
	public abstract IGridRouter getGridRouter(String gridPath);

	/**
	 * Returns the grid router info that contains the target grid information.
	 * 
	 * @param gridPath
	 *            Grid path relative to the root path
	 */
	public abstract GridRouterInfo getGridRouterInfo(String gridPath);

	public IGridRouter getGridRouter(RouterType routerType)
	{
		switch (routerType) {
		case COST:
			return costBasedGridRouter;
		case LOCATION:
			return locationBasedGridRouter;
			// all targets all grids, router is for targeting a single grid
		case ALL:
			return null;
		default:
			return defaultGridRouter;
		}
	}
}
