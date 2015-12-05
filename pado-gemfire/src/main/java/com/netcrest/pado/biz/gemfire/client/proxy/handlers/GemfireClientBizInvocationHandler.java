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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IGridCollector;
import com.netcrest.pado.IGridRouter;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.BizType;
import com.netcrest.pado.annotation.OnPath;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.annotation.WithGridCollector;
import com.netcrest.pado.biz.IBizStatistics;
import com.netcrest.pado.biz.gemfire.BizStatisticsManager;
import com.netcrest.pado.biz.gemfire.annotation.WithResultCollector;
import com.netcrest.pado.biz.gemfire.client.proxy.functionrouters.FunctionRouter;
import com.netcrest.pado.biz.gemfire.client.proxy.functionrouters.OnRegionFunctionRouter;
import com.netcrest.pado.biz.gemfire.client.proxy.functionrouters.OnServerFunctionRouter;
import com.netcrest.pado.biz.gemfire.proxy.GemfireBizUtil;
import com.netcrest.pado.biz.gemfire.proxy.functions.BizArguments;
import com.netcrest.pado.biz.gemfire.proxy.functions.BizContextClientImpl;
import com.netcrest.pado.exception.BizException;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.info.AppInfo;
import com.netcrest.pado.internal.impl.GridService;
import com.netcrest.pado.internal.impl.PadoClientManager;

/**
 * Abstract base class for Gemfire IBiz invocation handlers.
 * 
 */
public abstract class GemfireClientBizInvocationHandler<T>
{

	protected Class<T> targetClass;
	protected Class<T> futureClass;

	protected ConcurrentHashMap<String, MethodAnnotationEntry> methodAnnotationCache;
	private ResultCollector rcInstance = null;

	protected IBizContextClient bizContext;
	
	protected IBizStatistics bizStats;

	/**
	 * biz object level BizType. Applies to all methods unless overridden by
	 * individual methods.
	 */
	protected BizType bizType = BizType.DEFAULT;
	protected String defaultGridPath;

	private static LogWriter logger = CacheFactory.getAnyInstance().getLogger();

	protected class MethodAnnotationEntry
	{
		final Annotation methodAnnotation;
		final Annotation routingAnnotation;
		final Annotation collectorAnnotation;
		final Annotation gridCollectorAnnotation;
		final FunctionRouter functionRouter;
		final BizType bizType;

		public MethodAnnotationEntry(BizMethod methodAnnotation, Annotation routingAnnotation,
				Annotation collectorAnnotation, Annotation gridCollectorAnnotation) throws ClassNotFoundException,
				InstantiationException, IllegalAccessException
		{
			super();
			this.methodAnnotation = methodAnnotation;
			this.collectorAnnotation = collectorAnnotation;
			this.gridCollectorAnnotation = gridCollectorAnnotation;
			this.routingAnnotation = routingAnnotation;

			// Supports OnPath and OnServer
			if (routingAnnotation.annotationType() == OnPath.class) {
				functionRouter = new OnRegionFunctionRouter();
			} else {
				functionRouter = new OnServerFunctionRouter();
			}

			if (methodAnnotation != null && methodAnnotation.bizType() != BizType.DEFAULT) {
				bizType = methodAnnotation.bizType();
			} else {
				bizType = GemfireClientBizInvocationHandler.this.bizType;
			}
		}
	}

	protected AppInfo getAppInfo()
	{
		if (bizContext.getGridService() == null) {
			return null;
		}
		return PadoClientManager.getPadoClientManager().getAppInfo(bizContext.getGridService().getAppId());
	}

	public String getDefaultGridPath()
	{
		return defaultGridPath;
	}

	/**
	 * @param targetClass
	 *            The target service class
	 */
	public GemfireClientBizInvocationHandler(final Class<T> targetClass, GridService gridService)
	{
		try {
			if (GemfireBizUtil.isFuture(targetClass)) {
				this.futureClass = targetClass;
				this.targetClass = (Class<T>) Class.forName(GemfireBizUtil.getBizInterfaceName(this.futureClass));
			} else {
				this.targetClass = targetClass;
			}
		} catch (ClassNotFoundException ex) {
			throw new BizException(ex);
		}
		this.bizType = GemfireBizUtil.getBizType(this.targetClass);
		this.defaultGridPath = GemfireBizUtil.getPath(this.targetClass);
		this.bizContext = new BizContextClientImpl(gridService, bizStats);
		this.methodAnnotationCache = createAnnotationCache(this.targetClass);
		
		this.bizStats = BizStatisticsManager.getBizStatistics(targetClass, targetClass.getSimpleName(), "");
	}

