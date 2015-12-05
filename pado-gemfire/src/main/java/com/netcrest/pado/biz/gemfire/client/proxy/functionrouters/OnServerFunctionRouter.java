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
package com.netcrest.pado.biz.gemfire.client.proxy.functionrouters;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.gemstone.gemfire.cache.RegionService;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.client.PoolManager;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.biz.gemfire.client.proxy.handlers.GemfireClientBizInvocationHandler;
import com.netcrest.pado.gemfire.GemfireGridService;

/**
 * Implementation of a FunctionRouter that will add Gemfire's onServer or
 * onServers routing to the execution context. This class is used by the
 * onServer annotation to provide suitable runtime context configuration.
 */
public class OnServerFunctionRouter implements FunctionRouter
{

	/*
	 * Applies the onServer/onServers configuration to the execution context
	 * being used to invoke the function
	 * 
	 * @see FunctionRouter
	 */
	@Override
	public Execution addRoutingContext(Annotation a, GemfireClientBizInvocationHandler handler, String gridId, IBizContextClient bizContext,
			Set routingKeySet, Object[] args)
	{
		OnServer os = (OnServer) a;

		RegionService regionService = null;
		GemfireGridService gridService = (GemfireGridService)bizContext.getGridService();
		if (gridService != null) {
			regionService = gridService.getRegionService(gridId);
		}

		Pool pool = null;
		if (regionService == null) {
			pool = gridService.getPool(gridId);
			if (pool == null) {
				pool = PoolManager.find(os.connectionName());
				gridService.putPool(gridId, pool);
				// get regionService again in case it is set by gridService for the new pool
				regionService = gridService.getRegionService(gridId);
			}
		}

		if (regionService != null) {
			if (os.broadcast()) {
				return FunctionService.onServers(regionService);
			}
			return FunctionService.onServer(regionService);
		} else {
			// Pool shouldn't be null. If so, GemFire throws a RunTimeException
			if (os.broadcast()) {
				return FunctionService.onServers(pool);
			}
			return FunctionService.onServer(pool);
		}
	}
}
