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
package com.netcrest.pado.biz.mon.impl.gemfire;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Resource;

import com.gemstone.gemfire.admin.RegionNotFoundException;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.CacheTransactionManager;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.distributed.PoolCancelledException;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.BizType;
import com.netcrest.pado.biz.mon.DeploymentFailedException;
import com.netcrest.pado.biz.mon.ISysBiz;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.gemfire.info.GemfireCacheInfo;
import com.netcrest.pado.gemfire.info.GemfireKeyTypeInfo;
import com.netcrest.pado.info.BizInfo;
import com.netcrest.pado.info.CacheInfo;
import com.netcrest.pado.info.CacheServerInfo;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.GridPathInfo;
import com.netcrest.pado.info.KeyTypeInfo;
import com.netcrest.pado.info.PadoInfo;
import com.netcrest.pado.info.ServerInfo;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.factory.InfoFactory;
import com.netcrest.pado.internal.impl.GridRoutingTable;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.temporal.TemporalData;
import com.netcrest.pado.util.GridUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SysBizImpl implements ISysBiz
{
	@Resource
	private IBizContextServer bizContext;

	/**
	 * Returns a sorted map of all key types in each region. This method call
	 * can be expensive as it searches all regions by iterating all entries. The
	 * returned results contain (full region path, Set &lt;KeyTypeInfo&gt;).
	 */
	private Map<String, Set<KeyTypeInfo>> getAllRegionKeyTypes()
	{
		Map<String, Set<KeyTypeInfo>> map = new TreeMap();
		Cache cache = CacheFactory.getAnyInstance();
		if (cache == null) {
			return map;
		}
		Set<Region<?, ?>> regionSet = cache.rootRegions();
		for (Region<?, ?> region : regionSet) {
			if (region.isDestroyed()) {
				continue;
			}
			map = getKeyTypes(region, map);
		}
		return map;
	}

	private Map<String, Set<KeyTypeInfo>> getKeyTypes(Region region, Map<String, Set<KeyTypeInfo>> map)
	{
		if (region == null || region.isDestroyed()) {
			return map;
		}
		Region localRegion;
		if (region instanceof PartitionedRegion) {
			localRegion = PartitionRegionHelper.getLocalPrimaryData(region);
		} else {
			localRegion = region;
		}

		Set<Region.Entry<?, ?>> entrySet = localRegion.entrySet(false);
		KeyMap keyMap = null;
		TreeSet<KeyTypeInfo> keyTypeInfoSet = new TreeSet();
		HashMap<String, KeyTypeInfo> nameMap = new HashMap();
		for (Region.Entry<?, ?> entry : entrySet) {
			Object obj = entry.getValue();
			keyMap = null;
			if (obj instanceof KeyMap) {
				keyMap = (KeyMap) obj;
			} else if (obj instanceof TemporalData) {
				obj = ((TemporalData) obj).getValue();
				if (obj instanceof KeyMap) {
					keyMap = (KeyMap) obj;
				}
			}
			if (keyMap == null || keyMap.getKeyType() == null) {
				break;
			}
			KeyTypeInfo info = nameMap.get(keyMap.getKeyTypeName());
			if (info == null) {
				info = new GemfireKeyTypeInfo();
				info.setKeyTypeClassName(keyMap.getKeyTypeName());
				info.setMergePoint(keyMap.getKeyType().getMergePoint());
				info.setVersion(keyMap.getKeyTypeVersion());
				info.setInstanceCount(1);
				nameMap.put(info.getKeyTypeClassName(), info);
				keyTypeInfoSet.add(info);
			} else {
				info.setInstanceCount(info.getInstanceCount() + 1);
			}
		}
		if (keyTypeInfoSet.size() > 0) {
			map.put(region.getFullPath(), keyTypeInfoSet);
		}

		Set<Region> subRegionSet = region.subregions(false);
		for (Region region2 : subRegionSet) {
			map = getKeyTypes(region2, map);
		}

		return map;
	}

	/**
	 * Ignored. This method is never used in the server side. It always returns
	 * null.
	 */
	@BizMethod
	@Override
	public IBizContextClient getBizContext()
	{
		return null;
	}

	@BizMethod
	@Override
	public PadoInfo getPadoInfo(String appId)
	{
		return InfoFactory.getInfoFactory().createPadoInfo(appId);
	}

	@BizMethod
	@Override
	public CacheInfo getCacheInfo()
	{
		return new GemfireCacheInfo(PadoServerManager.getPadoServerManager().getGridId(), CacheFactory.getAnyInstance());
	}

	@BizMethod
	@Override
	public List<CacheInfo> getCacheInfoList()
	{
		ArrayList<CacheInfo> list = new ArrayList(2);
		list.add(getCacheInfo());
		return list;
	}

	@BizMethod
	@Override
	public Map<String, List<CacheInfo>> getMapOfCacheInfoList()
	{
		HashMap<String, List<CacheInfo>> map = new HashMap(1, 1f);
		map.put(PadoServerManager.getPadoServerManager().getGridId(), getCacheInfoList());
		return map;
	}

	@BizMethod
	@Override
	public List<ServerInfo> getServerInfoList(String fullPath)
	{
		GridInfo gridInfo = PadoServerManager.getPadoServerManager().getGridInfo();
		CacheInfo cacheInfo = gridInfo.getCacheInfo();
		List<CacheServerInfo> cacheServerInfoList = cacheInfo.getCacheServerInfoList();
		List<ServerInfo> serverInfoList = new ArrayList(cacheServerInfoList.size() + 1);
		for (CacheServerInfo cacheServerInfo : cacheServerInfoList) {
			serverInfoList.add(InfoFactory.getInfoFactory().createServerInfo(gridInfo, cacheInfo, cacheServerInfo,
					fullPath));
		}
		return serverInfoList;
	}

	@BizMethod
	@Override
	public Map<String, List<ServerInfo>> getServerInfoListMap(String fullPath)
	{
		HashMap<String, List<ServerInfo>> map = new HashMap(1, 1f);
		map.put(PadoServerManager.getPadoServerManager().getGridId(), getServerInfoList(fullPath));
		return map;
	}

	@BizMethod
	@Override
	public Map<String, Set<KeyTypeInfo>>[] getKeyTypeInfoMaps()
	{
		Map[] maps = new Map[2];
		Map<String, Set<KeyTypeInfo>> map = KeyTypeManager.getAllRegisteredKeyTypeInfos();
		maps[0] = map;
		map = getAllRegionKeyTypes();
		maps[1] = map;
		return maps;
	}

	@BizMethod
	@Override
	public Map<String, Set<KeyTypeInfo>> getRegisteredKeyTypeInfoMap()
	{
		return KeyTypeManager.getAllRegisteredKeyTypeInfos();
	}

	/**
	 * Returns a map that contains KeyTypeInfo for each region in the server.
	 * 
	 * @return &lt;full path of region, Set&lt;KeyTypeInfo&gt;&gt
	 */
	@BizMethod
	@Override
	public Map<String, Set<KeyTypeInfo>> getRegionKeyTypeInfoMap()
	{
		return getAllRegionKeyTypes();
	}

	@BizMethod
	@Override
	public String[] getRegisteredMainKeyTypeNames()
	{
		return KeyTypeManager.getAllRegisteredMainKeyTypeNames();
	}

	@BizMethod
	@Override
	public void mergeKeyTypeVersions(String gridPath, String keyTypeClassName, int targetVersion, int[] versions)
	{
		String fullPath = GridUtil.getFullPath(gridPath);
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		if (region == null) {
			throw new PadoException(new RegionNotFoundException(gridPath + " is not found in the grid."));
		}

		int toVersion = targetVersion;
		int fromVersions[] = versions;
		KeyType toKeyType = null;
		try {
			Class clazz = Class.forName(keyTypeClassName);
			KeyType[] keyTypes = KeyTypeManager.getAllRegisteredVersions(clazz);
			if (keyTypes == null || keyTypes.length == 0) {
				throw new PadoException("Specified KeyType class not registered: " + keyTypeClassName);
			}
			toKeyType = KeyTypeManager.getKeyType((UUID) keyTypes[0].getId(), toVersion);
		} catch (ClassNotFoundException ex) {
			throw new PadoException("Specified KeyType class not found: " + keyTypeClassName);
		} catch (Exception ex) {
			throw new PadoException("Exception raised in the grid: " + ex.getMessage(), ex);
		}

		if (toKeyType == null) {
			throw new PadoException("The targer version not registred in the grid: [" + keyTypeClassName + ", version="
					+ toVersion + "]");
		}

		Region localRegion;
		if (region instanceof PartitionedRegion) {
			localRegion = PartitionRegionHelper.getLocalPrimaryData(region);
		} else {
			localRegion = region;
		}

		// Transact - very expensive operation
		// TODO: try breaking it down to multiple transactions
		CacheTransactionManager txManager = CacheFactory.getAnyInstance().getCacheTransactionManager();
		HashMap map = new HashMap(1001, 1f);
		try {
			txManager.begin();
			Set<Map.Entry> set = localRegion.entrySet();
			for (Map.Entry entry : set) {
				Object key = entry.getKey();
				Object value = entry.getValue();
				Object data = value;
				if (data instanceof TemporalData) {
					data = ((TemporalData) data).getValue();
				}
				if (data instanceof KeyMap) {
					KeyMap keyMap = (KeyMap) data;
					for (int i = 0; i < fromVersions.length; i++) {
						if (keyMap.getKeyType().getVersion() == fromVersions[i]) {
							keyMap.merge(toKeyType);
							map.put(key, value);
						}
					}
				}
				if (map.size() > 0 && map.size() % 1000 == 0) {
					region.putAll(map);
					map.clear();
				}
			}
			if (map.size() > 0) {
				region.putAll(map);
				map.clear();
			}
			txManager.commit();

		} catch (Exception ex) {

			throw new PadoException("Transaction failed. It is rolled back." + ex.getMessage(), ex);

		}
	}

	@BizMethod
	@Override
	public Set<BizInfo> getAllBizInfos()
	{
		return PadoServerManager.getPadoServerManager().getAllSysBizInfos();
	}

	@BizMethod
	@Override
	public Set<BizInfo> getAllAppBizInfos()
	{
		return PadoServerManager.getPadoServerManager().getAllAppBizInfos();
	}

	@BizMethod
	@Override
	public Map<String, GridPathInfo> getGridPathInfoMap()
	{
		return PadoServerManager.getPadoServerManager().getGridPathInfoMap();
	}

	@BizMethod
	@Override
	public long getCurrentTimeMillis()
	{
		return System.currentTimeMillis();
	}

	@BizMethod
	@Override
	public void deployJars(String[] jarNames, byte[][] jarContents, Date timestamp) throws DeploymentFailedException
	{
		new Deployment().save(jarNames, jarContents, timestamp);
	}

	@BizMethod
	@Override
	public void updateGridRoutingTable(GridRoutingTable routingTable)
	{
		if (routingTable == null) {
			return;
		}
		PadoServerManager.getPadoServerManager().updateGridRoutingTable(routingTable);
	}

	@BizMethod
	@Override
	public void updateGridPaths(Set<GridPathInfo> gridPathInfoSet)
	{
		if (gridPathInfoSet == null) {
			return;
		}
		PadoServerManager.getPadoServerManager().updateGridPaths(gridPathInfoSet);
	}

	@Override
	public void setGridEnabled(String appId, String gridId, boolean enabled)
	{
		PadoServerManager.getPadoServerManager().setGridEnabled(appId, gridId, enabled);
	}

	@Override
	public boolean isGridEnabled(String appId, String gridId)
	{
		return PadoServerManager.getPadoServerManager().isGridEnabled(appId, gridId);
	}

	@Override
	@BizMethod
	public void resetKeyTypeQueryRerences()
	{
		KeyTypeManager.resetDb(PadoUtil.getProperty(Constants.PROP_DB_DIR));
	}

	@Override
	@BizMethod
	public void registerKeyTypeQueryReferences(JsonLite jl, boolean isPersist) throws IOException,
			ClassNotFoundException, PadoException, ClassCastException
	{
		KeyTypeManager.registerQueryReferences(PadoUtil.getProperty(Constants.PROP_DB_DIR), jl, isPersist);
	}

	@Override
	@BizMethod
	public void registerKeyTypeQueryReferences(JsonLite[] list, boolean isPersist) throws IOException,
			ClassNotFoundException, PadoException, ClassCastException
	{
		if (list == null) {
			return;
		}
		for (JsonLite jl : list) {
			KeyTypeManager.registerQueryReferences(PadoUtil.getProperty(Constants.PROP_DB_DIR), jl, isPersist);
		}
	}

	@Override
	@BizMethod
	public void attachToParentGrid(String parentGridId)
	{
		Logger.info("Parent grid attachement command received and processing... [gridId="
				+ PadoServerManager.getPadoServerManager().getGridId() + ", parentGridId=" + parentGridId + "]");
		if (PadoServerManager.getPadoServerManager().getGridId().equals(parentGridId)) {
			Logger.info("Detaching grid failed. The specified grid " + parentGridId + " cannot be same as this grid.");
			return;
		}

		// First, attach itself to the parent grid by connecting to the parent
		PadoServerManager.getPadoServerManager().attachToParentGrid(parentGridId);

		// Next, provide this grid's GridInfo to the attached parent grid to
		// update the child grid metadata.
		GridInfo gridInfo = PadoServerManager.getPadoServerManager().getGridInfo();
		ISysBiz sysBiz = PadoServerManager.getPadoServerManager().getCatalog().newInstance(ISysBiz.class);
		sysBiz.attachToParentGridWithGridInfo(gridInfo);
		Logger.info("The grid " + PadoServerManager.getPadoServerManager().getGridId()
				+ " has successfully been attached to the specified parent grid " + parentGridId + ".");
	}

	@Override
	@BizMethod
	public void detachFromParentGrid(String parentGridId)
	{
		Logger.info("Parent grid detachment command received and processing... [gridId="
				+ PadoServerManager.getPadoServerManager().getGridId() + ", parentGridId=" 
				+ parentGridId + ", user=" + bizContext.getUserContext().getUsername() + "]");
		if (PadoServerManager.getPadoServerManager().getGridId().equals(parentGridId)) {
			Logger.info("Detaching grid failed. The specified grid " + parentGridId + " cannot be same as this grid.");
			return;
		}

		// First, notify the parent grid to remove this grid
		GridInfo gridInfo = PadoServerManager.getPadoServerManager().getGridInfo();
		ISysBiz sysBiz = PadoServerManager.getPadoServerManager().getCatalog().newInstance(ISysBiz.class);
		try {
			sysBiz.detachFromParentGridWithGridInfo(gridInfo);
		} catch (PoolCancelledException ex) {
			// Ignore. This is raised if the connectio pool has been destroyed
			// previously when detaching the grid. GemFire does not provide
			// an API to check whether the pool is connected. This is a
			// workaround.
		}

		// Next, remove itself from its parent notification service, i.e.,
		// heart beats sent to the parent
		PadoServerManager.getPadoServerManager().removeGrid(parentGridId, true);
		Logger.info("This grid has successfully been detached from the specified parent grid " + parentGridId + ") - "
				+ "[user=" + bizContext.getUserContext().getUsername() + "]");
	}

	@Override
	@BizMethod
	public void attachToParentGridWithGridInfo(GridInfo childGridInfo)
	{
		Logger.info("Child grid attachement command received and processing... [user=" 
					+ bizContext.getUserContext().getUsername() + "]");
		if (childGridInfo == null
				|| PadoServerManager.getPadoServerManager().getGridId().equals(childGridInfo.getGridId())) {
			Logger.info("Attaching child grid failed. The specified grid " + childGridInfo.getGridId()
					+ " cannot be same as this grid ID.");
			return;
		}

		PadoServerManager.getPadoServerManager().updateGrid(childGridInfo, false);
		Logger.info("The specified child grid " + childGridInfo.getGridId()
				+ " has successfully been attached to this parent grid (" 
				+ PadoServerManager.getPadoServerManager().getGridId() + ") - "
				+ "[user=" + bizContext.getUserContext().getUsername() + "]");
	}

	@Override
	@BizMethod
	public void detachFromParentGridWithGridInfo(GridInfo parentGridInfo)
	{
		Logger.info("Parent grid detachment command received and processing... [user="
				+ bizContext.getUserContext().getUsername() + "]");
		if (parentGridInfo == null
				|| PadoServerManager.getPadoServerManager().getGridId().equals(parentGridInfo.getGridId())) {
			Logger.info("Detaching grid failed. The specified grid " + parentGridInfo.getGridId()
					+ " cannot be same as this grid ID.");
			return;
		}

		PadoServerManager.getPadoServerManager().removeGrid(parentGridInfo, false);
		Logger.info("This grid has successfully been detached from the specified parent grid "
				+ parentGridInfo.getGridId()+ ") - "
				+ "[user=" + bizContext.getUserContext().getUsername() + "]");
	}

	@Override
	@BizMethod(bizType = BizType.PADO)
	public void attachChildToParentGrid(String childGridId)
	{
		// Not supported
	}

	@Override
	@BizMethod
	public void detachChildFromParentGrid(String childGridId)
	{
		Logger.info("Child grid detachment command received and processing... [gridId="
				+ PadoServerManager.getPadoServerManager().getGridId() + ", childGridId=" + childGridId 
				+ ", user=" + bizContext.getUserContext().getUsername() + "]");
		if (PadoServerManager.getPadoServerManager().getGridId().equals(childGridId)) {
			Logger.info("Detaching grid failed. The specified grid " + childGridId + " cannot be same as this grid.");
			return;
		}

		PadoServerManager.getPadoServerManager().removeGrid(childGridId, false);
		Logger.info("The specified child grid " + childGridId + 
				" has successfully been detached from the this parent grid (" +
				PadoServerManager.getPadoServerManager().getGridId() + ") - "
				+ "[user=" + bizContext.getUserContext().getUsername() + "]");
	}
}
