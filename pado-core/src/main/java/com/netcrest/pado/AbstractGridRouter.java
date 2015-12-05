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
package com.netcrest.pado;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

import com.netcrest.pado.internal.impl.GridRoutingTable;

/**
 * AbstractGridRouter provides basic routing information required by most
 * routers.
 * 
 * @author dpark
 * 
 */
public abstract class AbstractGridRouter implements IGridRouter, Externalizable
{
	private static final long serialVersionUID = 1L;

	protected Set<String> allowedGridIdSet;
	protected transient String allowedGridIds[];
	protected Set<String> gridIdSet = new HashSet<String>(3);
	protected transient String gridIds[];

	/**
	 * Constructs an empty grid router with the default settings.
	 */
	public AbstractGridRouter()
	{
	}

	/**
	 * Constructs an empty grid router with the specified grid ID set.
	 * 
	 * @param gridIdSet
	 *            Grid ID set
	 */
	public AbstractGridRouter(Set<String> gridIdSet)
	{
		setGridIdSet(gridIdSet);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract Type getType();

	/**
	 * Returns the ID of the grid that has the least cost from the specified
	 * grid IDs.
	 * 
	 * @param context
	 *            Biz context client
	 * @param gridIds
	 *            Grid IDs. If null then this grid router's grid IDs are
	 *            substitued.
	 */
	protected String getLeastCostGridId(IBizContextClient context, String... gridIds)
	{
		if (gridIds == null || gridIds.length == 0) {
			gridIds = this.gridIds;
		}
		if (gridIds == null || gridIds.length == 0 || gridIds.length > 1) {
			GridRoutingTable routingTable = GridRoutingTable.getGridRoutingTable(context.getGridService().getAppId());
			GridRoutingTable.Grid grid = routingTable.getLeastCostGrid(gridIds);
			if (grid == null) {
				return context.getGridService().getDefaultGridId();
			} else {
				return grid.getGridId();
			}
		} else {
			return gridIds[0];
		}
	}

	/**
	 * Returns true if this router contains the specified grid ID.
	 * 
	 * @param gridId
	 *            Grid ID
	 */
	protected boolean containsGridId(String gridId)
	{
		for (String gid : gridIds) {
			if (gid.equals(gridId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns all grid IDs managed by this router.
	 */
	public String[] getGridIds()
	{
		return this.gridIds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addGridId(String gridId)
	{
		gridIdSet.add(gridId);
		gridIds = gridIdSet.toArray(new String[gridIdSet.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeGridId(String gridId)
	{
		gridIdSet.remove(gridId);
		gridIds = gridIdSet.toArray(new String[gridIdSet.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setGridIdSet(Set<String> gridIdSet)
	{
		if (gridIdSet == null) {
			this.gridIdSet = new HashSet<String>(3);
		} else {
			this.gridIdSet = gridIdSet;
		}
		this.gridIds = this.gridIdSet.toArray(new String[this.gridIdSet.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getGridIdSet()
	{
		return gridIdSet;
	}

	/**
	 * Writes to external serialization.
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeObject(allowedGridIdSet);
		out.writeObject(gridIdSet);
	}

	/**
	 * Reads from external serialization.
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		allowedGridIdSet = (HashSet) in.readObject();
		if (allowedGridIdSet != null) {
			allowedGridIds = allowedGridIdSet.toArray(new String[allowedGridIdSet.size()]);
		}
		gridIdSet = (HashSet) in.readObject();
		if (gridIdSet != null) {
			gridIds = gridIdSet.toArray(new String[gridIdSet.size()]);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return Returns null. The sub-class should override this method if it
	 *         needs to determine the grid ID that is associated with the
	 *         specified grid path key.
	 */
	@Override
	public String getReachableGridIdForPath(IBizContextClient context, Object key)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return Returns null. The sub-class should override this method if it
	 *         needs to determine the grid ID that is associated with the
	 *         specified grid path key.
	 */
	@Override
	public String findGridIdForPath(IBizContextClient context, Object key)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return Returns null. Ths sub-class should override this method if it
	 *         needs to select a grid ID from one ore more grid IDs.
	 */
	@Override
	public String getGridIdForNode(IBizContextClient context, String... gridIds)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAllowedGridIdSet(Set<String> allowedGridIdSet)
	{
		this.allowedGridIdSet = allowedGridIdSet;
		if (this.allowedGridIdSet != null) {
			allowedGridIds = allowedGridIdSet.toArray(new String[allowedGridIdSet.size()]);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getAllowedGridIdSet()
	{
		if (allowedGridIdSet == null) {
			return gridIdSet;
		} else {
			return allowedGridIdSet;
		}
	}
}
