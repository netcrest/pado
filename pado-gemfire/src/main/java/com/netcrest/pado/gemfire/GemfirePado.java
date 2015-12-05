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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheClosedException;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.InterestPolicy;
import com.gemstone.gemfire.cache.InterestResultPolicy;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionExistsException;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.RegionService;
import com.gemstone.gemfire.cache.RegionShortcut;
import com.gemstone.gemfire.cache.Scope;
import com.gemstone.gemfire.cache.SubscriptionAttributes;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.client.PoolFactory;
import com.gemstone.gemfire.cache.client.PoolManager;
import com.gemstone.gemfire.internal.cache.GemFireCacheImpl;
import com.gemstone.gemfire.security.AuthenticationRequiredException;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.exception.BizException;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.gemfire.info.GemfireAppGridInfo;
import com.netcrest.pado.gemfire.info.GemfireConfigInfo;
import com.netcrest.pado.gemfire.info.GemfireRegionInfo;
import com.netcrest.pado.gemfire.info.GemfireUserLoginInfo;
import com.netcrest.pado.gemfire.util.GemfireGridUtil;
import com.netcrest.pado.index.internal.IndexMatrixUtil;
import com.netcrest.pado.info.AppInfo;
import com.netcrest.pado.info.ConfigInfo;
import com.netcrest.pado.info.LoginInfo;
import com.netcrest.pado.info.PathInfo;
import com.netcrest.pado.info.UserLoginInfo;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.factory.BizManagerFactory;
import com.netcrest.pado.internal.factory.InternalFactory;
import com.netcrest.pado.internal.impl.GridRoutingTable;
import com.netcrest.pado.internal.impl.PadoClientManager;
import com.netcrest.pado.internal.server.BizManager;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.io.ObjectSerializer;
import com.netcrest.pado.link.IPadoBizLink;
import com.netcrest.pado.security.RSACipher;

public class GemfirePado extends Pado
{
	private final static String DEFAULT_LOCATORS = "localhost:20000";

	private static boolean s_xmlConfigPerformed;
	private static boolean s_multiAppsEnabled = false;

	private static GemFireCacheImpl s_cache;
	private static String s_locators = DEFAULT_LOCATORS;
	private static Pool s_padoPool;
	private static Pool s_indexMatrixPool;

	private RegionService regionService;
	@SuppressWarnings("rawtypes")
	private Region rootRegion;
	@SuppressWarnings("rawtypes")
	private Region routerRegion;
	@SuppressWarnings("rawtypes")
	private Region messageRegion;
	private Region<Object, LoginInfo> loginRegion;
	@SuppressWarnings("rawtypes")
	private Region<String, KeyMap> vpRegion;

	private GemfirePado() throws PadoException, PadoLoginException
	{
		this(null, null, null, null, null, null, null);
	}

	private GemfirePado(String appId, String domain, String username, char[] password) throws PadoException,
			PadoLoginException
	{
		this(appId, domain, username, password, null, null, null);
	}

