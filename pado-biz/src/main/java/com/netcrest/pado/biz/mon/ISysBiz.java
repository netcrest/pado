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
package com.netcrest.pado.biz.mon;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.BizType;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.annotation.WithGridCollector;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.info.BizInfo;
import com.netcrest.pado.info.CacheInfo;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.GridPathInfo;
import com.netcrest.pado.info.KeyTypeInfo;
import com.netcrest.pado.info.PadoInfo;
import com.netcrest.pado.info.ServerInfo;
import com.netcrest.pado.internal.impl.GridRoutingTable;

/**
 * ISysBiz provides system-level metadata. This class is only available to users
 * with administration privileges.
 * 
 * @author dpark
 * 
 */
@BizClass(name = "ISysBiz")
public interface ISysBiz extends IBiz
{
	/**
	 * Returns PadoInfo that contains Pado system-level information. BizType is
	 * PADO such that this biz method is executed in the grid that the user is
	 * logged on.
	 * 
	 * @param appId
	 *            Application ID
	 */
	@BizMethod(bizType = BizType.PADO)
	@OnServer
	PadoInfo getPadoInfo(String appId);

	/**
	 * Returns CacheInfo from a single server of a grid.
	 */
	@BizMethod
	@OnServer
	CacheInfo getCacheInfo();

	/**
	 * Returns a list of CacheInfo objects from one or more grids.
	 */
	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	List<CacheInfo> getCacheInfoList();

