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

import com.gemstone.gemfire.cache.RegionService;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.client.PoolManager;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.exception.BizException;
import com.netcrest.pado.gemfire.GemfireGridService;
import com.netcrest.pado.gemfire.info.GemfireAppGridInfo;
import com.netcrest.pado.internal.impl.GridService;

/**
 * Server Pool invocation handler for invocations on specific
 * server pool(s)
 */
public class ServerPoolInvocationHandler <T>
	extends GemfireClientBizInvocationHandler<T>
	implements InvocationHandler {

	private final Pool pool;
	private final boolean broadcast;

	/**
	 * @param targetClass 	The service interface
	 * @param pool			Server pool tha invocation should be routed towards
	 * @param broadcast 	True if invocation routed to all severs in pool, false if routed to single pool member
	 */
	public ServerPoolInvocationHandler(Class<T> targetClass, Pool pool, boolean broadcast, GridService gridService) {
		super(targetClass, gridService);
		this.pool = pool;
		this.broadcast = broadcast;
	}

	
	/* 
	 * Performs invocation routing based on the configured server pool(s) 
	 * Will make use of any additional annotations associated with the class 
	 * to tailor the invocation
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {

		if (method.getName().equals("getBizContext") && method.getReturnType() == IBizContextClient.class
				&& method.getParameterAnnotations().length == 0) 
		{
			return bizContext;
		}
		
		String gridIds[] = bizContext.getGridContextClient().getGridIds();
		if (gridIds == null || gridIds.length == 0) {
			
			String gridId = getGridId(method);
			RegionService regionService = null;
			if (bizContext.getGridService() != null) {
				regionService = ((GemfireGridService)bizContext.getGridService()).getRegionService(gridId);
			}
			Execution primordialExec;
			if (regionService == null) {
				// add the pool based routing context 
				Pool pool = this.pool;
				if (pool == null) {
					pool = getPool(gridId);
					if (pool == null) {
						throw new BizException("Pool is not defined. Aborted.");
					}
				}
				if (broadcast == false) {
					primordialExec = FunctionService.onServer (pool);
				} else {
					primordialExec = FunctionService.onServers(pool);
				}
			} else {
				if (broadcast == false) {
					primordialExec = FunctionService.onServer (regionService);
				} else {
					primordialExec = FunctionService.onServers(regionService);
				}
			}
			try {
				return doInvocation(gridId, primordialExec, method, args);
			} catch (Throwable th) {
				throw new BizException("Exception occurred while executing \"" + method.toString() + "\" for gridId=" + gridId + ".", th);
			}
			
		} else {
			
			for (int i = 0; i < gridIds.length; i++) {
				if (gridIds[i] == null) {
					throw new BizException("Grid ID (" + i + ") is null. Must pass in valid grid IDs. IBiz invocation aborted.");
				}
			}
			
			ExecutionPair execPairs[] = new ExecutionPair[gridIds.length];
			Execution exec;
			int execPairIndex = 0;
			for (int i = 0; i < gridIds.length; i++ ) {
				RegionService regionService = ((GemfireGridService)bizContext.getGridService()).getRegionService(gridIds[i]);
				if (regionService == null) {
					Pool pool = getPool(gridIds[i]);
					if (pool != null) {
						if (broadcast == false) {
							exec = FunctionService.onServer (pool);
						} else {
							exec = FunctionService.onServers(pool);
						}
						execPairs[execPairIndex++] = new ExecutionPair(gridIds[i], exec);
					}
				} else {
					if (broadcast == false) {
						exec = FunctionService.onServer (regionService);
					} else {
						exec = FunctionService.onServers(regionService);
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
				throw new BizException("Exception occurred while executing \"" + method.toString() + "\" for gridIds=" + gridIdList + ".", ex);
			}

		}
	}
	
	private Pool getPool(String gridId)
	{
		String poolName = ((GemfireAppGridInfo)getAppInfo().getAppGridInfo(gridId)).getClientConnectionName();
		if (poolName == null) {
			return null;
		}
		return PoolManager.find(poolName);
	}

}