	/**
	 * This method uses reflection on the service interface definition to
	 * determine the type of IBiz methods that have been configured. As it finds
	 * valid IBiz declarations it caches as much of the information as it can so
	 * to avoid further reflection at runtime
	 * 
	 * @param targetClass
	 *            Annotated service interface
	 * 
	 * @return Cached reflection info
	 */
	private ConcurrentHashMap<String, MethodAnnotationEntry> createAnnotationCache(final Class<T> targetClass)
	{
		ConcurrentHashMap<String, MethodAnnotationEntry> retval = new ConcurrentHashMap<String, MethodAnnotationEntry>();
		
		// add the routing info to the execution context
		Method methods[] = targetClass.getMethods();
		for (Method m : methods) {
			Annotation methodAnnotations[] = m.getAnnotations();
			Annotation forRouting = null;
			Annotation forCollector = null;
			Annotation forGridCollector = null;
			Annotation forMethod = null;
			for (Annotation a : methodAnnotations) {
				if (a.annotationType() == WithResultCollector.class) {
					forCollector = a;
				} else if (a.annotationType() == WithGridCollector.class) {
					forGridCollector = a;
				} else if (a.annotationType() == BizMethod.class) {
					forMethod = a;
				} else if (a.annotationType() == OnPath.class) {
					forRouting = a;
				} else if (a.annotationType() == OnServer.class) {
					forRouting = a;
				}
			}

			String functionId = GemfireBizUtil.getGemfireFunctionId(targetClass, m);
			if (functionId != null) {
				if (forRouting == null) {
					throw new PadoException("Routing annotation undeclared. @OnPath or @OnServer is mandatory. " + m);
				}
				try {
					retval.put(functionId, new MethodAnnotationEntry((BizMethod) forMethod, forRouting, forCollector,
							forGridCollector));
				} catch (ClassNotFoundException e) {
					throw new BizException(e);
				} catch (InstantiationException e) {
					throw new BizException(e);
				} catch (IllegalAccessException e) {
					throw new BizException(e);
				}
			}

		}

		return retval;
	}

	public Class<T> getTargetClass()
	{
		return targetClass;
	}

	/**
	 * This method configures a Gemfire Execution context with routing
	 * information from annotations that have been added to the invoked
	 * interface java source. This method is required whenever the routing
	 * meta-data is configured using annotations rather than specified the
	 * routing at runtime.
	 * 
	 * @param targetMethod
	 *            Method invoked
	 * 
	 * @return execution context reference configured with routing data
	 * 
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected Execution addInvocationRoutingFromAnnotations(final Method targetMethod, final String gridId,
			final Set routingKeySet, final Object[] args) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException
	{

		MethodAnnotationEntry methodAnnotations = methodAnnotationCache.get(GemfireBizUtil.getGemfireFunctionId(
				targetClass, targetMethod));

		if (methodAnnotations == null || methodAnnotations.routingAnnotation == null) {
			throw new BizException("Could not find routing annotations for gfe function ["
					+ GemfireBizUtil.getGemfireFunctionId(targetClass, targetMethod) + "]");
		}

		Execution exec = null;

		// add the routing info to the execution context
		Annotation a = methodAnnotations.routingAnnotation;

		exec = methodAnnotations.functionRouter.addRoutingContext(a, this, gridId, bizContext, routingKeySet, args);

		return exec;

	}

	protected boolean isOnPath(final Method targetMethod)
	{
		MethodAnnotationEntry methodAnnotations = methodAnnotationCache.get(GemfireBizUtil.getGemfireFunctionId(
				targetClass, targetMethod));
		if (methodAnnotations == null || methodAnnotations.routingAnnotation == null) {
			throw new BizException("Could not find routing annotations for gfe function ["
					+ GemfireBizUtil.getGemfireFunctionId(targetClass, targetMethod) + "]");
		}
		return methodAnnotations.routingAnnotation.annotationType() == OnPath.class;
	}

	/**
	 * Returns true if the specified target method is OnServer and the broadcast
	 * attribute is true. Otherwise, it returns false.
	 * 
	 * @param targetMethod
	 *            the target method
	 */
	protected boolean isOnServerBroadcast(final Method targetMethod)
	{
		MethodAnnotationEntry methodAnnotations = methodAnnotationCache.get(GemfireBizUtil.getGemfireFunctionId(
				targetClass, targetMethod));
		if (methodAnnotations == null || methodAnnotations.routingAnnotation == null) {
			throw new BizException("Could not find routing annotations for gfe function ["
					+ GemfireBizUtil.getGemfireFunctionId(targetClass, targetMethod) + "]");
		}
		if (methodAnnotations.routingAnnotation.annotationType() == OnServer.class) {
			OnServer os = (OnServer) methodAnnotations.routingAnnotation;
			return os.broadcast();
		} else {
			return false;
		}
	}

