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
package com.netcrest.pado.biz.server.impl.gemfire;

import java.util.Properties;

import javax.annotation.Resource;
import javax.naming.AuthenticationException;

import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.IUserAuthentication;
import com.netcrest.pado.IUserPrincipal;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.biz.server.IPadoBiz;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.gemfire.biz.server.impl.gemfire.GemfirePadoBizImpl;
import com.netcrest.pado.info.AppInfo;
import com.netcrest.pado.info.ConfigInfo;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.LoginInfo;
import com.netcrest.pado.info.PathInfo;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.PadoServerManager;

public class PadoBizImpl implements IPadoBiz
{
	private GemfirePadoBizImpl gemfirePadoBizImpl;
	private IUserAuthentication userAuthentication;
	
	@Resource
	private IBizContextServer bizContext;
	
	
	public PadoBizImpl()
	{
		try {
			Class<?> clazz = getClass().getClassLoader().loadClass(PadoUtil.getProperty(Constants.PROP_CLASS_PADO_BIZ_IMPL, Constants.DEFAULT_CLASS_PADO_BIZ_IMPL));
			gemfirePadoBizImpl = (GemfirePadoBizImpl)clazz.newInstance();
			
			clazz = getClass().getClassLoader().loadClass(PadoUtil.getProperty(Constants.PROP_CLASS_USER_AUTHENTICATION, Constants.DEFAULT_CLASS_USER_AUTHENTICATION));
			if (clazz != null) {
				userAuthentication = (IUserAuthentication)clazz.newInstance();
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}
	
	public IBizContextClient getBizContext()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@Override
	public LoginInfo login(String appId, String domain, String username, char[] password) throws PadoLoginException
	{
		try {
			IUserPrincipal userPrincipal = null;
			if (userAuthentication != null) {
				Properties props = null;
				Object[] args = bizContext.getGridContextServer().getAdditionalArguments();
				if (args != null && args.length >= 1) {
					props = (Properties)args[0];
				}
				userPrincipal = userAuthentication.authenticate(appId, domain, username, password, props);
			}
			return gemfirePadoBizImpl.login(appId, domain, username, password, userPrincipal);
		} catch (AuthenticationException e) {
			throw new PadoLoginException(e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@Override
	public ConfigInfo getConfigInfo(String appId, boolean multiAppsEnabled, boolean includeCacheXmlContent)
	{
		return gemfirePadoBizImpl.getConfigInfo(appId, multiAppsEnabled, includeCacheXmlContent);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@Override
	public ConfigInfo getConfigInfo(String appId)
	{
		return getConfigInfo(appId, false, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@Override
	public GridInfo getGridInfo()
	{
		return PadoServerManager.getPadoServerManager().getGridInfo();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@Override
	public AppInfo getAppInfo(String appId)
	{
		return PadoServerManager.getPadoServerManager().getAppInfo(appId);
	}

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@Override
	public PathInfo getPathInfo()
	{
		return gemfirePadoBizImpl.getPathInfo();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@Override
	public PathInfo getVirtualPathInfo()
	{
		return gemfirePadoBizImpl.getVirtualPathInfo();
	}

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@Override
	public String[] getRegisteredMainKeyTypeNames()
	{
		return KeyTypeManager.getAllRegisteredMainKeyTypeNames();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@Override
	public byte[] echo(byte[] payload)
	{
		return payload;
	}

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	public int getServerCount(String gridId)
	{
		return PadoServerManager.getPadoServerManager().getServerCount(gridId);
	}

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	public Object[] getServerIds(String gridId)
	{
		return PadoServerManager.getPadoServerManager().getServerIds(gridId);
	}
}
