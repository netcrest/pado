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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.biz.IVirtualPathBiz;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.pql.VirtualCompiledUnit2;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.server.VirtualPathEngine;

@SuppressWarnings({ "rawtypes" })
public class VirtualPathBizImpl
{
	@Resource
	IBizContextServer bizContext;

	@BizMethod
	public List __execute(String virtualPath, int depth, long validAtTime, long asOfTime, String... args)
	{
		VirtualPathEngine vpe = VirtualPathEngine.getVirtualPathEngine();
		VirtualCompiledUnit2 vcu = vpe.getVirtualCompiledUnit(virtualPath);
		if (vcu != null && vcu.isEntity()) {
			List results = vpe.executeEntity(virtualPath, validAtTime, asOfTime, args);
			if (results != null && depth != 0) {
				if (results.size() > 0) {
					if (results.get(0) instanceof KeyMap) {
						if (depth > IVirtualPathBiz.MAX_DEPTH) {
							depth = IVirtualPathBiz.MAX_DEPTH;
						} else if (depth < -1) {
							depth = -1;
						}
						VirtualPathEntityFinder finder = new VirtualPathEntityFinder();
						for (Object obj : results) {
							KeyMap keyMap = (KeyMap) obj;
							// Following returns the same keyMap.
							finder.getReferences(PadoServerManager.getPadoServerManager().getCatalog(), virtualPath,
									keyMap, depth, validAtTime, asOfTime, null);
						}
					}

				}
			}
			return results;
		} else {
			return vpe.execute(virtualPath, validAtTime, asOfTime, args);
		}
	}

	@BizMethod
	public List __executeEntity(String virtualPath, int depth, long validAtTime, long asOfTime, String... args)
	{
		VirtualPathEngine vpe = VirtualPathEngine.getVirtualPathEngine();
		List results = vpe.executeEntity(virtualPath, validAtTime, asOfTime, args);
		if (results != null && depth != 0) {
			if (results.size() > 0) {
				if (results.get(0) instanceof KeyMap) {
					if (depth > IVirtualPathBiz.MAX_DEPTH) {
						depth = IVirtualPathBiz.MAX_DEPTH;
					} else if (depth < -1) {
						depth = -1;
					}
					VirtualPathEntityFinder finder = new VirtualPathEntityFinder();
					for (Object obj : results) {
						KeyMap keyMap = (KeyMap) obj;
						// Following returns the same keyMap.
						finder.getReferences(PadoServerManager.getPadoServerManager().getCatalog(), virtualPath, keyMap,
								depth, validAtTime, asOfTime, null);
					}
				}
			}
		}
		return results;
	}

	@BizMethod
	public List __executeVirtualPathDefinition(KeyMap vpd, int depth, long validAtTime, long asOfTime, String... args)
	{
		VirtualPathEngine vpe = VirtualPathEngine.getVirtualPathEngine();
		List results = vpe.executeVirtualPathDefinition(vpd, validAtTime, asOfTime, args);
		if (results != null && depth != 0) {
			if (results.size() > 0) {
				if (results.get(0) instanceof KeyMap) {
					if (depth > IVirtualPathBiz.MAX_DEPTH) {
						depth = IVirtualPathBiz.MAX_DEPTH;
					} else if (depth < -1) {
						depth = -1;
					}
					VirtualPathEntityFinder finder = new VirtualPathEntityFinder();
					for (Object obj : results) {
						KeyMap keyMap = (KeyMap) obj;
						// Following returns the same keyMap.
						finder.getReferences(PadoServerManager.getPadoServerManager().getCatalog(), vpd, keyMap,
								depth, validAtTime, asOfTime, null);
					}
				}
			}
		}
		return results;
	}

	@BizMethod
	public KeyMap getVirtualPathDefinition(String virtualPath)
	{
		return KeyTypeManager.getVirtualPathDefinition(virtualPath);
	}

	@BizMethod
	public void addVirtualPathDefinition(KeyMap virtualPathDefinition) throws IOException
	{
		String dbDir = PadoUtil.getProperty(Constants.PROP_DB_DIR);
		KeyTypeManager.registerVirtualPath(dbDir, virtualPathDefinition, true);
	}

	@BizMethod
	public void removeVirtualPathDefinition(String virtualPath)
	{
		String dbDir = PadoUtil.getProperty(Constants.PROP_DB_DIR);
		KeyTypeManager.removeVirtualPath(dbDir, virtualPath);
	}

	@BizMethod
	public String[] getAllVirtualPaths()
	{
		return KeyTypeManager.getAllVirtualPaths();
	}

	@BizMethod
	public Map<String, KeyMap> getAllVirtualPathDefinitions()
	{
		return KeyTypeManager.getAllVirtualPathDefinitions();
	}
}
