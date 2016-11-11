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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.ExpirationAction;
import com.gemstone.gemfire.cache.ExpirationAttributes;
import com.gemstone.gemfire.cache.InterestPolicy;
import com.gemstone.gemfire.cache.PartitionAttributes;
import com.gemstone.gemfire.cache.PartitionAttributesFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionAttributes;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.RegionShortcut;
import com.gemstone.gemfire.cache.Scope;
import com.gemstone.gemfire.cache.SubscriptionAttributes;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.client.PoolManager;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.netcrest.pado.IGridRouter;
import com.netcrest.pado.IUserPrincipal;
import com.netcrest.pado.biz.file.CompositeKeyInfo;
import com.netcrest.pado.biz.gemfire.proxy.GemfireBizManager;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.exception.BizException;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.exception.PadoServerException;
import com.netcrest.pado.gemfire.info.GemfireAppInfo;
import com.netcrest.pado.gemfire.info.GemfireCacheInfo;
import com.netcrest.pado.gemfire.info.GemfireGridInfo;
import com.netcrest.pado.gemfire.info.GemfireLoginInfo;
import com.netcrest.pado.gemfire.info.GemfireRegionInfo;
import com.netcrest.pado.gemfire.info.GemfireUserLoginInfo;
import com.netcrest.pado.gemfire.util.GemfireGridUtil;
import com.netcrest.pado.gemfire.util.RegionUtil;
import com.netcrest.pado.gemfire.util.TaskFunction;
import com.netcrest.pado.index.gemfire.function.EntitySearchCacheRetrieveBaseFunction;
import com.netcrest.pado.index.gemfire.function.IndexMatrixBuildFunction;
import com.netcrest.pado.index.gemfire.function.OQLEntitySearchFunction;
import com.netcrest.pado.index.gemfire.function.ServerEntitySearchFunction;
import com.netcrest.pado.index.gemfire.function.TemporalEntitySearchFunction;
import com.netcrest.pado.index.gemfire.function.TemporalEntrySearchFunction;
import com.netcrest.pado.index.internal.IndexMatrixUtil;
import com.netcrest.pado.index.provider.lucene.LuceneTemporalQueryFunction;
import com.netcrest.pado.index.provider.lucene.LuceneTemporalSearchFunction;
import com.netcrest.pado.info.AppGridInfo;
import com.netcrest.pado.info.AppInfo;
import com.netcrest.pado.info.BizInfo;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.GridPathInfo;
import com.netcrest.pado.info.GridRouterInfo;
import com.netcrest.pado.info.LoginInfo;
import com.netcrest.pado.info.PathInfo;
import com.netcrest.pado.info.message.GridStatusInfo;
import com.netcrest.pado.info.message.GridStatusInfo.Status;
import com.netcrest.pado.info.message.MessageType;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.config.dtd.PathConfig;
import com.netcrest.pado.internal.config.dtd.PathListConfig;
import com.netcrest.pado.internal.factory.BizManagerFactory;
import com.netcrest.pado.internal.factory.InfoFactory;
import com.netcrest.pado.internal.factory.InternalFactory;
import com.netcrest.pado.internal.impl.GridRoutingTable;
import com.netcrest.pado.internal.impl.GridRoutingTable.Grid;
import com.netcrest.pado.internal.impl.GridService;
import com.netcrest.pado.internal.security.AESCipher;
import com.netcrest.pado.internal.server.BizManager;
import com.netcrest.pado.internal.server.impl.CatalogServerImpl;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.internal.util.StringUtil;
import com.netcrest.pado.link.IGridBizLink;
import com.netcrest.pado.link.IPadoBizLink;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.security.RSACipher;
import com.netcrest.pado.server.MasterFailoverListener;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.temporal.gemfire.GemfireTemporalManager;
import com.netcrest.pado.temporal.gemfire.impl.TemporalCacheListener;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class GemfirePadoServerManager extends PadoServerManager
{
	private Cache cache;
	private Region rootRegion;
	private Region<Object, LoginInfo> loginRegion;
	// <appId, AppInfo>
	private Region<String, GemfireAppInfo> appRegion;
	// <gridId, GridInfo>
	private Region<String, GridInfo> gridRegion;
	// <gridPath, GridRouterInfo>
	private Region<String, GridRouterInfo> routerRegion;
	// <serverId, BucketId> - Not Used 9/23/2012
	private Region<String, GridInfo> serverRegion;
	private Region messageRegion;
	// Global region holding internals that are grid specific. For example,
	// MasterServerLock uses this region to obtain a lock used to assign
	// master/slave.
	private Region systemRegion;

	// Veritual path region. Contains VP definitions.
	private Region<String, KeyMap> vpRegion;

	// <gridId, Region>
	private Map<String, Region> parentRootRegionMap = new ConcurrentHashMap<String, Region>(2);
	private Map<String, Region> parentPadoRegionMap = new ConcurrentHashMap<String, Region>(2);
	private Map<String, Region> parentAppRegionMap = new ConcurrentHashMap<String, Region>(2);
	private Map<String, Region> parentGridRegionMap = new ConcurrentHashMap<String, Region>(2);
	private Map<String, Region> parentLoginRegionMap = new ConcurrentHashMap<String, Region>(2);
	private Map<String, Region> parentMessageRegionMap = new ConcurrentHashMap<String, Region>(2);
	private Map<String, Region> parentRouterRegionMap = new ConcurrentHashMap<String, Region>(2);
	private Map<String, Region> parentServerRegionMap = new ConcurrentHashMap<String, Region>(2);

	// <gridId, Region>
	private Map<String, Region> childRootRegionMap = new ConcurrentHashMap<String, Region>(5);
	private Map<String, Region> childPadoRegionMap = new ConcurrentHashMap<String, Region>(5);
	private Map<String, Region> childAppRegionMap = new ConcurrentHashMap<String, Region>(5);
	private Map<String, Region> childGridRegionMap = new ConcurrentHashMap<String, Region>(5);
	private Map<String, Region> childLoginRegionMap = new ConcurrentHashMap<String, Region>(5);
	private Map<String, Region> childMessageRegionMap = new ConcurrentHashMap<String, Region>(5);
	private Map<String, Region> childRouterRegionMap = new ConcurrentHashMap<String, Region>(5);
	private Map<String, Region> childServerRegionMap = new ConcurrentHashMap<String, Region>(5);

	// default connection name for grids to connect to the grid this cache is
	// member
	// of. If not specified, it defaults to the grid ID.
	private String connectionName;
	// shared connection name for grid-to-grid communications
	private String sharedConnectionName;
	// default IndexMatrix connection name for grids. This can be null if not
	// defined.
	private String indexMatrixConnectionName;
	// default connection name for clients to connect to the grid this cache is
	// member
	// of clientConnectionName defaults to connectionName if not specified.
	private String clientConnectionName;
	// shared connection is client connection with multiuser-authentication
	// disabled (false)
	private String clientSharedConnectionName;
	// single hop flag for clientConnectionName. If not defined, then false.
	private boolean clientConnectionSingleHopEnabled;
	// multiuser enabled flag. clients require RegionService if enabled.
	private boolean clientConnectionMultiuserAuthenticationEnabled;
	// default client locators.
	private String clientLocators;
	// default IndexMatrix connection name for clients. This can be null if not
	// defined. clientIndexMatrixConnectionName defaults to
	// indexMatrixConnectionName if not
	// specified.
	private String clientIndexMatrixConnectionName;

	public GemfirePadoServerManager()
	{
	}

	private void logDisclaimer(String disclaimer)
	{
		LogWriter logger = CacheFactory.getAnyInstance().getLogger();
		logger.info(disclaimer);
	}

	/**
	 * Configures this grid. It initializes internal regions and launches the
	 * "GridRegion Updater" timer thread that periodically broadcasts its
	 * GridInfo to the parents. Note that it does not configure client
	 * connections.
	 * 
	 * @param padoConfig
	 *            JABX pado config object
	 * @param configFilePath
	 *            Configuration file path used for loggin purpose only.
	 */
	protected void config(com.netcrest.pado.internal.config.dtd.generated.Pado padoConfig, String configFilePath)
	{
		cache = CacheFactory.getAnyInstance();
		com.netcrest.pado.internal.config.dtd.generated.Gemfire gemfire = padoConfig.getGemfire();
		gridId = padoConfig.getId();
		siteId = PadoUtil.getProperty(Constants.PROP_SITE_ID, Constants.DEFAULT_SITE_ID);
		serverId = cache.getDistributedSystem().getDistributedMember().getId();
		serverNum = PadoUtil.getProperty(Constants.PROP_SERVER_NUM);
		PadoUtil.getPadoProperties().setProperty(Constants.PROP_SERVER_ID, serverId);
		serverName = cache.getName();
		location = padoConfig.getLocation();
		if (gridId == null) {
			throw new PadoException("pado id (gridId) undefined in the config file, " + configFilePath
					+ ". Please check the config file. A unique grid ID is required. It must be an alphanumeric string begins with a letter.");
		}
		if (location == null) {
			throw new PadoException("pado location undefined in the config file, " + configFilePath
					+ ". Please check the config file. Location must be specified.");
		}
		connectionName = gemfire.getPoolName();
		if (connectionName == null) {
			connectionName = gridId + "-pool";
			Logger.config("gemfire pool-name undefined in the config file, " + configFilePath
					+ ". gemfire pool-name is assigned to, " + connectionName);
		}

		locators = gemfire.getLocators();
		if (locators == null) {
			String val = cache.getDistributedSystem().getProperties().getProperty("locators");
			if (val != null) {
				val = val.replaceAll("\\[", ":");
				locators = val.replaceAll("\\]", "");
			}
			Logger.config("locators undefined in the config file, " + configFilePath
					+ ". The default locators will be used for grid-to-grid communications. locators=" + locators);
		}

		isParent = gemfire.getIsParent().equals("true");

		com.netcrest.pado.internal.config.dtd.generated.GemfireClient gemfireClient = gemfire.getGemfireClient();
		if (gemfireClient == null) {

			clientConnectionName = connectionName;
			clientSharedConnectionName = clientConnectionName + "-shared";
			clientConnectionSingleHopEnabled = false;
			clientConnectionMultiuserAuthenticationEnabled = false;
			String host = cache.getDistributedSystem().getDistributedMember().getHost();
			clientLocators = locators.replaceAll("localhost", host);
			clientIndexMatrixConnectionName = indexMatrixConnectionName = clientSharedConnectionName;

		} else {
			clientConnectionName = gemfireClient.getPoolName();
			if (clientConnectionName == null) {
				clientConnectionName = connectionName;
				Logger.config("gemfire-client pool-name undefined in the config file, " + configFilePath
						+ ". gemfire-client pool-name is assigned to the value of pado pool-name, " + connectionName);
			}

			clientSharedConnectionName = gemfireClient.getSharedPoolName();
			if (clientSharedConnectionName == null) {
				clientSharedConnectionName = clientConnectionName + "-shared";
				Logger.config("gemfire-client shared-pool-name undefined in the config file, " + configFilePath
						+ ". gemfire-client shared-pool-name is assigned to " + clientSharedConnectionName);
			}

			String val = gemfireClient.getPoolSingleHopEnabled();
			if (val == null) {
				Logger.config("gemfire-client pool-single-hop-enabled undefined. "
						+ "All client communications will be done with single hop disabled.");
			} else {
				clientConnectionSingleHopEnabled = val.equals("true");
			}

			val = gemfireClient.getPoolMultiuserAuthenticationEnabled();
			if (val == null) {
				Logger.config("gemfire-client client-pool-multiuser-authentication undefined in the config file, "
						+ configFilePath
						+ ". Multiuser mode is disabled unless defined in the GemFire config file, server.xml.");
			} else {
				clientConnectionMultiuserAuthenticationEnabled = val.equals("true");
			}

			clientLocators = gemfireClient.getLocators();
			if (clientLocators == null) {
				Logger.config("gemfire-client locators undefined in the config file, " + configFilePath
						+ ". The default locators will be used for client-to-(this)grid communications. locators="
						+ locators);
				clientLocators = locators;
			}

			clientIndexMatrixConnectionName = gemfireClient.getIndexMatrixPoolName();
			if (clientIndexMatrixConnectionName == null) {
				clientIndexMatrixConnectionName = indexMatrixConnectionName;
				if (clientIndexMatrixConnectionName == null) {
					clientIndexMatrixConnectionName = clientSharedConnectionName;
					Logger.config("gemfire-client client-index-matrix-pool-name undefined in the config file, "
							+ configFilePath + ". gemfire-client index-matrix-pool-name is assigned to pool-name, "
							+ clientSharedConnectionName);
				}
			}
		}

		// sharedConnectionName & indexMatrixConnectionName
		sharedConnectionName = gemfire.getSharedPoolName();
		indexMatrixConnectionName = gemfire.getIndexMatrixPoolName();
		if (sharedConnectionName == null) {
			sharedConnectionName = indexMatrixConnectionName;
		}
		if (indexMatrixConnectionName == null) {
			indexMatrixConnectionName = sharedConnectionName;
		}
		if (sharedConnectionName == null) {
			sharedConnectionName = clientSharedConnectionName;
			Logger.config("gemfire shared-pool-name undefined in the config file, " + configFilePath
					+ ". index-matrix-pool-name is assigned to gemfire client-shared-pool-name, "
					+ clientSharedConnectionName);
		}
		if (indexMatrixConnectionName == null) {
			indexMatrixConnectionName = clientIndexMatrixConnectionName;
			Logger.config("gemfire index-matrix-pool-name undefined in the config file, " + configFilePath
					+ ". index-matrix-pool-name is assigned to gemfire clinet-index-matrix-pool-name, "
					+ clientIndexMatrixConnectionName);
		}

		// Log server config
		Logger.config(
				"Pado configuration: [" + "gridId=" + gridId + ", isParent=" + isParent + ", poolName=" + connectionName
						+ ", indexMatrixConnectionName=" + indexMatrixConnectionName + ", locators=" + locators + "]");

		// Log client settings
		Logger.config("Client configuration: [" + "clientConnectionName=" + clientConnectionName
				+ ", clientSharedConnectionName=" + clientSharedConnectionName + ", clientConnectionSingleHopEnabled="
				+ clientConnectionSingleHopEnabled + ", clientConnectionMultiuserAuthenticationEnabled="
				+ clientConnectionMultiuserAuthenticationEnabled + ", clientLocators=" + clientLocators
				+ ", clientIndexMatrixConnectionName=" + clientIndexMatrixConnectionName + "]");

		// Create __pado regions (partitioned) for the grid
		initGridRegions(configFilePath);

		initIndexMatrixRegions();

		// Initialize app-specifics
		initAppRegions(padoConfig, configFilePath);

		// Launch the grid updater thread that periodically updates GridInfo
		// to parents (parent grid regions) as well as to itself (its grid
		// region).
		launchGridUpdaterThread(configFilePath);
	}

	public long getInitDelay()
	{
		return Long.parseLong(padoConfig.getGemfire().getInitDelay());
	}

	public Map<String, GridPathInfo> getGridPathInfoMap()
	{
		return gridPathInfoMap;
	}

	/**
	 * Initializes all pado regions, i.e., __pado and its sub-regions. The
	 * regions created effectively elevates this grid to the Pado status. They
	 * are used internally by clients and other grids to interact.
	 */
	private void initGridRegions(String configFilePath)
	{
		com.netcrest.pado.internal.config.dtd.generated.Gemfire gemfire = padoConfig.getGemfire();
		String rootRegionPath = gemfire.getRootRegionPath();
		if (rootRegionPath == null) {
			rootRegionPath = "/" + gridId;
			Logger.config("gemfire root-region-path undefined in the config file, " + configFilePath
					+ ". gemfire root-region-path is assigned to gridId, " + rootRegionPath);
		} else if (rootRegionPath.startsWith("/") == false) {
			rootRegionPath = "/" + rootRegionPath;
		}
		gridIdMap.put(rootRegionPath, gridId);
		this.rootRegion = cache.getRegion(rootRegionPath);

		RegionFactory rf = cache.createRegionFactory();
		rf.setDataPolicy(DataPolicy.EMPTY);
		rf.setScope(Scope.DISTRIBUTED_NO_ACK);
		if (rootRegion == null) {
			String rootRegionName = GemfireGridUtil.getRootName(rootRegionPath);
			this.rootRegion = cache.createRegionFactory().create(rootRegionName);
		}
		int totalNumBuckets = 113;
		try {
			totalNumBuckets = Integer.parseInt(gemfire.getRouterRegionTotalNumBuckets());
		} catch (Exception ex) {
			Logger.config(
					"Error occurred while reading gemfire router-region-total-num-buckets. It is assigned to the default value of "
							+ totalNumBuckets + ".");
		}

		Region padoRegion = rootRegion.getSubregion("__pado");
		if (padoRegion == null) {
			padoRegion = rf.createSubregion(rootRegion, "__pado");
		}
		loginRegion = padoRegion.getSubregion("login");
		appRegion = padoRegion.getSubregion("app");
		gridRegion = padoRegion.getSubregion("grid");
		messageRegion = padoRegion.getSubregion("message");
		routerRegion = padoRegion.getSubregion("router");
		serverRegion = padoRegion.getSubregion("server");
		systemRegion = padoRegion.getSubregion("system");
		vpRegion = padoRegion.getSubregion("vp");
		rf = cache.createRegionFactory();
		rf.setDataPolicy(DataPolicy.PARTITION);
		PartitionAttributesFactory paf = new PartitionAttributesFactory();
		paf.setRedundantCopies(1);
		paf.setTotalNumBuckets(totalNumBuckets);
		rf.setPartitionAttributes(paf.create());

		if (loginRegion == null) {
			// 15 min user login idle timeout
			// TODO: add this to the dtd file.
			long userIdleTimeoutInterval = 15000 * 60;
			String idleTimeStr = PadoUtil.getProperty(Constants.PROP_USER_IDLE_TIMEOUT);
			if (idleTimeStr != null) {
				try {
					userIdleTimeoutInterval = Long.parseLong(idleTimeStr);
					// minimum user idle timeout is 5 min
					// long minimumIdleTimeoutInterval = 5000 * 60;
					long minimumIdleTimeoutInterval = 0;
					if (userIdleTimeoutInterval < minimumIdleTimeoutInterval) {
						userIdleTimeoutInterval = minimumIdleTimeoutInterval;
						Logger.config(Constants.PROP_USER_IDLE_TIMEOUT + " " + idleTimeStr
								+ " msec is too low. It must be greater than or equal to " + userIdleTimeoutInterval
								+ " msec. Set to the miminum of " + userIdleTimeoutInterval + " msec.");
					}
				} catch (Exception ex) {
					Logger.config("Unable to parse " + Constants.PROP_USER_IDLE_TIMEOUT + " " + idleTimeStr
							+ ". Defaulting to " + userIdleTimeoutInterval + " msec");
				}
			}
			Logger.config(Constants.PROP_USER_IDLE_TIMEOUT + " set to " + userIdleTimeoutInterval + " msec ("
					+ userIdleTimeoutInterval / 60000 + " min)");

			ExpirationAttributes expAttrs = new ExpirationAttributes((int) (userIdleTimeoutInterval / 1000),
					ExpirationAction.DESTROY);
			RegionFactory rf2 = cache.createRegionFactory();
			rf2.setDataPolicy(DataPolicy.REPLICATE);
			rf2.setEntryIdleTimeout(expAttrs);
			rf2.setStatisticsEnabled(true);
			rf2.addCacheListener(new LoginServerCacheListenerImpl());
			loginRegion = rf2.createSubregion(padoRegion, "login");
		}
		if (appRegion == null) {
			RegionFactory rf2 = cache.createRegionFactory();
			rf2.setDataPolicy(DataPolicy.REPLICATE);
			appRegion = rf2.createSubregion(padoRegion, "app");
		}
		if (routerRegion == null) {
			routerRegion = rf.createSubregion(padoRegion, "router");
		}
		if (serverRegion == null) {
			serverRegion = rf.createSubregion(padoRegion, "server");
		}
		if (gridRegion == null) {
			int updateInterval = Integer.parseInt(gemfire.getGridInfoUpdateInterval());
			RegionFactory rf2 = cache.createRegionFactory();
			rf2.setDataPolicy(DataPolicy.REPLICATE);
			// af2.addCacheListener(new GridInfoCacheListenerImpl());
			// tack on additional 10 sec
			ExpirationAttributes expAttrs = new ExpirationAttributes((updateInterval / 1000) + 10,
					ExpirationAction.LOCAL_DESTROY);
			rf2.setEntryIdleTimeout(expAttrs);
			gridRegion = rf2.createSubregion(padoRegion, "grid");
		}

		// broadcast
		if (messageRegion == null) {
			RegionFactory rf2 = cache.createRegionFactory();
			// rf2.setDataPolicy(DataPolicy.REPLICATE);
			rf2.setScope(Scope.DISTRIBUTED_NO_ACK);
			rf2.setDataPolicy(DataPolicy.EMPTY);
			SubscriptionAttributes sa = new SubscriptionAttributes(InterestPolicy.ALL);
			rf2.setSubscriptionAttributes(sa);
			messageRegion = rf.createSubregion(padoRegion, "message");
		}

		if (systemRegion == null) {
			RegionFactory rf2 = cache.createRegionFactory();
			rf2.setDataPolicy(DataPolicy.REPLICATE);
			rf2.setScope(Scope.GLOBAL);
			systemRegion = rf2.createSubregion(padoRegion, "system");
		}
		if (vpRegion == null) {
			RegionFactory rf2 = cache.createRegionFactory(RegionShortcut.REPLICATE);
			rf2.setScope(Scope.GLOBAL);
			vpRegion = rf2.createSubregion(padoRegion, "vp");
		}
	}

	/**
	 * Initializes Index Matrix regions. It creates them if they do not exist.
	 */
	private void initIndexMatrixRegions()
	{
		String rootRegionPath = getRootRegion().getFullPath();
		String systemRegionPath = rootRegionPath + "/__pado/system";
		String indexRegionPath = rootRegionPath + "/__pado/index";
		String resultsRegionPath = rootRegionPath + "/__pado/results";
		String luceneRegionPath = rootRegionPath + "/__pado/lucene";

		Properties props = IndexMatrixUtil.getIndexMatrixProperties();
		props.setProperty(com.netcrest.pado.index.internal.Constants.PROP_REGION_SYSTEM, systemRegionPath);
		props.setProperty(com.netcrest.pado.index.internal.Constants.PROP_REGION_INDEX, indexRegionPath);
		props.setProperty(com.netcrest.pado.index.internal.Constants.PROP_REGION_RESULTS, resultsRegionPath);
		props.setProperty(com.netcrest.pado.index.internal.Constants.PROP_REGION_LUCENE, luceneRegionPath);

		// Create regions. Skip system, which already exists.
		Region padoRegion = rootRegion.getSubregion("__pado");

		// index
		Region indexRegion = cache.getRegion(indexRegionPath);
		if (indexRegion == null) {
			RegionFactory rf = cache.createRegionFactory();
			rf.setDataPolicy(DataPolicy.PARTITION);
			// 4 min idle timeout
			ExpirationAttributes idleTimeout = new ExpirationAttributes(240, ExpirationAction.DESTROY);
			rf.setEntryIdleTimeout(idleTimeout);
			indexRegion = rf.createSubregion(padoRegion, "index");
		}

		// results
		Region resultsRegion = cache.getRegion(resultsRegionPath);
		if (resultsRegion == null) {
			RegionFactory rf = cache.createRegionFactory();
			rf.setScope(Scope.LOCAL);
			// 5 min idle timeout
			ExpirationAttributes idleTimeout = new ExpirationAttributes(300, ExpirationAction.LOCAL_DESTROY);
			rf.setEntryIdleTimeout(idleTimeout);
			rf.addCacheListener(new ResultsCacheListenerImpl());
			resultsRegion = rf.createSubregion(padoRegion, "results");
		}

		// lucene - RAMDirectory
		Region luceneRegion = cache.getRegion(luceneRegionPath);
		if (luceneRegion == null) {
			RegionFactory rf = cache.createRegionFactory();
			rf.setScope(Scope.LOCAL);
			luceneRegion = rf.createSubregion(padoRegion, "lucene");
		}

		// Function services
		// IndexMatrixBuildFunction required by index server only
		IndexMatrixBuildFunction indexMatrixBuildFunction = new IndexMatrixBuildFunction();

		// EntitySearchCachetRetrieveBaseFunction & OQLEntitySearchFunction are
		// required by the grid that hosts data
		EntitySearchCacheRetrieveBaseFunction entitySearchCacheRetrieveBaseFunction = new EntitySearchCacheRetrieveBaseFunction();
		OQLEntitySearchFunction oqlEntitySearchFunction = new OQLEntitySearchFunction();
		ServerEntitySearchFunction serverEntitySearchFunction = new ServerEntitySearchFunction();

		// Now-relative temporal entries
		TemporalEntitySearchFunction temporalEntitySearchFunction = new TemporalEntitySearchFunction();

		// ITemporalBiz.getEntryResultSet() query
		TemporalEntrySearchFunction temporalEntrySearchFunction = new TemporalEntrySearchFunction();

		// Generic task function used for building Lucene index
		TaskFunction taskFunction = new TaskFunction();

		// Lucene and index-matrix based temporal data search functions
		LuceneTemporalQueryFunction luceneTemporalQueryFunction = new LuceneTemporalQueryFunction();
		LuceneTemporalSearchFunction luceneTemporalSearchFunction = new LuceneTemporalSearchFunction();

		// Register all the above functions required by IndexMatrix
		FunctionService.registerFunction(indexMatrixBuildFunction);
		FunctionService.registerFunction(entitySearchCacheRetrieveBaseFunction);
		FunctionService.registerFunction(oqlEntitySearchFunction);
		FunctionService.registerFunction(serverEntitySearchFunction);
		FunctionService.registerFunction(temporalEntitySearchFunction);
		FunctionService.registerFunction(temporalEntrySearchFunction);
		FunctionService.registerFunction(taskFunction);
		FunctionService.registerFunction(luceneTemporalQueryFunction);
		FunctionService.registerFunction(luceneTemporalSearchFunction);
	}

	private void initAppRegions(com.netcrest.pado.internal.config.dtd.generated.Pado padoConfig, String configFilePath)
	{
		com.netcrest.pado.internal.config.dtd.generated.PathList pathList = padoConfig.getPathList();
		if (pathList != null) {
			Region rootRegion = getRootRegion();
			PathListConfig pathListConfig = new PathListConfig(pathList);
			List<PathConfig> paths = pathListConfig.getPathConfig();
			for (PathConfig pathConfig : paths) {
				initAppRegion(pathConfig, rootRegion);
			}
		}
	}

	private void initAppRegion(PathConfig pathConfig, Region parentRegion)
	{
		if (parentRegion == null) {
			return;
		}
		String pathName = pathConfig.getName();
		String fullPath = parentRegion.getFullPath() + "/" + pathName;
		Region region = parentRegion.getSubregion(pathName);

		if (region == null) {

			Set<String> gridIdSet = pathConfig.getGrids();

			// If this grid is not allowed then return
			// If "grids" is null then the path is part of this grid.
			// If the grid ID is not in "grids" then the path is not part
			// of this grid.
			// boolean isNotReal = gridIdSet != null &&
			// gridIdSet.contains(this.gridId) == false;
			// if (isNotReal) {
			// return;
			// }

			String refid = pathConfig.getPath().getRefid();
			RegionFactory rf;
			PartitionAttributesFactory paf = null;

			if (refid == null) {

				// If refid is not defined, then create a basic region.
				// If temporal, create partitioned region
				// else create normal region.
				rf = cache.createRegionFactory();
				if (pathConfig.isTemporalEnabled()) {
					rf.setDataPolicy(DataPolicy.PARTITION);
					paf = new PartitionAttributesFactory();
					paf.setRedundantCopies(1);
					GemfireTemporalManager.addTemporalAttributes(fullPath, pathConfig.isLuceneEnabled(), rf, paf);
				} else {
					rf.setDataPolicy(DataPolicy.REPLICATE);
				}

			} else {

				// refid is defined then make sure first it is defined in
				// server.xml.
				RegionAttributes ra = cache.getRegionAttributes(refid);
				if (ra == null) {
					throw new PadoServerException(
							"Reference ID specified in pado.xml is undefined in the GemFire configuration file (server.xml): refid="
									+ refid);
				}
				PartitionAttributes pa = ra.getPartitionAttributes();
				if (pa != null) {
					paf = new PartitionAttributesFactory(pa);
				}

				// Create a region factory with the region
				// attributes defined in server.xml.
				rf = cache.createRegionFactory(ra);

				if (paf != null) {
					if (pathConfig.isTemporalEnabled()) {
						GemfireTemporalManager.addTemporalAttributes(fullPath, pathConfig.isLuceneEnabled(), rf, paf);
					}
					rf.setPartitionAttributes(paf.create());
				}
			}

			region = rf.createSubregion(parentRegion, pathConfig.getName());

			// If partitioned region then set composite key if defined
			if (paf != null) {
				CompositeKeyInfo ckInfo = pathConfig.getCompositeKeyInfo();
				if (ckInfo != null) {
					RegionUtil.setCompositeKeyInfoForIdentityKeyPartionResolver(fullPath, ckInfo);
					Logger.config("CompositeKeyInfo registered: path=" + fullPath + ", " + ckInfo);
				}
			}
		}

		List<PathConfig> paths = pathConfig.getPathConfig();
		for (PathConfig pathConfig2 : paths) {
			initAppRegion(pathConfig2, region);
		}
	}

	/**
	 * Returns a string with '/' replaced with '_'.
	 * 
	 * @param gridPath
	 *            Grid path
	 */
	public static String getQueueId(String gridPath)
	{
		return gridPath.replace('/', '_');
	}

	/**
	 * Adds TemporalCacheListenerImpl if the region does not have one
	 * registered.
	 * 
	 * @param region
	 *            Partitioned region
	 */
	private void addTemporalCacheListener(Region region)
	{
		if (region == null || region instanceof PartitionedRegion == false) {
			return;
		}
		CacheListener listeners[] = region.getAttributes().getCacheListeners();
		boolean temporalCacheListenerRegistered = false;
		for (CacheListener cacheListener : listeners) {
			temporalCacheListenerRegistered = cacheListener instanceof TemporalCacheListener;
			if (temporalCacheListenerRegistered) {
				break;
			}
		}
		if (temporalCacheListenerRegistered == false) {
			TemporalCacheListener temporalCacheListener = new TemporalCacheListener();
			Properties props = new Properties();
			props.setProperty("fullPath", region.getFullPath());
			temporalCacheListener.init(props); // use default values
			region.getAttributesMutator().addCacheListener(temporalCacheListener);
		}
	}

	/**
	 * Launches a dedicated thread to periodically broadcast its GridInfo to
	 * itself and to all parent grids. It publishes GridInfo in the
	 * "__pado/grid" region only if it is the master server.
	 */
	private void launchGridUpdaterThread(String configFilePath)
	{
		com.netcrest.pado.internal.config.dtd.generated.Gemfire gemfire = padoConfig.getGemfire();
		long updateDelay = Long.parseLong(gemfire.getGridInfoUpdateDelay());
		final long updateInterval = Long.parseLong(gemfire.getGridInfoUpdateInterval());
		Timer timer = new Timer("Pado-GemfirePadoServerManager GridRegion Updater", true);
		// Initialize MasterServerLock
		MasterServerLock.initialize(systemRegion);

		timer.schedule(new TimerTask() {
			public void run()
			{
				if (cache.isClosed()) {
					return;
				}
				try {

					// Refresh gridInfo
					gridInfo = createGridInfo();

					if (isMaster()) {

						// publish it to its own grid
						gridRegion.put(gridInfo.getGridId(), gridInfo);

						// publish it to parents
						Collection<Region> values = parentGridRegionMap.values();
						for (Region region : values) {
							// Region may have been destroyed by the parent
							// which has full rights to detach its children
							// at any time.
							if (region.isDestroyed() == false) {
								region.put(gridInfo.getGridId(), gridInfo);
							}
						}
					}

				} catch (Throwable th) {
					Logger.warning("Unable to report GridInfo due to " + th.getMessage(), th);
				}

			}
		}, updateDelay, updateInterval);
	}

	private void validateXml(File xmlFile) throws PadoException
	{
		// First validate it
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		SAXParser saxParser;
		try {
			saxParser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler() {
				@Override
				public void warning(SAXParseException e) throws SAXException
				{
					Logger.warning(createLogMessage("Waring:", e));
				}

				@Override
				public void error(SAXParseException e) throws SAXException
				{
					Logger.warning(createLogMessage("Error:", e));
					throw e;
				}

				@Override
				public void fatalError(SAXParseException e) throws SAXException
				{
					Logger.warning(createLogMessage("Fatal Error:", e));
					throw e;
				}

			};
			saxParser.parse(xmlFile, handler);
		} catch (ParserConfigurationException e) {
			throw new PadoException(e);
		} catch (SAXException e) {
			throw new PadoException(e);
		} catch (IOException e) {
			throw new PadoException(e);
		}
	}

	private String createLogMessage(String header, SAXParseException e)
	{
		StringBuffer buffer = new StringBuffer(100);
		buffer.append(header);
		buffer.append("\n");
		buffer.append("   PublicId: " + e.getPublicId() + "\n");
		buffer.append("   SystemId: " + e.getSystemId() + "\n");
		buffer.append("   Line Number: " + e.getLineNumber() + "\n");
		buffer.append("   Column Number: " + e.getColumnNumber() + "\n");
		buffer.append("   Message: " + e.getMessage() + "\n");
		return buffer.toString();
	}

	protected LoginInfo loginToParent() throws PadoLoginException
	{
		// Create parent __pado regions
		reinitRequired = false;
		initParents(padoConfig);
		if (reinitRequired) {
			throw new PadoLoginException();
		}

		initRouterRegion();

		LoginInfo loginInfo = null;
		if (isParent) {
			// do nothing for now
		} else {
			// login only if it is configured to connect to one or more parents
			if (parentPadoRegionMap.size() > 0) {
				String appId = PadoUtil.getProperty(Constants.PROP_SECURITY_PARENT_APPID, "sys");
				String domain = PadoUtil.getProperty(Constants.PROP_SECURITY_PARENT_DOMAIN, "netcrest");
				String username = PadoUtil.getProperty(Constants.PROP_SECURITY_PARENT_USERNAME, "grid");
				String pass = PadoUtil.getProperty(Constants.PROP_SECURITY_PARENT_PASS);
				String ep = "";
				if (pass != null) {
					try {
						ep = AESCipher.decryptUserTextToText(pass);
					} catch (Exception ex) {
						throw new PadoLoginException(
								"Parent grid login password decryption error. Please check security properties in pado.properties or system properties. "
										+ ex.getMessage() + " [appId=" + appId + ", domain=" + domain + ", username="
										+ username + "]",
								ex);
					}
				}

				Properties loginProps = null;
				boolean securityEnabled = PadoUtil.isProperty(Constants.PROP_SECURITY_ENABLED);
				if (securityEnabled) {
					try {
						RSACipher cipher = new RSACipher(false);
						loginProps = cipher.getSignature(RSACipher.createCredentialProperties());
					} catch (Exception ex) {
						throw new PadoLoginException(
								"Error occurred while retrieving digital signature. " + ex.getMessage(), ex);
					}
				}
				padoBiz.getBizContext().reset();
				padoBiz.getBizContext().getGridContextClient().setAdditionalArguments(loginProps);
				loginInfo = padoBiz.login(appId, domain, username, ep.toCharArray());
				padoBiz.getBizContext().reset();
			}
		}

		// Create gridBiz used for onMember (within this grid) communications
		BizManager<IGridBizLink> svcMgr = getSystemBizManager("com.netcrest.pado.biz.server.IGridBiz");
		this.gridBiz = svcMgr.newClientProxy();

		// Create parent and child IGridBiz
		// TODO: Token must be obtained for each grid. Use the parent token for
		// all for now.
		GemfireGridService gridService = (GemfireGridService) padoBiz.getBizContext().getGridService();
		if (gridService != null && loginInfo != null) {
			gridService.setToken(loginInfo.getToken());
		}
		initAllGridBiz(parentPadoRegionMap, parentGridBizMap);
		initAllGridBiz(childGridRegionMap, childGridBizMap);

		// Get the parent app region for initializing PadoClientManager
		// which is required for IBiz objects. Only 1 parent supported for now.
		// TODO: Support multiple parents
		Region parentAppRegion = null;
		for (Region region : parentAppRegionMap.values()) {
			parentAppRegion = region;
			break;
		}

		// Add the parent app region to PadoClientManager under the "sys"
		// app ID. The "sys" app ID is reserved for servicing all IBiz objects.
		GemfirePadoClientManager.getPadoClientManager().addAppRegion("sys", parentAppRegion);

		return loginInfo;
	}

	protected void initThisGrid()
	{
		// Register the GridInfo listener to listen on child heartbeats
		// Listen on child grid heartbeats. Receives GridInfo objects.
		gridRegion.getAttributesMutator().addCacheListener(new GridInfoCacheListenerImpl());

		// Initialize AppInfo objects. Initially, AppInfo objects do not contain
		// grid information, which is added as grids are individually polled.
		initAppInfos();

		// Register sys biz classes
		registerBiz();

		// Create the catalog for this grid
		try {
			catalog = new CatalogServerImpl(padoBiz);
		} catch (PadoLoginException e) {
			Logger.error(e);
		}
	}

	private void initAllGridBiz(Map<String, Region> regionMap, Map<String, IGridBizLink> gridBizMap)
	{
		GemfireGridService gridService = (GemfireGridService) padoBiz.getBizContext().getGridService();
		Set<Map.Entry<String, Region>> entrySet = regionMap.entrySet();
		for (Map.Entry<String, Region> entry : entrySet) {
			String gridId = entry.getKey();
			if (gridBizMap.containsKey(gridId) == false) {
				Pool pool = PoolManager.find(entry.getValue());
				BizManager<IGridBizLink> bizManager = getSystemBizManager(IGridBizLink.class);
				IGridBizLink gridBiz = ((GemfireBizManager<IGridBizLink>) bizManager).newClientProxy(pool, true);
				gridBizMap.put(gridId, gridBiz);
				gridService.putPool(gridId, pool);
				Logger.config("Registered gridId=" + gridId + ", poolName=" + pool.getName());
			}
		}
	}

	private void initGridBiz(GridInfo gridInfo, boolean isParent)
	{
		Map<String, Region> padoRegionMap;
		Map<String, IGridBizLink> gridBizMap;
		if (isParent) {
			padoRegionMap = parentPadoRegionMap;
			gridBizMap = parentGridBizMap;
		} else {
			padoRegionMap = childGridRegionMap;
			gridBizMap = childGridBizMap;
		}
		String gridId = gridInfo.getGridId();
		if (gridBizMap.containsKey(gridId) == false) {
			Region padoRegion = padoRegionMap.get(gridId);
			if (padoRegion != null) {
				Pool pool = PoolManager.find(padoRegion);
				BizManager<IGridBizLink> bizManager = getSystemBizManager("com.netcrest.pado.biz.server.IGridBiz");
				IGridBizLink gridBiz = ((GemfireBizManager<IGridBizLink>) bizManager).newClientProxy(pool, true);
				gridBizMap.put(gridId, gridBiz);
				GemfireGridService gridService = (GemfireGridService) padoBiz.getBizContext().getGridService();
				gridService.putPool(gridId, pool);
				Logger.config("Added grid to GridService: gridId=" + gridId + ", poolName=" + pool.getName());
			}
		}
	}

	private void removeGridBiz(GridInfo gridInfo, boolean isParent)
	{
		if (gridInfo == null || gridId.equals(gridInfo.getGridId())) {
			return;
		}
		Map<String, Region> padoRegionMap;
		Map<String, IGridBizLink> gridBizMap;
		if (isParent) {
			padoRegionMap = parentPadoRegionMap;
			gridBizMap = parentGridBizMap;
		} else {
			padoRegionMap = childGridRegionMap;
			gridBizMap = childGridBizMap;
		}
		String gridId = gridInfo.getGridId();
		IGridBizLink gridBiz = gridBizMap.remove(gridId);
		GemfireGridService gridService = (GemfireGridService) padoBiz.getBizContext().getGridService();
		Pool pool = gridService.getPool(gridId);
		String poolName = null;
		if (pool != null) {
			poolName = pool.getName();
		}
		gridService.remove(gridId);
		Logger.config("Removed grid from GridService: gridId=" + gridId + ", poolName=" + poolName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateGrid(GridInfo gridInfo, boolean isParent)
	{
		// if (gridInfo == null || gridId.equals(gridInfo.getGridId())) {
		// return;
		// }
		if (gridInfo == null) {
			return;
		}

		// Create child grid regions only if they do not exist
		createGridRegions(gridInfo, isParent);

		// Initialize GridBiz for this grid if it hasn't been done so already
		initGridBiz(gridInfo, isParent);

		// Update AppInfo and GridRouterInfo only if they are different from
		// GridInfo.
		updateAppInfo(gridInfo);
	}

	@Override
	public void attachToParentGrid(String parentGridId)
	{
		if (parentGridId == null || this.gridId.equals(parentGridId)) {
			return;
		}

		// Currently supports only one parent. Ignore parentGridId for now.
		initLogin();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeGrid(GridInfo gridInfo, boolean isParent)
	{
		// Do not self-destruct!
		if (gridInfo == null || gridId.equals(gridInfo.getGridId())) {
			return;
		}
		// Remove grid resources in reverse order to updateGrid()
		removeAppInfo(gridInfo);
		removeGridBiz(gridInfo, isParent);
		try {
			// removeGridRouterInfo() may raise an exception if gridInfo's grid
			// has been stopped. Ignore the exception in that case so
			// that the caller can continue.
			// Note that this call is really unnecessary since the
			// the gridInfo's grid regions will be destroyed. Only
			// benefit is updating the gridInfo's grid if it exists.
			removeGridRouterInfo(((GemfireGridInfo) gridInfo).getRootPathInfo(), gridInfo.getGridId());
		} catch (Exception ex) {
			// ignore
		}
		removeGridRegions(gridInfo, isParent);

		// publish grid down message to clients
		String gridType;
		if (isParent) {
			gridType = "parent";
		} else {
			gridType = "child";
		}
		GridStatusInfo gridStatusInfo = InfoFactory.getInfoFactory().createGridStatusInfo(Status.GRID_NOT_AVAILABLE,
				gridId, null, null,
				"The " + gridType + " grid, " + gridInfo.getGridId() + ", has been removed from the " + this.gridId
						+ " Grid servers for " + gridInfo.getGridId() + " are no longer reachable from " + this.gridId
						+ ".");
		putMessage(MessageType.GridStatus, gridStatusInfo);
		Logger.info(gridStatusInfo.getMessage());
	}

	/**
	 * Removes the specified grid from AppInfo.
	 * 
	 * @param gridId
	 *            Grid ID
	 */
	public void removeFromAppInfo(String gridId)
	{
		if (gridId == null) {
			return;
		}
		Collection<GemfireAppInfo> col = appRegion.values();
		for (GemfireAppInfo appInfo : col) {
			AppGridInfo agi = appInfo.removeAppGridInfo(gridId);
		}
	}

	protected void initParents()
	{
		if (reinitRequired) {
			Thread thread = new Thread(new Runnable() {
				public void run()
				{
					if (cache.isClosed()) {
						return;
					}
					while (reinitRequired) {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// ignore
						}
						reinit();
					}
					Logger.config(
							"Initialization complete. All parent/child grids are detected and properly initialized.");
				}
			}, "Pado-GemfirePadoServerManager.initParents");
			thread.setDaemon(true);
			thread.start();
		}

		// @dpark
		// // Create the catalog. This is done here due to nested call to
		// // PadoManager.
		// try {
		// // Get the parent router region for creating the catalog
		// // TODO: Even though multiple parents can be specified, the current
		// // implementation supports only one parent (the last one in the list)
		// // Note that this is different from this.routerRegion which is for
		// // this grid as Pado providing GridRouterInfo to clients.
		// // Region parentRouterRegion = null;
		// // for (Region region : padoManager.parentRouterRegionMap.values()) {
		// // parentRouterRegion = region;
		// // break;
		// // }
		// catalog = new CatalogServerImpl(padoManager.padoBiz);
		// } catch (LoginException ex) {
		// Logger.error("This grid may not be able to execute IBiz.", ex);
		// }
	}

	private void reinit()
	{
		reinitRequired = false;

		// Create child __pado regions (local via pool)
		initParents(padoConfig);

		if (reinitRequired == false) {
			// Create gridBiz used for onMember (within this grid)
			// communications
			BizManager<IGridBizLink> svcMgr = getSystemBizManager("com.netcrest.pado.biz.server.IGridBiz");
			this.gridBiz = svcMgr.newClientProxy();

			// Create parent and child IGridBiz
			initAllGridBiz(parentPadoRegionMap, parentGridBizMap);
			initAllGridBiz(childGridRegionMap, childGridBizMap);
		}

		// Update AppInfo objects with GridInfo retrieved from each child grid
		updateAppInfos();

		// Publish GridInfo to all parent grids
		publishGridInfoToParentGrids();

		if (isParent == false
				&& ((GemfireGridService) padoBiz.getBizContext().getGridService()).getPadoPool() == null) {
			// Get the parent router region for creating the catalog
			// TODO: Even though multiple parents can be specified, the current
			// implementation
			// supports only one parent (the last one in the list)
			// Note that this is different from this.routerRegion which is for
			// this grid as Pado providing GridRouterInfo to clients.
			//
			// Furthermore, if the parent is not running then this
			// parentRouterRegionMap
			// will be empty. In that case, GridService which depends on this
			// information needs
			// to be updated later when the parent eventually runs. This is done
			// in reinit().
			Region routerRegion = null;
			for (Region region : parentRouterRegionMap.values()) {
				routerRegion = region;
				break;
			}
			if (routerRegion != null) {
				Pool pool = PoolManager.find(routerRegion);
				((GemfireGridService) padoBiz.getBizContext().getGridService()).setPadoPool(pool);
				padoBiz.getBizContext().getGridService().refresh();
			}
		}

		// @dpark catalog already initialized?
		if (reinitRequired == false) {
			if (catalog == null) {
				try {
					catalog = new CatalogServerImpl(padoBiz);
				} catch (PadoLoginException ex) {
					Logger.error("This grid may not be able to execute IBiz.", ex);
				}
			} else {
				catalog.refresh();
			}
		}
	}

	private void initRouterRegion()
	{
		Region parentRouterRegion = null;
		Pool pool = null;
		if (isParent) {
			// This VM is the parent. Use its router region.
			parentRouterRegion = this.routerRegion;
		} else {
			// Get the parent router region for creating the catalog
			// TODO: Even though multiple parents can be specified, the current
			// implementation
			// supports only one parent (the last one in the list)
			// Note that this is different from this.routerRegion which is for
			// this grid as Pado providing GridRouterInfo to clients.
			//
			// Furthermore, if the parent is not running then this
			// parentRouterRegionMap
			// will be empty. In that case, GridService which depends on this
			// information needs
			// to be updated later when the parent eventually runs. This is done
			// in reinit().
			for (Region region : parentRouterRegionMap.values()) {
				parentRouterRegion = region;
				break;
			}
		}

		((GemfireGridService) padoBiz.getBizContext().getGridService()).setRouterRegion(parentRouterRegion);
	}

	private void registerBiz()
	{
		try {
			// Register system-level IBiz classes
			// IPadoBiz is for clients and grids. grids act as clients to
			// Pado.
			BizManager<IPadoBizLink> padoBizMgr = BizManagerFactory.getBizManagerFactory()
					.createBizManager("com.netcrest.pado.biz.server.IPadoBiz", false);

			// TODO: Handle multiple parents later.
			Properties credentials = null;
			if (PadoUtil.isProperty(Constants.PROP_SECURITY_ENABLED)) {
				try {
					credentials = RSACipher.createCredentialProperties("sys", "netcrest", null, null, null, null, null);
				} catch (Exception ex) {
					throw new PadoException(ex);
				}
			}
			GridService gridService = InternalFactory.getInternalFactory().createGridService(gridId, "sys",
					credentials, /* token */
					null, /* username */null, isParent);

			padoBizMgr.setGridService(gridService);
			padoBizMgr.init();
			addSystemBizMananger(padoBizMgr);
			padoBiz = padoBizMgr.newClientProxy();

			// IGridBiz is for grid-to-grid communications
			BizManager<IGridBizLink> gridBizMgr = BizManagerFactory.getBizManagerFactory()
					.createBizManager("com.netcrest.pado.biz.server.IGridBiz", false);
			gridBizMgr.setGridService(gridService);
			gridBizMgr.init();
			addSystemBizMananger(gridBizMgr);

			// Create gridBiz used for onMember (within this grid)
			// communications
			BizManager<IGridBizLink> svcMgr = getSystemBizManager("com.netcrest.pado.biz.server.IGridBiz");
			this.gridBiz = svcMgr.newClientProxy();
		} catch (BizException ex) {
			Logger.severe(ex);
		} catch (ClassNotFoundException ex) {
			Logger.severe(ex);
		}
	}

	/**
	 * Initializes the __pado and its sub-regions for the parents that this grid
	 * must communicate with. All regions created by this method is backed by a
	 * pool in client/server topology.
	 * 
	 * @return Returns false if padoConfig is null or has no parent info.
	 */
	private boolean initParents(com.netcrest.pado.internal.config.dtd.generated.Pado padoConfig)
	{
		String gridIds[] = null;
		Map<String, Region> padoRegionMap;
		Map<String, Region> loginRegionMap;
		Map<String, Region> appRegionMap;
		Map<String, Region> gridRegionMap;
		Map<String, Region> messageRegionMap;
		Map<String, Region> routerRegionMap;
		Map<String, Region> serverRegionMap;
		Map<String, IGridBizLink> gridBizMap;

		List<com.netcrest.pado.internal.config.dtd.generated.GemfireParent> gemfireParentList = padoConfig.getGemfire()
				.getGemfireParent();
		if (gemfireParentList == null || gemfireParentList.size() == 0) {
			return false;
		}
		padoRegionMap = parentPadoRegionMap;
		loginRegionMap = parentLoginRegionMap;
		appRegionMap = parentAppRegionMap;
		gridRegionMap = parentGridRegionMap;
		messageRegionMap = parentMessageRegionMap;
		routerRegionMap = parentRouterRegionMap;
		serverRegionMap = parentServerRegionMap;
		gridBizMap = parentGridBizMap;

		RegionFactory rf = cache.createRegionFactory();
		rf.setScope(Scope.LOCAL);
		for (com.netcrest.pado.internal.config.dtd.generated.GemfireParent gemfireParent : gemfireParentList) {
			String gridId = gemfireParent.getId();
			try {
				String rootPath = gemfireParent.getRootRegionPath();
				if (rootPath == null) {
					rootPath = "/" + gridId;
				} else if (rootPath.startsWith("/") == false) {
					rootPath = "/" + rootPath;
				}
				gridIdMap.put(rootPath, gridId);
				Region rootRegion = cache.getRegion(rootPath);
				if (rootRegion == null) {
					rf.setDataPolicy(DataPolicy.EMPTY);
					rootRegion = rf.create(GemfireGridUtil.getRootName(rootPath));
				}
				Region padoRegion = rootRegion.getSubregion("__pado");
				boolean isMultiuserAuthenticationEnabled = false; // false for
																	// grid-to-grid?
				String poolName = gemfireParent.getPoolName();
				Pool pool = null;
				if (poolName != null) {
					pool = PoolManager.find(poolName);
				}
				if (pool == null) {
					poolName = gemfireParent.getId() + "-pool";
					String locators = gemfireParent.getLocators();
					pool = GemfireGridUtil.getPool(poolName, locators, isMultiuserAuthenticationEnabled, false, true);
				}

				// TODO: Take care of padoBiz which logs in to the parent grid.
				// The parent pool
				// is set here as a hack.
				if (pool != null) {
					GemfireGridService gridService = (GemfireGridService) padoBiz.getBizContext().getGridService();
					gridService.setPadoPool(pool);
				}

				rf.setPoolName(poolName);

				if (padoRegion == null) {
					padoRegion = rf.createSubregion(rootRegion, "__pado");
				}

				Region loginRegion = padoRegion.getSubregion("login");
				Region gridRegion = padoRegion.getSubregion("grid");
				Region appRegion = padoRegion.getSubregion("app");
				Region messageRegion = padoRegion.getSubregion("message");
				Region routerRegion = padoRegion.getSubregion("router");
				Region serverRegion = padoRegion.getSubregion("server");
				if (loginRegion == null) {
					// loginRegion = padoRegion.createSubregion("login",
					// af.create());
					loginRegion = createLocalRegion("login", padoRegion, DataPolicy.EMPTY, poolName);
				}
				if (gridRegion == null) {
					// gridRegion = padoRegion.createSubregion("grid",
					// af.create());
					gridRegion = createLocalRegion("grid", padoRegion, DataPolicy.EMPTY, poolName);
					// gridRegion.put(this.gridId, gridInfo = createGridInfo());
				}
				if (appRegion == null) {
					appRegion = createLocalRegion("app", padoRegion, DataPolicy.NORMAL, poolName);
					appRegion.getAttributesMutator().addCacheListener(new AppInfoCacheListenerImpl());
				}
				appRegion.registerInterestRegex(".*");
				if (routerRegion == null) {
					routerRegion = createLocalRegion("router", padoRegion, DataPolicy.NORMAL, poolName);
				}
				routerRegion.registerInterest(".*");
				if (serverRegion == null) {
					serverRegion = createLocalRegion("server", padoRegion, DataPolicy.NORMAL, poolName);
				}
				serverRegion.registerInterest(".*");
				if (messageRegion == null) {
					messageRegion = createLocalRegion("message", padoRegion, DataPolicy.EMPTY, poolName);
				}
				messageRegion.registerInterest(".*");

				padoRegionMap.put(gridId, padoRegion);
				loginRegionMap.put(gridId, loginRegion);
				gridRegionMap.put(gridId, gridRegion);
				appRegionMap.put(gridId, appRegion);
				messageRegionMap.put(gridId, messageRegion);
				routerRegionMap.put(gridId, routerRegion);
				serverRegionMap.put(gridId, serverRegion);

				// Update the grid region
				// if (gridRegion != null) {
				// gridRegion.put(this.gridId, gridInfo = getGridInfo());
				// }

			} catch (Exception ex) {

				reinitRequired = true;
				// Logger.warning("Unable to connect to grid (gridId=" + gridId
				// + ", isParent=" + isParent
				// +
				// "). Initialization for this grid will be done peroidically
				// until succesful. "
				// +
				// "Client access to this grid will not be available until
				// successful initialization.",
				// ex);

			}
		}

		return true;
	}

	private Region createLocalRegion(String regionName, Region parentRegion, DataPolicy dataPolicy, String poolName)
	{
		Region region = parentRegion.getSubregion(regionName);
		if (region != null) {
			return region;
		}
		RegionFactory rf = cache.createRegionFactory();
		rf.setScope(Scope.LOCAL);
		rf.setDataPolicy(dataPolicy);
		rf.setPoolName(poolName);
		return rf.createSubregion(parentRegion, regionName);
	}

	public synchronized void createGridRegions(GridInfo gridInfo, boolean isParent)
	{
		if (gridInfo == null) {
			return;
		}

		if (containsGrid(gridInfo.getGridId())) {
			return;
		}

		GemfireGridInfo gemfireGridInfo = (GemfireGridInfo) gridInfo;

		Map<String, Region> rootRegionMap;
		Map<String, Region> padoRegionMap;
		Map<String, Region> loginRegionMap;
		Map<String, Region> appRegionMap;
		Map<String, Region> gridRegionMap;
		Map<String, Region> messageRegionMap;
		Map<String, Region> routerRegionMap;
		Map<String, Region> serverRegionMap;
		Map<String, IGridBizLink> gridBizMap;

		if (isParent) {
			rootRegionMap = parentRootRegionMap;
			padoRegionMap = parentPadoRegionMap;
			loginRegionMap = parentLoginRegionMap;
			appRegionMap = parentAppRegionMap;
			gridRegionMap = parentGridRegionMap;
			messageRegionMap = parentMessageRegionMap;
			routerRegionMap = parentRouterRegionMap;
			serverRegionMap = parentServerRegionMap;
			gridBizMap = parentGridBizMap;
		} else {
			rootRegionMap = childRootRegionMap;
			padoRegionMap = childPadoRegionMap;
			loginRegionMap = childLoginRegionMap;
			appRegionMap = childAppRegionMap;
			gridRegionMap = childGridRegionMap;
			messageRegionMap = childMessageRegionMap;
			routerRegionMap = childRouterRegionMap;
			serverRegionMap = childServerRegionMap;
			gridBizMap = childGridBizMap;
		}

		String gridId = gemfireGridInfo.getGridId();
		String rootPath = gemfireGridInfo.getGridRootPath();
		if (rootPath == null) {
			rootPath = "/" + gridId;
		} else if (rootPath.startsWith("/") == false) {
			rootPath = "/" + rootPath;
		}
		gridIdMap.put(rootPath, gridId);
		Region rootRegion = cache.getRegion(rootPath);
		if (rootRegion == null) {
			RegionFactory rf = cache.createRegionFactory();
			rf.setScope(Scope.LOCAL);
			rf.setDataPolicy(DataPolicy.EMPTY);
			rootRegion = rf.create(GemfireGridUtil.getRootName(rootPath));
		}
		rootRegionMap.put(gridId, rootRegion);

		Region padoRegion = rootRegion.getSubregion("__pado");
		// boolean isMultiuserAuthenticationEnabled =
		// props.getProperty("multiuserAuthenticationEnabled",
		// "false").equals("true");
		boolean isMultiuserAuthenticationEnabled = false; // false for
															// grid-to-grid?
		String poolName = gemfireGridInfo.getConnectionName();
		Pool pool = PoolManager.find(poolName);
		if (pool == null) {
			String locators = gemfireGridInfo.getLocators();
			pool = GemfireGridUtil.getPool(poolName, locators, isMultiuserAuthenticationEnabled, false, true);
		}

		// No pool for the parent "__pado" region
		if (padoRegion == null) {
			RegionFactory rf = cache.createRegionFactory();
			rf.setScope(Scope.LOCAL);
			rf.setPoolName(poolName);
			rf.setDataPolicy(DataPolicy.NORMAL);
			padoRegion = rf.createSubregion(rootRegion, "__pado");
		}

		Region loginRegion = padoRegion.getSubregion("login");
		Region gridRegion = padoRegion.getSubregion("grid");
		Region appRegion = padoRegion.getSubregion("app");
		Region messageRegion = padoRegion.getSubregion("message");
		Region routerRegion = padoRegion.getSubregion("router");
		Region serverRegion = padoRegion.getSubregion("server");
		if (loginRegion == null || loginRegion.isDestroyed()) {
			RegionFactory rf = cache.createRegionFactory();
			rf.setScope(Scope.LOCAL);
			rf.setPoolName(poolName);
			rf.setDataPolicy(DataPolicy.NORMAL);
			loginRegion = rf.createSubregion(padoRegion, "login");
		}
		if (gridRegion == null || gridRegion.isDestroyed()) {
			RegionFactory rf = cache.createRegionFactory();
			rf.setScope(Scope.LOCAL);
			rf.setPoolName(poolName);
			rf.setDataPolicy(DataPolicy.NORMAL);
			gridRegion = rf.createSubregion(padoRegion, "grid");
		}
		if (appRegion == null || appRegion.isDestroyed()) {
			RegionFactory rf = cache.createRegionFactory();
			rf.setScope(Scope.LOCAL);
			rf.setPoolName(poolName);
			rf.setDataPolicy(DataPolicy.NORMAL);
			appRegion = rf.createSubregion(padoRegion, "app");
			appRegion.registerInterestRegex(".*");
		}
		if (routerRegion == null || routerRegion.isDestroyed()) {
			RegionFactory rf = cache.createRegionFactory();
			rf.setScope(Scope.LOCAL);
			rf.setPoolName(poolName);
			rf.setDataPolicy(DataPolicy.NORMAL);
			routerRegion = rf.createSubregion(padoRegion, "router");
			routerRegion.registerInterest(".*");
		}
		if (serverRegion == null || serverRegion.isDestroyed()) {
			RegionFactory rf = cache.createRegionFactory();
			rf.setScope(Scope.LOCAL);
			rf.setPoolName(poolName);
			rf.setDataPolicy(DataPolicy.NORMAL);
			serverRegion = rf.createSubregion(padoRegion, "server");
			serverRegion.registerInterest(".*");
		}
		if (messageRegion == null || messageRegion.isDestroyed()) {
			RegionFactory rf = cache.createRegionFactory();
			rf.setScope(Scope.LOCAL);
			rf.setPoolName(poolName);
			rf.setDataPolicy(DataPolicy.NORMAL);
			messageRegion = rf.createSubregion(padoRegion, "message");
			messageRegion.registerInterest(".*");
		}

		padoRegionMap.put(gridId, padoRegion);
		loginRegionMap.put(gridId, loginRegion);
		gridRegionMap.put(gridId, gridRegion);
		appRegionMap.put(gridId, appRegion);
		messageRegionMap.put(gridId, messageRegion);
		routerRegionMap.put(gridId, routerRegion);
		serverRegionMap.put(gridId, serverRegion);

		if (isParent == false) {
			// Put all login info objects to the child's login region
			// so that authorization can be performed across all grids.
			if (isMaster()) {
				loginRegion.putAll(this.loginRegion);
			}
		}
	}

	private synchronized void removeGridRegions(GridInfo gridInfo, boolean isParent)
	{
		if (gridInfo == null || gridId.equals(gridInfo.getGridId())) {
			return;
		}

		if (containsGrid(gridInfo.getGridId())) {
			return;
		}

		Map<String, Region> padoRegionMap;
		Map<String, Region> loginRegionMap;
		Map<String, Region> appRegionMap;
		Map<String, Region> gridRegionMap;
		Map<String, Region> messageRegionMap;
		Map<String, Region> routerRegionMap;
		Map<String, Region> serverRegionMap;
		Map<String, IGridBizLink> gridBizMap;

		if (isParent) {
			padoRegionMap = parentPadoRegionMap;
			loginRegionMap = parentLoginRegionMap;
			appRegionMap = parentAppRegionMap;
			gridRegionMap = parentGridRegionMap;
			messageRegionMap = parentMessageRegionMap;
			routerRegionMap = parentRouterRegionMap;
			serverRegionMap = parentServerRegionMap;
			gridBizMap = parentGridBizMap;
		} else {
			padoRegionMap = childPadoRegionMap;
			loginRegionMap = childLoginRegionMap;
			appRegionMap = childAppRegionMap;
			gridRegionMap = childGridRegionMap;
			messageRegionMap = childMessageRegionMap;
			routerRegionMap = childRouterRegionMap;
			serverRegionMap = childServerRegionMap;
			gridBizMap = childGridBizMap;
		}

		String gridId = gridInfo.getGridId();

		// Clean up the bookkeeping maps
		padoRegionMap.remove(gridId);
		loginRegionMap.remove(gridId);
		appRegionMap.remove(gridId);
		gridRegionMap.remove(gridId);
		messageRegionMap.remove(gridId);
		routerRegionMap.remove(gridId);
		serverRegionMap.remove(gridId);
		gridBizMap.remove(gridId);

		// Destroy the grid's root region which destroys all
		// of its child regions.
		String rootPath = ((GemfireGridInfo) gridInfo).getGridRootPath();
		Region rootRegion = cache.getRegion(rootPath);
		if (rootRegion == null) {
			return;
		}

		// Destroy locally
		if (rootRegion.isDestroyed() == false) {
			Region padoRegion = rootRegion.getSubregion("__pado");
			if (padoRegion != null) {
				if (padoRegion.isDestroyed() == false) {
					Set<Region> subRegionSet = padoRegion.subregions(true);
					for (Region region : subRegionSet) {
						if (region.isDestroyed() == false) {
							region.localDestroyRegion();
						}
					}
				}
				Pool pool = PoolManager.find(padoRegion);
				padoRegion.localDestroyRegion();
				if (pool != null) {
					pool.destroy();
				}
			}
			// rootRegion.localDestroyRegion();
			rootRegion.destroyRegion();
		}
	}

	/**
	 * Returns true if the specified grid ID is currently managed by this grid.
	 * 
	 * @param gridId
	 */
	public boolean containsGrid(String gridId)
	{
		if (this.gridId.equals(gridId)) {
			return true;
		} else if (childGridBizMap.containsKey(gridId)) {
			return true;
		} else {
			return parentGridBizMap.containsKey(gridId);
		}
	}

	/**
	 * Initializes AppInfo objects. AppInfo includes GridRoutingTable.Grid
	 * objects.
	 */
	protected void initAppInfos()
	{
		// Register self under the app ID "sys"
		// Note that "sys" can be overwritten if defined in appGridIds.
		GemfireAppInfo sysAppInfo = new GemfireAppInfo();
		sysAppInfo.setAppId("sys");
		sysAppInfo.setPadoId(gridId);
		sysAppInfo.setDefaultGridId(gridId);
		sysAppInfo.addBizSet(getAllAppBizInfos());
		GridInfo gridInfo = gridRegion.get(gridId);
		if (gridInfo == null) {
			this.gridInfo = gridInfo = createGridInfo();
			gridRegion.put(gridId, gridInfo);
		}
		sysAppInfo.update(gridInfo);

		if (padoConfig == null) {
			return;
		}

		// Create a routing table for each app
		com.netcrest.pado.internal.config.dtd.generated.AppList appList = padoConfig.getAppList();
		if (appList != null) {
			List<com.netcrest.pado.internal.config.dtd.generated.App> apps = appList.getApp();
			if (apps != null) {
				GridRoutingTable.Grid selfGrid = new GridRoutingTable.Grid(getGridId(), getLocation(), (byte) 5);
				sysAppInfo.addAllowedGrid(selfGrid);
				for (com.netcrest.pado.internal.config.dtd.generated.App app : apps) {
					com.netcrest.pado.internal.config.dtd.generated.GridList gridList = app.getGridList();
					if (gridList != null) {
						List<com.netcrest.pado.internal.config.dtd.generated.Grid> grids = gridList.getGrid();

						GemfireAppInfo appInfo = new GemfireAppInfo();
						appInfo.setAppId(app.getId());
						appInfo.setPadoId(gridId);
						appInfo.addAllowedGrid(selfGrid);
						appInfo.addBizSet(getAllAppBizInfos());

						int i = 0;
						String firstGridId = null;
						for (com.netcrest.pado.internal.config.dtd.generated.Grid gridConfig : grids) {
							String val = gridConfig.getRoutingWeight();
							byte weight = 5;
							if (val != null) {
								weight = Byte.parseByte(val);
							}

							Grid grid = new Grid(gridConfig.getId(), weight);
							grid.setDescription(StringUtil.removeExtraSpaces(gridConfig.getDescription()));
							// pick the first one as the default grid id
							if (i == 0) {
								firstGridId = grid.getGridId();
							}
							i++;

							appInfo.addAllowedGrid(grid);

							// TODO: The following seems not necessary. The
							// parent no longer gets GridInfo from children.
							// Instead, it relies on children to report
							// GridInfo.
							gridInfo = gridRegion.get(grid.getGridId());
							if (gridInfo == null) {
								gridInfo = getChildGridInfo(grid.getGridId());
								if (gridInfo != null) {
									gridRegion.put(grid.getGridId(), gridInfo);
								}
							}

							// Update sysAppInfo. "sys" is responsible for
							// all grids.
							if (sysAppInfo.isAllowedGrid(grid.getGridId()) == false) {
								Grid sysAppGrid;
								if (appInfo.getAppId().equals("sys")) {
									sysAppGrid = grid;
								} else {
									sysAppGrid = new Grid(grid.getGridId(), getLocation(), (byte) 5);
								}
								sysAppInfo.addAllowedGrid(sysAppGrid);
							}
							sysAppInfo.update(gridInfo);
						}

						// Determine the default grid ID. If not defined
						// in grid-list then assign the first grid id found
						// in the list. If all else fails, then assign this
						// grid's id.
						String defaultGridId = gridList.getDefaultGridId();
						if (defaultGridId == null) {
							defaultGridId = firstGridId;
						}
						if (defaultGridId == null) {
							defaultGridId = getGridId();
						}
						defaultGridMap.put(app.getId(), defaultGridId);
						appInfo.setDefaultGridId(defaultGridId);

						// register self for every app
						gridInfo = gridRegion.get(gridId);
						if (gridInfo == null) {
							this.gridInfo = gridInfo = createGridInfo();
							gridRegion.put(gridId, gridInfo);
						}
						appInfo.update(gridInfo);
						appRegion.put(appInfo.getAppId(), appInfo);

						GridRoutingTable.initializeGridRoutingTable(appInfo.getAppId());
					}
				}

				appRegion.put(sysAppInfo.getAppId(), sysAppInfo);
				GridRoutingTable.initializeGridRoutingTable(sysAppInfo.getAppId());

			}
		}
	}

	public void updateGridRoutingTable(GridRoutingTable routingTable)
	{
		if (routingTable == null) {
			return;
		}
		String appId = routingTable.getAppId();
		GemfireAppInfo appInfo = appRegion.get(appId);
		GridRoutingTable.Grid selfGrid = new GridRoutingTable.Grid(getGridId(), getLocation(), (byte) 5);

		appInfo = new GemfireAppInfo();
		appInfo.setAppId(appId);
		appInfo.setPadoId(gridId);
		appInfo.addAllowedGrid(selfGrid);
		appInfo.addBizSet(getAllAppBizInfos());

		GemfireAppInfo sysAppInfo = appRegion.get("sys");

		Grid firstGrid = null;
		for (Grid grid : routingTable.getCostBasedTable().values()) {
			appInfo.addAllowedGrid(grid);

			if (appId.equals(sysAppInfo.getAppId()) == false) {
				// Update sysAppInfo. "sys" is responsible for
				// all grids.
				if (sysAppInfo.isAllowedGrid(grid.getGridId()) == false) {
					// Add a new Grid w/ default settings
					sysAppInfo.addAllowedGrid(new Grid(getGridId()));
				}
			}

			if (firstGrid == null) {
				firstGrid = grid;
			}
		}

		// Set the default grid
		// TODO: use the first grid in the list for now
		if (firstGrid != null) {
			String defaultGridId = firstGrid.getGridId();
			defaultGridMap.put(appId, defaultGridId);
			appInfo.setDefaultGridId(defaultGridId);
		}

		// Register self for the app
		GridInfo gridInfo = gridRegion.get(gridId);
		if (gridInfo == null) {
			this.gridInfo = gridInfo = createGridInfo();
			gridRegion.put(gridId, gridInfo);
		}
		appInfo.update(gridInfo);

		synchronized (appRegion) {
			// Finally, put the AppInfo object in the app region. This
			// effectively makes the changes permanent.
			appRegion.put(appId, appInfo);

			if (appId.equals(sysAppInfo.getAppId()) == false) {
				// Update the sys app also
				appRegion.put(sysAppInfo.getAppId(), sysAppInfo);
			}
		}
	}

	public static GemfirePadoServerManager getPadoServerManager()
	{
		return (GemfirePadoServerManager) PadoServerManager.getPadoServerManager();
	}

	public AppInfo getAppInfo(String appId)
	{
		return appRegion.get(appId);
	}

	/**
	 * Updates AppInfo in appRegion with GridInfo retrieved from each child
	 * grid. An app spans across one or more grids.
	 */
	public void updateAppInfos()
	{
		Collection<GridInfo> col = gridRegion.values();
		for (GridInfo gridInfo : col) {
			updateAppInfo(gridInfo);
		}
	}

	/**
	 * Updates AppInfo appRegion with the specified GridInfo object. An app
	 * spans across one or more grids.
	 * 
	 * @param gridInfo
	 *            GridInfo containing grid specific information.
	 */
	public void updateAppInfo(GridInfo gridInfo)
	{
		if (gridInfo == null) {
			return;
		}

		synchronized (appRegion) {
			// update all AppInfo objects that allow the grid
			Collection<GemfireAppInfo> col = appRegion.values();
			for (GemfireAppInfo appInfo : col) {
				if (appInfo.update(gridInfo)) {
					appRegion.put(appInfo.getAppId(), appInfo);
				}
			}
		}

		// update GridRouterInfos
		GemfireRegionInfo rootRegionInfo = (GemfireRegionInfo) gridInfo.getRootPathInfo();
		updateGridRouterInfo(rootRegionInfo, gridInfo.getGridId());

		// update GridRouterInfos based on PathConfig info
		// com.netcrest.pado.internal.config.dtd.generated.PathList pathList =
		// padoConfig.getPathList();
		// if (pathList != null) {
		// PathListConfig pathListConfig = new PathListConfig(pathList);
		// List<PathConfig> paths = pathListConfig.getPathConfig();
		// for (PathConfig pathConfig2 : paths) {
		// updateGridRouterInfo(pathConfig2, gridInfo.getGridId());
		// }
		// }
	}

	private void removeAppInfo(GridInfo gridInfo)
	{
		// Do NOT self-destruct!
		if (gridInfo == null || gridId.equals(gridInfo.getGridId())) {
			return;
		}
		synchronized (appRegion) {
			Collection<GemfireAppInfo> col = appRegion.values();
			for (GemfireAppInfo appInfo : col) {
				if (appInfo.remove(gridInfo)) {
					appRegion.put(appInfo.getAppId(), appInfo);
				}
			}
		}

		// remove GridRouterInfos
		GemfireRegionInfo rootRegionInfo = (GemfireRegionInfo) gridInfo.getRootPathInfo();
		try {
			// removeGridRouterInfo() may raise an exception if gridInfo's grid
			// has been stopped. Ignore the exception in that case so
			// that the caller can continue.
			removeGridRouterInfo(rootRegionInfo, gridInfo.getGridId());
		} catch (Exception ex) {
			// ignore
		}
	}

	private void removeAppInfo(String gridId)
	{
		// Do not self-destruct!
		if (gridId == null || this.gridId.equals(gridId)) {
			return;
		}
		synchronized (appRegion) {
			Collection<GemfireAppInfo> col = appRegion.values();
			for (GemfireAppInfo appInfo : col) {
				if (appInfo.remove(gridInfo)) {
					appRegion.put(appInfo.getAppId(), appInfo);
				}
			}
		}

		// remove GridRouterInfos
		GemfireRegionInfo rootRegionInfo = (GemfireRegionInfo) gridInfo.getRootPathInfo();
		removeGridRouterInfo(rootRegionInfo, gridInfo.getGridId());
	}

	private void updateGridRouterInfo(PathConfig pathConfig, String gridId)
	{
		Set<String> gridIdSet = pathConfig.getGrids();
		if (gridIdSet == null || gridIdSet.contains(gridId)) {
			boolean gridRouterChanged = false;
			String gridPath = pathConfig.getGridPath();
			// Update this grid's routing info which the client uses
			// to determine the live grids for the router.
			// if (this.gridId.equals(gridId) == false) {
			Region<String, GridRouterInfo> thisRouterRegion = getRouterRegion(this.gridId);
			GridRouterInfo thisRouterInfo = thisRouterRegion.get(gridPath);
			if (thisRouterInfo == null) {
				thisRouterInfo = InfoFactory.getInfoFactory().createGridRouterInfo(gridPath);
				GridPathInfo gridPathInfo = getGridPathInfo(gridPath);
				if (gridPathInfo != null) {
					String routerClassName = gridPathInfo.getRouterClassName();
					if (routerClassName == null || routerClassName.trim().length() == 0) {
						routerClassName = Constants.DEFAULT_CLASS_ROUTER;
					}

					try {
						Class clazz = Class.forName(routerClassName);
						thisRouterInfo.setGridRouter((IGridRouter) clazz.newInstance());
						thisRouterInfo.getGridRouter().setAllowedGridIdSet(gridPathInfo.getGridIdSet());
						gridRouterChanged = true;
					} catch (Exception ex) {
						Logger.error("Exception occurred while configuring IGridRouter for the grid path " + gridPath
								+ ". The default grid router will be used.", ex);
					}

				}
			}
			if (thisRouterInfo != null) {
				if (thisRouterInfo.getGridRouter().getGridIdSet().contains(gridId) == false
						&& thisRouterInfo.getGridRouter().getAllowedGridIdSet().contains(gridId)) {
					thisRouterInfo.addGridId(gridId);
					gridRouterChanged = true;
				}
			}

			if (gridRouterChanged) {
				thisRouterRegion.put(gridPath, thisRouterInfo);
			}
		}

		List<PathConfig> pathConfigList = pathConfig.getPathConfig();
		if (pathConfigList != null) {
			for (PathConfig pathConfig2 : pathConfigList) {
				updateGridRouterInfo(pathConfig2, gridId);
			}
		}
	}

	private void updateGridRouterInfo(GemfireRegionInfo regionInfo, String gridId)
	{
		if (regionInfo == null || regionInfo.isHidden(false)) {
			return;
		}
		String gridPath = regionInfo.getGridRelativePath();
		Region<String, GridRouterInfo> routerRegion = getRouterRegion(gridId);

		// if (regionInfo.isDataPolicyPartitionedRegion(false)) {
		if (regionInfo.isScopeLocalRegion(false) == false && regionInfo.isHidden(false) == false) {
			GridRouterInfo routerInfo = routerRegion.get(gridPath);
			if (routerInfo == null) {
				routerInfo = InfoFactory.getInfoFactory().createGridRouterInfo(gridPath);
			}

			boolean gridRouterChanged = false;
			GridPathInfo gridPathInfo = getGridPathInfo(gridPath);
			if (gridPathInfo != null) {
				String routerClassName = gridPathInfo.getRouterClassName();
				if (routerClassName == null || routerClassName.trim().length() == 0) {
					routerClassName = Constants.DEFAULT_CLASS_ROUTER;
				}
				if (routerClassName.equals(routerInfo.getGridRouter().getClass().getName()) == false) {
					try {
						Class clazz = Class.forName(routerClassName);
						routerInfo.setGridRouter((IGridRouter) clazz.newInstance());
						routerInfo.getGridRouter().setAllowedGridIdSet(gridPathInfo.getGridIdSet());
						gridRouterChanged = true;
					} catch (Exception ex) {
						Logger.error("Exception occurred while configuring IGridRouter for the grid path " + gridPath
								+ ". The default grid router will be used.", ex);
					}
				}
			}
			if (routerInfo.getGridIdSet().contains(gridId) == false) {
				if (routerInfo.getGridRouter().getAllowedGridIdSet().contains(gridId)) {
					routerInfo.addGridId(gridId);
					gridRouterChanged = true;
				}
			}

			// Update this grid's routing info which the client uses
			// to determine the live grids for the router.
			// if (this.gridId.equals(gridId) == false) {
			Region<String, GridRouterInfo> thisRouterRegion = getRouterRegion(this.gridId);
			GridRouterInfo thisRouterInfo = thisRouterRegion.get(gridPath);
			if (thisRouterInfo != null) {
				if (thisRouterInfo.getGridRouter().getGridIdSet().contains(gridId) == false
						&& thisRouterInfo.getGridRouter().getAllowedGridIdSet().contains(gridId)) {
					thisRouterInfo.addGridId(gridId);
					thisRouterRegion.put(gridPath, thisRouterInfo);
				}
			}
			// }

			if (gridRouterChanged) {
				routerRegion.put(gridPath, routerInfo);
			}
		}
		List<PathInfo> childList = regionInfo.getChildList();
		for (PathInfo child : childList) {
			GemfireRegionInfo childRegionInfo = (GemfireRegionInfo) child;
			updateGridRouterInfo(childRegionInfo, gridId);
		}
	}

	private synchronized void removeGridRouterInfo(PathInfo pathInfo, String gridId)
	{
		if (pathInfo == null) {
			return;
		}
		Region<String, GridRouterInfo> routerRegion = getRouterRegion(gridId);
		if (routerRegion == null || routerRegion.isDestroyed()) {
			return;
		}
		String gridPath = pathInfo.getGridRelativePath();

		// If the grid that hosts the path is down then the following
		// will throw an exception. Just ignore.
		try {
			GridRouterInfo routerInfo = routerRegion.get(gridPath);
			if (routerInfo != null) {
				if (routerInfo.getGridIdSet().remove(gridId)) {
					if (routerInfo.getGridIdSet().size() == 0) {
						routerRegion.remove(gridPath);
					} else {
						routerRegion.put(gridPath, routerInfo);
					}
				}
			}
		} catch (Exception ex) {
			// ignore
		}

		// Now, clean up the routing info kept in this grid.
		Region<String, GridRouterInfo> thisRouterRegion = getRouterRegion(this.gridId);
		GridRouterInfo thisRouterInfo = thisRouterRegion.get(gridPath);
		if (thisRouterInfo != null) {
			if (thisRouterInfo.getGridRouter().getGridIdSet().contains(gridId)) {
				thisRouterInfo.removeGridId(gridId);
				thisRouterRegion.put(gridPath, thisRouterInfo);
			}
		}

		List<PathInfo> childList = pathInfo.getChildList();
		for (PathInfo child : childList) {
			GemfireRegionInfo childRegionInfo = (GemfireRegionInfo) child;
			removeGridRouterInfo(childRegionInfo, gridId);
		}

	}

	public GridInfo createGridInfo()
	{
		Cache cache = CacheFactory.getAnyInstance();
		GemfireGridInfo gridInfo = new GemfireGridInfo();
		gridInfo.setGridId(getGridId());
		gridInfo.setLocation(getLocation());
		gridInfo.setGridRootPath(getRootRegion().getFullPath());
		gridInfo.setConnectionName(getConnectionName());
		gridInfo.setIndexMatrixConnectionName(getIndexMatrixConnectionName());
		gridInfo.setLocators(getLocators());
		gridInfo.setParentGridIds(getParentGridIds());
		gridInfo.setChildGridIds(getChildGridIds());
		gridInfo.setClientIndexMatrixConnectionName(getClientIndexMatrixConnectionName());
		gridInfo.setClientConnectionName(getClientConnectionName());
		gridInfo.setClientSharedConnectionName(getClientSharedConnectionName());
		gridInfo.setClientConnectionSingleHopEnabled(isClientConnectionSingleHopEnabled());
		gridInfo.setClientConnectionMultiuserAuthenticationEnabled(isClientConnectionMultiuserAuthenticationEnabled());
		gridInfo.setClientLocators(getClientLocators());
		gridInfo.setBizSet(getAllAppBizInfos());
		gridInfo.setCacheInfo(new GemfireCacheInfo(gridInfo.getGridId(), cache, gridInfo.getGridRootPath()));
		return gridInfo;
	}

	public GridInfo getParentGridInfo(String parentGridId)
	{
		Region<String, GridInfo> region = parentGridRegionMap.get(parentGridId);
		if (region == null) {
			return null;
		}
		return region.get(parentGridId);
	}

	public GridInfo getChildGridInfo(String gridId)
	{
		Region<String, GridInfo> region = childGridRegionMap.get(gridId);
		if (region == null) {
			return null;
		}
		return region.get(gridId);
	}

	public Region getRootRegion()
	{
		return rootRegion;
	}

	public Region getLoginRegion()
	{
		return loginRegion;
	}

	/**
	 * &lt;appId, AppInfo&gt;
	 * 
	 * @return
	 */
	public Region<String, GemfireAppInfo> getAppRegion()
	{
		return appRegion;
	}

	public static boolean isServer()
	{
		return padoServerManager != null && ((GemfirePadoServerManager) padoServerManager).getAppRegion() != null;
	}

	/**
	 * &lt;gridId, GridInfo&gt;
	 * 
	 * @return
	 */
	public Region<String, GridInfo> getGridRegion()
	{
		return gridRegion;
	}

	public String getConnectionName()
	{
		return connectionName;
	}

	public String getSharedConnectionName()
	{
		return sharedConnectionName;
	}

	public String getIndexMatrixConnectionName()
	{
		return indexMatrixConnectionName;
	}

	public String getClientIndexMatrixConnectionName()
	{
		return clientIndexMatrixConnectionName;
	}

	public String getClientConnectionName()
	{
		return clientConnectionName;
	}

	public String getClientSharedConnectionName()
	{
		return clientSharedConnectionName;
	}

	public boolean isClientConnectionSingleHopEnabled()
	{
		return clientConnectionSingleHopEnabled;
	}

	public boolean isClientConnectionMultiuserAuthenticationEnabled()
	{
		return clientConnectionMultiuserAuthenticationEnabled;
	}

	public String getClientLocators()
	{
		return clientLocators;
	}

	/**
	 * Not functional
	 * 
	 * @param gridId
	 * @param serverId
	 * @return
	 */
	public int getChildBucketId(String gridId, Object serverId)
	{
		Region region = childServerRegionMap.get(gridId);
		Integer bucketId = (Integer) region.get(serverId);
		if (bucketId == null) {
			// request refresh from all servers
		}
		return bucketId;
	}

	public Region getServerRegion()
	{
		return serverRegion;
	}

	public Region getSystemRegion()
	{
		return systemRegion;
	}

	public Region getServerRegion(String gridId)
	{
		if (gridId == null) {
			return null;
		}
		if (gridId.equals(gridInfo.getGridId())) {
			return this.serverRegion;
		}
		Region serverRegion = childServerRegionMap.get(gridId);
		if (serverRegion == null) {
			serverRegion = parentServerRegionMap.get(gridId);
		}
		return serverRegion;
	}

	public Region<String, GridInfo> getGridRegion(String gridId)
	{
		if (gridId == null) {
			return null;
		}
		if (gridId.equals(gridInfo.getGridId())) {
			return this.gridRegion;
		}
		Region<String, GridInfo> gridRegion = childGridRegionMap.get(gridId);
		if (gridRegion == null) {
			gridRegion = parentGridRegionMap.get(gridId);
		}
		return gridRegion;
	}

	public GridInfo getGridInfoForGridId(String gridId)
	{
		Region<String, GridInfo> gridRegion = getGridRegion(gridId);
		if (gridRegion == null) {
			return null;
		}
		return gridRegion.get(gridId);
	}

	public String getGridIdForRootPath(String rootPath)
	{
		return gridIdMap.get(rootPath);
	}

	public boolean isPadoPath(String fullPath)
	{
		return getGridIdForFullPath(fullPath) != null;
	}

	public String getGridIdForFullPath(String fullPath)
	{
		return getGridIdForRootPath(GemfireGridUtil.getRootPath(fullPath));
	}

	public GridInfo getGridInfoForRootPath(String rootPath)
	{
		return getGridInfoForGridId(getGridIdForRootPath(rootPath));
	}

	public GridInfo getGridInfoForFullPath(String fullPath)
	{
		return getGridInfoForGridId(getGridIdForFullPath(fullPath));
	}

	public void putMessage(MessageType messageType, Object message)
	{
		messageRegion.put(messageType.ordinal(), message);
	}

	public void putMessageParents(MessageType messageType, Object message)
	{
		for (Region parentRegion : parentMessageRegionMap.values()) {
			parentRegion.put(messageType.ordinal(), message);
		}
	}

	public void putMessageChildren(MessageType messageType, Object message)
	{
		for (Region parentRegion : childMessageRegionMap.values()) {
			parentRegion.put(messageType.ordinal(), message);
		}
	}

	public LoginInfo login(String appId, String domainName, String username, char[] password,
			IUserPrincipal userPrincipal)
	{
		Set<BizInfo> bizSet = getAllAppBizInfos();
		Object token = UUID.randomUUID().toString();
		GemfireLoginInfo loginInfo = (GemfireLoginInfo) InfoFactory.getInfoFactory().createLoginInfo(appId, domainName,
				username, token, bizSet);
		loginInfo.setGridId(getGridId());
		loginInfo.setGridRootRegionPath(getRootRegion().getFullPath());
		loginInfo.setConnectionName(getClientConnectionName());
		loginInfo.setSharedConnectionName(getClientSharedConnectionName());
		loginInfo.setLocators(getClientLocators());
		loginInfo.setSingleHopEnabled(isClientConnectionSingleHopEnabled());
		loginInfo.setMultiuserAuthenticationEnabled(isClientConnectionMultiuserAuthenticationEnabled());
		loginInfo.setChildGridIds(getChildGridIds());
		loginInfo.setUserPrincipal(userPrincipal);

		GemfireUserLoginInfo userLoginInfo = new GemfireUserLoginInfo(loginInfo);

		// Register it in the this grid's login region
		loginRegion.put(loginInfo.getToken(), loginInfo);

		// Register with all child grids
		Set<Map.Entry<String, Region>> entrySet = childLoginRegionMap.entrySet();
		for (Map.Entry<String, Region> entry : entrySet) {
			String childGridId = entry.getKey();
			Region childLoginRegion = entry.getValue();
			try {
				childLoginRegion.put(loginInfo.getToken(), loginInfo);
				// } catch (NoAvailableLocatorsException ex) {
				// // remove this child grid
				// removeChildGrid(childGridId);
				// } catch (NoAvailableServersException ex) {
				// // remove this child grid
				// removeChildGrid(childGridId);
			} catch (Exception ex) {
				// remove this child grid
				removeChildGrid(childGridId);
			}
		}
		return userLoginInfo;
	}

	private void removeParentGrid(String parentGridId)
	{
		Region rootRegion = childRootRegionMap.remove(parentGridId);
		childPadoRegionMap.remove(parentGridId);
		childAppRegionMap.remove(parentGridId);
		childGridRegionMap.remove(parentGridId);
		childLoginRegionMap.remove(parentGridId);
		childMessageRegionMap.remove(parentGridId);
		childRouterRegionMap.remove(parentGridId);
		childServerRegionMap.remove(parentGridId);

		if (rootRegion.isDestroyed() == false) {
			try {
				rootRegion.destroyRegion();
			} catch (Exception ex) {
				// ignore
			}
		}
	}

	private void removeChildGrid(String childGridId)
	{
		Region rootRegion = childRootRegionMap.remove(childGridId);
		childPadoRegionMap.remove(childGridId);
		childAppRegionMap.remove(childGridId);
		childGridRegionMap.remove(childGridId);
		childLoginRegionMap.remove(childGridId);
		childMessageRegionMap.remove(childGridId);
		childRouterRegionMap.remove(childGridId);
		childServerRegionMap.remove(childGridId);

		if (rootRegion != null) {
			if (rootRegion.isDestroyed() == false) {
				try {
					rootRegion.destroyRegion();
				} catch (Exception ex) {
					// ignore
				}
			}
		}
	}

	@Override
	public boolean isValidToken(Object token)
	{
		return getLoginInfo(token) != null;
	}

	@Override
	public IUserPrincipal getUserPrincipal(Object token)
	{
		LoginInfo loginInfo = loginRegion.get(token);
		if (loginInfo == null) {
			return null;
		}
		return loginInfo.getUserPrincipal();
	}

	@Override
	public LoginInfo getLoginInfo(Object token)
	{
		return loginRegion.get(token);
	}

	@Override
	public void removeUserSession(Object token)
	{
		if (token == null) {
			return;
		}

		// Remove the token from this grid's region
		loginRegion.remove(token);

		// Remove it from all child grids
		Set<Map.Entry<String, Region>> entrySet = childLoginRegionMap.entrySet();
		for (Map.Entry<String, Region> entry : entrySet) {
			try {
				Region childLoginRegion = entry.getValue();
				if (childLoginRegion != null) {
					childLoginRegion.remove(token);
				}
			} catch (Exception ex) {
				// ignore
			}
		}
	}

	@Override
	public String getUsername(Object token)
	{
		LoginInfo loginInfo = getLoginInfo(token);
		if (loginInfo == null) {
			return null;
		}
		return loginInfo.getUsername();
	}

	@Override
	public void setGridEnabled(String appId, String gridId, boolean enabled)
	{
		GemfireAppInfo appInfo = (GemfireAppInfo) getAppInfo(appId);
		if (appInfo == null) {
			return;
		}
		// return if the grid ID is invalid.
		if (appInfo.isValidGrid(gridId) == false) {
			return;
		}

		if (enabled == false) {
			appInfo.addDisallowedGrid(gridId);
		} else {
			appInfo.removeDisallowedGrid(gridId);
		}

		Region<String, GridRouterInfo> routerRegion = getRouterRegion(gridId);
		if (routerRegion != null) {
			Set<Map.Entry<String, GridRouterInfo>> entrySet = routerRegion.entrySet();
			if (enabled) {
				// enable only if the grid is disabled
				for (Map.Entry<String, GridRouterInfo> entry : entrySet) {
					String gridPath = entry.getKey();
					GridRouterInfo gri = entry.getValue();
					if (gri.isGridEnabled(gridId) == false) {
						gri.setGridEnabled(gridId, true);
						routerRegion.put(gridPath, gri);
					}
				}
			} else {
				// disable only if enabled
				for (Map.Entry<String, GridRouterInfo> entry : entrySet) {
					String gridPath = entry.getKey();
					GridRouterInfo gri = entry.getValue();
					if (gri.isGridEnabled(gridId)) {
						gri.setGridEnabled(gridId, false);
						routerRegion.put(gridPath, gri);
					}
				}
			}
		}
		appRegion.put(appId, appInfo);
	}

	@Override
	public synchronized void removeGrid(String gridId, boolean isParent)
	{
		// Do not self-destruct!
		if (gridId == null || this.gridId.equals(gridId)) {
			return;
		}
		Region<String, GridInfo> gridRegion;
		if (isParent) {
			gridRegion = parentGridRegionMap.get(gridId);
		} else {
			gridRegion = childGridRegionMap.get(gridId);
		}
		if (gridRegion == null) {
			return;
		}
		if (gridRegion.isDestroyed() == false) {
			GridInfo gridInfo = gridRegion.get(gridId);
			removeGrid(gridInfo, isParent);
		}
	}

	private Region<String, GridRouterInfo> getRouterRegion(String gridId)
	{
		if (this.gridId.equals(gridId)) {
			return routerRegion;
		}
		Region<String, GridRouterInfo> routerRegion = childRouterRegionMap.get(gridId);
		if (routerRegion != null) {
			return routerRegion;
		}
		return parentRouterRegionMap.get(gridId);
	}

	@Override
	public boolean isGridEnabled(String appId, String gridId)
	{
		return false;
	}

	public Region getVirtualPathRegion()
	{
		return vpRegion;
	}

	@Override
	public boolean isMaster()
	{
		return MasterServerLock.getMasterServerLock().isMasterEnabled();
	}

	@Override
	public void addMasterFailoverListener(final MasterFailoverListener listener)
	{
		MasterServerLock.getMasterServerLock().addMasterFailoverListener(listener);
	}

	@Override
	public void removeMasterFailoverListener(final MasterFailoverListener listener)
	{
		MasterServerLock.getMasterServerLock().removeMasterFailoverListener(listener);
	}

	@Override
	public int getServerCount()
	{
		return GemfireGridUtil.getDistributedMemberIds().length;
	}

	// TODO: This method returns this grid's server count. It should return the
	// server count of the specified grid.
	@Override
	public int getServerCount(String gridId)
	{
		return this.getServerCount();
	}

	// TODO: This method returns this grid's server IDs. It should return the
	// server count of the specified grid.
	@Override
	public Object[] getServerIds(String gridId)
	{
		return GemfireGridUtil.getDistributedMemberIds();
	}
}
