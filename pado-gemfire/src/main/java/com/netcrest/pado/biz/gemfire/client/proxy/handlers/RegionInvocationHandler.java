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
package com.netcrest.pado.biz.gemfire.client.proxy.handlers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionService;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.netcrest.pado.IGridRouter;
import com.netcrest.pado.exception.BizException;
import com.netcrest.pado.gemfire.GemfireGridService;
import com.netcrest.pado.gemfire.info.GemfireAppGridInfo;
import com.netcrest.pado.info.AppInfo;
import com.netcrest.pado.internal.impl.GridService;

/**
 * Region invocation handler for data-dependent invocations
 */
public class RegionInvocationHandler<T> extends GemfireClientBizInvocationHandler<T> implements InvocationHandler
{

	private Region region;
	private Set<Object> filterset;

	/**
	 * @param targetClass
	 *            The service interface
	 * @param region
	 *            Absolute region
	 */
	public RegionInvocationHandler(Class<T> targetClass, Region region, GridService gridService)
	{
		super(targetClass, gridService);
		this.region = region;
	}

	/**
	 * @param targetClass
	 *            The service interface
	 * @param region
	 *            Absolute region
	 * @param filterSet
	 *            Set of routing keys
	 */
	public RegionInvocationHandler(Class<T> targetClass, Region region, Set<Object> filterSet, GridService gridService)
	{
		this(targetClass, region, gridService);
		this.filterset = filterSet;
	}

