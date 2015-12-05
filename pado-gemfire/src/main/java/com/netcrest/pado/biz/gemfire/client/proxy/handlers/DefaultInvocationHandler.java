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
import java.util.Map;
import java.util.Set;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionService;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.client.PoolManager;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IGridRouter;
import com.netcrest.pado.annotation.BizType;
import com.netcrest.pado.annotation.OnPath;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.annotation.RouterType;
import com.netcrest.pado.biz.gemfire.proxy.GemfireBizUtil;
import com.netcrest.pado.exception.BizException;
import com.netcrest.pado.gemfire.GemfireGridService;
import com.netcrest.pado.info.AppGridInfo;
import com.netcrest.pado.info.AppInfo;
import com.netcrest.pado.internal.impl.GridService;

/**
 * This class implements the default runtime invocation handler behavior which
 * expects to receive all invocation meta-data from annotations.
 * 
 */
public class DefaultInvocationHandler<T> extends GemfireClientBizInvocationHandler<T> implements InvocationHandler
{
	
	/**
	 * @param targetClass
	 *            Domain interface class
	 */
	public DefaultInvocationHandler(Class<T> targetClass, GridService gridService)
	{
		super(targetClass, gridService);
	}

	/*
	 * Performs invocation using the Gemfire Function Execution framework
	 * Configuration of the invocation (e.g. routing of request) is performed
	 * using annotations attached to the service interface class
	 * 
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
	 * java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		if (method.getName().equals("getBizContext") && method.getReturnType() == IBizContextClient.class
				&& method.getParameterAnnotations().length == 0) 
		{
			return bizContext;
		}
		
		String functionId = GemfireBizUtil.getGemfireFunctionId(targetClass, method);
		if (functionId == null) {
			throw new BizException("Non-IBiz method invoked. Make sure the method is annotated with @BizMethod and implmented in the server side: " + method);
		}
		MethodAnnotationEntry methodAnnotations = methodAnnotationCache.get(functionId);
		if (methodAnnotations == null || methodAnnotations.routingAnnotation == null) {
			throw new BizException("Could not find routing annotations for gfe function ["
					+ GemfireBizUtil.getGemfireFunctionId(targetClass, method) + "]");
		}
		
		long startTime = 0;
		if (bizStats.isLocal() == false) {
			startTime = bizStats.startCount(method.getName());
		}
		Object retval;
		if (methodAnnotations.routingAnnotation.annotationType() == OnPath.class) {
			// OnPath
			retval = invokeOnRegion((OnPath)methodAnnotations.routingAnnotation, methodAnnotations, method, args);
			
		} else {
			// OnServer
			retval = invokeOnServer((OnServer)methodAnnotations.routingAnnotation, methodAnnotations, method, args);
		}
		if (bizStats.isLocal() == false) {
			bizStats.endCount(method.getName(), startTime);
		}
		return retval;
	}
	
	private Object invokeOnRegion(OnPath onPath, MethodAnnotationEntry methodAnnotations, Method targetMethod, Object[] args) throws Throwable
	{
		GemfireGridService gridService = (GemfireGridService)bizContext.getGridService();
		String gridIds[] = bizContext.getGridContextClient().getGridIds();
		AppInfo appInfo = null;
		Map<String, Set> gridRoutingKeyMap = null;
		
		if (gridIds != null) {

			appInfo = getAppInfo();
			validateGridIds(appInfo, gridIds);
			
		} else {
			
			// Determine the target grids from IGridRouter if it exists. For OnRegion only.
			Set routingKeySet = bizContext.getGridContextClient().getRoutingKeys();
			// if routingKeySet is not defined then it's a broadcast
			if (routingKeySet != null) {
				String regionPath = bizContext.getGridContextClient().getGridPath();
				if (regionPath == null) {
					regionPath = onPath.path();
				}
				IGridRouter gridRouter = gridService.getGridRouter(regionPath);
	
				if (gridRouter != null) {
					gridRoutingKeyMap = new HashMap(10);
	
					for (Object key : routingKeySet) {
						String gridId = gridRouter.getReachableGridIdForPath(bizContext, key);
						if (gridId != null) {
							Set keySet = gridRoutingKeyMap.get(gridId);
							if (keySet == null) {
								keySet = new HashSet<String>(routingKeySet.size(), 1f);
								gridRoutingKeyMap.put(gridId, keySet);
							}
							keySet.add(key);
						}
					}
					gridIds = gridRoutingKeyMap.keySet().toArray(new String[gridRoutingKeyMap.keySet().size()]);
				}
			}
		}
		
		// This routine is invoked only if the routing keys are not set for
		// OnPath. In that case, it's a broadcast call.
		String gridPath = getGridPath(onPath);
		if (gridIds == null || gridIds.length == 0) {
			if (gridPath != null) {
				gridIds = gridService.getGridIds(gridPath);
			}
		}
		
		// Do one grid
		if (gridIds == null || gridIds.length <= 1) {
			String gridId;
			if (gridIds == null || gridIds.length == 0) {
				gridId = getGridId(appInfo, methodAnnotations);
			} else {
				gridId = gridIds[0];
			}
			
			Execution exec = createOnRegionExecution(gridPath, gridId, gridService, targetMethod, 
					bizContext.getGridContextClient().getRoutingKeys());
			
			try {
				return doInvocation(gridId, exec, targetMethod, args);
			} catch (Throwable th) {
				throw new BizException("Exception occurred while executing \"" + targetMethod.toString() + "\" for gridId=" + gridId + ".", th);
			}
			
		} 
		
		// Do many grids
		ExecutionPair execPairs[] = new ExecutionPair[gridIds.length];
		if (gridRoutingKeyMap != null) {
			for (int i = 0; i < execPairs.length; i++) {
				String gridId = gridIds[i];
				Set routingKeySet = gridRoutingKeyMap.get(gridId);
				Execution exec = createOnRegionExecution(gridPath, gridId, gridService, targetMethod, 
						routingKeySet);
				execPairs[i] = new ExecutionPair(gridIds[i], exec);
			}
		} else {
			// routingKeySet should be null to reach this routine. gridRoutingKeyMap should
			// not be null. It is null only if IGridRouter is not defined. In that case,
			// this routine includes all routing keys per grid. Note that if routing keys
			// are not defined then it broadcasts OnRegion.
			Set routingKeySet = bizContext.getGridContextClient().getRoutingKeys();
			for (int i = 0; i < execPairs.length; i++) {
				String gridId = gridIds[i];
				Execution exec = createOnRegionExecution(gridPath, gridId, gridService, targetMethod, 
						routingKeySet);
				execPairs[i] = new ExecutionPair(gridIds[i], exec);
			}
		}
		
		try {
			return doInvocationMany(execPairs, targetMethod, args);
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
			throw new BizException("Exception occurred while executing \"" + targetMethod.toString() + "\" for gridIds=" + gridIdList + ".", ex);
		}
	}
	
	private Execution createOnRegionExecution(String gridPath, String gridId, GemfireGridService gridService, Method targetMethod, Set routingKeySet) throws BizException
	{	
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
			throw new BizException("Region undefined: gridId=" + gridId + ", path=" + gridPath);
		}

		// Create Execution
		Execution exec = FunctionService.onRegion(region);
		
		if (routingKeySet != null) {
			exec = exec.withFilter(routingKeySet);
		}
		
		return exec;
	}
	
	private Object invokeOnServer(OnServer onServer, MethodAnnotationEntry methodAnnotations, Method targetMethod, Object[] args) throws Throwable
	{
		GemfireGridService gridService = (GemfireGridService)bizContext.getGridService();
		String gridIds[] = bizContext.getGridContextClient().getGridIds();
		AppInfo appInfo = null;
		if (gridIds != null && gridIds.length > 0) {
			appInfo = getAppInfo();
			validateGridIds(appInfo, gridIds);
		} else {
			if (onServer.broadcastGrids()) {
				gridIds = gridService.getGridIds();
			}
		}
		
		RouterType routerType = bizContext.getGridContextClient().getRouterType();
		if (routerType == null) {
			routerType = onServer.routerType();
		}

		if (routerType == RouterType.ALL || onServer.broadcastGrids())  {
			
			if (gridIds == null || gridIds.length <= 1) {
				
				// Do one grid
				String gridId = null;
				if (gridIds == null || gridIds.length == 0) {
					if (gridService.isPureClient()) {
						gridId = getGridId(appInfo, methodAnnotations);
					}
				} else {
					gridId = gridIds[0];
				}
				if (gridId == null && methodAnnotations.bizType != BizType.PADO) {
					gridId = gridService.getGridId();
				}
				
				Execution exec = createOnServerExecution(onServer, gridId, gridService, targetMethod);
				
				try {
					return doInvocation(gridId, exec, targetMethod, args);
				} catch (Throwable th) {
//					if (th instanceof PadoException) {
//						throw th;
//					}
//					throw new BizException("Exception occurred while executing \"" + targetMethod.toString() + "\" for gridId=" + gridId + ".", th);
					throw th;
				}
				
			} else {
	
				// Do many grids
				ExecutionPair execPairs[] = new ExecutionPair[gridIds.length];
				for (int i = 0; i < execPairs.length; i++) {
					String gridId = gridIds[i];
					Execution exec = createOnServerExecution(onServer, gridId, gridService, targetMethod);
					execPairs[i] = new ExecutionPair(gridId, exec);
				}
				
				try {
					return doInvocationMany(execPairs, targetMethod, args);
				} catch (FunctionException ex) {
					throw ex.getCause();
				} catch (Throwable th) {
//					String gridIdList = "[";
//					for (int i = 0; i < execPairs.length; i++) {
//						if (i < execPairs.length - 1) {
//							gridIdList += execPairs[i].gridId + ",";
//						} else {
//							gridIdList += execPairs[i].gridId;
//						}
//					}
//					gridIdList += "]";
//					throw new BizException("Exception occurred while executing \"" + targetMethod.toString() + "\" for gridIds=" + gridIdList + ".", ex);
					throw th;
				}
			}
			
		} else {
			
			// Do one grid, determine the grid with the router type
			String gridId = null;
			if (methodAnnotations.bizType != BizType.PADO) {
				IGridRouter gridRouter = gridService.getGridRouter(routerType);
				if (gridRouter != null) {
					gridId = gridRouter.getGridIdForNode(bizContext, gridIds);
				}
				if (gridId == null) {
					gridId = gridService.getGridId();
				}
			}
			Execution exec = createOnServerExecution(onServer, gridId, gridService, targetMethod);
			
			try {
				return doInvocation(gridId, exec, targetMethod, args);
			} catch (FunctionException ex) {
				throw ex.getCause();
			} catch (Throwable th) {
				throw th;
//				throw new BizException("Exception occurred while executing \"" + targetMethod.toString() + "\" for gridId=" + gridId + ".", th);
			}
			
		}
	}
	
	private String getGridId(AppInfo appInfo, MethodAnnotationEntry methodAnnotations)
	{
		if (appInfo == null) {
			appInfo = getAppInfo();
			if (appInfo == null) {
				return null;
			}
		}
		switch (methodAnnotations.bizType) {
		case APP:
			return appInfo.getDefaultGridId();
		case PADO:
			return appInfo.getPadoId();
		case DEFAULT:
		default:
			return appInfo.getDefaultGridId();
		}
	}
	
	private Execution createOnServerExecution(OnServer onServer, String gridId, GemfireGridService gridService, Method targetMethod) throws BizException
	{
		Execution exec;
		if (gridService.isGridLocal(gridId)) {
			
			// Use OnMember(s) if this VM is a grid
			
			if (onServer.broadcast()) {
				// invoke all members
				exec = FunctionService.onMembers(CacheFactory.getAnyInstance().getDistributedSystem());
			} else {
				// invoke locally
				Set<DistributedMember> set = (Set<DistributedMember>)bizContext.getGridContextClient().getProductSpecificData();
				if (set == null) {
					exec = FunctionService.onMember(CacheFactory.getAnyInstance().getDistributedSystem(),  
							CacheFactory.getAnyInstance().getDistributedSystem().getDistributedMember());
				} else {
					exec = FunctionService.onMembers(CacheFactory.getAnyInstance().getDistributedSystem(), set);
				}
			}
			
		} else {
			
			// Use onServer(s) for all grids if this VM is a pure client
			
			RegionService regionService = gridService.getRegionService(gridId);
			Pool pool = null;
			if (regionService == null) {
				pool = gridService.getPool(gridId); 
				if (pool == null) {
					pool = PoolManager.find(onServer.connectionName());
					if (pool != null) {
						gridService.putPool(gridId, pool);
						// get regionService again in case it is set by gridService for the new pool
						regionService = gridService.getRegionService(gridId);
					} else {
						throw new BizException("Undefined RegionService or Pool. Unable to execute onServer(s) for " 
									+ targetMethod.toString() + ".");
					}
				}
			}
			if (regionService != null) {
				if (onServer.broadcast()) {
					exec = FunctionService.onServers(regionService);
				} else {
					exec = FunctionService.onServer(regionService);
				}
			} else {
				if (onServer.broadcast()) {
					exec = FunctionService.onServers(pool);
				} else {
					exec = FunctionService.onServer(pool);
				}
			}
		}
		return exec;
	}

	/**
	 * Validates the specified grids IDs. It throws a BizException if invalid
	 * the specified grid ID array contains an invalid ID. Note that a null or empty array
	 * is valid.
	 * @param appInfo
	 * @param gridIds
	 * @throws BizException
	 */
	private void validateGridIds(AppInfo appInfo, String gridIds[]) throws BizException
	{
		if (gridIds == null) {
			return;
		}
		for (int i = 0; i < gridIds.length; i++) {
			if (gridIds[i] == null) {
				throw new BizException("DefaultInvocationHandler.validateGridIds(): IBiz.getBizContext().getGridContextClient().getIGridIds()[" + i
						+ "] is null. Must pass in valid grid IDs. IBiz invocation aborted.");
			}
			if (appInfo == null) {
				throw new BizException("DefaultInvocationHandler.validateGridIds(): AppInfo undefined. Biz.getBizContext().getGridContextClient().getIGridIds()[" + i
						+ "]=" + gridIds[i] + ". IBiz invocation aborted.");
			}
			AppGridInfo appGridInfo = appInfo.getAppGridInfo(gridIds[i]);
			if (appGridInfo == null) {
				throw new BizException("DefaultInvocationHandler.validateGridIds(): AppGridInfo undefined. Biz.getBizContext().getGridContextClient().getIGridIds()[" + i
						+ "]=" + gridIds[i] + ". IBiz invocation aborted.");
			}
		}
	}
	
