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
package com.netcrest.pado.gemfire;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionService;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.internal.cache.GemFireCacheImpl;
import com.netcrest.pado.IGridRouter;
import com.netcrest.pado.annotation.RouterType;
import com.netcrest.pado.gemfire.info.GemfireAppGridInfo;
import com.netcrest.pado.gemfire.util.GemfireGridUtil;
import com.netcrest.pado.info.AppInfo;
import com.netcrest.pado.info.GridRouterInfo;
import com.netcrest.pado.info.PathInfo;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.impl.CostBasedGridRouter;
import com.netcrest.pado.internal.impl.GridService;
import com.netcrest.pado.internal.impl.LocationBasedGridRouter;
import com.netcrest.pado.internal.impl.PadoClientManager;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.security.RSACipher;

public class GemfireGridService extends GridService
{
	/**
	 * The number of ping calls to determine the average latency between this
	 * client and a given grid. The sampling algorithm drops the low and the
	 * high values and averages the remaining ones.
	 */
	private final static int PING_SAMPLE_SIZE = 5;

	private RegionService padoRegionService;
	private Pool padoPool;
	private Pool indexMatrixPool;
	private RegionService defaultRegionService;
	private Pool defaultPool;
	private Pool sharedPool;
	// <gridPath, IGridRouter>
	private Region<String, GridRouterInfo> routerRegion;

	// <gridId, RegionService> - pairMap contains all grids that are reachable.
	private HashMap<String, Pair> pairMap;

	/**
	 * This constructor is invoked by servers.
	 * 
	 * @param gridId
	 * @param appId
	 * @param credentials
	 * @param token
	 * @param routerRegion
	 * @param isGridParent
	 */
	public GemfireGridService(String gridId, String appId, Properties credentials, Object token, String username,
			Region routerRegion, boolean isGridParent)
	{
		this(appId, credentials, token, username, null, null, null, routerRegion);
		this.gridId = gridId;
		this.isGridParent = isGridParent;
	}

	/**
	 * This constructor is invoked by clients.
	 * 
	 * @param appId
	 * @param credentials
	 * @param token
	 * @param username
	 * @param padoRegionService
	 *            Pado region service. Pado is the grid from which appId and
	 *            token were obtained, i.e., login.
	 * @param padoPool
	 * @param routerRegion
	 */
	public GemfireGridService(String appId, Properties credentials, Object token, String username,
			RegionService padoRegionService, Pool padoPool, Pool indexMatrixPool, Region routerRegion)
	{
		super(appId);
		super.token = token;
		super.username = username;
		this.padoRegionService = padoRegionService;
		this.padoPool = padoPool;
		this.indexMatrixPool = indexMatrixPool;

		if (token != null && PadoUtil.isProperty(Constants.PROP_SECURITY_ENABLED)) {
			this.credentials = RSACipher.createCredentialProperties(token);
		} else {
			this.credentials = credentials;
		}
		this.routerRegion = routerRegion;

		refresh();
	}

	public synchronized void refresh()
	{
		HashMap<String, Pair> pm = new HashMap<String, Pair>(5);
		TreeSet<String> ags = new TreeSet<String>();

		// get AppInfo freshly from the server
		AppInfo appInfo = PadoClientManager.getPadoClientManager().getAppInfo(appId);
		if (appInfo == null) {
			pairMap = pm;
			allowedGridIdSet = ags;
			putPool(null, padoPool);
			putRegionService(null, padoRegionService);
			return;
		}

		// Get all live grids. Note that the grids that are not allowed cannot
		// be live.
		String gridIds[] = appInfo.getGridIds();
		for (String gridId : gridIds) {
			Pair pair = new Pair(gridId);
			pm.put(gridId, pair);
		}

		// Allowed grid Ids. This set contains all grid IDs for the app
		// including live and dead.
		ags.addAll(appInfo.getAllowedGridIdSet());

		pairMap = pm;
		allowedGridIdSet = ags;
		padoGridId = appInfo.getPadoId();
		defaultGridId = appInfo.getDefaultGridId();
		defaultRegionService = getRegionService(defaultGridId);
		defaultPool = getPool(defaultGridId);
		sharedPool = getSharedPool(defaultGridId);

		defaultGridRouter = costBasedGridRouter = new CostBasedGridRouter(getAllowedGridIdSet());
		locationBasedGridRouter = new LocationBasedGridRouter(getAllowedGridIdSet());

		putRegionService(appInfo.getPadoId(), padoRegionService);
	}

