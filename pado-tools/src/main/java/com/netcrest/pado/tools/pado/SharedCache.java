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
package com.netcrest.pado.tools.pado;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.mon.ISysBiz;
import com.netcrest.pado.gemfire.info.GemfireGridInfo;
import com.netcrest.pado.info.CacheInfo;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.PadoInfo;
import com.netcrest.pado.info.PathInfo;
import com.netcrest.pado.internal.impl.PadoClientManager;

public class SharedCache
{
	private final static SharedCache sharedCache = new SharedCache();

	private String locators = null;
	private String appId = null;
	private String domain = null;
	private String user = null;
	private char[] password = null;

	private IPado pado;
	private ISysBiz sysBiz;
	private Map<String, List<CacheInfo>> cacheInfoMap;
	private PathInfo rootPathInfo;
	private PadoInfo hostPadoInfo;
	
	private Map<String, BufferInfo> bufferInfoMap = Collections.synchronizedSortedMap(new TreeMap<String, BufferInfo>());

	private SharedCache()
	{
	}

	public static SharedCache getSharedCache()
	{
		return sharedCache;
	}

	public void refresh()
	{
		refresh(true);
	}

	private void refresh(boolean refreshPado)
	{
		if (refreshPado && pado != null) {
			pado.refresh();
			sysBiz = (ISysBiz) pado.getCatalog().newInstance(ISysBiz.class);
		}
		if (sysBiz != null) {
			sysBiz.getBizContext().reset();
			// 4 sec
			hostPadoInfo = sysBiz.getPadoInfo(pado.getAppId());
			// // 10 sec
			// PadoInfoManager.getPadoInfoManager().setPadoInfo(padoInfo);

			// Set the CacheInfo list to GridInfo of each child. The GridInfo
			// obtained
			// from Pado (parent) does not contain the list.
			// TODO: This is an expensive call. It invokes all servers in all
			// grids.
			// 3 sec (14 sec w/ 12 grid1 servers)
			try {
				cacheInfoMap = sysBiz.getMapOfCacheInfoList();
			} catch (Exception ex) {
				// ignore
			}
			GridInfo childGridInfos[] = hostPadoInfo.getChildGridInfos();
			for (int i = 0; i < childGridInfos.length; i++) {
				GridInfo gridInfo = (GridInfo) childGridInfos[i];
				List<CacheInfo> cacheInfoList = cacheInfoMap.get(gridInfo.getGridId());
				gridInfo.setCacheInfoList(cacheInfoList);
			}

			// Set the CacheInfo list for the parent (pado)
			sysBiz.getBizContext().getGridContextClient().setGridIds(hostPadoInfo.getGridId());
			List<CacheInfo> cacheInfoList = sysBiz.getCacheInfoList();
			((GemfireGridInfo) hostPadoInfo.getGridInfo()).setCacheInfoList(cacheInfoList);

			// Refresh the the routing table. Note that there is only one
			// routing table
			// for SharedCache. Also, note that PadoClient.refresh(appId)
			// shouldn't be called here since it also refreshes pado.
			// 3 sec
			PadoClientManager.getPadoClientManager().refreshGridRoutingTables();

			// Create the root that contains all PadoInfo objects from all
			// grids.
			rootPathInfo = new RootPathInfo(hostPadoInfo);
		}
	}

	public IPado getPado()
	{
		return pado;
	}

	/**
	 * Returns the PathInfo object matching the specified full path. It returns
	 * null if the specified full path is not found.
	 * 
	 * @param fullPath
	 *            Full path beginning with "/".
	 */
	public PathInfo getPathInfo(String fullPath)
	{
		if (sysBiz == null || fullPath == null) {
			return null;
		}
		if (fullPath.equals("/")) {
			return rootPathInfo;
		} else {
			String gridId = sysBiz.getBizContext().getGridService().getGridId(fullPath);
			GridInfo gridInfo = hostPadoInfo.getGridInfo(gridId);
			if (gridInfo == null) {
				return null;
			}
			CacheInfo cacheInfo = gridInfo.getCacheInfo();
			if (cacheInfo == null) {
				return null;
			}
			return cacheInfo.getPathInfo(fullPath);
		}
	}