	public Object invoke_old2(Object proxy, Method method, Object[] args) throws Throwable
	{
		Method targetMethod = getTargetClass().getMethod(method.getName(), method.getParameterTypes());

		if (targetMethod.getName().equals("getBizContext") && targetMethod.getReturnType() == IBizContextClient.class
				&& method.getParameterAnnotations().length == 0) 
		{
			return bizContext;
		}

		// Search order of grid IDs:
		// 1. IBiz.getBizContext().getGridContextClient().getIGridIds() - user supplied.
		//    For OnRegion and OnServer.
		//    For OnRegion, note that this overrides IGridRouter such that it invokes all grids
		//    returned by this method call. This means routing keys will not
		//    be filtered at the grid level. Each grid will receive all routing keys. 
		// 2. getGridRoutingKeyMap() - IGridRouter determined gridIds. For OnRegion only.
		//    It uses IBiz.getBizContext().getGridContextClient().getRoutingKeys()
		//    to determine grid IDs. If this method returns null, then it is a broadcast (see #3 below).
		// 3. IBiz.getBizContext().getGridService().getGridIds(relativePath) - For OnRegion only.
		//    It returns all grids that contain the region path. Remote method is invoked on
		//    all of the grids it returns. It's a broadcast. This is the last step for OnRegion.
		// 4. getGridId() - Grid ID provided by AppInfo. If all else fails, then the AppInfo
		//    provided grid ID is used. For OnServer only. This is the last step for OnServer. 
		Map<String, Set> gridRoutingKeyMap = null;
		String gridIds[] = bizContext.getGridContextClient().getGridIds();
		if (gridIds != null) {
			if (gridIds.length > 0) {
				AppInfo appInfo = getAppInfo();
				for (int i = 0; i < gridIds.length; i++) {
					if (gridIds[i] == null) {
						throw new BizException("IBiz.getBizContext().getGridContextClient().getIGridIds()[" + i
								+ "] is null. Must pass in valid grid IDs. IBiz invocation aborted.");
					}
					AppGridInfo appGridInfo = appInfo.getAppGridInfo(gridIds[i]);
					if (appGridInfo == null) {
						throw new BizException("Undefined grid: Biz.getBizContext().getGridContextClient().getIGridIds()[" + i
								+ "]=" + gridIds[i] + ". IBiz invocation aborted.");
					}
				}
			}
		} else {
			// Determine the target grids from IGridRouter if it exists. For OnRegion only.
			gridRoutingKeyMap = getGridRoutingKeyMap(targetMethod);
			if (gridRoutingKeyMap != null) {
				gridIds = gridRoutingKeyMap.keySet().toArray(new String[gridRoutingKeyMap.keySet().size()]);
			} 
		}
		
		if (gridIds == null) {
			// This routine is invoked only if the routing keys are not set for
			// OnRegion. In that case, it's a broadcast call.
			if (isOnPath(targetMethod)) {
				String relativePath = getGridPath(targetMethod);
				if (relativePath != null) {
					gridIds = bizContext.getGridService().getGridIds(relativePath);
				}
			}
		}

		if (gridIds == null || gridIds.length <= 1) {
			
			String gridId;
			if (gridIds == null || gridIds.length == 0) {
				gridId = getGridId(method);
			} else {
				gridId = gridIds[0];
			}
			
			// gridId == null allowed for onServer
			Execution primordialExec = addInvocationRoutingFromAnnotations(targetMethod, gridId, 
					bizContext.getGridContextClient().getRoutingKeys(), args);
			if (primordialExec == null) {
				throw new BizException("GemFire function execution not created due to misconfiguration: gridId=" + gridId + ". \"" + method.toString() + "\"");
			}
			try {
				return doInvocation(gridId, primordialExec, method, args);
			} catch (Throwable th) {
				throw new BizException("Exception occurred while executing \"" + method.toString() + "\" for gridId=" + gridId + ".", th);
			}

		} else {
			
			ExecutionPair execPairs[] = new ExecutionPair[gridIds.length];
			if (gridRoutingKeyMap != null) {
				for (int i = 0; i < execPairs.length; i++) {
					Set routingKeySet = gridRoutingKeyMap.get(gridIds[i]);
					execPairs[i] = new ExecutionPair(gridIds[i], addInvocationRoutingFromAnnotations(targetMethod,
							gridIds[i], routingKeySet, args));
				}
			} else {
				Set routingKeySet = bizContext.getGridContextClient().getRoutingKeys();
				for (int i = 0; i < execPairs.length; i++) {
					execPairs[i] = new ExecutionPair(gridIds[i], addInvocationRoutingFromAnnotations(targetMethod,
							gridIds[i], routingKeySet, args));
				}
			}
			
			try {
				return doInvocationMany(execPairs, targetMethod, args);
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
				throw new BizException("Exception occurred while executing \"" + targetMethod.toString() + "\" for gridIds=" + gridIdList + ".", ex);
			}

		}
	}
	
