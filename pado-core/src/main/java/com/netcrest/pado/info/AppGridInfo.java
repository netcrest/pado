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

import java.util.Arrays;

/**
 * AppGridInfo is a light weight grid metadata class that contains grid specific
 * information. To obtain comprehensive grid details, use {@link GridInfo}. Note
 * that this AppGridInfo objects are created in the server using GridInfo by
 * invoking {@link #update(GridInfo)}.
 * 
 * @author dpark
 * 
 */
public abstract class AppGridInfo implements Comparable<AppGridInfo>
{
	/**
	 * Grid ID
	 */
	protected String gridId;
	/**
	 * Grid location
	 */
	protected String location;

	/**
	 * Grid locators
	 */
	protected String locators;

	/**
	 * Parent grid IDs
	 */
	protected String parentGridIds[];

	/**
	 * Child grid IDs
	 */
	protected String childGridIds[];

	/**
	 * Client locators
	 */
	protected String clientLocators;

	/**
	 * Connection name
	 */
	protected String connectionName;

	/**
	 * Shared connection name
	 */
	protected String sharedConnectionName;

	/**
	 * Index Matrix connection name used by the grid
	 */
	protected String indexMatrixConnectionName;

	/**
	 * Client Index Matrix connection name used by the grid
	 */
	protected String clientIndexMatrixConnectionName;

	/**
	 * Client connection name
	 */
	protected String clientConnectionName;

	/**
	 * Client shared connection name
	 */
	protected String clientSharedConnectionName;
	
	/**
	 * Root path info
	 */
	protected PathInfo rootPathInfo;
	
	/**
	 * Grid root full path
	 */
	protected String gridRootPath;


	/**
	 * Constructs a new empty AppGridInfo object.
	 */
	public AppGridInfo()
	{
	}

	/**
	 * Constructions a new AppGridInfo object containing information extracted
	 * from the specified GridInfo object.
	 * 
	 * @param gridInfo
	 *            Grid inforamtion
	 */
	public AppGridInfo(GridInfo gridInfo)
	{
		update(gridInfo);
	}

	/**
	 * Returns the grid ID.
	 */
	public String getGridId()
	{
		return gridId;
	}

	/**
	 * Sets the specified grid ID.
	 * 
	 * @param gridId
	 *            Grid ID
	 */
	public void setGridId(String gridId)
	{
		this.gridId = gridId;
	}

	/**
	 * Returns the grid location.
	 */
	public String getLocation()
	{
		return location;
	}

	/**
	 * Sets the specified grid location.
	 * 
	 * @param location
	 *            Grid location
	 */
	public void setLocation(String location)
	{
		this.location = location;
	}

	/**
	 * Returns the locators used by grids to connect to the grid.
	 */
	public String getLocators()
	{
		return locators;
	}

	/**
	 * Sets the locators used by grids to connect to the grid.
	 * 
	 * @param locators
	 *            Grid locators
	 */
	public void setLocators(String locators)
	{
		this.locators = locators;
	}

	/**
	 * Sets the locators used by clients to connect to the grid.
	 */
	public String getClientLocators()
	{
		return clientLocators;
	}

	/**
	 * Sets the locators used by clients to connect to the grid.
	 * 
	 * @param clientLocators
	 *            Client locators
	 */
	public void setClientLocators(String clientLocators)
	{
		this.clientLocators = clientLocators;
	}

	/**
	 * Returns IDs of all parent grids that the grid is connected to.
	 *
	 */
	public String[] getParentGridIds()
	{
		return parentGridIds;
	}

	/**
	 * Sets the specified parent grid IDs.
	 * 
	 * @param parentGridIds
	 *            Parent grid IDs
	 */
	public void setParentGridIds(String[] parentGridIds)
	{
		this.parentGridIds = parentGridIds;
	}

	/**
	 * Returns IDs of all child grids that the grid is connected to.
	 */
	public String[] getChildGridIds()
	{
		return childGridIds;
	}

	/**
	 * Sets IDs of all child grids that the grid is connected to.
	 * 
	 * @param childGridIds
	 */
	public void setChildGridIds(String[] childGridIds)
	{
		this.childGridIds = childGridIds;
	}
	

	/**
	 * Returns the name of the connection that the clients used to connect to
	 * the grid.
	 */
	public String getConnectionName()
	{
		return connectionName;
	}

	/**
	 * Sets the name of the connection that the clients used to connect to the
	 * grid.
	 * 
	 * @param connectionName
	 */
	public void setConnectionName(String connectionName)
	{
		this.connectionName = connectionName;
	}

	/**
	 * Returns the name of the shared connection that the grids used to connect
	 * to the grid.
	 */
	public String getSharedConnectionName()
	{
		return sharedConnectionName;
	}

	/**
	 * Sets the name of the shared connection that the grids used to connect to
	 * the grid.
	 * 
	 * @param sharedConnectionName
	 *            Shared connection name
	 */
	public void setSharedConnectionName(String sharedConnectionName)
	{
		this.sharedConnectionName = sharedConnectionName;
	}

	/**
	 * Returns the name of the Index Matrix connection the grids used to connect
	 * to the grid.
	 */
	public String getIndexMatrixConnectionName()
	{
		return indexMatrixConnectionName;
	}

	/**
	 * Sets the name of the Index Matrix connection the grids used to connect to
	 * the grid.
	 * 
	 * @param indexMatrixConnectionName
	 *            Index Matrix connection name
	 */
	public void setIndexMatrixConnectionName(String indexMatrixConnectionName)
	{
		this.indexMatrixConnectionName = indexMatrixConnectionName;
	}

