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
package com.netcrest.pado.server;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.lucene.search.BooleanQuery;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.IUserPrincipal;
import com.netcrest.pado.exception.ConfigurationException;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.info.AppInfo;
import com.netcrest.pado.info.BizInfo;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.GridPathInfo;
import com.netcrest.pado.info.LoginInfo;
import com.netcrest.pado.info.message.MessageType;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.biz.util.BizUtil;
import com.netcrest.pado.internal.config.dtd.ConfigUtil;
import com.netcrest.pado.internal.config.dtd.PathConfig;
import com.netcrest.pado.internal.config.dtd.PathListConfig;
import com.netcrest.pado.internal.factory.BizManagerFactory;
import com.netcrest.pado.internal.factory.InfoFactory;
import com.netcrest.pado.internal.impl.GridRoutingTable;
import com.netcrest.pado.internal.l.PadoDisclaimer;
import com.netcrest.pado.internal.server.BizManager;
import com.netcrest.pado.internal.server.impl.CatalogServerImpl;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.internal.util.StringUtil;
import com.netcrest.pado.link.IGridBizLink;
import com.netcrest.pado.link.IPadoBizLink;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.temporal.TemporalManager;

/**
 * PadoServerManager manages all of server-side activities as follows:
 * <p>
 * <ul>
 * <li>Initializes itself by reading the "pado.xml" configuration file as
 * follows:</li>
 * <ul>
 * <li>Constructs valid apps and manages their info in the form of AppInfo
 * objects</li>
 * <li>Constructs allowed grids per app</li>
 * <li>Constructs IBiz catalog per app</li>
 * <li>Constructs grid routers per app</li>
 * </ul>
 * 
 * <li>Initializes connections with all parent and child grids which involves
 * the following steps:</li>
 * <ul>
 * <li>Creates live-list of grids that it are able to connect</li>
 * <li>Creates dead-list of grids that it are unable to connect. The dead-list
 * is retried periodically until it is able to establish connections.
 * Successfully connected grids are moved to the live-list</li>
 * </ul>
 * 
 * <li>Organizes parent and child grid communications</li>
 * <li>Publishes child-to-parent heart beats in the form of {@link GridInfo}
 * </li>
 * <li>Listens on child grid heart beats. Upon receiving a heart beat the
 * derived class</li>
 * 
 * <ul>
 * <li>Updates grid paths</li>
 * <li>Initializes {@link IBiz} classes</li>
 * <li>Updates {@link AppInfo}</li>
 * </ul>
 * </ul>
 * 
 * @author dpark
 * 
 */
public abstract class PadoServerManager
{
	/**
	 * PadoServerManager singleton instance
	 */
	protected static PadoServerManager padoServerManager;

	static {
		// Default properties
		if (PadoUtil.getProperty(Constants.PROP_SECURITY_AES_USER_CERTIFICATE) == null) {
			PadoUtil.getPadoProperties().setProperty(Constants.PROP_SECURITY_AES_USER_CERTIFICATE,
					"../../security/user.cer");
		}
	}

	/**
	 * true if the PadoServerManager has been fully initialized and started
	 */
	private volatile boolean padoServerManagerStarted = false;

	/**
	 * Default locators for grids.
	 */
	protected String locators;

	/**
	 * Grid ID
	 */
	protected String gridId;

	/**
	 * Site ID
	 */
	protected String siteId;

	/**
	 * Server ID - Always unique
	 */
	protected String serverId;

	/**
	 * Server number (with leading zero)
	 */
	protected String serverNum;

	/**
	 * Server Name - human legible and unique
	 */
	protected String serverName;

	/**
	 * Grid location
	 */
	protected String location;

	/**
	 * true if parent, false otherwise. Note that a child grid can be also a
	 * parent to other grids.
	 */
	protected boolean isParent;

	/**
	 * true if encryption is enabled.
	 */
	protected boolean isEncryptionEnabled = PadoUtil.isProperty(Constants.PROP_SECURITY_ENCRYPTION_ENABLE);

	/**
	 * Grid ID map containing &lt;rootPath, gridId&gt; pairs
	 */
	protected Map<String, String> gridIdMap = new HashMap<String, String>(10);

	/**
	 * This grid's GridInfo. This should be updated by calling //
	 * createGridInfo()
	 */
	protected GridInfo gridInfo;

	// TODO: replace the delayed initialization mechanism.
	/**
	 * Transit list of app grid Ids assigned during the initialization phase for
	 * delayed initialization.
	 */
	protected String appGridIds;

	/**
	 * Grid biz object for interacting with grids
	 */
	protected IGridBizLink gridBiz;

	/**
	 * Parent grid biz map containing &lt;gridId, IGridBiz&gt; pairs
	 */
	protected final Map<String, IGridBizLink> parentGridBizMap = new TreeMap<String, IGridBizLink>();

	/**
	 * Child grid biz map containing &lt;gridId, IGridBizLink&gt; pairs
	 */
	protected final Map<String, IGridBizLink> childGridBizMap = new TreeMap<String, IGridBizLink>();

	/**
	 * <gridPath, GridPathInfo>
	 */
	protected ConcurrentHashMap<String, GridPathInfo> gridPathInfoMap = new ConcurrentHashMap<String, GridPathInfo>(40);

	/**
	 * System map containing system-level IBiz classes, i.e., &lt;IBiz class
	 * name, Biz Manager&gt;. System-level IBiz classes requires "sys" level
	 * permissions to access.
	 * 
	 * 
	 */
	@SuppressWarnings("rawtypes")
	protected TreeMap<String, BizManager> systemMap = new TreeMap<String, BizManager>();