	public void setToken(Object token)
	{
		this.token = token;
	}

	public void putPool(String gridId, Pool pool)
	{
		Pair pair = pairMap.get(gridId);
		if (pair == null) {
			pair = new Pair(gridId, pool, null);
		} else {
			pair.pool = pool;
		}
		if (pool != null && pool.getMultiuserAuthentication()) {
			GemFireCacheImpl cache = (GemFireCacheImpl) CacheFactory.getAnyInstance();
			if (PadoUtil.isProperty(Constants.PROP_SECURITY_ENABLED) && credentials != null && credentials.size() > 0) {
				pair.regionService = cache.createAuthenticatedCacheView(pool, credentials);
			} else {
				pair.regionService = null;
			}
		} else {
			pair.regionService = null;
		}
		if (pair.pool == null && pair.regionService == null) {
			pairMap.remove(gridId);
		} else {
			pairMap.put(gridId, pair);
		}
	}

	public void putRegionService(String gridId, RegionService regionService)
	{
		Pair pair = pairMap.get(gridId);
		if (pair == null) {
			pair = new Pair(gridId, null, regionService);
		}
		pairMap.put(gridId, pair);
	}

	public RegionService getRegionService(String gridId)
	{
		Pair pair = pairMap.get(gridId);
		if (pair == null) {
			if (gridId == null) {
				return padoRegionService;
			} else {
				return null;
			}
		}
		return pair.regionService;
	}

	@Override
	public String getRootPath(String gridId)
	{
		Pair pair = pairMap.get(gridId);
		if (pair == null) {
			return null;
		}
		return pair.gridRootRegionPath;
	}

	@Override
	public String getFullPath(String gridId, String gridPath)
	{
		if (gridId == null || gridPath == null) {
			return null;
		}
		String rootPath = getRootPath(gridId);
		if (rootPath == null) {
			return null;
		}
		return rootPath + "/" + gridPath;
	}

	/**
	 * 
	 * @param fullPath
	 */
	public RegionService getRegionServiceFromRegionPath(String fullPath)
	{
		return getRegionService(getGridId(fullPath));
	}

	public Pool getPool(String gridId)
	{
		if (gridId == null) {
			return getPadoPool();
		}
		Pair pair = pairMap.get(gridId);
		if (pair == null) {
			return null;
		}
		return pair.pool;
	}

	public Pool getSharedPool(String gridId)
	{
		Pair pair = pairMap.get(gridId);
		if (pair == null) {
			return null;
		}
		return pair.sharedPool;
	}

	public List<Pool> getPoolList(String gridId)
	{
		Pair pair = pairMap.get(gridId);
		if (pair == null) {
			return null;
		} else {
			return pair.poolList;
		}
	}

	/**
	 * Returns a set of all live grid IDs. It also includes the parent (pado)
	 * ID. It always returns a non-null set.
	 */
	@Override
	public Set<String> getGridIdSet()
	{
		return pairMap.keySet();
	}

	/**
	 * Returns all live grid IDs. It also includes the parent (pado) IDs. It
	 * always returns a non-null array.
	 */
	@Override
	public String[] getGridIds()
	{
		return pairMap.keySet().toArray(new String[pairMap.keySet().size()]);
	}

	/**
	 * Returns a map of &lt;gridId, Region&gt; that contains all grids that have
	 * the specified grid path.
	 * 
	 * @param relativePath
	 *            relative to the root path
	 */
	public Map<String, Region> getGridRegionMap(String relativePath)
	{
		Collection<Pair> col = pairMap.values();
		Map<String, Region> regionMap = new HashMap(col.size() + 1);
		for (Pair pair : col) {
			Region region = pair.getRegion(relativePath);
			if (region != null) {
				regionMap.put(pair.gridId, region);
			}
		}
		return regionMap;
	}

