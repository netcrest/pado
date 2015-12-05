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

import java.io.Serializable;
import java.util.Set;

import com.netcrest.pado.IGridRouter;

/**
 * GridPathInfo provides grid path information. A grid path defines a name space
 * in which data is stored by the underlying data grid. A grid path typically
 * represents a logical container of data. It can also contain child grid paths,
 * effectively providing hierarchical name spaces. Data that a grid path
 * represents is not limited to but normally a collection of key/value pairs.
 * Some other examples of a grid path are structured or unstructured text file,
 * binary file, database table, document, spreadsheet, and etc. Data translation
 * and transformation are performed by individual IBiz classes.
 * 
 * @author dpark
 * 
 */
public abstract class GridPathInfo implements Comparable<GridPathInfo>, Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Pado data type. Default is LOCAL.
	 * 
	 * @author dpark
	 */
	public enum DataType
	{
		/**
		 * Local to grid
		 */
		LOCAL,

		/**
		 * Replicate to multiple grids
		 */
		REPLICATE,

		/**
		 * Partition across multiple grids
		 */
		PARTITION
	};

	/**
	 * Grid access type. Default is PRIVATE
	 * 
	 * @author dpark
	 * 
	 */
	public enum AccessType
	{
		/**
		 * Access is limited to the clients logged in to the grid.
		 */
		PRIVATE,

		/**
		 * Access is granted to all clients
		 */
		PUBLIC
	};

	/**
	 * Grid Path. At a minimum gridPath must be defined
	 */
	protected String gridPath;

	/**
	 * Interit flag. If true, inherit config from the parent grid path. Default
	 * is true.
	 */
	protected boolean inherit = true;

	/**
	 * Data type. Default is LOCAL.
	 */
	protected DataType dataType = DataType.LOCAL;

	/**
	 * Access type. Default is PRIVATE.
	 */
	protected AccessType accessType = AccessType.PRIVATE;

	/**
	 * Set of IDS of grids in which this grid path is allowed.
	 */
	protected Set<String> gridIdSet;

	/**
	 * Temporal flag. If true, the grid path enables temporal data. Default is
	 * true.
	 */
	protected boolean temporalEnabled = true;

	/**
	 * Lucene flag. If true, Lucene indexes are automatically built. Default is
	 * false.
	 */
	protected boolean luceneEnabled = false;

	/**
	 * Fully-qualified key class name
	 */
	protected String keyClassName;

	/**
	 * Fully-qualified data class name
	 */
	protected String dataClassName;

	/**
	 * Fully qualified router class name. If undefined, then the default class
	 * is assigned.
	 */
	protected String routerClassName;

	/**
	 * Grid path description
	 */
	protected String description;

	/**
	 * Constructs an empty GridPathInfo object.
	 */
	public GridPathInfo()
	{
	}

	/**
	 * Constructs a GridPathInfo for the specified grid path.
	 * 
	 * @param gridPath
	 *            Grid path
	 */
	public GridPathInfo(String gridPath)
	{
		this.gridPath = gridPath;
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
	 * Returns true if this grid path inherits the parent attributes.
	 */
	public boolean isInherit()
	{
		return inherit;
	}

	/**
	 * Enables or disables inheritance. The default value is true.
	 * 
	 * @param inherit
	 *            true to enable or false to disable inheritance
	 */
	public void setInherit(boolean inherit)
	{
		this.inherit = inherit;
	}

	/**
	 * Enables or disables inheritances. The default value is "true".
	 * 
	 * @param inherit
	 *            "false" to disable inheritance. Others including null to
	 *            enable inheritance. Case insensitive.
	 */
	public void setInherit(String inherit)
	{
		this.inherit = inherit == null || inherit.equals("false") == false;
	}

	/**
	 * Returns the data type of this grid path.
	 */
	public DataType getDataType()
	{
		return dataType;
	}

	/**
	 * Sets the data type of this grid path. The default value is
	 * {@link DataType#LOCAL}.
	 * 
	 * @param dataType
	 *            Data type
	 */
	public void setDataType(DataType dataType)
	{
		this.dataType = dataType;
	}

	/**
	 * Returns the access type of this grid path.
	 */
	public AccessType getAccessType()
	{
		return accessType;
	}

	/**
	 * Sets the access type of this grid path. The default value is
	 * {@link AccessType#PRIVATE}.
	 * 
	 * @param accessType
	 */
	public void setAccessType(AccessType accessType)
	{
		this.accessType = accessType;
	}

	/**
	 * Sets the data type of this grid path.
	 * 
	 * @param dataType
	 *            One of "local", "partition", and "replicate". Case
	 *            insensitive. Other values including null default to "local".
	 */
	public void setDataType(String dataType)
	{
		if (dataType == null) {
			this.dataType = DataType.LOCAL;
		} else {
			if (dataType.equalsIgnoreCase(DataType.PARTITION.toString())) {
				this.dataType = DataType.PARTITION;
			} else if (dataType.equalsIgnoreCase(DataType.REPLICATE.toString())) {
				this.dataType = DataType.REPLICATE;
			} else {
				this.dataType = DataType.LOCAL;
			}
		}
	}

	/**
	 * Returns the access type of this grid path.
	 * 
	 * @param accessType
	 *            Access type. One of "public" and "private". Case insensitive.
	 *            Others values including null default to "private".
	 */
	public void setAccessType(String accessType)
	{
		if (accessType == null) {
			this.accessType = AccessType.PRIVATE;
		} else {
			if (accessType.equalsIgnoreCase("public")) {
				this.accessType = AccessType.PUBLIC;
			} else {
				this.accessType = AccessType.PRIVATE;
			}
		}
	}

	/**
	 * Sets the grid ID set. This only applies to the partition data type.
	 * 
	 * @param gridIdSet
	 *            Grid ID set
	 */
	public void setGridIdSet(Set<String> gridIdSet)
	{
		this.gridIdSet = gridIdSet;
	}

	/**
	 * Returns the gridID set. The returned grid IDs are useful only if the grid
	 * path is configured with the partition data type. It may return null if
	 * undefined.
	 */
	public Set<String> getGridIdSet()
	{
		return this.gridIdSet;
	}

	/**
	 * Returns true if this grid path contains temporal data.
	 */
	public boolean isTemporalEnabled()
	{
		return temporalEnabled;
	}

	/**
	 * Sets this grid path to contain temporal data.
	 * 
	 * @param enabled
	 *            true to enable or false to disable temporal data.
	 */
	public void setTemporal(boolean enabled)
	{
		this.temporalEnabled = enabled;
	}
	
	/**
	 * Returns true if Lucene indexes are built automatically for this path.
	 */
	public boolean isLuceneEnabled()
	{
		return luceneEnabled;
	}

	/**
	 * Enables/disables Lucenes indexes.
	 * 
	 * @param enabled
	 *            true to enable or false to disable Lucen indexes.
	 */
	public void setLuceneEnabled(boolean enabled)
	{
		this.luceneEnabled = enabled;
	}

	/**
	 * Sets this grid path to contain temporal data.
	 * 
	 * @param enabled
	 *            false to disable temporal data. Other values including null to
	 *            enable temporal data. Case insensitive. The default value is
	 *            true.
	 */
	public void setTemporalEnabled(boolean enabled)
	{
		this.temporalEnabled = enabled;
	}

	/**
	 * Returns the key class name.
	 */
	public String getKeyClassName()
	{
		return keyClassName;
	}

	/**
	 * Sets the key class name.
	 * 
	 * @param keyClassName
	 *            Key class name
	 */
	public void setKeyClassName(String keyClassName)
	{
		this.keyClassName = keyClassName;
	}

	/**
	 * Returns the data class name.
	 */
	public String getDataClassName()
	{
		return dataClassName;
	}

	/**
	 * Sets the data class name.
	 * 
	 * @param dataClassName
	 *            Data class name
	 */
	public void setDataClassName(String dataClassName)
	{
		this.dataClassName = dataClassName;
	}

	/**
	 * Returns the router class name.
	 * 
	 * @return Router class name
	 */
	public String getRouterClassName()
	{
		return routerClassName;
	}

	/**
	 * Sets the router class name.
	 * 
	 * @param routerClassName
	 *            Router class name
	 */
	public void setRouterClassName(String routerClassName)
	{
		this.routerClassName = routerClassName;
	}

	/**
	 * Returns a new instance of IGridRouter object.
	 * 
	 * @throws InstantiationException
	 *             Thrown if instantiation fails
	 * @throws IllegalAccessException
	 *             Thrown if security access fails
	 * @throws ClassNotFoundException
	 *             Thrown if the router class is not found
	 */
	public IGridRouter newGridRouterInstance() throws InstantiationException, IllegalAccessException,
			ClassNotFoundException
	{
		if (routerClassName == null) {
			throw new InstantiationException("Class name is null");
		} else {
			return (IGridRouter) Class.forName(routerClassName).newInstance();
		}
	}

	/**
	 * Returns the description of this grid path. Returns null if the
	 * description is not set.
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Sets the description.
	 * 
	 * @param description
	 *            Description.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * Compares grid paths.
	 */
	@Override
	public int compareTo(GridPathInfo anotherGridPathInfo)
	{
		if (anotherGridPathInfo == null) {
			return -1;
		} else if (gridPath == null) {
			return 1;
		}
		return gridPath.compareTo(anotherGridPathInfo.gridPath);
	}

	/**
	 * Returns the hash code of grid path.
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gridPath == null) ? 0 : gridPath.hashCode());
		return result;
	}

	/**
	 * Returns true if the specified object equals grid path.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GridPathInfo other = (GridPathInfo) obj;
		if (gridPath == null) {
			if (other.gridPath != null)
				return false;
		} else if (!gridPath.equals(other.gridPath))
			return false;
		return true;
	}
}
