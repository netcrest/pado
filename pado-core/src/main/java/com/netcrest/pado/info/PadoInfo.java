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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.util.GridUtil;

/**
 * PadoInfo provides Pado grid information.
 * 
 * @author dpark
 *
 */
public abstract class PadoInfo {
	/**
	 * Grid ID of this Pado grid.
	 */
	protected String gridId;

	/**
	 * GridInfo object representing this Pado grid.
	 */
	protected GridInfo gridInfo;

	/**
	 * Array of parent GridInfo objects.
	 */
	protected GridInfo[] parentGridInfos;

	/**
	 * Array of child GridInfo objects.
	 */
	protected GridInfo[] childGridInfos;

	/**
	 * Map of &lt;gridID, GridPathInfo&gt; pairs.
	 */
	protected Map<String, GridPathInfo> gridPathInfoMap;
	protected transient GridInfo[] gridInfos;
	protected transient List<PathInfo> pathInfoList;
	protected transient List<VirtualPathInfo> vpPathInfoList;
	protected transient List<PathInfo> temporalPathInfoList;
	protected transient List<String> temporalFullPathList;

	/**
	 * Constructs an empty PadoInfo object.
	 */
	public PadoInfo() {
	}

	/**
	 * Constructs a fully populated PadoInfo object if it is invoked by a fully
	 * initialized server.
	 * 
	 * @param appId
	 *            App ID
	 */
	public PadoInfo(String appId) {
		PadoServerManager sm = PadoServerManager.getPadoServerManager();
		if (sm == null) {
			return;
		}
		gridId = sm.getGridId();
		gridInfo = sm.createGridInfo();
		parentGridInfos = sm.getParentGridInfos();
		childGridInfos = sm.getChildGridInfos(appId);
		gridPathInfoMap = sm.getGridPathInfoMap();
	}

	/**
	 * Returns the grid ID.
	 */
	public String getGridId() {
		return gridId;
	}

	/**
	 * Returns the GridInfo object.
	 */
	public GridInfo getGridInfo() {
		return gridInfo;
	}

	/**
	 * Returns GridInfo objects of parent grids.
	 */
	public GridInfo[] getParentGridInfos() {
		return parentGridInfos;
	}

	/**
	 * Returns GridInfo objects of child grids.
	 */
	public GridInfo[] getChildGridInfos() {
		return childGridInfos;
	}

	/**
	 * Returns all parent and child GridInfo objects.
	 */
	public GridInfo[] getAllGridInfos() {
		ArrayList<GridInfo> list = new ArrayList();
		if (gridInfos == null) {
			if (gridInfo != null) {
				list.add(gridInfo);
			}
			if (childGridInfos != null) {
				for (GridInfo gridInfo : childGridInfos) {
					list.add(gridInfo);
				}
			}
			if (parentGridInfos != null) {
				for (GridInfo gridInfo : parentGridInfos) {
					list.add(gridInfo);
				}
			}
			Collections.sort(list);
			gridInfos = list.toArray(new GridInfo[list.size()]);
		}
		return gridInfos;
	}

	/**
	 * Returns the GridInfo object that matches the specified grid ID.
	 * 
	 * @param gridId
	 *            Grid ID
	 * @return null if not found
	 */
	public GridInfo getGridInfo(String gridId) {
		if (gridInfo != null) {
			if (gridInfo.getGridId().equals(gridId)) {
				return gridInfo;
			}
		}
		if (childGridInfos != null) {
			for (GridInfo gridInfo : childGridInfos) {
				if (gridInfo.getGridId().equals(gridId)) {
					return gridInfo;
				}
			}
		}
		if (parentGridInfos != null) {
			for (GridInfo gridInfo : parentGridInfos) {
				if (gridInfo.getGridId().equals(gridId)) {
					return gridInfo;
				}
			}
		}

		return null;
	}