	/*
	 * Performs invocation routing based on the specified region and optionally
	 * an set of keys. Will make use of any additional annotations associated
	 * with the class to tailor the invocation
	 * 
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
	 * java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		// First, determine the region. If it's not specified then
		// use the default region defined by AppInfo.
		Region region = this.region;
		String gridId = null;
		if (region == null) {
			AppInfo appInfo = getAppInfo();
			gridId = getGridId(method);
			region = getGridRegion(gridId);
		}
		if (region == null) {
			throw new BizException("Region is not defined. Aborted.");
		}

		// Determine the target grids from IGridRouter if it exists.
		String gridIds[];
		IGridRouter gridRouter = bizContext.getGridService().getGridRouter(region.getFullPath());
		HashMap<String, HashSet> gridRoutingKeyMap = null;
		if (gridRouter != null) {
			gridRoutingKeyMap = new HashMap(10);
			// this.filterset overrides
			// bizContext.getGridContextClient().getRoutingKeys()
			Set routingKeySet = this.filterset;
			if (routingKeySet == null) {
				routingKeySet = bizContext.getGridContextClient().getRoutingKeys();
			}
			for (Object key : routingKeySet) {
				gridId = gridRouter.getReachableGridIdForPath(bizContext, key);
				if (gridId != null) {
					HashSet keySet = gridRoutingKeyMap.get(gridId);
					if (keySet == null) {
						keySet = new HashSet<String>(10);
						gridRoutingKeyMap.put(gridId, keySet);
					}
					keySet.add(key);
				}
			}
			gridIds = gridRoutingKeyMap.keySet().toArray(new String[gridRoutingKeyMap.keySet().size()]);
			
		} else {
			gridIds = bizContext.getGridContextClient().getGridIds();
		}

		if (gridIds == null || gridIds.length == 0) {

			// add the data dependant routing information to the execution
			// context
			Execution primordialExec = null;
			Set routingKeySet = this.filterset;
			if (routingKeySet == null) {
				routingKeySet = bizContext.getGridContextClient().getRoutingKeys();
			}
			if (routingKeySet == null) {
				primordialExec = FunctionService.onRegion(region);
			} else {
				primordialExec = FunctionService.onRegion(region).withFilter(routingKeySet);
			}
			try {
				return doInvocation(gridId, primordialExec, method, args);
			} catch (Throwable th) {
				throw new BizException("Exception occurred while executing \"" + method.toString() + "\" for gridId=" + gridId + ", regionPath=" + region.getFullPath() + ".", th);
			}

		} else {

			for (int i = 0; i < gridIds.length; i++) {
				if (gridIds[i] == null) {
					throw new BizException("Grid ID (" + i
							+ ") is null. Must pass in valid grid IDs. IBiz invocation aborted.");
				}
			}

			ExecutionPair execPairs[] = new ExecutionPair[gridIds.length];
			Execution exec;
			int execPairIndex = 0;
			if (gridRoutingKeyMap != null) {
				for (int i = 0; i < gridIds.length; i++) {
					region = getGridRegion(gridIds[i]);
					if (region != null) {
						Set routingKeySet = gridRoutingKeyMap.get(gridIds[i]);
						if (routingKeySet == null) {
							exec = FunctionService.onRegion(region);
						} else {
							exec = FunctionService.onRegion(region).withFilter(routingKeySet);
						}
						execPairs[execPairIndex++] = new ExecutionPair(gridIds[i], exec);
					}
				}
			} else {
				Set routingKeySet = this.filterset;
				if (routingKeySet == null) {
					routingKeySet = bizContext.getGridContextClient().getRoutingKeys();
				}
				for (int i = 0; i < gridIds.length; i++) {
					region = getGridRegion(gridIds[i]);
					if (region != null) {
						if (routingKeySet == null) {
							exec = FunctionService.onRegion(region);
						} else {
							exec = FunctionService.onRegion(region).withFilter(routingKeySet);
						}
						execPairs[execPairIndex++] = new ExecutionPair(gridIds[i], exec);
					}
				}
			}

			// Remove all null pairs
			if (execPairIndex < execPairs.length) {
				ExecutionPair tempExecPairs[] = execPairs;
				execPairs = new ExecutionPair[execPairIndex];
				System.arraycopy(tempExecPairs, 0, execPairs, 0, execPairs.length);
			}
			try {
				return doInvocationMany(execPairs, method, args);
			} catch (Exception ex) {
				String gridIdList = "[";
				for (int i = 0; i < execPairs.length; i++) {
					if (i < execPairs.length - 1) {
						gridIdList += execPairs[i].gridId + ",";
					} else {
						gridIdList += execPairs[i].gridId;
					}
				}
				gridIdList += "]";
				String regionPathList = "[";
				for (int i = 0; i < execPairs.length; i++) {
					if (i < execPairs.length - 1) {
						regionPathList += getGridRegion(execPairs[i].gridId) + ",";
					} else {
						regionPathList += getGridRegion(execPairs[i].gridId);
					}
				}
				regionPathList += "]";
				throw new BizException("Exception occurred while executing \"" + method.toString() + "\" for gridIds=" + gridIdList + ", regionPaths=" + regionPathList + ".", ex);
			}

		}
	}
	
	public Object invoke_old(Object proxy, Method method, Object[] args) throws Throwable
	{

		String gridIds[] = bizContext.getGridContextClient().getGridIds();

		if (gridIds == null || gridIds.length == 0) {
			Region region = this.region;
			String gridId = null;
			if (region == null) {
				AppInfo appInfo = getAppInfo();
				gridId = getGridId(method);
				region = getGridRegion(gridId);
			}
			if (region == null) {
				throw new BizException("Region is not defined. Aborted.");
			}
			// add the data dependant routing information to the execution
			// context
			Execution primordialExec = null;
			if (filterset == null) {
				primordialExec = FunctionService.onRegion(region);
			} else {
				primordialExec = FunctionService.onRegion(region).withFilter(filterset);
			}

			return doInvocation(gridId, primordialExec, method, args);

		} else {

			for (int i = 0; i < gridIds.length; i++) {
				if (gridIds[i] == null) {
					throw new BizException("Grid ID (" + i
							+ ") is null. Must pass in valid grid IDs. IBiz invocation aborted.");
				}
			}

			ExecutionPair execPairs[] = new ExecutionPair[gridIds.length];
			Execution exec;
			int execPairIndex = 0;
			for (int i = 0; i < gridIds.length; i++) {
				Region region = getGridRegion(gridIds[i]);
				if (region != null) {
					if (filterset == null) {
						exec = FunctionService.onRegion(region);
					} else {
						exec = FunctionService.onRegion(region).withFilter(filterset);
					}
					execPairs[execPairIndex++] = new ExecutionPair(gridIds[i], exec);
				}
			}

			// Remove all null pairs
			if (execPairIndex < execPairs.length) {
				ExecutionPair tempExecPairs[] = execPairs;
				execPairs = new ExecutionPair[execPairIndex];
				System.arraycopy(tempExecPairs, 0, execPairs, 0, execPairs.length);
			}
			return doInvocationMany(execPairs, method, args);

		}
	}

	protected Region getGridRegion(String gridId)
	{
		if (region == null) {
			return null;
		}
		String rootRegionPath = ((GemfireAppGridInfo)getAppInfo().getAppGridInfo(gridId)).getGridRootPath();
		if (rootRegionPath == null) {
			return null;
		}
		String path = region.getFullPath();
		path = path.replaceFirst("/.*?/", rootRegionPath + "/");
		RegionService regionService = ((GemfireGridService)bizContext.getGridService()).getRegionService(gridId);
		if (regionService != null) {
			return regionService.getRegion(path);
		} else {
			return CacheFactory.getAnyInstance().getRegion(path);
		}
	}
}
