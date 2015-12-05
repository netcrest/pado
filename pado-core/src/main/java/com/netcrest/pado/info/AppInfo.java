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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.netcrest.pado.internal.impl.GridRoutingTable;
import com.netcrest.pado.internal.impl.GridRoutingTable.Grid;

/**
 * AppInfo contains application specific information. An application is
 * identified by a unique app ID managed by Pado.
 * 
 * @author dpark
 * 
 */
public abstract class AppInfo
{
	/**
	 * App ID.
	 */
	protected String appId;

	/**
	 * The ID of the pado that provided this AppInfo object.
	 */
	protected String padoGridId;

	/**
	 * Default grid ID;
	 */
	protected String defaultGridId;

	/**
	 * Allowed grids. &lt;gridId, Grid&gt; Some of the grids may not be
	 * available during initialization and updates. This set is used to
	 * determine whether to permit the grids to be part of the app as their grid
	 * information become available. Each grid object contains routing
	 * information needed for creating the grid routing table.
	 */
	protected Map<String, Grid> allowedGridMap = new HashMap<String, Grid>(5);

	/**
	 * Contains disallowed Grid IDs. Disallowed grids may still run and active
	 * but are not allowed by this app to access. If one of the disallowed grid
	 * IDs is the default grid ID then a new one is randomly assigned. It is
	 * however reinstated when it is taken off the disallowed list.
	 */
	protected Set<String> disallowedGridIdSet = new HashSet<String>(2);

	/**
	 * appGridInfoMap contains all grids including the pado that the app spans
	 * across. Note that a pado is always part of an app.
	 * <p>
	 * &lt;gridId, AppGridInfo&gt;
	 */
	protected Map<String, AppGridInfo> appGridInfoMap = new HashMap(5);

	/**
	 * Contains BizInfo of all AppGridInfo objects kept in appGridInfoMap. As
	 * such, bizSet must be cleaned up properly based on individual AppGridInfo
	 * in appGridInfoMap.
	 */
	protected HashSet<BizInfo> bizSet = new HashSet(20);

	/**
	 * Map of BizInfo of all grids. &lt;gridId, Set&lt;BizInfo&gt;&gt;
	 */
	protected transient HashMap<String, Set<BizInfo>> bizSetMap = new HashMap(10);

	/**
	 * Returns the app ID.
	 */
	public String getAppId()
	{
		return appId;
	}

	/**
	 * Sets the app ID.
	 * 
	 * @param appId
	 *            App ID
	 */
	public void setAppId(String appId)
	{
		this.appId = appId;
	}

	/**
	 * Returns the ID of the pado that authenticated login and provided this
	 * object. The returned grid ID can be part of {@link #getGridIds()}. In
	 * that case, the pado also participates in IBiz grid operations for this
	 * application.
	 */
	public String getPadoId()
	{
		return padoGridId;
	}

