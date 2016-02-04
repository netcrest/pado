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
package com.netcrest.pado.internal.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.netcrest.pado.IDataContext;
import com.netcrest.pado.IGridService;
import com.netcrest.pado.IUserContext;
import com.netcrest.pado.Pado;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.info.AppInfo;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.PadoServerManager;

public abstract class PadoClientManager {
	protected static PadoClientManager clientManager;

	static {
		try {
			Class<?> clazz = PadoUtil.getClass(Constants.PROP_CLASS_PADO_CLIENT_MANAGER,
					Constants.DEFAULT_CLASS_PADO_CLIENT_MANAGER);
			clientManager = (PadoClientManager) clazz.newInstance();
		} catch (Exception e) {
			Logger.severe("PadoClientManager creation failed. The client will not communicate with the grid.", e);
		}
	}

	private Class<IUserContext> userContextClass;
	private Class<IDataContext> dataContextClass;

	// <appId, Map<username, Pado>>
	protected final ConcurrentMap<String, Map<String, Pado>> padoMap = new ConcurrentHashMap<String, Map<String, Pado>>(
			2);

	/**
	 * Initializes PadoClientManager with the specified context classes. These
	 * classes are used by the client API to internally feed biz context
	 * information when invoking IBiz objects. If this method is not first
	 * invoked then {@link #getPadoClientManager()} returns the singleton
	 * instance that has no context information. Note that this method is
	 * typically invoked during startup by the underlying data grid initializer
	 * which is configured by Pado during the connection time, i.e., by invoking
	 * {@link Pado#connect(String, boolean)}.
	 * <p>
	 * <b>Important</b>
	 * <p>
	 * Each invocation of this method creates a new single PadoClientManager
	 * object. This allows the client to refresh with a new set of context
	 * classes. If the same set of classes are continue to be used then the
	 * {@link #clear()} should be invoked instead. Before invoking this method,
	 * the context (user and data) classes should be included as properties in
	 * PadoUtil.
	 * 
	 * @return Returns the singleton instance of PadoClientManager.
	 */
	public static synchronized PadoClientManager initialize() {
		try {
			String val = PadoUtil.getProperty(Constants.PROP_CLASS_USER_CONTEXT, Constants.DEFAULT_CLASS_USER_CONTEXT);
			Class<IUserContext> userContextClass = null;
			if (val != null) {
				try {
					userContextClass = (Class<IUserContext>) Class.forName(val);
				} catch (ClassNotFoundException e) {
					Logger.error("Specified userContextClass is not valid: " + val + ". userContextClass is not set.");
				}
			}
			val = PadoUtil.getProperty(Constants.PROP_CLASS_DATA_CONTEXT, Constants.DEFAULT_CLASS_DATA_CONTEXT);
			Class<IDataContext> dataContextClass = null;
			if (val != null) {
				try {
					dataContextClass = (Class<IDataContext>) Class.forName(val);
				} catch (ClassNotFoundException e) {
					Logger.error("Specified dataContextClass is not valid: " + val + ". dataContextClass is not set.");
				}
			}

			clientManager.init(new Properties());
			clientManager.clear();
			clientManager.userContextClass = userContextClass;
			clientManager.dataContextClass = dataContextClass;

		} catch (Exception ex) {
			throw new PadoException(ex);
		}

		return clientManager;
	}

	public static PadoClientManager getPadoClientManager() {
		return clientManager;
	}

	public void init(Properties props) {
	}

	public IUserContext createUserContext(IGridService gridService) {
		IUserContext userContext = null;
		if (userContextClass != null) {
			try {
				userContext = userContextClass.newInstance();
				userContext.initialize(gridService);
			} catch (Exception ex) {
				Logger.error("Error occurred creating UserContext.", ex);
			}
		}

		return userContext;
	}

	public IDataContext createDataContext(IGridService gridService) {
		IDataContext dataContext = null;
		if (dataContextClass != null) {
			try {
				dataContext = dataContextClass.newInstance();
				dataContext.initialize(gridService);
			} catch (Exception ex) {
				Logger.error("Error occurred creating DataContext.", ex);
			}
		}

		return dataContext;
	}

	public abstract AppInfo getAppInfo(String appId);

