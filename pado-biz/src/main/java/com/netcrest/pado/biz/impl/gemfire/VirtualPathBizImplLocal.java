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

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IPado;
import com.netcrest.pado.IVirtualPath;
import com.netcrest.pado.biz.IVirtualPathBiz;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.internal.pql.antlr4.LocalSrollableResultSet;
import com.netcrest.pado.pql.VirtualCompiledUnit2;
import com.netcrest.pado.pql.VirtualPath;

public class VirtualPathBizImplLocal<T> implements IVirtualPathBiz<T>, IBizLocal
{
	@Resource
	IVirtualPathBiz<T> biz;
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
	@Override
	public List<T> __execute(String virtualPath, int depth, long validAt, long asOf, String... args)
	{
		return biz.__execute(virtualPath, depth, validAt, asOf, args);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<T> __executeEntity(String virtualPath, int depth, long validAt, long asOf, String... args)
	{
		return biz.__executeEntity(virtualPath, depth, validAt, asOf, args);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<T> __executeVirtualPathDefinition(KeyMap vpd, int depth, long validAt, long asOf, String... args)
	{
		return __executeVirtualPathDefinition(vpd, depth, validAt, asOf, args);
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
		
		// See if this throws an exception for invalid entity PQL
		new VirtualCompiledUnit2(virtualPathDefinition);
		
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
	/**
	 * {@inheritDoc}
	 */
	@Override
	public IScrollableResultSet<T> execute(String virtualPath, long validAt, long asOf, String... args)
	{
		return execute(virtualPath, 0, validAt, asOf, args);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public IScrollableResultSet<T> execute(String virtualPath, int depth, long validAt, long asOf, String... args)
	{
		if (depth < -1) {
			depth = -1;
		} else if (depth > MAX_DEPTH) {
			depth = MAX_DEPTH;
		}
		List<T> results = biz.__execute(virtualPath, depth, validAt, asOf, args);
		if (results == null) {
			return null;
		} else {
			GridQuery gridQuery = new GridQuery();
			String gridIds[] = getBizContext().getGridContextClient().getGridIds();
			gridQuery.setGridIds(gridIds);
			if (gridIds != null && gridIds.length>0) {
				gridQuery.setFullPath(getBizContext().getGridService().getFullPath(gridIds[0], virtualPath));
			}
			String queryId = virtualPath + "?";
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					if (i > 0) {
						queryId += ",";
					}
					queryId += args[i];
				}
			}
			gridQuery.setId(queryId);
			return new LocalSrollableResultSet<T>(gridQuery, results);
		}
	}
	
	@Override
	public IScrollableResultSet<T> executeEntity(String virtualPath, long validAt, long asOf, String... args)
	{
		return executeEntity(virtualPath, 0, validAt, asOf, args);
	}
	
	@Override
	public IScrollableResultSet<T> executeEntity(String virtualPath, int depth, long validAt, long asOf, String... args)
	{
		if (depth < -1) {
			depth = -1;
		} else if (depth > MAX_DEPTH) {
			depth = MAX_DEPTH;
		}
		
		List<T> results = biz.__executeEntity(virtualPath, depth, validAt, asOf, args);
		if (results == null) {
			return null;
		} else {
			GridQuery gridQuery = new GridQuery();
			String gridIds[] = getBizContext().getGridContextClient().getGridIds();
			gridQuery.setGridIds(gridIds);
			if (gridIds != null && gridIds.length>0) {
				gridQuery.setFullPath(getBizContext().getGridService().getFullPath(gridIds[0], virtualPath));
			}
			String queryId = "entity." + virtualPath + "?";
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					if (i > 0) {
						queryId += ",";
					}
					queryId += args[i];
				}
			}
			gridQuery.setId(queryId);
			return new LocalSrollableResultSet<T>(gridQuery, results);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public IScrollableResultSet<T> executeVirtualPathDefinition(KeyMap vpd, int depth, long validAt, long asOf, String... args)
	{
		String virtualPath = (String)vpd.get("VirtualPath");
		if (virtualPath == null) {
			return null;
		}
		List<T> results = biz.__executeVirtualPathDefinition(vpd, depth, validAt, asOf, args);
		if (results == null) {
			return null;
		} else {
			
			GridQuery gridQuery = new GridQuery();
			String gridIds[] = getBizContext().getGridContextClient().getGridIds();
			gridQuery.setGridIds(gridIds);
			if (gridIds != null && gridIds.length>0) {
				gridQuery.setFullPath(getBizContext().getGridService().getFullPath(gridIds[0], virtualPath));
			}
			String queryId = "entity." + virtualPath + "?";
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					if (i > 0) {
						queryId += ",";
					}
					queryId += args[i];
				}
			}
			gridQuery.setId(queryId);
			return new LocalSrollableResultSet<T>(gridQuery, results);
		}
	}
}
