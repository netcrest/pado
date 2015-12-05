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

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IGridRouter;
import com.netcrest.pado.annotation.OnPath;
import com.netcrest.pado.biz.gemfire.client.proxy.handlers.GemfireClientBizInvocationHandler;
import com.netcrest.pado.gemfire.GemfireGridService;

public class OnRegionFunctionRouter implements FunctionRouter
{
	/**
	 * @param routingKeySet routing keys that pertain to a single grid
	 */
	@Override
	public Execution addRoutingContext (Annotation a, GemfireClientBizInvocationHandler handler, String gridId, 
			IBizContextClient bizContext, Set routingKeySet, Object[] args)
	{
		OnPath or = (OnPath) a;
		GemfireGridService gridService = (GemfireGridService)bizContext.getGridService();

		String gridPath = bizContext.getGridContextClient().getGridPath();
		if (gridPath == null) {
			gridPath = or.path();
		}
		if (gridPath == null) {
			gridPath = handler.getDefaultGridPath();
		}
		
		// Determine the grid ID using the first item in the passed-in routing key set.
		// If not defined then use the passed-in grid ID.
		IGridRouter router = gridService.getGridRouter(gridPath);
		if (routingKeySet != null && router != null) {
			for (Object key : routingKeySet) {
				gridId = router.getReachableGridIdForPath(bizContext, key);
				break;
			}
		}
		Region region = gridService.getRegion(gridId, gridPath);
		if (region == null) {
			return null;
		}

		// Create exec
		Execution exec = FunctionService.onRegion(region);
		
		if (routingKeySet != null) {
			exec = exec.withFilter(routingKeySet);
		}
		return exec;

	}
}