	/**
	 * Returns a map of CacheInfo lists from all of the grids. The map has
	 * &lt;gridId, List&lt;CacheInfo&gt;&gt; entries. This is a broadcast call
	 * to all grids.
	 */
	@BizMethod
	@OnServer(broadcast = true, broadcastGrids = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.GridMapOfListCollector")
	Map<String, List<CacheInfo>> getMapOfCacheInfoList();

	/**
	 * Returns a list of ServerInfo objects from one or more grids.
	 * 
	 * @param fullPath
	 *            Full path. If null, it returns a list of the entire ServerInfo
	 *            objects.
	 */
	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	List<ServerInfo> getServerInfoList(String fullPath);

	/**
	 * Returns a map of ServerInfo lists from all of the grids. The map has
	 * &lt;gridId, List&lt;ServerInfo&gt;&gt; entries. This is a broadcast call
	 * to all grids.
	 * 
	 * @param fullPath
	 *            Full path. If null, it returns a map of the entire ServerInfo
	 *            objects.
	 */
	@BizMethod
	@OnServer(broadcast = true, broadcastGrids = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.GridMapOfListCollector")
	Map<String, List<ServerInfo>> getServerInfoListMap(String fullPath);

	/**
	 * Returns all registered KeyMap key types.
	 */
	@BizMethod
	@OnServer
	Map<String, Set<KeyTypeInfo>>[] getKeyTypeInfoMaps();

	/**
	 * Returns all registered key types from all of the grids. It contacts only
	 * one server per grid.
	 */
	@BizMethod
	@OnServer(broadcastGrids = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.SortedMapOfSortedSetsCollector")
	Map<String, Set<KeyTypeInfo>> getRegisteredKeyTypeInfoMap();

	/**
	 * Returns a map of &lt;gridId, Set&lt;KeyTypeInfo&gt;&gt; entries
	 * containing all of key type versions actively used in the grid(s).
	 * 
	 * @return &lt;gridId, Set&lt;KeyTypeInfo&gt;&gt;
	 */
	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.mon.SortedMapOfSortedSetsKeyTypeInfoCollector")
	Map<String, Set<KeyTypeInfo>> getRegionKeyTypeInfoMap();

	/**
	 * Returns all registered KeyType main class names from all of the grids. It
	 * contacts only one server per grid.
	 */
	@BizMethod
	@OnServer(broadcastGrids = true)
	String[] getRegisteredMainKeyTypeNames();

	/**
	 * Merges all of the KeyMap objects in the specified grid path from the
	 * specified versions to the target version. This call blocks and may take a
	 * long time to complete.
	 * 
	 * @param gridPath
	 *            Grid path
	 * @param keyTypeClassName
	 *            KeyType fully-qualified class name
	 * @param targetVersion
	 *            Target version to merge
	 * @param versions
	 *            Versions to merge to the target version. These version numbers
	 *            must be less than the target version number.
	 */
	@BizMethod
	@OnServer(broadcast = true)
	void mergeKeyTypeVersions(String gridPath, String keyTypeClassName, int targetVersion, int[] versions);

	/**
	 * Returns all BizInfo object information including system and app-level
	 * IBiz classes.
	 */
	@BizMethod
	@OnServer(broadcast = false)
	Set<BizInfo> getAllBizInfos();

	/**
	 * Returns all BizInfo object information including system and app-level
	 * IBiz classes.
	 * 
	 * @param regex
	 *            Regular expression for filtering IBiz class names. If null, no
	 *            filtering occurs (same as calling {@link #getAllBizInfos()}.
	 */
	@BizMethod
	@OnServer(broadcast = false)
	Set<BizInfo> getBizInfos(String regex);

	/**
	 * Returns all BizInfo object information pertaining only to app-level IBiz
	 * classes. The system-level BizInfo objects are not included.
	 */
	@BizMethod
	@OnServer(broadcast = false)
	Set<BizInfo> getAllAppBizInfos();

	/**
	 * Returns all BizInfo object information pertaining only to app-level IBiz
	 * classes. The system-level BizInfo objects are not included.
	 * 
	 * @param regex
	 *            Regular expression for filtering IBiz class names. If null, no
	 *            filtering occurs (same as calling
	 *            {@link #getAllAppBizInfos()}.
	 */
	@BizMethod
	@OnServer(broadcast = false)
	Set<BizInfo> getAppBizInfos(String regex);

	/**
	 * Returns all system-level BizInfo object information. The app-level
	 * BizInfo objects are not included.
	 */
	@BizMethod
	@OnServer(broadcast = false)
	Set<BizInfo> getAllSysBizInfos();

	/**
	 * Returns all system-level BizInfo object information. The app-level
	 * BizInfo objects are not included.
	 * 
	 * @param regex
	 *            Regular expression for filtering IBiz class names. If null, no
	 *            filtering occurs (same as calling
	 *            {@link #getAllSysBizInfos()}.
	 */
	@BizMethod
	@OnServer(broadcast = false)
	Set<BizInfo> getSysBizInfos(String regex);

	/**
	 * Returns a map of all GridPathInfo objects maintained by Pado.
	 * &lt;gridPath, GridPathInfo&gt;. Note that GridPathInfo represents only a
	 * grid path configured in the pado XML config file or via tool/API.
	 */
	@BizMethod(bizType = BizType.PADO)
	@OnServer
	Map<String, GridPathInfo> getGridPathInfoMap();

	/**
	 * Returns the current time of one of servers in the grid.
	 */
	@BizMethod
	@OnServer
	long getCurrentTimeMillis();

	/**
	 * Deploys jar files to all of the grids.
	 * 
	 * @param jarNames
	 *            List of jar names.
	 * @param jarContents
	 *            List of jar file contents. The list must match the jar name
	 *            list.
	 * @param timestamp
	 *            Timestamp to be appended to the jar files in the server. The
	 *            timestamp should be obtained from the server by invoking
	 *            {@link #getCurrentTimeMillis()}.
	 * @throws DeploymentFailedException
	 *             Thrown if deployment fails
	 */
	@BizMethod
	@OnServer(broadcast = true, broadcastGrids = true)
	void deployJars(String[] jarNames, byte[][] jarContents, Date timestamp) throws DeploymentFailedException;

	/**
	 * Updates the routing table for a single app. A routing table is dedicated
	 * per app.
	 * 
	 * @param routingTable
	 *            Grid routing table (GRP)
	 */
	@BizMethod(bizType = BizType.PADO)
	@OnServer
	void updateGridRoutingTable(GridRoutingTable routingTable);

	/**
	 * Updates the grid path info objects that configure routers and routing
	 * tables. This update effectively replaces the all of the current grid
	 * paths that are being used in real time.
	 * 
	 * @param gridPathInfoSet
	 *            A set containing the entire grid paths
	 */
	@BizMethod(bizType = BizType.PADO)
	@OnServer(broadcast = true)
	void updateGridPaths(Set<GridPathInfo> gridPathInfoSet);

	/**
	 * Enables or disables the specified grid for the specified app ID. If
	 * disabled, the grid no longer participates in the Pado arena. If enabled,
	 * the grid immediately participates in the Pado arena.
	 * 
	 * @param appId
	 *            App ID
	 * @param gridId
	 *            Grid ID
	 * @param enabled
	 *            true to enable, false to disable
	 */
	@BizMethod(bizType = BizType.PADO)
	@OnServer
	void setGridEnabled(String appId, String gridId, boolean enabled);

	/**
	 * Returns true if the grid is enabled to participate in the Pado arena for
	 * the specified app ID. If it returns false then the grid does not
	 * participate in the Pado arena.
	 * 
	 * @param appId
	 *            App ID
	 * @param gridId
	 *            Grid ID
	 */
	@BizMethod(bizType = BizType.PADO)
	@OnServer
	boolean isGridEnabled(String appId, String gridId);

	/**
	 * Resets all KeyType query references to the persistent state.
	 */
	@BizMethod
	@OnServer(broadcast = true, broadcastGrids = true)
	public void resetKeyTypeQueryRerences();

	/**
	 * Registers the specified KeyType definition to all grids and servers.
	 * 
	 * @param keyTypeDefinition
	 *            JsonLite containing the key type definition for query
	 *            references. It must be of the following example format:
	 * 
	 *            <pre>
	 * {
	 *    "KeyType": "com.netcrest.pado.temporal.test.data.Portfolio",
	 *    "AccountId": {
	 *       "Query": "account",
	 *      "Depth": 2
	 *    },
	 *    "Positions": {
	 *       "Query": "position",
	 *       "Depth": 3
	 *    }
	 * }
	 *            </pre>
	 * 
	 * @param isPersist
	 *            True to persist, false to register in memory only. If false,
	 *            then the changes will be lost when the server restarts. This
	 *            could result in inconsistent query reference settings if the
	 *            stopped server restarts while others are still running. As a
	 *            best practice, it is recommended key type definitions should
	 *            be always persist.
	 * @throws IOException
	 *             Thrown if isPersist is true and persistence fails.
	 * @throws ClassNotFoundException
	 *             Thrown if the KeyType class name defined in the
	 *             dkeyTypeDefinition object is not found.
	 * @throws PadoException
	 *             Thrown if the KeyType class is not an enum class.
	 * @throws ClassCastException
	 *             Thrown if the class is not of KeyType.
	 */
	@SuppressWarnings("rawtypes")
	@BizMethod
	@OnServer(broadcast = true, broadcastGrids = true)
	void registerKeyTypeQueryReferences(JsonLite keyTypeDefinition, boolean isPersist)
			throws IOException, ClassNotFoundException, PadoException, ClassCastException;

	/**
	 * Registers the specified KeyType definitions to all grids and servers.
	 * 
	 * @param keyTypeDefinitions
	 *            An array of KeyType query reference definitions.
	 * @param isPersist
	 *            True to persist, false to register in memory only. If false,
	 *            then the changes will be lost when the server restarts. This
	 *            could result in inconsistent query reference settings if the
	 *            stopped server restarts while others are still running. As a
	 *            best practice, it is recommended key type definitions should
	 *            be always persist.
	 * @throws IOException
	 *             Thrown if isPersist is true and persistence fails.
	 * @throws ClassNotFoundException
	 *             Thrown if the KeyType class name defined in one of the
	 *             KeyType definition objects is not found.
	 * @throws PadoException
	 *             Thrown if one of the KeyType class is not an enum class.
	 * @throws ClassCastException
	 *             Thrown if one of the classes is not of KeyType.
	 */
	@SuppressWarnings("rawtypes")
	@BizMethod
	@OnServer(broadcast = true, broadcastGrids = true)
	void registerKeyTypeQueryReferences(JsonLite[] keyTypeDefinitions, boolean isPersist)
			throws IOException, ClassNotFoundException, PadoException, ClassCastException;

	/**
	 * Attaches the grid that this client has logged on to to the specified
	 * parent grid.
	 * 
	 * @param parentGridId
	 *            Parent grid ID
	 */
	@BizMethod(bizType = BizType.PADO)
	@OnServer(broadcast = true)
	void attachToParentGrid(String parentGridId);

	/**
	 * Detaches the grid to from the specified parent grid. Note that unlike the
	 * other attach/detach methods, this method requires the grid from which
	 * this method to be executed. Set the grid ID by invoking
	 * ISysBiz.getBizContext().getGridContextClient().setGridIds(gridId);
	 * 
	 * @param parentGridId
	 *            Parent grid ID
	 */
	@BizMethod
	@OnServer(broadcast = true)
	void detachFromParentGrid(String parentGridId);

	/**
	 * Attaches (updates or adds) the specified grid as a child grid to the
	 * parent grid.
	 * 
	 * @param childGridInfo
	 *            Child GridInfo object
	 */
	@BizMethod(bizType = BizType.PADO)
	@OnServer(broadcast = true)
	void attachToParentGridWithGridInfo(GridInfo childGridInfo);

	/**
	 * Detaches the grid that this client has logged on to from the specified
	 * parent grid.
	 * 
	 * @param gridInfo
	 */
	@BizMethod(bizType = BizType.PADO)
	@OnServer(broadcast = true)
	void detachFromParentGridWithGridInfo(GridInfo gridInfo);

	/**
	 * Attaches the specified child grid to the grid that this client has logged
	 * on to.
	 * 
	 * @param childGridId
	 */
	@BizMethod(bizType = BizType.PADO)
	@OnServer(broadcast = true)
	void attachChildToParentGrid(String childGridId);

	/**
	 * Detaches the grid that this client has logged on to from its parent grid.
	 */
	@BizMethod(bizType = BizType.PADO)
	@OnServer(broadcast = true)
	void detachChildFromParentGrid(String childGridId);
}