	/**
	 * App map containing app-level IBiz classes, i.e., &lt;IBiz class name,
	 * BizManager&gt; pairs.
	 */
	@SuppressWarnings("rawtypes")
	protected static TreeMap<String, BizManager> appMap = new TreeMap<String, BizManager>();

	/**
	 * Default grid map containing &lt;appId, gridId&gt; pairs.
	 */
	protected Map<String, String> defaultGridMap = new HashMap<String, String>(4);

	/**
	 * padoConfig contains Pado config info retrieved from the pado config file
	 * (pado.xml) via JAXB.
	 */
	protected static com.netcrest.pado.internal.config.dtd.generated.Pado padoConfig;

	/**
	 * true if re-initialization is required.
	 */
	protected boolean reinitRequired = false;

	/**
	 * Catalog of app IBiz classes.
	 */
	protected CatalogServerImpl catalog;

	/**
	 * Pado biz object for performing grid-to-grid authentication
	 */
	protected IPadoBizLink padoBiz;

	/**
	 * &lt;token, IUserPrincipal&gt; - Contains IUserPrincipal objects mapped by
	 * session tokens. An IUserPrincipal object is added to this map upon
	 * successful login via
	 * {@link IPadoBizLink#login(String, String, String, char[])} and removed
	 * upon logout or session idle timeout.
	 */
	protected Map<Object, IUserPrincipal> userPrincipalMap = new HashMap<Object, IUserPrincipal>();

	/**
	 * Constructs a new singleton PadoServerManager object.
	 */
	protected PadoServerManager()
	{
	}

	/**
	 * Partially initializes the server by validating pado.xml.
	 * 
	 * @param props
	 * @throws ConfigurationException
	 *             Thrown if a configuration error occurs
	 */
	protected void init(Properties props) throws ConfigurationException
	{
		String configFilePath = System.getProperty("pado.config-file");
		if (configFilePath == null) {
			configFilePath = props.getProperty("config-file",
					PadoUtil.getProperty(Constants.PROP_ETC_GRID_DIR) + "/pado.xml");
		}

		File configFile = new File(configFilePath);
		if (configFile.exists() == false) {
			throw new PadoException("Pado config file does not exist: " + configFile.getAbsolutePath());
		}

		// Validate the file
		validateXml(configFile);

		try {
			JAXBContext ctx = JAXBContext
					.newInstance(new Class[] { com.netcrest.pado.internal.config.dtd.generated.Pado.class });
			Unmarshaller um = ctx.createUnmarshaller();
			padoConfig = (com.netcrest.pado.internal.config.dtd.generated.Pado) um.unmarshal(configFile);

			// grid paths
			buildGridPaths();

			// validate grid paths with the allowed grid lists
			validateGridPaths();

			// Configure the internal regions. Note that client connection
			// points are initialized separately in a thread to prevent GemFire
			// initialization conflicts. See initializePadoManager().
			config(padoConfig, configFilePath);

		} catch (JAXBException e) {
			throw new ConfigurationException(e);
		}
	}

	private void buildGridPaths()
	{
		com.netcrest.pado.internal.config.dtd.generated.PathList pathList = padoConfig.getPathList();
		if (pathList != null) {
			PathListConfig pathListConfig = new PathListConfig(pathList);
			List<PathConfig> paths = pathListConfig.getPathConfig();
			for (PathConfig pathConfig : paths) {
				createGridPathInfo(pathConfig, null);
			}
		}
	}

	private GridPathInfo createGridPathInfo(PathConfig pathConfig, String parentPath)
	{
		String gridPath;
		if (parentPath == null || parentPath.length() == 0) {
			gridPath = pathConfig.getName();
		} else {
			gridPath = parentPath + "/" + pathConfig.getName();
		}
		GridPathInfo gridPathInfo = InfoFactory.getInfoFactory().createGridPathInfo(gridPath);
		gridPathInfo.setAccessType(pathConfig.getAccessType());
		gridPathInfo.setDataType(pathConfig.getDataType());
		gridPathInfo.setGridIdSet(pathConfig.getGrids());
		gridPathInfo.setDescription(StringUtil.removeExtraSpaces(pathConfig.getDescription()));
		gridPathInfo.setInherit(pathConfig.getInherit());
		gridPathInfo.setTemporalEnabled(pathConfig.isTemporalEnabled());
		gridPathInfo.setLuceneEnabled(pathConfig.isLuceneEnabled());
		gridPathInfo.setKeyClassName(pathConfig.getKeyClassName());
		gridPathInfo.setDataClassName(pathConfig.getValueClassName());
		gridPathInfo.setRouterClassName(pathConfig.getRouterClassName());
		gridPathInfoMap.put(gridPathInfo.getGridPath(), gridPathInfo);

		List<PathConfig> paths = pathConfig.getPathConfig();
		for (PathConfig pathConfig2 : paths) {
			createGridPathInfo(pathConfig2, gridPath);
		}
		return gridPathInfo;
	}

