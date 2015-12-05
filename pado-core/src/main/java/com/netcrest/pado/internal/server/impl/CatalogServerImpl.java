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
package com.netcrest.pado.internal.server.impl;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.link.IPadoBizLink;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.info.AppInfo;
import com.netcrest.pado.internal.biz.util.BizUtil;
import com.netcrest.pado.internal.impl.GridService;
import com.netcrest.pado.internal.impl.PadoClientManager;
import com.netcrest.pado.internal.server.BizManager;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.PadoServerManager;

public class CatalogServerImpl implements ICatalog
{
	private static PadoClientManager s_clientManager = PadoClientManager.getPadoClientManager();
	
	private String username = "admin";
	private String appId = "sys";
	private GridService gridService;
	private Set<String> gridIdSet = new TreeSet();
	private PadoServerImpl pado;
	

	public CatalogServerImpl(IPadoBizLink padoBiz) throws PadoLoginException
	{
		pado = new PadoServerImpl(username, null, this);
		gridService = padoBiz.getBizContext().getGridService();
		refresh();
	}
	
	private AppInfo getAppInfo(String appId)
	{
		return s_clientManager.getAppInfo(appId);
	}

	public String getAppId()
	{
		return this.appId;
	}
	
	public GridService getGridService()
	{
		return gridService;
	}
	
	public IPado getPado()
	{
		return pado;
	}
	
	/**
	 * Returns a new instance of the specified biz class. It returns null if
	 * the user does not have proper access rights.
	 */
	@SuppressWarnings("unchecked")
	public <T> T newInstance(Class<T> bizClass, Object... bizLocalArguments)
	{
		BizManager<IBiz> bizManager = getBizManager(bizClass);
		if (bizManager == null) {
			return null;
		}
		
		// If local implementation is defined then return a local instance
		String localImplClassName = BizUtil.getExplicitLocalImplClassName(bizClass);
		if (localImplClassName != null && localImplClassName.length() > 0) {
			try {
				IBizLocal bizLocal = (IBizLocal)bizManager.getTargetClass().getClassLoader().loadClass(localImplClassName).newInstance();
				return (T)newInstance(bizManager, bizLocal, bizLocalArguments);
			} catch (Exception e) {
				Logger.error("Error occurred while instantiating IBizLocal class: " + 
						localImplClassName + ".", e);
				throw new PadoException("Error occurred while instantiating IBizLocal class: " + 
					localImplClassName + ".", e);
			}
		} else {
			try {
				localImplClassName = BizUtil.getImplicitBizLocalImplClassName(bizClass);
				IBizLocal bizLocal = (IBizLocal)bizManager.getTargetClass().getClassLoader().loadClass(localImplClassName).newInstance();
				return (T)newInstance(bizManager, bizLocal, bizLocalArguments);
			} catch (Exception e) {
				return (T)bizManager.newClientProxy();
			}
		}
	}
	
	@Override
	public <T> T newInstanceLocal(Class<T> bizClass, IBizLocal bizLocal, Object... bizLocalArguments)
	{
		BizManager<IBiz> bizManager = this.getBizManager(bizClass);
		if (bizManager == null) {
			return null;
		}
		return (T)newInstance(bizManager, bizLocal, bizLocalArguments);
	}
	
	public IBiz newInstance(String bizInterfaceName, Object...bizLocalArguments)
	{
		BizManager<IBiz> bizManager = this.getGemFireBizManager(bizInterfaceName);
		if (bizManager == null) {
			return null;
		}
		Class<?> bizClass = bizManager.getTargetClass();
		// If local implementation is defined then return a local instance
		String localImplClassName = BizUtil.getExplicitLocalImplClassName(bizClass);
		if (localImplClassName != null && localImplClassName.length() > 0) {
			try {
				Class<?> bizLocalClass = bizManager.getTargetClass().getClassLoader().loadClass(localImplClassName);
				IBizLocal bizLocal = (IBizLocal)bizLocalClass.newInstance();
				return (IBiz)newInstance(bizManager, bizLocal, bizLocalArguments);
			} catch (Exception e) {
				Logger.error("Error occurred while instantiating IBizLocal class: " + 
						localImplClassName + ".", e);
				throw new PadoException("Error occurred while instantiating IBizLocal class: " + 
					localImplClassName + ".", e);
			}
		} else {
			try {
				localImplClassName = BizUtil.getImplicitBizLocalImplClassName(bizClass);
				Class<?> bizLocalClass = bizManager.getTargetClass().getClassLoader().loadClass(localImplClassName);
				IBizLocal bizLocal = (IBizLocal)bizLocalClass.newInstance();
				return (IBiz)newInstance(bizManager, bizLocal);
			} catch (Exception e) {
				return bizManager.newClientProxy();
			}
		}
	}
	