	@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
	private GemfirePado(String appId, String domain, String username, char[] password, String keystoreFilePath,
			String keystoreAlias, char[] keystorePassword) throws PadoException, PadoLoginException
	{
		Properties props = null;
		boolean securityEnabled = PadoUtil.isProperty(Constants.PROP_SECURITY_ENABLED);
		if (securityEnabled) {
			try {
				props = RSACipher.createCredentialProperties(appId, domain, username, password, keystoreFilePath,
						keystoreAlias, keystorePassword);
			} catch (Exception ex) {
				throw new PadoException(ex);
			}
			if (isMultiuserAuthenticationEnabled()) {
				regionService = s_cache.createAuthenticatedView(props, s_padoPool.getName());
			}
		}
		registerPadoBiz(appId, props, regionService, s_padoPool);

		// get LoginInfo
		// throws LoginException
		try {
			Properties loginProps = null;
			if (securityEnabled) {
				RSACipher cipher = new RSACipher(false);
				loginProps = cipher.getSignature(props);
			}
			padoBiz.getBizContext().reset();
			padoBiz.getBizContext().getGridContextClient().setAdditionalArguments(loginProps);
			this.loginInfo = (UserLoginInfo) padoBiz.login(appId, domain, username, password);
			padoBiz.getBizContext().reset();
		} catch (PadoLoginException ex) {
			throw ex;
		} catch (AuthenticationRequiredException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			if (ex.getCause() != null) {
				throw new PadoException("Login failed. " + ex.getCause().getMessage(), ex);
			} else {
				throw new PadoException("Login failed. " + ex.getMessage(), ex);
			}

		}

		// First, configure the client. This occurs only once per appId.
		// In other words, the first user configures the client app.
		if (isConfigInfoExist(appId) == false) {
			ConfigInfo configInfo = getConfigInfo(appId);
			if (configInfo == null) {
				throw new PadoLoginException("The specified app ID, " + appId + ", is not valid.");
			}

			// This code cannot be invoked more than once because
			// cache.loadCacheXml() fails
			// if pools are already defined. Either have VMware to remove that
			// limitation
			// or build our own XML configuration service.

			// Initialize regions by creating the required regions if they do
			// not exist
			Region rootRegion = initRegions();
			if (rootRegion != null) {
				Region appRegion = rootRegion.getSubregion("__pado/app");
				// PadoClientManager initialization is done by
				// PadoClientInitializer
				// configured in the XML loaded above. This means
				// PadoClientInitializer
				// defined in XML is mandatory.
				GemfirePadoClientManager manager = GemfirePadoClientManager.getPadoClientManager();
				manager.addAppRegion(appId, appRegion);
				s_clientManager = manager;
			}

			// Create all app pools first so that they exist before loading
			// the xml content
			if (createAppPools(appId) == false) {
				throw new PadoException("Inavalid app ID, " + appId + ". This app ID is not recognized by Pado.");
			}

			// Do XML configuration for only once until Pado is re-connected.
			// Also, do it first so that XML takes precedence over dynamic
			// creation of regions.
			// Initialize the client manager at this time with the con
			if (s_xmlConfigPerformed == false) {

				// Initialize PadoClientManager
				Properties padoUtilProps = PadoUtil.getPadoProperties();
				padoUtilProps.putAll(configInfo.getProperties());
				PadoClientManager.initialize();

				ConfigInfo xmlConfigInfo = padoBiz.getConfigInfo(appId, isMultiAppsEnabled(), true);
				if (xmlConfigInfo == null) {
					throw new PadoLoginException("Configuration file not available for the specified app ID " + appId
							+ ".");
				}
				String xml = xmlConfigInfo.getXmlContent();
				if (xml != null) {
					ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
					s_cache.getLogger().config(
							"Pado(): Initializing with cache.xml obtained from Pado: appId=" + appId + "\n" + xml);

					// load the cache xml content obtained from Pado
					s_cache.loadCacheXml(bais);

					try {
						bais.close();
					} catch (IOException ex) {
						throw new PadoException(ex);
					}

					// Reset padoPool
					s_padoPool = PoolManager.find(loginInfo.getConnectionName());
				}

				// Apply IObjectSerializer
				ObjectSerializer.addSerializerList(configInfo.getObjectSerializerList());

				// XML could be null. In that case, all of client configuration
				// is done dynamically.
				s_xmlConfigPerformed = true;

				// This needs to be invoked again because the cache cleared by
				// cache.loadCacheXml(bais)
				rootRegion = initRegions();
				if (rootRegion != null) {
					Region appRegion = rootRegion.getSubregion("__pado/app");
					// PadoClientManager initialization is done by
					// PadoClientInitializer
					// configured in the XML loaded above. This means
					// PadoClientInitializer
					// defined in XML is mandatory.
					GemfirePadoClientManager manager = GemfirePadoClientManager.getPadoClientManager();
					manager.addAppRegion(appId, appRegion);
					s_clientManager = manager;
				}

				// TODO: make this per app. Each app may have its own set of
				// KeyType classes.
				// Register all server registered KeyType classes.
				String keyTypeNames[] = padoBiz.getRegisteredMainKeyTypeNames();
				for (String keyTypeName : keyTypeNames) {
					KeyTypeManager.registerKeyType(keyTypeName);
				}
			}

			// Create all regions defined by each grid that is part of the app
			createAppRegions(appId);

			// create index region if it doesn't exist
			initIndexRegion(configInfo);
		} else {

			// Set region member variables
			initRegions();
		}

		AppInfo appInfo = getAppInfo(appId);
		if (appInfo == null) {
			throw new PadoLoginException("The specified app ID, " + appId + ", is not valid.");
		}

		catalog = new GemfireCatalogImpl(this, props, loginInfo, appInfo, regionService, s_padoPool, s_indexMatrixPool,
				routerRegion);

		PadoClientManager.getPadoClientManager().addPado(this);

		// Create GridRoutingTable for the app. This call creates the
		// routing table only if it doesn't already exist.
		GridRoutingTable grp = GridRoutingTable.initializeGridRoutingTable(appId);

		// Register LoginInfo interest
		loginRegion.registerInterest(loginInfo.getToken(), InterestResultPolicy.KEYS);
	}

