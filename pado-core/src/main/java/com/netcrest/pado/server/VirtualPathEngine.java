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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.pql.CompiledUnit;
import com.netcrest.pado.pql.VirtualPath;

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
			Logger.severe("Unable to create VirtualPathEngine. VirtualPathEngine must be created before this Pado server can continue.", e);
			System.exit(-1);
		}
	}

	private ConcurrentHashMap<String, VirtualPath> vpMap = new ConcurrentHashMap<String, VirtualPath>();

	public static VirtualPathEngine getVirtualPathEngine()
	{
		return vpe;
	}

	public void addVirtualPathDefinition(KeyMap vpd)
	{
		String path = (String) vpd.get("VirtualPath");
		if (path == null) {
			return;
		}
		ICatalog catalog = PadoServerManager.getPadoServerManager().getCatalog();
		int poolSize = Runtime.getRuntime().availableProcessors();
		VirtualPath vp = new VirtualPath(vpd, catalog, poolSize);
		vpMap.put(path, vp);
	}

	public void removeVirtualPathDefinition(String virtualPath)
	{
		if (virtualPath == null) {
			return;
		}
		VirtualPath vp = vpMap.remove(virtualPath);
		if (vp != null) {
			vp.close();
		}
	}

	public void reset()
	{
		clear();
		if (PadoServerManager.getPadoServerManager() == null) {
			return;
		}
		ICatalog catalog = PadoServerManager.getPadoServerManager().getCatalog();
		int poolSize = Runtime.getRuntime().availableProcessors();
		Map<String, KeyMap> map = KeyTypeManager.getAllVirtualPathDefinitions();
		Set<Map.Entry<String, KeyMap>> set = map.entrySet();
		for (Map.Entry<String, KeyMap> entry : set) {
			String path = entry.getKey();
			KeyMap vpd = entry.getValue();
			VirtualPath vp = new VirtualPath(vpd, catalog, poolSize);
			vpMap.put(path, vp);
		}
	}

	public List execute(String virtualPath, Object input, long validAtTime, long asOfTime)
	{
		VirtualPath vp = vpMap.get(virtualPath);
		if (vp == null) {
			return null;
		}
		return vp.execute(input, validAtTime, asOfTime);
	}

	public List execute(String pql, long validAtTime, long asOfTime)
	{
		CompiledUnit cu = new CompiledUnit(pql);
		String paths[] = cu.getPaths();
		if (paths == null || paths.length < 1) {
			return null;
		}
		String virtualPath = paths[0];
		VirtualPath vp = vpMap.get(virtualPath);
		if (vp == null) {
			return null;
		}
		String input = cu.getCompiledQuery();
		return vp.execute(input, validAtTime, asOfTime);
	}
	
//	public List<TemporalEntry<ITemporalKey, ITemporalData>> executeEntries(String pql, long validAtTime, long asOfTime)
//	{
//		CompiledUnit cu = new CompiledUnit(pql);
//		String paths[] = cu.getPaths();
//		if (paths == null || paths.length < 1) {
//			return null;
//		}
//		String virtualPath = paths[0];
//		VirtualPath vp = vpMap.get(virtualPath);
//		if (vp == null) {
//			return null;
//		}
//		String input = cu.getCompiledQuery();
//		return vp.execute(input, validAtTime, asOfTime);
//	}

	public boolean isVirtualPath(String gridPath)
	{
		if (gridPath == null) {
			return false;
		}
		return vpMap.containsKey(gridPath);
	}

	public void clear()
	{
		vpMap.clear();
	}
}
