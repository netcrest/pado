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
package com.netcrest.pado.gemfire;

import com.gemstone.gemfire.cache.Region;
import com.netcrest.pado.ICatalog;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.server.VirtualPathEngine;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class GemfireVirtualPathEngine extends VirtualPathEngine
{	
	@Override
	public void addVirtualPathDefinition(ICatalog catalog, KeyMap vpd)
	{
		if (vpd == null) {
			return;
		}
		String virtualPath = (String) vpd.get("VirtualPath");
		if (virtualPath == null) {
			return;
		}
		super.addVirtualPathDefinition(catalog, vpd);
		
		if (PadoUtil.isPureClient() == false) {
			Region<String, KeyMap> vpRegion = GemfirePadoServerManager.getPadoServerManager().getVirtualPathRegion();
			vpRegion.put(virtualPath, vpd);
		}
	}

	@Override
	public void removeVirtualPathDefinition(String virtualPath)
	{
		super.removeVirtualPathDefinition(virtualPath);
		Region<String, KeyMap> vpRegion = GemfirePadoServerManager.getPadoServerManager().getVirtualPathRegion();
		vpRegion.remove(virtualPath);	
	}

	@Override
	public void reset()
	{
		if (GemfirePadoServerManager.getPadoServerManager() != null && GemfirePadoServerManager.getPadoServerManager().isMaster()) {
			Region<String, KeyMap> vpRegion = GemfirePadoServerManager.getPadoServerManager().getVirtualPathRegion();
			vpRegion.clear();
		}
		super.reset();
	}
}