	/**
	 * Returns all physical PathInfo objects including parent and child grids.
	 */
	public synchronized List<PathInfo> getPathInfoList() {
		if (pathInfoList == null) {
			pathInfoList = new ArrayList<PathInfo>();
			if (gridInfo != null) {
				pathInfoList.addAll(gridInfo.getCacheInfo().getAllPathInfoList());
			}
			if (parentGridInfos != null) {
				for (GridInfo gridInfo : parentGridInfos) {
					pathInfoList.addAll(gridInfo.getCacheInfo().getAllPathInfoList());
				}
			}
			if (childGridInfos != null) {
				for (GridInfo gridInfo : childGridInfos) {
					pathInfoList.addAll(gridInfo.getCacheInfo().getAllPathInfoList());
				}
			}
//			Collections.sort(pathInfoList);
		}
		return pathInfoList;
	}
	
	/**
	 * Returns all virtual PathInfo objects including parent and child grids.
	 */
	public synchronized List<VirtualPathInfo> getVitualPathInfoList() {
		if (vpPathInfoList == null) {
			vpPathInfoList = new ArrayList<VirtualPathInfo>();
			if (gridInfo != null) {
				vpPathInfoList.addAll(gridInfo.getCacheInfo().getAllVirtualPathInfoList());
			}
			if (parentGridInfos != null) {
				for (GridInfo gridInfo : parentGridInfos) {
					vpPathInfoList.addAll(gridInfo.getCacheInfo().getAllVirtualPathInfoList());
				}
			}
			if (childGridInfos != null) {
				for (GridInfo gridInfo : childGridInfos) {
					vpPathInfoList.addAll(gridInfo.getCacheInfo().getAllVirtualPathInfoList());
				}
			}
//			Collections.sort(vpPathInfoList);
		}
		return vpPathInfoList;
	}

	/**
	 * Returns all temporal PathInfo objects including parent and child grids.
	 */
	public synchronized List<PathInfo> getTemporalPathInfoList() {
		if (temporalPathInfoList == null) {
			temporalPathInfoList = new ArrayList<PathInfo>();
			if (gridInfo != null) {
				temporalPathInfoList.addAll(gridInfo.getCacheInfo().getAllTemporalPathInfoList());
			}
			if (parentGridInfos != null) {
				for (GridInfo gridInfo : parentGridInfos) {
					temporalPathInfoList.addAll(gridInfo.getCacheInfo().getAllTemporalPathInfoList());
				}
			}
			if (childGridInfos != null) {
				for (GridInfo gridInfo : childGridInfos) {
					temporalPathInfoList.addAll(gridInfo.getCacheInfo().getAllTemporalPathInfoList());
				}
			}
		}
		return temporalPathInfoList;
	}

	/**
	 * Returns all temporal full paths including parent and child grids.
	 */
	public synchronized List<String> getTemporalFullPaths() {
		if (temporalFullPathList == null) {
			temporalFullPathList = new ArrayList<String>();
			if (gridInfo != null) {
				temporalFullPathList.addAll(gridInfo.getCacheInfo().getAllTemporalFullPaths());
			}
			if (parentGridInfos != null) {
				for (GridInfo gridInfo : parentGridInfos) {
					temporalFullPathList.addAll(gridInfo.getCacheInfo().getAllTemporalFullPaths());
				}
			}
			if (childGridInfos != null) {
				for (GridInfo gridInfo : childGridInfos) {
					temporalFullPathList.addAll(gridInfo.getCacheInfo().getAllTemporalFullPaths());
				}
			}
		}
		return temporalFullPathList;
	}

	/**
	 * Returns the set of all temporal grid paths defined in this Pado grid.
	 */
	public synchronized Set<String> getTemporalGridFullPaths() {
		List<String> fullPathList = getTemporalFullPaths();
		TreeSet treeSet = new TreeSet();
		for (String fullPath : fullPathList) {
			String gridPath = GridUtil.getChildPath(fullPath);
			treeSet.add(gridPath);
		}
		return treeSet;
	}

	/**
	 * Returns the modifiable map of &lt;gridId, GridPathInfo&gt; pairs.
	 */
	public Map<String, GridPathInfo> getGridPathInfoMap() {
		return gridPathInfoMap;
	}

