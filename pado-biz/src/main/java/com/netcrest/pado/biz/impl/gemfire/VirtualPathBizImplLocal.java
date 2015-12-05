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
package com.netcrest.pado.biz.impl.gemfire;

import java.util.Map;

import javax.annotation.Resource;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IPado;
import com.netcrest.pado.IVirtualPath;
import com.netcrest.pado.biz.IVirtualPathBiz;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.pql.VirtualPath;

public class VirtualPathBizImplLocal<T> implements IVirtualPathBiz<T>, IBizLocal
{
	@Resource IVirtualPathBiz<T> biz;
	private IPado pado;
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void init(IBiz biz, IPado pado, Object... args)
	{
		this.biz = (IVirtualPathBiz<T>) biz;
		this.pado = pado;
		String gridId = null;
		if (args != null && args.length > 0) {
			gridId = args[0].toString();
		}
		if (gridId == null) {
			gridId = biz.getBizContext().getGridService().getDefaultGridId();
		}
		biz.getBizContext().getGridContextClient().setGridIds(gridId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBizContextClient getBizContext()
	{
		return biz.getBizContext();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public KeyMap getVirtualPathDefinition(String virtualPath)
	{
		return biz.getVirtualPathDefinition(virtualPath);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IVirtualPath<T> getVirtualPath(String virtualPath)
	{
		return getVirtualPath(virtualPath, -1);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	public VirtualPath<T> getVirtualPath(String virtualPath, int threadPoolSize)
	{
		if (virtualPath == null) {
			return null;
		}
		KeyMap vpd = getVirtualPathDefinition(virtualPath);
		if (vpd == null) {
			return null;
		}
		VirtualPath<T> vp = new VirtualPath<T>(vpd, pado, threadPoolSize);
		return vp;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void addVirtualPathDefinition(KeyMap virtualPathDefinition)
	{
		if (virtualPathDefinition == null) {
			return;
		}
		// TODO: validate it first
		biz.addVirtualPathDefinition(virtualPathDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeVirtualPathDefinition(String virtualPath)
	{
		if (virtualPath == null) {
			return;
		}
		biz.removeVirtualPathDefinition(virtualPath);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getAllVirtualPaths()
	{
		return biz.getAllVirtualPaths();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Map<String, KeyMap> getAllVirtualPathDefinitions()
	{
		return biz.getAllVirtualPathDefinitions();
	}

}