	/**
	 * Validates all of grid paths against the allowed grid lists.
	 * 
	 * @throws ConfigurationException
	 *             Thrown if any of the grid IDs defined for grid paths is not
	 *             in the allowed grid lists.
	 */
	protected void validateGridPaths() throws ConfigurationException
	{
		com.netcrest.pado.internal.config.dtd.generated.AppList appList = padoConfig.getAppList();
		if (appList != null) {
			List<com.netcrest.pado.internal.config.dtd.generated.App> apps = appList.getApp();
			if (apps != null) {
				for (com.netcrest.pado.internal.config.dtd.generated.App app : apps) {
					com.netcrest.pado.internal.config.dtd.generated.GridList gridList = app.getGridList();
					if (gridList != null) {
						List<com.netcrest.pado.internal.config.dtd.generated.Grid> grids = gridList.getGrid();

						for (GridPathInfo gridPathInfo : gridPathInfoMap.values()) {
							if (gridPathInfo.getGridIdSet() == null) {
								continue;
							}
							for (String gridId : gridPathInfo.getGridIdSet()) {
								boolean allowed = false;
								for (com.netcrest.pado.internal.config.dtd.generated.Grid gridConfig : grids) {
									if (gridId.equals(gridConfig.getId())) {
										allowed = true;
										break;
									}
								}
								if (allowed == false) {
									throw new ConfigurationException(
											"Grid ID is not in the allowed list. [appId= " + app.getId() + ", gridId="
													+ gridId + ", path=" + gridPathInfo.getGridPath() + "]");
								}
							}
						}

					}
				}
			}
		}
	}

	public com.netcrest.pado.internal.config.dtd.generated.Pado getPadoConfig()
	{
		return padoConfig;
	}

	/**
	 * Returns the grid path info map that contains &lt;grid-path,
	 * GridPathInfo&gt; pairs.
	 */
	public Map<String, GridPathInfo> getGridPathInfoMap()
	{
		return gridPathInfoMap;
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
					Logger.error(createLogMessage("Error:", e));
					throw e;
				}