	/**
	 * Returns true if the specified full path exists
	 * 
	 * @param fullPath
	 *            Full path
	 */
	public boolean isFullPathExist(String fullPath)
	{
		return getPathInfo(fullPath) != null;
	}

	public boolean login(String locators, String appId, String domain, String user, char[] pw)
	{
		if (isLoggedIn()) {
			return false;
		}
		
		this.locators = locators;
		reconnect();
		this.locators = Pado.getLocators();
		this.pado = Pado.login(appId, domain, user, pw);
		this.appId = pado.getAppId();
		this.user = pado.getUsername();
		this.domain = domain;
		this.password = pw;

		refresh();
		return isLoggedIn();
	}

	public void login()
	{
		connect();
		if (pado == null || pado.isLoggedOut()) {
			pado = Pado.login(appId, domain, user, password);
		}
	}

	public void logout()
	{
		if (pado != null && pado.isLoggedOut() == false) {
			pado.logout();
			reset();
		}
	}

	public void relogin()
	{
		disconnect();
		login();
	}

	public void connect()
	{
		if (Pado.isClosed()) {
			Pado.connect(locators, false);
		}
	}

	public void disconnect()
	{
		logout();
		if (Pado.isClosed() == false) {
			Pado.close();
		}
	}

	public void reconnect()
	{
		disconnect();
		connect();
	}

	public String getLocators()
	{
		return locators;
	}

	public void setLocators(String locators)
	{
		this.locators = locators;
	}

	public String getUsername()
	{
		return user;
	}

	public String getAppId()
	{
		return appId;
	}

	public String getPadoUrlString(boolean maskPassword)
	{
		String pw;
		if (maskPassword || password == null) {
			pw = "****";
		} else {
			pw = password.toString();
		}
		String url = "pado://" + getAppId() + ":" + getUsername() + ":" + pw + "@" + getLocators();
		return url;
	}

	public boolean isConnected()
	{
		return Pado.isClosed() == false;
	}

	public boolean isLoggedIn()
	{
		return pado != null && pado.isLoggedOut() == false;
	}

	public ISysBiz getSysBiz()
	{
		return sysBiz;
	}
	
	public PadoInfo getHostPadoInfo()
	{
		return hostPadoInfo;
	}

	public GridInfo getGridInfo(String gridId)
	{
		return hostPadoInfo.getGridInfo(gridId);
	}

	public String getGridId(String fullPath)
	{
		if (isLoggedIn() && sysBiz != null) {
			return sysBiz.getBizContext().getGridService().getGridId(fullPath);
		} else {
			return null;
		}
	}

	public String getFullPath(String gridId, String path)
	{
		if (isLoggedIn() && sysBiz != null) {
			return sysBiz.getBizContext().getGridService().getFullPath(gridId, path);
		} else {
			return null;
		}
	}
	
	public BufferInfo getBufferInfo(String bufferName)
	{
		return bufferInfoMap.get(bufferName);
	}
	
	public void putBufferInfo(String bufferName, BufferInfo bufferInfo)
	{
		bufferInfoMap.put(bufferName, bufferInfo);
	}
	
	public void deleteBufferInfo(String bufferInfoName)
	{
		bufferInfoMap.remove(bufferInfoName);
	}
	
	public Set<String> getBufferInfoNameSet()
	{
		return Collections.unmodifiableSet(bufferInfoMap.keySet());
	}
	
	public Map<String, BufferInfo> getBufferInfoMap()
	{
		return bufferInfoMap;
	}
	
	/**
	 * Returns the home directory path which is the root directory of the
	 * host grid if logged on. Otherwise, "/" is the home directory.
	 */
	public String getHomePath()
	{
		if (isLoggedIn() && hostPadoInfo != null) {
			return hostPadoInfo.getGridInfo().getRootPathInfo().getFullPath();
		} else {
			return "/";
		}
	}
	
	public void reset()
	{	
		bufferInfoMap.clear();
		if (isLoggedIn() == false) {
//			locators = null;
//			appId = null;
//			user = null;
//			password = null;
			pado = null;
			sysBiz = null;
			cacheInfoMap = null;
			rootPathInfo = null;
			hostPadoInfo = null;
		}
	}
}
