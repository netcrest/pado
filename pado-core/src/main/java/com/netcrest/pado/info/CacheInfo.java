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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CacheInfo contains cache information of a single server. A cache instance
 * represents a process (or VM) that has one or more grid paths defined.
 * 
 * @author dpark
 * 
 */
public abstract class CacheInfo extends CacheHeaderInfo
{
	/**
	 * List of CacheServerInfo objects. A single cache instance can have
	 * multiple cache servers.
	 */
	protected List<CacheServerInfo> cacheServerInfoList = new ArrayList<CacheServerInfo>(3);

	/**
	 * List of GatewayHubInfo objects.
	 */
	protected List<GatewayHubInfo> gatewayHubInfoList = new ArrayList<GatewayHubInfo>(3);

	protected List<PathInfo> pathInfoList = new ArrayList<PathInfo>(10);

	/**
	 * Constructs a CacheInfo object.
	 */
	public CacheInfo()
	{
	}

	/**
	 * Constructs a CacheInfo object describing a cache instance in the
	 * specified grid.
	 * 
	 * @param gridId
	 *            Grid ID
	 */
	public CacheInfo(String gridId)
	{
		super(gridId);
	}

	/**
	 * Returns the list of CacheServerInfo objects which provide transport-level
	 * server information.
	 */
	public List<CacheServerInfo> getCacheServerInfoList()
	{
		return cacheServerInfoList;
	}

	/**
	 * Returns the list of GatewayHubInfo objects. Gateways represent bridges
	 * between replicated grids.
	 */
	public List<GatewayHubInfo> getGatewayHubInfoList()
	{
		return gatewayHubInfoList;
	}

	/**
	 * Returns the PathInfo object that matches the specified full path.
	 * 
	 * @param fullPath
	 *            The full path.
	 * @return Returns null if not found
	 */
	public PathInfo getPathInfo(String fullPath)
	{
		if (fullPath == null) {
			return null;
		}
		PathInfo foundInfo = null;
		for (PathInfo pathInfo : pathInfoList) {
			if (pathInfo.getFullPath().equals(fullPath)) {
				foundInfo = pathInfo;
				break;
			}
			foundInfo = getChildPathInfo(pathInfo, fullPath);
			if (foundInfo != null) {
				break;
			}
		}
		return foundInfo;
	}

	private PathInfo getChildPathInfo(PathInfo pathInfo, String fullPath)
	{
		PathInfo foundInfo = null;
		List<PathInfo> childList = pathInfo.getChildList();
		for (PathInfo childInfo : childList) {
			if (childInfo.getFullPath().equals(fullPath)) {
				foundInfo = childInfo;
				break;
			}
			foundInfo = getChildPathInfo(childInfo, fullPath);
			if (foundInfo != null) {
				break;
			}
		}
		return foundInfo;
	}
	
	/**
	 * Returns the top-level PathInfo objects.
	 */
	public List<PathInfo> getPathInfoList()
	{
		return pathInfoList;
	}

	/**
	 * Returns all PathInfo objects including children in a flat sorted list
	 */
	public List<PathInfo> getAllPathInfoList()
	{
		List<PathInfo> list = new ArrayList<PathInfo>(pathInfoList.size() + 10);
		for (PathInfo pathInfo : pathInfoList) {
			list.add(pathInfo);
			getChildPathInfo(pathInfo, list);
		}
		Collections.sort(list);
		return list;
	}

	protected void getChildPathInfo(PathInfo pathInfo, List<PathInfo> list)
	{
		List<PathInfo> childList = pathInfo.getChildList();
		for (PathInfo childInfo : childList) {
			list.add(childInfo);
			getChildPathInfo(childInfo, list);
		}
	}

	/**
	 * Returns all full paths in a flat list. The returned list includes
	 * all full paths including hidden, local, non-grid paths, etc. It
	 * always returns a non-null list.
	 */
	public List<String> getAllFullPaths()
	{
		List<PathInfo> pathInfoList = getAllPathInfoList();
		List<String> list = new ArrayList<String>(pathInfoList.size() + 1);
		for (PathInfo pathInfo : pathInfoList) {
			list.add(pathInfo.getFullPath());
		}
		Collections.sort(list);
		return list;
	}
	
	/**
	 * Returns all PathInfo objects that are visible from applications
	 * in a flat list. A PathInfo is app-visible if non-hidden, non-local,
	 * and has a grid root path. It always returns a non-empty list.
	 * @param rootPath Root path
	 */
	public List<PathInfo> getAllAppPathInfoList(String rootPath)
	{
		List<PathInfo> list = new ArrayList<PathInfo>(20);
		for (PathInfo pathInfo : pathInfoList) {
			if (pathInfo.getFullPath().equals(rootPath)) {
				getAppChildPathInfo(pathInfo, list);
				break;
			}
		}
		Collections.sort(list);
		return list;
	}
	
	/**
	 * Returns all full paths that are visible from applications
	 * in a flat list. A full path is app-visible if non-hidden, non-local,
	 * and has a grid root path. It always returns a non-empty list.
	 * @param rootPath Root path
	 */
	public List<String> getAllAppFullPaths(String rootPath)
	{
		List<PathInfo> pathInfoList = getAllAppPathInfoList(rootPath);
		List<String> list = new ArrayList<String>(pathInfoList.size() + 1);
		for (PathInfo pathInfo : pathInfoList) {
			list.add(pathInfo.getFullPath());
		}
		Collections.sort(list);
		return list;
	}

	public List<PathInfo> getAllTemporalPathInfoList()
	{
		List<PathInfo> list = new ArrayList(pathInfoList.size() + 10);
		for (PathInfo pathInfo : pathInfoList) {
			if (pathInfo.isTemporal(false)) {
				list.add(pathInfo);
			}
			getChildTemporalPathInfo(pathInfo, list);
		}
		Collections.sort(list);
		return list;
	}

	private void getChildTemporalPathInfo(PathInfo pathInfo, List<PathInfo> list)
	{
		List<PathInfo> childList = pathInfo.getChildList();
		for (PathInfo childInfo : childList) {
			if (childInfo.isTemporal(false)) {
				list.add(childInfo);
			}
			getChildTemporalPathInfo(childInfo, list);
		}
	}

	public List<String> getAllTemporalFullPaths()
	{
		List<PathInfo> pathInfoList = getAllTemporalPathInfoList();
		List<String> list = new ArrayList<String>(pathInfoList.size() + 1);
		for (PathInfo pathInfo : pathInfoList) {
			list.add(pathInfo.getFullPath());
		}
		Collections.sort(list);
		return list;
	}

	public boolean isPathExist(String gridPath)
	{
		if (gridPath == null) {
			return false;
		}
		// for (CacheServerInfo cacheServerInfo : cacheServerInfoList) {
		// if (cacheServerInfo.)
		// }
		return true;
	}
	
	// ----------------------------------------------------------------
	//            Abstract methods
	// ----------------------------------------------------------------
	protected abstract void getAppChildPathInfo(PathInfo pathInfo, List<PathInfo> list);
}