	public IBizLocal newInstanceLocal(String bizInterfaceName, String bizLocalClassName, Object...bizLocalArguments)
	{
		BizManager<IBiz> bizManager = this.getGemFireBizManager(bizInterfaceName);
		if (bizManager == null) {
			return null;
		}
		try {
			Class<?> bizLocalClass = bizManager.getTargetClass().getClassLoader().loadClass(bizLocalClassName);
			IBizLocal bizLocal = (IBizLocal)bizLocalClass.newInstance();
			return newInstance(bizManager, bizLocal, bizLocalArguments);
		} catch (ClassNotFoundException ex) {
			Logger.warning("Undefined IBizLocal class, " + bizLocalClassName + ". Aborted.", ex);
		} catch (Exception ex) {
			Logger.warning(ex);
		}
		return null;
	}
	
	private IBizLocal newInstance(BizManager<IBiz> bizManager, IBizLocal bizLocal, Object...bizLocalArguments)
	{
		IBiz biz = bizManager.newClientProxy();
		for (Field f : bizLocal.getClass().getDeclaredFields()) {
			if (f.getAnnotation(Resource.class) != null && f.getType() == bizManager.getTargetClass()) {
				f.setAccessible(true);
				try {
					f.set(bizLocal, biz);
					break;
				} catch (IllegalArgumentException e) {
					Logger.warning("Failed to set function context on field [" + f.getName() + "]", e);
				} catch (IllegalAccessException e) {
					Logger.warning("Failed to set function context on field [" + f.getName() + "]", e);
				}
			}
		}
		bizLocal.init(biz, pado, bizLocalArguments);
		return bizLocal;
	}
	
	
	/**
	 * Returns the IBiz manager that manages the specified IBiz class.
	 * @param bizClass IBiz interface class
	 */
	private BizManager<IBiz> getBizManager(Class<?> bizClass)
	{
		BizManager<IBiz> bizManager = PadoServerManager.getPadoServerManager().getAppBizManager(bizClass);
		if (bizManager != null) {
			bizManager.setAppId(appId);
			bizManager.setGridService(gridService);
		}
		return bizManager;
	}
	
	private BizManager<IBiz> getGemFireBizManager(String bizInterfaceName)
	{
		BizManager<IBiz> bizManager = PadoServerManager.getPadoServerManager().getAppBizManager(bizInterfaceName);
		if (bizManager != null) {
			bizManager.setAppId(appId);
			bizManager.setGridService(gridService);
		}
		return bizManager;
	}
	
	public String[] getAllBizClassNames()
	{
		return PadoServerManager.getPadoServerManager().getAllAppBizClassNames();
	}

	public Class<?>[] getAllBizClasses()
	{
		return PadoServerManager.getPadoServerManager().getAllAppBizClasses();
	}

	/**
	 * Removes all biz object classes and grid IDs
	 */
	public void clear()
	{
		PadoServerManager.getPadoServerManager().clearBizManagers();
		gridIdSet.clear();
	}
	
	public void refresh()
	{
		gridService.refresh();
	}
	
	/**
	 * Close the catalog. Upon return of this call, the catalog
	 * object is no longer valid. This method is invoked when
	 * the user logs out from Pado.
	 */
	public void close()
	{
		clear();
		gridService = null;
	}

	@Override
	public String[] getGridIds()
	{
		return gridIdSet.toArray(new String[gridIdSet.size()]);
	}
	
	public void addGridId(String gridId)
	{
		gridIdSet.add(gridId);
	}
	
	public void removeGridId(String gridId)
	{
		gridIdSet.remove(gridId);
	}
}
