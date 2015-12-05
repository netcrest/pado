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
package com.netcrest.pado.gemfire.info;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.netcrest.pado.gemfire.util.GemfireGridUtil;
import com.netcrest.pado.info.AppGridInfo;
import com.netcrest.pado.info.AppInfo;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.internal.impl.GridRoutingTable;
import com.netcrest.pado.internal.impl.GridRoutingTable.Grid;

public class GemfireAppInfo extends AppInfo implements DataSerializable
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * &lt;root-region-path, gridId&gt;
	 */
	private Map<String, String> gridIdMap = new HashMap(5);

	/**
	 * {@inheritDoc}
	 */
	public synchronized void removeAllowedGrid(String gridId)
	{
		GemfireAppGridInfo agi = (GemfireAppGridInfo)appGridInfoMap.remove(gridId);
		if (agi != null) {
			gridIdMap.remove(agi.getGridRootPath());
		}
		allowedGridMap.remove(gridId);
		disallowedGridIdSet.remove(gridId);
	}
	
	/**
	 * Updates this AppInfo object with the specified GridInfo only if the
	 * grid is in the allowed list. 
	 * @param gridInfo GridInfo to update
	 * @return Returns true if this object has been updated with the specified
	 *         GridInfo. It returns false if gridInfo is null, is not allowed, 
	 *         or contains the same info that this object already has.
	 */
	public synchronized boolean update(GridInfo gridInfo)
	{
		boolean updated = false;
		if (gridInfo != null && (isAllowedGrid(gridInfo.getGridId()) || getAppId().equals("sys"))) {
			AppGridInfo agi = getAppGridInfo(gridInfo.getGridId());
			if (agi == null) {
				agi = new GemfireAppGridInfo(gridInfo);
				updated = true;
			} else {
				updated = agi.update(gridInfo);
			}
			if (updated) {
				addAppGridInfo(agi);
				addBizSet(gridInfo.getBizSet());
				bizSetMap.put(gridInfo.getGridId(), new HashSet(gridInfo.getBizSet()));
			}
			
			// Update the routing table with the grid location. The routing 
			// table may not contain the grid location if it is dynamically
			// reconfigured.
			Grid grid = getAllowedGrid(gridInfo.getGridId());
			if (grid != null) {
				if (grid.getLocation() == null && gridInfo.getLocation() != null || 
						grid.getLocation() != null && grid.getLocation().equals(gridInfo.getLocation()) == false) {
					grid.setLocation(gridInfo.getLocation());
					updated = true;
				}
			}
		}
		
		return updated;
	}

	protected synchronized void addAppGridInfo(AppGridInfo agi)
	{
		if (agi != null) {
			GemfireAppGridInfo gagi = (GemfireAppGridInfo)agi;
			// Update the location for the grid in the allowed grid set
			// Location is required for GridRoutingTable to determine the
			// routing path
			GridRoutingTable.Grid grid = allowedGridMap.get(gagi.getGridId());
			if (grid != null) {
				grid.setLocation(gagi.getLocation());
			}
			appGridInfoMap.put(gagi.getGridId(),  gagi);
			gridIdMap.put(gagi.getGridRootPath(), gagi.getGridId());
		}
	}
	
	public synchronized AppGridInfo removeAppGridInfo(String gridId)
	{
		AppGridInfo appGridInfo = appGridInfoMap.remove(gridId);
		if (appGridInfo != null) {
			GemfireAppGridInfo gagi = (GemfireAppGridInfo)appGridInfo;
			for (String gridId2 : gridIdMap.values()) {
				if (gridId.equals(gridId2)) {
					gridIdMap.remove(getRootRegionPath(gagi.getGridRootPath()));
					break;
				}
			}
		}
		return appGridInfo;
	}
	
	/**
	 * Returns the default grid ID of the specified region path. The default
	 * grid ID determined by the root path of the region. Each grid has all
	 * of its application-level regions under a unique root region defined by
	 * Pado. Typically, the grid ID by default serves as the root region path.
	 * @param fullPath The absolute path beginning with "/". 
	 * @return The default grid ID of the specified region path. It returns null
	 *         if there are no matching grid ID for the specified full path. 
	 *         Note that each app spans across their own set of grids. This means
	 *         the specified region path may exist may not exist in all apps.
	 */
	public String getGridIdFromFullPath(String fullPath)
	{
		return gridIdMap.get(getRootRegionPath(fullPath));
	}
	
	/**
	 * Returns the root path of the specified region path.
	 * @param regionPath Absolute path of region. Must begin with "/".
	 * @return The root path begins with "/". It returns null if regionPath is
	 *         null or does not begin with "/".
	 */
	private String getRootRegionPath(String regionPath)
	{
		return GemfireGridUtil.getRootPath(regionPath);
	}

	/**
	 * Reads the state of this object from the given <code>DataInput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void fromData(DataInput input) throws IOException, ClassNotFoundException
	{
		appId = DataSerializer.readString(input);
		padoGridId = DataSerializer.readString(input);
		defaultGridId = DataSerializer.readString(input);
		allowedGridMap = DataSerializer.readHashMap(input);
		appGridInfoMap = DataSerializer.readObject(input);
		gridIdMap = DataSerializer.readObject(input);
		bizSet = DataSerializer.readHashSet(input);
	}

	/**
	 * Writes the state of this object to the given <code>DataOutput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void toData(DataOutput output) throws IOException
	{
		DataSerializer.writeString(appId, output);
		DataSerializer.writeString(padoGridId, output);
		DataSerializer.writeString(defaultGridId, output);
		DataSerializer.writeHashMap((HashMap)allowedGridMap, output);
		DataSerializer.writeObject(appGridInfoMap, output);
		DataSerializer.writeObject(gridIdMap, output);
		DataSerializer.writeHashSet(bizSet, output);
	}
}