	protected String getGridPath(final Method targetMethod)
	{
		String gridPath = bizContext.getGridContextClient().getGridPath();
		if (gridPath != null) {
			return gridPath;
		}

		MethodAnnotationEntry methodAnnotations = methodAnnotationCache.get(GemfireBizUtil.getGemfireFunctionId(
				targetClass, targetMethod));

		if (methodAnnotations == null || methodAnnotations.routingAnnotation == null) {
			throw new BizException("Could not find routing annotations for GemFire function ["
					+ GemfireBizUtil.getGemfireFunctionId(targetClass, targetMethod) + "]");
		}
		if (methodAnnotations.routingAnnotation.annotationType() == OnPath.class) {
			if (gridPath == null) {
				OnPath or = (OnPath) methodAnnotations.routingAnnotation;
				gridPath = or.path();
			}
			if (gridPath == null || gridPath.trim().length() == 0) {
				gridPath = defaultGridPath;
			}
		}
		return gridPath;
	}
	
	protected String getGridPath(OnPath onPath)
	{
		String gridPath = bizContext.getGridContextClient().getGridPath();
		if (gridPath == null) {
			gridPath = onPath.path();
			if (gridPath == null || gridPath.trim().length() == 0) {
				gridPath = getDefaultGridPath();
			}
		}
		return gridPath;
	}

	/**
	 * Returns a map of &lt;gridId, &gt; that contains routing key sets mapped
	 * by gridIds. It returns null if routing keys are not specified via
	 * <code>Biz#getBizContext().getGridContextClient().setRoutingKyes()</code>.
	 * If it returns null then the caller should broadcast the onRegion call to
	 * all grids. The grid Ids can be obtained from
	 * {@link GridService#getGridIds(String)}.
	 * 
	 * @param targetMethod
	 */
	protected Map<String, Set> getGridRoutingKeyMap(final Method targetMethod)
	{
		Set routingKeySet = bizContext.getGridContextClient().getRoutingKeys();
		if (routingKeySet == null) {
			return null;
		}

		MethodAnnotationEntry methodAnnotations = methodAnnotationCache.get(GemfireBizUtil.getGemfireFunctionId(
				targetClass, targetMethod));

		if (methodAnnotations == null || methodAnnotations.routingAnnotation == null) {
			throw new BizException("Could not find routing annotations for gfe function ["
					+ GemfireBizUtil.getGemfireFunctionId(targetClass, targetMethod) + "]");
		}

		HashMap<String, Set> gridRoutingKeyMap = null;
		if (methodAnnotations.routingAnnotation.annotationType() == OnPath.class) {
			String regionPath = bizContext.getGridContextClient().getGridPath();
			if (regionPath == null) {
				OnPath or = (OnPath) methodAnnotations.routingAnnotation;
				regionPath = or.path();
			}
			IGridRouter gridRouter = bizContext.getGridService().getGridRouter(regionPath);

			if (gridRouter != null) {
				gridRoutingKeyMap = new HashMap(10);

				for (Object key : routingKeySet) {
					String gridId = gridRouter.getReachableGridIdForPath(bizContext, key);
					if (gridId != null) {
						Set keySet = gridRoutingKeyMap.get(gridId);
						if (keySet == null) {
							keySet = new HashSet<String>(10);
							gridRoutingKeyMap.put(gridId, keySet);
						}
						keySet.add(key);
					}
				}
			}
		}
		return gridRoutingKeyMap;
	}

