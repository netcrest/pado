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
package com.netcrest.pado.gemfire.biz.server.impl.gemfire;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.CacheFactory;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.IUserPrincipal;
import com.netcrest.pado.biz.server.IPadoBiz;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.gemfire.GemfirePadoServerManager;
import com.netcrest.pado.gemfire.info.GemfireConfigInfo;
import com.netcrest.pado.gemfire.info.GemfireRegionInfo;
import com.netcrest.pado.gemfire.info.GemfireVirtualPathInfo;
import com.netcrest.pado.gemfire.util.GemfireSerializer;
import com.netcrest.pado.info.AppInfo;
import com.netcrest.pado.info.ConfigInfo;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.LoginInfo;
import com.netcrest.pado.info.PathInfo;
import com.netcrest.pado.internal.factory.InfoFactory;
import com.netcrest.pado.io.IObjectSerializer;
import com.netcrest.pado.pql.VirtualPath2;
import com.netcrest.pado.server.PadoServerManager;

public class GemfirePadoBizImpl implements IPadoBiz
{
	private final static String APP_CONFIG_DIR = System.getProperty("pado.appConfigDir", "../../etc/client");
	private final static String APP_MULTI_CONFIG_FILE_PATH = System.getProperty("pado.multiConfigFilePath",
			"../../etc/client/client.xml");

	@Resource
	private IBizContextServer bizContext;

	public IBizContextClient getBizContext()
	{
		return null;
	}
	
	public LoginInfo login(String appId, String domain, String username, char[] password, IUserPrincipal userPrincipal) throws PadoLoginException
	{
		try {
			GemfirePadoServerManager sm = GemfirePadoServerManager.getPadoServerManager();
			LoginInfo loginInfo = sm.login(appId, domain, username, password, userPrincipal);
			return loginInfo;
		} catch (Throwable th) {
			throw new PadoLoginException(th);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LoginInfo login(String appId, String domain, String username, char[] password) throws PadoLoginException
	{
		return login(appId, domain, username, password, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConfigInfo getConfigInfo(String appId, boolean multiAppsEnabled, boolean includeCacheXmlContent)
	{
		GemfireConfigInfo configInfo = null;

		try {
			String xml = null;
			if (includeCacheXmlContent) {
				File file;
				if (multiAppsEnabled) {
					file = new File(APP_MULTI_CONFIG_FILE_PATH);
				} else {
					file = new File(APP_CONFIG_DIR + "/" + appId + ".xml");
				}
				FileReader fr = new FileReader(file);
				BufferedReader reader = new BufferedReader(fr);
				StringBuilder builder = new StringBuilder(1024);
				String line = reader.readLine();
				while (line != null) {
					builder.append(line + "\n");
					line = reader.readLine();
				}
				xml = builder.toString();
				reader.close();
				fr.close();
			}
			GemfirePadoServerManager sm = GemfirePadoServerManager.getPadoServerManager();
			configInfo = (GemfireConfigInfo) InfoFactory.getInfoFactory().createConfigInfo();
			configInfo.setXmlContent(xml);
			configInfo.setClientLocators(sm.getClientLocators());
			configInfo.setClientMultiuserAuthenticationEnabled(sm.isClientConnectionMultiuserAuthenticationEnabled());
			configInfo.setClientIndexMatrixConnectionName(sm.getClientIndexMatrixConnectionName());
			Set<IObjectSerializer> objectSerializerSet = new HashSet<IObjectSerializer>(3);
			objectSerializerSet.add(new GemfireSerializer());

		} catch (FileNotFoundException ex) {

			CacheFactory.getAnyInstance().getLogger().error("PadoBizImpl.getConfigInfo():", ex);

		} catch (IOException ex) {

			CacheFactory.getAnyInstance().getLogger().error("PadoBizImpl.getConfigInfo():", ex);

		}

		return configInfo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConfigInfo getConfigInfo(String appId)
	{
		return getConfigInfo(appId, false, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GridInfo getGridInfo()
	{
		return PadoServerManager.getPadoServerManager().getGridInfo();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getServerCount(String gridId)
	{
		return PadoServerManager.getPadoServerManager().getServerCount(gridId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getServerIds(String gridId)
	{
		return PadoServerManager.getPadoServerManager().getServerIds(gridId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AppInfo getAppInfo(String appId)
	{
		return PadoServerManager.getPadoServerManager().getAppInfo(appId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GemfireRegionInfo getPathInfo()
	{
		GemfirePadoServerManager sm = GemfirePadoServerManager.getPadoServerManager();
		GemfireRegionInfo regionInfo = new GemfireRegionInfo(sm.getRootRegion(), true);
		return regionInfo;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public GemfireVirtualPathInfo getVirtualPathInfo()
	{
		GemfirePadoServerManager sm = GemfirePadoServerManager.getPadoServerManager();
		GemfireVirtualPathInfo virtualPathInfo = new GemfireVirtualPathInfo(VirtualPath2.getRootVirtualPath(), true);
		return virtualPathInfo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getRegisteredMainKeyTypeNames()
	{
		return KeyTypeManager.getAllRegisteredMainKeyTypeNames();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] echo(byte[] payload)
	{
		return payload;
	}
}