	/**
	 * Sets the Pado grid ID.
	 * 
	 * @param padoGridId
	 *            Pado grid ID.
	 */
	public void setPadoId(String padoGridId)
	{
		this.padoGridId = padoGridId;
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

	/**
	 * Sets the default grid ID.
	 * 
	 * @param defaultGridId
	 *            Default grid ID
	 */
	public void setDefaultGridId(String defaultGridId)
	{
		this.defaultGridId = defaultGridId;
	}

	/**
	 * Returns a set of allowed grid IDs.
	 */
	public Set<String> getAllowedGridIdSet()
	{
		return allowedGridMap.keySet();
	}

	/**
	 * Returns a set of allowed grids.
	 */
	public Set<GridRoutingTable.Grid> getAllowedGridSet()
	{
		return Collections.unmodifiableSet(new HashSet(allowedGridMap.values()));
	}

	/**
	 * Returns a set of disallowed grids.
	 */
	public Set<String> getDisallowedGridIdSet()
	{
		return Collections.unmodifiableSet(disallowedGridIdSet);
	}

	/**
	 * Adds the specified grid as an allowed grid only if the grid is not in the
	 * disallowed list.
	 * 
	 * @param grid
	 *            Grid to allow in this app
	 */
	public void addAllowedGrid(Grid grid)
	{
		if (grid == null || grid.getGridId() == null) {
			return;
		}
		// return if the grid is disallowed
		if (isDisallowedGrid(grid.getGridId())) {
			return;
		}

		allowedGridMap.put(grid.getGridId(), grid);
	}

	/**
	 * Removes the specified grid ID from the disallowed list. This call
	 * effectively allows the specified grid ID from enlisting to the
	 * allowed list.
	 * 
	 * @param gridId
	 *            Grid ID
	 * @return true if the specified grid ID was in the disallowed list
	 */
	public boolean removeDisallowedGrid(String gridId)
	{
		return disallowedGridIdSet.remove(gridId);
	}

	/**
	 * Returns the allowed grid that matches the specified grid ID.
	 * 
	 * @param gridId
	 *            Grid ID
	 */
	public GridRoutingTable.Grid getAllowedGrid(String gridId)
	{
		return allowedGridMap.get(gridId);
	}

	/**
	 * Returns true if the specified grid ID belongs to this app.
	 * 
	 * @param gridId
	 *            Grid ID to check if it is allowed in this app
	 */
	public boolean isAllowedGrid(String gridId)
	{
		return allowedGridMap.containsKey(gridId);
	}

	/**
	 * Adds the specified grid ID as a disallowed grid ID. Note that disallowed
	 * grid is not permanently removed from the grid. As soon as it is enabled,
	 * it becomes available in the allowed list.
	 * 
	 * @param gridId
	 *            Grid ID to disallow in this app
	 */
	public void addDisallowedGrid(String gridId)
	{
		Grid grid = allowedGridMap.remove(gridId);
		if (grid != null) {
			disallowedGridIdSet.add(gridId);
		}
	}
	
	/**
	 * Returns true if the specified ID is disallowed or invalid.
	 * @param gridId Grid ID
	 */
	public boolean isDisallowedGrid(String gridId)
	{
		return disallowedGridIdSet.contains(gridId);
	}
	
	/**
	 * Returns true if the specified grid ID is valid. A valid grid is one
	 * that is either allowed or disallowed. An allowed grid is one that is
	 * permitted to operate within Pado at the time of inquiry. A disallowed
	 * grid is one that is permitted to operate but has been disabled at
	 * the time of inquiry. 
	 * @param gridId Grid ID
	 */
	public boolean isValidGrid(String gridId)
	{
		return allowedGridMap.containsKey(gridId) || disallowedGridIdSet.contains(gridId);
	}

	/**
	 * Removes metadata relevant to the specified GridInfo.
	 * 
	 * @param gridInfo
	 * @return Returns true if its state changed due to removal. Note that false
	 *         doesn't necessary mean removal is not performed. It returns true
	 *         only if this object's state has changed and it must be notified
	 *         to other components accordingly. For example, this object must
	 *         explicitly be put in the "app" region.
	 */
	public synchronized boolean remove(GridInfo gridInfo)
	{
		boolean removed = false;
		if (gridInfo == null) {
			return removed;
		}
		if (isAllowedGrid(gridInfo.getGridId())) {
			AppGridInfo agi = removeAppGridInfo(gridInfo.getGridId());
			removed = removeBizSet(gridInfo.getGridId());
			if (agi != null) {
				removed = true;
			}
		}
		return removed;
	}

	/**
	 * Returns the set of BizInfo objects containing the allowed IBiz class
	 * information required in building the catalog for this app.
	 */
	public Set<BizInfo> getBizSet()
	{
		return bizSet;
	}

	/**
	 * Adds all of the BizInfo objects found in the specified biz set.
	 * 
	 * @param bizSet
	 *            BizInfo object set
	 */
	public void addBizSet(Set<BizInfo> bizSet)
	{
		this.bizSet.addAll(bizSet);
	}

	/**
	 * Removes all of BizInfo objects that are allowed in the specified grid ID.
	 * 
	 * @param gridId
	 *            Grid ID
	 */
	private boolean removeBizSet(String gridId)
	{
		boolean removed = false;

		// Remove the BizInfo set of this grid
		Set<BizInfo> removedBizSet = bizSetMap.remove(gridId);

		// Remove all BizInfos from removedBizSet that exist in the
		// remaining sets. The final removedBizSet contains BizInfos
		// that can be safely removed from this.bizSet which is
		// a consolidated set of all BizInfos.
		if (removedBizSet != null) {
			for (Set<BizInfo> bizSet : bizSetMap.values()) {
				for (BizInfo bizInfo : bizSet) {
					Iterator<BizInfo> iterator = removedBizSet.iterator();
					while (iterator.hasNext()) {
						BizInfo removedBizInfo = (BizInfo) iterator.next();
						if (bizInfo.equals(removedBizInfo)) {
							iterator.remove();
							break;
						}
					}
				}
			}
			// Remove BizInfos that are no longer visible to apps.
			removed = bizSet.removeAll(removedBizSet);
		}
		return removed;
	}

	/**
	 * Returns the AppGridInfo object that has the specified grid ID.
	 * 
	 * @param gridId
	 *            Grid ID
	 * @return Returns null if the AppGridInfo object is not found.
	 */
	public AppGridInfo getAppGridInfo(String gridId)
	{
		return appGridInfoMap.get(gridId);
	}

	/**
	 * Returns the set of IDs of "live" grids including the parents that are
	 * currently running.
	 */
	public Set<String> getGridIdSet()
	{
		return appGridInfoMap.keySet();
	}

	/**
	 * Returns the array of IDs of "live" grids including the parents that are
	 * currently running.
	 */
	public String[] getGridIds()
	{
		return appGridInfoMap.keySet().toArray(new String[appGridInfoMap.size()]);
	}
	
	// -----------------------------------------------------------------------
	// Abstract methods
	// -----------------------------------------------------------------------
	
	/**
	 * Removes the specified grid ID from the app. The specified grid is no
	 * longer part of this app after this call.
	 * 
	 * @param gridId
	 *            Grid ID to remove from the app
	 */
	public abstract void removeAllowedGrid(String gridId);
	
	/**
	 * Updates this AppInfo object with the specified GridInfo only if the grid
	 * is in the allowed list.
	 * 
	 * @param gridInfo
	 *            GridInfo to update
	 * @return Returns true if this object has been updated with the specified
	 *         GridInfo. It returns false if gridInfo is null, is not allowed,
	 *         or contains the same info that this object already has.
	 */
	public abstract boolean update(GridInfo gridInfo);
	
	/**
	 * Adds the specified AppGridInfo object.
	 * 
	 * @param agi
	 *            AppGridInfo object
	 */
	protected abstract void addAppGridInfo(AppGridInfo agi);
	
	/**
	 * Removes and returns the AppGridInfo object that has the specified grid
	 * ID.
	 * 
	 * @param gridId
	 *            Grid ID.
	 * @return Returns null if the AppGridInfo object is not found.
	 */
	public abstract AppGridInfo removeAppGridInfo(String gridId);

	/**
	 * Returns the default grid ID of the specified region path. The default
	 * grid ID determined by the root path of the region. Each grid has all of
	 * its application-level regions under a unique root region defined by Pado.
	 * Typically, the grid ID by default serves as the root region path.
	 * 
	 * @param fullPath
	 *            The absolute path beginning with "/".
	 * @return The default grid ID of the specified region path. It returns null
	 *         if there are no matching grid ID for the specified full path.
	 *         Note that each app spans across their own set of grids. This
	 *         means the specified region path may exist may not exist in all
	 *         apps.
	 */
	public abstract String getGridIdFromFullPath(String fullPath);
}