	/**
	 * Returns IDs of live grids that contain the specified region path. It always
	 * returns non-null array.
	 * 
	 * @param gridPath
	 *            Grid path relative to the root path
	 */
	@Override
	public String[] getGridIds(String gridPath)
	{
		// Better
//		Collection<Pair> col = pairMap.values();
//		ArrayList<String> list = new ArrayList(col.size());
//		for (Pair pair : col) {
//			if (pair.hasGridPath(gridPath)) {
//				list.add(pair.gridId);
//			}
//		}
//		return list.toArray(new String[list.size()]); 
		
		// Expensive
		Collection<Pair> col = pairMap.values();
		ArrayList<String> list = new ArrayList(col.size());
		AppInfo appInfo = PadoClientManager.getPadoClientManager().getAppInfo(appId);
		for (Pair pair : col) {
			GemfireAppGridInfo appGridInfo = (GemfireAppGridInfo) appInfo.getAppGridInfo(pair.gridId);
			if (appGridInfo.getRootPathInfo().hasChildPath(gridPath)) {
				list.add(pair.gridId);
			}
		}
		return list.toArray(new String[list.size()]); 
		
		// Old
//		Collection<Pair> col = pairMap.values();
//		ArrayList<String> list = new ArrayList(col.size());
//		Map<String, Region> regionMap = new HashMap(col.size() + 1);
//		for (Pair pair : col) {
//			Region region = pair.getRegion(gridPath);
//			if (region != null) {
//				list.add(pair.gridId);
//			}
//		}
//		return list.toArray(new String[list.size()]);
	}

	/**
	 * Returns the specified gridId's region.
	 * 
	 * @param gridId
	 *            Grid ID
	 * @param gridPath
	 *            relative to the root path
	 * @return
	 */
	public Region getRegion(String gridId, String gridPath)
	{
		Pair pair = pairMap.get(gridId);
		if (pair == null) {
			if (isPureClient() == false && getGridId().equals(gridId)) {
				return CacheFactory.getAnyInstance().getRegion(
						GemfirePadoServerManager.getPadoServerManager().getRootRegion().getFullPath() + "/" + gridPath);
			}
			return null;
		}
		return pair.getRegion(gridPath);
	}

	@Override
	public void remove(String gridId)
	{
		pairMap.remove(gridId);
	}

	public RegionService getPadoRegionService()
	{
		return padoRegionService;
	}

	public void setPadoRegionService(RegionService padoRegionService)
	{
		this.padoRegionService = padoRegionService;
	}

	public void setPadoPool(Pool padoPool)
	{
		this.padoPool = padoPool;
	}

	public Pool getPadoPool()
	{
		return padoPool;
	}

	public void setIndexMatrixPool(Pool indexMatrixPool)
	{
		this.indexMatrixPool = indexMatrixPool;
	}

	public Pool getIndexMatrixPool()
	{
		return indexMatrixPool;
	}

	public RegionService getDefaultRegionService()
	{
		return defaultRegionService;
	}

	public Pool getDefaultPool()
	{
		return defaultPool;
	}

	public Pool getSharedPool()
	{
		return sharedPool;
	}

	/**
	 * Returns all grid relative paths in a sorted set.
	 */
	@Override
	public Set<String> getGridPathSet()
	{
		TreeSet<String> gridPathSet = new TreeSet();
		AppInfo appInfo = PadoClientManager.getPadoClientManager().getAppInfo(appId);
		for (String gridId : appInfo.getGridIdSet()) {
			GemfireAppGridInfo api = (GemfireAppGridInfo) appInfo.getAppGridInfo(gridId);
			PathInfo pathInfo = api.getRootPathInfo();
			gridPathSet.addAll(pathInfo.getChildGridPathSet(true));
		}
		return gridPathSet;
	}

	/**
	 * Returns the grid router that determines the target grids to forward IBiz
	 * calls.
	 * 
	 * @param gridPath
	 *            grid path to the root path
	 */
	public IGridRouter getGridRouter(String gridPath)
	{
		if (routerRegion == null) {
			return null;
		}
		GridRouterInfo routerInfo = routerRegion.get(gridPath);
		if (routerInfo == null) {
			return null;
		}
		IGridRouter router = routerInfo.getGridRouter();
		if (router == null) {
			router = defaultGridRouter;
		}
		return router;
	}

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

	public void setRouterRegion(Region routerRegion)
	{
		this.routerRegion = routerRegion;
	}