	public abstract AppInfo getAppInfo(String appId, boolean fromRemote);

	public void addPado(Pado pado) {
		Map<String, Pado> userMap = padoMap.get(pado.getAppId());
		if (userMap == null) {
			userMap = new HashMap<String, Pado>(2);
			padoMap.put(pado.getAppId(), userMap);
		}
		synchronized (userMap) {
			userMap.put(pado.getUsername(), pado);
		}
	}

	/**
	 * Returns the first Pado that matches the specified appId. It returns null
	 * if not found.
	 * 
	 * @param appId
	 *            Application ID.
	 */
	public Pado getPado(String appId) {
		Map<String, Pado> userMap = padoMap.get(appId);
		if (userMap != null) {
			synchronized (userMap) {
				for (Pado pado : userMap.values()) {
					if (pado.isLoggedOut() == false) {
						return pado;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Removes the specified Pado from the client manager only if
	 * pado.isLoggedout() is true. In other words, if the specified Pado has not
	 * been logged out then it cannot be removed from the client manager.
	 * 
	 * @param pado
	 *            Pado instance
	 */
	public void removePado(Pado pado) {
		if (pado == null || pado.isLoggedOut() == false) {
			return;
		}
		Map<String, Pado> userMap = padoMap.get(pado.getAppId());
		if (userMap != null) {
			synchronized (userMap) {
				userMap.remove(pado.getUsername());
			}
		}
	}

	public void refresh(String appId) {
		Map<String, Pado> userMap = padoMap.get(appId);
		if (userMap != null) {
			for (Pado pado : userMap.values()) {
				pado.refresh();
			}
		}

		// if appId is "sys" and this VM is a part of a grid then refresh its
		// catalog.
		if (PadoServerManager.getPadoServerManager() != null
				&& PadoServerManager.getPadoServerManager().getCatalog() != null && appId.equals("sys")) {
			PadoServerManager.getPadoServerManager().getCatalog().refresh();
		}

		// Refresh the routing table
		GridRoutingTable routingTable = GridRoutingTable.getGridRoutingTable(appId);
		if (routingTable != null) {
			routingTable.refresh();
		}
	}

	/**
	 * Removes the app. It removes all associated Pado instances.
	 * 
	 * @param appId
	 *            Application ID
	 */
	public void removeApp(String appId) {
		padoMap.remove(appId);
		GridRoutingTable.removeGridRoutingTable(appId);
	}

	public void refreshGridRoutingTables() {
		// Remove all grid IDs that do not exist in the live grid id list
		// from the current routing table
		Set<String> appIdSet = GridRoutingTable.getAppIdSet();
		for (String appId : appIdSet) {
			GridRoutingTable routingTable = GridRoutingTable.getGridRoutingTable(appId);
			Set<String> routingTableGridIdSet = routingTable.getGridIdSet();
			Set<String> liveGridIdSet = getLiveGridIdSet();
			ArrayList<String> removalList = new ArrayList<String>(20);
			for (String gridId : routingTableGridIdSet) {
				if (liveGridIdSet.contains(gridId) == false) {
					removalList.add(gridId);
				}
			}
			for (String gridId : removalList) {
				routingTable.removeGrid(gridId);
			}
		}

		// Rebuild all routing tables by refreshing them
		for (String appId : appIdSet) {
			GridRoutingTable routingTable = GridRoutingTable.getGridRoutingTable(appId);
			routingTable.refresh();
		}
	}

	/**
	 * Returns all live grid IDs that are assigned to all apps that this VM has
	 * registered via Pado logins. It always returns a non-null set.
	 */
	public abstract Set<String> getLiveGridIdSet();

	/**
	 * Returns live grid IDs of the specified appId. It returns an empty set if
	 * the app Id doesn't exist. Alwas returns a non-null set.
	 * 
	 * @param appId
	 *            Application ID
	 */
	public Set<String> getLiveGridIdSet(String appId) {
		AppInfo appInfo = getAppInfo(appId);
		if (appInfo != null) {
			return appInfo.getGridIdSet();
		}
		return Collections.EMPTY_SET;
	}

	/**
	 * Clears all app IDs, resulting an empty manager.
	 */
	public void clear() {
		padoMap.clear();
	}
}