	public Object invoke_old(Object proxy, Method method, Object[] args) throws Throwable
	{
		Method targetMethod = getTargetClass().getMethod(method.getName(), method.getParameterTypes());

		if (targetMethod.getName().equals("getBizContext") && targetMethod.getReturnType() == IBizContextClient.class
				&& method.getParameterAnnotations().length == 0) 
		{
			return bizContext;
			
		} else {

			String gridIds[] = bizContext.getGridContextClient().getGridIds();
			AppInfo appInfo = getAppInfo();
			if (gridIds == null || gridIds.length == 0) {
				String gridId = getGridId(method);
				// gridId == null allowed
				Execution primordialExec = addInvocationRoutingFromAnnotations(targetMethod, gridId, null, args);
				return doInvocation(gridId, primordialExec, method, args);

			} else {
				for (int i = 0; i < gridIds.length; i++) {
					if (gridIds[i] == null) {
						throw new BizException("Grid ID (" + i
								+ ") is null. Must pass in valid grid IDs. IBiz invocation aborted.");
					}
				}
				ExecutionPair execPairs[] = new ExecutionPair[gridIds.length];
				for (int i = 0; i < execPairs.length; i++) {
					AppGridInfo appGridInfo = appInfo.getAppGridInfo(gridIds[i]);
					if (appGridInfo == null) {
						throw new BizException("Undefined grid: " + gridIds[i] + ". IBiz invocation aborted.");
					}
				}
				for (int i = 0; i < execPairs.length; i++) {
					execPairs[i] = new ExecutionPair(gridIds[i], addInvocationRoutingFromAnnotations(targetMethod,
							gridIds[i], null, args));
				}
				return doInvocationMany(execPairs, targetMethod, args);

			}
		}
	}

	
}
