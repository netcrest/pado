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

public abstract class CacheHeaderInfo implements Comparable<CacheHeaderInfo>
{
	/**
	 * Grid ID
	 */
	protected String gridId;

	/**
	 * Cache name or server name
	 */
	protected String name;

	/**
	 * Cache ID or server ID
	 */
	protected String id;

	/**
	 * Host or IP address
	 */
	protected String host;

	/**
	 * Process ID
	 */
	protected int processId;

	/**
	 * Constructs a CacheHeaderInfo object.
	 */
	public CacheHeaderInfo()
	{
	}

	/**
	 * Constructs a CacheHeaderInfo object describing a cache instance in the
	 * specified grid.
	 * 
	 * @param gridId
	 *            Grid ID
	 */
	public CacheHeaderInfo(String gridId)
	{
		this.gridId = gridId;
	}
	
	/**
	 * Returns the grid ID.
	 */
	public String getGridId()
	{
		return gridId;
	}
	
	/**
	 * Returns the name of the cache instance. This name may or may not be
	 * unique depending on the underlying data product. If in doubt, always use
	 * {@link #getId()} to obtain the unique ID. Note that the name is typically
	 * provided so that it is more legible than the ID.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the ID of the cache instance. This ID is guaranteed to be unique
	 * where as the cache name may or may not be unique depending on the
	 * underlying data grid product.
	 */
	public Object getId()
	{
		return id;
	}

	/**
	 * Returns the host name. The host name may be an IP address of the host
	 * depending on how Pado is configured.
	 * 
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * Returns the process ID of the cache instance.
	 */
	public int getProcessId()
	{
		return processId;
	}
	
	/**
	 * Compares CacheInfo. The order of CacheInfo attribute comparison is name,
	 * host, and id.
	 */
	@Override
	public int compareTo(CacheHeaderInfo anotherCacheInfo)
	{
		int compared = -1;
		if (name != null) {
			compared = name.compareTo(anotherCacheInfo.name);
		}
		if (compared == 0) {
			if (host != null) {
				compared = host.compareTo(anotherCacheInfo.host);
			}
			if (compared == 0) {
				if (id != null) {
					compared = id.compareTo(anotherCacheInfo.id);
				}
			}
		}
		return compared;
	}

	@Override
	public String toString()
	{
		return "CacheHeaderInfo [gridId=" + gridId + ", name=" + name + ", id=" + id + ", host=" + host
				+ ", processId=" + processId + "]";
	}
}