				@Override
				public void fatalError(SAXParseException e) throws SAXException
				{
					Logger.severe(createLogMessage("Fatal Error:", e));
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

	/**
	 * Returns IGridBizLink that provides grid-to-grid services.
	 */
	public IGridBizLink getGridBiz()
	{
		return gridBiz;
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
	 * Updates the grid paths. It overwrites the existing grid paths if any,
	 * 
	 * @param gridPathInfoSet
	 *            Grid path info set
	 */
	public void updateGridPaths(Set<GridPathInfo> gridPathInfoSet)
	{
		if (gridPathInfoSet == null) {
			return;
		}

		// Update gridPathInfoMap which contains all grid paths.
		ConcurrentHashMap<String, GridPathInfo> newMap = new ConcurrentHashMap<String, GridPathInfo>(
				gridPathInfoSet.size() + 2, .95f);
		for (GridPathInfo gridPathInfo : gridPathInfoSet) {
			if (gridPathInfo.getGridPath() != null && isValidGridPath(gridPathInfo.getGridPath())) {
				newMap.put(gridPathInfo.getGridPath(), gridPathInfo);
			}
		}
		gridPathInfoMap = newMap;

		// TODO: Update the XML file
	}

	/**
	 * Returns true if the specified grid path is a valid name. A valid name
	 * must be alpha numeric including the underscore character.
	 * 
	 * @param gridPath
	 *            Grid path
	 */
	private boolean isValidGridPath(String gridPath)
	{
		return gridPath.matches("^[a-zA-Z0-9_/]*$");
	}

	/**
	 * Publishes GridInfo to all parents and updates all child grid AppInfo
	 * objects.
	 */
	private void initAppGridIds()
	{
		// Publish GridInfo to all parent grids
		publishGridInfoToParentGrids();

		// Update AppInfo objects. It retrieves GridInfo
		// from child grids.
		try {
			updateAppInfos();
		} catch (Exception ex) {
			reinitRequired = true;
		}
	}

	/**
	 * Initializes the singleton object PadoManager. This must be called during
	 * the grid node startup time. It returns the single reference if it has
	 * already been created (initialized).
	 * 
	 * @return Returns the partially initialized singleton PadoManager instance.
	 *         {@link #startPadoManager()} must be invoked in order to fully
	 *         initialize the singleton instance.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static PadoServerManager initializePadoManager(final Properties properties)
	{
		if (padoServerManager == null) {
			String disclaimer = PadoDisclaimer.getDisclaimer();

			// The constructor initializes only this grid specifics.
			// It does not gather any of the other grids' information,
			// which is done in initLogin().
			try {
				Class clazz = PadoUtil.getClass(Constants.PROP_CLASS_PADO_SERVER_MANAGER,
						Constants.DEFAULT_CLASS_PADO_SERVER_MANAGER);
				padoServerManager = (PadoServerManager) clazz.newInstance();
				Method method = clazz.getDeclaredMethod("logDisclaimer", String.class);
				if (method != null) {
					method.setAccessible(true);
					method.invoke(padoServerManager, disclaimer);
				} else {
					Logger.info(disclaimer);
				}
				padoServerManager.init(properties);
			} catch (Exception ex) {
				if (ex instanceof RuntimeException) {
					throw (RuntimeException) ex;
				}
				throw new ConfigurationException(ex);
			}
		}
		return padoServerManager;
	}

	/**
	 * Starts the singleton instance of PadoManager. This method should not be
	 * invoked by applications. It is for internal use only. Invoking this
	 * method has no effect for applications.
	 */
	public void startPadoManager()
	{
		synchronized (this) {
			if (padoServerManagerStarted == false) {
				// Initialize this grid.
				padoServerManager.initThisGrid();

				// TODO: Delay may be required to allow GemFire to complete
				// initialization; otherwise, a race condition occurs. Need to
				// consult VMware.
				// long initDelay =
				// Long.parseLong(padoConfig.getGemfire().getInitDelay());
				Timer timer = new Timer("Pado-PadoServerManager Login Initializer", true);
				timer.schedule(new TimerTask() {
					public void run()
					{
						// initiLogin() logs in to the parent grid. It
						// indefinitely
						// tries to log in to the parent grid until successful.
						// Upon successful login, it initializes the grid.
						initLogin();
					}
				}, 0);

				padoServerManagerStarted = true;
			}
		}
	}

	/**
	 * Initializes the mechanics for communicating with all participating grids.
	 */
	protected final static void initLogin()
	{
		try {
			LoginInfo loginInfo = padoServerManager.loginToParent();
			padoServerManager.initParents();
			if (loginInfo != null) {
				Logger.config("Login to parent pado successful: ParentInfo[gridId=" + loginInfo.getGridId()
						+ ", locators=" + loginInfo.getLocators() + "]");
			}
		} catch (Exception ex1) {
			// If failed then the parent might not be running. Retry it later.
			Logger.warning("Login to parent pado failed. Login will occur periodically until successful.");

			// Periodically login to parent until successful
			Thread thread = new Thread(new Runnable() {
				public void run()
				{
					LoginInfo loginInfo = null;
					int count = 0;
					while (loginInfo == null) {
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							// ignore
						}
						try {
							loginInfo = padoServerManager.loginToParent();
							padoServerManager.initParents();
							Logger.config("Login to parent pado successful");
						} catch (Exception ex) {
							if (count % 10 == 0) {
								Logger.warning(
										"Login to parent pado failed. Login will occur periodically until successful. "
												+ ex.getMessage());
								count = 0;
							}
						}
						count++;
					}
				}
			}, "Pado-PadoServerManager.initLogin");
			thread.setDaemon(true);
			thread.start();
		}
	}

	/**
	 * Returns the instance of PadoServerManager.
	 */
	public static PadoServerManager getPadoServerManager()
	{
		return padoServerManager;
	}

	/**
	 * Returns GridInfo objects from all parent grids. It might be a partial
	 * list if one or more parent grids are down. Failed parent grids are logged
	 * with the error log level.
	 */
	public GridInfo[] getParentGridInfos()
	{
		ArrayList<GridInfo> list = new ArrayList<GridInfo>(parentGridBizMap.size() + 1);
		for (Map.Entry<String, IGridBizLink> entry : parentGridBizMap.entrySet()) {
			try {
				IGridBizLink gridBiz = entry.getValue();
				list.add(gridBiz.getGridInfo());
			} catch (Exception ex) {
				Logger.error("Error while retrieving parent GridInfo for the grid ID " + gridId
						+ ". This grid may not be available.", ex);
			}
		}
		return list.toArray(new GridInfo[list.size()]);
	}

	/**
	 * Returns GridInfo objects from all child grids. It might be a partial list
	 * if one or more child grids are down. Failed child grids are logged with
	 * the error log level.
	 */
	public GridInfo[] getChildGridInfos(String appId)
	{
		AppInfo appInfo = getAppInfo(appId);
		if (appInfo == null) {
			return null;
		}
		String allowedGridIds[] = appInfo.getAllowedGridIdSet()
				.toArray(new String[appInfo.getAllowedGridIdSet().size()]);
		ArrayList<GridInfo> list = new ArrayList<GridInfo>(childGridBizMap.size());
		for (int i = 0; i < allowedGridIds.length; i++) {
			IGridBizLink gridBiz = null;
			try {
				gridBiz = childGridBizMap.get(allowedGridIds[i]);
				if (gridBiz != null) {
					gridBiz.getBizContext().getGridContextClient().setGridIds(allowedGridIds[i]);
					list.add(gridBiz.getGridInfo());
				}
			} catch (Exception ex) {
				Logger.error("Error while retrieving child GridInfo for the grid ID " + gridId
						+ ". This grid may not be available.", ex);
			} finally {
				if (gridBiz != null) {
					gridBiz.getBizContext().reset();
				}
			}
		}
		Collections.sort(list);
		return list.toArray(new GridInfo[list.size()]);
	}

	/**
	 * Returns the cached GridPathInfo object for the specified grid path.
	 * 
	 * @param gridPath
	 *            Grid path.
	 */
	protected GridPathInfo getGridPathInfo(String gridPath)
	{
		return gridPathInfoMap.get(gridPath);
	}

	/**
	 * Publishes GridInfo to all of the parent grids.
	 */
	public void publishGridInfoToParentGrids()
	{
		GridInfo gridInfo = getGridInfo();
		for (IGridBizLink parentGridBiz : parentGridBizMap.values()) {
			try {
				parentGridBiz.publishGridInfo(gridInfo);
			} catch (Exception ex) {
				reinitRequired = true;
			}
		}
	}

	/**
	 * Returns the cached GridInfo object if exists. It returns a new GridInfo
	 * object otherwise. Invoke {@link #createGridInfo()} to get the latest.
	 * 
	 * @see #createGridInfo()
	 */
	public GridInfo getGridInfo()
	{
		if (gridInfo == null) {
			gridInfo = createGridInfo();
		}
		return gridInfo;
	}

	/**
	 * Creates and returns BizInfo objects for all BizManager objects in the
	 * specified map.
	 * 
	 * @param bixManagerMap
	 *            {@link #systemMap} or {@link #appMap}
	 */
	@SuppressWarnings("rawtypes")
	private Set<BizInfo> getAllBizInfos(Map<String, BizManager> bixManagerMap)
	{
		Collection<BizManager> col = bixManagerMap.values();
		HashSet<BizInfo> set = new HashSet<BizInfo>(20);
		for (BizManager manager : col) {
			BizInfo bizInfo = InfoFactory.getInfoFactory().createBizInfo(manager.getTargetClass().getName());
			set.add(bizInfo);
		}
		return set;
	}

	/**
	 * Returns BizManager for the specified IBiz class found in the specified
	 * BizManger map.
	 * 
	 * @param bizManagerMap
	 *            BizManager map, i.e., {@link #systemMap} or {@link #appMap}
	 * @param bizClass
	 *            IBiz class
	 * @return Returns null if BizManger is not found.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected BizManager getBizManager(Map<String, BizManager> bizManagerMap, Class<?> bizClass)
	{
		BizManager<IBiz> svcMgr = bizManagerMap.get(bizClass.getName());
		if (svcMgr == null) {
			if (BizUtil.isFuture(bizClass)) {
				if (bizManagerMap.containsKey(BizUtil.getBizInterfaceName(bizClass))) {
					svcMgr = BizManagerFactory.getBizManagerFactory().createBizManager(bizClass, true);
					svcMgr.init();
					bizManagerMap.put(svcMgr.getTargetClass().getName(), svcMgr);
				}
			}
		}

		return svcMgr;
	}

	/**
	 * Returns BizManager for the specified IBiz class name found in the
	 * specified BizManager map.
	 * 
	 * @param bizManagerMap
	 *            BizManager map, i.e., {@link #systemMap} or {@link #appMap}
	 * @param bizClassName
	 *            IBiz class name
	 * @return Returns null if BizManger is not found.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private BizManager getBizManager(Map<String, BizManager> bizManagerMap, String bizClassName)
	{
		BizManager<IBiz> bizManager = bizManagerMap.get(bizClassName);
		if (bizManager == null) {
			// TODO: isFuture() assumes all Future class names end with
			// "Future".
			if (BizUtil.isFuture(bizClassName)) {
				// Load the future class using the same IBiz target class
				// loader.
				bizManager = bizManagerMap.get(BizUtil.getBizInterfaceNameOfFuture(bizClassName));
				if (bizManager != null) {
					Class<?> bizClass = bizManager.getTargetClass();
					try {
						Class<?> futureBizClass = bizClass.getClassLoader().loadClass(bizClassName);
						bizManager = BizManagerFactory.getBizManagerFactory().createBizManager(futureBizClass, true);
						bizManager.init();
						bizManagerMap.put(bizClassName, bizManager);
					} catch (ClassNotFoundException e) {
						Logger.warning("Attempted to load undefined Future biz class, " + bizClassName + ". Aborted.");
					}
				}
			}
		}
		return bizManager;
	}

	/**
	 * Removes the specified system-level biz manager which is accessible only
	 * by users with system-level rights.
	 * 
	 * @param bizManager
	 *            System-level biz manager
	 */
	@SuppressWarnings("rawtypes")
	public void removeSystemBizManager(BizManager bizManager)
	{
		systemMap.remove(bizManager.getTargetClass().getName());
	}

	/**
	 * Adds the specified BizManager object as a system-level BizManager.
	 * 
	 * @param bizManager
	 *            Biz manager
	 */
	@SuppressWarnings("rawtypes")
	public void addSystemBizMananger(BizManager bizManager)
	{
		if (bizManager != null) {
			systemMap.put(bizManager.getTargetClass().getName(), bizManager);
		}
	}

	/**
	 * Returns the system-level BizManager object.
	 * 
	 * @param bizClass
	 * @return Returns null if BizManger is not found.
	 * @return Returns null if BizManger is not found.
	 */
	@SuppressWarnings("rawtypes")
	public BizManager getSystemBizManager(Class<?> bizClass)
	{
		return getBizManager(systemMap, bizClass);
	}

	/**
	 * Returns the system-level BizManager object
	 * 
	 * @param ibizClassName
	 *            IBiz class name
	 * @return Returns null if BizManger is not found.
	 */
	@SuppressWarnings("rawtypes")
	public BizManager getSystemBizManager(String ibizClassName)
	{
		return getBizManager(systemMap, ibizClassName);
	}

	/**
	 * Removes the BizManager object belonging to the specified system-level
	 * IBiz class name.
	 * 
	 * @param bizClassName
	 *            IBiz class name
	 */
	public void removeSystemBizManager(String bizClassName)
	{
		systemMap.remove(bizClassName);
	}

	/**
	 * Returns true if the BizManager object for the specified app-level IBiz
	 * class name is found.
	 */
	public boolean containsAppBizManager(String className)
	{
		return appMap.containsKey(className);
	}

	/**
	 * Returns all app-level IBiz class names. The returned class names
	 * represent the current snapshot of all app-level IBiz classes managed by
	 * this grid.
	 */
	public String[] getAllAppBizClassNames()
	{
		return appMap.keySet().toArray(new String[appMap.keySet().size()]);
	}

	/**
	 * Returns all app-level IBiz classes. The returned classes represent the
	 * current snapshot of all app-level IBiz classes managed by this grid.
	 */
	@SuppressWarnings("rawtypes")
	public Class<?>[] getAllAppBizClasses()
	{
		Collection<BizManager> col = appMap.values();
		Class<?>[] classes = new Class<?>[col.size()];
		int i = 0;
		for (BizManager bizManager : col) {
			classes[i++] = bizManager.getTargetClass();
		}
		return classes;
	}

	/**
	 * Clears all BizManager objects from this grid.
	 */
	@SuppressWarnings("rawtypes")
	public void clearBizManagers()
	{
		Collection<BizManager> col = systemMap.values();
		for (BizManager bizManager : col) {
			bizManager.close();
		}
		systemMap.clear();

		col = appMap.values();
		for (BizManager bizManager : col) {
			bizManager.close();
		}
		appMap.clear();
	}

	/**
	 * Returns all the system-level BizInfo objects.
	 */
	public Set<BizInfo> getAllSysBizInfos()
	{
		return getAllBizInfos(systemMap);
	}

	/**
	 * Adds the specified app-level BizManager object. It overwrites the
	 * existing BizManager.
	 * 
	 * @param bizManager
	 *            Biz manager
	 */
	@SuppressWarnings("rawtypes")
	public void addAppBizManager(BizManager bizManager)
	{
		if (bizManager != null) {
			appMap.put(bizManager.getTargetClass().getName(), bizManager);
		}
	}

	/**
	 * Returns the app-level BizManager object of the specified IBiz class.
	 * 
	 * @param bizClass
	 *            IBiz class
	 * @return Returns null if not found.
	 */
	@SuppressWarnings("rawtypes")
	public BizManager getAppBizManager(Class<?> bizClass)
	{
		return getBizManager(appMap, bizClass);
	}

	/**
	 * Returns the app-level BizManager object of the specified IBiz class name.
	 * 
	 * @param bizClassName
	 *            IBiz class name
	 * @return Returns null if not found.
	 */
	@SuppressWarnings("rawtypes")
	public BizManager getAppBizManager(String bizClassName)
	{
		return getBizManager(appMap, bizClassName);
	}

	/**
	 * Removes the specified app-level BizManager object.
	 * 
	 * @param bizManager
	 */
	@SuppressWarnings("rawtypes")
	public void removeAppBizManager(BizManager bizManager)
	{
		appMap.remove(bizManager.getTargetClass().getName());
	}

	/**
	 * Removes the BizManager object that belongs to the specified IBiz class
	 * name.
	 * 
	 * @param bizClassName
	 *            IBiz class name
	 */
	public void removeAppBizManager(String bizClassName)
	{
		appMap.remove(bizClassName);
	}

	/**
	 * Returns the application business class loader.
	 * 
	 * @return null if no business classes are defined. This should never occur
	 *         since there is always at least one application class defined.
	 */
	@SuppressWarnings("rawtypes")
	public ClassLoader getAppBizClassLoader()
	{
		Set<Map.Entry<String, BizManager>> set = appMap.entrySet();
		for (Map.Entry<String, BizManager> entry : set) {
			return entry.getValue().getTargetClass().getClassLoader();
		}
		return null;
	}

	/**
	 * Initializes all app business classes. This should be invoked once after
	 * the server-side catalog has been created.
	 */
	@SuppressWarnings("rawtypes")
	public void __initAppBizClasses()
	{
		Set<Map.Entry<String, BizManager>> set = appMap.entrySet();
		for (Map.Entry<String, BizManager> entry : set) {
			BizManager bizManager = entry.getValue();
			bizManager.init();
		}
	}

	/**
	 * Invokes the initialization bean if configured in pado.xml. Also,
	 * initializes properties defined in pado.properties.
	 */
	public void __initStartup()
	{
		com.netcrest.pado.internal.config.dtd.generated.Startup startup = padoConfig.getStartup();
		if (startup != null) {
			com.netcrest.pado.internal.config.dtd.generated.Bean bean = startup.getBean();
			if (bean != null) {
				try {
					Object obj = ConfigUtil.createBean(null, bean);
					if (obj != null) {
						Logger.config("Bean registered: " + obj.getClass().getName());
					}
				} catch (ClassNotFoundException ex) {
					Logger.severe("Startup bean class not found.", ex);
				} catch (InstantiationException ex) {
					Logger.severe("Startup bean instantiation failed.", ex);
				} catch (IllegalAccessException ex) {
					Logger.severe("Startup bean illegal access", ex);
				}
			}
		}

		try {
			String val = PadoUtil.getProperty(Constants.PROP_LUCENE_MAX_CLAUSE_COUNT);
			if (val != null) {
				int maxClauseCount = Integer.parseInt(val);
				BooleanQuery.setMaxClauseCount(maxClauseCount);
			}
		} catch (Exception ex) {
			Logger.warning("Invalid lucene.maxCluaseCount", ex);
		}

		// Enable temporal data - Even though temporal data is already
		// handled, due to GemFire persistent regions that fail to provide
		// all events to temporal cache listener, let's re-enable temporal
		// data as a safe measure.
		boolean buildLucene = padoConfig.getPathList().getBuildLuceneIndex() != null
				&& padoConfig.getPathList().getBuildLuceneIndex().equalsIgnoreCase("true");
		TemporalManager.setEnabledAll(true, buildLucene, false); // block till
																	// done
	}

	/**
	 * Returns all app-level BizInfo objects that this grid manages.
	 */
	public Set<BizInfo> getAllAppBizInfos()
	{
		return getAllBizInfos(appMap);
	}

	/**
	 * Returns the grid ID of this server.
	 */
	public String getGridId()
	{
		return gridId;
	}

	/**
	 * Returns the site ID of this server.
	 */
	public String getSiteId()
	{
		return siteId;
	}

	/**
	 * Returns the server ID of this server.
	 */
	public String getServerId()
	{
		return serverId;
	}

	/**
	 * Returns server number with leading zeroes.
	 */
	public String getServerNum()
	{
		return serverNum;
	}

	/**
	 * Returns the server name that is human legible and unique.
	 */
	public String getServerName()
	{
		return serverName;
	}

	/**
	 * Returns the location ID of this server.
	 */
	public String getLocation()
	{
		return location;
	}

	/**
	 * Returns the locators of the site this server belongs to.
	 */
	public String getLocators()
	{
		return locators;
	}

	/**
	 * Pairs the specified app ID and grid ID as the default. Each app has the
	 * default grid associated with it.
	 * 
	 * @param appId
	 *            App ID
	 * @param gridId
	 *            Grid ID
	 */
	public void putDefaultGridId(String appId, String gridId)
	{
		defaultGridMap.put(appId, gridId);
	}

	/**
	 * Removes the specified app ID from the default list.
	 * 
	 * @param appId
	 *            App ID
	 */
	public void removeDefaultGridId(String appId)
	{
		defaultGridMap.remove(appId);
	}

	/**
	 * Returns the default grid ID of the specified app ID.
	 * 
	 * @param appId
	 *            App ID
	 */
	public String getDefaultGridId(String appId)
	{
		return defaultGridMap.get(appId);
	}

	/**
	 * Returns the IDs of the connected parent grids. It returns an empty array
	 * if there are no connected parent grids.
	 */
	public String[] getParentGridIds()
	{
		return parentGridBizMap.keySet().toArray(new String[parentGridBizMap.size()]);
	}

	/**
	 * Returns the IDs of the connected child grids. It returns an empty array
	 * if there are no connected child grids.
	 */
	public String[] getChildGridIds()
	{
		return childGridBizMap.keySet().toArray(new String[childGridBizMap.size()]);
	}

	/**
	 * Returns the server-side catalog of IBiz classes.
	 */
	public ICatalog getCatalog()
	{
		return catalog;
	}

	/**
	 * Returns the server-side IPado instance. Unlike a client IPado
	 * implementation, the returned instance is not fully functional.
	 */
	public IPado getPado()
	{
		if (catalog == null) {
			return null;
		}
		return catalog.getPado();
	}

	/**
	 * Returns the grid ID associated with the specified root path.
	 * 
	 * @param rootPath
	 *            Root path that begins with "/".
	 */
	public String getGridIdForRootPath(String rootPath)
	{
		return gridIdMap.get(rootPath);
	}

	/**
	 * Returns true if the specified full path is a Pado grid path. A valid Pado
	 * grid path (or full path) has the grid ID associated with it. A valid full
	 * path typically has its root name mapped to a valid grid ID.
	 * 
	 * @param fullPath
	 *            Full path that begins with "/".
	 * @see #getGridIdForFullPath(String)
	 */
	public boolean isPadoPath(String fullPath)
	{
		return getGridIdForFullPath(fullPath) != null;
	}

	/**
	 * Returns true if this grid is a parent.
	 */
	public boolean isParent()
	{
		return isParent;
	}

	/**
	 * Returns true if encryption is enabled.
	 */
	public boolean isEncryptionEnabled()
	{
		return isEncryptionEnabled;
	}

	// ---------------------------------------------------------------------
	// Abstract methods
	// ---------------------------------------------------------------------

	/**
	 * Configures this grid. It initializes internal regions and launches the
	 * "GridRegion Updater" timer thread that periodically broadcasts its
	 * GridInfo to the parents. Note that it does not configure client
	 * connection points.
	 * 
	 * @param padoConfig
	 *            JABX pado config object
	 * @param configFilePath
	 *            Configuration file path used for loggin purpose only.
	 */
	protected abstract void config(com.netcrest.pado.internal.config.dtd.generated.Pado padoConfig,
			String configFilePath);

	/**
	 * Initializes the parent grids.
	 */
	protected abstract void initParents();

	/**
	 * Initializes AppInfo objects. AppInfo includes GridRoutingTable.Grid
	 * objects.
	 */
	protected abstract void initAppInfos() throws ConfigurationException;

	/**
	 * Logs in to the parent grid and returns the LoginInfo received from it.
	 * 
	 * @throws PadoLoginException
	 *             Thrown if login fails
	 */
	protected abstract LoginInfo loginToParent() throws PadoLoginException;

	/**
	 * Initializes this grid. This method is invoked at startup.
	 */
	protected abstract void initThisGrid();

	/**
	 * Returns the parent GridInfo object of the specified parent grid ID.
	 * 
	 * @param parentGridId
	 *            Parent grid ID.
	 */
	public abstract GridInfo getParentGridInfo(String parentGridId);

	/**
	 * Returns the child GridInfo object of the specified child grid ID.
	 * 
	 * @param childGridId
	 *            Child grid ID
	 */
	public abstract GridInfo getChildGridInfo(String childGridId);

	/**
	 * Returns AppInfo of the specified app ID.
	 * 
	 * @param appId
	 *            App ID
	 */
	public abstract AppInfo getAppInfo(String appId);

	/**
	 * Creates and returns a new GridInfo object containing the latest snapshot
	 * information of this grid.
	 * 
	 * @see #getGridInfo()
	 */
	public abstract GridInfo createGridInfo();

	/**
	 * Updates the grid routing table.
	 * 
	 * @param routingTable
	 *            Grid routing table
	 */
	public abstract void updateGridRoutingTable(GridRoutingTable routingTable);

	/**
	 * Updates all metadata related to the specified GridInfo only if the grid
	 * specified by GridInfo is in the app allowed lists.
	 * 
	 * @param gridInfo
	 *            GridInfo from the remote grid
	 * @param isParent
	 *            true if parent grid, else child grid
	 */
	public abstract void updateGrid(GridInfo gridInfo, boolean isParent);

	/**
	 * Attaches this grid to the specified parent grid. This version of Pado
	 * supports only a single parent. The specified parent Id is ignored.
	 * 
	 * @param parentGridId
	 *            Parent grid ID
	 */
	public abstract void attachToParentGrid(String parentGridId);

	/**
	 * Removes all metadata related to the specified GridInfo. Note that it does
	 * not allow removal of the hosting (this) grid's metadata.
	 * 
	 * @param gridInfo
	 *            GridInfo from the remote grid
	 * @param isParent
	 *            true if parent grid, else child grid
	 */
	public abstract void removeGrid(GridInfo gridInfo, boolean isParent);

	/**
	 * Removes all metadata related to the specified grid ID. Note that it does
	 * not allow removal of the hosting (this) grid's metadata.
	 * 
	 * @param gridId
	 *            Grid ID
	 * @param isParent
	 *            true if parent grid, else child grid
	 */
	public abstract void removeGrid(String gridId, boolean isParent);

	/**
	 * Removes the specified grid from AppInfo.
	 * 
	 * @param gridId
	 *            Grid ID
	 */
	public abstract void removeFromAppInfo(String gridId);

	/**
	 * Updates AppInfo in the app path with GridInfo retrieved from each child
	 * grid. An app spans across one or more grids.
	 */
	public abstract void updateAppInfos();

	/**
	 * Updates AppInfo in the app path with the specified GridInfo object. An
	 * app spans across one or more grids.
	 * 
	 * @param gridInfo
	 *            GridInfo containing grid specific information.
	 */
	public abstract void updateAppInfo(GridInfo gridInfo);

	/**
	 * Returns the grid ID of the specified full path. It returns null if the
	 * specified full path is not mapped to a grid ID.
	 * 
	 * @param fullPath
	 * @see #isPadoPath(String)
	 */
	public abstract String getGridIdForFullPath(String fullPath);

	/**
	 * Puts the specified message tagged by the specified messageType targeted
	 * for all user sessions.
	 * 
	 * @param messageType
	 *            Message type
	 * @param message
	 *            Serializable message
	 */
	public abstract void putMessage(MessageType messageType, Object message);

	/**
	 * Puts the specified message tagged by the specified message type targeted
	 * for all parent grids.
	 * 
	 * @param messageType
	 *            TypeMessage type
	 * @param message
	 *            Serializable message
	 */
	public abstract void putMessageParents(MessageType messageType, Object message);

	/**
	 * Puts the specified message tagged by the specified message type targeted
	 * for all child grids.
	 * 
	 * @param messageType
	 *            TypeMessage type
	 * @param message
	 *            Serializable message
	 */
	public abstract void putMessageChildren(MessageType messageType, Object message);

	/**
	 * Returns the LoginInfo object associated with the specified session token.
	 * 
	 * @param token
	 *            User session token
	 */
	public abstract LoginInfo getLoginInfo(Object token);

	/**
	 * Returns the user name of the specified session token. It returns null if
	 * the token is invalid.
	 * 
	 * @param token
	 *            User session token
	 */
	public abstract String getUsername(Object token);

	/**
	 * Enables or disables the specified app running in the specified grid.
	 * Disabled grids are removed from the routers and therefore are not
	 * available to the users.
	 * 
	 * @param appId
	 *            App ID
	 * @param gridId
	 *            Grid ID
	 * @param enabled
	 *            true to enable, false to disable
	 * @see #isGridEnabled(String, String)
	 */
	public abstract void setGridEnabled(String appId, String gridId, boolean enabled);

	/**
	 * Returns true if the specified grid ID is enabled in the specified app ID.
	 * 
	 * @param appId
	 *            App ID
	 * @param gridId
	 *            Grid ID
	 * @see #setGridEnabled(String, String, boolean)
	 */
	public abstract boolean isGridEnabled(String appId, String gridId);

	/**
	 * Returns true if this server is the master server.
	 */
	public abstract boolean isMaster();

	/**
	 * Adds the master failover listener.
	 *
	 * @param listener
	 *            the listener
	 */
	public abstract void addMasterFailoverListener(MasterFailoverListener listener);

	/**
	 * Removes the master failover listener.
	 *
	 * @param listener
	 *            the listener
	 */
	public abstract void removeMasterFailoverListener(MasterFailoverListener listener);

	/**
	 * Returns the IUserPrincial object mapped by the specified token.
	 * IUserPrincipal is typically created and cached upon successful login.
	 * 
	 * @param token
	 *            Session token
	 * @return null if the token is invalid. The token is invalid if the
	 *         specified token is null, the user session was never created, or
	 *         timed out.
	 */
	public abstract IUserPrincipal getUserPrincipal(Object token);

	/**
	 * Removes the user session from the server.
	 * 
	 * @param token
	 *            Session token
	 */
	public abstract void removeUserSession(Object token);

	/**
	 * Returns true if the specified token is valid.
	 * 
	 * @param token
	 *            User session token
	 */
	public abstract boolean isValidToken(Object token);

	/**
	 * Returns the total number of currently running servers in the specified
	 * grid.
	 * 
	 * @param gridId
	 *            Grid ID. null to return this grid's server count.
	 */
	public abstract int getServerCount(String gridId);

	/**
	 * Returns all unique server IDs in the specified grid.
	 * 
	 * @param gridId
	 *            Grid ID. null to return this grid's server IDs.
	 */
	public abstract Object[] getServerIds(String gridId);
	
	/**
	 * Returns the total number of currently running servers in this grid.
	 */
	public int getServerCount()
	{
		return getServerCount(null);
	}
	
	/**
	 * Returns all unique server IDs in this grid.
	 */
	public Object[] getServerIds()
	{
		return getServerIds(null);
	}
}