	private AppInfo getAppInfo(String appId)
	{
		return s_clientManager.getAppInfo(appId);
	}

	protected synchronized ConfigInfo getConfigInfo(String appId)
	{
		ConfigInfo configInfo = super.getConfigInfo(appId);
		if (configInfo == null) {
			configInfo = padoBiz.getConfigInfo(appId);
			putConfigInfo(appId, configInfo);
		}
		return configInfo;
	}

	private static boolean isXMLConfigPerformed()
	{
		return s_xmlConfigPerformed;
	}

	public static boolean isMultiAppsEnabled()
	{
		return s_multiAppsEnabled;
	}

	/**
	 * Initializes all internal regions if they do not exist
	 * 
	 * @return Returns the pado root region
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Region initRegions()
	{
		GemfireUserLoginInfo loginInfo = (GemfireUserLoginInfo) this.loginInfo;
		if (loginInfo.getGridRootRegionPath() == null || loginInfo.getGridRootRegionPath().length() <= 1) {
			return null;
		}
		rootRegion = s_cache.getRegion(loginInfo.getGridRootRegionPath());
		Pool pool = GemfireGridUtil.getPool(loginInfo.getConnectionName(), loginInfo.getLocators(),
				loginInfo.isMultiuserAuthenticationEnabled(), loginInfo.isSingleHopEnabled(), true);
		if (pool.getMultiuserAuthentication()) {
			if (loginInfo.getSharedConnectionName() == null) {
				loginInfo.setSharedConnectionName(pool.getName() + "-shared");
			}
		}

		// shared pool must not have multi-user set due to its requirement of
		// non-empty regions
		Pool sharedPool = GemfireGridUtil.getPool(loginInfo.getSharedConnectionName(), loginInfo.getLocators(), false,
				loginInfo.isSingleHopEnabled(), true);

		try {
			RegionFactory rf = s_cache.createRegionFactory();
			rf.setScope(Scope.LOCAL);
			if (rootRegion == null) {
				rootRegion = rf.create(GemfireGridUtil.getRootName(loginInfo.getGridRootRegionPath()));
			}
			SubscriptionAttributes sa = new SubscriptionAttributes(InterestPolicy.ALL);
			rf.setSubscriptionAttributes(sa);
			Region padoRegion = rootRegion.getSubregion("__pado");
			if (padoRegion == null) {
				rf.setPoolName(sharedPool.getName());
				padoRegion = rf.createSubregion(rootRegion, "__pado");
			}
			Region appRegion = padoRegion.getSubregion("app");
			if (appRegion == null) {
				rf.setPoolName(sharedPool.getName());
				rf.addCacheListener(new AppInfoCacheListenerImpl());
				appRegion = rf.createSubregion(padoRegion, "app");
				appRegion.registerInterest(loginInfo.getAppId());
			}
			routerRegion = padoRegion.getSubregion("router");
			if (routerRegion == null) {
				rf = s_cache.createRegionFactory();
				rf.setScope(Scope.LOCAL);
				rf.setPoolName(sharedPool.getName());
				routerRegion = rf.createSubregion(padoRegion, "router");
				routerRegion.registerInterestRegex(".*");
			}
			messageRegion = padoRegion.getSubregion("message");
			if (messageRegion == null) {
				rf = s_cache.createRegionFactory();
				rf.setScope(Scope.LOCAL);
				// af.setDataPolicy(DataPolicy.EMPTY);
				rf.setPoolName(sharedPool.getName());
				rf.addCacheListener(new MessageCacheListenerImpl());
				messageRegion = rf.createSubregion(padoRegion, "message");
				messageRegion.registerInterestRegex(".*");
			}
			loginRegion = padoRegion.getSubregion("login");
			if (loginRegion == null) {
				rf = s_cache.createRegionFactory();
				rf.setScope(Scope.LOCAL);
				rf.setDataPolicy(DataPolicy.NORMAL);
				rf.setPoolName(sharedPool.getName());
				rf.addCacheListener(new LoginClientCacheListenerImpl());
				sa = new SubscriptionAttributes(InterestPolicy.ALL);
				rf.setSubscriptionAttributes(sa);
				loginRegion = rf.createSubregion(padoRegion, "login");
			}

			// VirtualPath region caches all virtual path definitions.
			vpRegion = padoRegion.getSubregion("vp");
			if (vpRegion == null) {
				rf = s_cache.createRegionFactory(RegionShortcut.LOCAL);
				rf.setPoolName(sharedPool.getName());
				vpRegion = rf.createSubregion(padoRegion, "vp");
				vpRegion.registerInterest(".*");
			}

		} catch (Exception ex) {
			throw new PadoException("Exception occurred while creating pado internal regions. [locators="
					+ loginInfo.getLocators() + ", poolName=" + loginInfo.getSharedConnectionName() + "]", ex);
		}
		return rootRegion;
	}

	/**
	 * Creates all regions including sub-regions required by the specified app
	 * ID, if they do not exist. All created regions are empty and have the
	 * local scope. The required regions are determined by each grid's root
	 * region's RegionInfo. Hidden and local scoped regions specified by
	 * RegionInfo are not created.
	 * 
	 * @param appId
	 *            The app ID.
	 * @return true if the specified app ID exists
	 */
	private boolean createAppRegions(String appId)
	{
		AppInfo appInfo = s_clientManager.getAppInfo(appId);
		if (appInfo == null) {
			return false;
		}
		String gridIds[] = appInfo.getGridIds();
		for (String gridId : gridIds) {
			GemfireAppGridInfo api = (GemfireAppGridInfo) appInfo.getAppGridInfo(gridId);
			GemfireRegionInfo regionInfo = (GemfireRegionInfo) api.getRootPathInfo();
			if (regionInfo.isHidden(false) || regionInfo.isScopeLocalRegion(false)) {
				continue;
			}
			String poolName = api.getClientConnectionName();
			Pool pool = PoolManager.find(poolName);
			if (pool == null && api.getClientLocators() != null) {
				pool = GemfireGridUtil.getPool(poolName, api.getClientLocators(),
						api.isClientConnectionMultiuserAuthenticationEnabled(),
						api.isClientConnectionSingleHopEnabled(), true);
			}
			if (pool != null) {
				createEmptyRegion(regionInfo, pool, true);
			}

			// Create the server region for querying data via IndexMatrix.
			createEmptyRegion(regionInfo.getFullPath() + "/__pado/server", pool);
		}
		return true;
	}

