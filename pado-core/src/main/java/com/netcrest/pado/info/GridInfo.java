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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * GridInfo provides individual grid information. In Pado, a grid is comprised
 * of one or more replicated clusters of servers and owns a unique name space
 * identified by its grid ID.
 * 
 * @author dpark
 * 
 */
public abstract class GridInfo extends AppGridInfo
{
	/**
	 * CacheInf object
	 */
	protected CacheInfo cacheInfo;

	/**
	 * Set of BizInfo objects that the grid manages at the time of this object
	 * creation
	 */
	protected Set<BizInfo> bizSet;

	/**
	 * Map of &lt;&lt;grid-path&gt;, List&lt;ServerInfo&gt;&gt; pairs
	 */
	protected transient Map<String, List<ServerInfo>> serverInfoMap = new TreeMap<String, List<ServerInfo>>();

	/**
	 * List of all CacheInfo objects representing all grid servers.
	 */
	protected List<CacheInfo> cacheInfoList;

	/**
	 * Root path info
	 */
	protected transient PathInfo rootPathInfo;

	/**
	 * Constructs an empty GridInfo object.
	 */
	public GridInfo()
	{
	}

	/**
	 * Returns the CacheInfo object that provides the process-level cache
	 * information.
	 */
	public CacheInfo getCacheInfo()
	{
		return cacheInfo;
	}

	/**
	 * Sets the CacheInfo object.
	 * 
	 * @param cacheInfo
	 *            Cache info
	 */
	public void setCacheInfo(CacheInfo cacheInfo)
	{
		this.cacheInfo = cacheInfo;
		if (cacheInfo != null) {
			this.rootPathInfo = cacheInfo.getPathInfo(getGridRootPath());
		}
	}

	/**
	 * Sets the list of BizInfo objects that are registered to this grid.
	 * 
	 * @param bizSet
	 *            BizInfo object set
	 */
	public void setBizSet(Set<BizInfo> bizSet)
	{
		this.bizSet = bizSet;
	}

	/**
	 * Returns the set of BizInfo objects that are registered to this grid.
	 */
	public Set<BizInfo> getBizSet()
	{
		return bizSet;
	}

	/**
	 * Returns the list of ServerInfo objects for the specified grid path.
	 * 
	 * @param gridPath
	 *            Grid path
	 */
	public abstract List<ServerInfo> getServerInfoList(String gridPath);

	/**
	 * Sets the list of ServerInfo objects for the specified grid path.
	 * 
	 * @param gridPath
	 *            Grid path
	 * @param serverInfoList
	 *            List of ServerInfo objects
	 */
	public void putServerInfoList(String gridPath, List<ServerInfo> serverInfoList)
	{
		serverInfoMap.put(gridPath, serverInfoList);
	}

	/**
	 * Sets the list of CacheInfo objects representing all of this grid's
	 * servers.
	 * 
	 * @param cacheInfoList
	 *            CacheInfo list
	 */
	public void setCacheInfoList(List<CacheInfo> cacheInfoList)
	{
		this.cacheInfoList = cacheInfoList;
	}

	/**
	 * Returns the list of CacheInfo objects representing all of this grid's
	 * servers.
	 */
	public List<CacheInfo> getCacheInfoList()
	{
		return cacheInfoList;
	}

	/**
	 * Returns the root path info.
	 */
	public PathInfo getRootPathInfo()
	{
		return rootPathInfo;
	}
}