	/**
	 * Returns the name of the Index Matrix connection the clients used to
	 * connect to the grid.
	 */
	public String getClientIndexMatrixConnectionName()
	{
		return clientIndexMatrixConnectionName;
	}

	/**
	 * Sets the name of the Index Matrix connection the clients used to connect
	 * to the grid.
	 * 
	 * @param clientIndexMatrixConnectionName
	 *            Client Index Matrix connection name
	 */
	public void setClientIndexMatrixConnectionName(String clientIndexMatrixConnectionName)
	{
		this.clientIndexMatrixConnectionName = clientIndexMatrixConnectionName;
	}

	/**
	 * Returns the name of the connection that the clients used to connect to
	 * the grid.
	 */
	public String getClientConnectionName()
	{
		return clientConnectionName;
	}

	/**
	 * Sets the name of the connection that the clients used to connect to the
	 * grid.
	 * 
	 * @param clientConnectionName
	 *            Client connection name
	 */
	public void setClientConnectionName(String clientConnectionName)
	{
		this.clientConnectionName = clientConnectionName;
	}

	/**
	 * Returns the name of the shared connection that the clients used to
	 * connect to the grid.
	 */
	public String getClientSharedConnectionName()
	{
		return clientSharedConnectionName;
	}

	/**
	 * Sets the name of the shared connection that the clients used to connect
	 * to the grid.
	 * 
	 * @param clientSharedConnectionName
	 *            Client shared connection name
	 */
	public void setClientSharedConnectionName(String clientSharedConnectionName)
	{
		this.clientSharedConnectionName = clientSharedConnectionName;
	}
	
	public PathInfo getRootPathInfo()
	{
		return rootPathInfo;
	}
	
	/**
	 * Returns this grid's root path. The root path always begins with "/".
	 */
	public String getGridRootPath()
	{
		return gridRootPath;
	}

	/**
	 * Sets this grid's root path.
	 * 
	 * @param gridRootPath
	 *            Root path that begins with "/".
	 */
	public void setGridRootPath(String gridRootPath)
	{
		this.gridRootPath = gridRootPath;
	}

	/**
	 * Returns the hash code determined by select fields.
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(childGridIds);
		result = prime * result + ((clientLocators == null) ? 0 : clientLocators.hashCode());
		result = prime * result + ((gridId == null) ? 0 : gridId.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((locators == null) ? 0 : locators.hashCode());
		result = prime * result
				+ ((clientIndexMatrixConnectionName == null) ? 0 : clientIndexMatrixConnectionName.hashCode());
		result = prime * result + ((clientConnectionName == null) ? 0 : clientConnectionName.hashCode());
		result = prime * result + Arrays.hashCode(parentGridIds);
		result = prime * result + ((clientSharedConnectionName == null) ? 0 : clientSharedConnectionName.hashCode());
		result = prime * result + ((indexMatrixConnectionName == null) ? 0 : indexMatrixConnectionName.hashCode());
		result = prime * result + ((connectionName == null) ? 0 : connectionName.hashCode());
		result = prime * result + ((sharedConnectionName == null) ? 0 : sharedConnectionName.hashCode());
		return result;
	}

	/**
	 * Returns true if the specified object has the same field values
	 * as this object.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof AppGridInfo == false) // @dpark
			return false;
		AppGridInfo other = (AppGridInfo) obj;
		if (!Arrays.equals(childGridIds, other.childGridIds))
			return false;
		if (clientLocators == null) {
			if (other.clientLocators != null)
				return false;
		} else if (!clientLocators.equals(other.clientLocators))
			return false;
		if (gridId == null) {
			if (other.gridId != null)
				return false;
		} else if (!gridId.equals(other.gridId))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (locators == null) {
			if (other.locators != null)
				return false;
		} else if (!locators.equals(other.locators))
			return false;
		if (!Arrays.equals(parentGridIds, other.parentGridIds))
			return false;
		if (clientIndexMatrixConnectionName == null) {
			if (other.clientIndexMatrixConnectionName != null)
				return false;
		} else if (!clientIndexMatrixConnectionName.equals(other.clientIndexMatrixConnectionName))
			return false;
		if (clientConnectionName == null) {
			if (other.clientConnectionName != null)
				return false;
		} else if (!clientConnectionName.equals(other.clientConnectionName))
			return false;
		if (clientSharedConnectionName == null) {
			if (other.clientSharedConnectionName != null)
				return false;
		} else if (!clientSharedConnectionName.equals(other.clientSharedConnectionName))
			return false;
		if (indexMatrixConnectionName == null) {
			if (other.indexMatrixConnectionName != null)
				return false;
		} else if (!indexMatrixConnectionName.equals(other.indexMatrixConnectionName))
			return false;
		if (connectionName == null) {
			if (other.connectionName != null)
				return false;
		} else if (!connectionName.equals(other.connectionName))
			return false;
		if (sharedConnectionName == null) {
			if (other.sharedConnectionName != null)
				return false;
		} else if (!sharedConnectionName.equals(other.sharedConnectionName))
			return false;
		return true;
	}

	/**
	 * Compares the grid IDs of the specified AppGridInfo object.
	 */
	@Override
	public int compareTo(AppGridInfo anotherAppGridInfo)
	{
		return gridId == null ? -1 : gridId.compareTo(anotherAppGridInfo.gridId);
	}
	
	// -----------------------------------------------------------------------
	// Abstract methods
	// -----------------------------------------------------------------------
	
	/**
	 * Updates this object with contents of the specified GridInfo object
	 * 
	 * @param gridInfo
	 * @return Returns true if the passed in GridInfo object is different than
	 *         this object and this object has been updated.
	 */
	public abstract boolean update(GridInfo gridInfo);
}