	/**
	 * Creates all grid pools required for the specified app ID.
	 * 
	 * @param appId
	 *            App ID
	 * @return true if the specified app ID exists
	 */
	private boolean createAppPools(String appId)
	{
		AppInfo appInfo = s_clientManager.getAppInfo(appId);
		if (appInfo == null) {
			return false;
		}
		String gridIds[] = appInfo.getGridIds();
		for (String gridId : gridIds) {
			GemfireAppGridInfo api = (GemfireAppGridInfo) appInfo.getAppGridInfo(gridId);
			GemfireRegionInfo regionInfo = (GemfireRegionInfo) api.getRootPathInfo();
			if (regionInfo.isHidden(false) || regionInfo.isScopeLocalRegion(false)) {
				continue;
			}
			String poolName = api.getClientConnectionName();
			Pool pool = PoolManager.find(poolName);
			if (pool == null && api.getClientLocators() != null) {
				pool = GemfireGridUtil.getPool(poolName, api.getClientLocators(),
						api.isClientConnectionMultiuserAuthenticationEnabled(),
						api.isClientConnectionSingleHopEnabled(), true);
			}
			String sharedPoolName = api.getClientSharedConnectionName();
			Pool sharedPool = PoolManager.find(sharedPoolName);
			if (sharedPool == null) {
				sharedPool = GemfireGridUtil.getPool(sharedPoolName, api.getClientLocators(), false,
						api.isClientConnectionSingleHopEnabled(), true);
			}
		}
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initIndexRegion(ConfigInfo configInfo)
	{
		String rootRegionPath = rootRegion.getFullPath();
		String systemRegionPath = rootRegionPath + "/__pado/system";
		String indexRegionPath = rootRegionPath + "/__pado/index";
		String resultsRegionPath = rootRegionPath + "/__pado/results";
		String luceneRegionPath = rootRegionPath + "/__pado/lucene";

		Properties props = IndexMatrixUtil.getIndexMatrixProperties();
		props.setProperty(com.netcrest.pado.index.internal.Constants.PROP_REGION_SYSTEM, systemRegionPath);
		props.setProperty(com.netcrest.pado.index.internal.Constants.PROP_REGION_INDEX, indexRegionPath);
		props.setProperty(com.netcrest.pado.index.internal.Constants.PROP_REGION_RESULTS, resultsRegionPath);
		props.setProperty(com.netcrest.pado.index.internal.Constants.PROP_REGION_LUCENE, luceneRegionPath);

		Region padoRegion = rootRegion.getSubregion("__pado");

		String poolName = configInfo.getClientIndexMatrixConnectionName();
		String locators = configInfo.getClientLocators();
		s_indexMatrixPool = GemfireGridUtil.getPool(poolName, locators,
				((GemfireConfigInfo) configInfo).isClientMultiuserAuthenticationEnabled(), false, true);
		RegionFactory rf = s_cache.createRegionFactory();
		rf.setScope(Scope.LOCAL);
		rf.setDataPolicy(DataPolicy.NORMAL);
		rf.setPoolName(s_indexMatrixPool.getName());
		SubscriptionAttributes sa = new SubscriptionAttributes(InterestPolicy.ALL);
		rf.setSubscriptionAttributes(sa);
		rf.createSubregion(padoRegion, "index");
	}

	/**
	 * Returns the index matrix pool.
	 */
	public static Pool getIndexMatrixPool()
	{
		return s_indexMatrixPool;
	}

	/**
	 * Creates an empty local region for the specified region path if it does
	 * not exist. Unlike
	 * {@link #createEmptyRegion(GemfireRegionInfo, Pool, boolean)}, this method
	 * creates any type of region including hidden and local.
	 * 
	 * @param regionPath
	 * @param pool
	 * @return Returns the region created or the existing region.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Region createEmptyRegion(String regionPath, Pool pool)
	{
		Region region = s_cache.getRegion(regionPath);
		if (region == null) {
			String split[] = regionPath.split("/");
			// ignore the first one which is always an empty string assuming the
			// full path begins with "/".
			if (split.length > 1) {
				String rootRegionName = split[1];
				Region rootRegion = s_cache.getRegion(rootRegionName);
				RegionFactory rf = s_cache.createRegionFactory();
				rf.setScope(Scope.LOCAL);
				rf.setDataPolicy(DataPolicy.EMPTY);
				rf.setPoolName(pool.getName());
				if (rootRegion == null) {
					rootRegion = rf.create(rootRegionName);
				}
				Region subRegion = rootRegion;
				for (int i = 2; i < split.length; i++) {
					if (subRegion.getSubregion(split[i]) == null) {
						// Handle race condition when creating region.
						try {
							subRegion = rf.createSubregion(subRegion, split[i]);
						} catch (RegionExistsException ex) {
							subRegion = subRegion.getSubregion(split[i]);
						}
					} else {
						subRegion = subRegion.getSubregion(split[i]);
					}
				}
				region = subRegion;
			}
		}
		return region;
	}

	/**
	 * Creates an empty local region for the specified RegionInfo if it does not
	 * exist. Region is not created if the specified RegionInfo is hidden or has
	 * the scope of local.
	 * 
	 * @param regionInfo
	 *            RegionInfo containing region information.
	 * @param connectionName
	 *            Pool that connects all regions created by this method call.
	 * @param recursive
	 *            true to create child regions, false to create only the region
	 *            specified by the passed in RegionInfo.
	 */
	@SuppressWarnings("rawtypes")
	private Region createEmptyRegion(GemfireRegionInfo regionInfo, Pool pool, boolean recursive)
	{
		if (regionInfo.isHidden(false) || regionInfo.isScopeLocalRegion(false)) {
			return null;
		}

		Region region = createEmptyRegion(regionInfo.getFullPath(), pool);

		// Region region = s_cache.getRegion(regionInfo.getFullPath());
		// if (region == null) {
		// String split[] = regionInfo.getFullPath().split("/");
		// // ignore the first one which is always an empty string assuming the
		// // full path begins with "/".
		// if (split.length > 1) {
		// String rootRegionName = split[1];
		// Region rootRegion = s_cache.getRegion(rootRegionName);
		// AttributesFactory af = new AttributesFactory();
		// af.setScope(Scope.LOCAL);
		// af.setDataPolicy(DataPolicy.EMPTY);
		// af.setPoolName(pool.getName());
		// if (rootRegion == null) {
		// rootRegion = s_cache.createRegion(rootRegionName, af.create());
		// }
		// Region subRegion = rootRegion;
		// for (int i = 2; i < split.length; i++) {
		// subRegion = subRegion.createSubregion(split[i], af.create());
		// }
		// region = subRegion;
		// }
		// }

		if (recursive) {
			List<PathInfo> childList = regionInfo.getChildList();
			for (PathInfo pathInfo : childList) {
				GemfireRegionInfo regionInfo2 = (GemfireRegionInfo) pathInfo;
				createEmptyRegion(regionInfo2, pool, recursive);
			}
		}
		return region;
	}

	public static IPado login(String appId, String domain, String username, char[] password) throws PadoException,
			PadoLoginException
	{
		if (s_cache == null || s_cache.isClosed()) {
			throw new PadoLoginException(
					"Pado is not connected. You must first invoke Pado.connect() before invoking login().");
		}

		return login(appId, domain, username, password, null, null, null);
	}

	public static IPado login(String appId, String domain, String username, char[] password, String keystoreFilePath,
			String keystoreAlias, char[] keystorePassword) throws PadoException, PadoLoginException
	{
		IPado pado = getPado(username);
		if (pado == null) {
			pado = new GemfirePado(appId, domain, username, password, keystoreFilePath, keystoreAlias, keystorePassword);
			putPado(username, pado);
		}
		return pado;
	}

	public static String getLocators()
	{
		if (s_padoPool == null) {
			return null;
		}

		return s_locators;
	}

	/**
	 * Connects to the pado specified by locators for a client that hosts a
	 * single application, i.e., a single app ID throughout life of client. This
	 * call is analogous to passing false to multiAppsEnabled to
	 * {@link #connect(String, boolean, boolean)}
	 * 
	 * @param locators
	 *            Pado locators. Format:
	 *            host1:port1@server-group,host2:port2@server-group,...
	 * @param multiuserAuthenticationEnabled
	 *            true to enable multi-user authentication.
	 */
	public static void connect(String locators, boolean multiuserAuthenticationEnabled)
	{
		connect(locators, multiuserAuthenticationEnabled, false);
	}

	/**
	 * Connects to the pado specified by locators.
	 * 
	 * @param locators
	 *            Pado locators. Format:
	 *            host1:port1@server-group,host2:port2@server-group,...
	 * @param multiuserAuthenticationEnabled
	 *            true to enable multi-user authentication. Note that if the
	 *            Pado property "security-enabled" is false, then this parameter
	 *            has no effect.
	 * @param multiAppsEnabled
	 *            If true then the client is expected to host multiple
	 *            applications, i.e., multiple app IDs. Clients such as app
	 *            servers that host many applications typically set this to true
	 *            to allow multiple applications to connect to Pado.
	 */
	public static void connect(String locators, boolean multiuserAuthenticationEnabled, boolean multiAppsEnabled)
	{
		try {
			s_cache = (GemFireCacheImpl) CacheFactory.getAnyInstance();
			if (s_cache != null && s_cache.isClosed() == false) {
				if (getLocators() == null) {
					throw new PadoException(
							"Already connected. You must first close the existing connection before making a new connection.");
				} else {
					throw new PadoException("Already connected to " + getLocators()
							+ ". You must first close the existing connection before making a new connection.");
				}
			}

		} catch (CacheClosedException ex) {
			// fall thru to create a new instance of cache
		}

		try {

			// Clear all data structures
			reset();

			CacheFactory cacheFactory = new CacheFactory();
			cacheFactory.set("mcast-port", "0");
			s_cache = (GemFireCacheImpl) cacheFactory.create();

			// Default is "localhost:20000"
			String host = "localhost";
			int port = 20000;
			String serverGroup = null;

			if (locators == null) {
				locators = DEFAULT_LOCATORS;
			}

			s_padoPool = PoolManager.find("pado");

			if (s_padoPool == null) {
				PoolFactory poolFactory = PoolManager.createFactory();

				String split[] = locators.split(",");
				for (String locator : split) {
					String[] split2 = locator.split(":");
					if (split2.length > 0) {
						host = split2[0].trim();
					}
					if (split2.length > 1) {
						String[] portPart = split2[1].split("@");
						if (portPart.length > 0) {
							port = Integer.parseInt(portPart[0].trim());
						}
						if (portPart.length > 1) {
							serverGroup = portPart[1].trim();
						}
					}
					poolFactory.addLocator(host, port);
					poolFactory.setServerGroup(serverGroup);
					poolFactory.setReadTimeout(30000);
				}
				if (PadoUtil.isProperty(Constants.PROP_SECURITY_ENABLED) == false) {
					multiuserAuthenticationEnabled = false;
				}
				poolFactory.setMultiuserAuthentication(multiuserAuthenticationEnabled);
				s_padoPool = poolFactory.create("pado");
			}

			GemfirePado.s_locators = locators;
			GemfirePado.s_multiAppsEnabled = multiAppsEnabled;

		} catch (Exception ex) {

			throw new PadoException(ex);

		}
	}

	@SuppressWarnings("unchecked")
	private void registerPadoBiz(String appId, Properties credentials, RegionService regionService, Pool padoPool)
	{
		// IPadoBiz is accessible by all clients. It does not belong
		// in the catalog. Make it a permanent fixture.
		try {
			BizManager<IPadoBizLink> bizManager = BizManagerFactory.getBizManagerFactory().createBizManager("com.netcrest.pado.biz.server.IPadoBiz", true);
			bizManager.setAppId(appId);
			GemfireGridService gridService = (GemfireGridService) InternalFactory.getInternalFactory().createGridService(
					null, appId, credentials, null, null, false);
			gridService.setPadoPool(padoPool);
			gridService.setRouterRegion(routerRegion);
			gridService.setPadoRegionService(regionService);
			gridService.refresh();
			bizManager.setGridService(gridService);
			bizManager.init();
			padoBiz = bizManager.newClientProxy();
		} catch (BizException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void close()
	{
		try {
			reset();

			Cache cache = CacheFactory.getAnyInstance();
			if (cache != null && cache.isClosed() == false) {
				cache.close();
			}
		} catch (CacheClosedException ex) {
			throw new PadoException(ex);
		}
	}

	public static boolean isClosed()
	{
		try {
			Cache cache = CacheFactory.getAnyInstance();
			return cache == null || cache.isClosed();
		} catch (CacheClosedException ex) {
			return true;
		}
	}

	@Override
	public boolean isLoggedOut()
	{
		if (loginInfo == null) {
			return true;
		}
		Object token = loginInfo.getToken();
		if (token == null) {
			return false;
		}
		LoginInfo loginInfo = (LoginInfo) loginRegion.get(token);
		return loginInfo == null;
	}
	
	@Override
	public void resetSessionIdleTimeout()
	{
		loginRegion.get(getToken());
	}

	public void refresh()
	{
		if (catalog != null) {
			// Re-initialize pools and regions in case new information (such
			// as new grid) is received.
			createAppPools(catalog.getAppId());
			createAppRegions(catalog.getAppId());
			catalog.refresh();
		}
	}

	private static boolean isMultiuserAuthenticationEnabled()
	{
		return s_padoPool.getMultiuserAuthentication();
	}

	protected void resetUser()
	{
		if (isClosed() == false && loginRegion != null && loginRegion.isDestroyed() == false) {
			Object[] tokens = s_tokenMap.keySet().toArray();
			for (Object token : tokens) {
				loginRegion.unregisterInterest(token);
			}
		}
		logout();
	}

	protected static void reset()
	{
		Pado.reset();
		s_xmlConfigPerformed = false;
	}

	@SuppressWarnings("rawtypes")
	public KeyMap getVirtualPathDefinition(String virtualPath)
	{
		return vpRegion.get(virtualPath);
	}

	@SuppressWarnings("rawtypes")
	public void registerVirtualPathDefinition(KeyMap virtualPathDefinition)
	{
		if (virtualPathDefinition == null) {
			return;
		}
		String virtualPath = (String) virtualPathDefinition.get("VirtualPath");
		if (virtualPath == null) {
			return;
		}
		vpRegion.put(virtualPath, virtualPathDefinition);
	}

	public void removeVirtualPathDefinition(String virtualPath)
	{
		vpRegion.remove(virtualPath);
	}

	/**
	 * Returns a sorted array of all server registered virtual paths.
	 */
	public String[] getAllVirtualPaths()
	{
		Set<String> keySet = vpRegion.keySetOnServer();
		List<String> list = new ArrayList<String>(keySet);
		Collections.sort(list);
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Returns a map of all server-registered (virtual path, virtual definition)
	 * paired entries.
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, KeyMap> getAllVirtualPathDefinitions()
	{
		Set<String> keySet = vpRegion.keySetOnServer();
		Map<String, KeyMap> map = vpRegion.getAll(keySet);
		return map;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isVirtualPath(String virtualPath)
	{
		if (virtualPath == null) {
			return false;
		}
		return vpRegion.containsKeyOnServer(virtualPath);
	}
}