	/**
	 * Returns the list of full paths defined in this Pado grid.
	 */
	public synchronized List<String> getFullPaths() {
		List<String> list = new ArrayList();
		if (gridInfo != null) {
			list.addAll(gridInfo.getCacheInfo().getAllAppFullPaths(gridInfo.getRootPathInfo().getFullPath()));
		}
		if (parentGridInfos != null) {
			for (GridInfo gridInfo : parentGridInfos) {
				list.addAll(gridInfo.getCacheInfo().getAllAppFullPaths(gridInfo.getRootPathInfo().getFullPath()));
			}
		}
		if (childGridInfos != null) {
			for (GridInfo gridInfo : childGridInfos) {
				list.addAll(gridInfo.getCacheInfo().getAllAppFullPaths(gridInfo.getRootPathInfo().getFullPath()));
			}
		}
		return list;
	}

	/**
	 * Returns the set of all grid paths defined in this Pado grid.
	 */
	public synchronized Set<String> getGridPaths() {
		List<String> fullPathList = getFullPaths();
		TreeSet treeSet = new TreeSet();
		for (String fullPath : fullPathList) {
			String gridPath = GridUtil.getChildPath(fullPath);
			treeSet.add(gridPath);
		}
		return treeSet;
	}

	public PathInfo getPathInfo(String fullPath) {
		List<PathInfo> pathInfoList = getPathInfoList();
		for (PathInfo pathInfo : pathInfoList) {
			if (pathInfo.getFullPath().equals(fullPath)) {
				return pathInfo;
			}
		}
		return null;
	}
	
	public VirtualPathInfo getVirtualPathInfo(String fullPath) {
		List<VirtualPathInfo> vpList = getVitualPathInfoList();
		for (VirtualPathInfo pathInfo : vpList) {
			if (pathInfo.getFullPath().equals(fullPath)) {
				return pathInfo;
			}
		}
		return null;
	}

	public PathInfo getTemporalPathInfo(String fullPath) {
		List<PathInfo> pathInfoList = getTemporalPathInfoList();
		for (PathInfo pathInfo : pathInfoList) {
			if (pathInfo.getFullPath().equals(fullPath)) {
				return pathInfo;
			}
		}
		return null;
	}

	/**
	 * Returns the name of the temporal identity class assigned to the specified
	 * grid path. It returns null if not the class name is not assigned or the
	 * grid path is not temporal-enabled.
	 * 
	 * @param gridPath
	 *            Grid path
	 */
	public String getTemporalIdentityClassName(String targetPath) {
		List<PathInfo> pathInfoList = getTemporalPathInfoList();
		String identityClassName = null;
		for (PathInfo pathInfo : pathInfoList) {
			if (pathInfo.getGridRelativePath().equals(targetPath)) {
				identityClassName = pathInfo.getTemporalType().getIdentityKeyClassName();
				if (identityClassName == null && gridPathInfoMap != null) {
					GridPathInfo gpi = gridPathInfoMap.get(targetPath);
					if (gpi != null) {
						identityClassName = gpi.getKeyClassName();
					}
				}
				break;
			}
		}
		return identityClassName;
	}

	/**
	 * Returns the name of the temporal data class assigned to the specified
	 * grid path. It returns null if not the class name is not assigned or the
	 * grid path is not temporal-enabled.
	 * 
	 * @param gridPath
	 *            Grid path
	 */
	public String getTemporalDataClassName(String targetPath) {
		List<PathInfo> pathInfoList = getTemporalPathInfoList();
		String dataClassName = null;
		for (PathInfo pathInfo : pathInfoList) {
			if (pathInfo.getGridRelativePath().equals(targetPath)) {
				dataClassName = pathInfo.getTemporalType().getDataClassName();
				if ((dataClassName != null && dataClassName.equals(JsonLite.class.getName()))
						|| dataClassName == null && gridPathInfoMap != null) {
					// Get the KeyType class name
					GridPathInfo gpi = gridPathInfoMap.get(targetPath);
					if (gpi != null) {
						// KeyType
						dataClassName = gpi.getDataClassName();
					}
				}
				break;
			}
		}
		return dataClassName;
	}
}