	/**
	 * Returns the grid router info that contains the target grid information.
	 * 
	 * @param relativePath
	 *            relative to the root path
	 */
	public GridRouterInfo getGridRouterInfo(String relativePath)
	{
		if (routerRegion == null) {
			return null;
		}
		return routerRegion.get(relativePath);
	}

	class Pair
	{
		String gridId;
		Pool pool;
		Pool sharedPool;
		RegionService regionService;
		String gridRootRegionPath;
		List<Pool> poolList = new ArrayList(2);
		Set<String> childPathSet;

		Pair(String gridId)
		{
			this.gridId = gridId;
			AppInfo appInfo = PadoClientManager.getPadoClientManager().getAppInfo(appId);
			GemfireAppGridInfo appGridInfo = (GemfireAppGridInfo) appInfo.getAppGridInfo(gridId);
			if (appGridInfo != null) {
				if (GemfirePadoServerManager.isServer()
						&& GemfirePadoServerManager.getPadoServerManager().getGridId().equals(gridId)) {
					// For server, use onMember. Pool not required.
				} else if (appGridInfo.getConnectionName() != null) {
					pool = GemfireGridUtil.getPool(appGridInfo.getClientConnectionName(), appGridInfo.getClientLocators(),
							appGridInfo.isClientConnectionMultiuserAuthenticationEnabled(),
							appGridInfo.isClientConnectionSingleHopEnabled(), true);
					if (pool != null && pool.getMultiuserAuthentication()) {
						GemFireCacheImpl cache = (GemFireCacheImpl) CacheFactory.getAnyInstance();
						regionService = cache.createAuthenticatedCacheView(pool, credentials);
					}
					if (appGridInfo.getClientSharedConnectionName() != null) {
						sharedPool = GemfireGridUtil.getPool(appGridInfo.getClientSharedConnectionName(),
								appGridInfo.getClientLocators(), false,
								appGridInfo.isClientConnectionSingleHopEnabled(), true);
					}
				}
				gridRootRegionPath = appGridInfo.getGridRootPath();
			}

			if (pool != null) {
				poolList.add(pool);
			}
			if (sharedPool != pool && sharedPool != null) {
				poolList.add(sharedPool);
			}
			postInit();
		}

		Pair(String gridId, Pool pool, RegionService regionService)
		{
			this.gridId = gridId;
			this.pool = pool;
			this.regionService = regionService;

			AppInfo appInfo = PadoClientManager.getPadoClientManager().getAppInfo(appId);
			if (appInfo != null) {
				GemfireAppGridInfo appGridInfo = (GemfireAppGridInfo) appInfo.getAppGridInfo(gridId);
				if (appGridInfo != null) {
					gridRootRegionPath = appGridInfo.getGridRootPath();
				}
			}
			postInit();
		}
		
		private void postInit()
		{
			AppInfo appInfo = PadoClientManager.getPadoClientManager().getAppInfo(appId);
			if (appInfo != null) {
				GemfireAppGridInfo appGridInfo = (GemfireAppGridInfo) appInfo.getAppGridInfo(gridId);
				if (appGridInfo != null) {
					childPathSet = appGridInfo.getRootPathInfo().getChildGridPathSet(true);
				}
			}
		}
		
		boolean hasGridPath(String gridPath)
		{
			return childPathSet != null && childPathSet.contains(gridPath);
		}

		Region getRegion(String childPath)
		{
			if (childPath == null) {
				return null;
			}
			Region region;
			if (regionService != null) {
				try {
					region = regionService.getRegion(gridRootRegionPath + "/" + childPath);
				} catch (Exception ex) {
					region = CacheFactory.getAnyInstance().getRegion(gridRootRegionPath + "/" + childPath);
				}
			} else {
				region = CacheFactory.getAnyInstance().getRegion(gridRootRegionPath + "/" + childPath);
			}
			return region;

			// if (regionService == null || (gridId != null &&
			// gridId.equals(GridService.this.gridId))) {
			// return CacheFactory.getAnyInstance().getRegion(gridRootRegionPath
			// + "/" + regionPath);
			// } else {
			// return regionService.getRegion(gridRootRegionPath + "/" +
			// regionPath);
			// }
		}
	}
}
