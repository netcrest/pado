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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.biz.util.BizThreadPool;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.exception.PadoServerException;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.pql.antlr4.PqlEvalDriver;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.pql.CompiledUnit;
import com.netcrest.pado.pql.VirtualCompiledUnit2;
import com.netcrest.pado.pql.VirtualPath2;
import com.netcrest.pado.temporal.ITemporalBizLink;

@SuppressWarnings("rawtypes")
public class VirtualPathEngine
{
	protected static VirtualPathEngine vpe;

	static {
		try {
			Class clazz = PadoUtil.getClass(Constants.PROP_CLASS_VIRTUAL_PATH_ENGINE,
					Constants.DEFAULT_CLASS_VIRTUAL_PATH_ENGINE);
			vpe = (VirtualPathEngine) clazz.newInstance();
			vpe.reset();
		} catch (Exception e) {
			Logger.severe(
					"Unable to create VirtualPathEngine. VirtualPathEngine must be created before this Pado server can continue.",
					e);
			System.exit(-1);
		}
	}

	private BizThreadPool<ITemporalBizLink> temporalBizThreadPool = new BizThreadPool<ITemporalBizLink>(
			PadoServerManager.getPadoServerManager().getCatalog(), "com.netcrest.pado.biz.ITemporalBiz");
	private ConcurrentHashMap<String, VirtualPath2> vpMap = new ConcurrentHashMap<String, VirtualPath2>();
	private ConcurrentHashMap<String, VirtualCompiledUnit2> vcuMap = new ConcurrentHashMap<String, VirtualCompiledUnit2>();

	public static VirtualPathEngine getVirtualPathEngine()
	{
		return vpe;
	}

	public void addVirtualPathDefinition(ICatalog catalog, KeyMap vpd)
	{
		String path = (String) vpd.get("VirtualPath");
		if (path == null) {
			return;
		}
		int poolSize = Runtime.getRuntime().availableProcessors();
		VirtualPath2 vp = new VirtualPath2(vpd, catalog, poolSize);
		vpMap.put(path, vp);
		Boolean isEntity = (Boolean) vpd.get("IsEntity");
		if (isEntity == null) {
			isEntity = false;
		}
		if (isEntity) {
			VirtualCompiledUnit2 vcu = new VirtualCompiledUnit2(vpd);
			vcuMap.put(vcu.getVirtualPath(), vcu);
		}
	}

	public void removeVirtualPathDefinition(String virtualPath)
	{
		if (virtualPath == null) {
			return;
		}
		VirtualPath2 vp = vpMap.remove(virtualPath);
		if (vp != null) {
			vp.remove();
			vp.close();
		}
		vcuMap.remove(virtualPath);
	}

	public void reset()
	{
		clear();
		if (PadoServerManager.getPadoServerManager() == null) {
			return;
		}
		ICatalog catalog = PadoServerManager.getPadoServerManager().getCatalog();
		// int poolSize = Runtime.getRuntime().availableProcessors();
		Map<String, KeyMap> map = KeyTypeManager.getAllVirtualPathDefinitions();
		Set<Map.Entry<String, KeyMap>> set = map.entrySet();
		for (Map.Entry<String, KeyMap> entry : set) {
			// String path = entry.getKey();
			KeyMap vpd = entry.getValue();
			// VirtualPath2 vp = new VirtualPath2(vpd, catalog, poolSize);
			// vpMap.put(path, vp);
			addVirtualPathDefinition(catalog, vpd);
		}
	}

	public VirtualCompiledUnit2 getVirtualCompiledUnit(String virtualPath)
	{
		return vcuMap.get(virtualPath);
	}
	
	public VirtualPath2 getVirtualPath(String virtualPath)
	{
		return vpMap.get(virtualPath);
	}

	public List execute(String pql, long validAtTime, long asOfTime)
	{
		CompiledUnit cu = new CompiledUnit(pql);
		String paths[] = cu.getPaths();
		if (paths == null || paths.length < 1) {
			return null;
		}
		String virtualPath = paths[0];
		VirtualPath2 vp = vpMap.get(virtualPath);
		if (vp == null) {
			return null;
		}
//		String input = cu.getCompiledQuery();
		String[] argValues = cu.getArgValues();

		// TODO: Verify input shouldn't be passed in. Broken?
		return vp.execute(validAtTime, asOfTime, argValues);
	}

	public List execute(String virtualPath, long validAtTime, long asOfTime, String... args)
	{
		VirtualPath2 vp = vpMap.get(virtualPath);
		if (vp == null) {
			return null;
		}
		return vp.execute(validAtTime, asOfTime, args);
	}

	public List executeEntity(String virtualPath, long validAtTime, long asOfTime, String... args)
	{
		VirtualPath2 vp = vpMap.get(virtualPath);
		if (vp == null) {
			return null;
		}
		return vp.executeEntity(validAtTime, asOfTime, args);
	}

	/**
	 * Executes the virtual path query. Returns zero (0) depth if the virtual
	 * path is an entity.
	 * 
	 * @param vpd
	 *            Virtual path defintion
	 * @param validAtTime
	 *            Valid at time
	 * @param asOfTime
	 *            As of time
	 * @param args
	 *            Virtual path arguments
	 * @throws PadoServerException
	 *             Thrown if an error occurs
	 */
	public List executeVirtualPathDefinition(KeyMap vpd, long validAtTime, long asOfTime, String... args)
			throws PadoServerException
	{
		Boolean isEntity = (Boolean) vpd.get("IsEntity");
		if (isEntity != null && isEntity) {
			VirtualCompiledUnit2 vcu = new VirtualCompiledUnit2(vpd);
			String entityPql = vcu.getEntityPql(args);
			ITemporalBizLink temporalBiz = temporalBizThreadPool.getBiz();
			return temporalBiz.getQueryValues(entityPql, validAtTime, asOfTime);
		} else {
			String queryString = (String) vpd.get("Query");
			ITemporalBizLink temporalBiz = temporalBizThreadPool.getBiz();
			try {
				return PqlEvalDriver.executeValues(temporalBiz, validAtTime, asOfTime, queryString, args);
			} catch (IOException e) {
				throw new PadoServerException(e);
			}
		}
	}

	// public List<TemporalEntry<ITemporalKey, ITemporalData>>
	// executeEntries(String pql, long validAtTime, long asOfTime)
	// {
	// CompiledUnit cu = new CompiledUnit(pql);
	// String paths[] = cu.getPaths();
	// if (paths == null || paths.length < 1) {
	// return null;
	// }
	// String virtualPath = paths[0];
	// VirtualPath vp = vpMap.get(virtualPath);
	// if (vp == null) {
	// return null;
	// }
	// String input = cu.getCompiledQuery();
	// return vp.execute(input, validAtTime, asOfTime);
	// }

	public boolean isVirtualPath(String gridPath)
	{
		if (gridPath == null) {
			return false;
		}
		return vpMap.containsKey(gridPath);
	}
	
	public boolean isEntityVirtualPath(String gridPath)
	{
		if (gridPath == null) {
			return false;
		}
		return vcuMap.containsKey(gridPath);
	}

	public void clear()
	{
		vpMap.clear();
		vcuMap.clear();
		VirtualPath2.reset();
	}
}