	protected String getGridId(final Method targetMethod)
	{
		AppInfo appInfo = getAppInfo();
		if (appInfo != null && bizContext.getGridContextClient().isPadoAsTarget()) {
			return appInfo.getPadoId();
		}

		MethodAnnotationEntry methodAnnotations = methodAnnotationCache.get(GemfireBizUtil.getGemfireFunctionId(
				targetClass, targetMethod));
		if (methodAnnotations == null) {
			throw new BizException("Could not find method annotations for gfe function ["
					+ GemfireBizUtil.getGemfireFunctionId(targetClass, targetMethod) + "]");
		}

		BizType bizType = methodAnnotations.bizType;
		String gridId = null;
		if (appInfo != null) {
			switch (bizType) {
			case APP:
				gridId = appInfo.getDefaultGridId();
				break;
			case PADO:
				gridId = appInfo.getPadoId();
				break;
			case DEFAULT:
			default:
				gridId = appInfo.getDefaultGridId();
				break;
			}
		}
		return gridId;
	}

	/**
	 * Adds the service method arguments to the execution context
	 * 
	 * @param exec
	 *            The execution context
	 * @param args
	 *            The method arguments
	 * @throws IOException
	 */
	protected Execution addArgsToExecutionContext(Execution exec, final Object[] args) throws IOException
	{
		if (args != null && args.length > 0) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			DataSerializer.writeObjectArray(args, out);
			out.flush();
			return exec.withArgs(bos.toByteArray());
		}
		return exec;
	}

	/**
	 * This method checks if a specific result collector has been annotated
	 * against the invoked method - if so it modifies the execution context
	 * accordingly
	 * 
	 * @param exec
	 *            The execution context
	 * @param m
	 *            The method invoked
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	protected Execution addResultCollectorToExecutionContext(Execution exec, Method m) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException
	{
		rcInstance = createResultCollectorToExecutionContext(m);
		if (rcInstance != null) {
			exec = exec.withCollector(rcInstance);
		}
		return exec;
	}

	protected ResultCollector createResultCollectorToExecutionContext(Method m) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException
	{
		MethodAnnotationEntry methodAnnotations = methodAnnotationCache.get(GemfireBizUtil.getGemfireFunctionId(
				targetClass, m));

		// add the routing info to the execution context
		WithResultCollector rc = (WithResultCollector) methodAnnotations.collectorAnnotation;
		ResultCollector rcInstance = null;
		if (rc != null) {
			Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(rc.resultCollectorClass());
			rcInstance = (ResultCollector) c.newInstance();
		}
		return rcInstance;
	}

	protected IGridCollector createGridCollectorToExecutionContext(Method m) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException
	{
		MethodAnnotationEntry methodAnnotations = methodAnnotationCache.get(GemfireBizUtil.getGemfireFunctionId(
				targetClass, m));

		// add the routing info to the execution context
		WithGridCollector rc = (WithGridCollector) methodAnnotations.gridCollectorAnnotation;
		IGridCollector gridCollector = null;
		if (rc != null) {
			Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(rc.gridCollectorClass());
			gridCollector = (IGridCollector) c.newInstance();
		}
		return gridCollector;
	}

	/**
	 * Returns an array of empty result collectors if &#64;
	 * {@link WithResultCollector} is defined in the specified method. It
	 * returns null if &#64;{@link WithResultCollector} is not defined. The size
	 * of the array is equal to
	 * <code>bizContext.getGridContextClient().getGridIds() +
	 * 1</code>, including the primordial grid collector.
	 * 
	 * @param m
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected ResultCollector[] createManyResultCollectorToExecutionContext(Method m) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException
	{
		MethodAnnotationEntry methodAnnotations = methodAnnotationCache.get(GemfireBizUtil.getGemfireFunctionId(
				targetClass, m));
		WithResultCollector rc = (WithResultCollector) methodAnnotations.collectorAnnotation;
		if (rc == null) {
			return null;
		}

		// The primordial grid + other grids.
		String gridIds[] = bizContext.getGridContextClient().getGridIds();
		ResultCollector rcInstances[];
		if (gridIds != null) {
			rcInstances = new ResultCollector[gridIds.length + 1];
		} else {
			rcInstances = new ResultCollector[1];
		}

		for (ResultCollector rcInstance : rcInstances) {
			Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(rc.resultCollectorClass());
			rcInstance = (ResultCollector) c.newInstance();
		}

		return rcInstances;
	}

	/**
	 * Carries out the generic steps in making an Gemfire invocation
	 * 
	 * @param exec
	 * @param args
	 * @param method
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected Object doInvocation(String gridId, Execution exec, Method method, Object[] args) throws Throwable
	{
		Object retval = null;

		// add the args
		// exec = addArgsToExecutionContext(exec, args);
		exec = exec.withArgs(new BizArguments(GemfireBizUtil.getMethodName( method), bizContext, args,
				bizContext.getGridContextClient().getAttionalArguments(), bizContext.getGridContextClient().getTransientData()));

		// add the result collector. returned value is the instance that
		// collects results, if specified via @WithResultCollector.
		exec = addResultCollectorToExecutionContext(exec, method);

		// Add result arguments and collector to all Execution objects
		IGridCollector gridCollector = bizContext.getGridContextClient().getGridCollector();
		if (gridCollector == null) {
			gridCollector = createGridCollectorToExecutionContext(method);
		}

		// execute. Note that returned value is not same as rcInstance.
		// It is GemFire's internal collector that blocks on getResult().
		// Do not invoke rcInstance.getResult().
		try {

			ResultCollector rc = exec.execute(GemfireBizUtil.getGemfireFunctionId(targetClass));
			if (!method.getReturnType().equals(void.class)) {

				if (method.getReturnType().equals(Future.class)) {
					retval = new FutureWorker(rc, gridId, gridCollector, method);
				} else {

					// If result collector is not provided then
					// return the first item found in the list.
					// This assumes that most of IBiz calls return
					// a single value. It is IBiz developers' responsibility
					// to supply proper ResultCollector and/or IGridCollector.
					if (gridCollector == null) {
						if (rcInstance == null) {
							if (List.class.isAssignableFrom(method.getReturnType())) {
								retval = rc.getResult(); // list
							} else {
								List s = (List) rc.getResult();
								if (s != null && s.size() > 0) {
									retval = s.get(0);
								}
							}
						} else {
							retval = rc.getResult();
						}
					} else {
						gridCollector.addResult(gridId, rc.getResult());
						gridCollector.endResults();
						retval = gridCollector.getResult();
					}
				}
			}
			return retval;

		} catch (Throwable th) {

//			Class<?> exTypes[] = method.getExceptionTypes();
//			Throwable cause = th;
//			Throwable lastThrowable = th;
//			while (cause != null) {
//				lastThrowable = cause;
//				for (Class<?> exType : exTypes) {
//					if (exType == cause.getClass()) {
//						throw lastThrowable;
//					}
//				}
//				cause = cause.getCause();
//			}
//			throw lastThrowable;
			
			throw getCause(method, th);

		}
	}
	
	private Throwable getCause(Method method, Throwable th) throws Throwable
	{
		Class<?> exTypes[] = method.getExceptionTypes();
		Throwable cause = th;
		Throwable lastThrowable = th;
		while (cause != null) {
			lastThrowable = cause;
			for (Class<?> exType : exTypes) {
				if (exType == cause.getClass()) {
					RuntimeException ex = new RuntimeException();
					StackTraceElement ste[] = ex.getStackTrace(); 
					StackTraceElement causeSte[] = lastThrowable.getStackTrace();
					StackTraceElement aggregateSte[] = new StackTraceElement[ste.length + causeSte.length];
					System.arraycopy(causeSte, 0, aggregateSte, 0, causeSte.length);
					System.arraycopy(ste, 0, aggregateSte, causeSte.length, ste.length);
					lastThrowable.setStackTrace(aggregateSte);
					throw lastThrowable;
				}
			}
			cause = cause.getCause();
		}
		throw lastThrowable;
	}

	/**
	 * 
	 * @param execs
	 * @param method
	 * @param args
	 * @return
	 * @throws Exception
	 */
	protected Object doInvocationMany(ExecutionPair[] execPairs, Method method, Object[] args) throws Exception
	{
		Object retval = null;

		BizArguments bizArgs = new BizArguments(GemfireBizUtil.getMethodName(method), bizContext, args,
				bizContext.getGridContextClient().getAttionalArguments(), bizContext.getGridContextClient().getTransientData());

		// function id
		String functionId = GemfireBizUtil.getGemfireFunctionId(targetClass);

		// Add result arguments and collector to all Execution objects
		IGridCollector gridCollector = bizContext.getGridContextClient().getGridCollector();
		if (gridCollector == null) {
			gridCollector = createGridCollectorToExecutionContext(method);
		}
		FutureMany futureMany = null;
		if (method.getReturnType().equals(Future.class)) {
			futureMany = new FutureMany(gridCollector);
		}

		// If the return type is NOT Future then block till all threads
		// are complete.
		if (futureMany == null) {

			if (execPairs.length == 1) {
				Execution exec = execPairs[0].exec.withArgs(bizArgs);
				// add the result collector. returned value is the instance that
				// collects results, if specified via @WithResultCollector.
				ResultCollector clientRC = createResultCollectorToExecutionContext(method);
				if (clientRC != null) {
					exec.withCollector(clientRC);
				}
				ResultCollector rc = exec.execute(functionId);
				if (rc != null) {
					if (!method.getReturnType().equals(void.class)) {
						if (gridCollector == null) {
							if (clientRC == null) {
								List s = (List) rc.getResult();

								if (s != null && s.size() > 0) {
									retval = s.get(0);
								}
							} else {
								retval = rc.getResult();
							}
						} else {
							gridCollector.addResult(execPairs[0].gridId, rc.getResult());
							gridCollector.endResults();
							retval = gridCollector.getResult();
						}
					}
				}
			} else {
				for (int i = 0; i < execPairs.length; i++) {
					Execution exec = execPairs[i].exec.withArgs(bizArgs);
					// add the result collector. returned value is the instance
					// that collects results, if specified via
					// @WithResultCollector.
					ResultCollector clientRC = createResultCollectorToExecutionContext(method);
					if (clientRC != null) {
						exec.withCollector(clientRC);
					}
					ResultCollector rc = exec.execute(functionId);
					if (rc != null) {
						Object result = rc.getResult();
						if (gridCollector == null) {
							// Use the first result as the returned value if the
							// grid collector is not provided.
							if (i == 0) {
								retval = result;
							}
						} else {
							gridCollector.addResult(execPairs[i].gridId, result);
						}
					}
				}
				if (gridCollector != null) {
					gridCollector.endResults();
					retval = gridCollector.getResult();
				}
			}
		} else {

			for (ExecutionPair execPair : execPairs) {
				Execution exec = execPair.exec.withArgs(bizArgs);
				// add the result collector. returned value is the instance
				// that collects results, if specified via @WithResultCollector.
				ResultCollector clientRC = createResultCollectorToExecutionContext(method);
				if (clientRC != null) {
					exec.withCollector(clientRC);
				}
				ResultCollector rc = exec.execute(functionId);
				futureMany.addResultCollector(execPair.gridId, rc);
			}
			retval = futureMany;
		}

		return retval;
	}

	class ExecutionPair<T>
	{
		String gridId;
		Execution exec;

		ExecutionPair(String gridId, Execution exec)
		{
			this.gridId = gridId;
			this.exec = exec;
		}
	}

	class FutureMany implements Future
	{
		Map<String, ResultCollector> resultCollectorMap;
		IGridCollector gridCollector;
		boolean isDone = false;

		FutureMany(IGridCollector gridCollector)
		{
			this.resultCollectorMap = new HashMap<String, ResultCollector>(4);
			this.gridCollector = gridCollector;
		}

		void addResultCollector(String gridId, ResultCollector rc)
		{
			resultCollectorMap.put(gridId, rc);
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning)
		{
			return false;
		}

		@Override
		public boolean isCancelled()
		{
			return false;
		}

		@Override
		public boolean isDone()
		{
			return isDone;
		}

		@Override
		public Object get() throws InterruptedException, ExecutionException
		{
			try {
				Object retval = null;
				int i = 0;
				for (Map.Entry<String, ResultCollector> entry : resultCollectorMap.entrySet()) {
					String gridId = entry.getKey();
					ResultCollector rc = entry.getValue();
					Object result = rc.getResult();
					if (gridCollector == null) {
						// Use the first result as the returned value if the
						// grid collector is not provided.
						if (i == 0) {
							retval = result;
						}
						i++;
					} else {
						gridCollector.addResult(gridId, result);
					}
				}
				if (gridCollector != null) {
					gridCollector.endResults();
					retval = gridCollector.getResult();
				}
				isDone = true;
				return retval;
			} catch (FunctionException ex) {
				throw new ExecutionException(ex);
			} catch (Exception ex) {
				throw new BizException(ex);
			}
		}

		@Override
		public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
				TimeoutException
		{
			try {
				Object retval = null;
				int i = 0;
				for (Map.Entry<String, ResultCollector> entry : resultCollectorMap.entrySet()) {
					String gridId = entry.getKey();
					ResultCollector rc = entry.getValue();
					Object result = rc.getResult(timeout, unit);
					if (gridCollector == null) {
						// Use the first result as the returned value if the
						// grid collector is not provided.
						if (i == 0) {
							retval = result;
						}
						i++;
					} else {
						gridCollector.addResult(gridId, result);
					}
				}
				if (gridCollector != null) {
					gridCollector.endResults();
					retval = gridCollector.getResult();
				}
				isDone = true;
				return retval;
			} catch (FunctionException ex) {
				throw new ExecutionException(ex);
			} catch (Exception ex) {
				throw new BizException(ex);
			}
		}
	}

	class FutureWorker implements Future
	{
		ResultCollector rc;
		boolean isDone = false;
		String gridId;
		IGridCollector gridCollector;
		Method ibizMethod;

		public FutureWorker(ResultCollector rc, String gridId, IGridCollector gridCollector, Method ibizMethod)
		{
			this.rc = rc;
			this.gridId = gridId;
			this.gridCollector = gridCollector;
			this.ibizMethod = ibizMethod;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning)
		{
			return false;
		}

		@Override
		public boolean isCancelled()
		{
			return false;
		}

		@Override
		public boolean isDone()
		{
			return isDone;
		}
		
		private boolean isReturnTypeList()
		{
			boolean isList = false;
			
			Type type = ibizMethod.getGenericReturnType();
			if (type instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType)type;
				Type[] types = pt.getActualTypeArguments();
				if (types.length > 0) {
					ParameterizedType ptype = (ParameterizedType)types[0];
					Type raw = ptype.getRawType();
					if (List.class.isAssignableFrom((Class)raw)) {
						isList = true;
					}
				}
			}
			return isList;
		}

		@Override
		public Object get() throws InterruptedException, ExecutionException
		{
			try {
				Object retval = null;
				if (gridCollector == null) {
					if (rcInstance == null) {
						if (isReturnTypeList()) {
							retval = rc.getResult(); // list
						} else {
							List s = (List) rc.getResult();
							if (s != null && s.size() > 0) {
								retval = s.get(0);
							}
						}
					} else {
						retval = rc.getResult();
					}
				} else {
					gridCollector.addResult(gridId, rc.getResult());
					gridCollector.endResults();
					retval = gridCollector.getResult();
				}
				isDone = true;
				return retval;
			} catch (FunctionException ex) {
				throw new ExecutionException(ex);
			} catch (Exception ex) {
				throw new BizException(ex);
			}
		}

		@Override
		public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
				TimeoutException
		{
			try {
				Object retval = null;
				if (gridCollector == null) {
					if (rcInstance == null) {
						if (isReturnTypeList()) {
							retval = rc.getResult(); // list
						} else {
							List s = (List) rc.getResult(timeout, unit);
							if (s != null && s.size() > 0) {
								retval = s.get(0);
							}
						}
					} else {
						retval = rc.getResult();
					}
				} else {
					gridCollector.addResult(gridId, rc.getResult());
					gridCollector.endResults();
					retval = gridCollector.getResult();
				}
				isDone = true;
				return retval;
			} catch (FunctionException ex) {
				throw new ExecutionException(ex);
			} catch (Exception ex) {
				throw new BizException(ex);
			}
		}
	}
}
